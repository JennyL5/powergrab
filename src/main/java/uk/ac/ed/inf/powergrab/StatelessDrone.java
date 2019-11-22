package uk.ac.ed.inf.powergrab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

public class StatelessDrone extends Drone{
	

	public StatelessDrone(Position currentPos, Double coins, Double power, Integer seed, List <ChargingStation> Stations) throws IOException {
		//Position initPos = new Position(lat, lon);
		//super(lat, lon, seed, Stations, textfile);
		super(currentPos, coins, power, seed, Stations);

	}

	public void startGame() throws IOException {
		// TODO Auto-generated method stub
		System.out.println("Round 1");
		directionDecision();
		int c = 1;
		while (!isFinished() ) {
		//for(int i =0; i < 5; i++) {
			c++;
			System.out.print("Round ");
			System.out.println(c);
			System.out.println(this.currentPos.latitude);
			System.out.println(this.currentPos.longitude);

			directionDecision();


		}
		//System.out.print(movesHistory);

	
		
	}
	
	
	
	
}