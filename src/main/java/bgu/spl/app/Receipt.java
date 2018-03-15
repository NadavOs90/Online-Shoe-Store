package bgu.spl.app;

public class Receipt {

	private String seller;
	private String customer;
	private String shoeType;
	private boolean discount;
	private int issuedTick;
	private int requestTick;
	private int amountSold;
	
	public Receipt(String seller,String customer,String shoeType,boolean discount,int issuedTick,int requestTick,int amountSold) {
		this.seller = seller;
		this.customer = customer;
		this.shoeType = shoeType;
		this.discount = discount;
		this.issuedTick = issuedTick;
		this.requestTick = requestTick;
		this.amountSold = amountSold;
	}
	
	public String toString(){
		return "\nSeller: "+seller+
				"\nCustomer: "+customer+
				"\nShoe type: "+shoeType+
				"\nDiscount: "+discount+
				"\nIssued: "+issuedTick+
				"\nRequested: "+requestTick+
				"\nAmount sold: "+amountSold+"\n---------------------";
	}
	
	public String getShoeType(){
		return shoeType;
	}
	
	public int getAmount(){
		return amountSold;
	}

}
