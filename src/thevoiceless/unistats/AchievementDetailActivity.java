package thevoiceless.unistats;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class AchievementDetailActivity extends SherlockActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_achievement_details);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
}
