package uk.ac.ed.inf.powergrab;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
	//public  List <ChargingStation> goodStations = goodStations();
	//public  List <ChargingStation> badStations = badStations();

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
	protected void setMoves(Integer movesLeft) {this.movesLeft = movesLeft;}
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
		// to store nearby stations
		HashMap <Direction, ChargingStation> GoodDirectionCharging = new HashMap <Direction, ChargingStation>();
		HashMap <Direction, ChargingStation> BadDirectionCharging = new HashMap <Direction, ChargingStation>();

		// Loop through the 16 directions
		for (Direction d : Direction.values()) {
			// find nearest (good) stations from that d 
			GoodDirectionCharging= findNearestStations(goodStations(), d);	
			BadDirectionCharging= findNearestStations(badStations(), d);	
		}
		
		//System.out.println(GoodDirectionCharging.isEmpty());
		// Find good CS with min range
		HashMap <Direction, ChargingStation> minDirectionCharging = new HashMap <Direction, ChargingStation>();

		if (!GoodDirectionCharging.isEmpty()){
			// Find good CS with min range
			minDirectionCharging = getMinDistance(GoodDirectionCharging, lat, lon);
		} //else {
		//	System.out.print("!!!!!!!!!!!!!!!!!GoodDirectionCharging empty!!!!!!!!!!!!!!");
		//}
		
		// if no closest good station
			
	
			if (minDirectionCharging.isEmpty()) {
				System.out.println("random");
				// No bad stations nearby, move completely randomly 
				if (BadDirectionCharging.isEmpty()) {
					Direction d = getRandomDirection(this.seed);
					Position new_pos = getNewPos(d);
					if (new_pos.inPlayArea()){
						System.out.print("No bad Stations nearby");
						//Point direction_point = convertToPoint(new_pos);
						moveDroneRandomly(d); //newpos
						updateDrone(d);
						setCurrentPos(new_pos);
						}
				} else {
					// Avoid bad stations
					// get random d from not directions of BadDirectionCharging
					// find random station from not directions of BadDirectionCharging
					System.out.print("find random station from not directions of BadDirectionCharging");
					Random rand = new Random();
				    rand.setSeed(this.seed);
				   
				    Direction[] keys = (Direction[]) BadDirectionCharging.keySet().toArray();
				    Direction[] notBadDir = new Direction[Direction.values().length-keys.length];
				    for (int i=0;i<keys.length;i++) {
				    	if(keys[i]!=keys[i]) {
				    		notBadDir.add
				    	}
				    }
				    Direction randomDir = (Direction) keys[rand.nextInt(keys.length)];// not in keysFIX!!!!!! CREATE NEW ARRAY FOR NOT BAD STATIONS
				    ChargingStation randomSt = BadDirectionCharging.get(randomDir);
	
					Position new_pos = getNewPos(randomDir);
					if (new_pos.inPlayArea()){
						moveDrone(randomDir, randomSt); //newpos
						updateDrone(randomDir);
						setCurrentPos(new_pos);
						}
				} 
				
			}else {
				//Move to closest good station
				Direction dir =null;
				ChargingStation sta= null;
				System.out.println("move");
				for (Direction i : minDirectionCharging.keySet()) {
					dir=i;
					//System.out.println(i);
					sta=GoodDirectionCharging.get(i);
					//System.out.println(sta.coins);
				}
				//System.out.print(dir);
				Position new_pos = getNewPos(dir);
				if (new_pos.inPlayArea()){
					moveDrone(dir,sta);
					updateDrone(dir);
					updateStation(sta);
					setCurrentPos(new_pos);
				}
	
				//moveDrone((Direction) directionCharging.entrySet().toArray()[0], directionCharging.get(key));
			}
		

		//System.out.println(coinsHistory);
		//System.out.println(powerHistory);
		//System.out.println(movesHistory);
		//System.out.println(directionHistory);
		//System.out.println(directionHistory.size());

	}
	
	
	// add/subtract power and coins for charging station
	protected void updateStation(ChargingStation maxFeat) { 
		
		//ChargingStation c = new ChargingStation();

		for(ChargingStation CS : Stations) {
			if (CS.pos.equals(maxFeat.pos)) {
				if (CS.get_coins(CS)> 0) {
					CS.setCoins(CS, 0.0);
				}
				if (CS.get_power(CS) >0) {
					CS.setPower(CS, 0.0);
				}
			}
		}
	
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
	
	
	protected HashMap<Direction, ChargingStation> getMinDistance (HashMap<Direction,ChargingStation>directionCharging, Double lat, Double lon) {
		HashMap <Direction, ChargingStation> minDirectionCharging = new HashMap <Direction, ChargingStation>();
		
		Double minRange = (double) Integer.MAX_VALUE;
		Direction minDirection=null;
		ChargingStation minStation = null;
		
		//if (!directionCharging.isEmpty()) {
		for (Entry<Direction, ChargingStation> entry : directionCharging.entrySet()) {
		    Direction d = entry.getKey();
		    //System.out.print(d);
		    ChargingStation CS = entry.getValue();
		    //ChargingStation CS = (ChargingStation) value;
		    //System.out.print(CS.coins);

		    Point station_point = convertToPoint(CS.pos);
			Point direction_point = getPointOfDirection(d, lat, lon);
			Double distance = getRange(direction_point, station_point);
//			minRange = distance;
			minDirection = d;
			minStation = CS;
			if (distance<minRange) {
				minRange=distance;
				minDirection=d;
				minStation=CS;
			}
		}
		
		minDirectionCharging.put(minDirection, minStation);

		return minDirectionCharging;
	}
	
	
	
	// move drone to nearby charging station
	protected void moveDrone(Direction d, ChargingStation maxFeat) throws IOException {
		ChargingStation c = new ChargingStation();

		Position curr_Pos = getCurrentPos();
		//System.out.print(direction_point);
		Position new_pos = getNewPos(d);

		//System.out.print(new_pos.latitude);
		//System.out.print(new_pos.longitude);
		
		//exclude intial transaction
		//if (this.movesLeft != 250) { 
		//System.out.print(ChargingStation.get_power(maxFeat));
		//System.out.println(this.power);
		Double total_power = this.power + c.get_power(maxFeat);
		this.power = this.power + total_power;
		//System.out.println(this.power);

		Double total_coins = this.coins + c.get_coins(maxFeat);
		this.coins = this.coins + total_coins;

		updateDrone(d); //update power and moveLeft
		updateStation(maxFeat); //bad/good CS
		movesHistory.add(convertToPoint(this.currentPos));
			
		System.out.print("coins:");
		System.out.println(this.coins);

		
	}
	
	protected void moveDroneRandomly(Direction d) {
		// randomly generate direction d
		Position new_pos = getNewPos(d);
		updateDrone(d);
		movesHistory.add(convertToPoint(this.currentPos));
		System.out.print("coins: ");
		System.out.print(this.coins);
	}
	
	
	protected Position getNewPos(Direction d) { 
		Point direction_point =  getPointOfDirection(d, this.currentPos.latitude, this.currentPos.longitude);
		return new Position (direction_point.latitude(), direction_point.longitude());
	}
	



	protected HashMap<Direction, ChargingStation> findNearestStations(List<ChargingStation> list, Direction d) {
		HashMap <Direction, ChargingStation> directionCharging = new HashMap <Direction, ChargingStation>();
		Point direction_point = getPointOfDirection(d, this.currentPos.latitude, this.currentPos.longitude);

		int count =0;
		for (ChargingStation f: list) {
			Point station_point = convertToPoint(list.get(count).pos);
			//System.out.println(station_point);
			//System.out.println(direction_point);
			//System.out.println(getRange(direction_point, station_point));
			count++;
			//System.out.println(getRange(direction_point, station_point));
			if(getRange(direction_point, station_point) < 0.00025) {
				//System.out.println("direction vs station points");
				//System.out.println(d);
				//System.out.println("NEAR!!!!!!!!!!!!!!!!!!!");
				//System.out.print(f.coins);
				//System.out.print(f.power);

				directionCharging.put(d, f);
				//System.out.print("coins");
				//System.out.print(f.coins);
			}
		}
		return directionCharging;
	}

	protected List<ChargingStation> goodStations(){
		List<ChargingStation> goodStations_List = new ArrayList <ChargingStation>();
		//ChargingStation c = new ChargingStation();

		for (ChargingStation f: Stations) {
			if (f.get_marker(f).contains("lighthouse")) {
				goodStations_List.add(f);
			}
		}
		return goodStations_List;	
	}
	
	protected List<ChargingStation> badStations(){
		List<ChargingStation> badStations_List = new ArrayList <ChargingStation>();
		//ChargingStation c = new ChargingStation();

		for (ChargingStation f: Stations) {
			if (f.get_marker(f).contains("danger")) {
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
	

	protected static Direction getRandomDirection(Integer seed) {

	    Random random = new Random();
	    random.setSeed(seed);
		//Random random = new Random();
	    int i = random.nextInt(16);
	    //System.out.print(Arrays.asList(Direction.values()).get(i));
	    return Arrays.asList(Direction.values()).get(i);
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

