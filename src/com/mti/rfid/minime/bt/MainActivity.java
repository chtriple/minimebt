package com.mti.rfid.minime.bt;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final boolean DEBUG = false;
	private static final String TAG = "MINIMEBT";
	private static TextView tv_readerstatus;
	
	private enum Fragments {About, Bluetooth, Config, Detail, Inventory, Web};
	private FragmentTransaction ft;
	private Fragment objFragment;

	protected static BtCommunication mBtComm = new BtCommunication();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        tv_readerstatus = (TextView)findViewById(R.id.tv_readerstatus);
        if(setConnectionStatus(mBtComm.checkConnectionStatus()))
			toggleFragment(Fragments.Inventory, 0, null);
        else
			toggleFragment(Fragments.Bluetooth, 0, null);
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
				toggleFragment(Fragments.About, 0, null);
				break;
			case R.id.item_bluetooth:
				toggleFragment(Fragments.Bluetooth, 0, null);
				break;
			case R.id.item_config:
				toggleFragment(Fragments.Config, 0, null);
				break;
			case R.id.item_tag:
				toggleFragment(Fragments.Inventory, 0, null);
				break;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	
	private void toggleFragment(Fragments fragmentType, int index, String tagId) {
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
			case Detail:
				break;
			case Inventory:
				objFragment = new FragInventory();
				break;
			case Web:
				break;
		}
		
		ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.fl_maincontainer, objFragment);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
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
}
