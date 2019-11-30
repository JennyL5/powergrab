package uk.ac.ed.inf.powergrab;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;


public class App {
	public static FeatureCollection fc;
	
	/**
	 * Creates a list of all the stations from the features of the feature collection.
	 * It gets the charging stations's coins, power, marker and position.
	 * 
	 * @param  fc  a feature collection
	 * @result Stations   a list of all charging stations
	 */
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
	
	
	/**
	 * The main method that will run. It gets the arguments and splits them into 
	 * day, month, year, latitude, longitude, seed, and drone type to play the game.
	 * Once the game is finished, this writes the results to text file and json file.
	 * 
	 * @param  args[]  string arguments passed in
	 */
	public static void main(String[] args) throws IOException {

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
		if (drone_type.equals("stateless")) {
			System.out.println("Start Game for stateless drone");
			StatelessDrone stateless = new StatelessDrone(initPos,seed, Stations);
			stateless.startGame();
			map =game.convertToFile(stateless.movesHistory);
			txt=stateless.totxt();
			path="stateless"+"-"+day+"-"+month+"-"+year;
		} else {
			System.out.println("Start Game for stateful drone");
			StatefulDrone stateful = new StatefulDrone(initPos,seed, Stations);
			stateful.startGame();
			map =game.convertToFile(stateful.movesHistory);
			txt=stateful.totxt();
			path="stateful"+"-"+day+"-"+month+"-"+year;
		}


		PrintWriter writer1 = new PrintWriter(System.getProperty("user.dir") + "/" + path + ".geojson");
//		System.out.print(System.getProperty("user.dir") + "/" + path + ".geojson");
		writer1.print(map);
		writer1.close();
		PrintWriter writer2 = new PrintWriter(System.getProperty("user.dir") + "/" + path + ".txt");
		writer2.print(txt);
		writer2.close();
		System.out.print("game over");
		
		/*public void writeToFile(String filename, String filepath, String contents) {
	        try {
	            FileWriter file = new FileWriter(filename);
	            file.write(contents);
	            file.close();
	        }catch(Exception e) {
	            System.out.println(e.toString());
	        }
	    }*/

	}

}
	