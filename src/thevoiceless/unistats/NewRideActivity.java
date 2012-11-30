package thevoiceless.unistats;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class NewRideActivity extends SherlockActivity
{
	private EditText name, month, day, year;
	private CheckBox recordDistance, useGPS, recordPedals;
	
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
		name = (EditText) findViewById(R.id.enterName);
		month = (EditText) findViewById(R.id.enterMonth);
		day = (EditText) findViewById(R.id.enterDay);
		year = (EditText) findViewById(R.id.enterYear);
		recordDistance = (CheckBox) findViewById(R.id.checkboxDistance);
		useGPS = (CheckBox) findViewById(R.id.checkboxGPS);
		
		name.requestFocus();
	}
	
	private void setListeners()
	{
		recordDistance.setOnCheckedChangeListener(new OnCheckedChangeListener() 
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
		});
	}

}
