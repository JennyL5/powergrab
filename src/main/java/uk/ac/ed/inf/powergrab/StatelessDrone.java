package uk.ac.ed.inf.powergrab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

public class StatelessDrone extends Drone{
	
	

	public StatelessDrone(Position currentPos, Integer moves, Double coins, Double power, Integer seed, List <ChargingStation> Stations, String textfile) throws IOException {
		//Position initPos = new Position(lat, lon);
		//super(lat, lon, seed, Stations, textfile);
		super(currentPos,moves, coins, power, seed, Stations, textfile);
		setCurrentPos(currentPos);

	}
	
	protected void decide() throws IOException {
		//System.out.print(currentPos);
		Double lat = currentPos.latitude;
		Double lon = currentPos.longitude;
		while ( moves <= 250 || power > 0) {
			directionDecision(lat, lon);
		}
	}
	
	
	/*
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
			
	}*/
	
}