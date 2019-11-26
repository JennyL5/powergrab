package uk.ac.ed.inf.powergrab;

import java.awt.Color;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
		while (!isFinished() ) {
			c++;
			System.out.println("Round ");
			System.out.println(c);
			System.out.println(this.currentPos.latitude);
			System.out.println(this.currentPos.longitude);
			strategy();
		}
	}
	
	public void strategy() throws IOException {
		// for each direction // find nearby stations
		
		HashMap<Direction, ChargingStation> goodStationsInRange = new HashMap<Direction, ChargingStation>();
		HashMap<Direction, ChargingStation> badStationsInRange = new HashMap<Direction, ChargingStation>();
		for (Direction d : Direction.values()) {
			// find nearest (good) stations from that d within rANGE
			HashMap<Direction, ChargingStation>  goodStationsNearby = (findStationsInRange(goodStations(), d));
			goodStationsInRange.putAll(goodStationsNearby);
			HashMap<Direction, ChargingStation>  badStationsNearby = (findStationsInRange(badStations(), d));
			badStationsInRange.putAll(badStationsNearby);
		}
		
		//ArrayList<Direction> goodDirections = new ArrayList<Direction>(goodStationsInRange.keySet());
		//ArrayList <Direction> badDirections = new ArrayList<Direction>(badStationsInRange.keySet());

		
		//:D
		DecimalFormat df = new DecimalFormat("#.####");
		HashMap<ChargingStation, Double> goodStationsCoins = new HashMap<ChargingStation, Double>();
		HashMap<ChargingStation, Double> distanceOfGoodStations = new HashMap<ChargingStation, Double>();
		HashMap<ChargingStation, Double> greenessOfGoodStations = new HashMap<ChargingStation, Double>();

		for (ChargingStation a : goodStations()) { 
        	//goodStationsCoins.put(a, a.getCoins());
        	Double distance = calculateDistanceToStation(a.getPos());
        	distanceOfGoodStations.put(a, distance);
        	//greenessOfGoodStations.put(a,Double.valueOf(Color.decode(a.getColour()).getGreen()));
        }
		
		// sort good CS by nearest
		distanceOfGoodStations = sortByValue(distanceOfGoodStations);
		// get ave distance
		
		List<Object> distances = Arrays.asList(((distanceOfGoodStations.values().toArray())));
        
        // move to nearest charging station (min dist)
        ChargingStation goal = (ChargingStation) distanceOfGoodStations.keySet().toArray()[0];

        ArrayList<Direction> badDirectionsInRange = new ArrayList<Direction>(badStationsInRange.keySet());
        ArrayList <Direction> avoidBadDirections = avoidBadDirection(badDirectionsInRange);
        Direction minDir = null;
        double distance = Double.MAX_VALUE;
       
        // check coins value or reomve from good stations
        
        // Find the direction to take with the min distance to CS (avoids negative)
        for ( Direction d : avoidBadDirections) {
        	// find min d to get to CS
        	Double dist = calculateDistanceToStation(goal.pos);
        	if (dist < distance) {
        		distance = dist;
        		minDir = d;
        	}

        }
        
        
        if (minDir.equals(null)){
        	System.out.println("ERROR: no direction to take, as no direction is found for min distance");
        } else {
        	// move to
        	System.out.println("move");
			Position newPos = this.currentPos.nextPosition(minDir);
			if (newPos.inPlayArea()) {
	        	// check if goal station is within range
				if (getRange(convertToPoint(newPos), convertToPoint(goal.pos))<0.0025) {
	        		// move to near station
					System.out.println("Collect from station");
					moveDrone(minDir, goal);
					updateStation(goal);
					setCurrentPos(newPos);
	        	} else {
	        		// mvoe closer
	        		System.out.println("moving closer");
					updateDrone(minDir);
	        	}
			}
        }
	}
	
	
	public Position pythagoras (Position nextNextPos) {
		Double r = 0.0003;
		Double pyth =Math.sqrt(r*r - r*r);  
		double lat = nextNextPos.latitude+pyth;
		return new Position (lat, nextNextPos.longitude);
	}
	
	public void minAngle() { 
	   	Direction E = null;
		Position axisE = this.currentPos.nextPosition(E);
		
    	Direction W = null;
		Position axisW = this.currentPos.nextPosition(W);
        
        for (Direction d : Direction.values()) {
        	Position hypotenuse = this.currentPos.nextPosition(d);
        }
	}
	
	public Double calculateAverage(List<Object> distances) {
		Double sum = 0.0;
		Double ave = 0.0;
		DecimalFormat df = new DecimalFormat("#0.######");
		for (Object i : distances) {
			//System.out.println(i);
		    sum+=(Double)i;
		}
		//System.out.println(df.format(sum));
		if(distances.isEmpty()){
			ave=null;
		    System.out.println("List is empty");
		} else {
			ave = sum/(float)distances.size();
		    System.out.println("Average found is " + ave); 
		}
		return ave;
	}
	
	public Double calculateDistanceToStation(Position stationPos) {
		Double distance = getRange(convertToPoint(this.currentPos), convertToPoint(stationPos));
		return distance;
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
