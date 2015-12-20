package com.tesi.mymap;


import java.util.ArrayList;
import java.util.Arrays;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MapName extends Activity {
	
	private static final String code4="mapName";
	private EditText mapName;
	private Button save;
	private Button cancel;
	private Intent intent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_name);
		intent=getIntent();
		if(savedInstanceState!=null)
			intent.putExtras(savedInstanceState);
		
		save=(Button) findViewById(R.id.save_button);;
		save.setOnClickListener(new View.OnClickListener(){
			
			@Override
			public void onClick(View view){
				mapName=(EditText)findViewById(R.id.map_name);
				
				String n=(String)mapName.getText().toString();
				
				if(n.equals("")){
					Toast.makeText(view.getContext(), R.string.name_warning2,Toast.LENGTH_SHORT).show();
				}else{
					if(!contains(n)){
						intent.putExtra(code4, n);
						setResult(Activity.RESULT_OK,intent);
						finish();
					}else{
						Toast.makeText(view.getContext(), R.string.name_warning,Toast.LENGTH_SHORT).show();
					}
				}	
			}
		});
		cancel=(Button)findViewById(R.id.cancel_button);
		
		cancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
                intent.putExtra(code4,"map ");
				setResult(Activity.RESULT_OK,intent);
				
				finish();
			}
		});	
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
	   super.onSaveInstanceState(outState);
	   outState=intent.getExtras();

	}
	private boolean contains(String name){
		String[] mapNames=fileList();    	
    	ArrayList <String> names=new ArrayList<String>(Arrays.asList(mapNames));
    	
    	return names.contains(name);
	}
}
