package com.satfeed.services;

import android.media.AudioTrack;
import android.util.Log;

import com.satfeed.FeedStreamerApplication;

import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/*
 * Created by Andrew Brin on 4/27/2016.
 */
final public class AudioPlayer {

    private AudioTrack audioTrack;
    private TreeMap<Integer, byte[]> packetTreeMap;

    public AudioPlayer(AudioTrack audioTrack, TreeMap<Integer, byte[]> packetTreeMap) {
        this.audioTrack = audioTrack;
        this.packetTreeMap = packetTreeMap;
    }

    public Observable<Integer> playAudio() {
        final int numberOfPackets = packetTreeMap.size();
        return Observable
                .create(new Observable.OnSubscribe<Integer>() {
                    @Override
                    public void call(Subscriber<? super Integer> subscriber) {
                        audioTrack.play(); //  Streaming, so we must play first
                        int i = 0;
                        for (byte[] packet : packetTreeMap.values()) {
                            audioTrack.write(packet, 0, packet.length); //  write to audioTrack
                            subscriber.onNext(i++); //  send count downstream
                        }
                        subscriber.onCompleted();
                    }
                })
                .sample(100, TimeUnit.MILLISECONDS) //  only sample every 100 ms
                .map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(Integer i) {
                        float progress = (float) i / numberOfPackets; //  get normalized progress
                        return (int) (progress * 100); //  change progress from 0-1 to 0-100
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        audioTrack.stop(); //  Stop playing on unsubscribe
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }
}
