package uk.ac.ed.inf.powergrab;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App {
	public static FeatureCollection fc;
	public static List <ChargingStation> Stations;
	
	private static FeatureCollection parseGeoJSON(String day, String month, String year) throws IOException {
		URL mapURL = new URL(String.format("http://homepages.inf.ed.ac.uk/stg/powergrab/%s/%s/%s/powergrabmap.geojson",
				year, month, day));
		String mapSource = downloadGeoJSON(mapURL);
		return FeatureCollection.fromJson(mapSource);
	}
	
	/*private static List<ChargingStation> addStations() {
		List <ChargingStation> Stations = new ArrayList <ChargingStation>();
		for (Feature ft : fc.features()) {
			System.out.print(ft);
			ChargingStation c = new ChargingStation();
			c.coins = Double.parseDouble(ft.getProperty("coins").toString());
			c.power = Double.parseDouble(ft.getProperty("power").toString());
			Stations.add(c);
		}
		return Stations;
	}*/

	
	private static String downloadGeoJSON(URL mapUrl) throws IOException {
		//System.out.println("Making GET request to JSON server");
        HttpURLConnection conn = (HttpURLConnection) mapUrl.openConnection();
        conn.setReadTimeout(10000); // milliseconds
        conn.setConnectTimeout(15000); // milliseconds
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
		InputStream input = conn.getInputStream();
		return readStream(input);
	}
	
	private static String readStream(InputStream string1)throws IOException {
        // Read input from stream, build result as a string
        //System.out.println("Started processing JSON response");
        BufferedReader reader = new BufferedReader(new InputStreamReader(string1));
        StringBuilder result = new StringBuilder();
        String line;
        line = reader.readLine();
        result.append(line);
        while ((line = reader.readLine()) != null) {
            result.append("\n");
            result.append(line);
        }
        return result.toString();
    }
	private static LineString createBounds() {
		// test from left top corner to right bottom corner
		List<Point> points = new ArrayList<Point>();
		// longitude, latitude
		points.add(Point.fromLngLat(-3.192473, 55.946233));
		points.add(Point.fromLngLat(-3.184319, 55.946233));
		points.add(Point.fromLngLat(-3.184319, 55.942617));
		points.add(Point.fromLngLat(-3.192473, 55.942617));
		points.add(Point.fromLngLat(-3.192473, 55.946233));
		return LineString.fromLngLats(points);

	}

	private static List getChargingStations(FeatureCollection fc) {

    	for(Feature f : fc.features()) { //50 features
    		// Output current status of charging stations on map
    		//System.out.println(f.properties().keySet().getClass());
    		Point p = (Point) f.geometry();
    		System.out.println("coordinates of charging station" + p.coordinates().toString());
    		System.out.println("coins of charging station: " + f.getProperty("coins").toString());
    		System.out.println("power of chargin station: " + f.getProperty("power").toString());
    	}
		return null;
		
	}
	public static void main(String[] args) throws IOException {
		//15 09 2019 55.944425 -3.188396 5678 stateless
		//Scanner input = new Scanner(System.in);
    	//String command = input.nextLine();
    	//System.out.println("Command " + command);
    	//String [] command_array = command.split(" ");
    	//System.out.print(Arrays.toString(command_array));
    	String day = args[0];
    	String month = args[1];
    	String year = args[2];
    	Double lat = Double.parseDouble(args[3]);
    	Double lon = Double.parseDouble(args[4]);
    	Integer seed = Integer.parseInt(args[5]);
    	String drone_type = args[6];
    	
    	FeatureCollection fc = parseGeoJSON(day, month, year);
    	//List lst_station = addStations();
    	//System.out.print(addStations());
    	List <ChargingStation> Stations = new ArrayList <ChargingStation>();
		for (Feature ft : fc.features()) {
			System.out.print(ft);
			ChargingStation c = new ChargingStation();
			c.coins = Double.parseDouble(ft.getProperty("coins").toString());
			c.power = Double.parseDouble(ft.getProperty("power").toString());
			Stations.add(c);
		}
		System.out.print(Stations);
		ChargingStation.get_power(fc.features().get(0));
    	//System.out.print(ChargingStation.get_power(fc.features().get(0)));
		//Position initPos = new Position(lat, lon);
		if (drone_type == "stateless") {
			StatelessDrone stateless = new StatelessDrone(lat, lon, seed);
		} /*else {
			StatefulDrone stateful = new StatefuleDrone(lat, lon, seed);
		}*/
    	//List chargingStationsList = getChargingStations(FeatureCollection fc);
    	//List <List<String>>stations_list = new ArrayList<List<String>>();
    	/*
    	List <String>station_list = new ArrayList <String>();
    	int count=0;
    	int c = 0;
    	List <List>stations_list = new ArrayList <List>();
    	Hashtable<Integer, List> hash_table = new Hashtable<Integer, List>(); 
    	for(Feature f : fc.features()) { //50 features
    		count++;
    		// Output current status of charging stations on map
    		//System.out.println(f.properties().keySet().getClass());
    		Point p = (Point) f.geometry();
        	String station_id = f.getProperty("id").toString();
    		String station_coord = p.coordinates().toString();
    		String station_coins = f.getProperty("coins").toString();
    		String station_power = f.getProperty("power").toString();
    		//station_list.add(station_id);
    		station_list.add(p.coordinates().toString()) ;
    		station_list.add(f.getProperty("coins").toString()); 
    		station_list.add(f.getProperty("power").toString());
        
    		hash_table.put(count, station_list);
    		System.out.print(hash_table);
    		station_list.removeAll(station_list);
    	}
    	
    	
    	System.out.print(hash_table);
    	for (Feature f : fc.features()) {
        	String station_id = f.getProperty("id").toString();
    		//System.out.println(hash_table.get(station_id));
    	}
    	List <String>lst = new ArrayList <String>();

    	Hashtable<Integer, List> hash = new Hashtable<Integer, List>(); 
    	for (Feature f : fc.features()) {
    		count++;
    		hash.put(count, lst);

    		lst.add("hello1");
    		lst.add("hello2");
    		lst.add("hello3");
    		//System.out.print(lst);
    		hash.put(count, lst);
    		//System.out.print(hash);
    		lst.clear();
    	}
    	*/
    	//System.out.print(hash);

		
    	//for (int i = 0; i<station_list.size(); i++) {
    	//System.out.println(station_table);
    	//System.out.println(station_table.keys());
    	
    	//System.out.println(station_list.get(4));

    	//}
		

		// create a drone and its' path
		// position is longitude, latitude
		//Position initPos = new Position(lat, lon);
		//StatelessDrone stateless = new StatelessDrone(initPos);
		/*LineString statelessPath = stateless.getStatelessPath();
		System.out.println("Coins: " + stateless.getCoins() + ", Power: " + stateless.getPower());
		f = Feature.fromGeometry((Geometry) statelessPath);
		fc.features().add(f);

		// create a line to show the bounds	
		LineString boundsBox = createBounds();
		f = Feature.fromGeometry((Geometry) boundsBox);
		fc.features().add(f);
		*/
		//System.out.println(fc.toJson());
		
    
	}
	/*
	private static String createTextFile(String drone_type, String day, String month, String year) {
		String textfile = new String (String.format("%s-%d-%d-%d.txt", drone_type, day, month, year));
		System.out.println(textfile); // 250 lines
		//55.944425,-3.188396,SSE,55.944147836140246,-3.1882811949702905,0.0,248.75
		List<String> lines = Arrays.asList(prev_lat, prev_lon, direction, next_lat, next_lon, coins, power);
		List<String> movesList = movesList.add(lines);
		//Path file = Paths.get("the-file-name.txt");
		Path file = Paths.get(textfile);
		Files.write(file, movesList, StandardCharsets.UTF_8);
		return textfile;	
	}
	private static String createGeoJSONFile(String drone_type, String day, String month, String year) {
		String geojsonfile = new String (String.format("%s-%d-%d-%d.geojson", drone_type, day, month, year));
		System.out.println(geojsonfile);
    	for(Feature f : fc.features()) { //50 features
    		
    	}
		return geojsonfile;	
	}*/
	
}
	