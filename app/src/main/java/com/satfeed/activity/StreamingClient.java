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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.util.ByteProcessor;
import io.reactivex.netty.channel.Connection;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import io.reactivex.netty.util.StringLineDecoder;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func0;
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
                        .addChannelHandlerLast("string_decoder", new Func0<ChannelHandler>() {
                            @Override
                            public ChannelHandler call() {
                                return new StringLineDecoder();
                            }
                        })
                        .createConnectionRequest()
                        .flatMap(new Func1<Connection<Object, Object>, Observable<?>>() {
                            @Override
                            public Observable<?> call(final Connection<Object, Object> connection) {
                                final TCPStateCounter tcpStateCounter = new TCPStateCounter();
                                return Observable.create(new Observable.OnSubscribe<Object>() {
                                    @Override
                                    public void call(final Subscriber<? super Object> subscriber) {
                                        connection.getInput().doOnNext(new Action1<Object>() {
                                            @Override
                                            public void call(Object o) {
                                                switch (tcpStateCounter.state) {
                                                    case CONNECTING:
                                                        // write out
                                                        tcpStateCounter.connected();
                                                        final Observable<byte[]> outBytes = getIdPacketUTF8(o.toString().substring(6), hailing_email);
                                                        connection.writeBytesAndFlushOnEach(outBytes).subscribe();
                                                        break;
                                                    case AUTHENTICATING:
                                                        tcpStateCounter.authenticated();
                                                        final String status = o.toString();
                                                        motherSubscriber.onNext("Status: " + status);
                                                        break;
                                                    case STREAMING:
                                                        final byte[] allBytes;
                                                        int allBytesLength = 0;
                                                        try {
                                                            allBytes = serialize(o);
                                                            allBytesLength = allBytes.length;
                                                        if (allBytesLength == 0)
                                                            motherSubscriber.onCompleted();
                                                        final byte[] lessBytes = new byte[allBytesLength - 12];
                                                        final ByteBuffer buffer = ByteBuffer.wrap(allBytes);
                                                        Log.d(FeedStreamerApplication.TAG, "lessBytes would be: " + Integer.toString(allBytesLength - 12));
                                                        Log.d(FeedStreamerApplication.TAG, "labeled length is: " + buffer.getInt(8));
                                                        ByteBuffer.wrap(allBytes, 12, allBytesLength - 12).get(lessBytes);
                                                        Log.d(FeedStreamerApplication.TAG, "at state is: " + audioTrack.getState());
                                                        final int write = audioTrack.write(lessBytes, 0, lessBytes.length);
                                                        Log.d(FeedStreamerApplication.TAG, "write is: " + write);
                                                        subscriber.onNext(true);
                                                        } catch (IOException e) {
                                                            motherSubscriber.onError(e);
                                                        }
                                                        break;
                                                    default:
                                                        subscriber.onCompleted();

                                                }
                                            }
                                        }).subscribe();
                                    }
                                });
                            }
                        }).subscribe();
//                        .flatMap(new Func1<Connection<ByteBuf, ByteBuf>, Observable<?>>() {
//                            @Override // For each connection
//                            public Observable<?> call(final Connection<ByteBuf, ByteBuf> connection) {
//                                return Observable.create(new Observable.OnSubscribe<Object>() {
//                                    @Override
//                                    public void call(Subscriber<? super Object> subscriber) {
//                                        final TCPStateCounter tcpStateCounter = new TCPStateCounter();
//                                        connection
//                                                .getInput()
//                                                .map(new Func1<ByteBuf, String>() {
//                                                    @Override
//                                                    public String call(ByteBuf in) {
//                                                        while (in.isReadable()) {
//                                                            int startIndex = in.readerIndex();
//                                                            int lastReadIndex = in.forEachByte(LINE_END_FINDER);
//                                                            switch (tcpStateCounter.state) {
//                                                                case STREAMING:
//                                                                    in.skipBytes(12);
//                                                                    Log.d(FeedStreamerApplication.TAG, "in is readable"+in.isReadable());
////                                                                    ByteBuf streamingBuf = in.readBytes(in.nioBufferCount());
////                                                                    String streamingLine = streamingBuf.toString(StandardCharsets.UTF_8);
////                                                                    final byte[] bytes = line.getBytes(StandardCharsets.UTF_8);
////                                                                    final int write = audioTrack.write(bytes, 0, bytes.length);
//                                                                    final int write = audioTrack.write(in.nioBuffer(), 0, AudioTrack.WRITE_BLOCKING);
//                                                                    Log.d(FeedStreamerApplication.TAG, "write equal to: " + Integer.toString(write));
////                                                                    Log.d(FeedStreamerApplication.TAG, "write equal to: " + Integer.toString(write) + ", stream size: " + Integer.toString(line.length()));
//                                                                    break;
//                                                                case AUTHENTICATING:
//                                                                    ByteBuf slice = in.readSlice(lastReadIndex - startIndex);
//                                                                    String line = slice.toString(StandardCharsets.UTF_8);
//                                                                    tcpStateCounter.authenticated();
//                                                                    Log.d(FeedStreamerApplication.TAG, "authenticating recieved: " + line);
//                                                                    motherSubscriber.onNext("Authenticated: " + line);
//                                                                    in.skipBytes(1);
//                                                                    break;
//                                                                case CONNECTING:
//                                                                    ByteBuf connectingSlice = in.readSlice(lastReadIndex - startIndex);
//                                                                    String connectingLine = connectingSlice.toString(StandardCharsets.UTF_8);
//                                                                    tcpStateCounter.connected();
//                                                                    Log.d(FeedStreamerApplication.TAG, "connecting recieved: " + connectingLine);
//                                                                    final Observable<byte[]> outBytes = getIdPacketUTF8(connectingLine.substring(6), hailing_email);
//                                                                    connection.writeBytesAndFlushOnEach(outBytes).take(1).subscribe();
//                                                                    in.skipBytes(1);
//                                                                    break;
//                                                                default:
//                                                                    motherSubscriber.onError(new Throwable("Poor StreamingClient state"));
//                                                                    break;
//                                                            }
//                                                        }
//                                                        Log.d(FeedStreamerApplication.TAG, "in is no longer readable");
//                                                        return "Done";
//                                                    }
//                                                })
//                                                .doOnNext(new Action1<String>() {
//                                                    @Override
//                                                    public void call(String s) {
//                                                        Log.d(FeedStreamerApplication.TAG, "onCompleted in stream");
//                                                        motherSubscriber.onNext("Stream completed");
////                                                        motherSubscriber.onCompleted();
//                                                    }
//                                                }).subscribe();
//
//                                    }
//                                });
//                            }
//                        }).take(1).subscribe();
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

    private static byte[] serialize(Object object) throws IOException {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream()) {
            try (ObjectOutputStream o = new ObjectOutputStream(b)) {
                o.writeObject(object);
            }
            return b.toByteArray();
        }
    }
}
