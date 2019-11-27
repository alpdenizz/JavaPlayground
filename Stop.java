
public class Stop {
	private String stopName;
	private double latitude;
	private double longitude;
	
	public Stop(String stopName, double latitude, double longitude) {
		this.stopName = stopName;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public String getStopName() {
		return stopName;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
}
