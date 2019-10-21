package uk.ac.ed.inf.powergrab;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;


abstract public class Drone { 
	protected  Position currentPos; // lon, lat
	protected  Integer moves; // count the number of moves made
	protected  Double coins; // count coins
	protected  Double power; // keep track of power
	protected Integer seed;
	protected  List <ChargingStation> Stations;
	protected  String textfile;
	public static FeatureCollection fc;

	public Drone() {	
		moves = 0;
		coins = 0.0;
		power = 0.0;
	}
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
	
	protected void directionDecision(Double lat, Double lon, Integer seed, List <ChargingStation> Stations, String textfile) throws IOException {
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
			for (ChargingStation f: Stations) {
				System.out.print(f);

				// check if station is good or bad
				if (ChargingStation.get_marker(f) == "lighthouse") {
					// get point of charging station
					Point station_point = (Point) f.geometry();
					
					// check distance is within range
					if(getRange(direction_point, station_point) < 0.00025) {
						//within range
						//direction_set.add(d);
						// add direction to set w
						Double station_coins = Double.parseDouble(ChargingStation.get_coins(f));
						if (maxCoins > station_coins) {
							maxCoins = station_coins;
							maxFeat = f;
							maxDirection = d;
							direction_set.add(d);
						}
						
					}
				}
				
			}
		}
		if (!direction_set.isEmpty()) {
			Direction [] arr = (Direction[]) direction_set.toArray();
			//moveDrone(arr[0]); // move to random direction that has charging stations nearby
			for (int i =0; i < direction_set.size();i++) {
				// find the direction_point_set
				moveDrone(arr[0], Stations, textfile); // move to random direction that has charging stations nearby
				updateDrone(maxFeat);
				updateStation(Stations, maxFeat);
				
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
	protected void moveDrone(Direction d, List <ChargingStation> Stations, String textfile) throws IOException { 
		//point = Point.fromLngLat(newPos.longitude, newPos.latitude);
		Position curr_Pos = getCurrentPos();
		Point target_point = getPointOfDirection(d, curr_Pos.latitude, curr_Pos.longitude);
		moves +=1;
		// initialise prev position as new position 
		Position new_pos = new Position (target_point.longitude(), target_point.latitude());
		setCurrentPos(new_pos);
		writeToTextFile(curr_Pos,d, new_pos, Stations, textfile);
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
	protected void updateStation(List Stations, Feature maxFeat) { 
		Point point;
		Double dist;
		
		// subtract maxFeat coins from charging station coins
		//Double d = Double.parseDouble(ChargingStation.get_coins(maxFeat));
		//Stations
		ChargingStation c = new ChargingStation();
		
		// set new coins and power of station to 0
		c.setCoins(maxFeat, 0.0);
		c.setPower(maxFeat, 0.0);
		
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
	
	
	protected void writeToTextFile(Position curr_Pos, Direction d, Position new_pos, List <ChargingStation> Station, String textfile) throws IOException {
		//55.944425,-3.188396,SSE,55.944147836140246,-3.1882811949702905,0.0,248.75
		//before_lat, before_lon, dir, after_lat, after_lon, Drone_coins, Drone_power
		Double prev_lat = curr_Pos.latitude;
		Double prev_lon = curr_Pos.longitude;
		
		Double next_lat = new_pos.latitude;
		Double next_lon = new_pos.longitude;
		
		Double coins = this.coins;
		Double power = this.power;
		String list [] = new String[] {prev_lat.toString(), prev_lon.toString(), d.toString(), next_lat.toString(), next_lon.toString(), coins.toString(), power.toString()};
		List<String> movesList = Arrays.asList(list);
		
		
		Path file = Paths.get(textfile);
		Files.write(file, movesList, StandardCharsets.UTF_8);
		//System.out.print(textfile);
		//PrintWriter writer = new PrintWriter("the-file-name.txt", "UTF-8");
		//writer.println("The first line");
		//writer.close();
	}
}

