package thevoiceless.unistats;

import java.util.ArrayList;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

// Slightly modified version of the pedometer implementation from
// https://github.com/YunsuChoi/Pedometer.android
// and
// https://github.com/bagilevi/android-pedometer
public class PedalDetector implements SensorEventListener
{
	private SensorManager sensorManager;
	private Sensor sensor;
	private double xAxis, yAxis, zAxis;
	
    private float limit = 10;
    private float lastValues[] = new float[6];
    private float scale[] = new float[2];
    private int h = 480;
    private float yOffset = h * 0.5f;

    private float lastDirections[] = new float[6];
    private float lastExtremes[][] = { new float[6], new float[6] };
    private float lastDiff[] = new float[6];
    private int lastMatch = -1;
    
    private ArrayList<StepListener> stepListeners = new ArrayList<StepListener>();
	
	public PedalDetector(Context context)
	{
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
	    
        scale[0] = - (h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
        scale[1] = - (h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
	}
	
	@Override
	public void onSensorChanged(SensorEvent event)
	{
		Sensor sensor = event.sensor;
		synchronized (this)
		{
			if (sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			{
				float vSum = 0;
                for (int i = 0; i < 3; i++)
                {
                    final float v = yOffset + event.values[i] * scale[1];
                    vSum += v;
                }
                int k = 0;
                float v = vSum / 3;
                
                float direction = (v > lastValues[k] ? 1 : (v < lastValues[k] ? -1 : 0));
                if (direction == -lastDirections[k])
                {
                    // Direction changed
                    int extType = (direction > 0 ? 0 : 1); // Minimum or maximum?
                    lastExtremes[extType][k] = lastValues[k];
                    float diff = Math.abs(lastExtremes[extType][k] - lastExtremes[1 - extType][k]);

                    if (diff > limit)
                    {
                        boolean isAlmostAsLargeAsPrevious = diff > (lastDiff[k] * (2 / 3));
                        boolean isPreviousLargeEnough = lastDiff[k] > (diff / 3);
                        boolean isNotContra = (lastMatch != 1 - extType);
                        
                        if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra)
                        {
                            Log.i("PedalDetector", "step");
                            for (StepListener stepListener : stepListeners)
                            {
                                stepListener.onStep();
                            }
                            lastMatch = extType;
                        }
                        else
                        {
                            lastMatch = -1;
                        }
                    }
                    lastDiff[k] = diff;
                }
                lastDirections[k] = direction;
                lastValues[k] = v;
                
				xAxis = event.values[0];
	            yAxis = event.values[1];
	            zAxis = event.values[2];
//	            Log.v("values", xAxis + " " + yAxis + " " + zAxis);
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
	}
	
	public void addStepListener(StepListener sl)
	{
        stepListeners.add(sl);
    }
	
	public void stopCollectingData()
	{
		sensorManager.unregisterListener(this);
	}
}
