package com.tesi.mymap;

public class MyPoint {
	
	private double pixelX;
	private double pixelY;
	
	private double latitude;
	private double longitude;
	
	public MyPoint(double pX,double pY,double lat,double lon){
		this.pixelX=pX;
		this.pixelY=pY;
		this.latitude=lat;
		this.longitude=lon;
	}
	
	public double getX(){
		return this.pixelX;
	}
	
	public double getY(){
		return this.pixelY;
	}
	
	public double getLatitude(){
		return this.latitude;
	}
	
	public double getLongitude(){
		return this.longitude;
	}

}
