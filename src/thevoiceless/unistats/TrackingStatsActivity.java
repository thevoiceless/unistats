package thevoiceless.unistats;

import android.app.Activity;
import android.os.Bundle;

public class TrackingStatsActivity extends Activity
{
	public static final String STATS_WITH_RIDE_ID = "thevoiceless.unistats.STATS_WITH_RIDE";
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tracking_stats);
	}

}
