package uk.ac.ed.inf.powergrab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;

public class ChargingStation {
	public Feature f;
	public double coins;
	public double power;
	public String marker;
	
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
	
	public static String get_coins(Feature f) {
		return f.getProperty("coins").toString();
	}
	
	public static String get_power(Feature f) {
		return f.getProperty("power").toString();
	}
	
	public static String get_marker(Feature f) {
		return f.getProperty("marker-symbol").toString();
	}
	
	Map<String,Object> getStationMap() {
        Map<String,Object> stationData = new HashMap<>();
        stationData.put("coins", this.coins);
        stationData.put("power", this.power);
        return stationData;
    }
	
	protected void setCoins(Feature f, Double coins) { this.coins = coins;}
	protected void setPower(Feature f, Double power) { this.power = power;}
	
	public static void main(String[] args) throws IOException {
		//String day = args[0];
    	//String month = args[1];
    	//String year = args[2];
    	//FeatureCollection fc = parseGeoJSON(day, month, year);
	}
	
	protected List Add_Stations_List() {
		List <ChargingStation> Stations = new ArrayList <ChargingStation>();
		for (Feature ft : App.fc.features()) {
			ChargingStation c = new ChargingStation();
			c.coins = Double.parseDouble(ft.getProperty("coins").toString());
			c.power = Double.parseDouble(ft.getProperty("power").toString());
			Stations.add(c);
		}
		return Stations;
	}
	protected List Stations_List(Feature f) {
		List <ChargingStation> Stations = Add_Stations_List <ChargingStation>();
		Integer count=0;
		for (Feature ft: App.fc.features()) {
			count++;
			if (ft == f){
				ChargingStation c = new ChargingStation();
				c.coins = Double.parseDouble(f.getProperty("coins").toString());
				c.power = Double.parseDouble(f.getProperty("power").toString());
				Stations.add(count, c);

			}

		}
		return Stations;
	}
	
}
