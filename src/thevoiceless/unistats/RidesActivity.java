package thevoiceless.unistats;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class RidesActivity extends SherlockActivity
{
	private Cursor rides;
	private RidesAdapter ridesAdapter;
	private RideHelper dbHelper;
	private ListView ridesList;
	
	static class RideHolder
	{
		private TextView rideName, rideDate, rideDistance, ridePedals;
		
		RideHolder(View row)
		{
			rideName = (TextView) row.findViewById(R.id.rideNameArea);
			rideDate = (TextView) row.findViewById(R.id.rideDateArea);
			rideDistance = (TextView) row.findViewById(R.id.rideDistanceArea);
			ridePedals = (TextView) row.findViewById(R.id.ridePedalsArea);
		}
		
		void populateFrom(Cursor cursor, RideHelper helper)
		{
			rideName.setText(helper.getName(cursor));
			rideDate.setText(helper.getDate(cursor));
			rideDistance.setText(helper.getDistance(cursor));
			ridePedals.setText(helper.getPedals(cursor));
		}
	}
	
	private class RidesAdapter extends CursorAdapter
	{
		// TODO: See https://developer.android.com/reference/android/widget/CursorAdapter.html
		RidesAdapter(Cursor c)
		{
			super(RidesActivity.this, c);
		}
		
		@Override
		public void bindView(View row, Context context, Cursor cursor)
		{
			RideHolder holder = (RideHolder) row.getTag();
			holder.populateFrom(cursor, dbHelper);
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent)
		{
			LayoutInflater inflater = RidesActivity.this.getLayoutInflater();
			View row = inflater.inflate(R.layout.ride_row, parent, false);
			RideHolder holder = new RideHolder(row);
			row.setTag(holder);
			return row;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rides);
		setTitle(R.string.title_activity_rides);
		
		setDataMembers();
		initCursor();
		setAdapters();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getSupportMenuInflater().inflate(R.menu.menu_rides, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.menu_new_ride)
		{
			Intent i = new Intent(this, NewRideActivity.class);
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void setDataMembers()
	{
		ridesList = (ListView) findViewById(R.id.ridesList);
		dbHelper = new RideHelper(this);
	}
	
	private void initCursor()
	{
		if (rides != null)
		{
			rides.close();
		}
		
		rides = dbHelper.getAll("name");
	}
	
	private void setAdapters()
	{
		ridesAdapter = new RidesAdapter(rides);
		ridesList.setAdapter(ridesAdapter);
	}
	
}
