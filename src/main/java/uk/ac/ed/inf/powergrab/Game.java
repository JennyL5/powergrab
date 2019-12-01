package uk.ac.ed.inf.powergrab;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

/**
 * This class represents the game, and gets the map, writes to file and prepare
 * JSON file when the game has not yet started or has ended.
 * 
 * @author Jenny
 *
 */
public class Game {
	/**
	 * Represents Game's url, feature collection, jsonMap and list of features.
	 */
	protected String url;
	protected FeatureCollection fc;
	protected String jsonMap;
	private List<Feature> allFeatures;

	/**
	 * Creates a game with the Geo-JSON map passes in as String url, with url,
	 * jsonMap, feature collection and features.
	 * 
	 * @param url : String
	 */
	Game(String url) {
		this.url = url;
		this.jsonMap = Game.getMap(this.url);
		this.fc = FeatureCollection.fromJson(this.jsonMap);
		this.allFeatures = FeatureCollection.fromJson(this.jsonMap).features();
	}

	/**
	 * Gets the geojson map by perform http get request for url and reading the
	 * input string.
	 * 
	 * @param urlString string of locatin of geojson map
	 * 
	 * @return String of the result
	 * @throws IOEcxception
	 */
	protected static String getMap(String urlString) {

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
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result.toString();
	}

	/**
	 * Formats the string content ready for writing to json file. It gets the
	 * features from feature collections and appends the moveHistory coordinates for
	 * the line string for geo json.
	 * 
	 * @param movesHistory an arraylist of coordinate points drone has moved
	 * @return string of content for writing
	 */
	protected String prepareJson(ArrayList<Point> movesHistory) {

		Geometry geometryFlightPath = LineString.fromLngLats(movesHistory);
		Feature flightPathFeature = Feature.fromGeometry(geometryFlightPath);
		this.allFeatures.add(flightPathFeature);

		FeatureCollection fullMap = FeatureCollection.fromFeatures(this.allFeatures);

		return fullMap.toJson();
	}

	/**
	 * Writes the content (geo-JSON map and text file) to files.
	 * 
	 * @param filename a string of the file name for content to be saved as.
	 * @param contents a string of the geo-JSON map or movement history of drone.
	 * @return string of content for writing
	 * @throws Exception
	 */
	protected void writeToFile(String filename, String contents) {
		try {
			FileWriter file = new FileWriter(filename);
			file.write(contents);
			file.close();
			System.out.println("writing " + filename);
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

}
