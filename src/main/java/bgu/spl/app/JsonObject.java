package bgu.spl.app;

import java.util.List;
import java.util.Set;

/**
 * an object that contains the JSON file settings and data
 */
public class JsonObject {

	private ShoeStorageInfo[] initialStorage;
	private Services services;
	
	public ShoeStorageInfo[] getInitialStorage(){return initialStorage;}
	public Services getServices(){return services;}
	
	public static class Services {
		
		private TimeInfo time;
		private ManagerInfo manager;
		private int factories;
		private int sellers;
		private ClientInfo[] customers;
		
		public TimeInfo getTime(){return time;}
		public ManagerInfo getManager(){return manager;}
		public int getFactories(){return factories;}
		public int getSellers(){return sellers;}
		public ClientInfo[] getCustomers(){return customers;}
		
		public static class TimeInfo{	
			private int speed;
			private int duration;	
			
			public int getSpeed(){return speed;}
			public int getDuration(){return duration;}
		}
		
		public static class ManagerInfo{
			private List<DiscountSchedule> discountSchedule;
			public List<DiscountSchedule> getDiscountSchedule(){return discountSchedule;}
		}
		
		public static class ClientInfo{
			private String name;
			private List<PurchaseSchedule> purchaseSchedule;
			private Set<String> wishList;
			public String getName(){return name;}
			public List<PurchaseSchedule> getPurchaseSchedule(){return purchaseSchedule;}
			public Set<String> getWishList(){return wishList;}
		}

	}

}
