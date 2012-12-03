package thevoiceless.unistats;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class TrackingStatsActivity extends Activity
{
	public static final String STATS_WITH_RIDE_ID = "thevoiceless.unistats.STATS_WITH_RIDE";
	private String rideID;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tracking_stats);
		
		setDataMembers();
	}
	
	private void setDataMembers()
	{
		rideID = getIntent().getStringExtra(STATS_WITH_RIDE_ID);
		if (rideID != null)
		{
			Toast.makeText(this, "rideID: " + rideID, Toast.LENGTH_LONG).show();
		}
		else
		{
			Toast.makeText(this, "rideID is null!", Toast.LENGTH_LONG).show();
		}
	}

}
