package com.wifi.wifidirect;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.util.Pair;

public class MediaManager {
	private ArrayList<Pair<String, String>> titlesList = new ArrayList<Pair<String,String>>();
	private ContentResolver contentResolver;

	public MediaManager(ContentResolver contentResolver) {
		this.contentResolver = contentResolver;
	}

	public ArrayList<Pair<String, String>> getTitlesList() {
		return titlesList;
	}
	public void setTitlesList(ArrayList<Pair<String, String>> result) {
		this.titlesList = result;
		
	}

}
