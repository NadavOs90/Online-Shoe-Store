package bgu.spl.app;

import bgu.spl.mics.Request;

public class RestockRequest  implements Request<Boolean> {
	
	private String shoeType;

	public RestockRequest(String type) {
		shoeType = type;
	}
	
	public String getType(){
		return shoeType;
	}

}
