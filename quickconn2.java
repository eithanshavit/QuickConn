package com.oogeeya.quickconn;

import com.oogeeya.quickconn.R;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class quickconn extends Activity implements OnTouchListener {

	public static final String TAG = "QuickConn";
	private boolean FirstRun;
	private GestureDetector mGestureDetector;
	private MyGestureListener mGestureListener; 

	@Override
	public void onCreate(Bundle savedInstanceState) {

		Log.d(TAG, "Activity State: onCreate()");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		// check if first run
		SharedPreferences myPrefs = getSharedPreferences("file", MODE_PRIVATE);
		FirstRun = myPrefs.getBoolean("first_run", true);
		SharedPreferences.Editor prefEditor = myPrefs.edit();
		prefEditor.putBoolean("first_run", false);
		prefEditor.commit();
		if (FirstRun) {
			launchAbout();
		}

		// Obtain handles to UI objects
		ListView ConList = (ListView) findViewById(R.id.StarredList);

		// Populate the contact list
		populateContactList(ConList);

		// handle list select
	
		mGestureListener = new MyGestureListener();
		mGestureDetector = new GestureDetector(this,mGestureListener);
	
		ConList.setOnTouchListener(this);

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.d(TAG,"onTouch");
		v.
		mGestureListener.setList((ListView)v);
		if (mGestureDetector.onTouchEvent(event))
			return true;
		else
			return false;

	}

	protected void onStop() {
		super.onStop();
		finish();
	}

	
	// launch about.java when menu pressed
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		launchAbout();
		return true;

	}

	
	// Populate the contact list based on account currently selected in the
	// account spinner.

	private void populateContactList(ListView list) {
		// Build adapter with contact entries
		Cursor cursor = getContacts();
		String[] fields = new String[] { ContactsContract.Data.DISPLAY_NAME };
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.contact_entry, cursor, fields,
				new int[] { R.id.contactEntryName });
		list.setAdapter(adapter);
	}

	// Obtains the contact list for the currently selected account.
	// @return A cursor for for accessing the contact list.
	private Cursor getContacts() {
		// Run query
		Uri uri = ContactsContract.Contacts.CONTENT_URI;
		String[] projection = new String[] { ContactsContract.Contacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME };
		String selection = ContactsContract.Contacts.STARRED;
		String[] selectionArgs = null;
		String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
				+ " COLLATE LOCALIZED ASC";

		return managedQuery(uri, projection, selection, selectionArgs,
				sortOrder);
	}

	

	// launch about activity
	private void launchAbout() {
		Intent i = new Intent(this, About.class);
		startActivity(i);
	}

	
}

class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
	public static final String TAG = "QuickConn";

	private ListView list;
	public void setList(ListView lst) 
	{
			this.list = lst;
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		
		Log.d(TAG, "ListView Item - onFling");

		long id = list.pointToRowId(Math.round(e1.getX()),Math.round(e1.getY()));
        Log.d(TAG, String.valueOf(id));
        return false;
	}

	@Override
	public void onLongPress(MotionEvent e)

	{
		Log.d(TAG, "ListView Item - onLongPress");
		HandleTouch ht = new HandleTouch();
		ht.myItemClick(id);

	}

	@Override
	public void onShowPress(MotionEvent e)

	{

		Log.d(TAG, "ListView Item - onShowPress");

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e)

	{

		Log.d(TAG, "ListView Item - onSingleTapUp");


		return true;

	}
	  @Override
	  
	    public boolean onDown(MotionEvent e)
	 
	    {
	 
		  Log.d(TAG, "ListView Item - onDown");
	 
	        return true;
	 
	    }
}