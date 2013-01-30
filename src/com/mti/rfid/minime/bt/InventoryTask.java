package com.mti.rfid.minime.bt;

import java.util.Collections;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.util.Log;

public class InventoryTask extends AsyncTask<Integer, CharSequence, Void> {
	private static final boolean DEBUG = false;
	private static final String TAG = "MINIMEBT";

	private Context context;
	private Activity activity;
	private ProgressDialog dialog;
	
	public InventoryTask(Context cxt, boolean devType) {
		context = cxt;
		activity = (Activity)cxt;
		dialog = new ProgressDialog(context);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
	    dialog.setTitle("Inventory");
		dialog.setMessage("Searching...");
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		setOrientationSensor(false);
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		
		Collections.sort(FragInventory.alTags);
		FragInventory.aaTags.notifyDataSetChanged();
		dialog.dismiss();
		setOrientationSensor(true);
	}

	@Override
	protected void onProgressUpdate(CharSequence... values) {
		super.onProgressUpdate(values);
		FragInventory.aaTags.notifyDataSetChanged();
	}

	@Override
	protected Void doInBackground(Integer... scantimes) {
		int numTags;
		String tagId;
		ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

		FragInventory.alTags.clear();
    	for(int i = 0; i < scantimes[0]; i++) {
			CmdIso18k6cTagAccess.RFID_18K6CTagInventory sendCmd = CmdIso18k6cTagAccess.RFID_18K6CTagInventory.newInstance();
			
			if(sendCmd.setCmd(CmdIso18k6cTagAccess.Action.StartInventory)) {
				tagId = sendCmd.getTagId();
				if(sendCmd.getTagNumber() > 0) {
					tg.startTone(ToneGenerator.TONE_PROP_BEEP);
					if(!FragInventory.alTags.contains(tagId)) {
						FragInventory.alTags.add(tagId);
						publishProgress(tagId);
					}
//						sendCmd.setCmd(CMD_Iso18k6cTagAccess.Action.GetAllTags);
				}
				
				for(numTags = sendCmd.getTagNumber(); numTags > 1; numTags--) {
					if(sendCmd.setCmd(CmdIso18k6cTagAccess.Action.NextTag)) {
						tg.startTone(ToneGenerator.TONE_PROP_BEEP);
						tagId = sendCmd.getTagId();
						if(!FragInventory.alTags.contains(tagId)) {
							FragInventory.alTags.add(tagId);
							publishProgress(tagId);
						}
					}
				}
			} else {
				Log.d(TAG, "inventory error");
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
					activity.setRequestedOrientation(MainActivity.isPhone() ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					break;
				case 1:
					activity.setRequestedOrientation(MainActivity.isPhone() ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
					break;
				case 2:
					activity.setRequestedOrientation(MainActivity.isPhone() ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
					break;
				case 3:
					activity.setRequestedOrientation(MainActivity.isPhone() ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					break;
			}
		}
	}
}
