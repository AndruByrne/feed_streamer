package com.satfeed.services;

/*
 * Created by Andrew Brin on 4/18/2016.
 */

import android.app.Application;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Log;

import com.satfeed.FeedStreamerApplication;
import com.satfeed.R;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;

import io.netty.buffer.ByteBuf;
import io.netty.util.ByteProcessor;
import io.reactivex.netty.channel.Connection;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

final public class DownloadClient {

    public enum SocketStates {
        CONNECTING,
        AUTHENTICATING,
        STREAMING
    }

    final private Application application;
    final private TreeMap<Integer, byte[]> packetTreeMap;
    final private DownloadBufferHandler downloadBufferHandler;
    final private String hailingEmail;

    public DownloadClient(
            Application application,
            TreeMap<Integer, byte[]> packetTreeMap,
            DownloadBufferHandler downloadBufferHandler,
            String hailingEmail) {
        this.application = application;
        this.packetTreeMap = packetTreeMap;
        this.downloadBufferHandler = downloadBufferHandler;
        this.hailingEmail = hailingEmail;
    }

    private static final ByteProcessor LINE_END_FINDER = new ByteProcessor() {
        public static final char LF = 10;

        @Override
        public boolean process(byte value) throws Exception {
            char nextByte = (char) value;
            return LF != nextByte;
        }
    };

    public Observable<Integer> streamToTree() {
        //  wrapping TcpClient in a mother observable b/c it won't take the assignment to io thread
        //  TCPClient will return NetworkOnMainThreadError!
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(final Subscriber<? super Integer> motherSubscriber) {
                TcpClient
                        .<ByteBuf, ByteBuf>newClient(new InetSocketAddress( //  also must happen off main thread; oddly, throws exception when injected in Populator.
                                FeedStreamerApplication.ALIEN_SERVER,
                                Integer.parseInt(FeedStreamerApplication.STREAMING_PORT)))
                        .createConnectionRequest() //  we take one of these and subscribe on thread.
                        .flatMap(new Func1<Connection<ByteBuf, ByteBuf>, Observable<?>>() {
                            @Override
                            //  For each connection create an inner Observable; this is cleaned up by the holding TCPClient's take().
                            public Observable<?> call(final Connection<ByteBuf, ByteBuf> connection) {
                                return Observable.create(new Observable.OnSubscribe<Object>() {
                                    @Override
                                    //  This call has no return but has a side effect in the injected packetTreeMap; onErrors are propagated to the mother Observable.
                                    public void call(Subscriber<? super Object> unusedSubscriber) {
                                        final SocketState socketState = new SocketState();
                                        connection
                                                .getInput()
                                                .map(new Func1<ByteBuf, String>() { //  This is run once per OK click
                                                    @Override
                                                    public String call(ByteBuf in) {
                                                        switch (socketState.state) {
                                                            case STREAMING:
                                                                while (in.isReadable()) {
                                                                    if (!downloadBufferHandler.isInitialized()) { // we do not have a full header
                                                                        final byte[] copyHeaderBytes = new byte[Math.min(
                                                                                downloadBufferHandler.getHeaderBytesRemaining(),
                                                                                in.readableBytes())];
                                                                        try { //  copy the bytes from in to header
                                                                            in.readBytes(copyHeaderBytes);
                                                                            downloadBufferHandler.bufferHeader(copyHeaderBytes);
                                                                            in.discardReadBytes();
                                                                        } catch (Throwable e) {
                                                                            throw e;
                                                                        }
                                                                    }
                                                                    if (downloadBufferHandler.isInitialized()) { // we did initialize off a full header
                                                                        while (downloadBufferHandler.getPacketBufBytesRemaining() > 0 && in.isReadable()) { //  copy bytes from inbuffer to streaming buffer
                                                                            int copySize = Math.min(in.readableBytes(), downloadBufferHandler.getPacketBufBytesRemaining());
                                                                            final byte[] copyBytes = new byte[copySize];
                                                                            try { //  copy the bytes from in to streamingBuffer
                                                                                in.readBytes(copyBytes);
                                                                                downloadBufferHandler.buffer(copyBytes);
                                                                                in.discardReadBytes();
                                                                            } catch (Throwable e) {
                                                                                throw e;
                                                                            }
                                                                        }
                                                                        if (downloadBufferHandler.getPacketBufBytesRemaining() == 0) { // perform checksum and report to UI thread
                                                                            byte[] body = downloadBufferHandler.checkSumAndGetBody(); //  if good checksum, body will be audio data
                                                                            if (body != null) { //  if bad checksum, body will be null
                                                                                int sequenceNumber = downloadBufferHandler.getSequenceNumber();
                                                                                if (!packetTreeMap.containsKey(sequenceNumber)) { //  check for duplicates (may want to remove)
                                                                                    packetTreeMap.put(sequenceNumber, body); //  put into ordered map for later retrieval.
                                                                                    int treeMapSize = packetTreeMap.size();
                                                                                    if (treeMapSize % 24 == 0) {
                                                                                        motherSubscriber.onNext(treeMapSize);
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                                break;
                                                            case AUTHENTICATING:
                                                                while (in.isReadable()) {
                                                                    int startIndex = in.readerIndex();
                                                                    int lastReadIndex = in.forEachByte(LINE_END_FINDER); //  find end of line
                                                                    ByteBuf slice = in.readSlice(lastReadIndex - startIndex); //  read line
                                                                    String line = slice.toString(StandardCharsets.UTF_8);
                                                                    Log.i(FeedStreamerApplication.TAG, "authenticating recieved: " + line);
                                                                    socketState.authenticated(); //  update socket state
                                                                    in.skipBytes(1); //  skip carraige return
                                                                }
                                                                break;
                                                            case CONNECTING:
                                                                while (in.isReadable()) {
                                                                    int startIndex = in.readerIndex();
                                                                    int lastReadIndex = in.forEachByte(LINE_END_FINDER); //  find end of line
                                                                    ByteBuf slice = in.readSlice(lastReadIndex - startIndex); //  read line
                                                                    String line = slice.toString(StandardCharsets.UTF_8);
                                                                    socketState.connected(); //  update socket state
                                                                    Log.i(FeedStreamerApplication.TAG, "connecting recieved: " + line);
                                                                    final Observable<byte[]> outBytes = getIdPacketUTF8(line.substring(6), hailingEmail); //  get return to challenge
                                                                    connection.writeBytesAndFlushOnEach(outBytes).take(1).subscribe(); //  write return bytes
                                                                    in.skipBytes(1); //  skip carraige return
                                                                }
                                                                break;
                                                            default:
                                                                motherSubscriber.onError(new Throwable("Poor DownloadClient state"));
                                                                break;
                                                        }
                                                        return "Done";
                                                    }
                                                })
                                                .doOnError(new Action1<Throwable>() {
                                                    @Override
                                                    public void call(Throwable throwable) {
                                                        motherSubscriber.onError(throwable);
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
        return Observable.just(b); //  return an Obs with the bytes for answering whoru
    }

    private Resources getResources() {
        return application.getResources();
    }

    private class SocketState {
        // A simple monitor of the socket state
        SocketStates state;

        SocketState() {
            this.state = SocketStates.CONNECTING;
        }

        public void connected() {
            state = SocketStates.AUTHENTICATING;
        }

        public void authenticated() {
            state = SocketStates.STREAMING;
        }
    }
}
