package uk.ac.ed.inf.powergrab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StatelessDrone extends Drone{
	

	public StatelessDrone(Position currentPos, Integer seed, List <ChargingStation> Stations) throws IOException {
		super(currentPos, seed, Stations);
	}
	
	// starts the game
	public void startGame() throws IOException {
		int c = 0;
		while (!isFinished() ) {
			c++;
			System.out.println("Round ");
			System.out.println(c);
			System.out.println(this.currentPos.latitude);
			System.out.println(this.currentPos.longitude);
			strategy();
		}
	}
	
	// move the drone in any random direction
	public void moveInAnyDirection() {
		Direction d = getRandomDirection();
		Position newPos = this.currentPos.nextPosition(d);
		while (!newPos.inPlayArea()) {
			d = getRandomDirection();
			newPos = this.currentPos.nextPosition(d);
			System.out.println(d);
		}
		System.out.println("No bad Stations nearby or All directions have bad charging stations");
		moveDroneRandomly(d);
		setCurrentPos(newPos);
	}
	
	// the next move will put drone outside the play area
	public void outsideArea(ArrayList<Direction>goodDirections, ArrayList<Direction>badDirections) {
		System.out.print("Direction point outside play area");
		Direction randomDir = randDirection(goodDirections);
		Position newPos = this.currentPos.nextPosition(randomDir);
		int c = 0; 
		while (!newPos.inPlayArea()) {
			c++;
			if (c<16) {
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
	
	// splits station into good/bad, and tries to go to station with more coins and avoid negative stations
	protected void strategy() throws IOException {
		HashMap<Direction, ChargingStation> goodStationsInRange = new HashMap<Direction, ChargingStation>();
		HashMap<Direction, ChargingStation> badStationsInRange = new HashMap<Direction, ChargingStation>();
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
		
		if (!goodDirections.isEmpty()&& sta.getCoins() >0) {
			Position newPos = this.currentPos.nextPosition(maxCoinDirection);
			if (newPos.inPlayArea()) {
				System.out.println("move to closest good station");
				moveDrone(maxCoinDirection, sta);
				updateStation(sta);
				setCurrentPos(newPos);
			} else {
				outsideArea(goodDirections, badDirections);
			}
			
		} else if((badStationsInRange.isEmpty() || badStationsInRange.size()==16) ){
			moveInAnyDirection();
				
		} else {
			Direction randomDir = avoidBadStations(badDirections);

			moveDroneRandomly(randomDir); 
			Position newPos = this.currentPos.nextPosition(randomDir);
			setCurrentPos(newPos);
			
		}
	}

	
	
}