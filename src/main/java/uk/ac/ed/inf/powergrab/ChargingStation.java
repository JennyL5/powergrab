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
	public Position pos;
	public double coins;
	public double power;
	public String marker;
	List <ChargingStation> Stations = new ArrayList <ChargingStation>();
	public double get_coins(ChargingStation f) {
		//return f.getProperty("coins").toString();
		return f.coins;
	}
	
	public double get_power(ChargingStation f) {
		return f.power;
	}
	
	public String get_marker(ChargingStation f) {
		return f.marker;
	}
	
	public Position pos(ChargingStation f) {
		return f.pos;
	}
	
	protected void setCoins(ChargingStation f, Double coins) { f.coins = coins;}
	protected void setPower(ChargingStation f, Double power) { f.power = power;}
	protected void setMarker(ChargingStation f, String marker) { this.marker = marker;}
	protected void setPos(ChargingStation f, Position pos) { this.pos = pos;}
	
	Map<String,Object> getStationMap() {
        Map<String,Object> stationData = new HashMap<>();
        stationData.put("coins", this.coins);
        stationData.put("power", this.power);
        return stationData;
    }
	
	
}
