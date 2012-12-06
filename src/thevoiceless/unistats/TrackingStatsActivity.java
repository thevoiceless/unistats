package thevoiceless.unistats;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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
	private static final DecimalFormat DISTANCE_FORMAT = new DecimalFormat();
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
	private Cursor thisRideCursor;
	private Ride thisRide;
	private double acc = 0;
	private int numLocs = 0;
	// View elements
	private TextView title, distance, pedals, accuracy;
	private ImageButton pausePlayButton, stopButton;
	
	private PedalDetector pedalDetector;
	private int numPedals;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tracking_stats);
		
		setDataMembers();
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
	public void onResume()
	{
		super.onResume();
		initFields();
	}
	
	@Override
	public void onDestroy()
	{
		locManager.removeUpdates(onLocationChange);
		thisRideCursor.close();
		pedalDetector.stopCollectingData();
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
		numPedals++;
		thisRide.updatePedals(numPedals);
		updateDisplayedPedals();
	}
	
	private void setDataMembers()
	{
		pedalDetector = new PedalDetector(this);
		title = (TextView) findViewById(R.id.titleTracking);
		distance = (TextView) findViewById(R.id.recordedDistanceArea);
		pedals = (TextView) findViewById(R.id.recordedPedalsArea);
		accuracy = (TextView) findViewById(R.id.distanceAccuracyArea);
		pausePlayButton = (ImageButton) findViewById(R.id.buttonPausePlayStats);
		stopButton = (ImageButton) findViewById(R.id.buttonStopStats);
		initialLocation = true;
		
		thisRideID = getIntent().getStringExtra(RidesActivity.RIDE_ID_KEY);
		
		dbHelper = new DatabaseHelper(this);
		
		trackDistance = getIntent().getBooleanExtra(TRACK_DISTANCE_KEY, false);
		trackPedals = getIntent().getBooleanExtra(TRACK_PEDALS_KEY, false);
		useGPS = getIntent().getBooleanExtra(USE_GPS_KEY, false);
		if (!(trackDistance || trackPedals))
		{
			Log.wtf("TrackingStatsActivity", "Not tracking any stat");
		}
				
		initRide();
	}
	
	// Enable either network or GPS location updates
	private void setLocationUpdates()
	{
		if (trackDistance)
		{
			locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
			if (useGPS)
			{
				locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_UPDATE_FREQ, 0, onLocationChange);
			}
			else
			{
				locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_UPDATE_FREQ, 0, onLocationChange);
			}
		}
	}
	
	private void setListeners()
	{
		pedalDetector.addStepListener(this);
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
			distance.setText(String.valueOf(d));
		}
		else
		{
			distance.setText("-");
		}
		if (p >= 0)
		{
			pedals.setText(String.valueOf(p));
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
	}
	
	private void updateDisplayedPedals()
	{
		pedals.setText(String.valueOf(numPedals));
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
					distance.setText(String.format("%.2f m", thisRide.getDistance()));
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
