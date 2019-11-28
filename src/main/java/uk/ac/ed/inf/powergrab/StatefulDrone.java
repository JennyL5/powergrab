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
        ArrayList <ChargingStation> badStations = (ArrayList<ChargingStation>) badStations();
        ArrayList <ChargingStation> visitLater = new ArrayList<ChargingStation> ();

		while (!isFinished()) {
			c++;
			System.out.println("Round ");
			System.out.println(c);
			System.out.println(this.currentPos.latitude);
			System.out.println(this.currentPos.longitude);
			strategy(goodStations, badStations, visitLater);
		}
	}
	
	public void strategy(ArrayList <ChargingStation> goodStations, ArrayList <ChargingStation> badStations, ArrayList <ChargingStation> visitLater) throws IOException {

		HashMap<ChargingStation, Double> distanceOfGoodStations = new HashMap<ChargingStation, Double>();
		HashMap<Direction, ChargingStation> goodStationsInRange = new HashMap<Direction, ChargingStation>();
		HashMap<Direction, ChargingStation> badStationsInRange = new HashMap<Direction, ChargingStation>();

		for (Direction d : Direction.values()) {
			HashMap<Direction, ChargingStation>  goodStationsNearby = (findStationsInRange(goodStations, d));
			goodStationsInRange.putAll(goodStationsNearby);
			HashMap<Direction, ChargingStation>  badStationsNearby = (findStationsInRange(badStations, d));
			badStationsInRange.putAll(badStationsNearby);
		}
		
        ArrayList<Direction> badDirectionsInRange = new ArrayList<Direction>(badStationsInRange.keySet());
	
		System.out.println("Good stations");
		System.out.println(goodStations.size());
		
		
		
		distanceOfGoodStations = getSortedDistances(distanceOfGoodStations, goodStations);
		System.out.print(visitLater.size());
		if (distanceOfGoodStations.size()==0){
			if (visitLater.size() != 0) {
				System.out.println("no more good stations to go to");
				for (Direction d : Direction.values()) {
					HashMap<Direction, ChargingStation>  goodStationsNearby = (findStationsInRange(visitLater, d));
					goodStationsInRange.putAll(goodStationsNearby);
				}
				distanceOfGoodStations = getSortedDistances(distanceOfGoodStations, visitLater);
				ChargingStation goal = (ChargingStation) distanceOfGoodStations.keySet().toArray()[0];
		        Direction minDir = findMinDirection(badStationsInRange, goal);
		        System.out.println("move the drone");
				Position newPos = this.currentPos.nextPosition(minDir);
				if (newPos.inPlayArea()) {
					System.out.println("approaching station");
					if (getRange(convertToPoint(newPos), convertToPoint(goal.pos))<0.00025) {
			    		// move to near station
						visitLater.remove(goal);
						System.out.println("Collect from station");
						moveDrone(minDir, goal);
						updateStation(goal);
						setCurrentPos(newPos);
					}else {

			    		System.out.println("not within range, move closer to goal charging station");    		
			    		
			    		try {
			        		int len = directionHistory.size();
			        		
			        		System.out.println(directionHistory);
			    			System.out.print(".............................");
			    			System.out.print("+");
			        		System.out.print(minDir);
			    			System.out.print("+");

			        		System.out.print(directionHistory.get(len-4));
			    			System.out.print("+");
			        		
			        		System.out.print(directionHistory.get(len-2));
			    			if (directionHistory.get(len-2).equals(minDir) && directionHistory.get(len-2).equals(directionHistory.get(len-4)) ) {
			    				visitLater.add(goal);
			    				System.out.print("If goal station on/near bad station!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			    				Direction randomDir = avoidBadStations(badDirectionsInRange);
			    				//moveDroneRandomly(randomDir); 
			    				newPos = this.currentPos.nextPosition(randomDir);
			    				//setCurrentPos(newPos);
			    			} 
			    		}catch(Exception e) {
			    			System.out.println(e);
			    		}
						
			    		System.out.println("moving closer");
			    		System.out.print(minDir);
						moveDroneRandomly(minDir);
						setCurrentPos(newPos);


			    	
					}
					//approachStation(newPos, goal, minDir, goodStations, badDirectionsInRange, distanceOfGoodStations, badStationsInRange, visitLater);
				} else {
					System.out.println("outside area, remove original goal, set new goal");
					outsidePlayArea(goodStations, goal, distanceOfGoodStations, badStationsInRange);
			        }
			
				
			}else {
				System.out.println("no more good stations to go to");
				Direction randomDir = avoidBadStations(badDirectionsInRange);
				moveDroneRandomly(randomDir); 
				Position newPos = this.currentPos.nextPosition(randomDir);
				setCurrentPos(newPos);
			}
			
		} else {
		
	        ChargingStation goal = (ChargingStation) distanceOfGoodStations.keySet().toArray()[0];
	        Direction minDir = findMinDirection(badStationsInRange, goal);
	        if (minDir.equals(null)){
	        	System.out.println("ERROR: no direction to take, as no direction is found for min distance");
	        } else {
	        	System.out.println("move the drone");
				Position newPos = this.currentPos.nextPosition(minDir);
				if (newPos.inPlayArea()) {
					System.out.println("approach station");
					approachStation(newPos, goal, minDir, goodStations, badDirectionsInRange, distanceOfGoodStations, badStationsInRange, visitLater);
				} else {
					System.out.println("outside play area, remove original goal, set new goal");
					if (distanceOfGoodStations.size()>2 && distanceOfGoodStations.keySet().size()>2) {
						outsidePlayArea(goodStations, goal, distanceOfGoodStations, badStationsInRange);
					}else {
						Direction randomDir = avoidBadStations(badDirectionsInRange);
	    				//moveDroneRandomly(randomDir); 
	    				newPos = this.currentPos.nextPosition(randomDir);
	    				moveDroneRandomly(randomDir);
	    				setCurrentPos(newPos);

	    				
					}
			        }
			       
				}
	        
		}
	}

	public void approachStation(Position newPos, ChargingStation goal, Direction minDir, ArrayList<ChargingStation> goodStations, 
					ArrayList<Direction> badDirectionsInRange, HashMap<ChargingStation, Double>distanceOfGoodStations, 
					HashMap<Direction, ChargingStation> badStationsInRange, ArrayList<ChargingStation> visitLater) throws IOException {
		if (getRange(convertToPoint(newPos), convertToPoint(goal.pos))<0.00025) {
    		// move to near station
			System.out.println("Collect from station");
			moveDrone(minDir, goal);
			updateStation(goal);
			setCurrentPos(newPos);
			goodStations.remove(goal);
    	} else {
    		System.out.println("not within range, move closer to goal charging station");    		
    		
    		try {
        		int len = directionHistory.size();
        		
        		System.out.println(directionHistory);
    			System.out.print(".............................");
    			System.out.print("+");
        		System.out.print(minDir);
    			System.out.print("+");

        		System.out.print(directionHistory.get(len-4));
    			System.out.print("+");
        		
        		System.out.print(directionHistory.get(len-2));
    			if (directionHistory.get(len-2).equals(minDir) && directionHistory.get(len-2).equals(directionHistory.get(len-4)) ) {
    				goodStations.remove(goal);
    				visitLater.add(goal);
    				System.out.print("If goal station on/near bad station!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    				Direction randomDir = avoidBadStations(badDirectionsInRange);
    				//moveDroneRandomly(randomDir); 
    				newPos = this.currentPos.nextPosition(randomDir);
    				//setCurrentPos(newPos);
    			} 
    		}catch(Exception e) {
    			System.out.println(e);
    		}
			
    		System.out.println("moving closer");
    		System.out.print(minDir);
			moveDroneRandomly(minDir);
			setCurrentPos(newPos);


    	}
	}
	

	public void outsidePlayArea(ArrayList<ChargingStation> goodStations, ChargingStation goal, HashMap <ChargingStation, Double>distanceOfGoodStations, HashMap <Direction, ChargingStation> badStationsInRange ) throws IOException {
		ChargingStation goal1 = (ChargingStation) distanceOfGoodStations.keySet().toArray()[1];
        Direction minDir1 = findMinDirection(badStationsInRange, goal1);
        if (minDir1.equals(null)){
        	System.out.println("No direction is found for min distance");
        } else {
        	Position newPos1 = this.currentPos.nextPosition(minDir1);
			if (newPos1.inPlayArea()) {
	        	
				System.out.print("check if new goal station is within range");
				if (getRange(convertToPoint(newPos1), convertToPoint(goal1.pos))<0.00025) {
	        		// move to near station
					System.out.println("Collect from station");
					moveDrone(minDir1, goal1);
					updateStation(goal1);
					setCurrentPos(newPos1);
					//goodStations.remove(goal1);
	        	} else {
	        		System.out.println("moving closer");
					updateDrone(minDir1);
					setCurrentPos(newPos1);
	        	}
			} else {
	        	System.out.print("very stuuuuuuuck");

			}
        }
	}
	
	
	
	public Direction findMinDirection(HashMap<Direction, ChargingStation> badStationsInRange, ChargingStation goal) {
        ArrayList<Direction> badDirectionsInRange = new ArrayList<Direction>(badStationsInRange.keySet());
        ArrayList <Direction> avoidBadDirections = avoidBadDirection(badDirectionsInRange);
        Direction minDir = null;
        double distance = Double.MAX_VALUE;
       
        for ( Direction d : avoidBadDirections) {
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
	
	
	public HashMap<ChargingStation, Double> getSortedDistances(HashMap<ChargingStation, Double> hm, ArrayList<ChargingStation>goodStations) { 
		for (ChargingStation a : goodStations) { 
        	Double distance = getRange(convertToPoint(this.currentPos), convertToPoint(a.pos));
        	hm.put(a, distance);
        }
		
        List<Map.Entry<ChargingStation, Double> > list = new LinkedList<Map.Entry<ChargingStation, Double> >(hm.entrySet()); 
  
        Collections.sort(list, new Comparator<Map.Entry<ChargingStation, Double> >() { 
            public int compare(Map.Entry<ChargingStation, Double> o1, Map.Entry<ChargingStation, Double> o2){ 
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
