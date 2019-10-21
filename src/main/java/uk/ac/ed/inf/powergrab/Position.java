package uk.ac.ed.inf.powergrab;
import java.lang.Math;
import java.util.HashMap;

public class Position {   
	
	public double latitude;
	public double longitude;
	
	private static final double r = 0.0003;
    private static final double h2 = r*Math.sin(Math.toRadians(67.5));
	private static final double h3 = r*Math.sin(Math.toRadians(45));
	private static final double h4 = r*Math.sin(Math.toRadians(22.5));
    private static final double w2 = h4; 
    private static final double w3 = h3; 
    private static final double w4 = h2; 
	
	public Position(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	
	public Position nextPosition(Direction direction) {
		// Returns the next position of the drone when it makes a move in the specified compass direction

		// return position of one of the 16 possible positions
		switch(direction) {
			case N:
				return new Position(this.latitude+r, this.longitude); 
			case NNE:
				return new Position(this.latitude+h2, this.longitude+w2);
			case NE:
				return new Position(this.latitude+h3, this.longitude+w3);
			case ENE:
				return new Position(this.latitude+h4, this.longitude+w4);
			case E:
				return new Position(this.latitude, this.longitude+r);
			case ESE:
				return new Position(this.latitude-h4, this.longitude+w4);
			case SE:
				return new Position(this.latitude-h3, this.longitude+w3); 
			case SSE:
				return new Position(this.latitude-h2, this.longitude+w2); 
			case S: 
				return new Position(this.latitude-r, this.longitude);
			case SSW:
				return new Position(this.latitude-h2, this.longitude-w2); 
			case SW:
				return new Position(this.latitude-h3, this.longitude-w3); 
			case WSW:
				return new Position(this.latitude-h4, this.longitude-w4);
			case W:
				return new Position(this.latitude, this.longitude-r);
			case WNW:
				return new Position(this.latitude+h4, this.longitude-w4);
			case NW:
				return new Position(this.latitude+h3, this.longitude-w3);
			case NNW:
				return new Position(this.latitude+h2, this.longitude-w2);
			default:
				return this;
		}
		
	}

		
	public boolean inPlayArea() {
		// Checks whether or not this Position lies in the PowerGrab play area 
		return 55.942617 < this.latitude && this.latitude < 55.946233 && -3.192473 < this.longitude && this.longitude < -3.184319;
	}
}