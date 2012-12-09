package thevoiceless.unistats;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
		setListeners();
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
	
	private void setListeners()
	{
		copyToClipboard.setOnClickListener(pressCopyToClipboard);
	}
	
	/* LISTENERS */
	
	OnClickListener pressCopyToClipboard = new OnClickListener()
	{
		@SuppressLint("NewApi")
		@Override
		public void onClick(View v)
		{
			// Use android.content.ClipboardManager on Honeycomb and higher
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
			{
			     android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
			     ClipData clip = ClipData.newPlainText("achievement", achievement.replaceFirst("You", "I"));
			     clipboard.setPrimaryClip(clip); 
			}
			// Use android.text.ClipboardManager on anything older than Honeycomb
			else
			{
			    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
			    clipboard.setText(achievement.replaceFirst("You", "I"));
			}
			Toast.makeText(getApplicationContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
		}
	};
}
