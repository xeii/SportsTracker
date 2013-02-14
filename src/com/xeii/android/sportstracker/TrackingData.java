package com.xeii.android.sportstracker;

import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;
import com.xeii.android.sportstracker.Mapper_ContentProvider.MapperContent;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

public class TrackingData {
	
	public static final String	TAG	= "TrackingData";
	private TrackingListener listener;
	public boolean sessionOn = false;
	private ContentResolver contentResolver;
	public String sessionName = "";
	public double distance;
	private Location prevLocation;

	
	public TrackingData(ContentResolver contentResolver) {
		this.contentResolver = contentResolver;
	}

	public void setListener(TrackingListener listener) {
		this.listener = listener;
	}
	
	public void startSession() { sessionOn = true; distance = 0; prevLocation=null;}
	public void stopSession() { sessionOn = false; }
	public void setSessionName(String name) { sessionName =name; }
	public String getSessionName() { return sessionName; }
	public boolean isTracking() { return sessionOn; }
	public double getDistance() { return distance; }
	
	
	public void updateLocation(Location location) {
		if(this.listener != null) {
			listener.updateLocation(location);
			if(sessionOn) {
				insertDatabase(location);
				updateDistance(location);
			}
		}
	}
	
	private void updateDistance(Location location) {	
		if(prevLocation != null) {			
			distance += location.distanceTo(prevLocation);
			prevLocation = location;
			Log.d(TAG, "distance so far: "+ distance);
		} else {
			prevLocation = location;
		}
		
	}

	private void insertDatabase(Location location) {
		//Collect location data
		ContentValues rowData = new ContentValues();
		rowData.put(MapperContent.MAPPER_TIMESTAMP, System.currentTimeMillis());
		rowData.put(MapperContent.MAPPER_LATITUDE, location.getLatitude());
		rowData.put(MapperContent.MAPPER_LONGITUDE, location.getLongitude());
		rowData.put(MapperContent.MAPPER_SPEED, location.getSpeed());
		rowData.put(MapperContent.MAPPER_ALTITUDE, location.getAltitude());
		rowData.put(MapperContent.MAPPER_BEARING, location.getBearing());
		rowData.put(MapperContent.MAPPER_ACCURACY, location.getAccuracy());
		rowData.put(MapperContent.MAPPER_PROVIDER, location.getProvider());
		rowData.put(MapperContent.MAPPER_SESSION, sessionName);
		
		//Insert to database
		contentResolver.insert(MapperContent.CONTENT_URI, rowData);
		
		Log.d(TAG, "inserted a row in database");

	}
	
	public Cursor getSessionData() {
		Cursor cursor = contentResolver.query(MapperContent.CONTENT_URI, null, MapperContent.MAPPER_SESSION+" = "+"'"+this.sessionName+"'", null, null);
		return cursor;
	}
	
	public interface TrackingListener {
		void updateLocation(Location location);
	}

	public Cursor getSessions() {
		String selection = "1=1) GROUP BY (" + MapperContent.MAPPER_SESSION;
		Cursor data = contentResolver.query(MapperContent.CONTENT_URI, null, selection, null, null); 	
		return data;
	}	
}
