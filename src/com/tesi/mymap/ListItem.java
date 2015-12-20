package com.tesi.mymap;

import java.io.Serializable;


public class ListItem implements Serializable{
	
	private static final long serialVersionUID = 2L;
	private String text;
	private String uriImage;
	
	private boolean noPointsFlag;
	private boolean onePointFlag;
	private boolean twoPointsFlag;
	
	private double[] coordinates;
	
	public ListItem(String t,String u){
		this.text=t;
		this.uriImage=u;
		this.noPointsFlag=true;
		this.onePointFlag=false;
		this.twoPointsFlag=false;
		
	}
	
	public String getText(){
		return this.text;
	}
	
	public String getUriImage(){
		return this.uriImage;
	}
	
	public void setText(String text){
		this.text=text;
	}
	
	public void setUriImage(String u){
		this.uriImage=u;
	}
	
	public void setCoordinates(double[]cord){
		this.coordinates=cord;
	}
	
	public boolean getNoPointsFlag(){
		return this.noPointsFlag;
	}
	
	public boolean getOnePointFlag(){
		return this.onePointFlag;
	}
	
	public boolean getTwoPointsFlag(){
		return this.twoPointsFlag;
	}
	
	public void enableOnePoint(){
		this.onePointFlag=true;
		this.noPointsFlag=false;
	}
	
	public void enableTwoPoint(){
		this.twoPointsFlag=true;
		this.onePointFlag=false;
		this.noPointsFlag=false;
	}
	
	public double[] getCoordinates(){
		return this.coordinates;
	}
	
	public String toString(){
		return this.text;
	}

}
