package thevoiceless.unistats;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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
	private static String errors;
	private static Calendar cal;
	private String rideID;
	private RideHelper dbHelper;
	private EditText name, month, day, year;
	private CheckBox recordDistance, useGPS, recordPedals;
	private Button setDate, createAchievement, saveRide;
	
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
		saveRide = (Button) findViewById(R.id.buttonSaveRide);
	}
	
	private void initForm()
	{		
		name.requestFocus();
		updateDate();
	}
	
	private void setListeners()
	{
		setDate.setOnClickListener(pressDateButton);
		recordDistance.setOnCheckedChangeListener(distanceCheckboxChange);
		saveRide.setOnClickListener(pressSaveButton);
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
	
	private void updateDate()
	{
		Date today = cal.getTime();
		month.setText(new SimpleDateFormat("MMMMMMMMMM").format(today));
		day.setText(new SimpleDateFormat("d").format(today));
		year.setText(new SimpleDateFormat("yyyy").format(today));
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
			updateDate();
		}
	};
	
	OnCheckedChangeListener distanceCheckboxChange = new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			if (isChecked)
			{
				useGPS.setFocusable(true);
				useGPS.setEnabled(true);
				useGPS.setClickable(true);
			}
			else
			{
				useGPS.setChecked(false);
				useGPS.setFocusable(false);
				useGPS.setEnabled(false);
				useGPS.setClickable(false);
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
				Toast.makeText(RideDetailActivity.this, "pass", Toast.LENGTH_SHORT).show();
				if (rideID == null)
				{
					dbHelper.insert(name.getText().toString(), 
							(long) (cal.getTimeInMillis() / 1000L), 
							0, 0);
					finish();
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
