package com.mti.rfid.minime.bt;

import java.io.IOException;
import java.util.Collections;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.widget.Toast;

public class InventoryTask extends AsyncTask<Integer, CharSequence, Void> {
	private Context context;
	private Activity activity;
	private ProgressDialog dialog;
	private boolean isPhone;
	private SharedPreferences mSharedpref;
	
	private boolean webState;
	private String prefixUrl;
	private Toast toast;
	
	public InventoryTask(Context cxt, boolean devType) {
		context = cxt;
		activity = (Activity)cxt;
		dialog = new ProgressDialog(context);
/*
		isPhone = devType;
		mSharedpref = PreferenceManager.getDefaultSharedPreferences(context);

		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()     
        .detectDiskReads()     
        .detectDiskWrites()     
        .detectNetwork()     
        .penaltyLog()     
        .build());     
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()     
        .detectLeakedSqlLiteObjects()     
        .detectLeakedClosableObjects()     
        .penaltyLog()     
        .penaltyDeath()     
        .build());

		webState = true;
	    prefixUrl = mSharedpref.getString("cfg_web_url", "");
		
	    if(prefixUrl == "") {
			Toast.makeText(activity, "There is no Web Url setting in the configuration page!", Toast.LENGTH_SHORT).show();
			webState = false;
	    }
*/
}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
	    dialog.setTitle("Inventory");
		dialog.setMessage("Searching...");
		dialog.show();
//		setOrientationSensor(false);
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		
		Collections.sort(FragInventory.alTags);
		FragInventory.aaTags.notifyDataSetChanged();
		dialog.dismiss();
//		setOrientationSensor(true);
	}

	@Override
	protected void onProgressUpdate(CharSequence... values) {
		super.onProgressUpdate(values);
		FragInventory.aaTags.notifyDataSetChanged();
//		Toast.makeText(activity, values[0], Toast.LENGTH_SHORT).show();
//		txWebServer((String) values[0]);
	}

	@Override
	protected Void doInBackground(Integer... scantimes) {
		int numTags;
		String tagId;

		FragInventory.alTags.clear();
    	for(int i = 0; i < scantimes[0]; i++) {
//    		mMtiCmd = new CmdIso18k6cTagAccess.RFID_18K6CTagInventory();
			CmdIso18k6cTagAccess.RFID_18K6CTagInventory sendCmd = CmdIso18k6cTagAccess.RFID_18K6CTagInventory.newInstance();
			
			if(sendCmd.setCmd(CmdIso18k6cTagAccess.Action.StartInventory)) {
				tagId = sendCmd.getTagId();
				if(sendCmd.getTagNumber() > 0) {
					if(!FragInventory.alTags.contains(tagId)) {
						FragInventory.alTags.add(tagId);
						publishProgress(tagId);
					}
//						sendCmd.setCmd(CMD_Iso18k6cTagAccess.Action.GetAllTags);
				}
				
				for(numTags = sendCmd.getTagNumber(); numTags > 1; numTags--) {
					if(sendCmd.setCmd(CmdIso18k6cTagAccess.Action.NextTag)) {
						tagId = sendCmd.getTagId();
						if(!FragInventory.alTags.contains(tagId)) {
							FragInventory.alTags.add(tagId);
							publishProgress(tagId);
						}
					}
				}
			} else {
				// #### process error ####
			}
    	}
		return null;
	}

	private void setOrientationSensor(boolean status) {
		if(status)
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
		else {
			switch(activity.getWindowManager().getDefaultDisplay().getRotation()) {
				case 0:
					activity.setRequestedOrientation(isPhone ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					break;
				case 1:
					activity.setRequestedOrientation(isPhone ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
					break;
				case 2:
					activity.setRequestedOrientation(isPhone ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
					break;
				case 3:
					activity.setRequestedOrientation(isPhone ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					break;
			}
		}
	}

	private void txWebServer(String tagId) {
		String uriApi;
		
	    if(webState) {
				uriApi = prefixUrl + tagId.replace(" ", "");
			try {
				HttpGet httpRequest = new HttpGet(uriApi);
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
				HttpConnectionParams.setSoTimeout(httpParameters, 2000);
				DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
				HttpResponse httpResponse = httpClient.execute(httpRequest);
				if(httpResponse.getStatusLine().getStatusCode() == 200) {
					Toast.makeText(activity, "Sending  [ " + tagId + "]", Toast.LENGTH_SHORT).show();
				} else {
					saveMissData(tagId);
					Toast.makeText(activity, "Can't connect to server!!", Toast.LENGTH_SHORT).show();
					return;
				}
			} catch (ClientProtocolException e) {
				saveMissData(tagId);
				Toast.makeText(activity, "Can't connect to server!!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
				return;
			} catch (IOException e) {
				saveMissData(tagId);
				Toast.makeText(activity, "Can't connect to server!!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
				return;
			} catch (Exception e) {
				saveMissData(tagId);
				Toast.makeText(activity, "Can't connect to server!!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
				return;
			}
	    } else
	    	saveMissData(tagId);
	}
	
	private void saveMissData(String tagId) {
		webState = false;
	}
}
