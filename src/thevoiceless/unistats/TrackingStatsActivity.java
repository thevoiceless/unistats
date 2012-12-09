package thevoiceless.unistats;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class TrackingStatsActivity extends Activity implements StepListener
{
	public static final String TRACK_DISTANCE_KEY = "thevoiceless.unistats.TRACK_DISTANCE";
	public static final String USE_GPS_KEY = "thevoiceless.unistats.USE_GPS";
	public static final String TRACK_PEDALS_KEY = "thevoiceless.unistats.TRACK_PEDALS";
	// Time thresholds in milliseconds
	private static final int MIN_UPDATE_FREQ = 10 * 1000;
	// Distance thresholds in meters
	private static final int MIN_DISTANCE_NETWORK = 15;
	private static final int MIN_DISTANCE_GPS = 0;
	// Mean radius of Earth in meters
	private static final int RADIUS_OF_EARTH = 6371000;
	// Calendars for date comparison
	private static Calendar rideDate = Calendar.getInstance();
	private static Calendar goalDate = Calendar.getInstance();
	
	private LocationManager locManager;
	private Location lastLocation;
	private String thisRideID, thisRideName;
	private double thisRideDistance;
	private int thisRidePedals;
	private long thisRideDate;
	private boolean trackDistance, trackPedals, useGPS, initialLocation;
	private DatabaseHelper dbHelper;
	private Cursor thisRideCursor, goals;
	private Ride thisRide;
	private double acc = 0;
	private int numLocs = 0;
	// View elements
	private TextView title, distance, pedals, accuracy;
	private ImageButton pausePlayButton, stopButton;
	// Track number of pedals
	private PedalDetector pedalDetector;
	// Achievement notification ID and icon
	private int goalID = 0;
	private Bitmap achievementIcon;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tracking_stats);
		
		setDataMembers();
		initRide();
		setListeners();
		setLocationUpdates();
		
		if (trackDistance)
		{
			Toast.makeText(this, "Tracking distance", Toast.LENGTH_SHORT).show();
		}
		if (trackPedals)
		{
			Toast.makeText(this, "Tracking pedals", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onPause()
	{
		goals.close();
		super.onPause();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		goals = dbHelper.getAllGoals(DatabaseHelper.GOAL_COL_NAME);
		initFields();
	}
	
	@Override
	public void onDestroy()
	{
		locManager.removeUpdates(onLocationChange);
		if (trackPedals)
		{
			pedalDetector.stopCollectingData();
		}
		super.onDestroy();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		// Save statistics when the back button is pressed
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
	    {
	        if (thisRideID != null)
	        {
	        	updateThisRide();
	        	finish();
	        }
	        return true;
	    }

	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onStep()
	{
		thisRide.updatePedals(++thisRidePedals);
		updateDisplayedPedals();
	}
	
	private void setDataMembers()
	{
		title = (TextView) findViewById(R.id.titleTracking);
		distance = (TextView) findViewById(R.id.recordedDistanceArea);
		pedals = (TextView) findViewById(R.id.recordedPedalsArea);
		accuracy = (TextView) findViewById(R.id.distanceAccuracyArea);
		pausePlayButton = (ImageButton) findViewById(R.id.buttonPausePlayStats);
		stopButton = (ImageButton) findViewById(R.id.buttonStopStats);
		initialLocation = true;
		achievementIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_menu_star);
		
		thisRideID = getIntent().getStringExtra(RidesActivity.RIDE_ID_KEY);
		
		dbHelper = new DatabaseHelper(this);
		
		trackDistance = getIntent().getBooleanExtra(TRACK_DISTANCE_KEY, false);
		trackPedals = getIntent().getBooleanExtra(TRACK_PEDALS_KEY, false);
		useGPS = getIntent().getBooleanExtra(USE_GPS_KEY, false);
		pedalDetector = trackPedals ? new PedalDetector(this) : null;
		
		if (!(trackDistance || trackPedals))
		{
			Log.wtf("TrackingStatsActivity", "Not tracking any stat");
		}
	}
	
	// Enable either network or GPS location updates
	private void setLocationUpdates()
	{
		if (trackDistance)
		{
			locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
			if (useGPS)
			{
				locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_UPDATE_FREQ, MIN_DISTANCE_GPS, onLocationChange);
			}
			else
			{
				locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_UPDATE_FREQ, MIN_DISTANCE_NETWORK, onLocationChange);
			}
		}
	}
	
	private void setListeners()
	{
		if (trackPedals)
		{
			pedalDetector.addStepListener(this);
		}
		pausePlayButton.setOnClickListener(pressPauseButton);
		stopButton.setOnClickListener(pressStopButton);
	}
	
	// Load ride information from database into a Ride object for convenience
	private void initRide()
	{
		thisRideCursor = dbHelper.getRideById(thisRideID);
		thisRideCursor.moveToFirst();
				
		thisRideName = dbHelper.getRideName(thisRideCursor);
		thisRideDistance = dbHelper.getRideDistance(thisRideCursor);
		thisRidePedals = dbHelper.getRidePedals(thisRideCursor);
		thisRideDate = dbHelper.getRideDate(thisRideCursor).getTime();
				
		thisRide = new Ride(thisRideID, thisRideName, thisRideDate, thisRideDistance, thisRidePedals, trackDistance, trackPedals, useGPS);
		
		thisRideCursor.close();
	}
	
	// Initialize the form
	private void initFields()
	{
		// No accuracy value until locations have been recorded
		accuracy.setText("-");
		// Initialize distance and pedals if they have values, even if they are not being collected during this ride
		double d = thisRide.getDistance();
		int p = thisRide.getPedals();
		if (d >= 0)
		{
			updateDisplayedDistance();
		}
		else
		{
			distance.setText("-");
		}
		if (p >= 0)
		{
			updateDisplayedPedals();
			
		}
		else
		{
			pedals.setText("-");
		}
	}
	
	// Save the most recent statistics in the database
	private void updateThisRide()
	{
		if (trackDistance)
		{
			int result = dbHelper.updateRideDistance(thisRideID, thisRide.getDistance());
			if (result != 1)
			{
				Toast.makeText(TrackingStatsActivity.this, R.string.error_updating_ride, Toast.LENGTH_LONG).show();
			}
		}
		if (trackPedals)
		{
			int result = dbHelper.updateRidePedals(thisRideID, thisRide.getPedals());
			if (result != 1)
			{
				Toast.makeText(TrackingStatsActivity.this, R.string.error_updating_ride, Toast.LENGTH_LONG).show();
			}
		}
		checkGoals();
	}
	
	private void checkGoals()
	{
		NotificationManager notificationMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// Separate intents are created for each scenario because of the need to pass achievement info
		// The ID of the notification is passed as an extra with the intent and as the supposedly unused
		// second parameter (requestCode) of the PendingIntent constructor
		// This is done to avoid conflicts between the intents that prevented the correct information
		// from being passed with the second intent if two notifications were created
		// See http://stackoverflow.com/questions/3730258/mulitple-instances-of-pending-intent
		goals.moveToFirst();
		while (!goals.isAfterLast())
		{
			StringBuilder goalInfo = new StringBuilder();
			// Get date of goal, if any
			long gd = goals.getLong(DatabaseHelper.GOAL_DATE_INT);
			Log.e("goal date", GoalsActivity.dateFormat.format(new Date(gd * 1000L)));
			Log.e("ride date", GoalsActivity.dateFormat.format(new Date(thisRide.getDate())));
			goalDate.setTimeInMillis(gd * 1000);
			rideDate.setTimeInMillis(thisRide.getDate());
			boolean checkDate = (gd != -1L) ? true : false;
			// Only check distance and pedals if:
			//   a) Completion date does not matter, OR
			//   b) This ride occurred before the given completion date
			if (!checkDate || (checkDate && isRideBeforeGoalDate()))
			{
				// Check distance goals
				double dist = goals.getDouble(DatabaseHelper.GOAL_DIST_INT);
				if (dist != -1 && thisRide.getDistance() > dist)
				{
					// Create intent to launch the activity
					Intent distanceAchievement = new Intent(this, AchievementUnlockedActivity.class);
					// Create the achievement info string and add it as an intent extra
					goalInfo.append(getString(R.string.notification_goal_distance));
					goalInfo.append(" ");
					goalInfo.append(dist);
					goalInfo.append(" m");
					if (checkDate)
					{
						goalInfo.append(" ");
						goalInfo.append(getString(R.string.by));
						goalInfo.append(" ");
						goalInfo.append(GoalsActivity.dateFormat.format(goalDate.getTime()));
					}
					distanceAchievement.putExtra(AchievementUnlockedActivity.ACHIEVEMENT_INFO_KEY, goalInfo.toString());
					// Add the notification ID as an intent extra
					distanceAchievement.putExtra(AchievementUnlockedActivity.ACHIEVEMENT_ID_KEY, goalID);
					distanceAchievement.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					// Pass the notification ID as the second parameter to the constructor, set flag to CANCEL_CURRENT
					PendingIntent showDistanceAchievement = PendingIntent.getActivity(this, goalID, distanceAchievement, PendingIntent.FLAG_CANCEL_CURRENT);
					
					Notification distanceNotification = new NotificationCompat.Builder(this)
						.setContentTitle(getString(R.string.notification_achievement_get))
						.setContentText(goalInfo.toString())
						.setContentIntent(showDistanceAchievement)
						.setSmallIcon(R.drawable.icon_menu_star)
						.build();
					distanceNotification.flags |= Notification.FLAG_AUTO_CANCEL;
					
					notificationMgr.notify(goalID++, distanceNotification);
				}
				// Check pedal goals
				int ped = goals.getInt(DatabaseHelper.GOAL_PED_INT);
				if (ped != -1 && thisRide.getPedals() > ped)
				{
					// Create intent to launch the activity
					Intent pedalsAchievement = new Intent(this, AchievementUnlockedActivity.class);
					// Create the achievement info string and add it as an intent extra
					String pedalsInfo = getString(R.string.notification_goal_pedals) + " " + ped + " times";
					pedalsAchievement.putExtra(AchievementUnlockedActivity.ACHIEVEMENT_INFO_KEY, pedalsInfo);
					// Add the notification ID as an intent extra
					pedalsAchievement.putExtra(AchievementUnlockedActivity.ACHIEVEMENT_ID_KEY, goalID);
					pedalsAchievement.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					// Pass the notification ID as the second parameter to the constructor, set flag to CANCEL_CURRENT
					PendingIntent showPedalsAchievement = PendingIntent.getActivity(this, goalID, pedalsAchievement, PendingIntent.FLAG_CANCEL_CURRENT);
					
					Notification pedalsNotification = new NotificationCompat.Builder(this)
						.setContentTitle(getString(R.string.notification_achievement_get))
						.setContentText(pedalsInfo)
						.setContentIntent(showPedalsAchievement)
						.setSmallIcon(R.drawable.icon_menu_star)
						.build();
					pedalsNotification.flags |= Notification.FLAG_AUTO_CANCEL;
					
					notificationMgr.notify(goalID++, pedalsNotification);
				}
			}
			goals.moveToNext();
		}	
	}
	
	// Only compares year and day of year
	private boolean isRideBeforeGoalDate()
	{
		// Ride year is after goal year
		if (rideDate.get(Calendar.YEAR) > goalDate.get(Calendar.YEAR))
		{
			return false;
		}
		// Ride day of year is after goal day of year
		if (rideDate.get(Calendar.DAY_OF_YEAR) > goalDate.get(Calendar.DAY_OF_YEAR))
		{
			return false;
		}
		// Ride year is before goal year and day of year is before goal year
		return true;
	}
	
	private void updateDisplayedPedals()
	{
		pedals.setText(String.valueOf(thisRide.getPedals()));
	}
	
	private void updateDisplayedDistance()
	{
		distance.setText(String.format("%.2f m", thisRide.getDistance()));
	}
	
	// Use the haversine formula to calculate distance between two points
	// See http://www.movable-type.co.uk/scripts/latlong.html
	private double calcDistanceTraveled(Location loc1, Location loc2)
	{
		double lat2 = loc2.getLatitude();
		double lon2 = loc2.getLongitude();
		double lat1 = loc1.getLatitude();
		double lon1 = loc1.getLongitude();
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		
		double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0) + 
				Math.sin(dLon / 2.0) * Math.sin(dLon / 2.0) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
		return ((double) RADIUS_OF_EARTH) * c;
	}
	
	// Calculate the average accuracy every time a location is recorded
	private double calcAccuracy(float a)
	{
		acc += a;
		numLocs++;
		return (double) acc / numLocs;
	}
	
	/* LISTENERS */
	
	LocationListener onLocationChange = new LocationListener()
	{
		public void onLocationChanged(Location currentLoc)
		{
			if (trackDistance)
			{
				// Keep track of the previous location to calculate distance
				// getLastKnownLocation does not work here because currentLoc is returned
				if (initialLocation)
				{
					initialLocation = false;
					lastLocation = currentLoc;
				}
				// Update the ride's distance and the displayed distance
				// Displayed distance and accuracy are formatted to two decimal places
				else
				{
					thisRide.updateDistance(calcDistanceTraveled(lastLocation, currentLoc));
					updateDisplayedDistance();
					accuracy.setText(String.format("%.2f m", calcAccuracy(currentLoc.getAccuracy())));
					lastLocation = currentLoc;
				}
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{			
		}

		@Override
		public void onProviderEnabled(String provider)
		{			
		}

		@Override
		public void onProviderDisabled(String provider)
		{			
		}
	};
		
	OnClickListener pressPauseButton = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			// Stop location updates
			locManager.removeUpdates(onLocationChange);
			// Update the title
			title.setText(R.string.title_activity_tracking_stats_paused);
			// Change button image
			pausePlayButton.setImageResource(R.drawable.icon_play);
			// Set alternative listener
			pausePlayButton.setOnClickListener(pressPlayButton);
			// Save the ride
			updateThisRide();
		}
	};
	
	OnClickListener pressPlayButton = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			// Restart location updates
			setLocationUpdates();
			// Update the title
			title.setText(R.string.title_activity_tracking_stats);
			// Change button image
			pausePlayButton.setImageResource(R.drawable.icon_pause);
			// Set original listener
			pausePlayButton.setOnClickListener(pressPauseButton);
		}
	};
	
	OnClickListener pressStopButton = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			// Stop location updates, save the ride, and finish the activity
			locManager.removeUpdates(onLocationChange);
			updateThisRide();
			finish();
		}
	};
}
