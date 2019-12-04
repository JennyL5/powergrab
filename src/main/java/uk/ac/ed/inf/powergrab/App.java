package uk.ac.ed.inf.powergrab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;

/**
 * Represents an App. This is the main class which calls the methods from the
 * other classes.
 * 
 * @author s1705544
 *
 */
public class App {
	public static FeatureCollection fc;

	/**
	 * Creates a list of all the stations from the features of the feature
	 * collection. It gets the charging stations's coins, power, marker and
	 * position.
	 * 
	 * @param fc a feature collection
	 * @result Stations a list of all charging stations
	 */
	protected static List<ChargingStation> createStationsList(FeatureCollection fc) {
		List<ChargingStation> stations = new ArrayList<ChargingStation>();
		for (Feature f : fc.features()) {
			ChargingStation c = new ChargingStation();
			c.setCoins(f.getProperty("coins").getAsDouble());
			c.setPower(f.getProperty("power").getAsDouble());
			c.setMarker(f.getProperty("marker-symbol").getAsString());
			Point point = (Point) f.geometry();
			Position p = new Position(point.latitude(), point.longitude());
			c.setPos(p);
			stations.add(c);
		}
		return stations;
	}

	/**
	 * The main method that will run. It gets the arguments and splits them into
	 * day, month, year, latitude, longitude, seed, and drone type to play the game.
	 * Once the game is finished, this writes the results to text file and json
	 * file.
	 * 
	 * @param args[] string arguments passed in
	 * @throws IllegalArgumentException
	 */
	public static void main(String[] args) throws IOException {
		try {
			String day = args[0];
			String month = args[1];
			String year = args[2];
			Double lat = Double.parseDouble(args[3]);
			Double lon = Double.parseDouble(args[4]);
			Integer seed = Integer.parseInt(args[5]);
			String drone_type = args[6].toString();
			String mapString = String.format(
					"http://homepages.inf.ed.ac.uk/stg/powergrab/%s/%s/%s/powergrabmap.geojson", year, month, day);

			Game game = new Game(mapString);

			FeatureCollection fc = game.fc;

			List<ChargingStation> Stations = createStationsList(fc);

			Position initPos = new Position(lat, lon);

			String path = "";
			String map = "";
			String txt = "";
			if (drone_type.equals("stateless")) {
				System.out.println("Start Game for stateless drone");
				StatelessDrone stateless = new StatelessDrone(initPos, seed, Stations);
				stateless.startGame();
				map = game.prepareJson(stateless.movesHistory);
				txt = stateless.totxt();
				path = "stateless" + "-" + day + "-" + month + "-" + year;
			} else {
				System.out.println("Start Game for stateful drone");
				StatefulDrone stateful = new StatefulDrone(initPos, seed, Stations);
				stateful.startGame();
				map = game.prepareJson(stateful.movesHistory);
				txt = stateful.totxt();
				path = "stateful" + "-" + day + "-" + month + "-" + year;
			}

			game.writeToFile(System.getProperty("user.dir") + "/" + path + ".geojson", map);
			game.writeToFile(System.getProperty("user.dir") + "/" + path + ".txt", txt);
		} catch (IllegalArgumentException e) {
			System.out.print(e);
		} catch (Exception e) {
			System.out.print(e);
		}

	}

}
