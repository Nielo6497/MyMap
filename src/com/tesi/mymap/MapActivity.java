package com.tesi.mymap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.Toast;


public class MapActivity extends Activity {
	
	private static final int GET_POSITION_MENU_ITEM=1;
	private static final int ADD_COORDINATES_MENU_ITEM=2;
	private static final String PROVIDER_ID=LocationManager.GPS_PROVIDER;
	private static final String code1="coordinates";
	private static final String code2="index";
	private static final String code3="mapImage";
	
	private Vibrator vib;
	
	private int counter=1;
	
	//Gestione mappa
	private double[] coordinates;
	private int identifier;
	private boolean flag;
	private double factorPixelDegreesX;
	private double factorPixelDegreesY;
	
	//Reperimento mappa
	private Intent openMap;
	private ImageMap map;
	private Uri uri;
	
	//Punti di riferimento da assegnare
	private MyPoint first;
	private MyPoint second;
	private MyPoint origin;
	
	
	
	//Pulsanti del menu
	private MenuItem getPos;
	private MenuItem addCoordinates;
	
	//Fondamentale per l'utilizzo del gps
	private LocationManager manager;
	private LocationProvider gpsProvider;
	private Location myLocation;
	private LocationListener myLocationListener = new LocationListener() {
		
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
				Toast.makeText(ctx, R.string.no_signal, Toast.LENGTH_SHORT).show();
			} 
			else if(status== LocationProvider.OUT_OF_SERVICE){
				Toast.makeText(ctx, R.string.no_signal, Toast.LENGTH_SHORT).show();
				
			}else if(status==LocationProvider.AVAILABLE){
				vib.vibrate(1000);	
			}
		}
		
		@Override
		public void onProviderEnabled(String provider) {
			Toast.makeText(ctx, R.string.status_on, Toast.LENGTH_SHORT).show();						
		}
		@Override
		public void onProviderDisabled(String provider) {
			
			enableGps.show();
		}
		@Override
		public void onLocationChanged(Location location) {
			
			myLocation=location;
		}
	};
	
	//Dialogs
	private AlertDialog enableGps;
	private AlertDialog noGps;
	
	
	
	Context ctx=this;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);
		map=(ImageMap)findViewById(R.id.imageMap);
		openMap=getIntent();
		setTitle(openMap.getStringExtra("text"));
		String uriString=openMap.getStringExtra(code3);
		uri=Uri.parse(uriString);
		map.setImageURI(uri);
		map.setMaxZoom(20f);
		vib=(Vibrator)getSystemService(VIBRATOR_SERVICE);
		enableGps=createEnableGpsDialog();
		noGps=noGpsProvider();
		
		if(openMap.getBooleanExtra("noPoints",false)){
			flag=true;
			coordinates=new double[8];
			identifier=openMap.getIntExtra(code2, 0);
			
		}else if(openMap.getBooleanExtra("onePoint",false)){
			flag=true;
			counter=2;
			coordinates=openMap.getDoubleArrayExtra(code1);
			identifier=openMap.getIntExtra(code2,0);
			first=new MyPoint(coordinates[4],coordinates[5],coordinates[6],coordinates[7]);
			
			map.addGhost(first.getX(),first.getY());
			
		}else if(openMap.getBooleanExtra("twoPoints",false)){
			flag=false;
			coordinates=openMap.getDoubleArrayExtra(code1);
			fixCoordinates(coordinates);
			
		}
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		manager=(LocationManager) getSystemService(LOCATION_SERVICE);
		gpsProvider=manager.getProvider(PROVIDER_ID);
		if(gpsProvider==null){
			noGps.show();
		}
		manager.requestLocationUpdates(PROVIDER_ID, 5000, 0, myLocationListener);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationManager.removeUpdates(myLocationListener);
		
		if(enableGps.isShowing()){
			enableGps.dismiss();
		}
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		BitmapDrawable d=(BitmapDrawable)map.getDrawable();
		if(d!=null){
			Bitmap bitmap = d.getBitmap();
			if(bitmap!=null)
				bitmap.recycle();
		}
		map.invalidate();
		map.setImageDrawable(null);
		
		Log.d("onDestroy", "effettivamente viene invocato");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		getPos=menu.add(Menu.NONE, GET_POSITION_MENU_ITEM, 1, "Mia Posizione");
		getPos.setIcon(R.drawable.my_pos);
		addCoordinates=menu.add(Menu.NONE,ADD_COORDINATES_MENU_ITEM,2,"Aggiungi Coordinate");
		addCoordinates.setIcon(R.drawable.add_pin);
		if(flag)
			getPos.setVisible(false);
		else
			addCoordinates.setVisible(false);
		
		addCoordinates.setOnMenuItemClickListener(new OnMenuItemClickListener(){
        	
        	@Override
        	public boolean onMenuItemClick(MenuItem item) {
        		ZoomablePinView pin=map.getPin();
        		
        		if(myLocation!=null){
        			
        			if(pin!=null){
            			PointF temp=pin.getPositionInPixels();
            			if(counter==1){
            				Location current=myLocation;
            				double currentLat=current.getLatitude();
            				double currentLong=current.getLongitude();
            				first=new MyPoint(temp.x,temp.y,currentLat,currentLong);
            				myLocation=null;
            				current=null;
            				coordinates[4]=first.getX();
            				coordinates[5]=first.getY();
            				coordinates[6]=first.getLatitude();
            				coordinates[7]=first.getLongitude();
            				
            				openMap.putExtra("onePoint", true);
            				openMap.putExtra(code1, coordinates);
            				openMap.putExtra(code2,identifier);
            				setResult(Activity.RESULT_OK,openMap);
            				Log.d("FirstCoordinates", "x: " + first.getX() + " y : " + first.getY());
            				Log.d("fgpsCoords", "x: " + first.getLongitude() + " y : " + first.getLatitude());
            				counter+=1;
            			}else if(counter==2){
            				Location current=myLocation;
            				double currentLat=current.getLatitude();
            				double currentLong=current.getLongitude();
            				second=new MyPoint(temp.x,temp.y,currentLat,currentLong);
            				myLocation=null;
            				current=null;
            				map.negativeFlag();
            				counter+=1;
            				Log.d("SecondCoordinates", "x: " + second.getX() + " y : " + second.getY());
            				Log.d("sgpsCoords", "x: " + second.getLongitude() + " y : " + second.getLatitude());
            				
            			}  
            			Toast.makeText(ctx,R.string.assigned, Toast.LENGTH_SHORT).show();
        			}else{
        				Toast.makeText(ctx, R.string.no_pin, Toast.LENGTH_SHORT).show();
        			}
        		}else{
        			Toast.makeText(ctx,R.string.no_signal,Toast.LENGTH_SHORT).show();
        		}
        		
        		if(counter==3){
        			item.setVisible(false);
        			map.removePin();
        			getPos.setVisible(true);
        			fixFactors();
        			
        			openMap.removeExtra(code1);
        			openMap.removeExtra(code2);
        			openMap.removeExtra("onePoint");
        			openMap.putExtra(code1,coordinates);
        			openMap.putExtra(code2,identifier);
        			openMap.putExtra("twoPoints",true);
        			setResult(Activity.RESULT_OK,openMap);
        			
        			counter+=1;
        		}        		
        		return true;
        		}
        });
		
		getPos.setOnMenuItemClickListener(new OnMenuItemClickListener(){
			@Override
			public boolean onMenuItemClick(MenuItem item){
				if(myLocation!=null){
					Location current=myLocation;
					double x=(current.getLongitude()-origin.getLongitude())*factorPixelDegreesX;
					double y=(current.getLatitude()-origin.getLatitude())*factorPixelDegreesY;
					y=Math.abs(y-map.viewHeight);
					myLocation=null;
					
					map.addPosition(x , y);
					
					current=null;
				}else{
					Toast.makeText(ctx,R.string.no_signal, Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		});
		return true;
	}
	//METODO PER RIPRISTINARE I FATTORI DI CONVERSIONE E IL PUNTO NELL'ORIGINE
	private void fixCoordinates(double[] coords){
		map.negativeFlag();
		factorPixelDegreesX=coordinates[0];
		factorPixelDegreesY=coordinates[1];
		origin=new MyPoint(0,0,coordinates[2],coordinates[3]);
		
	}
	//METODO PER IL CALCOLO DEI FATTORI DI CONVERSIONE E DEL PUNTO ALL'ORIGINE DEGLI ASSI
	private void fixFactors(){
		double deltaPixelX=Math.abs(first.getX()-second.getX());
		double deltaPixelY=Math.abs(first.getY()-second.getY());
		
		double deltaLat=Math.abs(first.getLatitude()-second.getLatitude());
		double deltaLong=Math.abs(first.getLongitude()-second.getLongitude());
		
		factorPixelDegreesX=deltaPixelX/deltaLong;
		factorPixelDegreesY=deltaPixelY/deltaLat;
		double origLong=first.getLongitude()-(first.getX()/factorPixelDegreesX);
		double origLat=first.getLatitude()-(Math.abs(first.getY()-map.viewHeight)/factorPixelDegreesY);
		
		coordinates[0]=factorPixelDegreesX;
		coordinates[1]=factorPixelDegreesY;
		coordinates[2]=origLat;
		coordinates[3]=origLong;
		origin=new MyPoint(0,0,origLat,origLong);
	}
	
	//ALERTDIALOG PER GPS DISABILITATO
	private AlertDialog createEnableGpsDialog(){
		AlertDialog.Builder alertBuilder=new AlertDialog.Builder(ctx);
    	alertBuilder.setTitle("GPS");
    	alertBuilder.setMessage(R.string.status_off);
    	alertBuilder.setCancelable(false);
    	alertBuilder.setPositiveButton(R.string.alert_pos_button, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent in = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);   
				startActivity(in);
				
				dialog.dismiss();
			}
		});
    	alertBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				
			}
		});
    	AlertDialog gpsAlert=alertBuilder.create();
    	return gpsAlert;
	}
	
	//ALERTDIALOG NEL CASO IN CUI LO SMARTPHONE SIA SPROVVISTO DI GPS
	private AlertDialog noGpsProvider(){
		AlertDialog.Builder alertBuilder=new AlertDialog.Builder(ctx);
    	alertBuilder.setTitle("Avviso");
    	alertBuilder.setMessage("Il tuo dispositivo non disponde del gps.L'applicazione verrà terminata");
    	alertBuilder.setCancelable(false);
    	alertBuilder.setPositiveButton(R.string.alert_pos_button, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				dialog.dismiss();
				finish();
			}
		});
    	
    	AlertDialog gpsAlert=alertBuilder.create();
    	return gpsAlert;
	}	
}
