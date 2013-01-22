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
import android.widget.TextView;

public class FragBluetooth extends ListFragment {
	private static final boolean DEBUG = true;
	private static final String TAG = "MINIMEBT";
	private static final int REQUEST_ENABLE_BT = 1;
	
	private ArrayList<String> alBtDevice = new ArrayList<String>();
	private ArrayAdapter<String> aaBtDevice;
	
	private View vFragment;
//	private TextView tv_readerstatus;

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

        Button btn_toggle = (Button)vFragment.findViewById(R.id.btn_toggle);
        Button btn_discoverable = (Button)vFragment.findViewById(R.id.btn_discoverable);
        Button btn_discover = (Button)vFragment.findViewById(R.id.btn_discover);
        Button btn_connect = (Button)vFragment.findViewById(R.id.btn_connect);

        btn_toggle.setOnClickListener(btn_toggle_listener);
        btn_discoverable.setOnClickListener(btn_discoverable_listener);
        btn_discover.setOnClickListener(btn_discover_listener);
        btn_connect.setOnClickListener(btn_connect_listener);
	}


	@Override
	public void onDestroy() {
		super.onDestroy();

		getActivity().unregisterReceiver(mReceiver);
	}


	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		mBtComm.devSelection(((String)alBtDevice.get(position)).split(" \\| ")[1]);
		mBtComm.createConnectThread();
	}



	/* ############################# button listener ################################## */
	/* toggle bluetooth */
	private Button.OnClickListener btn_toggle_listener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(mBtComm.getBtAdapter().isEnabled()) {
				if(mBtComm.getConnectThread() != null)
					mBtComm.cancelConnectThread();
				if(mBtComm.getAcceptThread() != null)
					mBtComm.cancelAcceptThread();
				mBtComm.getBtAdapter().disable();
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
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
			startActivity(discoverableIntent);
	        if(mBtComm.getBtAdapter() != null) {
	        	mBtComm.createAcceptThread();
	        }

		}
	};
	
	/* search device */
	private Button.OnClickListener btn_discover_listener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			aaBtDevice.clear();
			mBtComm.getBtAdapter().cancelDiscovery();
			mBtComm.getBtAdapter().startDiscovery();
		}
	};
	
	/* connect to device */
	private Button.OnClickListener btn_connect_listener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			mBtComm.createConnectThread();
		}
	};
	

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
