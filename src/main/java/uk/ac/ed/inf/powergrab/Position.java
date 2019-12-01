package uk.ac.ed.inf.powergrab;

import java.util.HashMap;

/**
 * This class represents a position and stores the latitude and longitude of the
 * position.
 * 
 * @author Jenny
 *
 */
public class Position {
	public double latitude;
	public double longitude;
	double degree = 0;

	/**
	 * Represents the latitude and longitude of the position
	 * 
	 * @param latitude  : Double
	 * @param longitude : Double
	 */
	protected Position(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	/**
	 * Gets the latitude of the position
	 * 
	 * @param position a Position
	 * @return this.Latitude a Double
	 */
	protected double getLatitude(Position position) {
		return this.latitude;
	}

	/**
	 * Gets the longitude of the position
	 * 
	 * @param position a Position
	 * @return this.longitude a Double
	 */
	protected double getLongitude(Position position) {
		return this.longitude;
	}

	/**
	 * Sets the degrees for the 16 directions
	 * 
	 */
	protected static void setDegree() {
		double E = 0;
		double ENE = 22.5;
		double NE = 45;
		double NNE = 67.5;
		double N = 90;
		double NNW = 112.5;
		double NW = 135;
		double WNW = 157.5;
		double W = 180;
		double WSW = 202.5;
		double SW = 225;
		double SSW = 247.5;
		double S = 270;
		double SSE = 292.5;
		double SE = 315;
		double ESE = 337.5;
	}

	/**
	 * This method gets the degrees from the hashmap created.
	 * 
	 * @param Direction a direction from the 16 directinos in Direction class
	 * @return degree a hashmap of sorted charging stations and their distances.
	 */
	protected double getDegree(Direction direction) {
		HashMap<Direction, Double> dir = new HashMap<Direction, Double>() {
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

		if (dir.keySet().contains(direction)) {
			degree = dir.get(direction);
		}
		return degree;
	}

	/**
	 * Returns the next position of the drone when it makes a move in the specified
	 * compass direction. It adds values to the latitude and longitude of the
	 * current position
	 * 
	 * @param Direction a direction from the 16 directinos in Direction class
	 * @return nextPos a position when moved in a direction
	 */
	protected Position nextPosition(Direction direction) {
		double width = 0.0003 * Math.sin(Math.toRadians(getDegree(direction)));
		double height = 0.0003 * Math.cos(Math.toRadians(getDegree(direction)));

		Position nextPos = new Position(this.latitude + width, this.longitude + height);

		return nextPos;

	}

	/**
	 * This method return true if drone is within play area, and false if not.
	 * 
	 */
	protected boolean inPlayArea() {
		return 55.942617 < this.latitude && this.latitude < 55.946233 && -3.192473 < this.longitude
				&& this.longitude < -3.184319;
	}
}
