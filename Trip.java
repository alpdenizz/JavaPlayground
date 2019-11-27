import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;


public class Trip {
	private String identifier;
	private List<String> departureTimes;
	private List<String> arrivalTimes;
	private List<String> stopList;
	
	public Trip(String id, List<String> deps, List<String> arrs, List<String> stopList) {
		this.setIdentifier(id);
		this.setDepartureTimes(deps);
		this.setArrivalTimes(arrs);
		this.setStopList(stopList);
	}
	
	public Trip(String id){
		this.identifier = id;
	}

	public String getIdentifier() {
		return identifier;
	}

	private void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public List<String> getArrivalTimes() {
		return arrivalTimes;
	}

	public void setArrivalTimes(List<String> arrivalTimes) {
		this.arrivalTimes = arrivalTimes;
	}

	public List<String> getDepartureTimes() {
		return departureTimes;
	}

	public void setDepartureTimes(List<String> departureTimes) {
		this.departureTimes = departureTimes;
	}

	public List<String> getStopList() {
		return stopList;
	}

	public void setStopList(List<String> stopList) {
		this.stopList = stopList;
	}
	
	public List<Integer> findStops(String departure_id, String arrival_id) {
		int index1 = -1;
		int index2 = -1;
		List<Integer> indexes = new LinkedList<Integer>();
		for(int i=0; i<stopList.size(); i++) {
			String stop = stopList.get(i);
			if (stop.equals(departure_id)) index1 = i;
			if (stop.equals(arrival_id)) index2 = i;
		}
		
		if(index1 != -1 && index2 != -1 && index1 < index2) {
			indexes.add(index1);
			indexes.add(index2);
		}
		
		return indexes;
	}
	
	public boolean hasNextDeparture(String departure_id, String time) throws Exception {
		int index = this.stopList.indexOf(departure_id);
		String departureTime = this.departureTimes.get(index);
		departureTime = departureTime.substring(0, departureTime.length()-3);
		SimpleDateFormat parser = new SimpleDateFormat("HH:mm");
		//System.out.println("MY TIME: "+time);
		//System.out.println("DEPARTURE TIME: "+departureTime);
		Date date1 = parser.parse(departureTime);
		Date date2 = parser.parse(time);
		return date1.after(date2);
	}
	
	public String getServiceId() throws Exception {
		File file = new File("./gtfs/trips.txt");
		Scanner sc = new Scanner(file);
		sc.nextLine();
		String serviceId = "";
		while(sc.hasNextLine()) {
			StringTokenizer st = new StringTokenizer(sc.nextLine(),",");
			st.nextToken();
			String sid = st.nextToken();
			String tripId = st.nextToken();
			if(tripId.equals(this.identifier)) {
				serviceId = sid;
				break;
			}
		}
		sc.close();
		return serviceId;
	}
	
	public boolean isAvailableTrip(int dayOfWeek, String date) throws Exception {
		
		File file2 = new File("./gtfs/calendar.txt");
		Scanner sc2 = new Scanner(file2);
		sc2.nextLine();
		String serviceId = getServiceId();
		
		while(sc2.hasNextLine()) {
			StringTokenizer st = new StringTokenizer(sc2.nextLine(),",");
			String sid = st.nextToken();
			if(sid.equals(serviceId)) {
			String[] days = new String[7];
			for(int i=0; i<7; i++) {
				days[i] = st.nextToken();
			}
			
			if(days[dayOfWeek-1].equals("0")) return false;
			else {
				String startDate = st.nextToken();
				String endDate = st.nextToken();
				startDate = startDate.substring(0, 4) + "-" + startDate.substring(4,6) + "-" + startDate.substring(6,8);
				endDate = endDate.substring(0, 4) + "-" + endDate.substring(4,6) + "-" + endDate.substring(6,8);
				
				SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
				if(date.equals(startDate) || date.equals(endDate)) return true;
				else {
				Date date1 = parser.parse(date);
				Date startD = parser.parse(startDate);
				Date endD = parser.parse(endDate);
				if(date1.after(startD) && date1.before(endD)) return true;
				else return false;
				}
			}
 		  }
		}
		 return false;
	}

	public String getRouteId() throws Exception {
		File file = new File("./gtfs/trips.txt");
		Scanner sc = new Scanner(file);
		sc.nextLine();
		String routeId = "";
		while(sc.hasNextLine()) {
			StringTokenizer st = new StringTokenizer(sc.nextLine(),",");
			String rid = st.nextToken();
			st.nextToken();
			String tripId = st.nextToken();
			if(tripId.equals(this.identifier)) {
				routeId = rid;
				break;
			}
		}
		sc.close();
		return routeId;
	}

	public String getRouteName() throws Exception {
		File file = new File("./gtfs/routes.txt");
		Scanner sc = new Scanner(file);
		sc.nextLine();
		String routeShortName = "";
		String routeLongName = "";
		while(sc.hasNextLine()) {
			StringTokenizer st = new StringTokenizer(sc.nextLine(),",");
			String rid = st.nextToken();
			st.nextToken();
			routeShortName = st.nextToken();
			routeLongName = st.nextToken();
			if(rid.equals(getRouteId())) {
				break;
			}
		}
		sc.close();
		return routeShortName+" "+routeLongName;
	}

	public String getDepartureTime(String departure_id) {
		int index = this.stopList.indexOf(departure_id);	
		return this.departureTimes.get(index);
	}
	
	public String getArrivalTime(String arrival_id) {
		int index = this.stopList.indexOf(arrival_id);	
		return this.arrivalTimes.get(index);
	}

	public void print() {
		System.out.println("################################");
		System.out.println("TRIP ID: "+this.identifier);
		/*for(int i=0; i<this.arrivalTimes.size(); i++) {
			System.out.print(this.arrivalTimes.get(i)+" "+this.departureTimes.get(i)+" "+this.stopList.get(i));
			System.out.println();
		}*/
		System.out.println("#################################");
	}
}
