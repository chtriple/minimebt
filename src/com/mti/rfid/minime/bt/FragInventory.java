package com.mti.rfid.minime.bt;

import java.util.ArrayList;

import android.app.ListFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

public class FragInventory extends ListFragment {
	private static final boolean DEBUG = true;
	private static final String TAG = "MINIMEBT";

	protected static ArrayList<String> alTags = new ArrayList<String>();
	protected static ArrayAdapter<String> aaTags;

	private View vFragment;

	private BtCommunication mBtComm = MainActivity.getBtComm();
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		aaTags = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, alTags);
		setListAdapter(aaTags);
		vFragment = inflater.inflate(R.layout.frag_inventory, container, false);

        return vFragment;
	}
	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        Button btn_inventory = (Button)vFragment.findViewById(R.id.btn_inventory);

        btn_inventory.setOnClickListener(btn_inventory_listener);
	}


	/* inventory click */
	private Button.OnClickListener btn_inventory_listener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
//			if(getUsbState()) {
				aaTags.clear();
				new InventoryTask(getActivity(), true).execute(20);
//			} else
//				Toast.makeText(getActivity(), "The Reader is not connected", Toast.LENGTH_SHORT).show();
		}
	};

}
