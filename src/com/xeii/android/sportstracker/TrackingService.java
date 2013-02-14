package com.xeii.android.sportstracker;



import com.xeii.android.sportstracker.Mapper_ContentProvider.MapperContent;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class TrackingService extends Service implements LocationListener {
	
	public static final String TAG = "TrackingService";
	private final IBinder mBinder = new LocalBinder();
	private static LocationManager locationManager = null;
	private TrackingData syncData;
	
	
	/**
	 * INIT START
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		syncData = new TrackingData(getContentResolver());
		Log.d(TAG, "SYNC-code:"+syncData.hashCode());
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		
		long minTime = 5000; //milliseconds
		float minDistance = 0; //meters
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, this);
		
		Log.d(TAG, "Service started");
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(TAG, "onBind - tracking service");
		return mBinder;
	}
	

	public class LocalBinder extends Binder {
		TrackingData getDataObject() {
            // Return instance of data object that holds the data UI needs 
            return syncData;
        }
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
		locationManager.removeUpdates(this);
	}

	/**
	 * INIT END
	 */
	
	/**
	 * LOCATION SOURCE IMPLEMENTED METHODS
	 */
	@Override
	public void onLocationChanged(Location new_location) {
		Log.d(TAG, "onLocationChanged - Location Listener");
		//The location has been updated
		syncData.updateLocation(new_location);
	}

	@Override
	public void onProviderDisabled(String provider) {
		//TODO
		//What happens if the user disables GPS/Network?
	}

	@Override
	public void onProviderEnabled(String provider) {
		//TODO
		//What happens if the user enables GPS/Network?
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		//TODO
		//A change in the force has been detected :)
	}

	
}
