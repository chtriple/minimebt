package com.mti.rfid.minime.bt;

import java.util.ArrayList;

import android.app.ListFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class FragBluetooth extends ListFragment {
	private static final boolean DEBUG = false;
	private static final String TAG = "MINIMEBT";
	private static final int REQUEST_ENABLE_BT = 1;
	private static final int DISCOVERABLE_BT = 2;
	
	private static ArrayList<String> alBtDevice = new ArrayList<String>();
	private static ArrayAdapter<String> aaBtDevice;
	
	private View vFragment;
	private Button btn_toggle;
	private Button btn_discoverable;
	private Button btn_discover;
	private Button btn_disconnect;
	
	private BtCommunication mBtComm = MainActivity.getBtComm();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        /* register bluetooth broadcast */
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
//        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
//        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        getActivity().registerReceiver(mReceiver, filter);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		aaBtDevice = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, alBtDevice);
		setListAdapter(aaBtDevice);
		vFragment = inflater.inflate(R.layout.frag_bluetooth, container, false);

        return vFragment;
	}
	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        btn_toggle = (Button)vFragment.findViewById(R.id.btn_toggle);
        btn_discoverable = (Button)vFragment.findViewById(R.id.btn_discoverable);
        btn_discover = (Button)vFragment.findViewById(R.id.btn_discover);
        btn_disconnect = (Button)vFragment.findViewById(R.id.btn_disconnect);

        btn_toggle.setOnClickListener(btn_toggle_listener);
        btn_discoverable.setOnClickListener(btn_discoverable_listener);
        btn_discover.setOnClickListener(btn_discover_listener);
        btn_disconnect.setOnClickListener(btn_disconnect_listener);
        
        if(mBtComm.checkConnectionStatus())
        	setButtonStatus(false, false, true);
        else
        	setButtonStatus(true, true, false);
	}


	@Override
	public void onDestroy() {
		super.onDestroy();

		getActivity().unregisterReceiver(mReceiver);
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
			case REQUEST_ENABLE_BT:
	        	setButtonStatus(true, true, false);
				if(DEBUG) Log.d(TAG, "REQUEST_ENABLE_BT");
				break;
			case DISCOVERABLE_BT:
				if(resultCode == 120 && mBtComm.getBtAdapter() != null)
			        mBtComm.createAcceptThread();

				if(mBtComm.getBtAdapter().isEnabled())
					setButtonStatus(true, true, false);
		        else
		        	setButtonStatus(true, false, false);

		        if(DEBUG) Log.d(TAG, "DISCOVERABLE_BT");
		        break;
		}
	}


	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		String strDevice = (String)alBtDevice.get(position);
		mBtComm.devSelection((strDevice).split(" \\| ")[1]);
		mBtComm.createConnectThread();
		setButtonStatus(false, false, true);
		
		alBtDevice.clear();
		alBtDevice.add(strDevice);
		aaBtDevice.notifyDataSetChanged();

	}


	/* ############################# button listener ################################## */
	/* toggle bluetooth */
	private Button.OnClickListener btn_toggle_listener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(mBtComm.getBtAdapter().isEnabled()) {
				mBtComm.disconnect();
				mBtComm.getBtAdapter().disable();
				MainActivity.setConnectionStatus(false);
	        	setButtonStatus(true, false, false);
			} else {
				Intent enBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enBtIntent, REQUEST_ENABLE_BT);
			}
		}
	};
	
	/* set discoverable */
	private Button.OnClickListener btn_discoverable_listener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
	        if(mBtComm.checkConnectionStatus()) {
				setButtonStatus(false, false, true);
	        } else {
				Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
				startActivityForResult(discoverableIntent, DISCOVERABLE_BT);
	        }
		}
	};
	
	/* search device */
	private Button.OnClickListener btn_discover_listener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
	        if(mBtComm.checkConnectionStatus()) {
				setButtonStatus(false, false, true);
	        } else {
				aaBtDevice.clear();
				mBtComm.getBtAdapter().cancelDiscovery();
				mBtComm.getBtAdapter().startDiscovery();
				setButtonStatus(true, true, false);
	        }
		}
	};
	
	/* disconnect to device */
	private Button.OnClickListener btn_disconnect_listener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			mBtComm.disconnect();
        	setButtonStatus(true, true, false);
		}
	};
	

	private void setButtonStatus(boolean bDiscoverable, boolean bDiscover, boolean bDisconnect) {
        btn_discoverable.setEnabled(bDiscoverable);
        btn_discover.setEnabled(bDiscover);
        btn_disconnect.setEnabled(bDisconnect);
	}
	
	/* ########################### broadcast receiver ############################ */
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			BluetoothDevice device = null;
			BluetoothAdapter adapter = null;
			
			/* discovery found a device */
			if(BluetoothDevice.ACTION_FOUND.equals(action)) {
				device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String str = device.getName() + " | " + device.getAddress();
				if(alBtDevice.indexOf(str) == -1) {
					alBtDevice.add(str);
					aaBtDevice.notifyDataSetChanged();
				}
			} else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
				device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				switch(device.getBondState()) {
					case BluetoothDevice.BOND_BONDING:
						Log.d(TAG, "paring now...");
						break;
					case BluetoothDevice.BOND_BONDED:
						Log.d(TAG, "pare complete");
						break;
					case BluetoothDevice.BOND_NONE:
						Log.d(TAG, "pare cancelled");
						break;
					default:
						break;
				}
			} else if(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
				adapter = intent.getParcelableExtra(BluetoothAdapter.EXTRA_SCAN_MODE);
				switch(adapter.getScanMode()) {
					case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
						Log.d(TAG, "connectable discoverable mode");
						break;
					case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
						Log.d(TAG, "connectable mode");
						break;
					case BluetoothAdapter.SCAN_MODE_NONE:
						Log.d(TAG, "none scan mode");
						break;
					default:
						break;
				}
			}
		}
	};

}
