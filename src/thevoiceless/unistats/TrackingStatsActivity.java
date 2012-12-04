package thevoiceless.unistats;

import java.text.DecimalFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class TrackingStatsActivity extends Activity
{
	public static final String TRACK_DISTANCE_KEY = "thevoiceless.unistats.TRACK_DISTANCE";
	public static final String USE_GPS_KEY = "thevoiceless.unistats.USE_GPS";
	public static final String TRACK_PEDALS_KEY = "thevoiceless.unistats.TRACK_PEDALS";
	private static final DecimalFormat DISTANCE_FORMAT = new DecimalFormat();
	// Time thresholds in ms
	private static final int MIN_UPDATE_FREQ = 10 * 1000;
	// Distance thresholds in meters
	private static final int MIN_DISTANCE_NETWORK = 15;
	private static final int MIN_DISTANCE_GPS = 5;
	// Mean radius of Earth in meters
	private static final int RADIUS_OF_EARTH = 6371000;
	
	private LocationManager locManager;
	private Location lastLocation;
	private String thisRideID, thisRideName;
	private double thisRideDistance, thisRidePedals;
	private boolean trackDistance, trackPedals, useGPS, initialLocation;
	private DatabaseHelper dbHelper;
	private Cursor thisRideCursor;
	private Ride thisRide;
	private double acc = 0;
	private int numLocs = 0;
	// View elements
	private TextView title, distance, pedals, accuracy;
	private ImageButton pausePlayButton, stopButton;

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
		super.onDestroy();
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
		
		thisRideID = getIntent().getStringExtra(RidesActivity.RIDE_ID_KEY);
		// Temporary validation
//		if (rideID != null)
//		{
//			Toast.makeText(this, "rideID: " + rideID, Toast.LENGTH_LONG).show();
//		}
//		else
//		{
//			Toast.makeText(this, "rideID is null!", Toast.LENGTH_LONG).show();
//		}
		
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
		pausePlayButton.setOnClickListener(pressPauseButton);
		stopButton.setOnClickListener(pressStopButton);
	}
	
	private void initRide()
	{
		thisRideCursor = dbHelper.getRideById(thisRideID);
		thisRideCursor.moveToFirst();
		
		thisRideName = dbHelper.getRideName(thisRideCursor);
		thisRideDistance = dbHelper.getRideDistance(thisRideCursor);
		thisRidePedals = dbHelper.getRidePedals(thisRideCursor);
		
		// String id, String name, double distance, double pedals, boolean trackDistance, boolean trackPedals, boolean useGPS
		thisRide = new Ride(thisRideID, thisRideName, thisRideDistance, thisRidePedals, trackDistance, trackPedals, useGPS);
	}
	
	private void initFields()
	{
		accuracy.setText("-");
		double d = thisRide.getDistance();
		double p = thisRide.getPedals();
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
	
	// Uses the haversine formula to calculate distance between two points
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
				if (initialLocation)
				{
					initialLocation = false;
					lastLocation = currentLoc;
				}
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
			locManager.removeUpdates(onLocationChange);
			title.setText(R.string.title_activity_tracking_stats_paused);
			pausePlayButton.setImageResource(R.drawable.icon_play);
			pausePlayButton.setOnClickListener(pressPlayButton);
			updateThisRide();
		}
	};
	
	OnClickListener pressPlayButton = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			setLocationUpdates();
			title.setText(R.string.title_activity_tracking_stats);
			pausePlayButton.setImageResource(R.drawable.icon_pause);
			pausePlayButton.setOnClickListener(pressPauseButton);
		}
	};
	
	OnClickListener pressStopButton = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			locManager.removeUpdates(onLocationChange);
			updateThisRide();
			finish();
		}
	};

}
