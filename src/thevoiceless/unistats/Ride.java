package thevoiceless.unistats;

public class Ride
{
	private String id, name;
	private double distance, pedals;
	
	public Ride(String id, String name, double distance, double pedals)
	{
		this.id = id;
		this.name = name;
		this.distance = distance;
		this.pedals = pedals;
	}
	
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
}
