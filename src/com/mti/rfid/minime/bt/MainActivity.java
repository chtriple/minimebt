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
	private TextView tv_readerstatus;
	
	private enum Fragments {About, Bluetooth, Config, Detail, Tag, Web};
	private FragmentTransaction ft;
	private Fragment objFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        tv_readerstatus = (TextView)findViewById(R.id.tv_readerstatus);
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
				break;
			case R.id.item_bluetooth:
				toggleFragment(Fragments.Bluetooth, 0, null);
				break;
			case R.id.item_config:
				break;
			case R.id.item_tag:
				break;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	
	private void toggleFragment(Fragments fragmentType, int index, String tagId) {
		switch(fragmentType) {
			case About:
				break;
			case Bluetooth:
				objFragment = new FragBluetooth();
				break;
			case Config:
				break;
			case Detail:
				break;
			case Tag:
				break;
			case Web:
				break;
		}
		
		ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.fl_maincontainer, objFragment);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
	}
}
