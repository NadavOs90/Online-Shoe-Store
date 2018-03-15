package bgu.spl.app;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.impl.MessageBusImpl;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class ManagementService extends MicroService {
	
	private ConcurrentHashMap<Integer,ConcurrentLinkedQueue<DiscountSchedule>> discountSchedule;
	private ConcurrentHashMap<String,AtomicInteger> onOrder;
	private ConcurrentHashMap<String,ConcurrentLinkedQueue<RestockRequest>> requests;
	private AtomicInteger currentTick;

	/**
	 * constructor
	 * @param list - contains the discount schedule
	 * adds the discounts to the database according to their tick
	 * @param latch - the count down latch 
	 */
	public ManagementService(List<DiscountSchedule> list, CountDownLatch latch) {
		super("manager");
		this.latch = latch;
		currentTick = new AtomicInteger(0);
		onOrder=new ConcurrentHashMap<String,AtomicInteger>();
		requests= new ConcurrentHashMap<String,ConcurrentLinkedQueue<RestockRequest>>();
		discountSchedule = new ConcurrentHashMap<Integer,ConcurrentLinkedQueue<DiscountSchedule>>(list.size());
		for(int i=0; i<list.size(); i++)
			if (discountSchedule.containsKey(list.get(i).getTick()))
				discountSchedule.get(list.get(i).getTick()).add(list.get(i));
			else{
				discountSchedule.put(list.get(i).getTick(), new ConcurrentLinkedQueue<DiscountSchedule>());
				discountSchedule.get(list.get(i).getTick()).add(list.get(i));
			}
		
	}
	/**
	 * registers this micro-service to the message bus
	 * subscribes this micro service to restock requests 
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
			if(discountSchedule.containsKey(currentTick.get())){
				ConcurrentLinkedQueue<DiscountSchedule> tempList = discountSchedule.get(currentTick.get());
				while(!tempList.isEmpty()){
					DiscountSchedule temp = tempList.poll();
					Store.getInstance().addDiscount(temp.getType(), temp.getAmount());
					ShoeStoreRunner.logger.info(this.getName()+ " added discounted amount for "+temp.getType());
					ShoeStoreRunner.logger.info(this.getName()+ " sent discount broadcast for "+temp.getType());
					sendBroadcast(new NewDiscountBroadcast(temp.getType()));
				}
			}
		});
		subscribeRequest(RestockRequest.class, resRequest -> {
			ShoeStoreRunner.logger.info(this.getName() + " received restock request for " + resRequest.getType());
			if (onOrder.containsKey(resRequest.getType())){
				ShoeStoreRunner.logger.info(resRequest.getType() + " was already ordered");
				onOrder.get(resRequest.getType()).decrementAndGet();
				if (onOrder.get(resRequest.getType()).get() == 0)
					onOrder.remove(resRequest.getType());
				requests.get(resRequest.getType()).add(resRequest);
			}
			else{
				if(currentTick.get()%5 != 0)
					onOrder.put(resRequest.getType(), new AtomicInteger(currentTick.get()%5));
				ShoeStoreRunner.logger.info(this.getName() + " sending new manufacturing request for " + resRequest.getType());
				Request<Receipt> rq=new ManufacturingOrderRequest(resRequest.getType(),(currentTick.get()%5)+1, currentTick.get());
				boolean orderReceived = sendRequest(rq, receipt ->{
					ShoeStoreRunner.logger.info(this.getName() + " received a receipt from factory");
					if(onOrder.containsKey(receipt.getShoeType()))
						Store.getInstance().add(receipt.getShoeType(), onOrder.get(receipt.getShoeType()).get());
					ShoeStoreRunner.logger.info(this.getName() + " added the manufactured amount to the store");
					onOrder.remove(receipt.getShoeType());
					Store.getInstance().file(receipt);
					ConcurrentLinkedQueue<RestockRequest> temp= requests.get(receipt.getShoeType());
					while (!temp.isEmpty()){
						MessageBusImpl.getInstance().complete(temp.remove(), true);
					}
				});
				if (!orderReceived){
					ShoeStoreRunner.logger.info("no factories to receive the manufacturing order");
					MessageBusImpl.getInstance().complete(resRequest, false);
				}
				else{
					if (!requests.containsKey(resRequest.getType()))
						requests.put(resRequest.getType(), new ConcurrentLinkedQueue<RestockRequest>());
					requests.get(resRequest.getType()).add(resRequest);
				}
			}
		});
		latch.countDown();
		ShoeStoreRunner.logger.info(this.getName() + " has been initialized");
	}

}
