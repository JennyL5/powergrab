package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;


abstract public class Drone { 
	private Position currentPos; // lon, lat
	protected Integer moves = 0; // count the number of moves made
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

	protected Point getPointOfDirection(Direction d, Double lat, Double lon) {
		// lat lon of the current position
		Position initPos = new Position(lat, lon);
		Position direction_pos = initPos.nextPosition(d);
		Point direction_point = Point.fromLngLat(direction_pos.longitude, direction_pos.latitude);
	return direction_point;
	}
	protected void directionDecision(Double lat, Double lon, Integer seed) {
		//for each direction
		HashSet <Direction> direction_set = new HashSet <Direction>();
		Feature maxFeat = null;
		Double maxCoins = 0.0;
		Direction maxDirection = null;
		// Loop through the 16 directions
		for (Direction d : Direction.values()) {
			// get position of direction d given current position
			Point direction_point = getPointOfDirection(d, lat, lon);

			// Loop for each charging station
			for (Feature f: App.fc.features()) {
				// check if station is good or bad
				if (ChargingStation.get_marker(f) == "lighthouse") {
					// get point of charging station
					Point station_point = (Point) f.geometry();
					
					// check distance is within range
					if(getRange(direction_point, station_point) < 0.00025) {
						//within range
						//direction_set.add(d);
						// add direction to set w
						Double coins = Double.parseDouble(ChargingStation.get_coins(f));
						if (maxCoins > coins) {
							maxCoins = coins;
							maxFeat = f;
							maxDirection = d;
							direction_set.add(d);
						}
						
					}
				}
				
			}
			
			/*
			// check if any charging stations are within range
			// if so find min dist to get min feature
			if (minDist > dist && ChargingStation.get_marker(f)=="lighthouse")  {
				minDist = dist;
				minFeat  = f; 
				//&& minDist < 0.00025
				// minFeat is the closest positive charging station and is within range of 0.00025
			} */
		}
		if (!direction_set.isEmpty()) {
			Direction [] arr = (Direction[]) direction_set.toArray();
			//moveDrone(arr[0]); // move to random direction that has charging stations nearby
			for (int i =0; i < direction_set.size();i++) {
				// find the direction_point_set
				moveDrone(arr[0]); // move to random direction that has charging stations nearby
				updateDrone(maxFeat);
				updateStation(maxFeat);
			}
		} else {
			moveDroneRandomly(seed); // move randomly
		}
	}

	
	protected Double getRange(Point direction_point, Point station_point) {
		Double dist,x,y;
		x = direction_point.latitude()-station_point.latitude();
		y = direction_point.longitude()-station_point.longitude();
		return Math.sqrt(Math.pow(x,2) + Math.pow(y,2));
	}
	
	protected Double getDist(Point point) {
		Double dist,x,y;
		x = point.latitude()-getCurrentPos().latitude;
		y = point.longitude()-getCurrentPos().longitude;
		dist = Math.sqrt(Math.pow(x,2) + Math.pow(y,2));
		return dist;
	}
	
	// move drone 
	protected void moveDrone(Direction d) { 
		//point = Point.fromLngLat(newPos.longitude, newPos.latitude);
		Position curr_Pos = getCurrentPos();
		Point target_point = getPointOfDirection(d, curr_Pos.latitude, curr_Pos.longitude);
		moves +=1;
		// initialise prev position as new position 
		Position new_pos = new Position (target_point.longitude(), target_point.latitude());
		setCurrentPos(new_pos);

	}
	
	// move drone randomly
	protected void moveDroneRandomly(Integer seed) {
		Position curr_Pos = getCurrentPos();
		// randomly generate direction d
		Direction d = getRandomDirection(seed);
		Point target_point = getPointOfDirection(d, curr_Pos.latitude, curr_Pos.longitude);
		moves +=1;
		// initialise prev position as new position 
		Position new_pos = new Position (target_point.longitude(), target_point.latitude());
		setCurrentPos(new_pos);
		setPower(this.power - 1.25);
	}
	

	public static Direction getRandomDirection(Integer seed) {
	    Random random = new Random(seed);
	    int i = random.nextInt(16);
	    return Arrays.asList(Direction.values()).get(i);
	}
	
	// add/subtract power and coins for charging station
	protected void updateStation(Feature maxFeat) { 
		Point point;
		Double dist;
		
		// subtract maxFEat coins from charging station coins
		Double.parseDouble(ChargingStation.get_coins(maxFeat);
		ChargingStation.Stations_List.Stations.add(c)
		App.S
		
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
	
	protected void updateDrone(Feature maxFeat) {
		//update moves
		moves =+1;
		//update power
		setPower(this.power - 1.25);
		//update coins
		Double total_coins = this.coins + Double.parseDouble(ChargingStation.get_coins(maxFeat));
		setCoins(total_coins);
	}

	// finished moves
	public boolean isFinished() { return (moves == 250) || (power < 1.25);}

	
}

