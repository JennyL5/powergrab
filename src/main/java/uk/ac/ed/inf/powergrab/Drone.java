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
	public Position currentPos; // lon, lat
	public Integer movesLeft; // count the number of moves made
	public Double coins; // count coins
	public Double power; // keep track of power
	public Integer seed;
	public List<ChargingStation> Stations;
	private Random random;
	// public List <ChargingStation> goodStations = goodStations();
	// public List <ChargingStation> badStations = badStations();

	public String textfile;

	public ArrayList<Point> movesHistory = new ArrayList<Point>();
	public ArrayList<Double> coinsHistory = new ArrayList<Double>();
	public ArrayList<Double> powerHistory = new ArrayList<Double>();
	public ArrayList<Direction> directionHistory = new ArrayList<Direction>();
	public ArrayList<Integer> movesLeftHistory = new ArrayList <Integer>();

	// public List<ChargingStation> goodStations_List = goodStations(Stations);

	// constructors

	public Drone(Position currentPos, Double coins, Double power, Integer seed, List<ChargingStation> Stations) {
		this.currentPos = currentPos;
		this.movesLeft = 250;
		this.coins = 0.0;
		this.power = 250.0;
		this.seed = seed;
		this.Stations = Stations;
		this.random = new Random(seed);
		// this.movesHistory.add(currentPos);
	}

	// getters
	public Position getCurrentPos() {
		return currentPos;
	}

	public Integer getMovesLeft() {
		return this.movesLeft;
	}

	public double getCoins() {
		return this.coins;
	}

	public double getPower() {
		return this.power;
	}

	public Integer getSeed() {
		return this.seed;
	}

	public List<ChargingStation> getStations() {
		return this.Stations;
	}

	public ArrayList<Point> getMovesHistory() {
		return this.movesHistory;
	}

	// setters
	protected void setCurrentPos(Position currentPos) {
		this.currentPos = currentPos;
	}

	protected void setMoves(Integer movesLeft) {
		this.movesLeft = movesLeft;
	}

	protected void setCoins(Double coins) {
		this.coins = coins;
	}

	protected void setPower(Double power) {
		this.power = power;
	}

	protected void setSeed(Integer seed) {
		this.seed = seed;
	}

	protected void setStations(List<ChargingStation> Stations) {
		this.Stations = Stations;
	}

	protected void setMovesHistory(ArrayList<Point> movesHistory) {
		this.movesHistory = movesHistory;
	}

	// check if drone is within play area
	protected boolean inPlayArea(Position pos) {
		return (pos.inPlayArea());
	}


	protected void directionDecision() throws IOException {
		Double lat = this.currentPos.latitude;
		Double lon = this.currentPos.longitude;
		// to store nearby stations
		HashMap<Direction, ChargingStation> goodStationsInRange = new HashMap<Direction, ChargingStation>();
		HashMap<Direction, ChargingStation> badStationsInRange = new HashMap<Direction, ChargingStation>();

		// Loop through the 16 directions
		for (Direction d : Direction.values()) {
			// find nearest (good) stations from that d within rANGE
			HashMap<Direction, ChargingStation>  goodStationsNearby = (findStationsInRange(goodStations(), d));
			goodStationsInRange.putAll(goodStationsNearby);
			HashMap<Direction, ChargingStation>  badStationsNearby = (findStationsInRange(badStations(), d));
			badStationsInRange.putAll(badStationsNearby);
			}
		
		ArrayList<Direction> goodDirections = new ArrayList<Direction>(goodStationsInRange.keySet());
		ArrayList <Direction> badDirections = new ArrayList<Direction>(badStationsInRange.keySet());

		Direction maxCoinDirection= null;
		ChargingStation sta = null;
		if (!goodDirections.isEmpty()) {
			maxCoinDirection= findMaxCoins(goodDirections, goodStationsInRange);
			sta = goodStationsInRange.get(maxCoinDirection);
		}
		
		// System.out.println(GoodDirectionCharging.isEmpty());
		if (!goodDirections.isEmpty() && sta.get_coins() >0) {
			// Find good CS with max coins
			System.out.println("move to closest good station");
			System.out.println(maxCoinDirection);
			//System.out.println(sta);

			// System.out.println(dir);
			Position newPos = this.currentPos.nextPosition(maxCoinDirection);
			if (newPos.inPlayArea()) {
				moveDrone(maxCoinDirection, sta);
				updateStation(sta);
				setCurrentPos(newPos);
			} // ELSE DIRECTION POINT OUTSIDE PLAY AREA BUT CS IS IN PLAY AREA
			
		} else if((badStationsInRange.isEmpty() || badStationsInRange.size()==16) ){

		// No bad stations nearby, move completely randomly
		
			Direction d = getRandomDirection();
			Position newPos = this.currentPos.nextPosition(d);
			while (!newPos.inPlayArea()) {
				d = getRandomDirection();
				newPos = this.currentPos.nextPosition(d);
			}
			System.out.println("No bad Stations nearby or All directions have bad charging stations");
			// Point direction_point = convertToPoint(new_pos);
			moveDroneRandomly(d); 
			setCurrentPos(newPos);
				
		} else {
			// Avoid bad stations
			// get random d from not directions of BadDirectionCharging
			// find random station from not directions of BadDirectionCharging
			System.out.println("find random station from not directions of BadDirectionCharging");
			Direction randomDir = randDirection(badStationsInRange);
			ChargingStation randomSt =badStationsInRange.get(randomDir);
			Position newPos = this.currentPos.nextPosition(randomDir);

			while (!newPos.inPlayArea()) {
				randomDir = randDirection(badStationsInRange);
				randomSt = badStationsInRange.get(randomDir);
				newPos = this.currentPos.nextPosition(randomDir);
			}
			moveDroneRandomly(randomDir); 
			setCurrentPos(newPos);
			
		}

	}
	
	protected Direction findMaxCoins(ArrayList<Direction> GoodDirection, HashMap <Direction, ChargingStation> GoodDirectionCharging) {
		Double maxCoins = (double) Integer.MAX_VALUE;

		Direction maxDir = GoodDirection.get(0);
		for (Direction d : GoodDirection) {
			ChargingStation cs = GoodDirectionCharging.get(d);
			if (cs.get_coins()>maxCoins) {
				maxCoins=cs.get_coins();
				maxDir = d;
			}
		}
		return maxDir;
	}
	
	protected Direction randDirection( HashMap <Direction, ChargingStation> BadDirectionCharging) {
		Set<Direction> keys = (Set<Direction>) BadDirectionCharging.keySet();
		ArrayList <Direction> badDir = new ArrayList <Direction> (keys);
		ArrayList <Direction> notBadDir = new ArrayList <Direction>();

		for (Direction d : Direction.values()) {
			if (!badDir.contains(d)){
				notBadDir.add(d);
			}
		}
		
		int num = this.random.nextInt(notBadDir.size());

		Direction randomDir = notBadDir.get(num);

		return randomDir;
		
	}

	// add/subtract power and coins for charging station
	protected void updateStation(ChargingStation maxFeat) {
		for (ChargingStation CS : Stations) {
			if (CS.pos.equals(maxFeat.pos)) {
				if (CS.get_coins() > 0) {
					CS.setCoins(0.0);
					System.out.println("-----Charging Station: ");
					System.out.println("coins: ");
					System.out.println(CS.get_coins());	
				} else {
					// CS.power is negative
					CS.setCoins(this.coins-CS.coins);
					System.out.println("-----Charging Station: ");
					System.out.println("coins: ");
					System.out.println(CS.get_coins());	
				}
				
				if (CS.get_power() > 0) {
					CS.setPower( 0.0);
					System.out.println("power: ");
					System.out.println(CS.get_power());
				} else {
					// CS.power is negative
					CS.setPower(this.power-CS.power);
					System.out.println("power: ");
					System.out.println(CS.get_power());
				}
			}
		}

	}

	protected void updateDrone(Direction d) {
		this.movesLeft = getMovesLeft() - 1;
		System.out.println("moves left:");
		System.out.println(this.movesLeft);

		this.power = getPower() - 1.25;
		System.out.println("power: ");
		System.out.println(this.power);
		System.out.println(d);
		directionHistory.add(d);
		powerHistory.add(this.power);
		coinsHistory.add(this.coins);
		movesLeftHistory.add(this.movesLeft); //NEED REMOVED when finished
	}

	protected Direction getMinDistance(HashMap<Direction, ChargingStation> directionCharging,
			Double lat, Double lon) {

		Double minRange = (double) Integer.MAX_VALUE;
		
		Direction minDirection = null;

		for (Entry<Direction, ChargingStation> entry : directionCharging.entrySet()) {
			Direction d = entry.getKey();
			ChargingStation CS = entry.getValue();
			Point stationPoint = convertToPoint(CS.pos);
			Position directionPos =this.currentPos.nextPosition(d);
			Point direction_point = convertToPoint(directionPos);
			Double distance = getRange(direction_point, stationPoint);
			minDirection = d;
			if (distance < minRange) {
				minRange = distance;
				minDirection = d;
			}
		}
		System.out.println("min Direction");
		System.out.println(minDirection);

		return minDirection;
	}

	// move drone to nearby charging station
	protected void moveDrone(Direction d, ChargingStation c) throws IOException {
		//Position new_pos = curr_Pos.nextPosition(d);
		Double totalPower = this.power + c.get_power();
		this.power = totalPower;
		// System.out.println(this.power);

		Double totalCoins = this.coins + c.get_coins();
		this.coins = totalCoins;

		movesHistory.add(convertToPoint(this.currentPos));

		System.out.println("coins:");
		System.out.println(this.coins);
		
		updateDrone(d);
	}

	protected void moveDroneRandomly(Direction d) {
		// randomly generate direction d
		//Position new_pos = getNewPos(d);
		movesHistory.add(convertToPoint(this.currentPos));
		System.out.println("coins: ");
		System.out.println(this.coins);
		updateDrone(d);
	}

	protected HashMap<Direction, ChargingStation> findStationsInRange(List<ChargingStation> list, Direction d) {
		HashMap<Direction, ChargingStation> directionCharging = new HashMap<Direction, ChargingStation>();
		Position directionPos = this.currentPos.nextPosition(d);
		Point directionPoint = convertToPoint(directionPos);
		// find in range stations that are good/bad
		int count = 0;
		for (ChargingStation f : list) {
			Point station_point = convertToPoint(list.get(count).pos);
			count++;
			if (getRange(directionPoint, station_point) < 0.00025) {
				directionCharging.put(d, f);
			}
		}
		return directionCharging;
	}

	protected List<ChargingStation> goodStations() {
		List<ChargingStation> goodStations_List = new ArrayList<ChargingStation>();
		for (ChargingStation f : Stations) {
			if (f.get_marker().contains("lighthouse")) {
				goodStations_List.add(f);
			}
		}
		return goodStations_List;
	}

	protected List<ChargingStation> badStations() {
		List<ChargingStation> badStations_List = new ArrayList<ChargingStation>();
		for (ChargingStation f : Stations) {
			if (f.get_marker().contains("danger")) {
				badStations_List.add(f);
			}
		}
		return badStations_List;
	}

	// Picks direction bases on Direction with station with max coins

	protected Point convertToPoint(Position pos) {
		// System.out.println(Point.fromLngLat(pos.latitude,pos.longitude));
		return (Point) Point.fromLngLat(pos.longitude, pos.latitude);
	}

	protected Double getRange(Point direction_point, Point station) {
		Double x, y;
		x = station.latitude() - direction_point.latitude();
		y = station.longitude() - direction_point.longitude();
		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}

	protected Direction getRandomDirection() {
		int i = this.random.nextInt(16);
		return Arrays.asList(Direction.values()).get(i);
	}

	// finished moves
	public boolean isFinished() {
		return (this.movesLeft == 0) || (this.power < 1.25);
	}

	public String totxt() {
		String text = "";
		for (int i = 0; i <= this.movesHistory.size() - 2; i++) {
			String a = Double.toString(this.movesHistory.get(i).latitude());
			String b = Double.toString(this.movesHistory.get(i).longitude());
			String c = this.directionHistory.get(i).toString();
			String d = Double.toString(this.movesHistory.get(i + 1).latitude());
			String e = Double.toString(this.movesHistory.get(i + 1).longitude());
			String f = this.coinsHistory.get(i).toString();
			String g = this.powerHistory.get(i).toString();
			String h = this.movesLeftHistory.get(i).toString();
			text += a + ", " + b + ", " + c + ", " + d + ". " + e + ", " + f + ", " + g + "\n" + h +"," + "\n";
		}
		return text;
	}

}
