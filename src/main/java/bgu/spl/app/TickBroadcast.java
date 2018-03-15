package bgu.spl.app;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {
	
	private int currentTick;

	public TickBroadcast(int tick) {
		currentTick = tick;
	}
	
	public int getTick(){
		return currentTick;
	}

}
