package com.xeii.android.sportstracker;

import java.io.File;
import java.util.HashMap;

import com.xeii.android.sportstracker.TrackingService;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;

public class Mapper_ContentProvider extends ContentProvider {
	
	public static final String AUTHORITY = "com.xeii.android.sportstracker";
	
	//ContentProvider query path
	private static final int MAPPER = 1;
	private static final int MAPPER_ID = 2;
	
	public static final class MapperContent implements BaseColumns {
		private MapperContent() {};
		
		public static final Uri CONTENT_URI = Uri.parse("content://"+ Mapper_ContentProvider.AUTHORITY+"/mapper");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mapper.data";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mapper.data";
		
		public static final String MAPPER_ID = "_id";
		public static final String MAPPER_TIMESTAMP = "timestamp";
		public static final String MAPPER_LATITUDE = "latitude";
		public static final String MAPPER_LONGITUDE = "longitude";
		public static final String MAPPER_SPEED = "speed";
		public static final String MAPPER_ALTITUDE = "altitude";
		public static final String MAPPER_BEARING = "bearing";
		public static final String MAPPER_ACCURACY = "accuracy";
		public static final String MAPPER_PROVIDER = "provider";
		public static final String MAPPER_SESSION = "session";
	}
	
	private static SQLiteDatabase database = null;
	private static final int DATABASE_VERSION = 1;
	
	//Database filename in external storage
	private static final String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/Mapper/mapper.db";
	
	//Tables in the database
	private static final String[] DATABASE_TABLES = {
		"mapper"
	};
	
	//Table fields
	private static final String[] TABLES_FIELDS = {
		//Mapper data
		MapperContent.MAPPER_ID + " integer primary key autoincrement," +
		MapperContent.MAPPER_TIMESTAMP + " real default 0," +
		MapperContent.MAPPER_LATITUDE + " real default 0," +
		MapperContent.MAPPER_LONGITUDE + " real default 0," +
		MapperContent.MAPPER_SPEED + " real default 0," +
		MapperContent.MAPPER_ALTITUDE + " real default 0," +
		MapperContent.MAPPER_BEARING + " real default 0," +
		MapperContent.MAPPER_ACCURACY + " real default 0," +
		MapperContent.MAPPER_PROVIDER + " text default '', " +
		MapperContent.MAPPER_SESSION + " text default ''"
	};
	
	private static UriMatcher uriMatcher = null;
	private static HashMap<String, String> mapperProjection = null;
	static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(Mapper_ContentProvider.AUTHORITY, DATABASE_TABLES[0], MAPPER);
        uriMatcher.addURI(Mapper_ContentProvider.AUTHORITY, DATABASE_TABLES[0]+"/#", MAPPER_ID);
        
        mapperProjection = new HashMap<String, String>();
        mapperProjection.put(MapperContent.MAPPER_ID, MapperContent.MAPPER_ID);
        mapperProjection.put(MapperContent.MAPPER_TIMESTAMP, MapperContent.MAPPER_TIMESTAMP);
        mapperProjection.put(MapperContent.MAPPER_LATITUDE, MapperContent.MAPPER_LATITUDE);
        mapperProjection.put(MapperContent.MAPPER_LONGITUDE, MapperContent.MAPPER_LONGITUDE);
        mapperProjection.put(MapperContent.MAPPER_SPEED, MapperContent.MAPPER_SPEED);
        mapperProjection.put(MapperContent.MAPPER_ALTITUDE, MapperContent.MAPPER_ALTITUDE);
        mapperProjection.put(MapperContent.MAPPER_BEARING, MapperContent.MAPPER_BEARING);
        mapperProjection.put(MapperContent.MAPPER_ACCURACY, MapperContent.MAPPER_ACCURACY);
        mapperProjection.put(MapperContent.MAPPER_PROVIDER, MapperContent.MAPPER_PROVIDER);
        mapperProjection.put(MapperContent.MAPPER_SESSION, MapperContent.MAPPER_SESSION);
    }
	
	private static MapperDatabase databaseHelper = null;
	//Helper that creates the database automatically for you on the external storage
	public class MapperDatabase extends SQLiteOpenHelper {
        public MapperDatabase(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
            
            File folders = new File(Environment.getExternalStorageDirectory()+"/Mapper/");
            folders.mkdirs();
            
            File db = new File(name);
            SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(db, null);
            onCreate(database);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            for (int i=0; i < DATABASE_TABLES.length;i++)
            {
               db.execSQL("CREATE TABLE IF NOT EXISTS "+DATABASE_TABLES[i] +" ("+TABLES_FIELDS[i]+");");
               Log.d(TrackingService.TAG,"Created table "+DATABASE_TABLES[i]);
            }
            db.close();
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            for (int i=0; i < DATABASE_TABLES.length;i++)
            {
                db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLES[i]);
                Log.d(TrackingService.TAG,"Dropped table "+DATABASE_TABLES[i]);
            }
            onCreate(db);
        }
        @Override
        public  void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
        }
        
        @Override
        public  SQLiteDatabase getWritableDatabase() {
            return SQLiteDatabase.openDatabase(DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE);
        }
        @Override
        public  SQLiteDatabase getReadableDatabase() {
            return SQLiteDatabase.openDatabase(DATABASE_NAME, null, SQLiteDatabase.OPEN_READONLY); 
        }
    } 
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		if( database == null || ! database.isOpen()) database = databaseHelper.getWritableDatabase();
        
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case MAPPER:
                count = database.delete(DATABASE_TABLES[0], selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
	        case MAPPER:
	            return MapperContent.CONTENT_TYPE;
	        case MAPPER_ID:
	            return MapperContent.CONTENT_ITEM_TYPE;
	        default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if( database == null || ! database.isOpen()) database = databaseHelper.getWritableDatabase();
        
        ContentValues values = (initialValues != null) ? new ContentValues(initialValues) : new ContentValues();
        
        switch(uriMatcher.match(uri)) {
            case MAPPER:
                long mapper_id = database.insert(DATABASE_TABLES[0], MapperContent.MAPPER_TIMESTAMP, values);
                
                if (mapper_id > 0) {
                    Uri mapperUri = ContentUris.withAppendedId(MapperContent.CONTENT_URI, mapper_id);
                    getContext().getContentResolver().notifyChange(mapperUri, null);
                    return mapperUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
	}

	@Override
	public boolean onCreate() {
		if(databaseHelper == null) databaseHelper = new MapperDatabase(getContext(), DATABASE_NAME, null, DATABASE_VERSION);
        database = databaseHelper.getWritableDatabase();
        return (databaseHelper != null);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		if( database == null || ! database.isOpen()) database = databaseHelper.getWritableDatabase();
        
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (uriMatcher.match(uri)) {
            case MAPPER:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(mapperProjection);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Cursor c = qb.query(database, projection, selection, selectionArgs, null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        }catch ( IllegalStateException e ) {
            Log.e(TrackingService.TAG,e.getMessage());
            return null;
        }
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		
		if( database == null || ! database.isOpen()) database = databaseHelper.getWritableDatabase();
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case MAPPER:
                count = database.update(DATABASE_TABLES[0], values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}
}
