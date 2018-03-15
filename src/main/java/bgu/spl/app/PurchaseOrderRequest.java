package bgu.spl.app;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;

public class PurchaseOrderRequest  implements Request <Receipt> {
	
	private String shoeType;
	private boolean onlyOnDiscount;
	private MicroService requester;
	private int issuedTick;

	public PurchaseOrderRequest(String type, boolean discount, MicroService requester, int tick) {
		shoeType = type;
		onlyOnDiscount = discount;
		this.requester=requester;
		issuedTick = tick;
	}
	
	public String getType(){
		return shoeType;
	}
	
	public boolean onDiscount(){
		return onlyOnDiscount;
	}

	public MicroService getRequester() {
		return requester;
	}
	
	public int getTick(){
		return issuedTick;
	}
	
	

}
