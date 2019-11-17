package uk.ac.ed.inf.powergrab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

public class StatelessDrone extends Drone{
	

	public StatelessDrone(Position currentPos, Double coins, Double power, Integer seed, List <ChargingStation> Stations, String textfile) throws IOException {
		//Position initPos = new Position(lat, lon);
		//super(lat, lon, seed, Stations, textfile);
		super(currentPos, coins, power, seed, Stations, textfile);

	}

	public void startGame(Double lat, Double lon) throws IOException {
		// TODO Auto-generated method stub
		directionDecision(lat, lon);

		while (!isFinished()) {
		//for(int i =0; i < 5; i++) {
			directionDecision(this.currentPos.latitude, this.currentPos.latitude);


		}
		//System.out.print(movesHistory);

	
		
	}
	
	
	
	
}