package com.mti.rfid.minime.bt;

import org.apache.http.util.EncodingUtils;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

public class FragAbout extends PreferenceFragment{
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		String strSN = "n/a";
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.frag_about);

		if(MainActivity.checkConnectionStatus())
			strSN = getReaderSn();
	    Preference prefSn = (Preference) getPreferenceScreen().findPreference("about_reader_sn");
	    prefSn.setSummary(strSN);

	    Preference prefWeb = (Preference) getPreferenceScreen().findPreference("about_web");
	    prefWeb.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if(preference.getKey().equals("about_web")) {
					Uri uri = Uri.parse(getString(R.string.about_web_sum));
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(intent);
				}
				return true;
			}
        });
	}
	
	private String getReaderSn() {
		byte[] bSN = new byte[16];

	    for (int i = -1; i < 16; i++) {
	    	CmdFwAccess.RFID_MacReadOemData sendCmd = CmdFwAccess.RFID_MacReadOemData.newInstance();
			
			if(sendCmd.setCmd(i + 0x50))
				if(i >= 0)
					bSN[i] = sendCmd.getData();
		}
	    return EncodingUtils.getAsciiString(bSN);
	}

}
