package uk.ac.ed.inf.powergrab;

public class ChargingStation {
	public Position pos;
	public double coins;
	public double power;
	public String marker;
	public String colour;
	
	// getters
	public double getCoins() {
		return this.coins;
	}
	
	public double getPower() {
		return this.power;
	}
	
	public String getMarker() {
		return this.marker;
	}
	
	public Position getPos() {
		return this.pos;
	}
	
	public String getColour() {
		return this.colour;
	}
	// setters
	protected void setCoins(Double coins) { this.coins = coins;}
	protected void setPower(Double power) { this.power = power;}
	protected void setMarker(String marker) { this.marker = marker;}
	protected void setPos(Position pos) { this.pos = pos;}
	protected void setColour(String colour) { this.colour = colour;}

	
	
}
