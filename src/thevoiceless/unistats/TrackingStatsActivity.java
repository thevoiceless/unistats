package thevoiceless.unistats;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class TrackingStatsActivity extends Activity
{
	public static final String STATS_WITH_RIDE_ID = "thevoiceless.unistats.STATS_WITH_RIDE";
	public static final String MODIFY_OR_NEW = "thevoiceless.unistats.MODIFY_OR_NEW";
	public static final int NEW_RIDE = 0;
	public static final int UPDATE_RIDE = 1;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tracking_stats);
		
		String rideID = getIntent().getStringExtra(STATS_WITH_RIDE_ID);
		int updatingRide = getIntent().getIntExtra(MODIFY_OR_NEW, -1);
		if (rideID != null)
		{
			switch (updatingRide)
			{
				case UPDATE_RIDE:
					Toast.makeText(this, "Updating ride with ID " + rideID, Toast.LENGTH_LONG).show();
					break;
				case NEW_RIDE:
					Toast.makeText(this, "New ride with ID " + rideID, Toast.LENGTH_LONG).show();
					break;
				default:
					Log.wtf("TrackingStatsActivity", "No ride ID");
			}
		}
		else
		{
			Toast.makeText(this, "rideID is null!", Toast.LENGTH_LONG).show();
		}
	}

}
