package com.mti.rfid.minime.bt;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class FragConfig extends PreferenceFragment{
	private static int mTagMode = 0;		// 0:Gen2, 1:Gen2+RSSI, 2:ISO6B

	public PreferenceScreen prefScr;
	
	public static FragConfig newInstance() {
		FragConfig f = new FragConfig();
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.frag_config);
        
        prefScr = getPreferenceScreen();

		initListPreference("cfg_region");
		initListPreference("cfg_tag_mode");
		initEditTextPreference("cfg_pwr_level");
		initEditTextPreference("cfg_sen");
		initListPreference("cfg_link_freq");
		initListPreference("cfg_session");
		initListPreference("cfg_coding");
		initEditTextPreference("cfg_q_begin");
		initEditTextPreference("cfg_tid_length");
		initEditTextPreference("cfg_user_length");
		initCheckBoxPreference("cfg_sleep_mode");
		initEditTextPreference("cfg_inventory_times");
		initEditTextPreference("cfg_web_url");
		
		if(savedInstanceState == null) {
        	if(MainActivity.checkConnectionStatus()){
				getReaderRegion();
				getTagMode();
				getPowerLevel();
				get18c6Config();
//				setPowerState();
			} else {
				Toast.makeText(getActivity(), "The Reader is not connected", Toast.LENGTH_SHORT).show();
			}
		}
	}

	
	private void initListPreference(final String strPrefName) {
        ListPreference lPref = (ListPreference) prefScr.findPreference(strPrefName);
        lPref.setSummary(lPref.getValue());
        
		lPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				ListPreference lPref = (ListPreference) preference;
				String strPref = newValue.toString();
					
				lPref.setValue(strPref);
				lPref.setSummary(strPref);
				
				if(strPrefName.equals("cfg_region"))
					setReaderRegion();
				else if(strPrefName.equals("cfg_tag_mode"))
					setTagMode();
				else if(strPrefName.equals("cfg_link_freq") || strPrefName.equals("cfg_session") || strPrefName.equals("cfg_coding"))
					set18c6Config();
//				setPowerState();
				return false;
			}
		});
	}
	
	
	private void initEditTextPreference(final String strPrefName){
		EditTextPreference etPref = (EditTextPreference) prefScr.findPreference(strPrefName);
        etPref.setSummary(etPref.getText());
		etPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				EditTextPreference etPref = (EditTextPreference) preference;
				String strPref = newValue.toString();
				
				etPref.setText(strPref);
				etPref.setSummary(strPref);
				
				if(strPrefName.equals("cfg_pwr_level"))
					setPowerLevel();
				else if(strPrefName.equals("cfg_sen") || strPrefName.equals("cfg_q_begin"))
					set18c6Config();
				else if(strPrefName.equals("cfg_inventory_times")) {
//					setInventoryTimes();
				}
//				setPowerState();
				return false;
			}
		});
	}


	private void initCheckBoxPreference(final String strPrefName) {
        CheckBoxPreference cbPref = (CheckBoxPreference) prefScr.findPreference(strPrefName);
        
		cbPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(strPrefName.equals("cfg_sleep_mode")) {
					if(MainActivity.checkConnectionStatus()){
				    	CmdPwrMgt.RFID_PowerEnterPowerState sendCmd = CmdPwrMgt.RFID_PowerEnterPowerState.newInstance();
						if((Boolean)newValue)
							sendCmd.setCmd(CmdPwrMgt.PowerState.Sleep);
						else
							sendCmd.setCmd(CmdPwrMgt.PowerState.Standby);
			        }
				}
				return true;
			}
		});
	}
	
	
	private void setReaderRegion() {
		ListPreference lPref = (ListPreference) prefScr.findPreference("cfg_region");

    	CmdModConf.RFID_RadioSetRegion sendCmd = CmdModConf.RFID_RadioSetRegion.newInstance();
		
		if(sendCmd.setCmd((byte)lPref.findIndexOfValue(lPref.getValue()))) {
			lPref.setSummary(lPref.getValue());
		} else {
			getReaderRegion();
			// #### some module not support for this feature ####
//			Toast.makeText(getActivity(), "The region is not support.", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void getReaderRegion() {
    	CmdModConf.RFID_RadioGetRegion sendCmd = CmdModConf.RFID_RadioGetRegion.newInstance();

		if(sendCmd.setCmd()) {
			ListPreference lPref = (ListPreference) prefScr.findPreference("cfg_region");
        
			lPref.setValueIndex(sendCmd.getRegion());
			lPref.setSummary(lPref.getValue());
		} else {
			// #### process error ####
		}
	}

	private void setTagMode() {
		ListPreference lPref = (ListPreference) prefScr.findPreference("cfg_tag_mode");
	}

	private void getTagMode() {
		ListPreference lPref = (ListPreference) prefScr.findPreference("cfg_tag_mode");
		
		lPref.setSummary(lPref.getValue());
	}

	
	private void setPowerLevel() {
		EditTextPreference etPref = (EditTextPreference) prefScr.findPreference("cfg_pwr_level");

    	CmdAntPortOp.RFID_AntennaPortSetPowerLevel sendCmd = CmdAntPortOp.RFID_AntennaPortSetPowerLevel.newInstance();
		
		if(sendCmd.setCmd(Byte.parseByte(etPref.getText()))) {
			etPref.setSummary(etPref.getText());
		} else {
			getPowerLevel();
			// #### some module not support for this feature ####
//			Toast.makeText(getActivity(), "The Power is out of range.", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void getPowerLevel() {
    	CmdAntPortOp.RFID_AntennaPortGetPowerLevel sendCmd = CmdAntPortOp.RFID_AntennaPortGetPowerLevel.newInstance();

		if(sendCmd.setCmd()) {
			EditTextPreference etPref = (EditTextPreference) prefScr.findPreference("cfg_pwr_level");
        
			etPref.setText(String.valueOf(sendCmd.getPowerLevel()));
			etPref.setSummary(etPref.getText());
		} else {
			// #### process error ####
		}
	}
	
	private void set18c6Config() {
		EditTextPreference etPref;
		ListPreference lPref;
		
    	CmdIso18k6cTagAccess.RFID_18K6CSetQueryParameter sendCmd = CmdIso18k6cTagAccess.RFID_18K6CSetQueryParameter.newInstance();
		
		etPref = (EditTextPreference) prefScr.findPreference("cfg_sen");
		byte bSensitivity = Byte.parseByte(etPref.getText());
		
		lPref = (ListPreference) prefScr.findPreference("cfg_link_freq");
		byte bLinkFreq = 0x0;
		switch(lPref.findIndexOfValue(lPref.getValue())) {
			case 0:
				bLinkFreq = 0x00; break;
			case 1:
				bLinkFreq = 0x06; break;
			case 2:
				bLinkFreq = 0x08; break;
			case 3:
				bLinkFreq = 0x09; break;
			case 4:
				bLinkFreq = 0x0C; break;
			case 5:
				bLinkFreq = 0x0F; break;
		}

		lPref = (ListPreference) prefScr.findPreference("cfg_session");
		byte bSession = (byte)lPref.findIndexOfValue(lPref.getValue());
		
		lPref = (ListPreference) prefScr.findPreference("cfg_coding");
		byte bCoding = (byte)lPref.findIndexOfValue(lPref.getValue());
		
		etPref = (EditTextPreference) prefScr.findPreference("cfg_q_begin");
		byte bQBegin = Byte.parseByte(etPref.getText());

		if(sendCmd.setCmd((byte)0x01, bLinkFreq, (byte)0x01, bCoding,
				(byte)0x01, bSession, (byte)0x00, (byte)0x00,
				(byte)0x01, bQBegin, (byte)0x01, bSensitivity)) {
		} else {
			// #### process error ####
		}
	}
	
	private void get18c6Config() {
		EditTextPreference etPref;
		ListPreference lPref;
		
    	CmdIso18k6cTagAccess.RFID_18K6CSetQueryParameter sendCmd = CmdIso18k6cTagAccess.RFID_18K6CSetQueryParameter.newInstance();
		
		if(sendCmd.setCmd()) {
			String strTmp;
			
			etPref = (EditTextPreference) prefScr.findPreference("cfg_sen");
			strTmp = String.valueOf(sendCmd.getSensivity());
			etPref.setText(strTmp);
			etPref.setSummary(strTmp);
			
			lPref = (ListPreference) prefScr.findPreference("cfg_link_freq");
			switch(sendCmd.getLinkFrequency()) {
				case 0x00:
					lPref.setValueIndex(0); break;
				case 0x06:
					lPref.setValueIndex(1); break;
				case 0x08:
					lPref.setValueIndex(2); break;
				case 0x09:
					lPref.setValueIndex(3); break;
				case 0x0C:
					lPref.setValueIndex(4); break;
				case 0x0F:
					lPref.setValueIndex(5); break;
			}
			lPref.setSummary(lPref.getValue());

			lPref = (ListPreference) prefScr.findPreference("cfg_session");
			lPref.setValueIndex(sendCmd.getSession());
			lPref.setSummary(lPref.getValue());
						
			lPref = (ListPreference) prefScr.findPreference("cfg_coding");
			lPref.setValueIndex(sendCmd.getCoding());
			lPref.setSummary(lPref.getValue());
						
			etPref = (EditTextPreference) prefScr.findPreference("cfg_q_begin");
			etPref.setText(String.valueOf(sendCmd.getQBegin()));
			etPref.setSummary(etPref.getText());

		} else {
			// #### process error ####
		}
	}
	
	private void setPowerState() {
		if(MainActivity.checkConnectionStatus()) {
	    	CmdPwrMgt.RFID_PowerEnterPowerState sendCmd = CmdPwrMgt.RFID_PowerEnterPowerState.newInstance();

			CheckBoxPreference cbPref = (CheckBoxPreference) prefScr.findPreference("cfg_sleep_mode");

			if(cbPref.isChecked())
				sendCmd.setCmd(CmdPwrMgt.PowerState.Sleep);
			else
				sendCmd.setCmd(CmdPwrMgt.PowerState.Standby);
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {}
        }
	}

	private void setInventoryTimes() {
		EditTextPreference etPref = (EditTextPreference) prefScr.findPreference("cfg_inventory_times");
		if(Integer.parseInt(etPref.getSummary().toString()) > 50) {
			etPref.setText("50");
			etPref.setSummary("50");
		} 
	}
}
