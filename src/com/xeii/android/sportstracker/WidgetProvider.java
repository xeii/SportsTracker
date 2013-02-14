package com.xeii.android.sportstracker;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {
	
	private static final String	EXTRA_MESSAGE	= "com.xeii.android.sportstracker.MESSAGE";
	private static final String TAG = "WidgetProvider";

	@Override 
    public void onUpdate(Context ctxt, AppWidgetManager appWidgetManager, int[] appWidgetIds) {  
		for (int i=0; i<appWidgetIds.length; i++) {
	  	    Log.d("Tracking", "WIDGET UPDATE"); 
			
			RemoteViews views=new RemoteViews(ctxt.getPackageName(), R.layout.widget_layout);
			  
			Intent clickIntentWalk=new Intent(ctxt, TrackingDisplay.class);	
			clickIntentWalk.putExtra(EXTRA_MESSAGE, "walk");
			PendingIntent clickPIWalk=PendingIntent
			                          .getActivity(ctxt, 0,
			                        		  clickIntentWalk,
			                                        PendingIntent.FLAG_UPDATE_CURRENT);
			
			Intent clickIntentRun=new Intent(ctxt, TrackingDisplay.class);	
			clickIntentRun.putExtra(EXTRA_MESSAGE, "run");
			PendingIntent clickPIRun=PendingIntent
			                          .getActivity(ctxt, 1,
			                        		  clickIntentRun,
			                                        PendingIntent.FLAG_UPDATE_CURRENT);
			
			Intent clickIntentBike=new Intent(ctxt, TrackingDisplay.class);	
			clickIntentBike.putExtra(EXTRA_MESSAGE, "bike");
			PendingIntent clickPIBike=PendingIntent
			                          .getActivity(ctxt, 2,
			                        		  clickIntentBike,
			                                        PendingIntent.FLAG_UPDATE_CURRENT);
			
			Intent clickIntentSki=new Intent(ctxt, TrackingDisplay.class);	
			clickIntentSki.putExtra(EXTRA_MESSAGE, "ski");
			PendingIntent clickPISki=PendingIntent
			                          .getActivity(ctxt, 3,
			                        		  clickIntentSki,
			                                        PendingIntent.FLAG_UPDATE_CURRENT);
			  
			views.setOnClickPendingIntent(R.id.widgetWalk, clickPIWalk);
			views.setOnClickPendingIntent(R.id.widgetRun, clickPIRun);
			views.setOnClickPendingIntent(R.id.widgetSki, clickPISki);
			views.setOnClickPendingIntent(R.id.widgetBike, clickPIBike);

			appWidgetManager.updateAppWidget(appWidgetIds[i], views);
		}
		super.onUpdate(ctxt, appWidgetManager, appWidgetIds);
	} 
	
}
