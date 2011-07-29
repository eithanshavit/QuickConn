package com.oogeeya.quickconn;

import java.io.InputStream;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

// main activity, favorite contacts list
public class quickconn extends Activity {
	
	// set log tag
	public static final String TAG = "QuickConn";
	private boolean FirstRun;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// add different comment
		// log activity state
		Log.d(TAG, "Activity State: onCreate()");
		super.onCreate(null);
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
		// end check first run.

		// populate the contact list
		populateContactList();
		
	}
	
	
	// launch phone number chooser activity
	public void launchPhoneChooser(long ContactId) {
		
		Intent intent = new Intent(this, PhoneChooser.class);
		Log.d("QuickConn", "launchPhoneChooser, id = " + ContactId);
		intent.putExtra("ContactId", ContactId);
		startActivity(intent);
		
	}
	
	// calls a number
	private void callNumber(String phone) {
		toast("Calling " + phone);
		try {
			Intent intent = new Intent(Intent.ACTION_CALL);
			intent.setData(Uri.parse("tel:" + phone));
			finish();
			startActivity(intent);
		} catch (Exception e) {
			toast("Problem calling number.");
		}

	}
	
	// text a number
	private void textNumber(String phone) {
		toast("Texting " + phone);
		try {
			Intent sendIntent= new Intent(Intent.ACTION_VIEW); 
			sendIntent.putExtra("address",  phone); 
			sendIntent.setType("vnd.android-dir/mms-sms"); 
			sendIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			sendIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(sendIntent); 
		} catch (Exception e) {
			toast("Problem Texting number.");
		}

	}
	
	
	// easily create toast
	private void toast(String str) {
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}
	
	private Cursor getPhonesById(long id)
	{
		ContentResolver cr = getContentResolver();
		Cursor cur = cr.query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID
						+ " = ?", new String[] { String.valueOf(id) },
				null);
		return cur;
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

	// Populate the contact list
	private void populateContactList() {
		Uri uri = ContactsContract.Contacts.CONTENT_URI;
		String[] projection = new String[] { ContactsContract.Contacts._ID,
				ContactsContract.Data.PHOTO_ID,
				ContactsContract.Contacts.DISPLAY_NAME };
		String selection = ContactsContract.Contacts.STARRED;
		String[] selectionArgs = null;
		String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
				+ " COLLATE LOCALIZED ASC";
		Cursor myCursor = getContentResolver().query(uri, projection,
				selection, selectionArgs, sortOrder);
		startManagingCursor(myCursor);

		String[] fields = new String[] { ContactsContract.Data.DISPLAY_NAME };
		int[] names = new int[] { R.id.contactEntryName };
		MyCursorAdapter myAdapter = new MyCursorAdapter(
				getApplicationContext(), R.layout.contact_entry, myCursor,
				fields, names);
		ListView ConList = (ListView) findViewById(R.id.StarredList);
		ConList.setAdapter(myAdapter);

		// handle list select
		OnItemClickListener listener = new OnItemClickListener() {
			public void onItemClick(AdapterView<?> a, View v, int position,
					long id) {
				Log.d(TAG, "onItemClick, id = " + id);
				// vars
				String phone;

				// get contact's phone numbers
				Cursor cur = getPhonesById(id);

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
				Log.d(TAG, "onItemLongClick, id = " + id);
				// get contact's phone numbers
				Cursor cur = getPhonesById(id);

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

		ConList.setOnItemLongClickListener(listenerLong);
		ConList.setOnItemClickListener(listener);
	}
	
	// click text button
	void onItemTxtClick(long id)
	{
		Log.d(TAG, "onItemTxtClick, id = " + id);
		// vars
		String phone;

		// get contact's phone numbers
		Cursor cur = getPhonesById(id);

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
			if (phone != null) // text if found
			{
				textNumber(phone);
			} else // if not found choose default and check again
			{
				launchPhoneChooser(id);	
			}
		} else // if only one number text it
		{
			cur.moveToNext();
			textNumber(cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
		}
		cur.close();
	}

	// launch about activity
	private void launchAbout() {
		Intent i = new Intent(this, About.class);
		startActivity(i);
	}

	// onStop
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		finish();
	}
	
	// onRestart
	protected void onRestart() {
		Log.d(TAG, "onRestart");
		super.onRestart();
		
	}
	
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
	
	// class to retrieve contact photos
	class MyCursorAdapter extends SimpleCursorAdapter {
		private Cursor cCursor;
		private Context cContext;

		public MyCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
			cCursor = c;
			cContext = context;
		}
		


		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			// get contact id
			final long id = (long) cCursor.getInt(cCursor
					.getColumnIndex(ContactsContract.Contacts._ID));

			// get image view
			ImageView imageViewPhoto = (ImageView) view.findViewById(R.id.contactImage);
			ImageView imageViewTxt = (ImageView) view.findViewById(R.id.contactTxtImage);
			// get contact's photo
			Bitmap photo = BitmapFactory.decodeStream(openPhoto(id));
			if (photo!=null)
			{
				if (photo.getWidth()<60)
				{
					photo = Bitmap.createScaledBitmap(photo, 90, 90, true);
				}
				photo = getRoundedCornerBitmap(photo,18);
			}
			// set image to view
				imageViewPhoto.setImageBitmap(photo);
	

			// set listener for text image
			imageViewTxt.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View view) {
					Log.d(TAG, "List Txt click, id = " + String.valueOf(id));
					onItemTxtClick(id);
				}				
			});

			super.bindView(view, context, cursor);
		}
			
		

		private InputStream openPhoto(long contactId) {
			Uri contactUri = ContentUris.withAppendedId(
					ContactsContract.Contacts.CONTENT_URI, contactId);
			InputStream input = ContactsContract.Contacts
					.openContactPhotoInputStream(cContext.getContentResolver(),
							contactUri);
			return input;
		}

	}
}


