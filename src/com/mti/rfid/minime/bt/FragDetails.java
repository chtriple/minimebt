package com.mti.rfid.minime.bt;

import com.mti.rfid.minime.bt.CmdPwrMgt.PowerState;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

public class FragDetails extends Fragment implements OnFocusChangeListener {
	private static final boolean DEBUG = false;
	private static final String TAG = "MINIMEBT";

	private static final String TAG_ID = "mTagId";
	private String mTagId;
	
	private View mView;
	private EditText mEpcLength;
	private EditText mEpcNew;
	private Spinner mPwdType;
	private EditText mPwdNew;
	private EditText mTidData;
	private EditText mUserData;
	private Spinner mLockMemSpace;
	private Spinner mLockAction;
	private Spinner mNxpCmd;
	
	private SharedPreferences mSharedpref;

	private String strPwd = "";
	
	public FragDetails() {
	}
	
	public FragDetails(String tagId) {
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
		int iEpcLength;
		TextView tv;
		
		mView = inflater.inflate(R.layout.frag_detail, container, false);
		
		createSpinnerItems(mView, R.id.sp_pwdtype, R.array.pwd_type);
        createSpinnerItems(mView, R.id.sp_lockmemory, R.array.lock_mem);
        createSpinnerItems(mView, R.id.sp_lockaction, R.array.lock_act);
        createSpinnerItems(mView, R.id.sp_nxpcmd, R.array.nxp_cmd);
		
		tv = (TextView)mView.findViewById(R.id.tv_tagid);
		tv.setText(mTagId);
		
		iEpcLength = (mTagId.length() + 1) / 6;
		mEpcLength = (EditText)mView.findViewById(R.id.et_epclength);
		mEpcLength.setText(String.valueOf(iEpcLength));
		
		mEpcNew = (EditText)mView.findViewById(R.id.et_epcnew);
		mEpcNew.setText(mTagId.replace(" ", ""));
		
		mPwdType = (Spinner)mView.findViewById(R.id.sp_pwdtype);
		mPwdNew = (EditText)mView.findViewById(R.id.et_pwdnew);
		
		mTidData = (EditText)mView.findViewById(R.id.et_tiddata);
		mUserData = (EditText)mView.findViewById(R.id.et_userdata);
		
		mLockMemSpace = (Spinner)mView.findViewById(R.id.sp_lockmemory);
		mLockAction = (Spinner)mView.findViewById(R.id.sp_lockaction);
		
		mNxpCmd = (Spinner)mView.findViewById(R.id.sp_nxpcmd);
//		mNxpConf = (EditText)mView.findViewById(R.id.et_nxpconf);

		return mView;
	}

	    
	private void createSpinnerItems(View mView, int viewId, int itemArray) {
	    Spinner sp = (Spinner) mView.findViewById(viewId);
	    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	    		mView.getContext() , itemArray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
        sp.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View mView, int position, long id) {
                    }
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
    }


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mEpcLength.setOnFocusChangeListener(this);
		mEpcNew.setOnFocusChangeListener(this);

		mSharedpref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

		final Button btnEpc = (Button) mView.findViewById(R.id.btn_epc);
		btnEpc.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
 		    	int iEpcLength;

		    	iEpcLength = Integer.valueOf(mEpcLength.getText().toString());
		    	
		    	if( iEpcLength < 1 || iEpcLength > 27) {
		    		openOptionsDialog(getString(R.string.dlg_epc_length_title),
		    			getString(R.string.dlg_epc_length_message),
		    			getResources().getString(android.R.string.ok));
		    	}
		    	openPwdDialog(R.id.btn_epc);
			}
		});

		final Button btnPwd = (Button) mView.findViewById(R.id.btn_pwd);
		btnPwd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		    	openPwdDialog(R.id.btn_pwd);
			}
		});
		
		final Button btnLock = (Button) mView.findViewById(R.id.btn_lock);
		btnLock.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		    	openPwdDialog(R.id.btn_lock);
			}
		});
		
		final Button btnTidRead = (Button) mView.findViewById(R.id.btn_tid_read);
		btnTidRead.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		    	openPwdDialog(R.id.btn_tid_read);
			}
		});
		
		final Button btnTidWrite = (Button) mView.findViewById(R.id.btn_tid_write);
		btnTidWrite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		    	openPwdDialog(R.id.btn_tid_write);
			}
		});
		
		final Button btnUserRead = (Button) mView.findViewById(R.id.btn_user_read);
		btnUserRead.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		    	openPwdDialog(R.id.btn_user_read);
			}
		});
		
		final Button btnUserWrite = (Button) mView.findViewById(R.id.btn_user_write);
		btnUserWrite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		    	openPwdDialog(R.id.btn_user_write);
			}
		});
		
		final Button btnKill = (Button) mView.findViewById(R.id.btn_kill);
		btnKill.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		    	openPwdDialog(R.id.btn_kill);
			}
		});
		
		final Button btnNxp = (Button) mView.findViewById(R.id.btn_nxp);
		btnNxp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		    	openPwdDialog(R.id.btn_nxp);
			}
		});
		
		final Button btnNxpTrigger = (Button) mView.findViewById(R.id.btn_nxp_trigger);
		btnNxpTrigger.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//		    	openPwdDialog(R.id.btn_nxp_trigger);
				nxpTrigger();
			}
		});
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString(TAG_ID, mTagId);

		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onFocusChange(View view, boolean hasFocus) {
		int iEpcLength;
		int resId = view.getId();
		
		if(!hasFocus) {
			switch(view.getId()){
				case R.id.et_epclength:
					if(mEpcLength.getText().length() == 0)
						// #### process error ####
						return;
					else {
						iEpcLength = Integer.valueOf(mEpcLength.getText().toString());
						if(resId == R.id.et_epclength) {
							
					    	if( iEpcLength < 1 || iEpcLength > 27) {
					    		openOptionsDialog(getString(R.string.dlg_epc_length_title),
					    			getString(R.string.dlg_epc_length_message),
					    			getResources().getString(android.R.string.ok));
					    	}
						}
					}
					break;
				
				case R.id.et_epcnew:
					if(mEpcLength.getText().length() == 0)
						// #### process error ####
						return;
					else {
						iEpcLength = Integer.valueOf(mEpcLength.getText().toString());
						if(hasFocus) {
					    	InputFilter[] arrFilter = new InputFilter[1];
					    	arrFilter[0] = new InputFilter.LengthFilter(iEpcLength * 4);
					    	mEpcNew.setFilters(arrFilter);
						} else {
							if(iEpcLength != (mEpcNew.length() / 4)) {
					    		openOptionsDialog(getString(R.string.dlg_epc_new_title),
						    			getString(R.string.dlg_epc_new_message),
						    			getResources().getString(android.R.string.ok));
							}
						}
					}
					break;
			}
		}
	}

    private void openPwdDialog(final int btnres) {
        LayoutInflater factory = LayoutInflater.from(mView.getContext());
        final View textEntryView = factory.inflate(R.layout.password_dialog, null);
        TextView tv_pwd = (TextView)textEntryView.findViewById(R.id.et_pwd);
        tv_pwd.setText(strPwd);
        AlertDialog.Builder dialog = new AlertDialog.Builder(mView.getContext());
        dialog.setIconAttribute(android.R.attr.alertDialogIcon)
            .setTitle(R.string.pwd_title)
            .setView(textEntryView)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	EditText et = (EditText)textEntryView.findViewById(R.id.et_pwd);
                	strPwd = et.getText().toString();
                	if(strPwd.equals(""))
                		strPwd = "00000000";
                 	Long longPwd = Long.parseLong(strPwd, 16);
                	switch(btnres) {
	                	case R.id.btn_epc:
	                		setNewEpc(longPwd);
	                		break;
	                	case R.id.btn_pwd:
	                		setNewPwd(longPwd);
	                		break;
	                	case R.id.btn_tid_read:
	                		tidRead(longPwd);
	                		break;
	                	case R.id.btn_tid_write:
	                		tidWrite(longPwd);
	                		break;
	                	case R.id.btn_user_read:
	                		userRead(longPwd);
	                		break;
	                	case R.id.btn_user_write:
	                		userWrite(longPwd);
	                		break;
	                	case R.id.btn_lock:
	                		lockTag(longPwd);
	                		break;
	                	case R.id.btn_kill:
	                		killTag(longPwd);
	                		break;
	                	case R.id.btn_nxp:
	                		setNxpCmd(longPwd);
	                		break;
	                	case R.id.btn_nxp_trigger:
	                		nxpTrigger();
	                		break;
                	}
            		setPowerState();
                }
            })
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {}
            })
            .show();
    }
    
    
	private void openOptionsDialog(String title, String message, String posBtn) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(mView.getContext());
        dialog.setIconAttribute(android.R.attr.alertDialogIcon)
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton(posBtn,
			new DialogInterface.OnClickListener(){
				public void onClick(
							DialogInterface dialoginterface, int i) {}
			})
			.show();
	}


	private byte setNewEpc(Long pwd) {
		int iEpcLength = Integer.valueOf(mEpcLength.getText().toString());
		int byte1 = iEpcLength / 2;
		int byte2 = (iEpcLength % 2) * 8;
		String strLength = "" + byte1 + byte2 + "00";

    	CmdIso18k6cTagAccess.RFID_18K6CTagWrite sendCmd = CmdIso18k6cTagAccess.RFID_18K6CTagWrite.newInstance();
		if(sendCmd.setCmd(CmdIso18k6cTagAccess.MemoryBank.EPC, (byte)0x01, pwd, sendCmd.byteCmd(strLength + mEpcNew.getText().toString()))) {
			Toast.makeText(getActivity(), "Set New EPC", Toast.LENGTH_SHORT).show();
		} else {
    		openOptionsDialog(getString(R.string.dlg_epc_new_title),
    				sendCmd.getStatus(),
		    		getResources().getString(android.R.string.ok));
		}
		
		return 0;
	}

	
	private byte setNewPwd(Long pwd) {
		byte bMemAddr = 0x00;

    	CmdIso18k6cTagAccess.RFID_18K6CTagWrite sendCmd = CmdIso18k6cTagAccess.RFID_18K6CTagWrite.newInstance();
		switch(mPwdType.getSelectedItemPosition()) {
			case 0:
				bMemAddr = 0x00;
				break;
			case 1:
				bMemAddr = 0x02;
				break;
		}

		if(sendCmd.setCmd(CmdIso18k6cTagAccess.MemoryBank.Reserved, bMemAddr, pwd, sendCmd.byteCmd(mPwdNew.getText().toString()))) {
			Toast.makeText(getActivity(), "Set New Password", Toast.LENGTH_SHORT).show();
		} else {
    		openOptionsDialog(getString(R.string.dlg_pwd_title),
    				sendCmd.getStatus(),
	    		getResources().getString(android.R.string.ok));
		}
		return 0;
	}
	
	
	private byte lockTag(Long pwd) {
    	CmdIso18k6cTagAccess.RFID_18K6CTagLock sendCmd = CmdIso18k6cTagAccess.RFID_18K6CTagLock.newInstance();

		CmdIso18k6cTagAccess.MemorySpace lockMemSpace = null;
		CmdIso18k6cTagAccess.LockAction lockAction = null;
		
		switch(mLockMemSpace.getSelectedItemPosition()) {
			case 0:
				lockMemSpace = CmdIso18k6cTagAccess.MemorySpace.ReservedKillPassword;
				break;
			case 1:
				lockMemSpace = CmdIso18k6cTagAccess.MemorySpace.ReservedAccessPassword;
				break;
			case 2:
				lockMemSpace = CmdIso18k6cTagAccess.MemorySpace.EPC;
				break;
			case 3:
				lockMemSpace = CmdIso18k6cTagAccess.MemorySpace.TID;
				break;
			case 4:
				lockMemSpace = CmdIso18k6cTagAccess.MemorySpace.User;
				break;
		}
		
		switch(mLockAction.getSelectedItemPosition()) {
			case 0:
				lockAction = CmdIso18k6cTagAccess.LockAction.Accessible;
				break;
			case 1:
				lockAction = CmdIso18k6cTagAccess.LockAction.AlwaysAccessable;
				break;
			case 2:
				lockAction = CmdIso18k6cTagAccess.LockAction.PasswordAccessible;
				break;
			case 3:
				lockAction = CmdIso18k6cTagAccess.LockAction.AlwaysNotAccessible;
				break;
		}

		if(sendCmd.setCmd(lockAction, lockMemSpace, pwd)) {
			Toast.makeText(getActivity(), "Lock Tag", Toast.LENGTH_SHORT).show();
		} else {
    		openOptionsDialog(getString(R.string.dlg_lock_title),
	    		sendCmd.getStatus(),
	    		getResources().getString(android.R.string.ok));
		}
		return 0;
	}

	
	private byte tidRead(Long pwd) {
    	CmdIso18k6cTagAccess.RFID_18K6CTagRead sendCmd = CmdIso18k6cTagAccess.RFID_18K6CTagRead.newInstance();
		
		int dataLength = Integer.parseInt((mSharedpref.getString("cfg_tid_length", "1")));
		if(sendCmd.setCmd(CmdIso18k6cTagAccess.MemoryBank.TID, (byte)0x00, pwd, (byte)dataLength)) {
			Toast.makeText(getActivity(), "Read TID", Toast.LENGTH_SHORT).show();
			mTidData.setText(sendCmd.responseData(dataLength));
		} else {
    		openOptionsDialog(getString(R.string.dlg_read_tid_title),
	    		sendCmd.getStatus(),
	    		getResources().getString(android.R.string.ok));
		}
		
		return 0;
	}
	
	
	private byte tidWrite(Long pwd) {
    	CmdIso18k6cTagAccess.RFID_18K6CTagWrite sendCmd = CmdIso18k6cTagAccess.RFID_18K6CTagWrite.newInstance();

		String strData = mTidData.getText().toString().replace(" ", "");
		int intData = strData.length() % 4;
		for(int i = 4; intData != 0 && i > intData; i--)
			strData += "0";

		if(sendCmd.setCmd(CmdIso18k6cTagAccess.MemoryBank.TID, (byte)0x00, pwd, sendCmd.byteCmd(strData))) {
			Toast.makeText(getActivity(), "Write TID", Toast.LENGTH_SHORT).show();
		} else {
    		openOptionsDialog(getString(R.string.dlg_write_tid_title),
    				sendCmd.getStatus(),
	    		getResources().getString(android.R.string.ok));
		}
		return 0;
	}

	
	private byte userRead(Long pwd) {
    	CmdIso18k6cTagAccess.RFID_18K6CTagRead sendCmd = CmdIso18k6cTagAccess.RFID_18K6CTagRead.newInstance();
		
		int dataLength = Integer.parseInt((mSharedpref.getString("cfg_user_length", "1")));
		if(sendCmd.setCmd(CmdIso18k6cTagAccess.MemoryBank.User, (byte)0x00, pwd, (byte)dataLength)) {
			Toast.makeText(getActivity(), "Read User Memory", Toast.LENGTH_SHORT).show();
			mUserData.setText(sendCmd.responseData(dataLength));
		} else {
    		openOptionsDialog(getString(R.string.dlg_read_user_title),
    				sendCmd.getStatus(),
	    		getResources().getString(android.R.string.ok));
		}
		
		return 0;
	}
	
	
	private byte userWrite(Long pwd) {
    	CmdIso18k6cTagAccess.RFID_18K6CTagWrite sendCmd = CmdIso18k6cTagAccess.RFID_18K6CTagWrite.newInstance();

		String strData = mUserData.getText().toString().replace(" ", "");
		int intData = strData.length() % 4;
		for(int i = 4; intData != 0 && i > intData; i--)
			strData += "0";

		if(sendCmd.setCmd(CmdIso18k6cTagAccess.MemoryBank.User, (byte)0x00, pwd, sendCmd.byteCmd(strData))) {
			Toast.makeText(getActivity(), "Write User Memory", Toast.LENGTH_SHORT).show();
		} else {
    		openOptionsDialog(getString(R.string.dlg_write_user_title),
    				sendCmd.getStatus(),
	    		getResources().getString(android.R.string.ok));
		}
		return 0;
	}

	
	private byte killTag(Long pwd) {
    	CmdIso18k6cTagAccess.RFID_18K6CTagKill sendCmd = CmdIso18k6cTagAccess.RFID_18K6CTagKill.newInstance();

		if(sendCmd.setCmd(pwd)) {
			Toast.makeText(getActivity(), "Kill Tag", Toast.LENGTH_SHORT).show();
		} else {
    		openOptionsDialog(getString(R.string.dlg_pwd_title),
    				sendCmd.getStatus(),
	    		getResources().getString(android.R.string.ok));
		}
		return 0;
	}
	
	
	private byte setNxpCmd(Long pwd) {
    	CmdIso18k6cTagAccess.RFID_18K6CTagNXPCommand sendCmd = CmdIso18k6cTagAccess.RFID_18K6CTagNXPCommand.newInstance();
		
    	CmdIso18k6cTagAccess.NXPCommand nxpCommand = null;
    	CmdIso18k6cTagAccess.BitStatus bitStatus = CmdIso18k6cTagAccess.BitStatus.Set;
		short nxpConf = 0;

		switch(mNxpCmd.getSelectedItemPosition()) {
			case 0:
				nxpCommand = CmdIso18k6cTagAccess.NXPCommand.ReadProtectStauts;
				bitStatus = CmdIso18k6cTagAccess.BitStatus.Set;
				break;
			case 1:
				nxpCommand = CmdIso18k6cTagAccess.NXPCommand.ReadProtectStauts;
				bitStatus = CmdIso18k6cTagAccess.BitStatus.Reset;
				break;
			case 2:
				nxpCommand = CmdIso18k6cTagAccess.NXPCommand.EASStatus;
				bitStatus = CmdIso18k6cTagAccess.BitStatus.Set;
				break;
			case 3:
				nxpCommand = CmdIso18k6cTagAccess.NXPCommand.EASStatus;
				bitStatus = CmdIso18k6cTagAccess.BitStatus.Reset;
				break;
		}
		
		if(sendCmd.setCmd(nxpCommand, bitStatus, pwd, nxpConf)) {
			Toast.makeText(getActivity(), "Set NXP Command", Toast.LENGTH_SHORT).show();
		} else {
    		openOptionsDialog(getString(R.string.dlg_pwd_title),
    				sendCmd.getStatus(),
	    		getResources().getString(android.R.string.ok));
		}
		return 0;
	}
	
	
	private byte nxpTrigger() {
    	CmdIso18k6cTagAccess.RFID_18K6CTagNXPTriggerEASAlarm sendCmd = CmdIso18k6cTagAccess.RFID_18K6CTagNXPTriggerEASAlarm.newInstance();
		
		if(sendCmd.setCmd()) {
			Toast.makeText(getActivity(), "Tragger NXP EAS Alarm", Toast.LENGTH_SHORT).show();
		} else {
    		openOptionsDialog(getString(R.string.dlg_pwd_title),
    				sendCmd.getStatus(),
	    		getResources().getString(android.R.string.ok));
		}
		return 0;
	}

	
	private void setPowerState() {
    	CmdPwrMgt.RFID_PowerEnterPowerState sendCmd = CmdPwrMgt.RFID_PowerEnterPowerState.newInstance();
		
	    if(mSharedpref.getBoolean("cfg_sleep_mode", false))
	    	sendCmd.setCmd(PowerState.Sleep);
	}
}
