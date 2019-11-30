package uk.ac.ed.inf.powergrab;

public class ChargingStation {
	protected Position pos;
	protected double coins;
	protected double power;
	protected String marker;
	protected String colour;

	// getters
	protected double getCoins() {
		return this.coins;
	}

	protected double getPower() {
		return this.power;
	}

	protected String getMarker() {
		return this.marker;
	}

	protected Position getPos() {
		return this.pos;
	}

	protected String getColour() {
		return this.colour;
	}

	// setters
	protected void setCoins(Double coins) {
		this.coins = coins;
	}

	protected void setPower(Double power) {
		this.power = power;
	}

	protected void setMarker(String marker) {
		this.marker = marker;
	}

	protected void setPos(Position pos) {
		this.pos = pos;
	}

	protected void setColour(String colour) {
		this.colour = colour;
	}

}
