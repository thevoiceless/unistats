package thevoiceless.unistats;

import java.util.Calendar;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author riley
 *
 */
/**
 * @author riley
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper
{
	private static Calendar cal;
	// Name of database and tables
	private static final String DATABASE_NAME = "unistats.db";
	private static final String TABLE_RIDES = "rides";
	private static final String TABLE_GOALS = "goals";
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
	public static final int RIDE_NAME_INT = 1;
	public static final int RIDE_DATE_INT = 2;
	public static final int RIDE_GPS_INT = 3;
	public static final int RIDE_DIST_INT = 4;
	public static final int RIDE_PED_INT = 5;
	// Goal column names
	public static final String GOAL_COL_NAME = "name";
	public static final String GOAL_COL_DATE = "date";
	public static final String GOAL_COL_DIST = "distance";
	public static final String GOAL_COL_PED = "pedals";
	private static final String ALL_GOAL_COLS = GOAL_COL_NAME + ", "
			+ GOAL_COL_DATE + ", "
			+ GOAL_COL_DIST + ", "
			+ GOAL_COL_PED;
	// Goal column integers
	public static final int GOAL_NAME_INT = 1;
	public static final int GOAL_DATE_INT = 2;
	public static final int GOAL_DIST_INT = 3;
	public static final int GOAL_PED_INT = 4;
	
	private static final int SCHEMA_VERSION = 1;
	
	// SQL statements
	// Create database
	private static final String[] DB_CREATE = 
		{ "CREATE TABLE " + TABLE_RIDES
			+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ RIDE_COL_NAME + " TEXT, "
			+ RIDE_COL_DATE + " INTEGER, "
			+ RIDE_COL_GPS + " INTEGER, "
			+ RIDE_COL_DIST + " REAL, "
			+ RIDE_COL_PED + " INTEGER);",
			"CREATE TABLE " + TABLE_GOALS
			+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ GOAL_COL_NAME + " TEXT, "
			+ GOAL_COL_DATE + " INTEGER, "
			+ GOAL_COL_DIST + " REAL, "
			+ GOAL_COL_PED + " INTEGER);" };
	// Match provided arguments
	private static final String ID_MATCH_ARGS = "_ID=?";
	// Get all by ID
	private static final String DB_GET_RIDE_BY_ID = "SELECT _id, " + ALL_RIDE_COLS
			+ " FROM " + TABLE_RIDES 
			+ " WHERE " + ID_MATCH_ARGS;
	private static final String DB_GET_GOAL_BY_ID = "SELECT _id, " + ALL_GOAL_COLS
			+ " FROM " + TABLE_GOALS
			+ " WHERE " + ID_MATCH_ARGS;
	// Get all and order by given arguments
	private static final String DB_GET_ALL_RIDES_ORDER_BY = "SELECT _id, " + ALL_RIDE_COLS
			+ " FROM " + TABLE_RIDES
			+ " ORDER BY ";
	private static final String DB_GET_ALL_GOALS_ORDER_BY = "SELECT _id, " + ALL_GOAL_COLS
			+ " FROM " + TABLE_GOALS
			+ " ORDER BY ";
	// Delete from table
	private static final String DB_DELETE_RIDE = "DELETE FROM " + TABLE_RIDES 
			+ " WHERE " + ID_MATCH_ARGS;
	private static final String DB_DELETE_GOAL = "DELETE FROM " + TABLE_GOALS
			+ " WHERE " + ID_MATCH_ARGS;
	
	
	public DatabaseHelper(Context context)
	{
		super(context, DATABASE_NAME, null, SCHEMA_VERSION);
	}

	// Execute each SQL statement in the DB_CREATE array
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		for (String sql : DB_CREATE)
		{
			Log.v("create DB", sql);
			db.execSQL(sql);
		}
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
	
	/**
	 * @param name		Name of the ride
	 * @param date		long representing the date of the ride
	 * @param gps		Whether or not to use GPS if recording distance (1 for true, 0 for false)
	 * @param distance	Distance traveled
	 * @param pedals	Number of times pedaled
	 * @return			The row ID of the newly inserted row, or -1 if an error occurred
	 */
	public long insertRide(String name, long date, int gps, double distance, int pedals)
	{
		ContentValues cv = new ContentValues();
		cv.put(RIDE_COL_NAME, name);
		cv.put(RIDE_COL_DATE, date);
		cv.put(RIDE_COL_GPS, gps);
		cv.put(RIDE_COL_DIST, distance);
		cv.put(RIDE_COL_PED, pedals);
		
		return getWritableDatabase().insert(TABLE_RIDES, RIDE_COL_NAME, cv);
	}
	
	/**
	 * @param id		ID of the ride in the table
	 * @param name		Name of the ride
	 * @param date		long representing the date of the ride
	 * @param gps		Whether or not to use GPS if recording distance (1 for true, 0 for false)
	 * @param distance	Distance traveled
	 * @param pedals	Number of times pedaled
	 * @return			The number of rows affected
	 */
	public int updateRide(String id, String name, long date, int gps, double distance, int pedals)
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
	
	/**
	 * @param id		ID of the ride in the table
	 * @param distance	New distance value
	 * @return			The number of rows affected
	 */
	public int updateRideDistance(String id, double distance)
	{
		ContentValues cv = new ContentValues();
		String[] args = {id};
		
		cv.put(RIDE_COL_DIST, distance);
		
		return getWritableDatabase().update(TABLE_RIDES, cv, ID_MATCH_ARGS, args);
	}
	
	/**
	 * @param id		ID of the ride in the table
	 * @param pedals	New pedals value
	 * @return			The number of rows affected
	 */
	public int updateRidePedals(String id, int pedals)
	{
		ContentValues cv = new ContentValues();
		String[] args = {id};
		
		cv.put(RIDE_COL_PED, pedals);
		
		return getWritableDatabase().update(TABLE_RIDES, cv, ID_MATCH_ARGS, args);
	}
	
	/**
	 * @param id	ID of the ride to delete
	 * @return		Whether or not the ride was delete successfully
	 */
	public boolean deleteRide(String id)
	{
		String[] args = {id};
		int result = getWritableDatabase().delete(TABLE_RIDES, ID_MATCH_ARGS, args);
		
		return (result != 0) ? true : false;
	}
	
	/**
	 * @param name		Name of the goal
	 * @param date		long representing the desired date of completion
	 * @param distance	Distance goal, or -1 if not set
	 * @param pedals	Pedals goal, or -1 if not set
	 * @return			The row ID of the newly inserted row, or -1 if an error occurred
	 */
	public long insertGoal(String name, long date, double distance, int pedals)
	{
		ContentValues cv = new ContentValues();
		cv.put(GOAL_COL_NAME, name);
		cv.put(GOAL_COL_DATE, date);
		cv.put(GOAL_COL_DIST, distance);
		cv.put(GOAL_COL_PED, pedals);
		
		return getWritableDatabase().insert(TABLE_GOALS, GOAL_COL_NAME, cv);
	}
	
	/**
	 * @param id		ID of the goal in the table
	 * @param name		Name of the goal
	 * @param date		long representing the desired date of completion
	 * @param distance	Distance goal, or -1 if not set
	 * @param pedals	Pedals goal, or -1 if not set
	 * @return			The number of rows affected
	 */
	public int updateGoal(String id, String name, long date, double distance, int pedals)
	{
		ContentValues cv = new ContentValues();
		String[] args = {id};
		
		cv.put(GOAL_COL_NAME, name);
		cv.put(GOAL_COL_DATE, date);
		cv.put(GOAL_COL_DIST, distance);
		cv.put(GOAL_COL_PED, pedals);
		
		return getWritableDatabase().update(TABLE_GOALS, cv, ID_MATCH_ARGS, args);
	}
	
	/**
	 * @param id	ID of the goal in the table
	 * @return		Whether or not the goal was deleted successfully
	 */
	public boolean deleteGoal(String id)
	{
		String[] args = {id};
		int result = getWritableDatabase().delete(TABLE_GOALS, ID_MATCH_ARGS, args);
		
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
	
	public double getRideDistance(Cursor c)
	{
		return c.getDouble(RIDE_DIST_INT);
	}
	
	public int getRidePedals(Cursor c)
	{
		return c.getInt(RIDE_PED_INT);
	}
	
	public Cursor getAllGoals(String orderBy)
	{
		return getReadableDatabase().rawQuery(DB_GET_ALL_GOALS_ORDER_BY + orderBy, null);
	}
	
	public Cursor getGoalById(String id)
	{
		String[] args = {id};
		return getReadableDatabase().rawQuery(DB_GET_GOAL_BY_ID, args);
	}
	
	public String getGoalName(Cursor c)
	{
		return c.getString(GOAL_NAME_INT);
	}
	
	public Date getGoalDate(Cursor c)
	{
		long d = c.getLong(GOAL_DATE_INT);
		if (d == -1L)
		{
			return new Date(0);
		}
		else
		{
			cal = Calendar.getInstance();
			cal.setTimeInMillis(d * 1000);
			return cal.getTime();
		}
	}
	
	public double getGoalDistance(Cursor c)
	{
		return c.getDouble(GOAL_DIST_INT);
	}
	
	public int getGoalPedals(Cursor c)
	{
		return c.getInt(GOAL_PED_INT);
	}
}
