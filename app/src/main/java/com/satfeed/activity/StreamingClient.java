package com.satfeed.activity;

/*
 * Created by Andrew Brin on 4/18/2016.
 */

import android.app.Application;
import android.content.res.Resources;
import android.media.AudioTrack;
import android.support.annotation.NonNull;
import android.util.Log;

import com.satfeed.FeedStreamerApplication;
import com.satfeed.R;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.util.ByteProcessor;
import io.reactivex.netty.channel.Connection;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class StreamingClient {

    public enum TCPState {
        CONNECTING,
        AUTHENTICATING,
        STREAMING
    }

    // initial capacity for buffer
    public static final int INITIAL_CAPACITY = 256;

    //    private final ServiceComponent serviceComponent;
    private Application application;

    public StreamingClient(Application application) {
        this.application = application;
//        serviceComponent = ((FeedStreamerApplication) application).getServiceComponent();
    }

    private static final ByteProcessor LINE_END_FINDER = new ByteProcessor() {
        public static final char LF = 10;

        @Override
        public boolean process(byte value) throws Exception {
            char nextByte = (char) value;
            return LF != nextByte;
        }
    };

    public Observable<String> streamToTrack(final String hailing_email, final AudioTrack audioTrack) {
        // wrapping TcpClient in a mother observable b/c it won't take the assignment to io thread
        // TCPClient will return NetworkOnMainThreadError!
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> motherSubscriber) {
                TcpClient
                        .<ByteBuf, ByteBuf>newClient(new InetSocketAddress(
                                FeedStreamerApplication.ALIEN_SERVER,
                                Integer.parseInt(FeedStreamerApplication.STREAMING_PORT)))
                        .createConnectionRequest()
                        .flatMap(new Func1<Connection<ByteBuf, ByteBuf>, Observable<?>>() {
                            @Override // For each connection
                            public Observable<?> call(final Connection<ByteBuf, ByteBuf> connection) {
                                return Observable.create(new Observable.OnSubscribe<Object>() {
                                    @Override
                                    public void call(Subscriber<? super Object> subscriber) {
                                        final TCPStateCounter tcpStateCounter = new TCPStateCounter();
                                        connection
                                                .getInput()
                                                .map(new Func1<ByteBuf, String>() {
                                                    @Override
                                                    public String call(ByteBuf in) {
                                                        switch (tcpStateCounter.state) {
                                                            case STREAMING:
//                                                                    ByteBuf streamingBuf = in.readBytes(in.nioBufferCount());
//                                                                    String streamingLine = streamingBuf.toString(StandardCharsets.UTF_8);
//                                                                    final byte[] bytes = line.getBytes(StandardCharsets.UTF_8);
//                                                                    final int write = audioTrack.write(bytes, 0, bytes.length);
//                                                                    in.skipBytes(8);
                                                                Log.d(FeedStreamerApplication.TAG, "SEQ: " + Integer.toString(in.getInt(0)));
//                                                                Log.d(FeedStreamerApplication.TAG, "CHK: " + Integer.toString(in.getInt(4)));
                                                                final int purportedLength = in.getInt(8);
                                                                Log.d(FeedStreamerApplication.TAG, "LEN: " + Integer.toString(purportedLength));
                                                                ByteBuf streamingSlice = in.readSlice(purportedLength + 12);
                                                                final int capacity = streamingSlice.capacity();
                                                                Log.d(FeedStreamerApplication.TAG, "capacity equal to: " + Integer.toString(capacity));
                                                                final byte[] binner = new byte[purportedLength];
                                                                streamingSlice.getBytes(12, binner);
                                                                int write = audioTrack.write(binner, 0, binner.length);
                                                                Log.d(FeedStreamerApplication.TAG, "write equal to: " + Integer.toString(write));
                                                                break;
                                                            case AUTHENTICATING:
                                                                while (in.isReadable()) {
                                                                    int startIndex = in.readerIndex();
                                                                    int lastReadIndex = in.forEachByte(LINE_END_FINDER);
                                                                    ByteBuf slice = in.readSlice(lastReadIndex - startIndex);
                                                                    String line = slice.toString(StandardCharsets.UTF_8);
                                                                    tcpStateCounter.authenticated();
                                                                    motherSubscriber.onNext("Authenticated: " + line);
                                                                    in.skipBytes(1);
                                                                }
                                                                break;
                                                            case CONNECTING:
                                                                while (in.isReadable()) {
                                                                    int startIndex = in.readerIndex();
                                                                    int lastReadIndex = in.forEachByte(LINE_END_FINDER);
                                                                    ByteBuf slice = in.readSlice(lastReadIndex - startIndex);
                                                                    String line = slice.toString(StandardCharsets.UTF_8);
                                                                    tcpStateCounter.connected();
                                                                    Log.d(FeedStreamerApplication.TAG, "connecting recieved: " + line);
                                                                    final Observable<byte[]> outBytes = getIdPacketUTF8(line.substring(6), hailing_email);
                                                                    connection.writeBytesAndFlushOnEach(outBytes).take(1).subscribe();
                                                                    in.skipBytes(1);
                                                                }
                                                                break;
                                                            default:
                                                                motherSubscriber.onError(new Throwable("Poor StreamingClient state"));
                                                                break;
                                                        }
                                                        Log.d(FeedStreamerApplication.TAG, "in is no longer readable");
                                                        return "Done";
                                                    }
                                                })
                                                .subscribe();

                                    }
                                });
                            }
                        }).take(1).subscribe();
            }
        }).subscribeOn(Schedulers.io());
    }

    @NonNull
    private Observable<byte[]> getIdPacketUTF8(String identificationCode, String hailing_email) {
        final Resources resources = getResources();
        final String s = resources.getString(R.string.idpacket_part1) +
                identificationCode +
                resources.getString(R.string.idpacket_part2) +
                hailing_email +
                resources.getString(R.string.idpacket_part3);
        final byte[] b = s.getBytes(StandardCharsets.UTF_8);
        return Observable.just(b);
    }

    private Resources getResources() {
        return application.getResources();
    }

    private class TCPStateCounter {
        TCPState state;

        TCPStateCounter() {
            this.state = TCPState.CONNECTING;
        }

        public void connected() {
            state = TCPState.AUTHENTICATING;
        }

        public void authenticated() {
            state = TCPState.STREAMING;
        }
    }
}
