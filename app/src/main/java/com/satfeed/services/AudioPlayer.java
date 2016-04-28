package com.satfeed.services;

import android.media.AudioTrack;
import android.util.Log;

import com.satfeed.FeedStreamerApplication;

import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
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
                        audioTrack.play();
                        int i = 0;
                        for (byte[] packet : packetTreeMap.values()) {
                            audioTrack.write(packet, 0, packet.length);
                            subscriber.onNext(i++);
                        }
                        subscriber.onCompleted();
                    }
                })
                .sample(100, TimeUnit.MILLISECONDS)
                .map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(Integer i) {
                        return (int) ((float) i / numberOfPackets) * 100;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }
}
