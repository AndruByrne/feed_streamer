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
import com.satfeed.modules.ServiceComponent;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

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

    //    private final ServiceComponent serviceComponent;
    private Application application;

    public StreamingClient(Application application) {
        this.application = application;
//        serviceComponent = ((FeedStreamerApplication) application).getServiceComponent();
    }

    public Observable<String> streamToTrack(final String hailing_email, final AudioTrack audioTrack) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
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
                        .flatMap(new Func1<Connection<Object, Object>, Observable<String>>() {
                            @Override
                            public Observable<String> call(final Connection<Object, Object> connection) {

                                // first action is to receive the identification code
                                // and repond with idPacket
                                Observable<String> idCodeAndLogin = connection
                                        .getInput()
                                        .take(1)
                                        .map(new Func1<Object, String>() {
                                            @Override // remove first 6 characters i.e. no WHORU:
                                            public String call(Object o) {
                                                return o.toString().substring(6);
                                            }
                                        })
                                        .map(new Func1<String, String>() {
                                            @Override
                                            public String call(String identificationCode) {
                                                final Observable<String> s = getIdPacket(identificationCode, hailing_email);
                                                connection
                                                        .writeString(s)
                                                        .map(new Func1<Void, Object>() {
                                                            @Override
                                                            public Object call(Void aVoid) {
                                                                Log.d(FeedStreamerApplication.TAG, "second obs complete, in map");
                                                                return "";
                                                            }
                                                        });
                                                return identificationCode;
                                            }
                                        });

                                // second action is to receive the success or failure metric
                                Observable<String> successMessage = connection
                                        .getInput()
                                        .take(1)
                                        .map(new Func1<Object, String>() {
                                            @Override
                                            public String call(Object o) {
                                                Log.d(FeedStreamerApplication.TAG, "converting string in second obs");
                                                return o.toString();
                                            }
                                        });

                                //third action is to receive the stream
                                Observable<String> attachAudioToStream = connection
                                        .getInput()
                                        .take(4)
                                        .map(new Func1<Object, String>() {
                                            @Override
                                            public String call(Object o) {
                                                Log.d(FeedStreamerApplication.TAG, "we might be streaming");
                                                final int write = audioTrack.write(((ByteBuffer) o), 192, AudioTrack.WRITE_BLOCKING);
                                                if (write < 40 && (audioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING))
                                                    audioTrack.play();
                                                return "Streaming";
                                            }
                                        });

                                return Observable
                                        .concat(idCodeAndLogin, successMessage, attachAudioToStream);
                            }
                        })
                        .toBlocking()
                        .forEach(new Action1<String>() {
                            @Override
                            public void call(String s) {
                                subscriber.onNext(s);
                                subscriber.onCompleted();
                            }
                        });
            }
        });

    }

    @NonNull
    private Observable<String> getIdPacket(String identificationCode, String hailing_email) {
        final Resources resources = getResources();
        final String s = resources.getString(R.string.idpacket_part1) +
                identificationCode +
                resources.getString(R.string.idpacket_part2) +
                hailing_email +
                resources.getString(R.string.idpacket_part3);
        Log.d(FeedStreamerApplication.TAG, "made id: "+s);
        return Observable.just(s);
    }

    private Resources getResources() {
        return application.getResources();
    }
}
