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
		OnItemClickListener listener = new OnItemClickListener() {
			public void onItemClick(AdapterView<?> a, View v, int position,
					long id) {
				// vars
				String phone;

				// get contact's phone numbers
				ContentResolver cr = getContentResolver();
				Cursor cur = cr.query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID
								+ " = ?", new String[] { String.valueOf(id) },
						null);

				// check number of phones for contact
				if (cur.getCount() == 0) // no phone numbers
				{
					toast(getString(R.string.phone_chooser_no_numbers));
					cur.close();
					return;
				} else if (cur.getCount() > 1) // more then 1 number
				{
					// look for super primary entry
					phone = lookForPrimaryPhone(cur);
					if (phone != null) // call if found
					{
						callNumber(phone);
					} else // if not found choose default and check again
					{
						launchPhoneChooser(id);
						phone = lookForPrimaryPhone(cur);
						if (phone != null)
							callNumber(phone);
					}
				} else // if only one number call it
				{
					cur.moveToNext();
					callNumber(cur
							.getString(cur
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
				}
				cur.close();

			}
		};

		OnItemLongClickListener listenerLong = new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> a, View v,
					int position, long id) {
				// vars

				// get contact's phone numbers
				ContentResolver cr = getContentResolver();
				Cursor cur = cr.query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID
								+ " = ?", new String[] { String.valueOf(id) },
						null);

				// check number of phones for contact
				if (cur.getCount() > 1) // more then 1 number
				{
					launchPhoneChooser(id);

				}
				if (cur.getCount() == 0) {
					toast(getString(R.string.phone_chooser_no_numbers));
				}
				if (cur.getCount() == 1) {
					toast(getString(R.string.phone_chooser_one_number));
				}
				cur.close();
				return true;

			}
		};
		mGestureListener = new MyGestureListener();
		mGestureDetector = new GestureDetector(this,mGestureListener);
	//	ConList.setOnItemLongClickListener(listenerLong);
	//	ConList.setOnItemClickListener(listener);
	//	ConList.setOnTouchListener(this);

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.d(TAG,"onTouch");
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

	// looks for primary phone and return number
	// returns null if not found
	private String lookForPrimaryPhone(Cursor cur) {
		while (cur.moveToNext()) {
			if (cur.getInt(cur
					.getColumnIndex(ContactsContract.Data.IS_SUPER_PRIMARY)) == 1) {
				return cur
						.getString(cur
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			}
		}
		return null;
	}

	// calls a number and finishes
	private void callNumber(String phone) {
		finish();
		Toast.makeText(getApplicationContext(), "Calling " + phone,
				Toast.LENGTH_SHORT).show();
		try {
			Intent intent = new Intent(Intent.ACTION_CALL);
			intent.setData(Uri.parse("tel:" + phone));
			//startActivity(intent);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "Problem calling number.",
					Toast.LENGTH_LONG).show();
		}

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

	// launch phone number chooser activity
	public void launchPhoneChooser(long ContactId) {
		Intent i = new Intent(this, PhoneChooser.class);
		i.putExtra("ContactId", ContactId);
		startActivity(i);
	}

	// launch about activity
	private void launchAbout() {
		Intent i = new Intent(this, About.class);
		startActivity(i);
	}

	// easily create toast
	private void toast(String str) {
		Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
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