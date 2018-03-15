package bgu.spl.mics.impl;

import bgu.spl.app.ShoeStoreRunner;
import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.RequestCompleted;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;


public class MessageBusImpl implements MessageBus {

    private ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> services;
    private ConcurrentHashMap<Request<?>, MicroService> requesters;
    private ConcurrentHashMap<String,RoundRobinList<LinkedBlockingQueue<Message>>> subscribtionsQueues;
    
	private static class SingletonHolder {
           private static MessageBusImpl instance = new MessageBusImpl();
    }
  
	private MessageBusImpl() {
		services = new ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>>();
		requesters = new ConcurrentHashMap<Request<?>, MicroService> ();
		subscribtionsQueues = new ConcurrentHashMap<String,RoundRobinList<LinkedBlockingQueue<Message>>> ();
    }
    
	public static MessageBusImpl getInstance() {
            return SingletonHolder.instance;
    }

	@Override
	public synchronized void subscribeRequest(Class<? extends Request> type, MicroService m) {
		LinkedBlockingQueue<Message> tempQ= services.get(m);
		if (subscribtionsQueues.containsKey(type.getName())){
			 subscribtionsQueues.get(type.getName()).add(tempQ);
		}
		else{
			subscribtionsQueues.put(type.getName(), new RoundRobinList<LinkedBlockingQueue<Message>>());
			subscribtionsQueues.get(type.getName()).add(tempQ);
		}
	}

	@Override
	public synchronized void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		LinkedBlockingQueue<Message> tempQ= services.get(m);
		if (subscribtionsQueues.containsKey(type.getName())){
			subscribtionsQueues.get(type.getName()).add(tempQ);
		}
		else{
			subscribtionsQueues.put(type.getName(), new RoundRobinList<LinkedBlockingQueue<Message>>());
			subscribtionsQueues.get(type.getName()).add(tempQ);
		}	
	}

	@Override
	public <T> void complete(Request<T> r, T result) {
		RequestCompleted<T> rq = new RequestCompleted<T>(r,result);
		try {
			services.get(requesters.get(r)).put(rq);
		} catch (InterruptedException e) {ShoeStoreRunner.logger.warning("Message bus was interrupted");}
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		if(subscribtionsQueues.containsKey(b.getClass().getName())){
			RoundRobinList<LinkedBlockingQueue<Message>> tempL= subscribtionsQueues.get(b.getClass().getName());
			synchronized(tempL){
				for (LinkedBlockingQueue<Message> tempQ: tempL)
					try {
						tempQ.put(b);
					} catch (InterruptedException e) {ShoeStoreRunner.logger.warning("Message bus was interrupted");}
			}
		}
	}

	@Override
	public boolean sendRequest(Request<?> r, MicroService requester) {
		if(subscribtionsQueues.containsKey(r.getClass().getName())){
			RoundRobinList<LinkedBlockingQueue<Message>> tempL= subscribtionsQueues.get(r.getClass().getName());
			LinkedBlockingQueue<Message> tempQ= tempL.roundNext();
			requesters.put(r, requester);
			return tempQ.add(r);
		}
		return false;
	}

	@Override
	public void register(MicroService m) {
		if (!services.containsKey(m))
			services.put(m, new LinkedBlockingQueue<Message>());

	}

	@Override
	public synchronized void unregister(MicroService m) {
		for (RoundRobinList<LinkedBlockingQueue<Message>> temp: subscribtionsQueues.values())
			synchronized(temp){
				temp.remove(services.get(m));
			}
		services.remove(m);
		notifyAll();
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		if (!services.containsKey(m)){
			ShoeStoreRunner.logger.warning("Message didn't find A micro-services named "+ m.getName());
			throw new IllegalStateException();
		}
		return services.get(m).take();
	}
	
	/**
	 * 
	 * @return boolean if all the micro-services have been unregistered
	 */
	public boolean done(){
		return services.isEmpty();
	}
	

}
