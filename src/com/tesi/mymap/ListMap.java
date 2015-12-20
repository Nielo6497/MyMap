package com.tesi.mymap;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListMap extends Activity  {
	
	private static final String code1="coordinates";
	private static final String code2="index";
	private static final String code3="mapImage";
	private static final String code4="mapName";
	private static final String intentType="image/*";
	private static final int MENUITEM_ADD_MAP=1;
	private static final int MENUITEM_HELP=2;
	private static final int ADD_MAP_CODE=3;
	private static final int INSERT_NAME_CODE=4;
	private static final int GET_COORDINATES=5;
	private static final int RENAME_MENU_CODE=6;
	private static final int DELETE_MENU_CODE=7;
	private static Counter counter;
	private final Context ctx=this;
	private ListView list;
	private ArrayAdapter<ListItem> myAdapter;
	private String selectedImageUri;
	private Uri uri;
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_map);
        
        list=(ListView) findViewById(R.id.mapList);
        
        myAdapter= new ArrayAdapter<ListItem>(this,R.layout.array_item,R.id.itemText);
        list.setAdapter(myAdapter);
        registerForContextMenu(list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	@Override
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        		Intent localIntent=new Intent(ListMap.this,MapActivity.class);
        		ListItem clicked=myAdapter.getItem(position);
        		localIntent.putExtra(code3,clicked.getUriImage());
        		localIntent.putExtra("text", clicked.getText());
        		
        		if(clicked.getNoPointsFlag()){
        			localIntent.putExtra(code2,position);
        			localIntent.putExtra("noPoints", true);
        			startActivityForResult(localIntent,GET_COORDINATES);
        		}else if(clicked.getOnePointFlag()){
        			localIntent.putExtra(code1, clicked.getCoordinates());
        			localIntent.putExtra(code2, position);
        			localIntent.putExtra("onePoint", true);
        			startActivityForResult(localIntent,GET_COORDINATES);
        		}else if(clicked.getTwoPointsFlag()){
        			localIntent.putExtra(code1,clicked.getCoordinates());
        			localIntent.putExtra("twoPoints", true);
        			startActivity(localIntent);
        		}
        	}
		});
        loadItems();
        counter=loadCounter();
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem addButton=menu.add(Menu.NONE,MENUITEM_ADD_MAP,1,R.string.add_map);
        MenuItem helpButton=menu.add(Menu.NONE,MENUITEM_HELP,2,R.string.help);
        addButton.setIcon(R.drawable.new_map);
        helpButton.setIcon(R.drawable.help_icon);
        
        addButton.setOnMenuItemClickListener(new OnMenuItemClickListener(){
        	
        	@Override
        	public boolean onMenuItemClick(MenuItem item) {
        		
        		selectMap();
        		
        		return true;
        		}
        });
        
        helpButton.setOnMenuItemClickListener(new OnMenuItemClickListener(){
        	@Override
        	public boolean onMenuItemClick(MenuItem item){
        		AlertDialog help=createHelpDialog();
        		help.show();
        		
        		return true;
        	}
        });
        return true;
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu,View view,ContextMenu.ContextMenuInfo menuInfo){
    	menu.add(Menu.NONE,RENAME_MENU_CODE,1,R.string.rename);
    	menu.add(Menu.NONE,DELETE_MENU_CODE,2,R.string.delete);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item){
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	int index=info.position;
    	
    	int id=item.getItemId();
    	
    	switch(id){
    	case DELETE_MENU_CODE:
    		AlertDialog delete=createAlertDelete(index);
    		delete.show();
    		return true;
    	
    	case RENAME_MENU_CODE:
    		Dialog rename=createRenameDialog(index);
    		rename.show();
    		return true;
    	default:
    		
    		return super.onContextItemSelected(item);
    
    	}
    }
    
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if(requestCode==ADD_MAP_CODE && resultCode==Activity.RESULT_OK){
    		
    		uri=data.getData();		
    		insertName();    
    		
    	}else if(requestCode==INSERT_NAME_CODE && resultCode==Activity.RESULT_OK){
    			
    		try{
    			if(uri !=null){
   					String nome=data.getStringExtra(code4);
   					if(nome.equals("map ")){
   						nome=nome+counter.getCount();	
    				}
    					
    				int orient=getOrientation(uri);
        				
        			if(orient !=0){
     					selectedImageUri=saveImage(rotateImage(orient,uri));
       				}
       				else{
       					InputStream stream=getContentResolver().openInputStream(uri);
       					Bitmap image=BitmapFactory.decodeStream(stream);
       					selectedImageUri=saveImage(image);
       					stream.close();
       				}
    				
   					ListItem newMap=new ListItem(nome,selectedImageUri);
   					myAdapter.add(newMap);
    				saveItem(newMap);
    				uri=null;
    				selectedImageUri=null;
    				counter.increase();
    				saveCounter(counter);
    			}
    			
					
    		}catch(Exception e){
   				Toast.makeText(ctx, "errore memoria2", Toast.LENGTH_SHORT).show();
   				e.printStackTrace();
   			}
    			
    	}else if(requestCode==GET_COORDINATES && resultCode==Activity.RESULT_OK){
    		double [] coordinates=data.getDoubleArrayExtra(code1);
    		int index=data.getIntExtra(code2, 0);
    		
    		ListItem temp=myAdapter.getItem(index);
    		temp.setCoordinates(coordinates);
    		
    		if(data.getBooleanExtra("onePoint", false))
    			temp.enableOnePoint();
    		else if(data.getBooleanExtra("twoPoints", false)){
    			temp.enableTwoPoint();	
    		}
    			
    		try{
    			saveItem(temp);
    		}catch(Exception e){
    			Toast.makeText(ctx,"Eccezione", Toast.LENGTH_SHORT).show();
    		}	
    	}
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	if(myAdapter.getCount()>0){
	    	   TextView testo= (TextView) findViewById(R.id.vuota);
	    	   testo.setText("");
	       }else{
	    	   TextView testo= (TextView) findViewById(R.id.vuota);
	    	   testo.setText(R.string.emptyList);
	       }
    }
    
    //METODO PER CARICARE GLI ITEMS DALLO STORAGE
    private void loadItems(){
    	
    	String[] mapNames=fileList();
    	int len=mapNames.length;
    	for(int i=0;i<mapNames.length;i++){
    		try{
    			if(!mapNames[len-1-i].equals("contatore")){
    				FileInputStream fis = openFileInput(mapNames[len-1-i]);
        		    ObjectInputStream ois = new ObjectInputStream(fis);
        		    ListItem current =(ListItem) ois.readObject();
        		    myAdapter.add(current);
        		    ois.close();
        		    fis.close();   
    			}	
    		}catch(Exception e){
    			Toast.makeText(ctx, "Item non trovato", Toast.LENGTH_SHORT).show();
    			deleteFile(mapNames[len-1-i]);
    		}
    	}
    	if(mapNames.length==0){
    		counter=new Counter();
    		saveCounter(counter);
    	}
    }
    
    private String saveImage(Bitmap map){
    	
    	String root = Environment.getExternalStorageDirectory().toString();
    	File myDir = new File(root +"/MyMap");    
    	myDir.mkdirs();
    	Random generator = new Random();
    	int n = 10000;
    	n = generator.nextInt(n);
    	String fname = "Image-"+ n +".jpg";
    	File file = new File (myDir, fname);
    	if (file.exists ()) file.delete (); 
    	try {
           FileOutputStream out = new FileOutputStream(file);
           map.compress(Bitmap.CompressFormat.JPEG, 90, out);
           out.flush();
           out.close();

    	} catch (Exception e) {
        	e.printStackTrace();
    	}
    	Uri u=Uri.fromFile(file);
    	return u.toString();
    }
    //CREA UNA COPIA DELL'IMMAGINE IDENTIFICATA DA URI RUOTANDOLA DI ROTATION GRADI
    private Bitmap rotateImage(int rotation,Uri uri) {

	    Matrix matrix = new Matrix();
	    matrix.postRotate(rotation);
	    Bitmap sourceBitmap;
	    Bitmap result=null;
	    
	    try{
	    	 sourceBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
	    	 result=Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);
	    }catch(Exception e){
	    	
	    }
	    
	    return result;
	}
    
    //RESTITUISCE UN INTERO CHE RAPPRESENTA L'ANGOLO DI ROTAZIONE DELL'IMMAGINE
    private int getOrientation(Uri selectedImage) {
	    int orientation = 0;
	    final String[] projection = new String[]{MediaStore.Images.Media.ORIENTATION};      
	    final Cursor cursor = getContentResolver().query(selectedImage, projection, null, null, null);
	    if(cursor != null) {
	        final int orientationColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);
	        if(cursor.moveToFirst()) {
	            orientation = cursor.isNull(orientationColumnIndex) ? 0 : cursor.getInt(orientationColumnIndex);
	        }
	        cursor.close();
	    }
	    return orientation;
	}
    
    //METODO PER SALVARE UN OGGETTO LISTITEM NELLO STORAGE
    private void saveItem(ListItem item) throws IOException {
        FileOutputStream fos = openFileOutput(item.getText(), Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(item);
        oos.close();
        fos.close();
     }
    
    //METODO PER AVVIARE L'ACTIVITY DI SELEZIONE IMMAGINE MAPPA
    private void selectMap(){
    	Intent takeMap=new Intent();
    	takeMap.setType(intentType);
    	takeMap.setAction(Intent.ACTION_PICK);
    	startActivityForResult(takeMap,ADD_MAP_CODE);
    }
    //METODO PER AVVIARE L'ACTIVITY DI INSERIMENTO NOME MAPPA
    private void insertName(){
    	Intent selectName=new Intent(ListMap.this,MapName.class);
		startActivityForResult(selectName,INSERT_NAME_CODE);
    }
    //METODO PER LA CREAZIONE DI UN DIALOG PER RINOMINARE I FILE NELLO STORAGE 
    private Dialog createRenameDialog(int i){
    	final Dialog dialog=new Dialog(this);
    	final int index=i;
    	dialog.setContentView(R.layout.rename_dialog);
    	dialog.setTitle(R.string.rename_title);
    	
    	Button cancel=(Button) dialog.findViewById(R.id.cancel_button2);
    	cancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				
			}
		});
    	
    	Button save =(Button) dialog.findViewById(R.id.save_button2);
    	save.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ListItem current=myAdapter.getItem(index);
				String oldName=current.getText();
				EditText mapName=(EditText) dialog.findViewById(R.id.map_name2);
				String newName=mapName.getText().toString();
				if(!newName.equals("")){
					if(!contains(newName)){
						renameFile(oldName,newName,current);
						myAdapter.notifyDataSetChanged();
					}else{
						Toast.makeText(v.getContext(),R.string.name_warning,Toast.LENGTH_SHORT).show();
					}
					
				}
				dialog.dismiss();
				
			}
		});
    	
    	return dialog;
    }
    //METODO PER LA CREAZIONE DI UN ALERTDIALOG PER LA CANCELLAZIONE DI UN FILE DALLO STORAGE
    private AlertDialog createAlertDelete(int i){
    	
    	final int index=i;
    	AlertDialog.Builder alertBuilder=new AlertDialog.Builder(ctx);
    	alertBuilder.setTitle(R.string.alert_title);
    	alertBuilder.setMessage(R.string.alert_message);
    	alertBuilder.setCancelable(false);
    	alertBuilder.setPositiveButton(R.string.alert_pos_button, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ListItem current=myAdapter.getItem(index);
				Uri u=Uri.parse(current.getUriImage());
				File f=new File(u.getPath());
				f.delete();
				deleteFile(current.getText());
				myAdapter.remove(current);
				
				if(myAdapter.getCount()==0){
	    			TextView testo= (TextView) findViewById(R.id.vuota);
		    	    testo.setText(R.string.emptyList);
		    	    
	    		}
				
				Toast.makeText(ctx,R.string.correct_delete,Toast.LENGTH_SHORT).show();
				dialog.dismiss();
				
			}
		});
    	
    	alertBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				
			}
		});
    	AlertDialog deleteAlert=alertBuilder.create();
    	return deleteAlert;
    }
    
    private AlertDialog createHelpDialog(){
    	AlertDialog.Builder alertBuilder=new AlertDialog.Builder(ctx);
    	alertBuilder.setTitle(R.string.help);
    	alertBuilder.setMessage(R.string.help_dialog);
    	alertBuilder.setCancelable(false);
    	alertBuilder.setPositiveButton(R.string.alert_pos_button, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
    	AlertDialog help=alertBuilder.create();
    	return help;
    }
    
   //METODO PER RINOMINARE I FILE NELLO STORAGE
   private void renameFile(String old,String news,ListItem i){
	   try{
		   i.setText(news);
		   deleteFile(old);
		   saveItem(i);
	   	}catch(Exception e){
	   		Toast.makeText(ctx,"errore rename", Toast.LENGTH_SHORT).show();
	   		Log.e("File non trovato1",e.getMessage());
	   	} 
   }
   //METODO PER SALVARE IL CONTATORE NELLO STORAGE
   private void saveCounter(Counter counter){
		try{
			FileOutputStream fos=openFileOutput("contatore",Context.MODE_PRIVATE);
			ObjectOutputStream ois=new ObjectOutputStream(fos);
			ois.writeObject(counter);
			ois.close();
			fos.close();
		}catch(Exception e){
			
		}
	}
   private Counter loadCounter(){
		Counter result=null;
		
		try{
			FileInputStream fis=openFileInput("contatore");
			ObjectInputStream ob=new ObjectInputStream(fis);
			result=(Counter)ob.readObject();
			fis.close();
			ob.close();
		}catch(Exception e){
			Toast.makeText(this, "counter", Toast.LENGTH_SHORT).show();
		}
		return result;
	}
   private boolean contains(String name){
		String[] mapNames=fileList();    	
		ArrayList <String> names=new ArrayList<String>(Arrays.asList(mapNames));
		return names.contains(name);
	}
}
