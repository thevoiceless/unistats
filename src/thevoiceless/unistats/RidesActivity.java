package thevoiceless.unistats;

import java.text.SimpleDateFormat;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class RidesActivity extends SherlockActivity
{
	public static final String RIDE_ID_KEY = "thevoiceless.unistats.RIDE_ID";
	private Cursor rides;
	private RidesAdapter ridesAdapter;
	private DatabaseHelper dbHelper;
	private ListView ridesList;
	private TextView noRides;
	
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
		
		void populateFrom(Cursor cursor, DatabaseHelper helper)
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy");
			
			rideName.setText(helper.getRideName(cursor));
			rideDate.setText(dateFormat.format(helper.getRideDate(cursor)));
			
			if (Double.valueOf(helper.getRideDistance(cursor)) >= 0)
			{
				rideDistance.setText(helper.getRideDistance(cursor));
			}
			else
			{
				rideDistance.setText("");
			}
			
			if (Double.valueOf(helper.getRidePedals(cursor)) >= 0)
			{
				ridePedals.setText(helper.getRidePedals(cursor));
			}
			else
			{
				ridePedals.setText("");
			}
			
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
		initList();
	}
	
	@Override
	public void onResume()
	{
		initCursor();
		setAdapters();
		super.onResume();
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
		Intent i;
		switch (item.getItemId())
		{
			case R.id.menu_new_ride:
				i = new Intent(this, RideDetailActivity.class);
				startActivity(i);
				return true;
			case R.id.menu_achievements:
				i = new Intent(this, AchievementsActivity.class);
				startActivity(i);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void setDataMembers()
	{
		ridesList = (ListView) findViewById(R.id.ridesList);
		noRides = (TextView) findViewById(android.R.id.empty);
		dbHelper = new DatabaseHelper(this);
		
		ridesList.setEmptyView(noRides);
	}
	
	private void initCursor()
	{
		if (rides != null)
		{
			rides.close();
		}
		
		rides = dbHelper.getAllRides(DatabaseHelper.RIDE_COL_NAME);
	}
	
	private void setAdapters()
	{
		ridesAdapter = new RidesAdapter(rides);
		ridesList.setAdapter(ridesAdapter);
	}
	
	private void initList()
	{
		ridesList.setOnItemClickListener(selectRideFromList);
	}
	
	/* LISTENERS */
	
	OnItemClickListener selectRideFromList = new OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			Intent i = new Intent(RidesActivity.this, RideDetailActivity.class);
			i.putExtra(RIDE_ID_KEY, String.valueOf(id));
			startActivity(i);
		}
	};
	
}
