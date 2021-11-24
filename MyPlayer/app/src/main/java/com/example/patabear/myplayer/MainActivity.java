package com.example.patabear.myplayer;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by Patabear on 2016-12-21.
 */

public class MainActivity extends AppCompatActivity { // TODO : 내가 만든 리스트 보기, 추천 리스트 자동만들기 기능 (optional)
    // TODO : 어플 다운후 첫실행때 도움말 보여주기, 폴더명 다듬기
    // TODO : 전화가 올때 일시정지 전화 끝나면 재생, 어플 백그라운드에서 종료 안되게 하기
    FloatingActionButton fab; // play, add music in list
    FloatingActionButton fab2; // clear list
    ImageView mAlbumArt;
    TextView mMusicName;
    Button mPrev;
    Button mPlay;
    Button mNext;
    SeekBar mSeekBar;
    TextView mListCount;
    boolean FabStatus = true; // false == add,  true == play
    boolean ListStatus = false; // false == FolderList,   true == musicList
    boolean FirstTime = true; // when first play
    boolean Registerd = false;
    int CurrentPlaying; // current playing music
    Thread thread; // to show seekBar

    public ListView mListView;
    public ListAdapter mListAdpater;
    public MediaPlayer mMediaPlayer = new MediaPlayer();
    public LinkedList<MusicStruct> mMyMusicList = new LinkedList<>(); // music list
    public NotificationManager notificationManager;
    public PendingIntent pendingIntent;
    public NotificationCompat.Builder mCompatBuilder;
    public MusicIntentReceiver musicIntentReceiver;

    SharedPreferences sharedPreferences;
    ContextWrapper contextWrapper = new ContextWrapper(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = contextWrapper.getSharedPreferences("my_db", Context.MODE_PRIVATE);
        boolean Visited = sharedPreferences.getBoolean("HasVisited", false);
        if(!Visited) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("HasVisited", true);
            editor.apply();
            Intent intent = new Intent(this, FirstStepActivity.class);
            this.startActivity(intent);
        }

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0)
        { // checking resume app by notification click
            finish(); // finish new Activity, use old Activity
            return;
        }
        setContentView(R.layout.activity_main);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck == PackageManager.PERMISSION_DENIED)
        { // External storage permission check
            int i = 0;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, i);
        }

        // For notification, check func : setNotificationManager together
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        mCompatBuilder = new NotificationCompat.Builder(this);

        musicIntentReceiver = new MusicIntentReceiver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        registerReceiver(musicIntentReceiver, intentFilter);
        Registerd = true;

        mAlbumArt = (ImageView) findViewById(R.id.album_art);
        mMusicName = (TextView) findViewById(R.id.music_name);
        mPrev = (Button) findViewById(R.id.previous_button);
        mPlay = (Button) findViewById(R.id.play_button);
        mNext = (Button) findViewById(R.id.next_button);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mListCount = (TextView) findViewById(R.id.list_count);

        mListView = (ListView) findViewById(R.id.music_list);
        mListAdpater = new ListAdapter(this);
        mListView.setAdapter(mListAdpater);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab.setOnClickListener(new View.OnClickListener()
        { // change mode (add, start)
            @Override
            public void onClick(View view) {
                FabStatus = !FabStatus;
                changeImageFab();
            }
        });

        fab2.setOnClickListener(new View.OnClickListener()
        { // clear music list
            @Override
            public void onClick(View view) {
                mMyMusicList.clear();
                mListCount.setText(String.valueOf(CurrentPlaying+1) + "/" + String.valueOf(mMyMusicList.size()));
            }
        });

        mListView.setOnItemClickListener(new ListView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if(!ListStatus)
                { // Folder List
                    mListAdpater.showMusicList(position);
                    mListAdpater.notifyDataSetChanged();
                    ListStatus = true;
                }
                else
                { // Music List
                    if (FabStatus)
                    { // start music mode
                        if(mMyMusicList.isEmpty()) {
                            FabStatus = false;
                            changeImageFab();
                        }
                        CurrentPlaying = 0;
                        mMyMusicList.addFirst(mListAdpater.getMusicInfo(position));
                        playMusic(mMyMusicList.getFirst().Data);
                        mPlay.setBackgroundResource(R.drawable.media_pause);
                        FirstTime = false;
                    }
                    else
                    { // add music mode
                        mMyMusicList.addLast(mListAdpater.getMusicInfo(position));
                        mListCount.setText(String.valueOf(CurrentPlaying+1) + "/" + String.valueOf(mMyMusicList.size()));
                    }
                }
            }
        });

        mListView.setAdapter(mListAdpater);

        mPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goPrev();
            }
        });

        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goPlay();
            }
        });

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNext();
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser)
                    mMediaPlayer.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mListCount.setText("0/0");
    }

    @Override
    public void onDestroy() {
        if(notificationManager != null)
            notificationManager.cancel(133);
        if(Registerd)
            unregisterReceiver(musicIntentReceiver);
        super.onDestroy();
        KillThread();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    @Override
    public void onBackPressed() {
        if(ListStatus){ // music list
            mListAdpater.showFolderList();
            mListAdpater.notifyDataSetChanged();
            ListStatus = false;
        }
        else{ // folder list , same effect when press 'home button'
            KillThread();
            moveTaskToBack(true);
        }

        Log.d("tag", "onBackPressed Called");
    }

    @Override
    public void onResume(){
        super.onResume();
        KillThread();
        SeekThread();
    }

    private void changeImageFab(){
        if(!FabStatus) {
            fab.setImageResource(R.drawable.plus);
        }
        else {
            fab.setImageResource(R.drawable.play);
        }
    }

    private void playMusic(String path) {
        try {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepare();
            mMediaPlayer.start();

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) { // when music is ended
                    CurrentPlaying++;
                    if(mMyMusicList.size()-1 < CurrentPlaying)
                        CurrentPlaying = 0;
                    playMusic(mMyMusicList.get(CurrentPlaying).Data);
                }
            });

        } catch (IOException e) {
            Log.d("tag", e.getMessage());
        }

        mSeekBar.setMax(mMediaPlayer.getDuration()); // set seek bar
        showPlayStatus();
        SeekThread();
        notificationManager.cancel(133);
        setNotificationManager();
    }

    private void showPlayStatus() { // show music information
        MusicStruct NowPlaying = mMyMusicList.get(CurrentPlaying);
        Bitmap Art = getArtworkQuick(this, Integer.parseInt(NowPlaying.AlbumArtID), 1024, 1024);
        if(Art == null)
            Art = BitmapFactory.decodeResource(this.getResources(), R.drawable.konata);
        mAlbumArt.setImageBitmap(Art);
        mMusicName.setText(NowPlaying.Title);
        mSeekBar.setMax(mMediaPlayer.getDuration());
        mListCount.setText(String.valueOf(CurrentPlaying+1) + "/" + String.valueOf(mMyMusicList.size()));
    }

    private void goPrev()
    {
        if(CurrentPlaying <= 0)
        {
            CurrentPlaying = mMyMusicList.size()-1;
        }
        else
            CurrentPlaying--;

        mListCount.setText(String.valueOf(CurrentPlaying+1) + "/" + String.valueOf(mMyMusicList.size()));

        KillThread();
        if(mMediaPlayer.isPlaying())
            playMusic(mMyMusicList.get(CurrentPlaying).Data);
    }

    public void goPlay()
    {
        if(mMediaPlayer.isPlaying())
        {
            mMediaPlayer.pause();
            mPlay.setBackgroundResource(R.drawable.media_play);
            notificationManager.cancel(133);
        }
        else if(!FirstTime)
        {
            mMediaPlayer.start();
            mPlay.setBackgroundResource(R.drawable.media_pause);
            SeekThread();
            setNotificationManager();
        }
        else
        { // only first time
            if(mMyMusicList.size() != 0)
            {
                playMusic(mMyMusicList.get(CurrentPlaying).Data);
                FirstTime = false;
            }
        }

    }
    private void goNext()
    {
        CurrentPlaying++;
        if(CurrentPlaying >= mMyMusicList.size())
        {
            CurrentPlaying = 0;
        }

        mListCount.setText(String.valueOf(CurrentPlaying+1) + "/" + String.valueOf(mMyMusicList.size()));

        KillThread();
        if(mMediaPlayer.isPlaying())
            playMusic(mMyMusicList.get(CurrentPlaying).Data);
    }

    public void SeekThread(){ // seekbar
        Runnable task = new Runnable(){
            public void run(){
                while(mMediaPlayer.isPlaying())
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(mMediaPlayer.isPlaying())
                    {
                       mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
                    }
                }
            }
        };
        thread = new Thread(task);
        thread.start();
    }

    public void KillThread()
    {
        Thread.currentThread().interrupt();
        thread = null;
    }

    // open source - Bring Album Art
    private static final BitmapFactory.Options sBitmapOptionsCache = new BitmapFactory.Options();
    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");

    private static Bitmap getArtworkQuick(Context context, int album_id, int w, int h) {
        // NOTE: There is in fact a 1 pixel frame in the ImageView used to
        // display this drawable. Take it into account now, so we don't have to
        // scale later.
        w -= 2;
        h -= 2;
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        ContentResolver res = context.getContentResolver();
        if (uri != null) {
            ParcelFileDescriptor fd = null;
            try {
                fd = res.openFileDescriptor(uri, "r");
                int sampleSize = 1;

                // Compute the closest power-of-two scale factor
                // and pass that to sBitmapOptionsCache.inSampleSize, which will
                // result in faster decoding and better quality
                sBitmapOptionsCache.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, sBitmapOptionsCache);
                int nextWidth = sBitmapOptionsCache.outWidth >> 1;
                int nextHeight = sBitmapOptionsCache.outHeight >> 1;
                while (nextWidth>w && nextHeight>h) {
                    sampleSize <<= 1;
                    nextWidth >>= 1;
                    nextHeight >>= 1;
                }

                sBitmapOptionsCache.inSampleSize = sampleSize;
                sBitmapOptionsCache.inJustDecodeBounds = false;
                Bitmap b = BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, sBitmapOptionsCache);

                if (b != null) {
                    // finally rescale to exactly the size we need
                    if (sBitmapOptionsCache.outWidth != w || sBitmapOptionsCache.outHeight != h) {
                        Bitmap tmp = Bitmap.createScaledBitmap(b, w, h, true);
                        b.recycle();
                        b = tmp;
                    }
                }

                return b;
            } catch (FileNotFoundException e) {
            } finally {
                try {
                    if (fd != null)
                        fd.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    private void setNotificationManager()
    {
        mCompatBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mCompatBuilder.setContentTitle(mMyMusicList.get(CurrentPlaying).Title);
        mCompatBuilder.setContentText(mMyMusicList.get(CurrentPlaying).Artist);
        mCompatBuilder.setShowWhen(false);
        mCompatBuilder.setContentIntent(pendingIntent);
        mCompatBuilder.setOngoing(true);
        mCompatBuilder.setUsesChronometer(true);

        notificationManager.notify(133, mCompatBuilder.build());
    }
}