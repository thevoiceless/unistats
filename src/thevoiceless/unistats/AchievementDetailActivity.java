package thevoiceless.unistats;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;

public class AchievementDetailActivity extends SherlockActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_achievement_details);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
}
