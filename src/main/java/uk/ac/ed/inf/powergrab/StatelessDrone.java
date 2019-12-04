package uk.ac.ed.inf.powergrab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class represents the Stateless drone, it inherits from its parents class
 * (Drone), and also has methods which is unique to the stateless drone class,
 * and cannot be accessed from the stateful drone class.
 * 
 * @author Jenny
 *
 */
public class StatelessDrone extends Drone {

	/**
	 * Represents StatelessDrone inherits from Drone
	 * 
	 * @param currentPos Position
	 * @param seed       Integer
	 * @param stations   List<ChargingStation>
	 * @throws IOException
	 */
	public StatelessDrone(Position currentPos, Integer seed, List<ChargingStation> stations) throws IOException {
		super(currentPos, seed, stations);
	}

	/**
	 * Starts and ends the game.
	 * 
	 * @throws IOException
	 */
	protected void startGame() throws IOException {
		int c = 0;
		while (!isFinished()) {
			c++;
			System.out.println("Round ");
			System.out.println(c);
			System.out.println(this.currentPos.latitude);
			System.out.println(this.currentPos.longitude);
			strategy();
		}
	}

	/**
	 * Runs function if the drone's next move is outside the play area. This
	 * generates a random direction from the list of directions that a good, and
	 * moves towards that random direction generated. If there are no good
	 * directions the drone will need to go towards a bad direction. And sets the
	 * new position of the drone.
	 * 
	 * @param goodDirection an arraylist of direction that are positively charged
	 * @param badDirection  an arraylist of direction that are negatively charged
	 */
	private void outsideArea(ArrayList<Direction> goodDirections, ArrayList<Direction> badDirections) {
		System.out.print("Direction point outside play area");
		Direction randomDir = randDirection(goodDirections);
		Position newPos = this.currentPos.nextPosition(randomDir);
		int c = 0;
		while (!newPos.inPlayArea()) {
			c++;
			if (c < 16) {
				randomDir = randDirection(goodDirections);
				newPos = this.currentPos.nextPosition(randomDir);
			} else {
				while (!newPos.inPlayArea()) {
					System.out.print("Just go through");
					randomDir = randDirection(badDirections);
					newPos = this.currentPos.nextPosition(randomDir);
				}
			}
		}
		moveDroneRandomly(randomDir);
		setCurrentPos(newPos);
	}

	/**
	 * Controls the strategy of the drone. Finds the good and bad stations that are
	 * within range of the drone's current position, and gets their directions.
	 * Tries to find from the good charging station within range, the one with the
	 * maximum coins, and performs move if that move is within play area. Else it
	 * will pick a random direction to move in.
	 * 
	 */
	private void strategy() throws IOException {
		HashMap<Direction, ChargingStation> goodStationsInRange = new HashMap<Direction, ChargingStation>();
		HashMap<Direction, ChargingStation> badStationsInRange = new HashMap<Direction, ChargingStation>();
		for (Direction d : Direction.values()) {
			HashMap<Direction, ChargingStation> goodStationsNearby = (findStationsInRange(goodStations(), d));
			goodStationsInRange.putAll(goodStationsNearby);
			HashMap<Direction, ChargingStation> badStationsNearby = (findStationsInRange(badStations(), d));
			badStationsInRange.putAll(badStationsNearby);
		}

		ArrayList<Direction> goodDirections = new ArrayList<Direction>(goodStationsInRange.keySet());
		ArrayList<Direction> badDirections = new ArrayList<Direction>(badStationsInRange.keySet());
		ArrayList<Direction> avoidBadDirections = avoidBadDirection(badDirections);

		Direction maxCoinDirection = null;
		ChargingStation sta = null;
		if (!goodDirections.isEmpty()) {
			maxCoinDirection = findMaxCoins(goodDirections, goodStationsInRange);
			sta = goodStationsInRange.get(maxCoinDirection);
		}

		if (!goodDirections.isEmpty() && sta.getCoins() > 0 && avoidBadDirections.contains(maxCoinDirection)) {
			Position newPos = this.currentPos.nextPosition(maxCoinDirection);
			if (newPos.inPlayArea()) {
				System.out.println("move to closest good station");
				moveDrone(maxCoinDirection, sta);
				updateStation(sta);
				setCurrentPos(newPos);
			} else {
				Direction randomDir = avoidBadStations(badDirections);
				moveDroneRandomly(randomDir);
				newPos = this.currentPos.nextPosition(randomDir);
				setCurrentPos(newPos);
			}
		} else if (badDirections.size() == 16) {
			System.out.print("all directions are bad");
			maxCoinDirection = findMaxCoins(badDirections, badStationsInRange);
			sta = goodStationsInRange.get(maxCoinDirection);
			Position newPos = this.currentPos.nextPosition(maxCoinDirection);
			while (!newPos.inPlayArea()) {
				badDirections.remove(maxCoinDirection);
				maxCoinDirection = findMaxCoins(badDirections, badStationsInRange);
				sta = goodStationsInRange.get(maxCoinDirection);
				newPos = this.currentPos.nextPosition(maxCoinDirection);
			}
			moveDrone(maxCoinDirection, sta);
			updateStation(sta);
			setCurrentPos(newPos);
		} else {
			Direction randomDir = avoidBadStations(badDirections);
			moveDroneRandomly(randomDir);
			Position newPos = this.currentPos.nextPosition(randomDir);
			setCurrentPos(newPos);

		}
	}

}