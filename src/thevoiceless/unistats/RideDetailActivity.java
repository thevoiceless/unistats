package thevoiceless.unistats;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class RideDetailActivity extends SherlockActivity
{
	// Date formats used in the form
	private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("MMMMMMMMMM");
	private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("d");
	private static final SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy");
	
	private static Calendar cal = Calendar.getInstance();
	private static String errors;
	private String rideID;
	private double distance;
	private int pedals;
	private DatabaseHelper dbHelper;
	private EditText name, month, day, year;
	private CheckBox recordDistance, useGPS, recordPedals;
	private Button setDate, createGoal, save, saveAndStart;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ride_details);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public void onPause()
	{
		dbHelper.close();
		super.onPause();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		setDataMembers();
		initForm();
		setListeners();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		 switch (item.getItemId())
		 {
			// Navigate up, clear activity from the stack to prevent issues with the back button
	        case android.R.id.home:
	        	Intent i = new Intent(this, RidesActivity.class);
	        	i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
	        			| Intent.FLAG_ACTIVITY_NEW_TASK);
	        	startActivity(i);
	        	finish();
	        	return true;
	        default:
	        	return super.onOptionsItemSelected(item);
		 }
	}
	
	private void setDataMembers()
	{
		rideID = getIntent().getStringExtra(RidesActivity.RIDE_ID_KEY);
		dbHelper = new DatabaseHelper(this);
		
		name = (EditText) findViewById(R.id.enterRideName);
		month = (EditText) findViewById(R.id.enterRideMonth);
		day = (EditText) findViewById(R.id.enterRideDay);
		year = (EditText) findViewById(R.id.enterRideYear);
		setDate = (Button) findViewById(R.id.buttonSetRideDate);
		recordDistance = (CheckBox) findViewById(R.id.checkboxRideDistance);
		useGPS = (CheckBox) findViewById(R.id.checkboxRideGPS);
		recordPedals = (CheckBox) findViewById(R.id.checkboxRidePedalCount);
		createGoal = (Button) findViewById(R.id.buttonCreateGoal);
		save = (Button) findViewById(R.id.buttonSaveRide);
		saveAndStart = (Button) findViewById(R.id.buttonSaveAndStartRide);
		
		month.setKeyListener(null);
		day.setKeyListener(null);
		year.setKeyListener(null);
	}
	
	// Populate the form with the values of the selected ride, or the default values if creating a new ride
	private void initForm()
	{
		// Selected an already-existing ride
		if (rideID != null)
		{
			// Editing this ride, so change the title of the activity
			setTitle(R.string.title_activity_edit_ride);
			
			Cursor c = dbHelper.getRideById(rideID);
			c.moveToFirst();
			
			name.setText(dbHelper.getRideName(c));
			
			Date date = dbHelper.getRideDate(c);
			month.setText(MONTH_FORMAT.format(date));
			day.setText(DAY_FORMAT.format(date));
			year.setText(YEAR_FORMAT.format(date));
			updateCalendar();
			checkIfDateIsToday();
			
			// Distance was recorded if value is greater than -1
			distance = dbHelper.getRideDistance(c);
			if (distance >= 0)
			{
				recordDistance.setChecked(true);
				enableGPSCheckbox();
			}
			else
			{
				recordDistance.setChecked(false);
				disableGPSCheckbox();
			}
			
			if (dbHelper.getRideUseGPS(c))
			{
				useGPS.setChecked(true);
			}
			else
			{
				useGPS.setChecked(false);
			}
			
			// Pedals were recorded if value is greater than -1
			pedals = dbHelper.getRidePedals(c);
			if (pedals >= 0)
			{
				recordPedals.setChecked(true);
			}
			else
			{
				recordPedals.setChecked(false);
			}
			
			c.close();
		}
		// Creating a new ride, so set the date to today
		else
		{
			updateDisplayedDate();
		}
	}
	
	private void setListeners()
	{
		setDate.setOnClickListener(pressDateButton);
		recordDistance.setOnCheckedChangeListener(distanceCheckboxChange);
		createGoal.setOnClickListener(pressCreateGoal);
		save.setOnClickListener(pressSaveButton);
		saveAndStart.setOnClickListener(pressSaveAndStartButton);
	}
	
	private void enableGPSCheckbox()
	{
		useGPS.setFocusable(true);
		useGPS.setEnabled(true);
		useGPS.setClickable(true);
	}
	
	private void disableGPSCheckbox()
	{
		useGPS.setChecked(false);
		useGPS.setFocusable(false);
		useGPS.setEnabled(false);
		useGPS.setClickable(false);
	}
	
	// Validate the form, return true if there were no issues
	private boolean validateForm()
	{
		StringBuilder formErrors = new StringBuilder();
		// Verify name
		if (name.getText().toString().trim().equals(""))
		{
			formErrors.append(getString(R.string.error_no_name));
		}
		// Check chosen statistic(s)
		if (!(recordDistance.isChecked() || recordPedals.isChecked()))
		{
			if (formErrors.length() > 0)
			{
				formErrors.append("\n");
			}
			formErrors.append(getString(R.string.no_stat_selected));
		}
		// If GPS is selected, check if it is enabled
		if (useGPS.isChecked())
		{
			LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			{
				if (formErrors.length() > 0)
				{
					formErrors.append("\n");
				}
				formErrors.append(getString(R.string.gps_not_enabled));
			}
		}
		
		// Compile the errors, if any, into a single string
		errors = formErrors.toString();
		if (errors.equals(""))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	// Format the date values and display them in the form
	private void updateDisplayedDate()
	{
		Date chosenDate = cal.getTime();
		month.setText(MONTH_FORMAT.format(chosenDate));
		day.setText(DAY_FORMAT.format(chosenDate));
		year.setText(YEAR_FORMAT.format(chosenDate));		
	}
	
	// If the date is not today, do not allow the user to start the ride
	private void checkIfDateIsToday()
	{
		Calendar today = Calendar.getInstance();
		// Compare month, day, and year
		if (cal.get(Calendar.MONTH) != today.get(Calendar.MONTH)
			|| cal.get(Calendar.DAY_OF_MONTH) != today.get(Calendar.DAY_OF_MONTH)
			|| cal.get(Calendar.YEAR) != today.get(Calendar.YEAR))
		{
			saveAndStart.setEnabled(false);
		}
		else
		{
			saveAndStart.setEnabled(true);
		}
	}
	
	// Set calendar values to the text values in the form
	private void updateCalendar()
	{
		StringBuilder dateString = new StringBuilder();
		dateString.append(month.getText().toString());
		dateString.append(" ");
		dateString.append(day.getText().toString());
		dateString.append(" ");
		dateString.append(year.getText().toString());
		
		try
		{
			cal.setTime(new SimpleDateFormat("MMMMMMMMMM d yyyy").parse(dateString.toString()));
		}
		catch (Exception e)
		{
			Log.wtf("updateCalendar", getString(R.string.exception_setting_date));
		}
	}
	
	/* LISTENERS */
	
	OnClickListener pressDateButton = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			// Show DatePickerDialog initialized to the calendar's current date
			new DatePickerDialog(RideDetailActivity.this, 
					selectDate, 
					cal.get(Calendar.YEAR), 
					cal.get(Calendar.MONTH), 
					cal.get(Calendar.DAY_OF_MONTH))
			.show();			
		}
	};
	
	DatePickerDialog.OnDateSetListener selectDate = new DatePickerDialog.OnDateSetListener()
	{
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,	int dayOfMonth)
		{
			// Update the calendar to match the selected values
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, monthOfYear);
			cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			// Display the new date and check if today
			updateDisplayedDate();
			checkIfDateIsToday();
		}
	};
	
	OnCheckedChangeListener distanceCheckboxChange = new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			if (isChecked)
			{
				enableGPSCheckbox();
				// Select GPS by default
				useGPS.setChecked(true);
			}
			else
			{
				disableGPSCheckbox();
			}
		}
	};
	
	OnClickListener pressCreateGoal = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			// Show the goal creation form
			Intent i = new Intent(RideDetailActivity.this, GoalDetailActivity.class);
			startActivity(i);
		}
	};
	
	OnClickListener pressSaveButton = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			// Validate the form
			if (validateForm())
			{				
				// Create new ride
				if (rideID == null)
				{
					// Set default values for distance, GPS use, and pedals unless specified by the user
					double d = recordDistance.isChecked() ? 0 : -1;
					int g = useGPS.isChecked() ? 1 : 0;
					int p = recordPedals.isChecked() ? 0 : -1;
					
					long result = dbHelper.insertRide(name.getText().toString(), 
							(long) (cal.getTimeInMillis() / 1000L), 
							g, d, p);
					
					if (result != -1)
					{
						Toast.makeText(RideDetailActivity.this, R.string.ride_created_successfully, Toast.LENGTH_SHORT).show();
						finish();
					}
					else
					{
						Log.e("create", getString(R.string.error_creating_ride));
						Toast.makeText(RideDetailActivity.this, R.string.error_creating_ride, Toast.LENGTH_SHORT).show();
					}
					
				}
				// Update an already-existing ride
				else
				{
					// Set default values for distance, GPS use, and pedals unless specified by the user
					double d = recordDistance.isChecked() ? distance : -1;
					int g = useGPS.isChecked() ? 1 : 0;
					int p = recordPedals.isChecked() ? pedals : -1;
					
					int result = dbHelper.updateRide(rideID, name.getText().toString(), 
							(long) (cal.getTimeInMillis() / 1000L), g, d, p);
					
					if (result == 1)
					{
						Toast.makeText(RideDetailActivity.this, R.string.ride_updated_successfully, Toast.LENGTH_SHORT).show();
						finish();
					}
					else
					{
						Log.e("update", "Rows affected: " + result);
						Toast.makeText(RideDetailActivity.this, R.string.error_updating_ride, Toast.LENGTH_SHORT).show();
					}
				}
			}
			// Display a toast with the form errors
			else
			{
				// TODO: Show error dialog
				Toast.makeText(RideDetailActivity.this, errors, Toast.LENGTH_SHORT).show();
			}
		}
	};
	
	OnClickListener pressSaveAndStartButton = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			// Validate the form
			if (validateForm())
			{				
				// Save and start new ride
				if (rideID == null)
				{
					// Set default values for distance, GPS use, and pedals unless specified by the user
					double d = recordDistance.isChecked() ? 0 : -1;
					int g = useGPS.isChecked() ? 1 : 0;
					int p = recordPedals.isChecked() ? 0 : -1;
					
					long result = dbHelper.insertRide(name.getText().toString(), 
							(long) (cal.getTimeInMillis() / 1000L), g, d, p);
					
					if (result != -1)
					{
						Toast.makeText(RideDetailActivity.this, R.string.ride_created_successfully, Toast.LENGTH_SHORT).show();
						// Create intent with ride ID, distance boolean, GPS boolean, and pedals boolean
						Intent i = new Intent(RideDetailActivity.this, TrackingStatsActivity.class);
						i.putExtra(RidesActivity.RIDE_ID_KEY, String.valueOf(result));
						i.putExtra(TrackingStatsActivity.TRACK_DISTANCE_KEY, recordDistance.isChecked());
						i.putExtra(TrackingStatsActivity.USE_GPS_KEY, useGPS.isChecked());
						i.putExtra(TrackingStatsActivity.TRACK_PEDALS_KEY, recordPedals.isChecked());
						startActivity(i);
						finish();
					}
					else
					{
						Log.e("create", getString(R.string.error_creating_ride));
						Toast.makeText(RideDetailActivity.this, R.string.error_creating_ride, Toast.LENGTH_SHORT).show();
					}
				}
				// Save and continue existing ride
				else
				{
					// Set default values for distance, GPS use, and pedals unless specified by the user
					double d = recordDistance.isChecked() ? distance : -1;
					int g = useGPS.isChecked() ? 1 : 0;
					int p = recordPedals.isChecked() ? pedals : -1;
					
					int result = dbHelper.updateRide(rideID, name.getText().toString(), 
							(long) (cal.getTimeInMillis() / 1000L), g, d, p);
					
					if (result == 1)
					{
						Toast.makeText(RideDetailActivity.this, R.string.ride_updated_successfully, Toast.LENGTH_SHORT).show();
						// Create intent with ride ID, distance boolean, GPS boolean, and pedals boolean
						Intent i = new Intent(RideDetailActivity.this, TrackingStatsActivity.class);
						i.putExtra(RidesActivity.RIDE_ID_KEY, rideID);
						i.putExtra(TrackingStatsActivity.TRACK_DISTANCE_KEY, recordDistance.isChecked());
						i.putExtra(TrackingStatsActivity.USE_GPS_KEY, useGPS.isChecked());
						i.putExtra(TrackingStatsActivity.TRACK_PEDALS_KEY, recordPedals.isChecked());
						startActivity(i);
						finish();
					}
					else
					{
						Log.e("update", "Rows affected: " + result);
						Toast.makeText(RideDetailActivity.this, R.string.error_updating_ride, Toast.LENGTH_SHORT).show();
					}
				}
			}
			// Display a toast with the form errors
			else
			{
				// TODO: Show error dialog
				Toast.makeText(RideDetailActivity.this, errors, Toast.LENGTH_SHORT).show();
			}
		}
	};
}
