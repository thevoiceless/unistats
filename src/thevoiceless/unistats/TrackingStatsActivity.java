package thevoiceless.unistats;

import java.text.DecimalFormat;

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
import android.support.v4.app.TaskStackBuilder;
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
	private static final int MIN_DISTANCE_GPS = 5;
	// Mean radius of Earth in meters
	private static final int RADIUS_OF_EARTH = 6371000;
	
	private LocationManager locManager;
	private Location lastLocation;
	private String thisRideID, thisRideName;
	private double thisRideDistance;
	private int thisRidePedals;
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
				
		thisRide = new Ride(thisRideID, thisRideName, thisRideDistance, thisRidePedals, trackDistance, trackPedals, useGPS);
		
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
		
		Intent showAchievement = new Intent(this, AchievementUnlockedActivity.class);
		// The stack builder object will contain an artificial back stack for the started Activity
		// This ensures that navigating backward from the Activity leads out of your application to the home screen
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(AchievementUnlockedActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(showAchievement);
		PendingIntent achievement = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		
		goals.moveToFirst();
		while (!goals.isAfterLast())
		{
			// Check distance goals
			double d = goals.getDouble(DatabaseHelper.GOAL_DIST_INT);
			if (d != -1 && thisRide.getDistance() > d)
			{				
				Notification notification = new NotificationCompat.Builder(this)
					.setContentTitle(getString(R.string.notification_achievement_get))
					.setContentText(getString(R.string.notification_goal_distance) + " " + d + " m")
					.setContentIntent(achievement)
					.setSmallIcon(R.drawable.icon_menu_star)
					.build();
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
				
				notificationMgr.notify(goalID++, notification);
			}
			// Check pedal goals
			int p = goals.getInt(DatabaseHelper.GOAL_PED_INT);
			if (p != -1 && thisRide.getPedals() > p)
			{
				Notification notification = new NotificationCompat.Builder(this)
					.setContentTitle(getString(R.string.notification_achievement_get))
					.setContentText(getString(R.string.notification_goal_pedals) + " " + p + " times")
					.setContentIntent(achievement)
					.setSmallIcon(R.drawable.icon_menu_star)
					.build();
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
				
				notificationMgr.notify(goalID++, notification);
			}
			goals.moveToNext();
		}	
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
