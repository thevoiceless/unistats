package thevoiceless.unistats;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class AchievementUnlockedActivity extends Activity
{
	public static final String ACHIEVEMENT_INFO_KEY = "thevoiceless.unistats.ACHIEVEMENT_INFO";
	private TextView accomplishment;
	private ImageButton google, facebook, twitter, tumblr;
	private Button copyToClipboard;
	private String achievement;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_achievement_unlocked);
		
		setDataMembers();
	}
	
	private void setDataMembers()
	{
		accomplishment = (TextView) findViewById(R.id.whatYouDid);
		google = (ImageButton) findViewById(R.id.buttonGooglePlus);
		facebook = (ImageButton) findViewById(R.id.buttonFacebook);
		twitter = (ImageButton) findViewById(R.id.buttonTwitter);
		tumblr = (ImageButton) findViewById(R.id.buttonTumblr);
		copyToClipboard = (Button) findViewById(R.id.buttonCopyToClipboard);
		achievement = getIntent().getStringExtra(ACHIEVEMENT_INFO_KEY);
		
		accomplishment.setText(achievement);
	}
	
	/* LISTENERS */
	
	OnClickListener pressCopyToClipboard = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			// TODO Auto-generated method stub
		}
	};
}
