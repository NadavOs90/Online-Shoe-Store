package bgu.spl.app;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import com.google.gson.Gson;
import bgu.spl.mics.impl.MessageBusImpl;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ShoeStoreRunner {
	
	public static Logger logger = Logger.getLogger(ShoeStoreRunner.class.getName());
	
	/**
	 * the main function
	 * first reads the JSON file
	 * Initiates and creates the threads according to the JSON FILE
	 * at the end prints all the receipts from the store
	 * and terminates gracefully all the threads
	 */

	public static void main(String[] args){
		Gson gson = new Gson();
		JsonObject obj = null;
		int serviceCounter = 0; // the amount of micro services counter.
		BufferedReader jsonFile = null;	
		try{
			jsonFile = new BufferedReader(new FileReader(args[0]));
			obj = gson.fromJson(jsonFile, JsonObject.class);
			jsonFile.close();
			logger.info("json file created.");
		} catch (IOException e){
			logger.severe("NO SUCH FILE!!");
		} 
		serviceCounter= obj.getServices().getFactories()+obj.getServices().getSellers()+obj.getServices().getCustomers().length+1;
		CountDownLatch latch = new CountDownLatch(serviceCounter); // a count down latch to assure that the timer starts last.
		Store.getInstance().load(obj.getInitialStorage());
		logger.info("The store has been loaded");
		for (int i=0; i<obj.getServices().getFactories(); i++){
			Runnable factory= new ShoeFactoryService("factory "+i, latch);
			Thread tF = new Thread(factory);
			tF.start();
		}
		logger.info("The factories have been created");
		for (int i=0; i<obj.getServices().getSellers(); i++){
			Runnable seller= new SellingService("seller "+i, latch);
			Thread t= new Thread(seller);
			t.start();
		}
		logger.info("The selling services have been created");
		Runnable manager= new ManagementService(obj.getServices().getManager().getDiscountSchedule(), latch);
		Thread tM= new Thread (manager);
		tM.start();
		logger.info("The manager has been created");
		Runnable time= new TimerService(obj.getServices().getTime().getSpeed(), obj.getServices().getTime().getDuration(), latch);
		Thread tT=new Thread (time);
		tT.start();
		logger.info("The timer has been created");
		for (int i=0; i<obj.getServices().getCustomers().length; i++){
			Runnable client= new WebsiteClientService(obj.getServices().getCustomers()[i].getName(),
					obj.getServices().getCustomers()[i].getPurchaseSchedule(),
					obj.getServices().getCustomers()[i].getWishList(), latch);
			Thread tC= new Thread(client);
			tC.start();
		}
		logger.info("The clients have been created");
		//waits for all the micro-services to gracefully terminate
		synchronized (MessageBusImpl.getInstance()){
			while(!MessageBusImpl.getInstance().done())
				try {
					MessageBusImpl.getInstance().wait();
				} catch (InterruptedException e) {logger.severe("main interrupted before the end of the program");}
		}
		
		System.out.println("\nEnd of the day");
		System.out.println("\nPrinting the store files\n");
		Store.getInstance().print();
		System.out.println("\nSystem shutdown good bye =]");
		System.exit(0);
	}

}
