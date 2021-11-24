package com.example.patabear.myplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Patabear on 2016-12-22.
 */
public class MusicIntentReceiver extends BroadcastReceiver {
    Context mContext;

    MusicIntentReceiver() {}

    MusicIntentReceiver(Context c) {
        mContext = c;
    }
    @Override public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
            int state = intent.getIntExtra("state", -1);
            Log.i("tag", "headset plug/unplug");
            switch (state) {
                case 0:
                    if(((MainActivity)mContext).mMediaPlayer.isPlaying())
                        ((MainActivity)mContext).goPlay();
                    break;
                case 1: // 이어폰이 꽂혔을때인데 고민중 그냥 아무것도 안하기로함
                    if(!((MainActivity)mContext).mMediaPlayer.isPlaying())
                        ((MainActivity)mContext).goPlay();
                    break;
                default:
            }
        }
    }
}