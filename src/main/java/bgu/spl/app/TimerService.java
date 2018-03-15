package bgu.spl.app;

import java.util.Timer;
import java.util.TimerTask;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.MessageBusImpl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TimerService extends MicroService {
	
	private final int SPEED;
	private final int DURATION;
	private Timer timer;
	private TimerTask schedule;

	/**
	 * constructor
	 * @param speed - the time in milliseconds between each "tick"
	 * @param duration - the tick on which to end the "day" 
	 * @param latch - the count down latch
	 */
	public TimerService(int speed, int duration, CountDownLatch latch) {
		super("timer");
		this.latch = latch;
		SPEED = speed;
		DURATION = duration;
		timer = new Timer("timer");
		schedule= new TimerTask(){
			private int currentTick = 1;
			@Override
			public void run() {
				if(currentTick <= DURATION){
					TickBroadcast tick = new TickBroadcast(currentTick);
					ShoeStoreRunner.logger.info("Current tick is: " + currentTick);
					sendBroadcast(tick);
					currentTick++;
				}
				else {
					ShoeStoreRunner.logger.info("Time is up, sending termination broadcast");
					TerminateBroadcast terminate = new TerminateBroadcast();
					sendBroadcast(terminate);
					timer.cancel();
				}
			}
		};
	}

	/**
	 * registers this micro-service to the message bus
	 * subscribes this micro service to termination broadcasts
	 */
	@Override
	protected void initialize() {
		try {
			latch.await();
		} catch (InterruptedException e) {}
		MessageBusImpl.getInstance().register(this);
		ShoeStoreRunner.logger.info(this.getName()+" registered");
		subscribeBroadcast(TerminateBroadcast.class, termination ->{
			terminate();
			ShoeStoreRunner.logger.info(this.getName()+" terminated");
			MessageBusImpl.getInstance().unregister(this);
			ShoeStoreRunner.logger.info(this.getName()+" unregistered");
		});
		ShoeStoreRunner.logger.info(this.getName() + " has been initialized");
		timer.schedule(schedule, 0, TimeUnit.MILLISECONDS.toMillis(SPEED));
	}

}
