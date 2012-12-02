package thevoiceless.unistats;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class AchievementsActivity extends SherlockActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_achievements);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getSupportMenuInflater().inflate(R.menu.menu_achievements, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Intent i;
		switch (item.getItemId())
		{
			case R.id.menu_new_achievement:
				Toast.makeText(this, "New", Toast.LENGTH_SHORT).show();
				return true;
			case R.id.menu_rides:
				i = new Intent(this, RidesActivity.class);
				startActivity(i);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
