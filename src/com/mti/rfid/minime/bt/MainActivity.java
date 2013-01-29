package com.mti.rfid.minime.bt;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity implements FragInventory.OnTagSelectedListener {
	private static final boolean DEBUG = false;
	private static final String TAG = "MINIMEBT";
	private static TextView tv_readerstatus;
	
	private enum Fragments {About, Bluetooth, Config, Detail, Inventory, Web};
	private static boolean isPhone;
	private FragmentTransaction ft;
	private Fragment objFragment;

	protected static BtCommunication mBtComm = new BtCommunication();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        tv_readerstatus = (TextView)findViewById(R.id.tv_readerstatus);
    	isPhone = ((getResources().getConfiguration().smallestScreenWidthDp < 600) ? true : false);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        
        if(savedInstanceState == null) {
        	if(setConnectionStatus(mBtComm.checkConnectionStatus()))
				toggleFragment(Fragments.Inventory);
	        else
				toggleFragment(Fragments.Bluetooth);
    	} else
    		setConnectionStatus(mBtComm.checkConnectionStatus());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_fragment, menu);
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId())
		{
			case R.id.item_about:
				toggleFragment(Fragments.About);
				break;
			case R.id.item_bluetooth:
				toggleFragment(Fragments.Bluetooth);
				break;
			case R.id.item_config:
				toggleFragment(Fragments.Config);
				break;
			case R.id.item_tag:
				toggleFragment(Fragments.Inventory);
				break;
			case R.id.item_exit:
				finish();
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	public void onTagSelected(String strTag) {
		toggleFragment(Fragments.Detail, strTag);
	}
	
	@Override
	public void onTagLongPress(String strTag) {
		toggleFragment(Fragments.Web, strTag);
	}

	
	private void toggleFragment(Fragments fragmentType) {
		switch(fragmentType) {
			case About:
				objFragment = new FragAbout();
				break;
			case Bluetooth:
				objFragment = new FragBluetooth();
				break;
			case Config:
				objFragment = new FragConfig();
				break;
			case Inventory:
				objFragment = new FragInventory();
				break;
		}
		transitionFragment(fragmentType);
	}

	private void toggleFragment(Fragments fragmentType, String tagId) {
		switch(fragmentType) {
			case Detail:
				objFragment = new FragDetails(tagId);
				break;
			case Web:
				objFragment = new FragWeb(tagId);
				break;
		}
//		transitionFragment(fragmentType);
	}

	private void transitionFragment(Fragments fragmentType) {
		ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.fl_maincontainer, objFragment);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.addToBackStack(null);
		ft.commit();
	}
	

	public static BtCommunication getBtComm() {
		return mBtComm;
	}
	
	public static boolean checkConnectionStatus() {
		return setConnectionStatus(mBtComm.checkConnectionStatus());
	}

	public static boolean setConnectionStatus(boolean status) {
    	tv_readerstatus.setText(status ? "Connected" : "Disconnected");
		tv_readerstatus.setTextColor(status ? android.graphics.Color.GREEN : android.graphics.Color.RED);
		
		return status;
	}
	
	public static boolean isPhone() {
		return isPhone;
	}
}
