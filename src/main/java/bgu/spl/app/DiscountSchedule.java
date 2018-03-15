package bgu.spl.app;

public class DiscountSchedule {
	
	private int amount;
	private String shoeType;
	private int tick;

	public DiscountSchedule(String type, int tick, int amount) {
		this.shoeType=type;
		this.tick=tick;
		this.amount=amount;
	}
	
	public String getType(){
		return shoeType;
	}
	
	public int getAmount(){
		return amount;
	}
	
	public int getTick(){
		return tick;
	}

}
