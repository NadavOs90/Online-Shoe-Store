package bgu.spl.app;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.impl.MessageBusImpl;

public class WebsiteClientService extends MicroService {
	
	private ConcurrentHashMap<Integer, ConcurrentLinkedQueue<PurchaseSchedule>> purchaseSchedule;
	private ConcurrentHashMap<String,Boolean> wishList;
	private AtomicInteger currentTick;
	private ConcurrentLinkedQueue<Receipt> receipts;
	
	/**
	 * constructor
	 * @param name - the name of the client
	 * @param list - the clients purchase schedule that will be sorted by their ticks
	 * @param wish - the clients wish list
	 * @param latch - the count down latch
	 */
	public WebsiteClientService(String name,  List<PurchaseSchedule> list, Set<String> wish, CountDownLatch latch) {
		super(name);
		this.latch = latch;
		currentTick = new AtomicInteger(0);
		receipts = new ConcurrentLinkedQueue<Receipt>();
		purchaseSchedule = new ConcurrentHashMap<Integer, ConcurrentLinkedQueue<PurchaseSchedule>>(list.size());
		wishList = new ConcurrentHashMap<String,Boolean>(wish.size());
		Iterator<String> t = wish.iterator();
		while(t.hasNext()){
			String temp = t.next();
			wishList.put(temp,true);
		}
		for(int i=0; i<list.size(); i++)
			if (purchaseSchedule.containsKey(list.get(i).getTick()))
				purchaseSchedule.get(list.get(i).getTick()).add(list.get(i));
			else{
				purchaseSchedule.put(list.get(i).getTick(), new ConcurrentLinkedQueue<PurchaseSchedule>());
				purchaseSchedule.get(list.get(i).getTick()).add(list.get(i));
			}
		
	}

	/**
	 * registers this micro-service to the message bus
	 * subscribes this micro service to new discount broadcasts
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
			if(purchaseSchedule.containsKey(currentTick.get())){
				ConcurrentLinkedQueue<PurchaseSchedule> tempList = new ConcurrentLinkedQueue<PurchaseSchedule>(purchaseSchedule.get(currentTick.get()));
				while(!tempList.isEmpty()){
					PurchaseSchedule temp = tempList.poll();
					ShoeStoreRunner.logger.info(this.getName()+ " is sending a purchase request for "+ temp.getType());
					Request<Receipt> rq= new PurchaseOrderRequest(temp.getType(),false, this, currentTick.get());
					sendRequest(rq, receipt ->{
						purchaseSchedule.get(((PurchaseOrderRequest) rq).getTick()).poll();
						if (purchaseSchedule.get(((PurchaseOrderRequest) rq).getTick()).isEmpty())
							purchaseSchedule.remove(((PurchaseOrderRequest) rq).getTick());
						if (receipt != null){
							ShoeStoreRunner.logger.info(this.getName()+ " received a receipt for "+ receipt.getShoeType());
							receipts.add(receipt);
						}
						if (purchaseSchedule.isEmpty() && wishList.isEmpty()){
							ShoeStoreRunner.logger.info (this.getName()+ " has finished shopping");
							terminate();
							ShoeStoreRunner.logger.info(this.getName()+" terminated");
							MessageBusImpl.getInstance().unregister(this);
							ShoeStoreRunner.logger.info(this.getName()+" unregistered");
						}
					});
				}
			}
		});
		subscribeBroadcast(NewDiscountBroadcast.class, discountCallback ->{
			ShoeStoreRunner.logger.info (this.getName()+ " received a discount broadcast for "+ discountCallback.getType());
			if (wishList.containsKey(discountCallback.getType()) && wishList.get(discountCallback.getType())){
				ShoeStoreRunner.logger.info(this.getName()+ " is sending a purchase request for "+ discountCallback.getType());
				Request<Receipt> rq= new PurchaseOrderRequest(discountCallback.getType(),true, this, currentTick.get());
				wishList.put(discountCallback.getType(), false);
				sendRequest(rq, receipt ->{
					if (receipt != null){
						wishList.remove(discountCallback.getType());
						ShoeStoreRunner.logger.info(this.getName()+ " received a receipt for "+ receipt.getShoeType());
						receipts.add(receipt);
					}
					else
						wishList.put(discountCallback.getType(), true);
					if (purchaseSchedule.isEmpty() && wishList.isEmpty()){
						ShoeStoreRunner.logger.info (this.getName()+ " has finished shopping");
						terminate();
						ShoeStoreRunner.logger.info(this.getName()+" terminated");
						MessageBusImpl.getInstance().unregister(this);
						ShoeStoreRunner.logger.info(this.getName()+" unregistered");
					}
				});
			}
		});
		ShoeStoreRunner.logger.info(this.getName() + " has been initialized");
		latch.countDown();
	}

}
