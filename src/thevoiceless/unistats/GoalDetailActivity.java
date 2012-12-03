package thevoiceless.unistats;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.content.Intent;
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

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class GoalDetailActivity extends SherlockActivity
{
	private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("MMMMMMMMMM");
	private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("d");
	private static final SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy");
	private static Calendar cal = Calendar.getInstance();
	private static String errors;
	private EditText name, distance, pedals, month, day, year;
	private CheckBox setDistance, setPedals, anyDate;
	private Button setDate, save;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_goal_details);
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
		        	Intent i = new Intent(this, GoalsActivity.class);
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
		name = (EditText) findViewById(R.id.enterGoalName);
		distance = (EditText) findViewById(R.id.enterGoalDistance);
		pedals = (EditText) findViewById(R.id.enterGoalPedals);
		month = (EditText) findViewById(R.id.enterGoalMonth);
		day = (EditText) findViewById(R.id.enterGoalDay);
		year = (EditText) findViewById(R.id.enterGoalYear);
		setDistance = (CheckBox) findViewById(R.id.checkboxGoalDistance);
		setPedals = (CheckBox) findViewById(R.id.checkboxGoalPedals);
		anyDate = (CheckBox) findViewById(R.id.checkboxAnyDate);
		setDate = (Button) findViewById(R.id.buttonSetGoalDate);
		save = (Button) findViewById(R.id.buttonSaveGoal);
		
		month.setKeyListener(null);
		day.setKeyListener(null);
		year.setKeyListener(null);
	}
	
	private void setListeners()
	{
		anyDate.setOnCheckedChangeListener(dateCheckboxChange);
		setDistance.setOnCheckedChangeListener(distanceCheckboxChange);
		setPedals.setOnCheckedChangeListener(pedalsCheckboxChange);
		setDate.setOnClickListener(pressDateButton);
	}
	
	private void enableDistance()
	{
		distance.setEnabled(true);
		distance.setFocusable(true);
		distance.setFocusableInTouchMode(true);
	}
	
	private void disableDistance()
	{
		distance.setEnabled(false);
		distance.setFocusable(false);
		distance.setText("");
	}
	
	private void enablePedals()
	{
		pedals.setEnabled(true);
		pedals.setFocusable(true);
		pedals.setFocusableInTouchMode(true);
	}
	
	private void disablePedals()
	{
		pedals.setEnabled(false);
		pedals.setFocusable(false);
		pedals.setText("");
	}
	
	private void enableDateSelection()
	{
		month.setEnabled(true);
		month.setFocusable(true);
		day.setEnabled(true);
		day.setFocusable(true);
		year.setEnabled(true);
		year.setFocusable(true);
		setDate.setEnabled(true);
		setDate.setFocusable(true);
	}
	
	private void disableDateSelection()
	{
		month.setEnabled(false);
		month.setFocusable(false);
		month.setText("");
		day.setEnabled(false);
		day.setFocusable(false);
		day.setText("");
		year.setEnabled(false);
		year.setFocusable(false);
		year.setText("");
		setDate.setEnabled(false);
		setDate.setFocusable(false);
	}
	
	private void updateDisplayedDate()
	{
		Date chosenDate = cal.getTime();
//		Log.v("updateDisplayedDate", chosenDate.toString());
		month.setText(MONTH_FORMAT.format(chosenDate));
		day.setText(DAY_FORMAT.format(chosenDate));
		year.setText(YEAR_FORMAT.format(chosenDate));		
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
	
	OnCheckedChangeListener dateCheckboxChange = new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			if (isChecked)
			{
				disableDateSelection();
			}
			else
			{
				enableDateSelection();
			}
		}
	};
	
	OnCheckedChangeListener distanceCheckboxChange = new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			if (isChecked)
			{
				enableDistance();
			}
			else
			{
				disableDistance();
			}
		}
	};
	
	OnCheckedChangeListener pedalsCheckboxChange = new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			if (isChecked)
			{
				enablePedals();
			}
			else
			{
				disablePedals();
			}
		}
	};
	
	OnClickListener pressDateButton = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			updateCalendar();
			
			new DatePickerDialog(GoalDetailActivity.this, 
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
		}
	};
}
