package uk.ac.ed.inf.powergrab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.mapbox.geojson.Point;

abstract public class Drone {
	protected Position currentPos;
	protected Integer movesLeft;
	protected Double coins;
	protected Double power;
	protected Integer seed;
	protected List<ChargingStation> Stations;
	protected Random random;

	protected ArrayList<Point> movesHistory = new ArrayList<Point>();
	protected ArrayList<Double> coinsHistory = new ArrayList<Double>();
	protected ArrayList<Double> powerHistory = new ArrayList<Double>();
	protected ArrayList<Direction> directionHistory = new ArrayList<Direction>();

	// constructors
	public Drone(Position currentPos, Integer seed, List<ChargingStation> Stations) {
		this.currentPos = currentPos;
		this.movesLeft = 250;
		this.coins = 0.0;
		this.power = 250.0;
		this.seed = seed;
		this.Stations = Stations;
		this.random = new Random(seed);
	}

	// getters
	protected Position getCurrentPos() {
		return currentPos;
	}

	protected Integer getMovesLeft() {
		return this.movesLeft;
	}

	protected double getCoins() {
		return this.coins;
	}

	protected double getPower() {
		return this.power;
	}

	protected Integer getSeed() {
		return this.seed;
	}

	protected List<ChargingStation> getStations() {
		return this.Stations;
	}

	protected ArrayList<Point> getMovesHistory() {
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

	/**
	 * Checks if th drone is within the play area.
	 * 
	 * @param pos the position of the drone's move
	 * @return true if within play area, else false.
	 */
	protected boolean inPlayArea(Position pos) {
		return (pos.inPlayArea());
	}

	/**
	 * Find the charging station with the maximum amount of coins and returns the
	 * direction to get to that charging station
	 * 
	 * @param goodDirection         an arraylist of direction that are good
	 * @param goodDirectionCharging a hashmap wtih the directions and their charging
	 *                              stations that are good
	 * @return maxDir the best direction to take to get to that charging station
	 *         with max coins
	 */
	protected Direction findMaxCoins(ArrayList<Direction> goodDirection,
			HashMap<Direction, ChargingStation> goodDirectionCharging) {
		Double maxCoins = (double) Integer.MAX_VALUE;

		Direction maxDir = goodDirection.get(0);
		for (Direction d : goodDirection) {
			ChargingStation cs = goodDirectionCharging.get(d);
			if (cs.getCoins() > maxCoins) {
				maxCoins = cs.getCoins();
				maxDir = d;
			}
		}
		return maxDir;
	}

	/**
	 * Get a random direction that is absent from the Direction from the input
	 * hashmap (bad directions). This helps to pick directions to avoid the
	 * negatively charged station.
	 * 
	 * @param badDirection an arraylist of direction that are bad
	 * @return randomDir the best direction to take to get to that charging station
	 *         with max coins
	 */
	protected Direction randDirection(ArrayList<Direction> badDirection) {
		ArrayList<Direction> notBadDir = avoidBadDirection(badDirection);
		int num = this.random.nextInt(notBadDir.size());
		Direction randomDir = notBadDir.get(num);
		return randomDir;
	}

	/**
	 * Generates a list of possible directions the drone can take, in which its next
	 * position will not be within the range negatively charged stations.
	 * 
	 * @param badDirection an arraylist of direction that are bad
	 * @return notBadDirections an arraylist of direction absent from given list
	 */
	// gets a random direction that is absent from the Directions from input HashMap
	protected ArrayList<Direction> avoidBadDirection(ArrayList<Direction> badDirection) {
		ArrayList<Direction> notBadDirections = new ArrayList<Direction>();
		System.out.println(badDirection.size());
		for (Direction d : Direction.values()) {
			if (!badDirection.contains(d)) {
				notBadDirections.add(d);
			}
		}
		return notBadDirections;
	}

	/**
	 * Returns a random direction for the drone and avoids the bad directions. It
	 * also ensures that the new position is within the range.
	 *
	 * @param badDirections an arraylist of directions with bad stations
	 * @return the direction to move in
	 */
	protected Direction avoidBadStations(ArrayList<Direction> badDirections) {
		System.out.println("find random station from not directions of BadDirectionCharging");
		Direction randomDir = randDirection(badDirections);
		Position newPos = this.currentPos.nextPosition(randomDir);

		while (!newPos.inPlayArea()) {
			randomDir = randDirection(badDirections);
			newPos = this.currentPos.nextPosition(randomDir);
		}
		return randomDir;
	}

	/**
	 * Updates the power and coins of the charging station
	 * 
	 * @param goal a charging station
	 */
	protected void updateStation(ChargingStation goal) {
		for (ChargingStation CS : Stations) {
			if (CS.pos.equals(goal.pos)) {
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
					CS.setPower(0.0);
					System.out.println("power: ");
					System.out.println(CS.getPower());
				} else {

					System.out.println("power: ");
					System.out.println(CS.getPower());
					// CS.setPower(CS.power-this.power);
					CS.setPower(0.0);

					System.out.println("power: ");
					System.out.println(CS.getPower());

				}
			}
		}

	}

	/**
	 * Decrement the number of moves the drone has left, and reduces the drone's
	 * power, and adds current moves, direction, power and coins to their relevant
	 * arraylist to keep track of the drone's movements.
	 *
	 * @param d a direction moved in
	 */
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
	}

	/**
	 * Moves the drone in a certain direction to achieve charging from the nearby
	 * charging station.
	 *
	 * @param d the direction taken to get nearby the charging station
	 * @param c a charging station the drone will charge from
	 */
	protected void moveDrone(Direction d, ChargingStation c) throws IOException {
		Double total_power = this.power + c.getPower();
		setPower(total_power);
		Double total_coins = this.coins + c.getCoins();
		setCoins(total_coins);
		System.out.println("coins:");
		System.out.println(this.coins);
		updateDrone(d);
	}

	/**
	 * Moves the drone in a certain given random direction.
	 *
	 * @param d the random generated direction for the drone to move in
	 */
	protected void moveDroneRandomly(Direction d) {
		System.out.println("coins: ");
		System.out.println(this.coins);
		updateDrone(d);
	}

	/**
	 * Returns a hashmap of the directions and charging stations that are within the
	 * charging range.
	 * 
	 * @param list a list of charging stations either positive charging or negative
	 *             charging
	 * @param d    the direction taken to get nearby the charging station
	 * @return directionCharging a hashmap of the directions and charging station
	 *         within range
	 */
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

	/**
	 * Returns a list of charging station of the charging stations that are
	 * positively charging, identified by the 'lighthouse' marker.
	 * 
	 * @return goodStations_List a list of positively charged stations
	 */
	protected List<ChargingStation> goodStations() {
		List<ChargingStation> goodStations_List = new ArrayList<ChargingStation>();
		for (ChargingStation f : Stations) {
			if (f.getMarker().contains("lighthouse")) {
				goodStations_List.add(f);
			}
		}
		return goodStations_List;
	}

	/**
	 * Returns a list of charging station of the charging stations that are
	 * positively charging, identified by the 'danger' marker.
	 * 
	 * @return badStations_List a list of negatively charged stations
	 */
	protected List<ChargingStation> badStations() {
		List<ChargingStation> badStations_List = new ArrayList<ChargingStation>();
		for (ChargingStation f : Stations) {
			if (f.getMarker().contains("danger")) {
				badStations_List.add(f);
			}
		}
		return badStations_List;
	}

	/**
	 * Converts the given position into a Point
	 * 
	 * @param pos a position from the Position class
	 * @return point
	 */
	protected Point convertToPoint(Position pos) {
		return (Point) Point.fromLngLat(pos.longitude, pos.latitude);
	}

	/**
	 * Returns the distance between the given points
	 * 
	 * @param direction_point a a given point
	 * @param station         a given point
	 * @return goodStations_List a list of positively charged stations
	 */
	protected Double getRange(Point direction_point, Point station_point) {
		Double x = station_point.latitude() - direction_point.latitude();
		Double y = station_point.longitude() - direction_point.longitude();
		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}

	/**
	 * Returns a random generate direction from the 16 possible directions
	 * 
	 * @return a direction
	 */
	protected Direction getRandomDirection() {
		int i = this.random.nextInt(16);
		return Arrays.asList(Direction.values()).get(i);
	}

	/**
	 * Returns a boolean if condition is met. True, if the no moves left and power
	 * is less than 1.25
	 * 
	 * @return true if finished, false if not finished
	 */
	public boolean isFinished() {
		return (this.movesLeft == 0) || (this.power < 1.25);
	}

	/**
	 * Prepares string to write to file with the drone's moves, directions taken,
	 * coins and power collected in the format of
	 * "lat1,lon2,dir,lat2,lon2,coins,power" for all the moves made.
	 * 
	 * @return a string of concatenated history of drone's move
	 */
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
			text += lat1 + "," + lon1 + "," + dir + "," + lat2 + "," + lon2 + "," + coins + "," + power + "\n";
		}
		int moves = movesHistory.size() - 1;
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
