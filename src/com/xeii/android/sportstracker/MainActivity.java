package com.xeii.android.sportstracker;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	
	public final static String EXTRA_MESSAGE = "com.xeii.android.sportstracker.MESSAGE";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public void openTracking(View view) {
    	String id = view.getTag().toString();    	
    	Intent intent = new Intent(this, TrackingDisplay.class);
    	intent.putExtra(EXTRA_MESSAGE, id);
    	startActivity(intent);
    }
    
    public void openArchive(View view) {
    	Intent intent = new Intent(this, ArchiveActivity.class);
    	startActivity(intent);
    }
    
}
