package bgu.spl.app;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.MessageBusImpl;

public class SellingService extends MicroService {
	
	private AtomicInteger currentTick;
	
	/**
	 * 
	 * @param name - the name of the micro-service
	 * @param latch - the count down latch
	 */
	public SellingService(String name, CountDownLatch latch) {
		super(name);
		this.latch = latch;
		currentTick = new AtomicInteger(0);
	}
	
	/**
	 * registers this micro-service to the message bus
	 * subscribes this micro service to purchase order requests 
	 * subscribes this micro service to tick broadcasts
	 * subscribes this micro service to termination broadcasts 
	 */
	@Override
	protected void initialize() {
		MessageBusImpl.getInstance().register(this);
		ShoeStoreRunner.logger.info(this.getName()+" registered");
		subscribeBroadcast(TerminateBroadcast.class, termination ->{
			terminate();
			ShoeStoreRunner.logger.info(this.getName()+" terminated");
			MessageBusImpl.getInstance().unregister(this);
			ShoeStoreRunner.logger.info(this.getName()+" unregistered");
		});
		subscribeBroadcast(TickBroadcast.class, tick ->{
			currentTick.getAndSet(tick.getTick());
		});
		subscribeRequest(PurchaseOrderRequest.class, purchaseOrder -> {
			ShoeStoreRunner.logger.info(this.getName()+ " received a purchase request for "+ purchaseOrder.getType());
			BuyResult result = Store.getInstance().take(purchaseOrder.getType(),
					purchaseOrder.onDiscount());
			switch (result) {
	        case NOT_IN_STOCK:
	        	ShoeStoreRunner.logger.info(purchaseOrder.getType()+ " is not in stock");
	        	RestockRequest r = new RestockRequest(purchaseOrder.getType());
	        	ShoeStoreRunner.logger.info(this.getName()+ " is sending a restock request for " + purchaseOrder.getType());
	            sendRequest(r, stockSuccess ->{
	            	if(!stockSuccess)
	            		complete(purchaseOrder, null);
	            	else{
	            		ShoeStoreRunner.logger.info(this.getName()+ " is sending a receipt to " + purchaseOrder.getRequester().getName() + " for " + r.getType());
	            		Receipt receipt = new Receipt(this.getName(),
	            				purchaseOrder.getRequester().getName(), purchaseOrder.getType(),
	                			false, purchaseOrder.getTick(), currentTick.get(), 1);
	                	complete(purchaseOrder, receipt);
	                	Store.getInstance().file(receipt);	
	            	}
	            });
	            break;
	                
	        case NOT_ON_DISCOUNT:
	        	ShoeStoreRunner.logger.info(purchaseOrder.getType()+ " is not on discount");
	        	complete(purchaseOrder, null); 
	            break;
	                     
	        case REGULAR_PRICE:
	        	ShoeStoreRunner.logger.info(this.getName()+ " is sending a receipt to " + purchaseOrder.getRequester().getName() + " for " + purchaseOrder.getType());
	        	Receipt receipt = new Receipt(this.getName(),
	        			purchaseOrder.getRequester().getName(), purchaseOrder.getType(),
	        			false, purchaseOrder.getTick(), currentTick.get(), 1);
	        	complete(purchaseOrder, receipt);
	        	Store.getInstance().file(receipt);
	        	break;
	        	
	        case DISCOUNTED_PRICE:
	        	ShoeStoreRunner.logger.info(this.getName()+ " is sending a receipt to " + purchaseOrder.getRequester().getName() + " for " + purchaseOrder.getType());
	        	receipt = new Receipt(this.getName(),
	        			purchaseOrder.getRequester().getName(), purchaseOrder.getType(),
	        			true, purchaseOrder.getTick(), currentTick.get(), 1);
	        	complete(purchaseOrder, receipt);
	        	Store.getInstance().file(receipt); 
	            break;  
			}	
		});	
		latch.countDown();
		ShoeStoreRunner.logger.info(this.getName() + " has been initialized");
	}
	

}
