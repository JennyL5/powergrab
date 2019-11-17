package uk.ac.ed.inf.powergrab;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class Main {
	
	private static FeatureCollection parseGeoJSON(String day, String month, String year) throws IOException {
		URL mapURL = new URL(String.format("http://homepages.inf.ed.ac.uk/stg/powergrab/%s/%s/%s/powergrabmap.geojson",
				year, month, day));
		String mapSource = downloadUrl(mapURL);
		// Check output of getGeoJSON
		// System.out.println(mapSource);
		return FeatureCollection.fromJson(mapSource);
	}
	
	private static String downloadUrl(URL mapUrl) throws IOException {
		System.out.println("Making GET request to JSON server");
        HttpURLConnection conn = (HttpURLConnection) mapUrl.openConnection();
        conn.setReadTimeout(10000); // milliseconds
        conn.setConnectTimeout(15000); // milliseconds
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
		InputStream input = conn.getInputStream();
		
		return readStream(input);
	}
	/*
	private static String loadFileFromNetwork(InputStream in_stream) throws IOException {
        return readStream(downloadUrl(new URL(in_stream)));
    }*/
	
	private static String readStream(InputStream string1)throws IOException {
        // Read input from stream, build result as a string
        System.out.println("Started processing JSON response");
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
	
	public static void main(String[] args) throws IOException {
		//System.out.println(Direction.N);
		//15 09 2019 55.944425 -3.188396 5678 stateless
		Scanner input = new Scanner(System.in);
    	String command = input.nextLine();
    	System.out.println("Command " + command);
    	String [] command_array = command.split(" ");
    	//System.out.print(Arrays.toString(command_array));
    	String day = command_array[0];
    	String month = command_array[1];
    	String year = command_array[2];
    	String lat = command_array[3];
    	String lon = command_array[4];
    	String seed = command_array[5];
    	String drone_type = command_array[6];
    	
    	
    	FeatureCollection fc = parseGeoJSON(day, month, year);
    	//InputStream in_stream = downloadUrl(mapUrl);
    	System.out.print(fc);
    	
    	Feature f;
//      for(Feature f : fc.features()) {
//      	// Type cast the geometry object f.geometry into a Point object without 
//      	// creating a new object **********************CHANGEEEEE
//      	Point p = (Point) f.geometry();
//      	System.out.println(p.coordinates().toString());
//      	System.out.println(f.properties());
//      	// f.getProperty( _ ).getAsFloat();
//      	System.out.println("c: " + f.getProperty("coins").toString());
//      	System.out.println("p: " + f.getProperty("power").toString());
//      	// f.getProperty( _ ).getAsString();
//      	System.out.println("ms: " + f.getProperty("marker-symbol").toString());
//      }
    	/*
		// create a drone and its' path
		// position is longitude, latitude
		Position initPos = new Position(55.9439527, -3.1878134);
		StatelessDrone stateless = new StatelessDrone(initPos);
		LineString statelessPath = stateless.getStatelessPath();
		System.out.println("Coins: " + stateless.getCoins() + ", Power: " + stateless.getPower());
		f = Feature.fromGeometry((Geometry) statelessPath);
		fc.features().add(f);

		// create a line to show the bounds	
		LineString boundsBox = createBounds();
		f = Feature.fromGeometry((Geometry) boundsBox);
		fc.features().add(f);
		
		System.out.println(fc.toJson());
		*/
    
	}
	/*
	private static URL createUrl(String day, String month, String year) throws MalformedURLException {
		//http://homepages.inf.ed.ac.uk/stg/powergrab/2019/09/15/powergrabmap.geojson
    	String mapString = String.format("http://homepages.inf.ed.ac.uk/stg/powergrab/%s/%s/%s/powergrabmap.geojson",year,month,day);
    	URL mapUrl = new URL(mapString);
    	return mapUrl;
	}*/
	

	
}
	