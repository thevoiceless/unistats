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
	private static String errors;
	private static Calendar cal;
	private String rideID;
	private RideHelper dbHelper;
	private EditText name, month, day, year;
	private CheckBox recordDistance, useGPS, recordPedals;
	private Button setDate, createAchievement, save, saveAndStart;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ride_details);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
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
		        	i.addFlags(
		                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
		                    Intent.FLAG_ACTIVITY_NEW_TASK);
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
		cal = Calendar.getInstance();
		dbHelper = new RideHelper(this);
		
		name = (EditText) findViewById(R.id.enterName);
		month = (EditText) findViewById(R.id.enterMonth);
		day = (EditText) findViewById(R.id.enterDay);
		year = (EditText) findViewById(R.id.enterYear);
		setDate = (Button) findViewById(R.id.buttonSetDate);
		recordDistance = (CheckBox) findViewById(R.id.checkboxDistance);
		useGPS = (CheckBox) findViewById(R.id.checkboxGPS);
		recordPedals = (CheckBox) findViewById(R.id.checkboxPedalCount);
		createAchievement = (Button) findViewById(R.id.buttonCreateAchievement);
		save = (Button) findViewById(R.id.buttonSaveRide);
		saveAndStart = (Button) findViewById(R.id.buttonSaveAndStartRide);
	}
	
	private void initForm()
	{
		if (rideID != null)
		{
			Cursor c = dbHelper.getById(rideID);
			c.moveToFirst();
			
			name.setText(dbHelper.getName(c));
			
			Date d = dbHelper.getDate(c);
			month.setText(MONTH_FORMAT.format(d));
			day.setText(DAY_FORMAT.format(d));
			year.setText(YEAR_FORMAT.format(d));
			
			if (Double.valueOf(dbHelper.getDistance(c)) >= 0)
			{
				recordDistance.setChecked(true);
			}
			else
			{
				recordDistance.setChecked(false);
			}
			
			if (dbHelper.getUseGPS(c))
			{
				useGPS.setChecked(true);
				enableGPSCheckbox();
			}
			else
			{
				disableGPSCheckbox();
			}
			
			if (Double.valueOf(dbHelper.getPedals(c)) >= 0)
			{
				recordPedals.setChecked(true);
			}
			else
			{
				recordPedals.setChecked(false);
			}
		}
		
		updateDisplayedDate();
		name.requestFocus();
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
		Log.v("test", chosenDate.toString());
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
	
	/* LISTENERS */
	
	OnClickListener pressDateButton = new OnClickListener()
	{
		@Override
		public void onClick(View v)
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
				Log.wtf("ohshit", "Excption while setting date in pressDateButton");
			}
			
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
				if (rideID == null)
				{
					double d = recordDistance.isChecked() ? 0 : -1;
					int g = useGPS.isChecked() ? 1 : 0;
					double p = recordPedals.isChecked() ? 0 : -1;
					Toast.makeText(RideDetailActivity.this, "Saving ride...", Toast.LENGTH_SHORT).show();
					if (rideID == null)
					{
						dbHelper.insert(name.getText().toString(), 
								(long) (cal.getTimeInMillis() / 1000L), 
								g, d, p);
						finish();
					}
				}
				else
				{
					Toast.makeText(RideDetailActivity.this, "update", Toast.LENGTH_SHORT).show();
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