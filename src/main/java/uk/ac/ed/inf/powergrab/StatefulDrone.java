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
	public StatefulDrone(Position currentPos, Integer seed, List <ChargingStation> Stations) throws IOException {
		super(currentPos, seed, Stations);
	}

	public void startGame() throws IOException {
		int c = 0;
        ArrayList <ChargingStation> goodStations = (ArrayList<ChargingStation>) goodStations();

		while (!isFinished() ) {
			c++;
			System.out.println("Round ");
			System.out.println(c);
			System.out.println(this.currentPos.latitude);
			System.out.println(this.currentPos.longitude);
			strategy(goodStations);
		}
	}
	
	public void strategy(ArrayList <ChargingStation> goodStations) throws IOException {

		// for each direction // find nearby stations
		HashMap<ChargingStation, Double> distanceOfGoodStations = new HashMap<ChargingStation, Double>();

		HashMap<Direction, ChargingStation> goodStationsInRange = new HashMap<Direction, ChargingStation>();
		HashMap<Direction, ChargingStation> badStationsInRange = new HashMap<Direction, ChargingStation>();

		for (Direction d : Direction.values()) {
			// find nearest (good) stations from that d within rANGE
			HashMap<Direction, ChargingStation>  goodStationsNearby = (findStationsInRange(goodStations, d));
			goodStationsInRange.putAll(goodStationsNearby);
			HashMap<Direction, ChargingStation>  badStationsNearby = (findStationsInRange(badStations(), d));
			badStationsInRange.putAll(badStationsNearby);
		}
		
		System.out.println("Good stations");
		System.out.println(goodStations.size());
		for (ChargingStation a : goodStations) { 
        	Double distance = getRange(convertToPoint(this.currentPos), convertToPoint(a.pos));
        	distanceOfGoodStations.put(a, distance);
        }
		
		// sort good CS by nearest
		distanceOfGoodStations = sortByValue(distanceOfGoodStations);
        // move to nearest charging station (min dist)
		System.out.print(distanceOfGoodStations.size());
	
		Direction lastDir = null;
		ChargingStation lastGoal = null;


		
		if (distanceOfGoodStations.size()==0){
			// no more good stations
			System.out.print("no more good stations to go to");

			//directionHistory.get(250-movesLeft-1);
			
	        ArrayList<Direction> badDirectionsInRange = new ArrayList<Direction>(badStationsInRange.keySet());
	        //ArrayList <Direction> avoidBadDirections = avoidBadDirection(badDirectionsInRange);
	        Direction maxDir = randDirection(badDirectionsInRange);
	        Position newPos = this.currentPos.nextPosition(maxDir);
	        
			while (!newPos.inPlayArea()) {
		        maxDir = randDirection(badDirectionsInRange);
		        newPos = this.currentPos.nextPosition(maxDir);
			}

			System.out.println("maxDir");
			System.out.println(maxDir);
			updateDrone(maxDir);
			setCurrentPos(newPos);
			System.out.print("coins");
			System.out.println(this.coins);
			
		} else {
		
	        ChargingStation goal = (ChargingStation) distanceOfGoodStations.keySet().toArray()[0];
	        Direction minDir = findMinDirection(badStationsInRange, goal);
	        
	    	
	        if (minDir.equals(null)){
	        	System.out.println("ERROR: no direction to take, as no direction is found for min distance");
	        } else {
	        	// move to
	        	System.out.println("minDir");
	        	System.out.println(minDir);
	        	System.out.println("move the drone");
				Position newPos = this.currentPos.nextPosition(minDir);
				if (newPos.inPlayArea()) {
		        	// check if goal station is within range
					System.out.print("hi");
					if (getRange(convertToPoint(newPos), convertToPoint(goal.pos))<0.00025) {
		        		// move to near station
						System.out.println("Collect from station");
						moveDrone(minDir, goal);
						updateStation(goal);
						setCurrentPos(newPos);
						goodStations.remove(goal);
		        	} else {
		        		// mvoe closer
		        		System.out.println("moving closer");
						updateDrone(minDir);
						setCurrentPos(newPos);
						
		        	}
				} else {
					System.out.print("outside play area");
					//goodStations.remove(goal);
					ChargingStation goal1 = (ChargingStation) distanceOfGoodStations.keySet().toArray()[1];
			        Direction minDir1 = findMinDirection(badStationsInRange, goal);
			        if (minDir.equals(null)){
			        	System.out.println("ERROR: no direction to take, as no direction is found for min distance");
			        } else {
		
			        	Position newPos1 = this.currentPos.nextPosition(minDir1);
						if (newPos1.inPlayArea()) {
				        	// check if goal station is within range
							System.out.print("hi");
							if (getRange(convertToPoint(newPos1), convertToPoint(goal1.pos))<0.00025) {
				        		// move to near station
								System.out.println("Collect from station");
								moveDrone(minDir1, goal1);
								updateStation(goal1);
								setCurrentPos(newPos1);
								goodStations.remove(goal1);
				        	} else {
				        		// mvoe closer
				        		
				        		System.out.println("moving closer");
								updateDrone(minDir1);
								setCurrentPos(newPos1);
								
				        	}
						} else {
				        	System.out.print("very stuuuuuuuck");

						}
			        }
			       
				}
	        }
		}
	}
	
	// gets a random direction that is absent from the Directions from input HashMap
	protected static ArrayList<Direction> avoidBadDirection( ArrayList <Direction> badDir) {
		ArrayList <Direction> notBadDir = new ArrayList <Direction>();
		System.out.println(badDir.size());
		for (Direction d : Direction.values()) {
			if (!badDir.contains(d)){
				notBadDir.add(d);
			}
		}
		return notBadDir;
	}

	
	public Direction findMinDirection(HashMap<Direction, ChargingStation> badStationsInRange, ChargingStation goal) {
        ArrayList<Direction> badDirectionsInRange = new ArrayList<Direction>(badStationsInRange.keySet());
        ArrayList <Direction> avoidBadDirections = avoidBadDirection(badDirectionsInRange);
        Direction minDir = null;
        double distance = Double.MAX_VALUE;
       
        // Find the direction to take with the min distance to CS (avoids negative)
        for ( Direction d : avoidBadDirections) {
        	// find min d to get to CS
        	Double dist = getRange(convertToPoint(this.currentPos.nextPosition(d)), convertToPoint(goal.pos));
        	if (dist < distance) {
        		System.out.print(d);
        		distance = dist;
        		minDir = d;
        	}

        }
		return minDir;
	}
	
	public static HashMap<ChargingStation, Double> sortByValue(HashMap<ChargingStation, Double> hm) 
    { 
        // Create a list from elements of HashMap 
        List<Map.Entry<ChargingStation, Double> > list = 
               new LinkedList<Map.Entry<ChargingStation, Double> >(hm.entrySet()); 
  
        // Sort the list 
        Collections.sort(list, new Comparator<Map.Entry<ChargingStation, Double> >() { 
            public int compare(Map.Entry<ChargingStation, Double> o1,  
                               Map.Entry<ChargingStation, Double> o2) 
            { 
                return (o1.getValue()).compareTo(o2.getValue()); 
            } 
        }); 
          
        // put data from sorted list to hashmap  
        HashMap<ChargingStation, Double> temp = new LinkedHashMap<ChargingStation, Double>(); 
        for (Map.Entry<ChargingStation, Double> aa : list) { 
            temp.put(aa.getKey(), aa.getValue()); 
        } 
        return temp; 
    } 
  
	
	
}
