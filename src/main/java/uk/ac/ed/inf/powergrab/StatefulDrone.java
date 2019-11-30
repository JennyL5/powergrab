package uk.ac.ed.inf.powergrab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StatefulDrone extends Drone {
	public StatefulDrone(Position currentPos, Integer seed, List<ChargingStation> Stations) throws IOException {
		super(currentPos, seed, Stations);
	}

	/**
	 * Starts and ends the game. Created goodStations, badStations, and visitLater
	 * lists.
	 * 
	 */
	protected void startGame() throws IOException {

		int c = 0;
		ArrayList<ChargingStation> goodStations = (ArrayList<ChargingStation>) goodStations();
		ArrayList<ChargingStation> badStations = (ArrayList<ChargingStation>) badStations();
		ArrayList<ChargingStation> visitLater = new ArrayList<ChargingStation>();

		while (!isFinished()) {
			c++;
			System.out.println("Round ");
			System.out.println(c);
			System.out.println(this.currentPos.latitude);
			System.out.println(this.currentPos.longitude);
			strategy(goodStations, badStations, visitLater);
		}
	}

	/**
	 * This function is run to find the strategy to determine how the drone should
	 * move. Created a hashmap for storing goodStations and badStations nearby, to
	 * find their distances and sort them in an ascending order to find minimum
	 * distance. The drone will try get to the closest charging station, and will
	 * charge if in range, else either move closer or change goal or move randomly.
	 * 
	 * @param goodStations a list of positively charged charging station
	 * @param baddStations a list of negatively charged charging station
	 * @param visitLater   a list of charging stations to be visited later due to it
	 *                     being difficult to get
	 */
	private void strategy(ArrayList<ChargingStation> goodStations, ArrayList<ChargingStation> badStations,
			ArrayList<ChargingStation> visitLater) throws IOException {

		HashMap<ChargingStation, Double> distanceOfGoodStations = new HashMap<ChargingStation, Double>();
		HashMap<Direction, ChargingStation> goodStationsInRange = new HashMap<Direction, ChargingStation>();
		HashMap<Direction, ChargingStation> badStationsInRange = new HashMap<Direction, ChargingStation>();

		for (Direction d : Direction.values()) {
			HashMap<Direction, ChargingStation> goodStationsNearby = (findStationsInRange(goodStations, d));
			goodStationsInRange.putAll(goodStationsNearby);
			HashMap<Direction, ChargingStation> badStationsNearby = (findStationsInRange(badStations, d));
			badStationsInRange.putAll(badStationsNearby);
		}

		ArrayList<Direction> badDirectionsInRange = new ArrayList<Direction>(badStationsInRange.keySet());

		System.out.println("Good stations");
		System.out.println(goodStations.size());

		distanceOfGoodStations = getSortedDistances(distanceOfGoodStations, goodStations);

		System.out.print(visitLater.size());

		if (distanceOfGoodStations.size() == 0) {
			checkVisitLater(goodStations, badDirectionsInRange, distanceOfGoodStations, badStationsInRange,
					goodStationsInRange, visitLater);

		} else {

			ChargingStation goal = (ChargingStation) distanceOfGoodStations.keySet().toArray()[0];
			Direction minDir = findMinDirection(badStationsInRange, goal);

			System.out.println("move the drone");
			Position newPos = this.currentPos.nextPosition(minDir);
			if (newPos.inPlayArea()) {
				System.out.println("approach station");
				approachStation(newPos, goal, minDir, goodStations, badDirectionsInRange, distanceOfGoodStations,
						badStationsInRange, visitLater);
			} else {
				if (distanceOfGoodStations.size() > 2 && distanceOfGoodStations.keySet().size() > 2) {
					System.out.println("TRAPPED: (temp) remove original goal, set new goal");
					changeGoal(goodStations, badDirectionsInRange, distanceOfGoodStations, badStationsInRange,
							visitLater);
				} else {
					System.out.print("no new goal move random");
					Direction randomDir = avoidBadStations(badDirectionsInRange);
					newPos = this.currentPos.nextPosition(randomDir);
					moveDroneRandomly(randomDir);
					setCurrentPos(newPos);
				}
			}
		}
	}

	/**
	 * This function checks if the drone can move to the dismissed charging stations
	 * (added to checkVisitLater) and tries to charge from them, by sorting the
	 * visitLater list distances in an ascending order to find minimum distance. The
	 * drone will try get to the closest charging station, and will charge if in
	 * range, else either move closer or change goal or move randomly.
	 * 
	 * @param goodStations           a list of positively charged charging station
	 * @param badDirectionsInRange   a list of directions for positively charged
	 *                               charging station
	 * @param distanceOfGoodStations a hashmap of positively charged charging
	 *                               station with their distances
	 * @param badStationsInRange     a hashmap of negatively charged charging
	 *                               station within range
	 * @param goodStationsInRange    a hashmap of positively charged charging
	 *                               station within range
	 * @param visitLater             a list of charging stations to be visited later
	 *                               due to it being difficult to get
	 */
	private void checkVisitLater(ArrayList<ChargingStation> goodStations, ArrayList<Direction> badDirectionsInRange,
			HashMap<ChargingStation, Double> distanceOfGoodStations,
			HashMap<Direction, ChargingStation> badStationsInRange,
			HashMap<Direction, ChargingStation> goodStationsInRange, ArrayList<ChargingStation> visitLater)
			throws IOException {
		if (visitLater.size() != 0) {
			System.out.println("no more good stations to go to so check visitLater");
			for (Direction d : Direction.values()) {
				HashMap<Direction, ChargingStation> goodStationsNearby = (findStationsInRange(visitLater, d));
				goodStationsInRange.putAll(goodStationsNearby);
			}
			distanceOfGoodStations = getSortedDistances(distanceOfGoodStations, visitLater);
			ChargingStation goal = (ChargingStation) distanceOfGoodStations.keySet().toArray()[0];
			Direction minDir = findMinDirection(badStationsInRange, goal);
			System.out.println("move the drone");
			Position newPos = this.currentPos.nextPosition(minDir);
			if (newPos.inPlayArea()) {
				System.out.println("approaching station");
				if (getRange(convertToPoint(newPos), convertToPoint(goal.pos)) < 0.00025) {
					visitLater.remove(goal);
					System.out.println("Collect from station");
					moveDrone(minDir, goal);
					updateStation(goal);
					setCurrentPos(newPos);
				} else {

					System.out.println("not within range, move closer to goal charging station");
					try {
						minDir = getOutOfLoop(minDir, newPos, goal, badDirectionsInRange, visitLater, goodStations);
						// changeGoal(goodStations, badDirectionsInRange, distanceOfGoodStations,
						// badStationsInRange, visitLater);

						newPos = this.currentPos.nextPosition(minDir);
					} catch (Exception e) {
						System.out.println(e);
					}

					moveDroneRandomly(minDir);
					setCurrentPos(newPos);
				}
			} else {
				System.out.println("outside area, remove original goal, set new goal");
				changeGoal(goodStations, badDirectionsInRange, distanceOfGoodStations, badStationsInRange, visitLater);
			}
		} else {
			System.out.println("no more good stations to go to");
			Direction randomDir = avoidBadStations(badDirectionsInRange);
			Position newPos = this.currentPos.nextPosition(randomDir);
			moveDroneRandomly(randomDir);
			setCurrentPos(newPos);
		}
	}

	/**
	 * This function tries to get closer to the charging station or to get to goal
	 * charging station to charge. It checks if the charging station is within range
	 * to charge, if so, it will move to charge from the charging station and
	 * removes it from the list of goodStations and sets its new position. Otherwise
	 * it will try and get out a loop, an move to a random direction that is not
	 * near a negatively charged station.
	 * 
	 * @param newPos                 a new position when moved in direction
	 * @param goal                   a charging station the drone is trying to get
	 *                               to
	 * @param minDir                 a direction the drone is taking with minimum
	 *                               distance to nearest charging station
	 * @param goodStations           a list of positively charged charging station
	 * @param badDirectionsInRange   a list of directions for positively charged
	 *                               charging station
	 * @param distanceOfGoodStations a hashmap of positively charged charging
	 *                               station with their distances
	 * @param badStationsInRange     a hashmap of negatively charged charging
	 *                               station within range
	 * @param visitLater             a list of charging stations to be visited later
	 *                               due to it being difficult to get
	 */
	private void approachStation(Position newPos, ChargingStation goal, Direction minDir,
			ArrayList<ChargingStation> goodStations, ArrayList<Direction> badDirectionsInRange,
			HashMap<ChargingStation, Double> distanceOfGoodStations,
			HashMap<Direction, ChargingStation> badStationsInRange, ArrayList<ChargingStation> visitLater)
			throws IOException {
		if (getRange(convertToPoint(newPos), convertToPoint(goal.pos)) < 0.00025) {
			System.out.println("Collect from station");
			moveDrone(minDir, goal);
			updateStation(goal);
			setCurrentPos(newPos);
			goodStations.remove(goal);
		} else {
			System.out.println("not within range, move closer to goal charging station");
			int len = movesHistory.size();
			if (len > 5 && directionHistory.get(len - 2).equals(minDir)
					&& movesHistory.get(len - 2).equals(movesHistory.get(len - 4))) {
				minDir = getOutOfLoop(minDir, newPos, goal, badDirectionsInRange, visitLater, goodStations);
				// changeGoal(goodStations, badDirectionsInRange, distanceOfGoodStations,
				// badStationsInRange, visitLater);

				newPos = this.currentPos.nextPosition(minDir);

			}
			moveDroneRandomly(minDir);
			setCurrentPos(newPos);
		}
	}

	/**
	 * This function is called when the next position of move is out of play area
	 * and will set a new charging station as goal .
	 * 
	 * @param goodStations           a list of positively charged charging station
	 * @param badDirectionsInRange   a list of directions for positively charged
	 *                               charging station
	 * @param distanceOfGoodStations a hashmap of positively charged charging
	 *                               station with their distances
	 * @param badStationsInRange     a hashmap of negatively charged charging
	 *                               station within range
	 * @param visitLater             a list of charging stations to be visited later
	 *                               due to it being difficult to get
	 */
	private void changeGoal(ArrayList<ChargingStation> goodStations, ArrayList<Direction> badDirectionsInRange,
			HashMap<ChargingStation, Double> distanceOfGoodStations,
			HashMap<Direction, ChargingStation> badStationsInRange, ArrayList<ChargingStation> visitLater)
			throws IOException {
		System.out.println(distanceOfGoodStations.keySet().size());

		distanceOfGoodStations = getSortedDistances(distanceOfGoodStations, visitLater);
		ChargingStation goal1 = (ChargingStation) distanceOfGoodStations.keySet().toArray()[0];

		try {

			// if (distanceOfGoodStations.size()>2) {
			distanceOfGoodStations = getSortedDistances(distanceOfGoodStations, goodStations);
			goal1 = (ChargingStation) distanceOfGoodStations.keySet().toArray()[1];
		} catch (Exception e) {
			System.out.print(e);
		}

		Direction minDir1 = findMinDirection(badStationsInRange, goal1);
		if (minDir1.equals(null)) {
			System.out.println("No direction is found for min distance");
			Direction minDir = avoidBadStations(badDirectionsInRange);
			Position newPos = this.currentPos.nextPosition(minDir);
			moveDroneRandomly(minDir);
			setCurrentPos(newPos);
		} else {
			Position newPos1 = this.currentPos.nextPosition(minDir1);
			if (newPos1.inPlayArea()) {

				System.out.print("check if new goal station is within range");
				approachStation(newPos1, goal1, minDir1, goodStations, badDirectionsInRange, distanceOfGoodStations,
						badStationsInRange, visitLater);

			} else {
				System.out.print("ERROR: stuuuuuuuck");
				Direction minDir = avoidBadStations(badDirectionsInRange);
				Position newPos = this.currentPos.nextPosition(minDir);
				moveDroneRandomly(minDir);
				setCurrentPos(newPos);

			}
		}
	}

	/**
	 * This method return a direction to try get out of a dead end, where the drone
	 * goes back and forth. It looks for repeated positions/directions in the
	 * history arraylist already made by drone.
	 * 
	 * @param minDir             a direction the drone is taking with minimum
	 *                           distance to nearest charging station
	 * @param newPos             a new position when moved in direction
	 * @param goal               a charging station the drone is trying to get to
	 * @param badStationsInRange a hashmap of negatively charged charging station
	 *                           within range
	 * @param visitLater         a list of charging stations to be visited later due
	 *                           to it being difficult to get
	 * @param goodStations       a list of positively charged charging station
	 * @return minDir a direction to get out loop
	 */
	private Direction getOutOfLoop(Direction minDir, Position newPos, ChargingStation goal,
			ArrayList<Direction> badDirectionsInRange, ArrayList<ChargingStation> visitLater,
			ArrayList<ChargingStation> goodStations) {

		int len = directionHistory.size();
		if (directionHistory.get(len - 2).equals(minDir)
				&& movesHistory.get(len - 2).equals(movesHistory.get(len - 4))) {
			visitLater.add(goal);
			goodStations.remove(goal);
			System.out.print("If goal station on/near bad station!!!!");
			// maybe change goal (in approach & visitlater)
			// changeGoal(goodStations, badDirectionsInRange, distanceOfGoodStations,
			// badStationsInRange, visitLater);

			minDir = avoidBadStations(badDirectionsInRange);
			newPos = this.currentPos.nextPosition(minDir);
			while (!newPos.inPlayArea()) {
				minDir = avoidBadStations(badDirectionsInRange);
				newPos = this.currentPos.nextPosition(minDir);
			}
		}

		return minDir;

	}

	/**
	 * This method get the direction with the closest distance to goal charging
	 * station.
	 * 
	 * @param badStationsInRange a hashmap of negatively charged charging station
	 *                           within range
	 * @param goal               a charging station the drone is trying to get to
	 * @return minDir the directin with the smallest range from the goal charging
	 *         station
	 */
	private Direction findMinDirection(HashMap<Direction, ChargingStation> badStationsInRange, ChargingStation goal) {
		ArrayList<Direction> badDirectionsInRange = new ArrayList<Direction>(badStationsInRange.keySet());
		ArrayList<Direction> avoidBadDirections = avoidBadDirection(badDirectionsInRange);
		Direction minDir = null;
		double distance = Double.MAX_VALUE;

		for (Direction d : avoidBadDirections) {
			Double dist = getRange(convertToPoint(this.currentPos.nextPosition(d)), convertToPoint(goal.pos));
			if (dist < distance) {
				System.out.print(d);
				System.out.print("+");

				distance = dist;
				minDir = d;
			}

		}
		return minDir;
	}

	/**
	 * This method gets and sorts distance of nearby good/bad stations in ascending
	 * order.
	 * 
	 * @param distanceOfStations a hashmap of positively/negatively charged charging
	 *                           station with their distances
	 * @param theStations        a list of positively/negatively charged charging
	 *                           station
	 * @return temp a hashmap of sorted charging stations and their distances.
	 */
	private HashMap<ChargingStation, Double> getSortedDistances(HashMap<ChargingStation, Double> distanceOfStations,
			ArrayList<ChargingStation> theStations) {
		for (ChargingStation a : theStations) {
			Double distance = getRange(convertToPoint(this.currentPos), convertToPoint(a.pos));
			distanceOfStations.put(a, distance);
		}

		List<Map.Entry<ChargingStation, Double>> list = new LinkedList<Map.Entry<ChargingStation, Double>>(
				distanceOfStations.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<ChargingStation, Double>>() {
			public int compare(Map.Entry<ChargingStation, Double> o1, Map.Entry<ChargingStation, Double> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		HashMap<ChargingStation, Double> temp = new LinkedHashMap<ChargingStation, Double>();
		for (Map.Entry<ChargingStation, Double> aa : list) {
			temp.put(aa.getKey(), aa.getValue());
		}
		return temp;
	}

}
