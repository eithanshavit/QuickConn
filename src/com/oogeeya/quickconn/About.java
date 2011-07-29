package com.oogeeya.quickconn;

import com.oogeeya.quickconn.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class About extends Activity{
	
	private Button okBut;
	public static final String TAG = "QuickConn";
	public void onCreate(Bundle savedInstanceState)
	{
		// vars
		Log.d(TAG, "About onCreate");
		
		String title = getString(R.string.app_name)+ " " + getString(R.string.app_ver);
		setTitle(title);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);	
		this.okBut = (Button)this.findViewById(R.id.about_ok_button);
		this.okBut.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View v) {
		      finish();
		    }
		});
	}
	
	 protected void onStop()
	    {
	    	super.onStop();
	    	finish();
	    }
	
	
}
