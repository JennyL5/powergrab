package uk.ac.ed.inf.powergrab;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
	
	private static FeatureCollection parseGeoJSON(String day, String month, String year) throws IOException {
		URL mapURL = new URL(String.format("http://homepages.inf.ed.ac.uk/stg/powergrab/%s/%s/%s/powergrabmap.geojson",
				year, month, day));
		String mapSource = downloadGeoJSON(mapURL);
		return FeatureCollection.fromJson(mapSource);
	}

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
	
	protected static List<ChargingStation> Create_Stations_List(FeatureCollection fc) {
		List <ChargingStation> Stations = new ArrayList <ChargingStation>();
		for (Feature f : fc.features()) {
			ChargingStation c = new ChargingStation();
			c.setCoins(c, f.getProperty("coins").getAsDouble());
			c.setPower(c, f.getProperty("power").getAsDouble());
			c.setMarker(c, f.getProperty("marker-symbol").getAsString());
			Point point = (Point) f.geometry();
			Position p = new Position (point.latitude(), point.longitude());
			c.setPos(c, p);
			//System.out.print(c.coins);
			//System.out.print(c.power);
			Stations.add(c);
			//System.out.println(c);
		}
		//System.out.print(Stations.get(0).pos);
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

    	// Prepare charging station
    	FeatureCollection fc = parseGeoJSON(day, month, year);
    	List <ChargingStation> Stations = Create_Stations_List(fc);
    	
		String textfile = createTextFile(drone_type, day, month, year);
		//System.out.print(drone_type.toCharArray());
		
		if (drone_type.contains("stateless")) {
			Position initPos = new Position(lat, lon);

			//System.out.println(Stations);
			//Position currentPos, Integer moves, Double coins, Double power, Integer seed, List <ChargingStation> Stations, String textfile
			StatelessDrone stateless = new StatelessDrone(initPos, 0, 0.0, 0.0,seed, Stations, textfile);
			stateless.decide();
		} 
	}
	
	private static String createTextFile(String drone_type, String day, String month, String year) {
		return new String (String.format("%s-%s-%s-%s.txt", drone_type, day, month, year));	
	}
	

	private static String createGeoJSONFile(String drone_type, String day, String month, String year) {
		String geojsonfile = new String (String.format("%s-%s-%s-%s.geojson", drone_type, day, month, year));
		System.out.println(geojsonfile);
    	for(Feature f : fc.features()) { //50 features
    		
    	}
		return geojsonfile;	
	}
	
}
	