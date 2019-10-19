package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;

public class StatelessDrone extends Drone{
	
	public StatelessDrone(Position initPos) {setCurrentPos(initPos);}
	public StatelessDrone() {setCurrentPos(new Position(-3.192473,55.946233));}



	// check vicinity and choose best option
	private Position chooseMove() {
		// choose position
		// drone can see around r =0.0003 and sucks coins/power at 0.00025
		//HashMap<String, Direction> inNeighbourhood = getNeighbourhood();
		// TODO: Directions
		// go towards some station
		
		// get away from stations
		
		// nothing in vicinity carry on in same direction
		//if (inNeighbourhood.isEmpty()) {}		
		
		
	}

	
	
	// get a random path from selecting random from valid moves
	public LineString getStatelessPath() {
		// test from left top corner to right bottom corner
		List<Point> points =new ArrayList<Point>();
		updateStats();
		while(!isFinished() && inPlayArea()) {
			// position p from choose move
			Position p = chooseMove();
			points.add(Point.fromLngLat(p.longitude, p.latitude));
			moveDrone(p);
			updateStats();
			System.out.println("moves: " + !isFinished() + "   area: " + inPlayArea());
		}
		
		
		return LineString.fromLngLats(points);
			
	}

}