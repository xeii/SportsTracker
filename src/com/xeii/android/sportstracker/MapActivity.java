package com.xeii.android.sportstracker;

import java.util.concurrent.TimeUnit;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.xeii.android.sportstracker.Mapper_ContentProvider.MapperContent;

import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MapActivity extends FragmentActivity {
	
	private static final String TAG = "MapActivity";
	
	private static GoogleMap mMap = null;
	private PolylineOptions trace;
	private TrackingData mSyncData;
	private Cursor data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		if(getContentResolver() != null)
			mSyncData = new TrackingData(getContentResolver());
		
		int errCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		ConnectionResult result = new ConnectionResult(errCode, null);
		if(result.getErrorCode() == ConnectionResult.SUCCESS) {
			Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.mapFinal);
			SupportMapFragment mapFragment = (SupportMapFragment)fragment;
			mMap = mapFragment.getMap();
		} else {
			Log.d(TAG, "Error in initializing Google Map - ERROR CODE: " + result.getErrorCode());
		}
		
		mSyncData.setSessionName(getIntent().getStringExtra(TrackingDisplay.SESSION_NAME));
		data = mSyncData.getSessionData();
		setHeader();
		drawTrace();
		calcAvgSpeed();
		calcDistance();
		calcTime();
		
	}
	
	private void setHeader() {
		String text = getIntent().getStringExtra(TrackingDisplay.SESSION_NAME);
		if(text.startsWith("walk"))
			((TextView) findViewById(R.id.trackingHeaderFinal)).setText("Walking");
		if(text.startsWith("run"))
			((TextView) findViewById(R.id.trackingHeaderFinal)).setText("Running");
		if(text.startsWith("ski"))
			((TextView) findViewById(R.id.trackingHeaderFinal)).setText("Skiing");
		if(text.startsWith("bike"))
			((TextView) findViewById(R.id.trackingHeaderFinal)).setText("Bicycling");
	}
	
	private void calcTime() {
		long startTimeStamp = 0;
		long endTimeStamp = 0;
		
		if( data != null && data.moveToFirst() ) {
			startTimeStamp = data.getLong(data.getColumnIndex(MapperContent.MAPPER_TIMESTAMP));
		}
		if( data != null && data.moveToLast() ) {
			endTimeStamp = data.getLong(data.getColumnIndex(MapperContent.MAPPER_TIMESTAMP));
		}
		long millis = endTimeStamp - startTimeStamp;
		String time = String.format("%02d:%02d:%02d", 
			    TimeUnit.MILLISECONDS.toHours(millis),
			    TimeUnit.MILLISECONDS.toMinutes(millis) - 
			    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
			    TimeUnit.MILLISECONDS.toSeconds(millis) - 
			    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
		((TextView) findViewById(R.id.timerFinal)).setText(time);
	}
	
	private void calcDistance() {
		double distance= 0;
		Location location = null;
		Location prevLocation = null;
		
		if( data != null && data.moveToFirst() ) {
			do{			
				if(prevLocation == null) {
					prevLocation = new Location("database");
					prevLocation.setLatitude(
							data.getDouble(data.getColumnIndex(MapperContent.MAPPER_LATITUDE)));
					prevLocation.setLongitude(
							data.getDouble(data.getColumnIndex(MapperContent.MAPPER_LONGITUDE)));
				} else {
					location = new Location("database");
					location.setLatitude(
							data.getDouble(data.getColumnIndex(MapperContent.MAPPER_LATITUDE)));
					location.setLongitude(
							data.getDouble(data.getColumnIndex(MapperContent.MAPPER_LONGITUDE)));
					distance += location.distanceTo(prevLocation);
					prevLocation = location;
				}
				
			}while(data.moveToNext());
			if(distance < 1000)
				((TextView) findViewById(R.id.distanceFinal)).setText(Math.round(distance)+ " m");
			else {
				double longerDist = (double) Math.round(distance / 100) / 10;
				((TextView) findViewById(R.id.distanceFinal)).setText(longerDist+ " km");
			
			}
		}
	}
	
	private void drawTrace() {
		trace = new PolylineOptions();
		trace.color(Color.BLUE);
		
		if( data != null && data.moveToFirst() ) {
			do{			
				LatLng coordinates = new LatLng(
						data.getDouble(data.getColumnIndex(MapperContent.MAPPER_LATITUDE)), 
						data.getDouble(data.getColumnIndex(MapperContent.MAPPER_LONGITUDE)));
				Log.d("Tracking - trace:", coordinates.latitude+ " - "+coordinates.longitude);
				trace.add(coordinates);
			}while(data.moveToNext());
			
			mMap.addPolyline(trace);
			
			data.moveToFirst();
			LatLng coordinates = new LatLng(
					data.getDouble(data.getColumnIndex(MapperContent.MAPPER_LATITUDE)), 
					data.getDouble(data.getColumnIndex(MapperContent.MAPPER_LONGITUDE)));		
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 14));
			
			Log.d("Tracking", "Map drawn from database");
		}
	}
	
	private void calcAvgSpeed() {
		int i =0;
		double avgSpeed = 0;
		double speed = 0;
		
		if( data != null && data.moveToFirst() ) {		
			do{			
				speed = data.getDouble(data.getColumnIndex(MapperContent.MAPPER_SPEED));
				if(speed < 1000) {
					i++;
					avgSpeed += speed;
				}
			}while(data.moveToNext());
			
			((TextView) findViewById(R.id.speedFinal)).setText(Math.round((avgSpeed/i)*3.6)+ " km/h");
			mMap.addPolyline(trace);
			Log.d("Tracking", "Map drawn from database");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_map, menu);
		return true;
	}

}
