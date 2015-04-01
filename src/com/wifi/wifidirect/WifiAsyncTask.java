package com.wifi.wifidirect;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Pair;
import android.widget.Toast;

public class WifiAsyncTask extends AsyncTask<ContentResolver, Void, ArrayList<Pair<String, String>>> {

	private ContentResolver contentResolver;
	private MediaManager mMedia;
	
	ArrayList<Pair<String, String>> titlePathList = new ArrayList<Pair<String,String>>();
	
	private Context context;

	public WifiAsyncTask(Context context, ContentResolver contentResolver, MediaManager mMedia) {
		this.contentResolver = contentResolver;
		this.mMedia = mMedia;
		this.context = context;
	}
	
	@Override
	protected ArrayList<Pair<String, String>> doInBackground(ContentResolver... params) {
		retrieveMediaFiles();
		return titlePathList;
	}

	@Override
	protected void onPostExecute(ArrayList<Pair<String, String>> result) {
		if (result != null) {
			mMedia.setTitlesList(result);
			Pair<String, String> titlePath = result.get(0);
			String title = titlePath.first;
			String path = titlePath.second;
			Toast.makeText(context, "AsyncTask result "+ title + " " + path + " NO: "+result.size(), Toast.LENGTH_SHORT).show();
		}
	}

	public void retrieveMediaFiles() {
		Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor cursor = contentResolver.query(uri, null, null, null, null);
		if (cursor == null) {
		    // query failed, handle error.
		} else if (!cursor.moveToFirst()) {
		    // no media on the device
		} else {
		    int titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
		    int data = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DATA);
		    String filePathUri = Uri.parse(cursor.getString(data)).getPath();
		    System.out.println("*******************************************************************"+uri.getPath());
		    int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
		    int coverPath = cursor.getColumnIndex(android.provider.MediaStore.Audio.AlbumColumns.ALBUM_ART);
		    
		    do {
		       long thisId = cursor.getLong(idColumn);
		       String thisTitle = cursor.getString(titleColumn);
		       //byte[] image = cursor.getBlob(coverPath);
		       // ...process entry...
		       Pair<String, String> titlePath = new Pair<String, String>(thisTitle, filePathUri);
		       titlePathList.add(titlePath);
		    } while (cursor.moveToNext());
		}		
	}
}
