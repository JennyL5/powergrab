package uk.ac.ed.inf.powergrab;
import java.lang.Math;
import java.util.HashMap;

public class Position {   
	
	public double latitude;
	public double longitude;
	double degree = 0;
	
	public Position(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public double get_Degree(Direction direction) {
		// Gets the degree(in double) of direction
		
		// Creates a hashmap containing the 16 directions and their degrees
		HashMap<Direction, Double> dir = new HashMap<Direction, Double>()
		{
		    {
		        put(Direction.E, 0.0);
		        put(Direction.ENE, 22.5);
		        put(Direction.NE, 45.0);
		        put(Direction.NNE, 67.5);
		        put(Direction.N, 90.0);
		        put(Direction.NNW, 112.5);
		        put(Direction.NW, 135.0);
		        put(Direction.WNW, 157.5);
		        put(Direction.W, 180.0);
		        put(Direction.WSW, 202.5);
		        put(Direction.SW, 225.0);
		        put(Direction.SSW, 247.5);
		        put(Direction.S, 270.0);
		        put(Direction.SSE, 292.5);
		        put(Direction.SE, 315.0);
		        put(Direction.ESE, 337.5);
		    }
		};
		
		// If given direction is from Direction class, return that direction's degree
		if (dir.keySet().contains(direction)) {
			degree = dir.get(direction);
		}
		return degree;
		
	}
	
	
	public Position nextPosition(Direction direction) {
		// Returns the next position of the drone when it makes a move in the specified compass direction

		// Values to be added to current latitude/longitude
		double width = 0.0003 * Math.sin(Math.toRadians(get_Degree(direction)));
		double height = 0.0003 * Math.cos(Math.toRadians(get_Degree(direction)));
		
		// Create new position
		Position nextPos = new Position(this.latitude + width, this.longitude + height);
		
		// Returns the next position of the drone
		return nextPos;
		
	}
		
	public boolean inPlayArea() {
		// Checks whether or not this Position lies in the PowerGrab play area 
		return 55.942617 < this.latitude && this.latitude < 55.946233 && -3.192473 < this.longitude && this.longitude < -3.184319;
	}
}