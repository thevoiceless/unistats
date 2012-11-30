package thevoiceless.unistats;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockActivity;

public class NewRideActivity extends SherlockActivity
{
	private EditText name, month, day, year;
	private CheckBox recordDistance, useGPS, recordPedals;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_ride);
		
		setDataMembers();
		setListeners();
	}
	
	private void setDataMembers()
	{
		name = (EditText) findViewById(R.id.enterName);
		month = (EditText) findViewById(R.id.enterMonth);
		day = (EditText) findViewById(R.id.enterDay);
		year = (EditText) findViewById(R.id.enterYear);
		recordDistance = (CheckBox) findViewById(R.id.checkboxDistance);
		useGPS = (CheckBox) findViewById(R.id.checkboxGPS);
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
				}
				else
				{
					useGPS.setChecked(false);
					useGPS.setFocusable(true);
					useGPS.setEnabled(true);
				}
			}
		});
	}

}
