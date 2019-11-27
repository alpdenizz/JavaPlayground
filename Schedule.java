import java.util.List;


public class Schedule {
	private int dayOfWeek;
	private String departureTime;
	private List<Route> routeList;
	
	public Schedule (int dayOfWeek, String departureTime, List<Route> routeList) {
		this.dayOfWeek = dayOfWeek;
		this.departureTime = departureTime;
		this.routeList = routeList;
	}
	
	public int getDayOfWeek() {
		return dayOfWeek;
	}
	
	public String getDepartureTime() {
		return departureTime;
	}
	
	public List<Route> getRouteList() {
		return routeList;
	}
	
}
