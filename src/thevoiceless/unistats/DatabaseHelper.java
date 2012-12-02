package thevoiceless.unistats;

import java.util.Calendar;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper
{
	private static Calendar cal;
	// Name of database and tables
	private static final String DATABASE_NAME = "unistats.db";
	private static final String TABLE_RIDES = "rides";
	private static final String TABLE_ACHIEVEMENTS = "achievements";
	// Ride column names
	public static final String RIDE_COL_NAME = "name";
	public static final String RIDE_COL_DATE = "date";
	public static final String RIDE_COL_GPS = "gps";
	public static final String RIDE_COL_DIST = "distance";
	public static final String RIDE_COL_PED = "pedals";
	private static final String ALL_RIDE_COLS = RIDE_COL_NAME + ", " 
			+ RIDE_COL_DATE + ", " 
			+ RIDE_COL_GPS + ", "
			+ RIDE_COL_DIST + ", " 
			+ RIDE_COL_PED;
	// Ride column integers
	private static final int RIDE_NAME_INT = 1;
	private static final int RIDE_DATE_INT = 2;
	private static final int RIDE_GPS_INT = 3;
	private static final int RIDE_DIST_INT = 4;
	private static final int RIDE_PED_INT = 5;
	
	private static final int SCHEMA_VERSION = 1;
	
	// SQL statements
	// Create database
	private static final String DB_CREATE = "CREATE TABLE " + TABLE_RIDES
			+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ RIDE_COL_NAME + " TEXT, "
			+ RIDE_COL_DATE + " INTEGER, "
			+ RIDE_COL_GPS + " INTEGER, "
			+ RIDE_COL_DIST + " REAL, "
			+ RIDE_COL_PED + " REAL);";
	// Match provided arguments
	private static final String ID_MATCH_ARGS = "_ID=?";
	// Get all by ID
	private static final String DB_GET_RIDE_BY_ID = "SELECT _id, " + ALL_RIDE_COLS
			+ " FROM " + TABLE_RIDES 
			+ " WHERE " + ID_MATCH_ARGS;
	// Get all and order by given arguments
	private static final String DB_GET_ALL_RIDES_ORDER_BY = "SELECT _id, " + ALL_RIDE_COLS
			+ " FROM " + TABLE_RIDES
			+ " ORDER BY ";
	// Delete from rides table
	private static final String DB_DELETE_RIDE = "DELETE FROM " + TABLE_RIDES 
			+ " WHERE " + ID_MATCH_ARGS;
	
	
	public DatabaseHelper(Context context)
	{
		super(context, DATABASE_NAME, null, SCHEMA_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(DB_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// Switch with no breaks, upgrades will cascade
		switch (oldVersion)
		{
			// Upgrade from v1
			case 1:
			{
			}
		}		
	}
	
	/* DATABASE MODIFICATION */
	
	public long insertRide(String name, long date, int gps, double distance, double pedals)
	{
		ContentValues cv = new ContentValues();
		cv.put(RIDE_COL_NAME, name);
		cv.put(RIDE_COL_DATE, date);
		cv.put(RIDE_COL_GPS, gps);
		cv.put(RIDE_COL_DIST, distance);
		cv.put(RIDE_COL_PED, pedals);
		return getWritableDatabase().insert(TABLE_RIDES, RIDE_COL_NAME, cv);
	}
	
	public int updateRide(String id, String name, long date, int gps, double distance, double pedals)
	{
		ContentValues cv = new ContentValues();
		String[] args = {id};
		
		cv.put(RIDE_COL_NAME, name);
		cv.put(RIDE_COL_DATE, date);
		cv.put(RIDE_COL_GPS, gps);
		cv.put(RIDE_COL_DIST, distance);
		cv.put(RIDE_COL_PED, pedals);
		
		return getWritableDatabase().update(TABLE_RIDES, cv, ID_MATCH_ARGS, args);
	}
	
	public boolean deleteRide(String id)
	{
		String[] args = {id};
		int result = getWritableDatabase().delete(TABLE_RIDES, ID_MATCH_ARGS, args);
		return (result != 0) ? true : false;
	}
	
	/* GETTERS */
	
	public Cursor getAllRides(String orderBy)
	{
		return getReadableDatabase().rawQuery(DB_GET_ALL_RIDES_ORDER_BY + orderBy, null);
	}
	
	public Cursor getRideById(String id)
	{
		String[] args = {id};
		return getReadableDatabase().rawQuery(DB_GET_RIDE_BY_ID, args);
	}
	
	public String getRideName(Cursor c)
	{
		return c.getString(RIDE_NAME_INT);
	}
	
	public Date getRideDate(Cursor c)
	{
		cal = Calendar.getInstance();
		cal.setTimeInMillis((long) (c.getLong(RIDE_DATE_INT) * 1000));
		return cal.getTime();
	}
	
	public boolean getRideUseGPS(Cursor c)
	{
		return c.getInt(RIDE_GPS_INT) == 1;
	}
	
	public String getRideDistance(Cursor c)
	{
		return c.getString(RIDE_DIST_INT);
	}
	
	public String getRidePedals(Cursor c)
	{
		return c.getString(RIDE_PED_INT);
	}
}