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
    	
    	String mapString = String.format("http://homepages.inf.ed.ac.uk/stg/powergrab/%s/%s/%s/powergrabmap.geojson",
				year, month, day);
    	
    	new Game(mapString);
    	// Prepare charging station
    	FeatureCollection fc = parseGeoJSON(day, month, year);
    	ArrayList<Feature> features = (ArrayList<Feature>) fc.features();
    	
    	List <ChargingStation> Stations = Create_Stations_List(fc);
		Position initPos = new Position(lat, lon);

		//System.out.print(drone_type.toCharArray());
    	if(initPos.inPlayArea()) {

			String path= "";
			String map = "";
			String txt="";
			if (drone_type.contains("stateless")) {
				System.out.print("Start Game");
				System.out.print(initPos.latitude);
				System.out.print(initPos.longitude);
	
				//System.out.println(Stations);
				//Position currentPos, Integer moves, Double coins, Double power, Integer seed, List <ChargingStation> Stations,
				StatelessDrone stateless = new StatelessDrone(initPos, 0.0, 0.0,seed, Stations);
				stateless.startGame(lat, lon);
				map =converttofile(stateless.movesHistory, features);
				txt=stateless.totxt();
				path="stateless"+day+month+year;
			} 
			//String filepath = "/afs/inf.ed.ac.uk/user/s17/s1705544/Documents/powergrab/"+path;
			String filepath = "C:\\Users\\Jenny\\Downloads\\"+path;

			PrintWriter writer1 = new PrintWriter(filepath + ".geojson");
			writer1.println(map);
			writer1.close();
			PrintWriter writer2 = new PrintWriter(filepath + ".txt");
			writer2.println(txt);
			writer2.close();
			System.out.print("game over");
    	}
	}

	 private static String converttofile(ArrayList<Point> movesHistory, ArrayList<Feature> allFeatures ){
	    	ArrayList<Point> pointsMoved = new ArrayList<Point>();
			String jsonfile = "";
			jsonfile += "{\n" + 
					"  \"type\": \"FeatureCollection\",\n" + 
					"  \"date-generated\": \"Sun Sep 15 2019\",\n" + 
					"  \"features\": [\n" + 
					"    \n" + 
					"    \n" + 
					"      {\n" + 
					"      \"type\": \"Feature\",\n" + 
					"      \"geometry\": {\n" + 
					"        \"type\": \"LineString\",\n" + 
					"        \"coordinates\": [" ;
			for (int i=0; i<movesHistory.size()-1;i++) {
				jsonfile +=  movesHistory.get(i).coordinates() + ", ";
			}
			jsonfile+= movesHistory.get(movesHistory.size()-1).coordinates() + "] },\n" + 
					"      \"properties\": {\n" + 
					"        \"prop0\": \"value0\",\n" + 
					"        \"prop1\": 0.0\n" + 
					"      }\n" + 
					"    },";
			for (int x =0; x< allFeatures.size()-1; x++)
			{
				jsonfile+= allFeatures.get(x).toJson() + ",";
			}
			jsonfile+= allFeatures.get(allFeatures.size()-1).toJson() + "]}";
			return jsonfile;
		}

	
	
	
}
	