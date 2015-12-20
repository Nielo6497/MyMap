package com.tesi.mymap;

import java.io.Serializable;

public class Counter implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private int count;
	
	public Counter(){
		count=1;
	}
	
	public void increase(){
		this.count+=1;
	}
	
	public void decrease(){
		this.count-=1;
	}
	
	public int getCount(){
		return this.count;
	}

}
