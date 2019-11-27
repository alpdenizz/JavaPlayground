import java.util.List;


public class Route {
	private String routeName;
	private List<Stop> stopList;
	private List<Schedule> scheduleOfStops;
	
	public Route (String routeName, List<Stop> stopList, List<Schedule> scheduleOfStops) {
		this.routeName = routeName;
		this.stopList = stopList;
		this.scheduleOfStops = scheduleOfStops;
	}
	
	public String getRouteName() {
		return routeName;
	}

	public List<Stop> getStopList() {
		return stopList;
	}

	public List<Schedule> getScheduleOfStops() {
		return scheduleOfStops;
	}
	
}