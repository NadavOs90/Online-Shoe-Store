package bgu.spl.app;

import bgu.spl.mics.Broadcast;

public class NewDiscountBroadcast implements Broadcast {
	
	private String shoeType;

	public NewDiscountBroadcast(String type) {
		shoeType = type;
	}
	
	public String getType(){
		return shoeType;
	}

}
