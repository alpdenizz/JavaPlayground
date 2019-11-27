
public class Boundary {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String departure_id = args[0];
		String arrival_id = args[1];
		String date = args[2];
		String time = args[3];
		
		String message = TripPlanner.findDirectRoute(departure_id, arrival_id, date, time);
		System.out.println(message);
	}

}
