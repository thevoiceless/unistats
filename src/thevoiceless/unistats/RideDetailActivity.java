package thevoiceless.unistats;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
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
	private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("MMMMMMMMMM");
	private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("d");
	private static final SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy");
	private static Calendar cal = Calendar.getInstance();
	private static String errors;
	private String rideID;
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
	
	private void initForm()
	{
		if (rideID != null)
		{
			setTitle(R.string.title_activity_edit_ride);
			
			Cursor c = dbHelper.getRideById(rideID);
			c.moveToFirst();
			
			name.setText(dbHelper.getRideName(c));
			
			Date d = dbHelper.getRideDate(c);
//			Log.v("initForm", d.toString());
			month.setText(MONTH_FORMAT.format(d));
			day.setText(DAY_FORMAT.format(d));
			year.setText(YEAR_FORMAT.format(d));
			
			if (Double.valueOf(dbHelper.getRideDistance(c)) >= 0)
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
			
			if (Double.valueOf(dbHelper.getRidePedals(c)) >= 0)
			{
				recordPedals.setChecked(true);
			}
			else
			{
				recordPedals.setChecked(false);
			}
			
			c.close();
		}
		else
		{
			updateDisplayedDate();
		}
	}
	
	private void setListeners()
	{
		setDate.setOnClickListener(pressDateButton);
		recordDistance.setOnCheckedChangeListener(distanceCheckboxChange);
		save.setOnClickListener(pressSaveButton);
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
	
	private boolean validateForm()
	{
		StringBuilder formErrors = new StringBuilder();
		if (name.getText().toString().trim().equals(""))
		{
			formErrors.append(getString(R.string.error_no_name));
		}
		if (!(recordDistance.isChecked() || recordPedals.isChecked()))
		{
			if (formErrors.length() > 0)
			{
				formErrors.append("\n");
			}
			formErrors.append(getString(R.string.no_stat_selected));
		}
		
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
	
	private void updateDisplayedDate()
	{
		Date chosenDate = cal.getTime();
//		Log.v("updateDisplayedDate", chosenDate.toString());
		month.setText(MONTH_FORMAT.format(chosenDate));
		day.setText(DAY_FORMAT.format(chosenDate));
		year.setText(YEAR_FORMAT.format(chosenDate));		
	}
	
	private void checkIfDateIsToday()
	{
		Calendar today = Calendar.getInstance();
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
			Log.wtf("ohshit", getString(R.string.exception_setting_date));
		}
	}
	
	/* LISTENERS */
	
	OnClickListener pressDateButton = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			updateCalendar();
			
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
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, monthOfYear);
			cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
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
			}
			else
			{
				disableGPSCheckbox();
			}
		}
	};
	
	OnClickListener pressSaveButton = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if(validateForm())
			{
				double d = recordDistance.isChecked() ? 0 : -1;
				int g = useGPS.isChecked() ? 1 : 0;
				double p = recordPedals.isChecked() ? 0 : -1;
				
				if (rideID == null)
				{					
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
				else
				{					
					int result = dbHelper.updateRide(rideID,
							name.getText().toString(), 
							(long) (cal.getTimeInMillis() / 1000L), 
							g, d, p);
					
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
			else
			{
				// TODO: Show error dialog
				Toast.makeText(RideDetailActivity.this, errors, Toast.LENGTH_SHORT).show();
			}
		}
	};
}
