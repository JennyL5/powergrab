package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

public class StatelessDrone extends Drone{
	
	public StatelessDrone(Double lat, Double lon, Integer seed) {
		//Position initPos = new Position(lat, lon);
		setCurrentPos(new Position(lat, lon));
		
		while ( moves <= 250 || power > 0) {
			directionDecision(lat, lon, seed);
		}
		
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