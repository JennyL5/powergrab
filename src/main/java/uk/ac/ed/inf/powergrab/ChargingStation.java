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
	public double get_coins() {
		//return f.getProperty("coins").toString();
		return this.coins;
	}
	
	public double get_power() {
		return this.power;
	}
	
	public String get_marker() {
		return this.marker;
	}
	
	public Position pos() {
		return this.pos;
	}
	
	protected void setCoins(Double coins) { this.coins = coins;}
	protected void setPower(Double power) { this.power = power;}
	protected void setMarker(String marker) { this.marker = marker;}
	protected void setPos(Position pos) { this.pos = pos;}
	
	Map<String,Object> getStationMap() {
        Map<String,Object> stationData = new HashMap<>();
        stationData.put("coins", this.coins);
        stationData.put("power", this.power);
        return stationData;
    }
	
	
}
