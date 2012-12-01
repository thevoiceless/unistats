package thevoiceless.unistats;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class NewRideActivity extends SherlockActivity
{
	private static String errors;
	private static Calendar chosenDate;
	private String rideID;
	private RideHelper dbHelper;
	private EditText name, month, day, year;
	private CheckBox recordDistance, useGPS, recordPedals;
	private Button createAchievement, saveRide;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_ride);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		setDataMembers();
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
		dbHelper = new RideHelper(this);
		name = (EditText) findViewById(R.id.enterName);
		month = (EditText) findViewById(R.id.enterMonth);
		day = (EditText) findViewById(R.id.enterDay);
		year = (EditText) findViewById(R.id.enterYear);
		recordDistance = (CheckBox) findViewById(R.id.checkboxDistance);
		useGPS = (CheckBox) findViewById(R.id.checkboxGPS);
		recordPedals = (CheckBox) findViewById(R.id.checkboxPedalCount);
		createAchievement = (Button) findViewById(R.id.buttonCreateAchievement);
		saveRide = (Button) findViewById(R.id.ButtonSaveRide);
		
		name.requestFocus();
	}
	
	private void setListeners()
	{
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
		if (!validateDate())
		{
			if (formErrors.length() > 0)
			{
				formErrors.append("\n");
			}
			formErrors.append(getString(R.string.error_bad_date));
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
	
	private boolean validateDate()
	{
		if (month.getText().toString().trim().equals("")
			|| day.getText().toString().trim().equals("")
			|| year.getText().toString().trim().equals(""))
		{
			return false;
		}
		try
		{
			int monthNum = Integer.valueOf(month.getText().toString());
			int dayNum = Integer.valueOf(day.getText().toString());
			int yearNum = Integer.valueOf(year.getText().toString());
			chosenDate = Calendar.getInstance();
			chosenDate.set(Calendar.YEAR, yearNum);
			chosenDate.set(Calendar.MONTH, monthNum);
			chosenDate.set(Calendar.DAY_OF_MONTH, dayNum);
		}
		catch (Exception e)
		{
			return false;
		}
		
		return true;
	}
	
	/* LISTENERS */
	
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
				Toast.makeText(NewRideActivity.this, "pass", Toast.LENGTH_SHORT).show();
				if (rideID == null)
				{
					dbHelper.insert(name.getText().toString(), 
							(long) (chosenDate.getTimeInMillis() / 1000L), 
							0, 0);
					finish();
				}
			}
			else
			{
				// TODO: Show error dialog
				Toast.makeText(NewRideActivity.this, errors, Toast.LENGTH_SHORT).show();
			}
		}
	};
}
