package uk.ac.ed.inf.powergrab;

/**
 * This class represents a charging station, and is used to get the features of
 * the charging stations faster. This convenience allows the rendered Geo-JSON
 * features to be accessed more conveniently.
 * 
 * @author Jenny
 *
 */
public class ChargingStation {
	/**
	 * Represents the charging station's position, coins, power, and marker.
	 * 
	 */
	protected Position pos;
	protected Double coins;
	protected Double power;
	protected String marker;

	/**
	 * Gets the charging stations's coins
	 * 
	 * @return this.coins a Double
	 */
	protected Double getCoins() {
		return this.coins;
	}

	/**
	 * Gets the charging stations's power
	 * 
	 * @return this.power a Double
	 */
	protected Double getPower() {
		return this.power;
	}

	/**
	 * Gets the charging stations's marker
	 * 
	 * @return this.marker a String
	 */
	protected String getMarker() {
		return this.marker;
	}

	/**
	 * Gets the charging stations's position
	 * 
	 * @return this.pos a Position
	 */
	protected Position getPos() {
		return this.pos;
	}

	/**
	 * Sets the charging stations's coins
	 * 
	 * @param coins a Double
	 */
	protected void setCoins(Double coins) {
		this.coins = coins;
	}

	/**
	 * Sets the charging stations's power
	 * 
	 * @param power a Double
	 */
	protected void setPower(Double power) {
		this.power = power;
	}

	/**
	 * Sets the charging stations's position
	 * 
	 * @param pos a Position
	 */
	protected void setPos(Position pos) {
		this.pos = pos;
	}

	protected void setMarker(String marker) {
		this.marker = marker;
	}

}
