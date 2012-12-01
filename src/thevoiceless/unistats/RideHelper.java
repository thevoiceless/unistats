package thevoiceless.unistats;

import java.util.Calendar;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RideHelper extends SQLiteOpenHelper
{
	private static Calendar cal;
	// Name of database and main rides table
	private static final String DATABASE_NAME = "rides.db";
	private static final String TABLE_RIDES = "rides";
	// Column names
	private static final String COL_NAME = "name";
	private static final String COL_DATE = "date";
	private static final String COL_GPS = "gps";
	private static final String COL_DIST = "distance";
	private static final String COL_PED = "pedals";
	private static final String ALL_COLS = COL_NAME + ", " 
			+ COL_DATE + ", " 
			+ COL_GPS + ", "
			+ COL_DIST + ", " 
			+ COL_PED;
	// Column integers
	private static final int NAME_INT = 1;
	private static final int DATE_INT = 2;
	private static final int GPS_INT = 3;
	private static final int DIST_INT = 4;
	private static final int PED_INT = 5;
	
	private static final int SCHEMA_VERSION = 1;
	
	// SQL statements
	// Create database
	private static final String DB_CREATE = "CREATE TABLE " + TABLE_RIDES
			+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_NAME + " TEXT, "
			+ COL_DATE + " INTEGER, "
			+ COL_GPS + " INTEGER, "
			+ COL_DIST + " REAL, "
			+ COL_PED + " REAL);";
	// Match provided arguments
	private static final String ID_MATCH_ARGS = "_ID=?";
	// Get all by ID
	private static final String DB_GET_BY_ID = "SELECT _id, " + ALL_COLS
			+ " FROM " + TABLE_RIDES 
			+ " WHERE " + ID_MATCH_ARGS;
	// Get all and order by given arguments
	private static final String DB_GET_ALL_ORDER_BY = "SELECT _id, " + ALL_COLS
			+ " FROM " + TABLE_RIDES
			+ " ORDER BY ";
	// Delete from rides table
	private static final String DB_DELETE_RIDE = "DELETE FROM " + TABLE_RIDES 
			+ " WHERE " + ID_MATCH_ARGS;
	
	
	public RideHelper(Context context)
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
	
	/* DB MODIFICATION */
	
	public void insert(String name, long date, int gps, double distance, double pedals)
	{
		ContentValues cv = new ContentValues();
		cv.put(COL_NAME, name);
		cv.put(COL_DATE, date);
		cv.put(COL_GPS, gps);
		cv.put(COL_DIST, distance);
		cv.put(COL_PED, pedals);
		getWritableDatabase().insert(TABLE_RIDES, COL_NAME, cv);
	}
	
	public boolean delete(String id)
	{
		String[] args = {id};
		int result = getWritableDatabase().delete(TABLE_RIDES, ID_MATCH_ARGS, args);
		return (result != 0) ? true : false;
	}
	
	/* GETTERS */
	
	public Cursor getAll(String orderBy)
	{
		return getReadableDatabase().rawQuery(DB_GET_ALL_ORDER_BY + orderBy, null);
	}
	
	public Cursor getById(String id)
	{
		String[] args = {id};
		return getReadableDatabase().rawQuery(DB_GET_BY_ID, args);
	}
	
	public String getName(Cursor c)
	{
		return c.getString(NAME_INT);
	}
	
	public Date getDate(Cursor c)
	{
		cal = Calendar.getInstance();
		cal.setTimeInMillis((long) (c.getLong(DATE_INT) * 1000));
		return cal.getTime();
	}
	
	public boolean getUseGPS(Cursor c)
	{
		return c.getInt(GPS_INT) == 1;
	}
	
	public String getDistance(Cursor c)
	{
		return c.getString(DIST_INT);
	}
	
	public String getPedals(Cursor c)
	{
		return c.getString(PED_INT);
	}
}
