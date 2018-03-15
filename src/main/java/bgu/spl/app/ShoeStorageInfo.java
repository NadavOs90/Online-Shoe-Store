package bgu.spl.app;

public class ShoeStorageInfo {
	
	private String shoeType;
	private int amount=0;
	private int discountedAmount=0;
	
	public ShoeStorageInfo(String type, int amountInStorage) {
		shoeType = type;
		amount = amountInStorage;
	}
	
	public void addStorageAmount(int amount){
		this.amount += amount;
	}
	
	public void addDiscountedAmount(int amount){
		discountedAmount += amount;
	}
	
	public String getType(){
		return shoeType;
	}
	
	public int getAmount(){
		return amount;
	}
	
	public int getDiscountedAmount(){
		return discountedAmount;
	}
	
	public boolean hasAmountOnDiscount(){
		return discountedAmount>0;
	}
	
	/**
	 * lets you buy 1 shoe.
	 * if the item is on discount it will let you buy on discount
	 * otherwise the item will be bought not on discount
	 * @return returns true if the item was bought on discount
	 * returns false if the item was bought not on discount
	 */
	public boolean buy(){
		amount--;
		if(hasAmountOnDiscount()){
			discountedAmount--;
			return true;
		}
		return false;
	}
	
	public boolean inStock(){
		return amount>0;
	}
	
	/**
	 * returns in a readable string the information about the item
	 */
	public String toString(){
		return "[Shoe Type: " + shoeType + " Amount on storage: " + amount +
				" Discounted Amount: " + discountedAmount + "]";
	}
}
