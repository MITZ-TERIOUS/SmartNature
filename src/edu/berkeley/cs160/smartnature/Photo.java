package edu.berkeley.cs160.smartnature;

import com.google.gson.annotations.Expose;

import android.net.Uri;

public class Photo {
	
	private int id;
	/** uri is a string for json convenience */
	private String uri;
	@Expose private String title;
	
	private int photo_num;
	
	Photo(Uri uri) {
		this.uri = uri.toString();
	}
	
	public int getServerId() { return id; }
	
	public String getTitle() { return title; }
	
	public Uri getUri() { return Uri.parse(uri); }
	
	public void setServerId(int serverId) { this.id = serverId; }
	
	public void setTitle(String title) { this.title = title; }
	
	public void setUri(Uri uri) { this.uri = uri.toString(); }
	
	public int getPhotoNum() { return this.photo_num; }

	public void setPhotoNum(int photo_num) { this.photo_num = photo_num; }
	
}