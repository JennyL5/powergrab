package uk.ac.ed.inf.powergrab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import com.mapbox.geojson.Point;

abstract public class Drone {
	public Position currentPos; 
	public Integer movesLeft; 
	public Double coins; 
	public Double power; 
	public Integer seed;
	public List<ChargingStation> Stations;
	private Random random;

	public ArrayList<Point> movesHistory = new ArrayList<Point>();
	public ArrayList<Double> coinsHistory = new ArrayList<Double>();
	public ArrayList<Double> powerHistory = new ArrayList<Double>();
	public ArrayList<Direction> directionHistory = new ArrayList<Direction>();
	public ArrayList<Integer> movesLeftHistory = new ArrayList <Integer>();

	// constructors
	public Drone(Position currentPos,Integer seed, List<ChargingStation> Stations) {
		this.currentPos = currentPos;
		this.movesLeft = 250;
		this.coins = 0.0;
		this.power = 250.0;
		this.seed = seed;
		this.Stations = Stations;
		this.random = new Random(seed);
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

	
	// gets direction of station with maximum coins
	protected Direction findMaxCoins(ArrayList<Direction> GoodDirection, HashMap <Direction, ChargingStation> GoodDirectionCharging) {
		Double maxCoins = (double) Integer.MAX_VALUE;

		Direction maxDir = GoodDirection.get(0);
		for (Direction d : GoodDirection) {
			ChargingStation cs = GoodDirectionCharging.get(d);
			if (cs.getCoins()>maxCoins) {
				maxCoins=cs.getCoins();
				maxDir = d;
			}
		}
		return maxDir;
	}
	
	// gets a random direction that is absent from the Directions from input HashMap
	protected Direction randDirection( ArrayList <Direction> badDir) {
		ArrayList <Direction> notBadDir = avoidBadDirection(badDir);
		int num = this.random.nextInt(notBadDir.size());
		Direction randomDir = notBadDir.get(num);
		return randomDir;
	}
	
	// gets a random direction that is absent from the Directions from input HashMap
	protected ArrayList<Direction> avoidBadDirection( ArrayList <Direction> badDir) {
		ArrayList <Direction> notBadDir = new ArrayList <Direction>();
		System.out.println(badDir.size());
		for (Direction d : Direction.values()) {
			if (!badDir.contains(d)){
				notBadDir.add(d);
			}
		}
		return notBadDir;
	}
	
	/**
	 * Returns a random direction for the drone and avoids the bad directions.
	 * It also ensures that the new position is within the range.
	 *
	 * @param  badDirections  an arraylist of directions with bad stations
	 * @return      the direction to move in
	 */
	// method for drone moving in random directions but avoiding bad stations
	public Direction avoidBadStations(ArrayList<Direction> badDirections) {
		// Avoid bad stations
		// get random d from not directions of BadDirectionCharging
		// find random station from directions absent from BadDirectionCharging
		System.out.println("find random station from not directions of BadDirectionCharging");
		Direction randomDir = randDirection(badDirections);
		Position newPos = this.currentPos.nextPosition(randomDir);

		while (!newPos.inPlayArea()) {
			randomDir = randDirection(badDirections);
			newPos = this.currentPos.nextPosition(randomDir);
		}
		return randomDir;
	}
	
	
	// updates power and coins of charging station // move to CS
	protected void updateStation(ChargingStation maxFeat) {
		for (ChargingStation CS : Stations) {
			if (CS.pos.equals(maxFeat.pos)) {
				if (CS.getCoins() > 0) {

					System.out.println("-----Charging Station: ");
					System.out.println("coins: ");
					System.out.println(CS.getCoins());	
					CS.setCoins(0.0);
					System.out.println("coins: ");
					System.out.println(CS.getCoins());	
				} else {
					// CS.power is negative
					System.out.println("-----Charging Station: ");
					System.out.println("coins: ");
					System.out.println(CS.getCoins());	
					CS.setCoins(0.0);
					System.out.println("coins: ");
					System.out.println(CS.getCoins());	
				}
				
				if (CS.getPower() > 0) {

					System.out.println("power: ");
					System.out.println(CS.getPower());
					CS.setPower( 0.0);
					System.out.println("power: ");
					System.out.println(CS.getPower());
				} else {
					// CS.power is negative

					System.out.println("power: ");
					System.out.println(CS.getPower());
					//CS.setPower(CS.power-this.power);
					CS.setPower(0.0);

					System.out.println("power: ");
					System.out.println(CS.getPower());
				
				}
			}
		}

	}
	
	// updates drone's movesLeft, power, and adds to their history
	protected void updateDrone(Direction d) {
		System.out.print("update drone");
		this.movesLeft = getMovesLeft() - 1;
		System.out.println("moves left:");
		System.out.println(this.movesLeft);

		this.power = getPower() - 1.25;
		System.out.println("power: ");
		System.out.println(this.power);
		movesHistory.add(convertToPoint(this.currentPos));
		directionHistory.add(d);
		powerHistory.add(this.power);
		coinsHistory.add(this.coins);
		movesLeftHistory.add(this.movesLeft); //NEED REMOVED when finished
	}

	// move drone to direction of best charging station and charges drone
	protected void moveDrone(Direction d, ChargingStation c) throws IOException {
		Double total_power = this.power + c.getPower();
		setPower(total_power);
		Double total_coins = this.coins + c.getCoins();
		setCoins( total_coins);
		System.out.println("coins:");
		System.out.println(this.coins);
		updateDrone(d);
	}
	
	// move drone randomly given random direction
	protected void moveDroneRandomly(Direction d) {
		System.out.println("coins: ");
		System.out.println(this.coins);
		updateDrone(d);
	}
	
	// find the stations that are within range from the input list(good/bad stations)
	protected HashMap<Direction, ChargingStation> findStationsInRange(List<ChargingStation> list, Direction d) {
		HashMap<Direction, ChargingStation> directionCharging = new HashMap<Direction, ChargingStation>();
		Position direction_pos = this.currentPos.nextPosition(d);
		Point direction_point = convertToPoint(direction_pos);
		int count = 0;
		for (ChargingStation f : list) {
			Point station_point = convertToPoint(list.get(count).pos);
			count++;
			if (getRange(direction_point, station_point) < 0.00025) {
				directionCharging.put(d, f);
			}
		}
		return directionCharging;
	}
	
	// gets the lighthouse stations
	protected List<ChargingStation> goodStations() {
		List<ChargingStation> goodStations_List = new ArrayList<ChargingStation>();
		for (ChargingStation f : Stations) {
			if (f.getMarker().contains("lighthouse")) {
				goodStations_List.add(f);
			}
		}
		return goodStations_List;
	}
	
	// gets the danger stations
	protected List<ChargingStation> badStations() {
		List<ChargingStation> badStations_List = new ArrayList<ChargingStation>();
		for (ChargingStation f : Stations) {
			if (f.getMarker().contains("danger")) {
				badStations_List.add(f);
			}
		}
		return badStations_List;
	}

	// convert Position class to Point Class
	protected Point convertToPoint(Position pos) {
		return (Point) Point.fromLngLat(pos.longitude, pos.latitude);
	}
	
	// gets the distance between station and direction movement
	protected Double getRange(Point direction_point, Point station) {
		Double x, y;
		x = station.latitude() - direction_point.latitude();
		y = station.longitude() - direction_point.longitude();
		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}
	
	// generates random direction for movement
	protected Direction getRandomDirection() {
		int i = this.random.nextInt(16);
		return Arrays.asList(Direction.values()).get(i);
	}

	// finishes if no moves left or not enough power
	public boolean isFinished() {
		return (this.movesLeft == 0) || (this.power < 1.25);
	}
	
	// structure for writing to textfile
	public String totxt() {
		System.out.print(movesHistory.size());
		String text = "";
		for (int i = 0; i <= this.movesHistory.size() - 2; i++) {
			String lat1 = Double.toString(this.movesHistory.get(i).latitude());
			String lon1 = Double.toString(this.movesHistory.get(i).longitude());
			String dir = this.directionHistory.get(i).toString();
			String lat2 = Double.toString(this.movesHistory.get(i + 1).latitude());
			String lon2 = Double.toString(this.movesHistory.get(i + 1).longitude());
			String coins = this.coinsHistory.get(i).toString();
			String power = this.powerHistory.get(i).toString();
			text += lat1 + "," + lon1 + "," + dir + "," + lat2 + "," + lon2 + "," + coins + "," + power +"\n";
		}
		int moves = movesHistory.size()-1;
		String lat1 = Double.toString(this.movesHistory.get(moves).latitude());
		String lon1 = Double.toString(this.movesHistory.get(moves).longitude());
		String dir = this.directionHistory.get(moves).toString();
		String lat2 = Double.toString(this.currentPos.latitude);
		String lon2 = Double.toString(this.currentPos.longitude);
		String coins = this.coinsHistory.get(moves).toString();
		String power = this.powerHistory.get(moves).toString();
		text += lat1 + "," + lon1 + "," + dir + "," + lat2 + "," + lon2 + "," + coins + "," + power;

		return text;
	}

}
