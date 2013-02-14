package com.xeii.android.sportstracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.maps.model.LatLng;
import com.xeii.android.sportstracker.Mapper_ContentProvider.MapperContent;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ArchiveActivity extends Activity {
	
	private TrackingData mSyncData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_archive);
		
		mSyncData = new TrackingData(getContentResolver());
		inflateList();
	}
	
	private void inflateList() {
		ListView listView = (ListView) findViewById(R.id.archiveList);
		Cursor data = mSyncData.getSessions();
		
		List<Map<String, String>> sessionList = new ArrayList<Map<String,String>>();
		
		
		if( data != null && data.moveToFirst() ) {
			do{			
				HashMap<String, String> session = new HashMap<String, String>();
			    session.put("session", 
			    			data.getString(data.getColumnIndex(MapperContent.MAPPER_SESSION)));
				sessionList.add(session);
				
			}while(data.moveToNext());
			
			SimpleAdapter simpleAdpt = new SimpleAdapter(
					this, 
					sessionList, 
					android.R.layout.simple_list_item_1, 
					new String[] {"session"},
					new int[] {android.R.id.text1});
			
			listView.setAdapter(simpleAdpt);
			
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				 
				@Override
				public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
					// TODO Auto-generated method stub
					TextView clickedView = (TextView) view;
					Intent i = new Intent(ArchiveActivity.this, MapActivity.class);
					i.putExtra(TrackingDisplay.SESSION_NAME, clickedView.getText());
					startActivity(i);
				}
			});
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_archive, menu);
		return true;
	}

}
