package com.oogeeya.quickconn;

import java.util.ArrayList;

import com.oogeeya.quickconn.R;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;





public class PhoneChooser extends Activity{
	
	private long ConnId = (long) 0;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// vars
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.phone_chooser);
		
		// get data from main activity
		Bundle extras = getIntent().getExtras(); 
		if(extras !=null)
		{
			ConnId = extras.getLong("ContactId");
		}

		// populate list with contact's phones
		final ListView Phonelist = (ListView) findViewById(R.id.phone_chooser_list);
	 	Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
		String[] selectionArgs = new String[] { String.valueOf(ConnId) };
        String sortOrder = null;
        Cursor cur = managedQuery(uri, null, selection, selectionArgs, sortOrder);
        String[] fields = new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.pchooser_entry, cur,
        		fields, new int[] {R.id.chooserEntryPhone});
        Phonelist.setAdapter(adapter);
    
        // handle item click
        OnItemClickListener listener = new OnItemClickListener() {
			public void onItemClick(AdapterView<?> a, View v, int position,	long id)
			{
				
				// unsuper primary all phone numbers
				 ArrayList<ContentProviderOperation> opsClearAll = new ArrayList<ContentProviderOperation>(); 
				 opsClearAll.add(ContentProviderOperation.newUpdate(Data.CONTENT_URI)
				          .withSelection(Data._ID + "=?", new String[]{String.valueOf(ConnId)})
				          .withValue(Data.IS_SUPER_PRIMARY,0)
				          .build());
				 try {
					getContentResolver().applyBatch(ContactsContract.AUTHORITY, opsClearAll);
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (OperationApplicationException e) {
					e.printStackTrace();
				}
				
				// super primary selected phone number
				ArrayList<ContentProviderOperation> opsSetSelected = new ArrayList<ContentProviderOperation>(); 
				 opsSetSelected.add(ContentProviderOperation.newUpdate(Data.CONTENT_URI)
				          .withSelection(Data._ID + "=?", new String[]{String.valueOf(id)})
				          .withValue(Data.IS_SUPER_PRIMARY,1)
				          .build());
				 try {
					getContentResolver().applyBatch(ContactsContract.AUTHORITY, opsSetSelected);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OperationApplicationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				
				finish();

				
			}
		};
    	 
		Phonelist.setOnItemClickListener(listener);
        
	}
	
	 protected void onStop()
	    {
	    	super.onStop();
	    	finish();
	    }

}
