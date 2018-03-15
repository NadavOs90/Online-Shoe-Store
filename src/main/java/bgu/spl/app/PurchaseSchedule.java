package bgu.spl.app;

public class PurchaseSchedule {
	
	private String shoeType;
	private int tick;
	
	public PurchaseSchedule(String type, int tick){
		this.shoeType = type;
		this.tick=tick;
	}
	
	public int getTick(){
		return tick;
	}
	
	public String getType(){
		return shoeType;
	}

}
