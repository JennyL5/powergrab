package uk.ac.ed.inf.powergrab;

import java.util.HashMap;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;


abstract public class Drone {
	private Position currentPos; // lon, lat
	private Integer moves = 0; // count the number of moves made
	protected Double coins = 0.0; // count coins
	protected Double power = 0.0; // keep track of power
	static final Double threshold = 0.00025; // range within threshold degrees of chrging st
	static final Double distance = 0.003; // every move is 0.003 degrees 
	static final Double minusPower = 1.25; // every move reduce power by 1.25
		
	// getters
	public Position getCurrentPos() { return currentPos;}
	public Integer getMoves() { return this.moves;}
	public double getCoins() { return this.coins;}
	public double getPower() { return this.power;}


	// setters
	protected void setCurrentPos(Position currentPos) { this.currentPos = currentPos;}
	protected void setMoves(Integer moves) {this.moves = moves;}
	protected void setCoins(Double coins) { this.coins = coins;}
	protected void setPower(Double power) { this.power = power;}
	
	
	// check if drone is within play area
	protected boolean inPlayArea() { return getCurrentPos().inPlayArea();}
	
	/*
	// find stations within r distance
	protected HashMap<String, Direction> getNeighbourhood() {
		//////////////////////// TODO: updateStats and getNeighbourhood are similar
		// get nearby charging stations
		Point point;
		Double dist,x,y;
		
		HashMap<String, Direction> result = new HashMap<String,Direction>();
		
		for(Feature f: App.fc.features()) {
			point = (Point) f.geometry(); 
			
			x = point.latitude()-getCurrentPos().latitude;
			y = point.longitude()-getCurrentPos().longitude;
			dist = Math.sqrt(Math.pow(x,2) + Math.pow(y,2));
			
			if(dist <= Position.r) {
				// check which direction is best way to get to it
				// TODO: do this 
				
			}
		}
		return result; // id of station and direction it is in
	}*/
	
	// move drone 
	protected void moveDrone(Position newPos) { 
		moves +=1;
		setCurrentPos(newPos);
		setPower(this.power- 1.25);
	}
	
	
	// add/subtract power and coins for charging station
	protected void updateStats() { 
		// if this.currentPos is close to a station(0.00025)
		Point point;
		Double dist;
		
		for(Feature f: App.fc.features()) {
			point = (Point) f.geometry(); 
			// Point object's renamed longitude as latitude and vice versa
			dist = Math.sqrt(Math.pow((point.latitude()-getCurrentPos().latitude),2) 
					+ Math.pow((point.longitude()-getCurrentPos().longitude),2));
			if(dist <= threshold) {
				// System.out.println("before: " + f.properties().toString());
				setCoins(coins + f.getProperty("coins").getAsDouble());
				setPower(power + f.getProperty("power").getAsDouble());
				// change station properties
				f.properties().addProperty("coins", (Number) 0 );
				f.properties().addProperty("power", (Number) 0 );
				// System.out.println("after: " + f.properties().toString());
			}
		}
	}

	// finished moves
	public boolean isFinished() { return (moves == 250) || (power < 1.25);}

	
}

