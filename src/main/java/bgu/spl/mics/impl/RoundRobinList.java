package bgu.spl.mics.impl;

import java.util.LinkedList;

public class RoundRobinList<E> extends LinkedList<E> {
	
	private int indexOfLastUsed =-1;
	
	public synchronized <E> E roundNext(){
		indexOfLastUsed++;
		if (indexOfLastUsed >= this.size()){
			indexOfLastUsed = 0;
			return (E) this.get(indexOfLastUsed);
		}	
		return (E) this.get(indexOfLastUsed);
	}

}
