package thevoiceless.unistats;

public class Ride
{
	private String id, name;
	private double distance, pedals;
	private boolean trackDistance, trackPedals, useGPS;
	
	public Ride(String id, String name, double distance, double pedals, boolean trackDistance, boolean trackPedals, boolean useGPS)
	{
		this.id = id;
		this.name = name;
		this.distance = distance;
		this.pedals = pedals;
		this.trackDistance = trackDistance;
		this.trackPedals = trackPedals;
		this.useGPS = useGPS;
	}
	
	/* GETTERS */
	
	public String getID()
	{
		return id;
	}
	
	public String getName()
	{
		return name;
	}
	
	public double getDistance()
	{
		return distance;
	}
	
	public double getPedals()
	{
		return pedals;
	}
	
	public boolean isTrackingDistance()
	{
		return trackDistance;
	}
	
	public boolean isTrackingPedals()
	{
		return trackPedals;
	}
	
	public boolean isUsingGPS()
	{
		return useGPS;
	}
	
	/* SETTERS */
	
	public void updateDistance(double distanceTraveled)
	{
		distance += distanceTraveled;
	}
	
	public void updatePedals(double numPedals)
	{
		pedals += numPedals;
	}
}
