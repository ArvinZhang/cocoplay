/**
 * Project Name:cocoplay_20140509
 * File Name:Mp3Loader.java
 * Package Name:com.arvin.cocoplay
 * Date:2014年5月9日下午5:49:03
 * Copyright (c) 2014, www.kugou.com All Rights Reserved.
 *
*/

package com.arvin.cocoplay;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;

import com.arvin.custom.CharacterParser;
import com.arvin.custom.PinyinComparator;
import com.arvin.db.Mp3DBManager;
import com.arvin.pojo.Mp3;

/**
 * ClassName:Mp3Loader
 * Function: TODO ADD FUNCTION.
 * Reason:	 TODO ADD REASON.
 * Date:     2014年5月9日 下午5:49:03
 * @author   arvinzhang
 * @version  
 * @see 	 
 */
public class Mp3Loader {
	private static final String TAG = "com.arvin.cocoplay.Mp3Loader";
	private final static int MP3_TIME_FILTER = 30 * 1000;
	private final static String SP_NAME = "com.arvin.cocoplay"; 
	
	private static Mp3Loader mp3Loader = null;
	private static ContentResolver contentResolver;
	private PinyinComparator pyComparator;

	private CharacterParser characterParser;
	private static List<Mp3> mp3List = new ArrayList<Mp3>();
	private Mp3DBManager mp3dbm = Mp3DBManager.getInstance(MyApplication.getInstance());
	private Uri contentUri = Media.EXTERNAL_CONTENT_URI;
	private String sortStr = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
	private SharedPreferences sp;
	
	public static Mp3Loader getInstance(ContentResolver pContentResolver) {
		if (mp3Loader == null) {
			synchronized(Mp3Loader.class) {
				if (mp3Loader == null) {
					contentResolver = pContentResolver;
					mp3Loader = new Mp3Loader();
				}
			}
		}
		return mp3Loader;
	}
	
	private void getMp3ListFromMedia() {
		Cursor cursor = contentResolver.query(contentUri, null, null, null, sortStr);
		Log.i(TAG, "getMp3ListFromMedia");

		if(cursor == null) {
			Log.v(TAG,"Line(37	)	Music Loader cursor == null.");
		}else if(!cursor.moveToFirst()){
			Log.v(TAG,"Line(39	)	Music Loader cursor.moveToFirst() returns false.");
		}else {
			do {
				int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
				long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
				String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
	
				File mp3File = new File(url);
				
				Log.i(TAG, cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
				
				if (mp3File.exists() && duration >= MP3_TIME_FILTER) {
					Mp3 mp3 = new Mp3();
					String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
					mp3.setTitle(title);
					mp3.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
					mp3.setAlbum_id(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
					mp3.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
					mp3.setArtist_id(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)));
					mp3.setBookmark(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.BOOKMARK)));
					mp3.setDuration(duration);
					mp3.setId(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
					mp3.setUrl(url);
	
					// 汉字转换成拼音
					String pinyin = characterParser.getSelling(title);
					String sortString = pinyin.substring(0, 1).toUpperCase();
	
					// 正则表达式，判断首字母是否是英文字母
					if (sortString.matches("[A-Z]")) {
						mp3.setSortLetters(sortString.toUpperCase());
					} else {
						mp3.setSortLetters("#");
					}
					mp3List.add(mp3);
				}
			} while (cursor.moveToNext());
		}
		cursor.close();
	}
	
	public List<Mp3> refreshMp3List() {
		mp3List.clear();
		getMp3ListFromMedia();
		mp3dbm.addMp3List(mp3List);
		mp3List  = mp3dbm.getMp3List();
		// 根据a-z进行排序源数据
		Collections.sort(mp3List, pyComparator);
		editSp();
		return mp3List;
	}
	
	private void editSp() {
		Editor editor = sp.edit();
		editor.putBoolean("isScan", true);
		editor.commit();
	}
	

	private Mp3Loader() {
		// 实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();
		
		sp = MyApplication.getInstance().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
		boolean isScanFromMediaStore = sp.getBoolean("isScan", false);
		
		if (!isScanFromMediaStore) {
			getMp3ListFromMedia();
			mp3dbm.addMp3List(mp3List);
			editSp();
		}
		mp3List = mp3dbm.getMp3List();

		// 实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();
		pyComparator = new PinyinComparator();
		// 根据a-z进行排序源数据
		Collections.sort(mp3List, pyComparator);
	}
	
	public List<Mp3> getMp3List() {
		return mp3List;
	}
	
	public Uri getMusicUriById(long id){
		Uri uri = ContentUris.withAppendedId(contentUri, id);
		return uri;
	}	
}
