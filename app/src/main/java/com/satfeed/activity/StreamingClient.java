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
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import io.netty.buffer.ByteBuf;
import io.netty.util.ByteProcessor;
import io.reactivex.netty.channel.Connection;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

final public class StreamingClient {

    public enum TCPState {
        CONNECTING,
        AUTHENTICATING,
        STREAMING
    }

    private Application application;

    public StreamingClient(Application application) {
        this.application = application;
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
                                        final StreamingBufferHandler streamingBufferHandler = new StreamingBufferHandler();
                                        connection
                                                .getInput()
                                                .map(new Func1<ByteBuf, String>() {
                                                    @Override
                                                    public String call(ByteBuf in) {
                                                        switch (tcpStateCounter.state) {
                                                            case STREAMING:
                                                                while (in.isReadable()) {
                                                                    if (streamingBufferHandler.unreadPacketBytes == 0) {
                                                                        streamingBufferHandler.initializeToLength(in.getInt(8));
                                                                    }
                                                                    Log.d(FeedStreamerApplication.TAG, "LEN: " + Integer.toString(streamingBufferHandler.packetLength));
                                                                    while (streamingBufferHandler.unreadPacketBytes > 0 && in.writerIndex() > 0) {
                                                                        int copySize = Math.min(in.writerIndex() - in.readerIndex(), streamingBufferHandler.unreadPacketBytes);
                                                                        if (copySize == 0)
                                                                            Log.d(FeedStreamerApplication.TAG, "copy size is 0");
                                                                        final byte[] copyBytes = new byte[copySize];
                                                                        try {
                                                                            in.readBytes(copyBytes);
                                                                            streamingBufferHandler.buffer(copyBytes);
                                                                            streamingBufferHandler.reportReadBytes(copySize);
                                                                            Log.d(FeedStreamerApplication.TAG, "unread packet bytes reduced to: "
                                                                                    + Integer.toString(streamingBufferHandler.unreadPacketBytes));
                                                                            in.discardReadBytes();
                                                                        } catch (Throwable e) {
                                                                            motherSubscriber.onError(e);
                                                                        }
                                                                    }
                                                                    if (streamingBufferHandler.unreadPacketBytes == 0) {
                                                                        if (streamingBufferHandler.flipAndCheckSum()) {
                                                                            Log.d(FeedStreamerApplication.TAG, "good checksum");
                                                                            int write = streamingBufferHandler.writeTo(audioTrack);
                                                                            Log.d(FeedStreamerApplication.TAG, "write equal to: " + Integer.toString(write));
                                                                        } else {
                                                                            Log.d(FeedStreamerApplication.TAG, "bad checksum, wasn't: " + streamingBufferHandler.checksum);
                                                                        }
                                                                    }
                                                                }
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

    static private class StreamingBufferHandler {
        int packetLength;
        private ByteBuffer streamingBuffer;
        private int unreadPacketBytes;
        private byte[] binner;
        private byte[] xorSum;
        final byte[] sequenceNumber = new byte[4];
        final byte[] checksum = new byte[4];

        public StreamingBufferHandler() {
        }

        public void initializeToLength(int length) {
            this.packetLength = length;
            this.streamingBuffer = ByteBuffer.allocate(length + 12);
            this.unreadPacketBytes = length + 12;
            this.binner = new byte[length];
        }

        public void buffer(byte[] buffer) {
            try {
                streamingBuffer.put(buffer);
            } catch (Exception e) {
                Log.e(FeedStreamerApplication.TAG, "could not put into streaming buffer: " + e);
            }
        }

        public boolean flipAndCheckSum() {
            streamingBuffer.flip();
            streamingBuffer.get(sequenceNumber);
            streamingBuffer.get(checksum);
            int throwawayLength = streamingBuffer.getInt();
            Log.d(FeedStreamerApplication.TAG, "Length By Second Buffer: "+ throwawayLength);
            try {
                streamingBuffer.get(binner);
            } catch (Throwable e) {
                Log.e(FeedStreamerApplication.TAG, "index out of bounds for ioget: " + e.getMessage());
            }
            int remainder = packetLength % 4;
            xorSum = sequenceNumber.clone();
            Log.d(FeedStreamerApplication.TAG, "calculating checksum with remainder: " + Integer.toString(remainder));
            int i;
            for (i = 0; i < packetLength / 4; i++) {
                xorSum = xorThis(xorSum, binner, i);
            }
            Log.d(FeedStreamerApplication.TAG, "got regulars, sum: " + xorSum);
            if (remainder != 0) {
                xorSum = xorRemainder(xorSum, binner, remainder, i);
                Log.d(FeedStreamerApplication.TAG, "got irregulars, sum: " + xorSum);
            }
            return Arrays.equals(xorSum, checksum);
        }

        public void reportReadBytes(int bytesRead) {
            unreadPacketBytes -= bytesRead;
        }

        public int writeTo(AudioTrack audioTrack) {
            return audioTrack.write(binner, 0, binner.length);
        }

        private byte[] xorRemainder(byte[] xorSum, byte[] binner, int remainder, int iterationOnBinner) {
            byte[] response = new byte[4];
            int r;
            for (r = 0; r < remainder; r++) {
                response[r] = ((byte) (xorSum[r] ^ binner[iterationOnBinner * 4 + r]));
            }
            for (r = remainder; r < 4; r++) {
                response[r] = ((byte) (xorSum[r] ^ (byte) (0xab)));
            }
            return response;
        }

        private byte[] xorThis(byte[] xorSum, byte[] binner, int iterationsOnBinner) {
            byte[] response = new byte[4];
            int j;
            for (j = 0; j < 4; j++) {
                response[j] = ((byte) (xorSum[j] ^ binner[iterationsOnBinner * 4 + j]));
            }
            return response;
        }
    }
}
