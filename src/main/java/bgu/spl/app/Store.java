package bgu.spl.app;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * creates a store object as a singleton
 */
public class Store {

	private ConcurrentHashMap<String, ShoeStorageInfo> storage;
	private LinkedList<Receipt> files;
	
	private static class SingletonHolder {
        private static Store instance = new Store();
	}

	private Store() {
		storage = new ConcurrentHashMap<String, ShoeStorageInfo>();
		files = new LinkedList<Receipt>();
	}
 
	public static Store getInstance() {
         return SingletonHolder.instance;
	}
	
	/**
	 * loads the initial storage of shoes
	 * @param storage the shoes to put in storage
	 */
	public void load ( ShoeStorageInfo[]  storage ){
		for (int i=0; i<storage.length; i++){
			this.storage.put(storage[i].getType(), storage[i]);
		}
	}
	
	/**
	 * lets the seller sell a specific shoe
	 * @param shoeType - the shoe to be bought
	 * @param onlyDiscount - if the client wants to buy the item only if its on discount
	 * @return the BuyResult of the purchase
	 */
	public BuyResult  take ( String  shoeType , boolean onlyDiscount ){
		if(!storage.containsKey(shoeType)){
			if(onlyDiscount)
				return BuyResult.NOT_ON_DISCOUNT;
			return BuyResult.NOT_IN_STOCK;
		}
		else{
			synchronized(storage.get(shoeType)){
				if (onlyDiscount && !storage.get(shoeType).hasAmountOnDiscount())
					return BuyResult.NOT_ON_DISCOUNT;
				if (!storage.get(shoeType).inStock())
					return BuyResult.NOT_IN_STOCK;
				boolean boughtWithDiscount = storage.get(shoeType).buy();
				if (boughtWithDiscount)
					return BuyResult.DISCOUNTED_PRICE;	
				return BuyResult.REGULAR_PRICE;
			}
		}
	}
	
	/**
	 * adds a shoe to the inventory
	 * @param shoeType - the shoe to be added
	 * @param amount - the amount of the shoe to add
	 */
	public void add ( String  shoeType ,int amount){
		if (storage.containsKey(shoeType)){
			synchronized(storage.get(shoeType)){
			storage.get(shoeType).addStorageAmount(amount);
			}
		}
		else{
			storage.put(shoeType, new ShoeStorageInfo(shoeType,amount));
		}
	}
	
	/**
	 * adds a discount
	 * @param shoeType - the shoe thats now on discount
	 * @param amount - the amount of shoes on to add to the discount
	 */
	public void addDiscount ( String  shoeType ,int amount){
		if (storage.containsKey(shoeType)){
			synchronized(storage.get(shoeType)){
				if(storage.get(shoeType).getAmount() < amount + storage.get(shoeType).getDiscountedAmount())
					storage.get(shoeType).addDiscountedAmount(storage.get(shoeType).getAmount() - storage.get(shoeType).getDiscountedAmount());
				else	
					storage.get(shoeType).addDiscountedAmount(amount);
			}
		}
	}
	
	/**
	 * @param receipt is added to the store database
	 */
	public void file(Receipt receipt){
		synchronized(files){files.add(receipt);}
	}
	
	/**
	 * prints all the receipts saved in the store
	 */
	public void print(){
		for (ShoeStorageInfo temp: storage.values())
			System.out.println(temp.toString());
		Iterator<Receipt> temp =files.iterator();
		while(temp.hasNext()){
			System.out.println(temp.next().toString());
		}
		System.out.println("The total amount of receipts is "+  files.size());
	}

}
