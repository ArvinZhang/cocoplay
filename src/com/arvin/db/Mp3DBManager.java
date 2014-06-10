package com.arvin.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.arvin.pojo.Mp3;

public class Mp3DBManager {
	private final static String TAG = "com.arvin.db.Mp3DBManager";
	private DBHelper helper;
	private SQLiteDatabase db;
	private static Mp3DBManager mp3dbm = null;
	
	private Mp3DBManager() {};
	
	private Mp3DBManager(Context context) {
		helper = new DBHelper(context);
		db = helper.getWritableDatabase();
	}
	
	public static Mp3DBManager getInstance(Context context) {
		if (mp3dbm == null) {
			synchronized (Mp3DBManager.class) {
				if (mp3dbm == null) {
					mp3dbm = new Mp3DBManager(context);
				}
			}
		}
		return mp3dbm;
	}
	
	public void addMp3List(List<Mp3> mp3List) {
		db.beginTransaction();
		try {
			String sql = "DELETE FROM mp3";
			db.execSQL(sql);
			
			for (Mp3 mp3 : mp3List) {
				sql = "INSERT INTO mp3 VALUES(?, ?, ?, ?, ?, ?, ? ,? ,?, ?)";
				db.execSQL(sql, new Object[] {mp3.getId(), mp3.getTitle(), 
						mp3.getUrl(), mp3.getAlbum(), 
						mp3.getAlbum_id(), mp3.getArtist(), 
						mp3.getArtist_id(), mp3.getBookmark(), 
						mp3.getDuration(), mp3.getSortLetters()}
				);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}
	
	public List<Mp3> getMp3List() {
		Log.i(TAG, "getMp3List");
		List<Mp3> mp3List = new ArrayList<Mp3>();
		Cursor c = getMp3Cursor();
		
		Mp3 mp3 = null;
		while (c.moveToNext()) {
			mp3 = new Mp3();
			mp3.setAlbum(c.getString(c.getColumnIndex("album")));
			mp3.setAlbum_id(c.getLong(c.getColumnIndex("album_id")));
			mp3.setArtist(c.getString(c.getColumnIndex("artist")));
			mp3.setArtist_id(c.getLong(c.getColumnIndex("artist_id")));
			mp3.setBookmark(c.getString(c.getColumnIndex("bookmark")));
			mp3.setDuration(c.getLong(c.getColumnIndex("duration")));
			mp3.setId(c.getLong(c.getColumnIndex("id")));
			mp3.setSortLetters(c.getString(c.getColumnIndex("sortLetters")));
			mp3.setTitle(c.getString(c.getColumnIndex("title")));
			mp3.setUrl(c.getString(c.getColumnIndex("url")));
			mp3List.add(mp3);
		}
		c.close();
		
		return mp3List;
	}
	
	public Cursor getMp3Cursor() {
		return db.rawQuery("SELECT * FROM mp3", null);
	}
}
