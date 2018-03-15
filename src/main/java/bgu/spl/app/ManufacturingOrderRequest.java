package bgu.spl.app;

import bgu.spl.mics.Request;

public class ManufacturingOrderRequest implements Request<Receipt> {
	
	private int amountToOrder;
	private String shoeType;
	private int issuedTick;

	public ManufacturingOrderRequest(String type, int amount, int tick) {
		shoeType = type;
		amountToOrder = amount;
		issuedTick = tick;
	}
	
	public String getType(){
		return shoeType;
	}
	
	public int getAmountToOrder(){
		return amountToOrder;
	}
	
	public int getTick(){
		return issuedTick;
	}

}
