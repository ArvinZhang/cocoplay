package com.arvin.pojo;

import android.os.Parcel;
import android.os.Parcelable;

public class Mp3 implements Parcelable {
	private long id;
	private String title;
	private String url;
	private String album;
	private long album_id;
	private String artist;
	private long artist_id;
	private String bookmark;
	private long duration;
	private String sortLetters; // 显示数据拼音的首字母

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public long getAlbum_id() {
		return album_id;
	}

	public void setAlbum_id(long album_id) {
		this.album_id = album_id;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public long getArtist_id() {
		return artist_id;
	}

	public void setArtist_id(long artist_id) {
		this.artist_id = artist_id;
	}

	public String getBookmark() {
		return bookmark;
	}

	public void setBookmark(String bookmark) {
		this.bookmark = bookmark;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getSortLetters() {
		return sortLetters;
	}

	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}

	/*
	 * 内容接口描述，默认返回0就可以
	 * 
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/*
	 * 将类的数据写入外部提供的Parcel中.即打包需要传递的数据到Parcel容器保存，以便从parcel容器获取数据
	 * 
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(title);
		dest.writeString(url);
		dest.writeString(album);
		dest.writeLong(album_id);
		dest.writeString(artist);
		dest.writeLong(artist_id);
		dest.writeString(bookmark);
		dest.writeLong(duration);
		dest.writeString(sortLetters);
	}

	public static final Parcelable.Creator<Mp3> CREATOR = new Creator<Mp3>() {

		/*
		 * 供外部类反序列化本类数组使用
		 * 
		 * @see android.os.Parcelable.Creator#newArray(int)
		 */
		@Override
		public Mp3[] newArray(int size) {
			return new Mp3[size];
		}

		/*
		 * 从Parcel容器中读取传递数据值，封装成Parcelable对象返回逻辑层
		 * 
		 * @see
		 * android.os.Parcelable.Creator#createFromParcel(android.os.Parcel)
		 */
		@Override
		public Mp3 createFromParcel(Parcel source) {
			Mp3 mp3 = new Mp3();
			mp3.setId(source.readLong());
			mp3.setTitle(source.readString());
			mp3.setUrl(source.readString());
			mp3.setAlbum(source.readString());
			mp3.setAlbum_id(source.readLong());
			mp3.setArtist(source.readString());
			mp3.setArtist_id(source.readLong());
			mp3.setBookmark(source.readString());
			mp3.setDuration(source.readLong());
			mp3.setSortLetters(source.readString());
			return mp3;
		}
	};

}
