package uk.ac.ed.inf.powergrab;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

import java.net.HttpURLConnection;
import java.net.URL;


public class App {
	public static FeatureCollection fc;
	
	protected static List<ChargingStation> Create_Stations_List(FeatureCollection fc) {
		List <ChargingStation> Stations = new ArrayList <ChargingStation>();
		for (Feature f : fc.features()) {
			ChargingStation c = new ChargingStation();
			c.setCoins(f.getProperty("coins").getAsDouble());
			c.setPower(f.getProperty("power").getAsDouble());
			c.setMarker(f.getProperty("marker-symbol").getAsString());
			Point point = (Point) f.geometry();
			Position p = new Position (point.latitude(), point.longitude());
			c.setPos(p);
			Stations.add(c);
		}
		return Stations;
	}
	
	public static void main(String[] args) throws IOException {
		//15 09 2019 55.944425 -3.188396 5678 stateless

    	String day = args[0];
    	String month = args[1];
    	String year = args[2];
    	Double lat = Double.parseDouble(args[3]);
    	Double lon = Double.parseDouble(args[4]);
    	Integer seed = Integer.parseInt(args[5]);
    	String drone_type = args[6].toString();
    	
    	String mapString = String.format("http://homepages.inf.ed.ac.uk/stg/powergrab/%s/%s/%s/powergrabmap.geojson",
				year, month, day);
    	
    	Game game = new Game(mapString);
    
    	FeatureCollection fc = game.fc;
    	List <ChargingStation> Stations = Create_Stations_List(fc);
    	
		Position initPos = new Position(lat, lon);

		String path= "";
		String map = "";
		String txt="";
		if (drone_type.contains("stateless")) {
			System.out.print("Start Game");
			System.out.print(initPos.latitude);
			System.out.print(initPos.longitude);

			StatelessDrone stateless = new StatelessDrone(initPos,seed, Stations);
			stateless.startGame();
			map =game.convertToFile(stateless.movesHistory);
			txt=stateless.totxt();
			path="stateless"+day+month+year;
		} 
		String filepath = "/afs/inf.ed.ac.uk/user/s17/s1705544/Documents/powergrab/"+path;
		//String filepath = "C:\\Users\\Jenny\\Downloads\\"+path;

		PrintWriter writer1 = new PrintWriter(filepath + ".geojson");
		writer1.println(map);
		writer1.close();
		PrintWriter writer2 = new PrintWriter(filepath + ".txt");
		writer2.println(txt);
		writer2.close();
		System.out.print("game over");
	}

}
	