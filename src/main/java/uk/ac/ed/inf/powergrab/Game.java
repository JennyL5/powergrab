package uk.ac.ed.inf.powergrab;


import com.mapbox.geojson.*;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Game {
	private List <ChargingStation> Stations;
	private List <ChargingStation> goodStations;
	private List <ChargingStation> badStations;
	private boolean gameState;

	protected String url;
	protected FeatureCollection fc;
	protected String jsonMap;
	private List<Feature> allFeatures;

    Game(String url) {
        this.url = url;
        this.jsonMap = Game.getMap(this.url);
        this.fc = FeatureCollection.fromJson(this.jsonMap);
        this.allFeatures = FeatureCollection.fromJson(this.jsonMap).features();
    }
    

	 private static String getMap(String urlString) {
	
	    StringBuilder result = new StringBuilder();
	    try {
	        URL mapURL = new URL(urlString);
	        HttpURLConnection conn = (HttpURLConnection) mapURL.openConnection();
	        conn.setReadTimeout(10000);
	        conn.setConnectTimeout(15000);
	        conn.setRequestMethod("GET");
	        conn.setDoInput(true);
	        conn.connect();
	
	        String line;
	        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        while ((line = reader.readLine()) != null) {
	            result.append(line);
	        }
	        reader.close();
	    } catch (IOException e){
	        e.printStackTrace();
	    }
	
	    return result.toString();
	}
	 
	 public  String convertToFile(ArrayList<Point> movesHistory ){
	        FeatureCollection full_map = FeatureCollection.fromFeatures(this.allFeatures);
	  
	        
	        ArrayList<Point> pointsMoved = new ArrayList<Point>();
			String jsonfile = "";
			jsonfile += "{\n" + 
					"  \"type\": \"FeatureCollection\",\n" + 
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
			for (int x =0; x< this.allFeatures.size()-1; x++)
			{
				jsonfile+= this.allFeatures.get(x).toJson() + ",";
			}
			jsonfile+= this.allFeatures.get(this.allFeatures.size()-1).toJson() + "]}";
			return jsonfile;
		}
	 /*
	  * public void writeToFile(String filename, String filepath, String contents) {
        try {
            FileWriter file = new FileWriter(filename);
            file.write(contents);
            file.close();
        }catch(Exception e) {
            System.out.println(e.toString());
        }
    }

	  */
	 
	 public String writeToFile(ArrayList<Point> movesHistory) {
	        FeatureCollection full_map = FeatureCollection.fromFeatures(this.allFeatures);
	        return "{\n" + fc + "\n" +
	        		"features=" + "[{" + "\n" +
	                     "type="+ "Feature" + "," +
	                     "geometry=" + "{" + "\n" +
	                       "type=" + "LineString" + "," +
	                       "coordinates=" + movesHistory +"\n" +
	        		//"type=" + fc.features() +	"\n" +        		
	        		'}';
	        		
	 }


}
