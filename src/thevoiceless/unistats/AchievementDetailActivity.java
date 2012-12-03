package thevoiceless.unistats;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class AchievementDetailActivity extends SherlockActivity
{
	private EditText name, distance, pedals, month, day, year;
	private CheckBox setDistance, setPedals, anyDate;
	private Button setDate, save;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_achievement_details);
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
		        	Intent i = new Intent(this, AchievementsActivity.class);
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
		name = (EditText) findViewById(R.id.enterAchievementName);
		distance = (EditText) findViewById(R.id.enterAchievementDistance);
		pedals = (EditText) findViewById(R.id.enterAchievementPedals);
		month = (EditText) findViewById(R.id.enterAchievementMonth);
		day = (EditText) findViewById(R.id.enterAchievementDay);
		year = (EditText) findViewById(R.id.enterAchievementYear);
		setDistance = (CheckBox) findViewById(R.id.checkboxAchievementDistance);
		setPedals = (CheckBox) findViewById(R.id.checkboxAchievementPedals);
		anyDate = (CheckBox) findViewById(R.id.checkboxAnyDate);
		setDate = (Button) findViewById(R.id.buttonSetAchievementDate);
		save = (Button) findViewById(R.id.buttonSaveAchievement);
		
		month.setKeyListener(null);
		day.setKeyListener(null);
		year.setKeyListener(null);
	}
	
	private void setListeners()
	{
		anyDate.setOnCheckedChangeListener(dateCheckboxChange);
		setDistance.setOnCheckedChangeListener(distanceCheckboxChange);
		setPedals.setOnCheckedChangeListener(pedalsCheckboxChange);
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
		day.setEnabled(false);
		day.setFocusable(false);
		year.setEnabled(false);
		year.setFocusable(false);
		setDate.setEnabled(false);
		setDate.setFocusable(false);
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
}
