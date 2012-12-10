package thevoiceless.unistats;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
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

public class GoalsActivity extends SherlockActivity
{
	public static final String GOAL_ID_KEY = "thevoiceless.unistats.GOAL_ID";
	// Date format used in the goal details
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yy");
	// If no date is given, use Date(0) to allow comparisons
	public static final Date NO_DATE = new Date(0);
	private static Context context;
	private Cursor goals;
	private GoalsAdapter goalsAdapter;
	private DatabaseHelper dbHelper;
	private ListView goalsList;
	private TextView noGoals;

	static class GoalHolder
	{
		private TextView goalName, goalDetails;
		private StringBuilder detailsBuilder = new StringBuilder();
		
		GoalHolder(View row)
		{
			goalName = (TextView) row.findViewById(R.id.goalNameArea);
			goalDetails = (TextView) row.findViewById(R.id.goalDetailsArea);
		}
		
		// Populate the row layout
		void populateFrom(Cursor cursor, DatabaseHelper helper)
		{
			detailsBuilder.setLength(0);
			
			goalName.setText(helper.getGoalName(cursor));
			
			double goalDistance = helper.getGoalDistance(cursor);
			double goalPedals = helper.getGoalPedals(cursor);
			// Display the goal values if they are greater than -1
			if (goalDistance >= 0)
			{
				detailsBuilder.append(goalDistance + " m");
			}
			if (goalPedals >= 0)
			{
				if (detailsBuilder.length() > 0)
				{
					detailsBuilder.append(", ");
				}
				detailsBuilder.append(goalPedals + " pedals");
			}
			detailsBuilder.append(" ");
			
			Date d = helper.getGoalDate(cursor);
			// Check if a date was specified
			if (d.compareTo(NO_DATE) != 0)
			{
				detailsBuilder.append(context.getString(R.string.by) + " " + dateFormat.format(d));
			}
			
			goalDetails.setText(detailsBuilder.toString());
		}
	}
	
	private class GoalsAdapter extends CursorAdapter
	{
		// TODO: See https://developer.android.com/reference/android/widget/CursorAdapter.html
		GoalsAdapter(Cursor c)
		{
			super(GoalsActivity.this, c);
		}
		
		@Override
		public void bindView(View row, Context context, Cursor cursor)
		{
			GoalHolder holder = (GoalHolder) row.getTag();
			holder.populateFrom(cursor, dbHelper);
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent)
		{
			LayoutInflater inflater = GoalsActivity.this.getLayoutInflater();
			View row = inflater.inflate(R.layout.goal_row, parent, false);
			GoalHolder holder = new GoalHolder(row);
			row.setTag(holder);
			return row;
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_goals);
		
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
		getSupportMenuInflater().inflate(R.menu.menu_goals, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Intent i;
		switch (item.getItemId())
		{
			// Create new goal
			case R.id.menu_new_goal:
				i = new Intent(this, GoalDetailActivity.class);
				startActivity(i);
				return true;
			// Switch to rides list, clear this activity from the stack to prevent back-and-forth
			case R.id.menu_rides:
				i = new Intent(this, RidesActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void setDataMembers()
	{
		dbHelper = new DatabaseHelper(this);
		goalsList = (ListView) findViewById(R.id.goalsList);
		noGoals = (TextView) findViewById(android.R.id.empty);
		context = this.getApplicationContext();
		
		goalsList.setEmptyView(noGoals);
	}
	
	private void initCursor()
	{
		if (goals != null)
		{
			goals.close();
		}
		
		goals = dbHelper.getAllGoals(DatabaseHelper.GOAL_COL_NAME);
	}
	
	private void setAdapters()
	{
		goalsAdapter = new GoalsAdapter(goals);
		goalsList.setAdapter(goalsAdapter);
	}
	
	private void initList()
	{
		goalsList.setOnItemClickListener(selectGoalFromList);
		goalsList.setFastScrollEnabled(true);
	}
	
	/* LISTENERS */
	
	OnItemClickListener selectGoalFromList = new OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			Intent i = new Intent(GoalsActivity.this, GoalDetailActivity.class);
			i.putExtra(GOAL_ID_KEY, String.valueOf(id));
			startActivity(i);
		}
	};
}
