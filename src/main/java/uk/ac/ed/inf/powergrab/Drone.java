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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;


abstract public class Drone { 
	private static final Direction N = null;
	private static final Direction S = null;
	private static final Direction SSE = null;
	public  Position currentPos; // lon, lat
	public Integer movesLeft; // count the number of moves made
	public  Double coins; // count coins
	public  Double power; // keep track of power
	public Integer seed;
	public  List <ChargingStation> Stations;
	public  String textfile;

	public ArrayList<Point> movesHistory = new ArrayList<Point>();
	public ArrayList<Double> coinsHistory = new ArrayList<Double>();
	public ArrayList<Double> powerHistory = new ArrayList<Double>();
	public ArrayList<Direction> directionHistory = new ArrayList<Direction>();

	//public List<ChargingStation> goodStations_List = goodStations(Stations);

	// constructors 
	
	public Drone(Position currentPos, Double coins, Double power, Integer seed, List <ChargingStation> Stations) {	
		this.currentPos = currentPos;
		this.movesLeft = 250;
		this.coins = 0.0;
		this.power = 250.0;
		this.seed = seed;
		this.Stations = Stations;
		//this.movesHistory.add(currentPos);
	}

	// getters
	public Position getCurrentPos() { return currentPos;}
	public Integer getMovesLeft() { return this.movesLeft;}
	public double getCoins() { return this.coins;}
	public double getPower() { return this.power;}
	public Integer getSeed() { return this.seed;}
	public List <ChargingStation> getStations() { return this.Stations;}
	public ArrayList<Point> getMovesHistory() { return this.movesHistory;}
	
	
	// setters
	protected void setCurrentPos(Position currentPos) { this.currentPos = currentPos;}
	protected void setMoves(Integer moves) {this.movesLeft = movesLeft;}
	protected void setCoins(Double coins) { this.coins = coins;}
	protected void setPower(Double power) { this.power = power;}
	protected void setSeed(Integer seed) {this.seed = seed;}
	protected void setStations(List <ChargingStation> Stations) {this.Stations = Stations;}
	protected void setMovesHistory(ArrayList <Point> movesHistory) {this.movesHistory = movesHistory;}
	
	// check if drone is within play area
	protected boolean inPlayArea(Position pos) { 
		return (pos.inPlayArea());
		}

	protected Point getPointOfDirection(Direction d, Double lat, Double lon) {
		// lat lon of the current position
		Position initPos = new Position(lat, lon);
		Point i = convertToPoint(initPos);
		Position direction_pos = initPos.nextPosition(d);
		Point direction_point = convertToPoint(direction_pos);
	return direction_point;
	}
	

	protected void directionDecision(Double lat, Double lon) throws IOException {
		HashMap <Direction, ChargingStation> directionCharging = new HashMap <Direction, ChargingStation>();
		// Loop through the 16 directions
		directionCharging.clear();
		for (Direction d : Direction.values()) {
			// get position of direction d given current position
			//System.out.print("lat");
			//System.out.print(lat);
			Point direction_point = getPointOfDirection(d, lat, lon);
			//System.out.print(direction_point.latitude());
			// find nearest (good) stations from that d 
			findGoodNearestStations(directionCharging, d, direction_point);	
		}
		//System.out.print("Size of direction charging");
		//System.out.print(directionCharging.size());
		//System.out.print(directionCharging.keySet());


		if (directionCharging.isEmpty()) {
			System.out.println("random");
			Direction d = getRandomDirection(seed);
			Position new_pos = getNewPos(d);
			if (new_pos.inPlayArea()){
				moveDroneRandomly(d); //newpos
				setCurrentPos(new_pos);

			} else {
				System.out.println("Stuck!!!");
			}
			
		}else {
			Direction dir =null;
			ChargingStation sta= null;
			System.out.println("move");
			for (Direction i : directionCharging.keySet()) {
				dir=i;
				//System.out.println(i);
				sta=directionCharging.get(i);
				//System.out.println(sta.coins);
				
			}
			Position new_pos = getNewPos(dir);
			if (new_pos.inPlayArea()){
				moveDrone(dir,sta);
				setCurrentPos(new_pos);
			} else {
				System.out.println("Stuck!!! R");

			}

			//moveDrone((Direction) directionCharging.entrySet().toArray()[0], directionCharging.get(key));
		}
		//System.out.println(coinsHistory);
		//System.out.println(powerHistory);
		//System.out.println(movesHistory);
		//System.out.println(directionHistory);
		//System.out.println(directionHistory.size());

	}
	
	// move drone to nearby charging station
	protected void moveDrone(Direction d, ChargingStation maxFeat) throws IOException { 
		Position curr_Pos = getCurrentPos();
		//System.out.print(direction_point);
		Position new_pos = getNewPos(d);

		//System.out.print(new_pos.latitude);
		//System.out.print(new_pos.longitude);
		Double total_power = this.power + ChargingStation.get_power(maxFeat);
		this.power = this.power + total_power;
		Double total_coins = this.coins + ChargingStation.get_coins(maxFeat);
		this.coins = this.coins + total_coins;

		updateDrone(d);
		movesHistory.add(convertToPoint(this.currentPos));
		
		Double st_coins, st_power =0.0;
		
		if (ChargingStation.get_coins(maxFeat) > 0){
			st_coins =0.0;
		} else{
			st_coins =ChargingStation.get_coins(maxFeat) + this.coins;
		}
		
		if (ChargingStation.get_power(maxFeat) >0) {
			st_power = 0.0;
		}else {
			st_power = ChargingStation.get_power(maxFeat) + this.power;
		}
		maxFeat.setCoins(maxFeat, st_coins);
		System.out.print("coins:");
		System.out.println(this.coins);
		maxFeat.setPower(maxFeat, st_power);
	}
	
	protected Position getNewPos(Direction d) { 
		Point direction_point =  getPointOfDirection(d, this.currentPos.latitude, this.currentPos.longitude);

		return new Position (direction_point.latitude(), direction_point.longitude());
	}
	
	protected void moveDroneRandomly(Direction d) {
		//HashMap <Direction, ChargingStation> directionCharging = new HashMap <Direction, ChargingStation>();
		// randomly generate direction d
		//System.out.print(d);
		// initialise prev position as new position 
		Position new_pos = getNewPos(d);
		updateDrone(d);
		movesHistory.add(convertToPoint(this.currentPos));

		Point n = convertToPoint(new_pos);

		Point a = convertToPoint(getCurrentPos());
		//System.out.print(new_pos.latitude);
		//System.out.print(new_pos.longitude);
		Point b = convertToPoint(getCurrentPos());

		System.out.print("coins: ");
		System.out.print(this.coins);
	}
	
	
	protected void findGoodNearestStations(HashMap <Direction, ChargingStation>directionCharging, Direction d, Point direction_point) {
		int count =0;
		for (ChargingStation f: goodStations()) {
			Point station_point = convertToPoint(Stations.get(count).pos);
			count++;
			//System.out.println(getRange(direction_point, station_point));
			if(getRange(direction_point, station_point) < 0.00025) {
				//System.out.println("direction vs station points");
				//System.out.print(d);
				//System.out.print(f.coins);
				//System.out.print(f.power);

				directionCharging.put(d, f);
				//System.out.print("coins");
				//System.out.print(f.coins);
			}
		}
	}


	protected List<ChargingStation> goodStations(){
		List<ChargingStation> goodStations_List = new ArrayList <ChargingStation>();
		for (ChargingStation f: Stations) {
			if (ChargingStation.get_marker(f).contains("lighthouse")) {
				goodStations_List.add(f);
			}
		}
		return goodStations_List;	
	}
	
	protected List<ChargingStation> badStations(){
		List<ChargingStation> badStations_List = new ArrayList <ChargingStation>();
		for (ChargingStation f: Stations) {
			if (ChargingStation.get_marker(f).contains("danger")) {
				badStations_List.add(f);
			}
		}
		return badStations_List;	
	}
	
	
	// Picks direction bases on Direction with station with max coins
	
	
	protected Point convertToPoint(Position pos) {
		//System.out.print(Point.fromLngLat(pos.latitude,pos.longitude));
		return (Point) Point.fromLngLat(pos.longitude,pos.latitude);
	}
	
	protected Double getRange(Point direction_point, Point station) {
		Double x,y;
		x = station.latitude()-direction_point.latitude();
		y = station.longitude()-direction_point.longitude();
		return Math.sqrt(Math.pow(x,2) + Math.pow(y,2));
	}
	
	
	protected void updateDrone(Direction d) {
		this.movesLeft = getMovesLeft()-1;
		System.out.print("moves left:");
		System.out.println(this.movesLeft);

		this.power = getPower()-1.25;
		System.out.print("power: ");
		System.out.println(this.power);
		//System.out.println(curr_Pos.latitude);
		//System.out.println(curr_Pos.longitude);
				
		//movesHistory.add(convertToPoint(this.currentPos));
		//System.out.println(convertToPoint(curr_Pos).coordinates());
		//System.out .print(this.currentPos.latitude);
		//System.out .print(this.currentPos.longitude);

		directionHistory.add(d);
		powerHistory.add(this.power);
		coinsHistory.add(this.coins);
	}
	
	public static Direction getRandomDirection(Integer seed) {
	    //Random random = new Random(seed);
		Random random = new Random();
	    int i = random.nextInt(16);
	    System.out.print(".............");
	    System.out.print(i);
	    System.out.print(Arrays.asList(Direction.values()).get(i));
	    return Arrays.asList(Direction.values()).get(i);
	}
	
	// add/subtract power and coins for charging station
	protected void updateStation(ChargingStation maxFeat) { 
		Point point;
		Double dist;
		
		// subtract maxFeat coins from charging station coins
		//Double d = Double.parseDouble(ChargingStation.get_coins(maxFeat));
		//Stations
		//maxFeat.setCoins(maxFeat, st_coins);
		// set new coins and power of station to 0
		//c.setCoins(maxFeat, 0.0);
		//c.setPower(maxFeat, 0.0);
		
	}
	

	// finished moves
	public boolean isFinished() { return (this.movesLeft == 0) || (this.power < 1.25);}
	
	
	public String totxt() {
		String text ="";
		for (int i = 0; i<=this.movesHistory.size()-2;i++) {
			String a = Double.toString(this.movesHistory.get(i).latitude());
			String b = Double.toString(this.movesHistory.get(i).longitude());
			String c = this.directionHistory.get(i).toString();
			String d = Double.toString(this.movesHistory.get(i+1).latitude());
			String e = Double.toString(this.movesHistory.get(i+1).longitude());
			String f = this.coinsHistory.get(i).toString();
			String g = this.powerHistory.get(i).toString();
			text += a+", "+b+", "+c+", "+d+". "+e+", "+f+", "+g+"\n";
		}
		return text;
	}
	
}

