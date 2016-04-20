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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
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
        CONNECTED,
        AUTHENTICATING,
        STREAMING
    }

    //    private final ServiceComponent serviceComponent;
    private Application application;

    public StreamingClient(Application application) {
        this.application = application;
//        serviceComponent = ((FeedStreamerApplication) application).getServiceComponent();
    }


    public Observable<String> streamToTrack(final String hailing_email, final AudioTrack audioTrack) {
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
                                        connection.getInput().take(100).doOnNext(new Action1<Object>() {
                                            @Override
                                            public void call(Object o) {
                                                switch (tcpStateCounter.get()) {
                                                    case CONNECTED:
                                                        // write out
                                                        final Observable<byte[]> outBytes = getIdPacketUTF8(o.toString().substring(6), hailing_email);
                                                        connection.writeBytesAndFlushOnEach(outBytes).subscribe();
                                                        break;
                                                    case AUTHENTICATING:
                                                        final String status = o.toString();
                                                        motherSubscriber.onNext("Status: " + status);
                                                        break;
                                                    case STREAMING:
                                                        final byte[] bytes = o.toString().getBytes(StandardCharsets.UTF_8);
                                                        Log.d(FeedStreamerApplication.TAG, "byte length is: "+bytes.length);
                                                        final int write = audioTrack.write(bytes, 0, bytes.length);
                                                        Log.d(FeedStreamerApplication.TAG, "write is: "+write);
                                                        subscriber.onNext(true);
                                                        break;
                                                    default:
                                                        subscriber.onCompleted();

                                                }
                                            }
                                        }).subscribe();
                                    }
                                });
                            }
                        }).take(100).subscribe();
            }
        }).subscribeOn(Schedulers.newThread());
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
            this.state = TCPState.CONNECTED;
        }

        public TCPState get() {
            switch (state) {
                case CONNECTED:
                    state = TCPState.AUTHENTICATING;
                    return TCPState.CONNECTED;
                case AUTHENTICATING:
                    state = TCPState.STREAMING;
                    return TCPState.AUTHENTICATING;
                case STREAMING:
                    return TCPState.STREAMING;
            }
            return null;
        }
    }
}
