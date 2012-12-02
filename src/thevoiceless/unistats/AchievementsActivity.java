package thevoiceless.unistats;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class AchievementsActivity extends SherlockActivity
{
	private DatabaseHelper dbHelper;
	
	static class AchievementHolder
	{		
		AchievementHolder(View row)
		{
		}
		
		void populateFrom(Cursor cursor, DatabaseHelper helper)
		{
		}
	}
	
	private class AchievementsAdapter extends CursorAdapter
	{
		// TODO: See https://developer.android.com/reference/android/widget/CursorAdapter.html
		AchievementsAdapter(Cursor c)
		{
			super(AchievementsActivity.this, c);
		}
		
		@Override
		public void bindView(View row, Context context, Cursor cursor)
		{
			AchievementHolder holder = (AchievementHolder) row.getTag();
			holder.populateFrom(cursor, dbHelper);
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent)
		{
			LayoutInflater inflater = AchievementsActivity.this.getLayoutInflater();
			View row = inflater.inflate(R.layout.ride_row, parent, false);
			AchievementHolder holder = new AchievementHolder(row);
			row.setTag(holder);
			return row;
		}
	}
	
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
				i = new Intent(this, AchievementDetailActivity.class);
				startActivity(i);
				return true;
			case R.id.menu_rides:
				i = new Intent(this, RidesActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
