package com.xeii.android.sportstracker;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.xeii.android.sportstracker.Mapper_ContentProvider.MapperContent;
import com.xeii.android.sportstracker.TrackingData.TrackingListener;
import com.xeii.android.sportstracker.TrackingService.LocalBinder;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

public class TrackingDisplay extends FragmentActivity implements TrackingListener {
	
    private static final String TAG = "TrackingDisplayActivity";
    public static final String SESSION_NAME ="SessionName";
	TrackingData mSyncData;
    boolean mBound = false;
	protected PolylineOptions trace = null;
	private static GoogleMap mMap = null;
	private String activityType;
	private Marker me = null;

	//Connection to service
	private ServiceConnection mConnection = new ServiceConnection() {

		
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
        	
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mSyncData = binder.getDataObject();
            mSyncData.setListener(TrackingDisplay.this);
            //mMap.setLocationSource(mSyncData);
            mBound = true;
            
        	Log.d(TAG, "Service connection opened");


        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            mSyncData.setListener(null);
        }
    };


    /**
     *  START OF LIFECYCLE METHODS
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tracking_display);
		((Button) findViewById(R.id.trackingFinish)).setEnabled(false);
		int errCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		ConnectionResult result = new ConnectionResult(errCode, null);
		if(result.getErrorCode() == ConnectionResult.SUCCESS) {
			Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.map);
			SupportMapFragment mapFragment = (SupportMapFragment)fragment;
			mMap = mapFragment.getMap();
		} else {
			Log.d(TAG, "Error in initializing Google Map - ERROR CODE: " + result.getErrorCode());
		}
		update();
		
        // Bind to LocalService
        Intent intent = new Intent(this, TrackingService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        
		Log.d(TAG, "Activity Created");

	}
	
	@Override
	public void finish() {
		
		super.finish();
		if (mBound) {
    		mSyncData.setListener(null);
    		unbindService(mConnection);
    		mBound = false;
        }
	}
	
	@Override
	public void onBackPressed() {
	    new AlertDialog.Builder(this)
	        .setIcon(android.R.drawable.ic_dialog_alert)
	        .setTitle("Closing Activity")
	        .setMessage("Are you sure you want to close this activity?")
	        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
	    {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            finish();    
	        }

	    })
	    .setNegativeButton("No", null)
	    .show();
	}
	
	@Override
    protected void onStart() {
        super.onStart();
        if (!mBound) {
    		Intent intent = new Intent(this, TrackingService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }

		Log.d(TAG, "Activity Started");

    }

    @Override
    protected void onStop() {
        super.onStop();

		trace = null;
		Log.d(TAG, "Activity Stopped");

    }
    
    @Override
	protected void onDestroy() {
    	super.onDestroy();		
    	if (mBound) {
    		mSyncData.setListener(null);
    		unbindService(mConnection);
    		mBound = false;
        }
		Log.d(TAG, "Activity Destroyed");
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_tracking_display, menu);
		return true;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "Activity paused");
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "Activity resumed");
		
	}
	/**
     *  END OF LIFECYCLE METHODS
     */
	
	
	/**
     *  START OF RESPONSES TO UI ELEMENTS
     */
	public void finishTracking(View view) {
		mSyncData.stopSession();
		((Chronometer) findViewById(R.id.timer)).stop();
		long elapsedMillis = SystemClock.elapsedRealtime() - ((Chronometer) findViewById(R.id.timer)).getBase();
		Log.d("Elapsed time in seconds:", "" + elapsedMillis/1000);	
    	Intent intent = new Intent(this, MapActivity.class);
    	intent.putExtra(SESSION_NAME, mSyncData.getSessionName());
    	startActivity(intent);
    }
	
	public void startTracking(View view) {
		long dtMili = System.currentTimeMillis();
		Date dt = new Date(dtMili);
		mSyncData.setSessionName(activityType+": "+ dt);
		mSyncData.startSession();
		((Chronometer) findViewById(R.id.timer)).setBase(SystemClock.elapsedRealtime());
		((Chronometer) findViewById(R.id.timer)).start();
		((Button) findViewById(R.id.startTrackingButton)).setEnabled(false);
		((Button) findViewById(R.id.trackingFinish)).setEnabled(true);
	}
	
	/**
     *  END OF RESPONSES TO UI ELEMENTS
     */
	
	
	
	private void update() {	
		
		Intent intent = getIntent();
		String id = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
		TextView headerTextView = (TextView) findViewById(R.id.trackingHeader);
		Log.d("Tracking", "update tracking header: " + id);
		activityType = id;
		if(id.equals("bike"))
			headerTextView.setText(R.string.string_bike);
		if(id.equals("walk"))
			headerTextView.setText(R.string.string_walk);
		if(id.equals("run"))
			headerTextView.setText(R.string.string_run);
		if(id.equals("ski"))
			headerTextView.setText(R.string.string_ski);
	}
	
	
	
	//Overriden method of our TrackingListener this class implements
	@Override
	public void updateLocation(Location location) {
		/**
		 * Update current location on map
		 */
		//What to do with the new location?
		Log.d(TAG, "Current location = " + location.toString());
		
		//HINT: get the location street address from the coordinates
		Geocoder geocoder = new Geocoder(getApplicationContext());
		try {
			List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			for(int i=0; i<addresses.size(); i++) {
				Address address = addresses.get(i);
				
				String street = "";
				for(int j=0; j < address.getMaxAddressLineIndex(); j++) {
					street += address.getAddressLine(j) + " ";
				}
				
			}
		} catch (IOException e) {
			Log.d(TAG,"Failed to reverse geo-location to an address, do you have internet?");
			//e.printStackTrace();
		}
		
		
		//Remove previous marker if existent
		if( me != null ) {
			me.remove();
		}
	
		//Should be configurable or adjustable as you zoom in/out of the map
		int zoom_level = 14;
		
		MarkerOptions options = new MarkerOptions();
		LatLng coordinates = new LatLng(location.getLatitude(), location.getLongitude());
		options.position(coordinates);
		options.title("You");
		options.snippet("Accuracy: " + location.getAccuracy() + " meters");
		me = mMap.addMarker(options);
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, zoom_level));
		
		//Check if we started tracing the path yet or not
		if( trace == null ) {
			trace = new PolylineOptions();
			trace.color(Color.BLUE);
		}
		//update the trace
		if(mSyncData.isTracking()) {
			trace.add(coordinates);
			mMap.addPolyline(trace);
			((TextView) findViewById(R.id.speed)).setText(""+Math.round(location.getSpeed()*3.6) + " km/h");
			((TextView) findViewById(R.id.distance)).setText(""+Math.round(mSyncData.getDistance())+ " m");
		}
	}

}

