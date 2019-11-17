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
		
		for (Direction d : Direction.values()) {
			// get position of direction d given current position
			Point direction_point = getPointOfDirection(d, lat, lon);
			// find nearest (good) stations from that d 
			findGoodNearestStations(directionCharging, d, direction_point);	
		}
		if (directionCharging.isEmpty()) {
			System.out.println("random");
			moveDroneRandomly(getSeed());
			
		}else {
			Direction dir =null;
			ChargingStation sta= null;
			System.out.println("move");
			for (Direction i : directionCharging.keySet()) {
				dir=i;
				sta=directionCharging.get(i);
				//System.out.println(dir);
			}
			moveDrone(dir,sta);
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
		Point direction_point = getPointOfDirection(d, curr_Pos.latitude, curr_Pos.longitude);
		
		Double total_power = this.power + ChargingStation.get_power(maxFeat);
		this.power = this.power + total_power;
		Double total_coins = this.coins + ChargingStation.get_coins(maxFeat);
		this.coins = this.coins + total_coins;

		updateDrone(curr_Pos, d);

		// initialise prev position as new position 
		Position new_pos = new Position (direction_point.latitude(), direction_point.longitude());

		//if (inPlayArea(new_pos)==true) {
		setCurrentPos(new_pos);
		//}
		//updateStation(maxFeat);
		//update station
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

		
		//writeToTextFile(curr_Pos,d, new_pos, textfile);
	}
	
	protected void moveDroneRandomly(Integer seed) {
		//HashMap <Direction, ChargingStation> directionCharging = new HashMap <Direction, ChargingStation>();
		Position curr_Pos = getCurrentPos();
		// randomly generate direction d
		Direction d = getRandomDirection(seed);
		Point direction_point = getPointOfDirection(d, curr_Pos.latitude, curr_Pos.longitude);
		updateDrone(curr_Pos, d);
		//System.out.print(d);
		// initialise prev position as new position 
		Position new_pos = new Position (direction_point.latitude(), direction_point.longitude());
		Point n = convertToPoint(new_pos);
		Point a = convertToPoint(getCurrentPos());

		setCurrentPos(new_pos);
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
				directionCharging.put(d, f);
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
		System.out.print(Point.fromLngLat(pos.latitude,pos.longitude));
		return (Point) Point.fromLngLat(pos.latitude,pos.longitude);
	}
	
	protected Double getRange(Point direction_point, Point station) {
		Double dist,x,y;
		x = station.latitude()-direction_point.latitude();
		y = station.longitude()-direction_point.longitude();
		return Math.sqrt(Math.pow(x,2) + Math.pow(y,2));
	}
	
	
	protected void updateDrone(Position curr_Pos, Direction d) {
		this.movesLeft = getMovesLeft()-1;
		System.out.print("moves left:");
		System.out.println(this.movesLeft);

		this.power = getPower()-1.25;
		System.out.print("power: ");
		System.out.println(this.power);
		//System.out.println(curr_Pos.latitude);
		//System.out.println(curr_Pos.longitude);
				
		movesHistory.add(convertToPoint(curr_Pos));
		//System.out.println(convertToPoint(curr_Pos).coordinates());

		directionHistory.add(d);
		powerHistory.add(this.power);
		coinsHistory.add(this.coins);
	}
	
	public static Direction getRandomDirection(Integer seed) {
	    Random random = new Random(seed);
	    int i = random.nextInt(16);
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

