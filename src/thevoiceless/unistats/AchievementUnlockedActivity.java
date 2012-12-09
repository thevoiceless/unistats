package thevoiceless.unistats;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageButton;

public class AchievementUnlockedActivity extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_achievement_unlocked);
		
		Bitmap g = BitmapFactory.decodeResource(getResources(), R.drawable.google);
		ImageButton gb = (ImageButton) findViewById(R.id.buttonGooglePlus);
		gb.setMinimumWidth(g.getWidth());
		gb.setMinimumHeight(g.getHeight());
	}

}
