package com.mti.rfid.minime.bt;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

public class MiniMeBtActivity extends Activity {
	static boolean DEBUG = true;
	static final String TAG = "MINIMEBT";
	static final int REQUEST_ENABLE_BT = 1;
	
	private static ScrollView sv_response;
	private TextView tv_response, et_send;
	private Button btn_toggle, btn_connect, btn_discover, btn_discoverable, btn_send, btn_clear, btn_exit;
	private Spinner sp_found;
	
	private BtCommunication mBtComm = new BtCommunication();

	private String msg = "";
	
	ArrayList<String> mListDevices = new ArrayList<String>();
	ArrayAdapter<String> mAdapterDevice;

	
	/* ############### override activity method ################# */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /* gui setting */
        sv_response = (ScrollView)findViewById(R.id.sv_response);
        tv_response = (TextView)findViewById(R.id.tv_response);
        et_send = (TextView)findViewById(R.id.et_send);
        
        btn_toggle = (Button)findViewById(R.id.btn_toggle);
        btn_connect = (Button)findViewById(R.id.btn_connect);
        btn_discover = (Button)findViewById(R.id.btn_discover);
        btn_discoverable = (Button)findViewById(R.id.btn_discoverable);
        btn_send = (Button)findViewById(R.id.btn_send);
        btn_clear = (Button)findViewById(R.id.btn_clear);
        btn_exit = (Button)findViewById(R.id.btn_exit);
        
        btn_toggle.setOnClickListener(btn_toggle_listener);
        btn_connect.setOnClickListener(btn_connect_listener);
        btn_discover.setOnClickListener(btn_discover_listener);
        btn_discoverable.setOnClickListener(btn_discoverable_listener);
        btn_send.setOnClickListener(btn_send_listener);
        btn_clear.setOnClickListener(btn_clear_listener);
        btn_exit.setOnClickListener(btn_exit_listener);
        
        sp_found = (Spinner)findViewById(R.id.sp_found);
        mAdapterDevice = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mListDevices);
        mAdapterDevice.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_found.setAdapter(mAdapterDevice);
        
        sp_found.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
				mBtComm.devSelection(((String)mListDevices.get(position)).split(" \\| ")[1]);
				adapterView.setVisibility(View.VISIBLE);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
        });

        /* register bluetooth broadcast */
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
//        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
//        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
    }
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(DEBUG) {
			String strResult = "";
			switch (resultCode) {
				case RESULT_OK:
					strResult = "RESULT_OK";
					break;
				case RESULT_CANCELED:
					strResult = "RESULT_CANCELED";
					break;
				case REQUEST_ENABLE_BT:
					strResult = "REQUEST_ENABLE_BT";
					break;
			}
			Log.d(TAG, " ## result code = " + strResult + " ##");
		}
	}

	
	@Override
	protected void onDestroy() {
		super.onDestroy();

		this.unregisterReceiver(mReceiver);
	}

	
	Handler btHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message inputMessage) {
			tv_response.append(BtCommunication.msg);
			sv_response.fullScroll(ScrollView.FOCUS_DOWN);
		}
	};
	

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
	
	/* search device */
	private Button.OnClickListener btn_discover_listener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			mAdapterDevice.clear();
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
	
	/* submit */
	private Button.OnClickListener btn_send_listener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			try {
				tv_response.append(et_send.getText().toString() + "\n");
				sv_response.fullScroll(ScrollView.FOCUS_DOWN);
				mBtComm.getOutputStream().write(et_send.getText().toString().getBytes());
				et_send.setText("");
//				btHandler.sendEmptyMessage(0);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	};
	
	/* clear */
	private Button.OnClickListener btn_clear_listener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			tv_response.setText(null);
			et_send.setText(null);
		}
	};
	
	/* exit */
	private Button.OnClickListener btn_exit_listener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
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
				if(mListDevices.indexOf(str) == -1) {
					mListDevices.add(str);
					mAdapterDevice.notifyDataSetChanged();
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
