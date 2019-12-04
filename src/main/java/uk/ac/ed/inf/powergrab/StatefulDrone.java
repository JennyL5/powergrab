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

/**
 * This class represents the Stateful drone, it inherits from its parents class
 * (Drone), and also has methods which is unique to the stateful drone class,
 * and cannot be accessed from the stateless drone class.
 * 
 * @author Jenny
 *
 */
public class StatefulDrone extends Drone {
	/**
	 * Represents StatelefulDrone inherits from Drone
	 * 
	 * @param currentPos
	 * @param seed
	 * @param Stations
	 * @throws IOException
	 */
	public StatefulDrone(Position currentPos, Integer seed, List<ChargingStation> Stations) throws IOException {
		super(currentPos, seed, Stations);
	}

	/**
	 * Starts and ends the game. Creates goodStations, badStations, and visitLater
	 * lists.
	 * 
	 * @throws IOException
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
	 * Finds the strategy to determine how the drone should move. Creates a hashmap
	 * for storing goodStations and badStations nearby, to find their distances and
	 * sort them in an ascending order to find minimum distance. The drone will try
	 * get to the closest charging station, and will charge if in range, else either
	 * move closer or change goal or move randomly. It ensures that the best
	 * position to move in will not be in a direction of a negative charging
	 * station.
	 * 
	 * @param goodStations a list of positively charged charging station
	 * @param badStations  a list of negatively charged charging station
	 * @param visitLater   a list of charging stations to be visited later due to it
	 *                     being difficult to get
	 * @throws IOException
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
		ArrayList<Direction> avoidBadDirections = avoidBadDirection(badDirectionsInRange);

		System.out.println("Good stations");
		System.out.println(goodStations.size());
		System.out.println("visit later");
		System.out.println(visitLater.size());

		distanceOfGoodStations = getSortedDistances(distanceOfGoodStations, goodStations);

		if (distanceOfGoodStations.size() == 0) {
			checkVisitLater(goodStations, badDirectionsInRange, distanceOfGoodStations, badStationsInRange,
					goodStationsInRange, visitLater);
		} else {
			ChargingStation goal = (ChargingStation) distanceOfGoodStations.keySet().toArray()[0];
			Direction minDir = findMinDirection(badStationsInRange, goal);

			System.out.println("move the drone");
			Position newPos = this.currentPos.nextPosition(minDir);

			if (newPos.inPlayArea() && avoidBadDirections.contains(minDir) && goal.coins > 0) {
				System.out.println("approach station");
				approachStation(newPos, goal, minDir, goodStations, badDirectionsInRange, distanceOfGoodStations,
						badStationsInRange, visitLater);
			} else if ((isTrapped() && availableGoodStations(goodStations, visitLater))) {
				goodStations.remove(goal);
				visitLater.add(goal);
				changeGoal(goodStations, badDirectionsInRange, distanceOfGoodStations, badStationsInRange, visitLater);
			} else if (badDirectionsInRange.size() == 16) {
				allDirectionsBad(badDirectionsInRange, badStationsInRange, goodStationsInRange);
			} else {
				System.out.print("no new goal move random");
				Direction randomDir = avoidBadStations(badDirectionsInRange);
				newPos = this.currentPos.nextPosition(randomDir);
				moveDroneRandomly(randomDir);
				setCurrentPos(newPos);
			}
		}
	}

	/**
	 * Checks if the drone can move to the dismissed charging stations (added to
	 * checkVisitLater) and tries to charge from them, by sorting the visitLater
	 * list distances in an ascending order to find minimum distance. The drone will
	 * try get to the closest charging station, and will charge if in range, else
	 * either move closer or change goal or move randomly. It ensures that the best
	 * position to move in will not be in a direction of a negative charging
	 * station.
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
		ArrayList<Direction> avoidBadDirections = avoidBadDirection(badDirectionsInRange);

		if (visitLater.size() != 0) {
			System.out.println("no more good stations to go, to so check visitLater");

			for (Direction d : Direction.values()) {
				HashMap<Direction, ChargingStation> goodStationsNearby = (findStationsInRange(visitLater, d));
				goodStationsInRange.putAll(goodStationsNearby);
			}

			ChargingStation goal = null;
			if (visitLater.size() == 1) {
				goal = visitLater.get(0);
			} else {
				distanceOfGoodStations = getSortedDistances(distanceOfGoodStations, visitLater);
				goal = (ChargingStation) distanceOfGoodStations.keySet().toArray()[0];
			}

			Direction minDir = findMinDirection(badStationsInRange, goal);

			Position newPos = this.currentPos.nextPosition(minDir);

			if (newPos.inPlayArea() && avoidBadDirections.contains(minDir) && goal.coins > 0) {
				System.out.println("approaching station");
				if (getRange(convertToPoint(newPos), convertToPoint(goal.pos)) < 0.00025) {
					visitLater.remove(goal);
					System.out.println("Collect from station");
					moveDrone(minDir, goal);
					updateStation(goal);
					setCurrentPos(newPos);
				} else if (isTrapped()) {
					changeGoal(goodStations, badDirectionsInRange, distanceOfGoodStations, badStationsInRange,
							visitLater);
				} else {
					System.out.print("move closer to goal");
					newPos = this.currentPos.nextPosition(minDir);
					moveDroneRandomly(minDir);
					setCurrentPos(newPos);
				}
			} else {
				System.out.println("outside area, check if trapped, remove original goal, set new goal");
				if ((isTrapped())) {
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

		} else {

			System.out.print("no new goal move random");
			Direction randomDir = avoidBadStations(badDirectionsInRange);
			Position newPos = this.currentPos.nextPosition(randomDir);
			moveDroneRandomly(randomDir);
			setCurrentPos(newPos);
		}
	}

	/**
	 * Tries to get closer to the charging station or to get to goal charging
	 * station to charge. It checks if the charging station is within range to
	 * charge, if so, it will move to charge from the charging station and removes
	 * it from the list of goodStations and sets its new position. Otherwise if it
	 * is trapped (repeating same position) then change charging station goal, or
	 * move closer if valid, or move randomly.
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
		System.out.println("movesHistory");
		System.out.println(movesHistory.size());
		if (getRange(convertToPoint(newPos), convertToPoint(goal.pos)) < 0.00025) {
			System.out.println("Collect from station");
			moveDrone(minDir, goal);
			updateStation(goal);
			setCurrentPos(newPos);
			goodStations.remove(goal);
		} else if ((isTrapped() && availableGoodStations(goodStations, visitLater))) {
			goodStations.remove(goal);
			visitLater.add(goal);
			changeGoal(goodStations, badDirectionsInRange, distanceOfGoodStations, badStationsInRange, visitLater);
		} else {
			System.out.println("move closer to goal station");
			moveDroneRandomly(minDir);
			setCurrentPos(newPos);
		}
	}

	/**
	 * 
	 * This function is called when the next position of move is out of play area or
	 * if the drone is trapped and will set a new charging station as goal using the
	 * goodStations first then checking visitLater list. It ensures that the best
	 * position to move in will not be in a direction of a negative charging
	 * station.
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
	 * @throws IOException
	 */
	private void changeGoal(ArrayList<ChargingStation> goodStations, ArrayList<Direction> badDirectionsInRange,
			HashMap<ChargingStation, Double> distanceOfGoodStations,
			HashMap<Direction, ChargingStation> badStationsInRange, ArrayList<ChargingStation> visitLater)
			throws IOException {
		ArrayList<Direction> avoidBadDirections = avoidBadDirection(badDirectionsInRange);

		distanceOfGoodStations.clear();
		ChargingStation goal = null;

		if (!goodStations.isEmpty()) {
			System.out.print("good stations");
			if (goodStations.size() == 1) {
				goal = goodStations.get(0);
			} else {
				distanceOfGoodStations = getSortedDistances(distanceOfGoodStations, goodStations);
				goal = (ChargingStation) distanceOfGoodStations.keySet().toArray()[0];
			}

			Direction minDir = findMinDirection(badStationsInRange, goal);

			Position newPos1 = this.currentPos.nextPosition(minDir);

			if (newPos1.inPlayArea() && avoidBadDirections.contains(minDir) && goal.coins > 0) {

				if (getRange(convertToPoint(newPos1), convertToPoint(goal.pos)) < 0.00025) {
					System.out.println("Collect from station");
					moveDrone(minDir, goal);
					updateStation(goal);
					setCurrentPos(newPos1);
					goodStations.remove(goal);
				} else if (isTrapped()) {
					System.out.print("Move randomly but avoid bad stations");
					minDir = avoidBadStations(badDirectionsInRange);
					Position newPos = this.currentPos.nextPosition(minDir);
					moveDroneRandomly(minDir);
					setCurrentPos(newPos);

				} else {
					System.out.println("move closer to goal station");
					moveDroneRandomly(minDir);
					setCurrentPos(newPos1);
				}

			} else {
				System.out.print("Move randomly but avoid bad stations");
				minDir = avoidBadStations(badDirectionsInRange);
				Position newPos = this.currentPos.nextPosition(minDir);
				moveDroneRandomly(minDir);
				setCurrentPos(newPos);
			}

		} else {
			if (visitLater.size() == 1) {

				goal = visitLater.get(0);
			} else {
				distanceOfGoodStations = getSortedDistances(distanceOfGoodStations, visitLater);
				goal = (ChargingStation) distanceOfGoodStations.keySet().toArray()[0];

			}

			Direction minDir = findMinDirection(badStationsInRange, goal);

			Position newPos1 = this.currentPos.nextPosition(minDir);

			if (newPos1.inPlayArea() && avoidBadDirections.contains(minDir) && goal.coins > 0) {

				if (getRange(convertToPoint(newPos1), convertToPoint(goal.pos)) < 0.00025) {
					System.out.println("Collect from station");
					moveDrone(minDir, goal);
					updateStation(goal);
					setCurrentPos(newPos1);
					visitLater.remove(goal);

				} else if (isTrapped()) {
					System.out.print("Move randomly but avoid bad stations");
					minDir = avoidBadStations(badDirectionsInRange);
					Position newPos = this.currentPos.nextPosition(minDir);
					moveDroneRandomly(minDir);
					setCurrentPos(newPos);

				} else {
					System.out.println("move closer to goal station");
					moveDroneRandomly(minDir);
					setCurrentPos(newPos1);
				}

			} else {
				System.out.print("Move randomly but avoid bad stations");
				minDir = avoidBadStations(badDirectionsInRange);
				Position newPos = this.currentPos.nextPosition(minDir);
				moveDroneRandomly(minDir);
				setCurrentPos(newPos);

			}
		}

	}

	/**
	 * Checks if drone is ending up in the same position previously
	 * 
	 * @return boolean
	 */
	private boolean isTrapped() {
		return (movesHistory.size() > 5
				&& movesHistory.get(movesHistory.size() - 2).equals(movesHistory.get(movesHistory.size() - 4))
				&& movesHistory.get(movesHistory.size() - 1).equals(movesHistory.get(movesHistory.size() - 3)));
	}

	/**
	 * Checks if there are still any available good stations to charge from.
	 * 
	 * @param goodStations ArrayList<ChargingStation>goodStations
	 * @param visitLater   ArrayList<ChargingStation>goodStations
	 * @return boolean
	 */
	private boolean availableGoodStations(ArrayList<ChargingStation> goodStations,
			ArrayList<ChargingStation> visitLater) {
		return (visitLater.size() > 0 || goodStations.size() > 0);
	}

	/**
	 * Gets the direction with the closest distance to goal charging station.
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
	 * Gets and sorts distance of nearby good/bad stations in ascending order.
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
