package com.satfeed.activity;

/*
 * Created by Andrew Brin on 4/18/2016.
 */

import android.app.Activity;
import android.content.res.Resources;
import android.test.mock.MockApplication;
import android.util.Log;
import android.view.SurfaceView;

import com.satfeed.FeedStreamerApplication;
import com.satfeed.R;
import com.satfeed.modules.ServiceComponent;
import com.satfeed.modules.SocketAddressModule;

import java.lang.annotation.Annotation;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.Objects;

import javax.inject.Inject;

import dagger.Module;
import dagger.Provides;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.logging.LogLevel;
import io.paperdb.Paper;
import io.reactivex.netty.channel.Connection;
import io.reactivex.netty.channel.ContentSource;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import io.reactivex.netty.util.StringLineDecoder;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class StreamingClient {

    public Observable<String> streamToSurface(final SurfaceView view, final String hailing_email) {

        Log.d(FeedStreamerApplication.TAG, "surface stream starting");
        Log.d(FeedStreamerApplication.TAG, "surfaceView not null? : "+Boolean.toString(view!=null));

        return Observable.create(new Observable.OnSubscribe<String>() {
            private String challenge_number;

            @Override
            public void call(final Subscriber<? super String> subscriber) {
                Log.d(FeedStreamerApplication.TAG, "surface stream started");
                TcpClient
                        .<ByteBuf, ByteBuf>newClient(getServiceComponent(view).getServerAddress())
                        .enableWireLogging(LogLevel.DEBUG)
                        .addChannelHandlerLast("string_decoder", new Func0<ChannelHandler>() {
                            @Override
                            public ChannelHandler call() {
                                return new StringLineDecoder();
                            }
                        })
                        .createConnectionRequest()
                                // get identification number from connection
                        .concatMap(new Func1<Connection<Object, Object>, Observable<Connection<Object, Object>>>() {
                            @Override
                            public Observable<Connection<Object, Object>> call(final Connection<Object, Object> connection) {
                                return connection
                                        .getInput()
                                        .take(1)
                                        .map(new Func1<Object, String>() {
                                            @Override
                                            public String call(Object o) {
                                                return o.toString();
                                            }
                                        })
                                        .doOnNext(new Action1<String>() {
                                            @Override
                                            public void call(String s) {
                                                subscriber.onNext(s);
                                                // datastore;
                                            }
                                        })
                                        .map(new Func1<String, String>() {
                                            @Override
                                            public String call(String s) {
                                                return s.substring(6);
                                            }
                                        })
                                        .doOnNext(new Action1<String>() {
                                            @Override
                                            public void call(String s) {
                                                challenge_number = s;
                                            }
                                        })
                                        .map(new Func1<String, Connection<Object, Object>>() {
                                            @Override
                                            public Connection<Object, Object> call(String s) {
                                                return connection;
                                            }
                                        });
                            }
                        }).doOnNext(new Action1<Connection<Object, Object>>() {
                    @Override
                    public void call(Connection<Object, Object> connection) {
                        Log.d(FeedStreamerApplication.TAG, "amazingly, we got past the first connection on the first try");
                    }
                })
                                // respond with populated id packet
                        .concatMap(new Func1<Connection<Object, Object>, Observable<Connection<Object, Object>>>() {
                            @Override
                            public Observable<Connection<Object, Object>> call(final Connection<Object, Object> connection) {
                                return connection
                                        .writeString(getIdPacket())
                                        .map(new Func1<Void, Connection<Object, Object>>() {
                                            @Override
                                            public Connection<Object, Object> call(Void aVoid) {
                                                return connection;
                                            }
                                        });
                            }
                        }).doOnNext(new Action1<Connection<Object, Object>>() {
                    @Override
                    public void call(Connection<Object, Object> connection) {
                        Log.d(FeedStreamerApplication.TAG, "sent response");
                    }
                })
                        .concatMap(new Func1<Connection<Object, Object>, Observable<Connection<Object, Object>>>() {
                            @Override
                            public Observable<Connection<Object, Object>> call(final Connection<Object, Object> connection) {
                                return connection
                                        .getInput()
                                        .take(1)
                                        .map(new Func1<Object, String>() {
                                            @Override
                                            public String call(Object o) {
                                                return o.toString();
                                            }
                                        })
                                        .doOnNext(new Action1<String>() {
                                            @Override
                                            public void call(String s) {
                                                subscriber.onNext(s);
                                            }
                                        }).map(new Func1<String, Connection<Object, Object>>() {
                                            @Override
                                            public Connection<Object, Object> call(String s) {
                                                return connection;
                                            }
                                        });
                            }
                        }).doOnNext(new Action1<Connection<Object, Object>>() {
                    @Override
                    public void call(Connection<Object, Object> connection) {
                        Log.d(FeedStreamerApplication.TAG, "about to stream!");

                    }
                })
                        .flatMap(new Func1<Connection<Object, Object>, Observable<?>>() {
                            @Override
                            public Observable<?> call(final Connection<Object, Object> connection) {
                                return Observable.create(new Observable.OnSubscribe<Object>() {
                                    @Override
                                    public void call(Subscriber<? super Object> subscriber) {
                                        for (int i = 0; i < 10; i++) {
                                            connection.getInput();
                                            subscriber.onNext(true);
                                        }
                                    }
                                });
                            }
                        })
                        .take(10)
                        .toBlocking()
                        .forEach(new Action1<Object>() {
                            @Override
                            public void call(Object o) {
                                subscriber.onNext("Completed Stream!");
                            }
                        });
                subscriber.onCompleted();
            }

            private Observable<String> getIdPacket() {
                final Resources resources = getResources(view);
                return Observable.just(
                        resources.getString(R.string.idpacket_part1) +
                                challenge_number +
                                resources.getString(R.string.idpacket_part2) +
                                hailing_email +
                                resources.getString(R.string.idpacket_part3));
            }})
                .take(3)
                .subscribeOn(Schedulers.newThread());
    }

    private Resources getResources(SurfaceView view) {
        return ((Activity) view.getContext()).getResources();
    }

    private static ServiceComponent getServiceComponent(SurfaceView view) {
        return ((FeedStreamerApplication) ((Activity) view.getContext()).getApplication())
                .getServiceComponent();
    }
}
