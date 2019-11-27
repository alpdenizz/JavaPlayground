import java.text.SimpleDateFormat;
import java.util.List;
import java.io.File;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Date;


public class TripPlanner {

	public static String findDirectRoute(String departure, String arrival, String given_date, String given_time) {
		// TODO Auto-generated method stub
		String departure_id = departure;
		String arrival_id = arrival;
		String date = given_date;
		String time = given_time;
		
		
		try {
			List<Trip> allTrips = getAllTrips();
			List<Trip> possibleTrips = new LinkedList<Trip>();
			
			for(int i=0; i<allTrips.size(); i++) {
				//allTrips.get(i).print();
				List<Integer> indexes = allTrips.get(i).findStops(departure_id, arrival_id);
				if (!indexes.isEmpty()) {
					possibleTrips.add(allTrips.get(i));
				}
			} 
			//System.out.println("################### DONE ###############################");
			int dayOfWeek = dayOfWeek(date);
			
			List<Trip> filteredPossibleTrips = new LinkedList<Trip>();
			
			if(!possibleTrips.isEmpty()) {
			
			for(int i=0; i<possibleTrips.size(); i++) {
				boolean hasNext = (possibleTrips.get(i).hasNextDeparture(departure_id, time));
				//System.out.println("################CASE: "+hasNext+" ##################");
				if(hasNext) {
					filteredPossibleTrips.add(possibleTrips.get(i));
				}
			}
			
			Trip found = null;
			for(int i=0; i<filteredPossibleTrips.size(); i++) {
				//filteredPossibleTrips.get(i).print();
				if(filteredPossibleTrips.get(i).isAvailableTrip(dayOfWeek, date)){
					found = filteredPossibleTrips.get(i);
					break;
				}
			}
			
			return generateMessage(found, departure_id, arrival_id);
			}
			else {
				return generateError();
			}
			
		}
		catch(Exception e){
			return generateError();
		}
		
	}
	
	public static List<Trip> getAllTrips() throws Exception {
		File file = new File("./gtfs/stop_times.txt");
		Scanner sc = new Scanner(file);
		List<Trip> allTrips = new LinkedList<Trip>();
		sc.nextLine();
		String secondLine = sc.nextLine();
		StringTokenizer st2 = new StringTokenizer(secondLine,",");
		
		List<String> arrivalList = new LinkedList<String>();
		List<String> departureList = new LinkedList<String>();
		List<String> stopList = new LinkedList<String>();
		
		String tripId = st2.nextToken();
		allTrips.add(new Trip(tripId));
		arrivalList.add(st2.nextToken());
		departureList.add(st2.nextToken());
		stopList.add(st2.nextToken());
		
		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			StringTokenizer st = new StringTokenizer(line,",");
			String tID = st.nextToken();
			if (tripId.equals(tID)) {
				arrivalList.add(st.nextToken());
				departureList.add(st.nextToken());
				stopList.add(st.nextToken());
			}
			else {
				allTrips.get(allTrips.size()-1).setArrivalTimes(arrivalList);
				allTrips.get(allTrips.size()-1).setDepartureTimes(departureList);
				allTrips.get(allTrips.size()-1).setStopList(stopList);
				arrivalList = new LinkedList<String>();
				departureList = new LinkedList<String>();
				stopList = new LinkedList<String>();
				tripId = tID;
				allTrips.add(new Trip(tripId));
				arrivalList.add(st.nextToken());
				departureList.add(st.nextToken());
				stopList.add(st.nextToken());
				
			}
		}
		allTrips.get(allTrips.size()-1).setArrivalTimes(arrivalList);
		allTrips.get(allTrips.size()-1).setDepartureTimes(departureList);
		allTrips.get(allTrips.size()-1).setStopList(stopList);
		sc.close();
		return allTrips;
		
	}
	
	public static boolean isBelongToSameRoute(Trip t1, Trip t2) {

		List<String> stopList1 = t1.getStopList();
		List<String> stopList2 = t2.getStopList();
		if(stopList1.size() != stopList2.size()) {
			return false;
		}
		else {
			for(int i=0; i<stopList1.size(); i++){
				if( !(stopList1.get(i).equals(stopList2.get(i))) ) return false;
			}
			return true;
		}
	}

	public static void test(List<Trip> allTrips) {
		for(int i=0; i<allTrips.size(); i++) {
			for(int j=i+1; j<allTrips.size(); j++) {
				System.out.println(i+","+j);
				if(isBelongToSameRoute(allTrips.get(i),allTrips.get(j))) {
					System.out.println("TRIP: "+allTrips.get(i).getIdentifier()+" and TRIP: "+allTrips.get(j).getIdentifier()+" are same");
				}
				else {
					System.out.println("TRIP: "+allTrips.get(i).getIdentifier()+" and TRIP: "+allTrips.get(j).getIdentifier()+" are different");
				}
			}
		}
	}
	
	public static int dayOfWeek(String date) throws Exception {
		Calendar c = Calendar.getInstance();
		String input_date=date;
		SimpleDateFormat format1=new SimpleDateFormat("yyyy-MM-dd");
		Date dt1=format1.parse(input_date);
		c.setTime(dt1);
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == 1) return 7;
		else return (dayOfWeek - 1);
	}
	
	public static String generateMessage(Trip trip, String departure_id, String arrival_id) throws Exception{
		if (trip == null) {
			return "There are no direct routes from departure stop to arrival stop";
		}
		else {
			String message = "";
			message += trip.getRouteName()+"\n";
			message += "DEPARTURE TIME FROM GIVEN DEPARTURE STOP: "+trip.getDepartureTime(departure_id)+"\n";
			message += "ARRIVAL TIME TO GIVEN ARRIVAL STOP: "+trip.getArrivalTime(arrival_id)+"\n";
			return message;
		}
	}
	
	public static String generateError() {
		return "There are no direct routes from departure stop to arrival stop";
	}
}
