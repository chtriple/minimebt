package com.mti.rfid.minime.bt;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

public class FragInventory extends ListFragment implements OnItemLongClickListener {
	private static final boolean DEBUG = false;
	private static final String TAG = "MINIMEBT";

	protected static ArrayList<String> alTags = new ArrayList<String>();
	protected static ArrayAdapter<String> aaTags;

	private View vFragment;
	private OnTagSelectedListener mListener;
	private SharedPreferences mSharedpref;

	public interface OnTagSelectedListener {
		public void onTagSelected(String strTag);
		public void onTagLongPress(String strTag);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mListener = (OnTagSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnTagSelectedListener");
		}
	}

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

        getListView().setOnItemLongClickListener(this);

        mSharedpref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		String strTag = getListTagId(position);
		
		if(onItemSelect(strTag)) {
			mListener.onTagSelected(strTag);
		} else {
			// #### process error ####
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> av, View v, int position, long id) {
	    String strTag = getListTagId(position);
	    mListener.onTagLongPress(strTag.replace(" ", ""));
		return true;
	}

	// #### select a tag ####
	private boolean onItemSelect(String tagId) {
		boolean bStatus = false;
		
		if(MainActivity.checkConnectionStatus()) {
	    	CmdIso18k6cTagAccess.RFID_18K6CTagSelect sendCmd = CmdIso18k6cTagAccess.RFID_18K6CTagSelect.newInstance();
			
			bStatus = sendCmd.setCmd(sendCmd.byteCmd(tagId.replace(" ", "")));
			
			if(!bStatus)
				Toast.makeText(getActivity(), "The Tag is not available, please try again.", Toast.LENGTH_SHORT).show();
		} else
			Toast.makeText(getActivity(), "The Reader is not connected", Toast.LENGTH_SHORT).show();
		return bStatus;
	}
	
	// #### get tag id ####
	private String getListTagId(int position) {
		return alTags.get(position).toString();
	}


	// #### inventory click ####
	private Button.OnClickListener btn_inventory_listener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			int scantimes = Integer.parseInt((mSharedpref.getString("cfg_inventory_times", "25")));
			if(MainActivity.checkConnectionStatus()) {
				aaTags.clear();
				new InventoryTask(getActivity(), true).execute(scantimes);
			} else
				Toast.makeText(getActivity(), "The reader is not connected", Toast.LENGTH_SHORT).show();
		}
	};
}
