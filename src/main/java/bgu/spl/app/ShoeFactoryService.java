package bgu.spl.app;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.MessageBusImpl;

public class ShoeFactoryService extends MicroService {
	
	private ConcurrentLinkedQueue<ManufacturingOrderRequest> requests;
	private AtomicInteger currentTick;
	private int manufacturedAmount;
	private ManufacturingOrderRequest currentRequest = null;

	/**
	 * 
	 * @param name - the name of the micro-service
	 * @param latch - the count down latch
	 */
	public ShoeFactoryService(String name, CountDownLatch latch) {
		super(name);
		this.latch = latch;
		requests = new ConcurrentLinkedQueue<ManufacturingOrderRequest>();
		currentTick = new AtomicInteger(0);
		manufacturedAmount = 0;
	}

	/**
	* registers this micro-service to the message bus
	 * subscribes this micro service to manufacture order requests 
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
			//ShoeStoreRunner.logger.info(this.getName()+ " received tick "+currentTick.get());
			if(currentRequest != null){
				if(manufacturedAmount< currentRequest.getAmountToOrder()){
					manufacturedAmount++;
					ShoeStoreRunner.logger.info(this.getName()+ " manufactured "+ currentRequest.getType());
				}
				else {
					manufacturedAmount = 0;
					ShoeStoreRunner.logger.info(this.getName()+" is sending a receipt to the manager for "+currentRequest.getType());
					Receipt r = new Receipt(this.getName(), "manager", currentRequest.getType(),
							false, currentTick.get(), currentRequest.getTick(), currentRequest.getAmountToOrder());
					complete(currentRequest, r);
					currentRequest = requests.poll();
					if(currentRequest != null)
					{
						manufacturedAmount++;
						ShoeStoreRunner.logger.info(this.getName()+ " manufactured "+ currentRequest.getType());
					}
				}
			}
		});
		subscribeRequest(ManufacturingOrderRequest.class, manufactureRequest -> {
			requests.add(manufactureRequest);	
			if(currentRequest == null)
				currentRequest = requests.poll();
			ShoeStoreRunner.logger.info(this.getName()+ " received manufacturing request for "+manufactureRequest.getType());
		});
		latch.countDown();
		ShoeStoreRunner.logger.info(this.getName() + " has been initialized");
	}

}
