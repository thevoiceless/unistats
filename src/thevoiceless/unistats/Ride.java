package thevoiceless.unistats;

// Convenience class used while recording statistics
public class Ride
{
	// Data members for each aspect of a ride
	private String id, name;
	private double distance;
	int pedals;
	private boolean trackDistance, trackPedals, useGPS;
	
	// TODO: Determine if all fields are necessary, possibly use Builder pattern instead
	public Ride(String id, String name, double distance, int pedals, boolean trackDistance, boolean trackPedals, boolean useGPS)
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
	
	public int getPedals()
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
	
	public void updatePedals(int numPedals)
	{
		pedals = numPedals;
	}
}
