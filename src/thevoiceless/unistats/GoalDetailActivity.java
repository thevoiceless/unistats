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

public class GoalDetailActivity extends SherlockActivity
{
	// Date formats used in the form
	private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("MMMMMMMMMM");
	private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("d");
	private static final SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy");
	
	private static Calendar cal = Calendar.getInstance();
	private static String errors;
	private String goalID;
	private DatabaseHelper dbHelper;
	private EditText name, distance, pedals, month, day, year;
	private CheckBox setDistance, setPedals, anyDate;
	private Button setDate, save;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_goal_details);
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
		goalID = getIntent().getStringExtra(GoalsActivity.GOAL_ID_KEY);
		dbHelper = new DatabaseHelper(this);
		
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
	
	// Populate the form with the values of the selected goal, if it exists
	private void initForm()
	{
		// Selected an already-existing goal
		if (goalID != null)
		{
			// Editing this goal, so change the title of the activity
			setTitle(R.string.title_activity_edit_goal);
			
			Cursor c = dbHelper.getGoalById(goalID);
			c.moveToFirst();
			
			name.setText(dbHelper.getGoalName(c));
			
			double dist = dbHelper.getGoalDistance(c);
			// Distance goal was set if the value is greater than -1
			if (dist >= 0)
			{
				setDistance.setChecked(true);
				enableDistance();
				distance.setText(String.valueOf(dist));
			}
			// Pedals goal was set if the value is greater than -1
			double ped = dbHelper.getGoalPedals(c);
			if (ped >= 0)
			{
				setPedals.setChecked(true);
				enablePedals();
				pedals.setText(String.valueOf(ped));
			}
			
			Date date = dbHelper.getGoalDate(c);
			// Populate the date area if a date was set
			if (date.compareTo(GoalsActivity.NO_DATE) != 0)
			{
				anyDate.setChecked(false);
				month.setText(MONTH_FORMAT.format(date));
				day.setText(DAY_FORMAT.format(date));
				year.setText(YEAR_FORMAT.format(date));
				enableDateSelection();
			}
			
			c.close();
		}
	}
	
	private void setListeners()
	{
		anyDate.setOnCheckedChangeListener(dateCheckboxChange);
		setDistance.setOnCheckedChangeListener(distanceCheckboxChange);
		setPedals.setOnCheckedChangeListener(pedalsCheckboxChange);
		setDate.setOnClickListener(pressDateButton);
		save.setOnClickListener(pressSaveButton);
	}
	
	// Allow the user to enter a distance
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
	
	// Allow the user to enter a number of pedals
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
		month.setText(MONTH_FORMAT.format(chosenDate));
		day.setText(DAY_FORMAT.format(chosenDate));
		year.setText(YEAR_FORMAT.format(chosenDate));		
	}
	
	// Update the calendar to the date selected
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
		if (!(setDistance.isChecked() || setPedals.isChecked()))
		{
			if (formErrors.length() > 0)
			{
				formErrors.append("\n");
			}
			formErrors.append(getString(R.string.no_goal_set));
		}
		// Verify values
		else
		{
			if (setDistance.isChecked())
			{
				// Validate distance
				try
				{
					Double.valueOf(distance.getText().toString());
				}
				catch (Exception e)
				{
					if (formErrors.length() > 0)
					{
						formErrors.append("\n");
					}
					formErrors.append(getString(R.string.invalid_distance));
				}
			}
			
			if (setPedals.isChecked())
			{
				// Validate number of pedals
				try
				{
					Double.valueOf(pedals.getText().toString());
				}
				catch (Exception e)
				{
					if (formErrors.length() > 0)
					{
						formErrors.append("\n");
					}
					formErrors.append(getString(R.string.invalid_pedals));
				}
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
				updateDisplayedDate();
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
	
	OnClickListener pressSaveButton = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			// Validate the form
			if (validateForm())
			{
				// Set default values for distance, pedals, and date unless specified by the user
				double d = setDistance.isChecked() ? Double.valueOf(distance.getText().toString()) : -1;
				int p = setPedals.isChecked() ? Integer.valueOf(pedals.getText().toString()) : -1;
				long date = anyDate.isChecked() ? -1L : (long) (cal.getTimeInMillis() / 1000L);
				
				// Creating a new goal
				if (goalID == null)
				{
					long result = dbHelper.insertGoal(name.getText().toString(), date, d, p);
					
					if (result != -1)
					{
						Toast.makeText(GoalDetailActivity.this, R.string.goal_created_successfully, Toast.LENGTH_SHORT).show();
						finish();
					}
					else
					{
						Log.e("create", getString(R.string.error_creating_ride));
						Toast.makeText(GoalDetailActivity.this, R.string.error_creating_ride, Toast.LENGTH_SHORT).show();
					}
				}
				// Update an already-existing goal
				else
				{					
					int result = dbHelper.updateGoal(goalID, name.getText().toString(), date, d, p);
					
					if (result == 1)
					{
						Toast.makeText(GoalDetailActivity.this, R.string.goal_updated_successfully, Toast.LENGTH_SHORT).show();
						finish();
					}
					else
					{
						Log.e("update", "Rows affected: " + result);
						Toast.makeText(GoalDetailActivity.this, R.string.error_updating_goal, Toast.LENGTH_SHORT).show();
					}
				}
			}
			// Display a toast with the form errors
			else
			{
				// TODO: Show error dialog
				Toast.makeText(GoalDetailActivity.this, errors, Toast.LENGTH_SHORT).show();
			}
		}
	};
}
