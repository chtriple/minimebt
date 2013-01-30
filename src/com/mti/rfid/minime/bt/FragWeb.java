package com.mti.rfid.minime.bt;

import org.apache.http.util.EncodingUtils;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class FragWeb extends Fragment {
	private static final boolean DEBUG = false;
	private static final String TAG = "MINIMEBT";

	private static final String TAG_ID = "mTagId";
	private String mTagId;
	private WebView wvContant;
	
	public FragWeb() {
	}
	
	public FragWeb(String tagId) {
		mTagId = tagId;
	}

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(savedInstanceState != null)
			mTagId = savedInstanceState.getString(TAG_ID);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SharedPreferences mSharedpref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
	    final String prefixUrl = mSharedpref.getString("cfg_web_url", "");
		
		View view = inflater.inflate(R.layout.frag_web, container, false);
		wvContant =(WebView) view.findViewById(R.id.wv_contant);
				
        WebSettings webSettings = wvContant.getSettings();
        webSettings.setJavaScriptEnabled(true);
        wvContant.setWebViewClient(new WebViewClient(){
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				wvContant.setWebViewClient(null);
			}
        });

//        if(prefixUrl.contains("?")) {						// #### GET  ####
        	wvContant.loadUrl(prefixUrl + mTagId);
//        } else {											// #### POST ####
//        	wvContant.postUrl(prefixUrl, EncodingUtils.getBytes("epc=" + mTagId, "BASE64"));
//        }
		return view;
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString(TAG_ID, mTagId);

		super.onSaveInstanceState(savedInstanceState);
	}

}
