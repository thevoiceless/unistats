package thevoiceless.unistats;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class AchievementUnlockedActivity extends Activity
{
	public static final String ACHIEVEMENT_INFO_KEY = "thevoiceless.unistats.ACHIEVEMENT_INFO";
	public static final String ACHIEVEMENT_ID_KEY = "thevoiceless.unistats.ACHIEVEMENT_ID";
	private static StringBuilder stringBuilder = new StringBuilder();
	private TextView accomplishment;
	private ImageButton google, facebook, twitter, tumblr;
	private Button shareOther;
	private String achievement;
	private String whatIDid;
	
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
		shareOther = (Button) findViewById(R.id.buttonShareOther);
		achievement = getIntent().getStringExtra(ACHIEVEMENT_INFO_KEY);
		
		stringBuilder.append(getString(R.string.on_a_unicycle));
		stringBuilder.append(" ");
		stringBuilder.append(getString(R.string.by));
		whatIDid = achievement.replace(getString(R.string.by), stringBuilder.toString()).replaceFirst("You", "I");
				
		accomplishment.setText(achievement);
	}
	
	private void setListeners()
	{
		google.setOnClickListener(shareOnGooglePlus);
		facebook.setOnClickListener(shareOnFacebook);
		twitter.setOnClickListener(shareOnTwitter);
		tumblr.setOnClickListener(shareOnTumblr);
		shareOther.setOnClickListener(pressShareOther);
	}
	
	@SuppressLint("NewApi")
	private void copyToClipboard()
	{
		// Use android.content.ClipboardManager on Honeycomb and higher
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
		{
		     android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
		     ClipData clip = ClipData.newPlainText("achievement", whatIDid);
		     clipboard.setPrimaryClip(clip); 
		}
		// Use android.text.ClipboardManager on anything older than Honeycomb
		else
		{
		    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
		    clipboard.setText(whatIDid);
		}
		Toast.makeText(AchievementUnlockedActivity.this, R.string.copied_to_clipboard, Toast.LENGTH_LONG).show();
	}
	
	/* LISTENERS */
	
	OnClickListener pressShareOther = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(Intent.EXTRA_TEXT, whatIDid);
			startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
		}
	};
	
	// Google+
	OnClickListener shareOnGooglePlus = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			try
			{
				Intent shareIntent = ShareCompat.IntentBuilder.from(AchievementUnlockedActivity.this)
				   .setText(whatIDid)
				   .setType("text/plain")
				   .getIntent()
				   .setPackage("com.google.android.apps.plus");
				
				startActivity(shareIntent);
			}
			catch (Exception e)
			{
				Intent shareIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.uri_google_plus)));
				copyToClipboard();
				startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
			}
		}
	};
	
	// Facebook
	OnClickListener shareOnFacebook = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			try
			{
				Intent shareIntent = ShareCompat.IntentBuilder.from(AchievementUnlockedActivity.this)
				   .setText(whatIDid)
				   .setType("text/plain")
				   .getIntent()
				   .setPackage("com.facebook.katana");
				
				copyToClipboard();
				
				startActivity(shareIntent);
			}
			catch (Exception e)
			{
				Intent shareIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.uri_facebook)));
				startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
			}
		}
	};
	
	// Twitter
	// TODO: Test this
	OnClickListener shareOnTwitter = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			try
			{
				Intent shareIntent = ShareCompat.IntentBuilder.from(AchievementUnlockedActivity.this)
				   .setText(whatIDid)
				   .setType("text/plain")
				   .getIntent()
				   .setPackage("com.twitter.android");
				
				copyToClipboard();
				
				startActivity(shareIntent);
			}
			catch (Exception e)
			{
				Intent shareIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.uri_twitter)));
				//String tweet = getString(R.string.uri_twitter_direct_url) + achievement.replaceAll(" ", "+");
				//Intent shareIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(tweet));
				startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
			}
		}
	};
	
	// Tumblr
	// TODO: Test this
	OnClickListener shareOnTumblr = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			try
			{
				Intent shareIntent = ShareCompat.IntentBuilder.from(AchievementUnlockedActivity.this)
				   .setText(whatIDid)
				   .setType("text/plain")
				   .getIntent()
				   .setPackage("com.tumblr");
				
				copyToClipboard();
				
				startActivity(shareIntent);
			}
			catch (Exception e)
			{
				Intent shareIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.uri_tumblr)));
				startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
			}
		}
	};
}
