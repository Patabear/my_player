package com.example.patabear.myplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Patabear on 2016-12-21.
 */
public class ListAdapter extends BaseAdapter {

    private ArrayList<String> mMusicIDList;
    private ArrayList<String> mAlbumArtIDList;
    private ArrayList<String> mTitleList;
    private ArrayList<String> mArtistList;
    private ArrayList<String> mAlbumList;
    private ArrayList<String> mDataList; // file path
    private ArrayList<String> mFolderList;

    private boolean ListStatus = false; // false == show folderlist,  true == show musiclist

    private Context mContext;

    ListAdapter(Context c){
        mContext = c;
        mMusicIDList = new ArrayList<String>();
        mAlbumArtIDList = new ArrayList<String>();
        mTitleList = new ArrayList<String>();
        mArtistList = new ArrayList<String>();
        mAlbumList = new ArrayList<String>();
        mDataList = new ArrayList<String>();
        mFolderList = new ArrayList<String>();
        getFolderList();
    }

    @Override
    public int getCount() {
        return mTitleList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item, null);
        }

        TextView itemTitle = (TextView) convertView.findViewById(R.id.music_title);
        TextView itemSinger = (TextView) convertView.findViewById(R.id.music_artist);
        TextView itemAlbum = (TextView) convertView.findViewById(R.id.music_album);

        itemTitle.setText(mTitleList.get(position));

        if(ListStatus) { // music list
            itemSinger.setText(mArtistList.get(position));
            itemAlbum.setText(mAlbumList.get(position));
        }
        else { // folder list
            itemSinger.setText("");
            itemAlbum.setText("");
        }

        return convertView;
    }

    public void showMusicList(int position) {
        ListStatus = true;
        getMusicList(position);
    }

    public void showFolderList() {
        ListStatus = false;
        getFolderList();
    }

    private void getFolderList(){
        mTitleList.clear();
        mAlbumList.clear();
        mArtistList.clear();
        mMusicIDList.clear();
        mAlbumArtIDList.clear();
        mDataList.clear();

        String[] folderColumn = { // read Folders what include music file
                "distinct replace("+MediaStore.Audio.Media.DATA+", "+ MediaStore.Audio.Media.DISPLAY_NAME+", '')"
        };

        ContentResolver resolver = mContext.getContentResolver();

        Cursor musicCursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                folderColumn, null, null, null);

        if(musicCursor != null && musicCursor.getCount() > 0) {
            musicCursor.moveToFirst();
            while(!musicCursor.isAfterLast()) {
                mTitleList.add(musicCursor.getString(0));
                musicCursor.moveToNext();
            }
        }
        Log.d("tag", "mFolderList : "+mTitleList);


        for(int i = 0; i < mTitleList.size(); i++)
        {
            String temp;
            temp = mTitleList.get(i);
            mFolderList.add(i, temp);
        }

        if (musicCursor != null) {
            musicCursor.close();
        }
    }


    private void getMusicList(int position){
        String[] proj = {MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM
        };

        mTitleList.clear();
        mAlbumList.clear();
        mArtistList.clear();
        mMusicIDList.clear();
        mAlbumArtIDList.clear();
        mDataList.clear();

        ContentResolver resolver = mContext.getContentResolver();

        String where = MediaStore.Audio.Media.DATA + " like ?";
        String whereArgs[] = {mFolderList.get(position)+"%"};
        Cursor musicCursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj, where, whereArgs, null);

        int musicIDCol = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
        int dataCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        int musicTitleCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int singerCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int albumIDCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
        int albumCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);

        if(musicCursor.getCount() > 0) {
            musicCursor.moveToFirst();
            while(!musicCursor.isAfterLast()) {
                mMusicIDList.add(musicCursor.getString(musicIDCol));
                mDataList.add(musicCursor.getString(dataCol));
                mTitleList.add(musicCursor.getString(musicTitleCol));
                mArtistList.add(musicCursor.getString(singerCol));
                mAlbumArtIDList.add(musicCursor.getString(albumIDCol));
                mAlbumList.add(musicCursor.getString(albumCol));
                musicCursor.moveToNext();
            }
        }

        Log.d("tag", "musicList : "+mTitleList);

        musicCursor.close();
    }



    public MusicStruct getMusicInfo(int position){
        MusicStruct struct = new MusicStruct();
        struct.Title = mTitleList.get(position);
        struct.Album = mAlbumList.get(position);
        struct.Artist = mArtistList.get(position);
        struct.Data = mDataList.get(position);
        struct.MusicID = mMusicIDList.get(position);
        struct.AlbumArtID = mAlbumArtIDList.get(position);
        return struct;
    }
}
