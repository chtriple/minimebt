package com.mti.rfid.minime.bt;

import java.util.ArrayList;
import java.util.Arrays;

import android.util.Log;

public abstract class CmdMti {
/*	
	// RFID Module Configuration
	private static final byte[] RFID_RadioSetRegion					= {(byte)0xA8, 0x03};
	private static final byte[] RFID_RadioGetRegion					= {(byte)0xAA, 0x02};
	// Antenna Port Operation
	private static final byte[] RFID_AntennaPortSetPowerLevel		= {(byte)0xC0, 0x03};
	private static final byte[] RFID_AntennaPortGetPowerLevel		= {(byte)0xC2, 0x02};
	private static final byte[] RFID_AntennaPortSetFrequency		= {		 0x41, 0x09};
	private static final byte[] RFID_AntennaPortSetOperation		= {(byte)0xE4, 0x04};
	private static final byte[] RFID_AntennaPortCtrlPowerState		= {		 0x18, 0x03};
	private static final byte[] RFID_AntennaPortTransmitPattern		= {(byte)0xE6, 0x04};
	private static final byte[] RFID_AntennaPortTransmitPulse		= {(byte)0xEA, 0x04};
	// ISO 18000-6C Tag Access
	private static final byte[] RFID_18K6CSetQueryParameter			= {		 0x59, 0x0E};
	private static final byte[] RFID_18K6CTagInventory				= {		 0x31, 0x03};
	private static final byte[] RFID_18K6CTagInventoryRSSI			= {		 0x43, 0x03};
	private static final byte[] RFID_18K6CTagSelect					= {		 0x33, 0x03};		// 0x03~0x40
	private static final byte[] RFID_18K6CTagRead					= {		 0x37, 0x09};
	private static final byte[] RFID_18K6CTagWrite					= {		 0x35, 0x0B};		// 0x0B~0x3F
	private static final byte[] RFID_18K6CTagKill					= {		 0x3D, 0x06};
	private static final byte[] RFID_18K6CTagLock					= {		 0x3B, 0x08};
	private static final byte[] RFID_18K6CTagBlockWrite				= {		 0x70, 0x0B};		// 0x0B~0x3F
	private static final byte[] RFID_18K6CTagNXPCommand				= {		 0x45, 0x0A};
	private static final byte[] RFID_18K6CTagNXPTriggerEASAlarm		= {		 0x72, 0x02};
	// ISO 18000-6B Tag Access
	private static final byte[] RFID_18K6BTagInventory				= {		 0x3F, 0x0D};
	private static final byte[] RFID_18K6BTagRead					= {		 0x49, 0x0C};
	private static final byte[] RFID_18K6BTagWrite					= {		 0x47, 0x0C};		// 0x0C~0x40
	// RFID Module Firmware Access
	private static final byte[] RFID_MacGetModuleID					= {		 0x10, 0x03};
	private static final byte[] RFID_MacGetDebugValue				= {(byte)0xA2, 0x02};
	private static final byte[] RFID_MacBypassWriteRegister			= {		 0x1A, 0x06};
	private static final byte[] RFID_MacBypassReadRegister			= {		 0x1C, 0x03};
	private static final byte[] RFID_MacWriteOemData				= {(byte)0xA4, 0x05};
	private static final byte[] RFID_MacReadOemData					= {(byte)0xA6, 0x04};
	private static final byte[] RFID_MacSoftReset					= {(byte)0xA0, 0x02};
	private static final byte[] RFID_MacEnterUpdateMode				= {(byte)0xD0, 0x02};
	// RFID Module Manufacturer Engineering
	private static final byte[] RFID_EngSetExternalPA				= {(byte)0xE0, 0x03};
	private static final byte[] RFID_EngGetAmbientTemp				= {(byte)0xE2, 0x02};
	private static final byte[] RFID_EngGetForwardRFPower			= {(byte)0xEC, 0x02};
	private static final byte[] RFID_EngTransmitSerialPattern		= {(byte)0xE8, 0x07};
	private static final byte[] RFID_EngWriteFullOemData			= {(byte)0xEE, 0x05};
*/
	private static final boolean DEBUG = true;
	private static final String TAG = "MINIMEBT";

	private static final int CMD_LENGTH = 64;
	private static final int RESPONSE_LENGTH = 64;
	private static final byte[] cmdHeader = {(byte)0x4d, (byte)0x54, (byte)0x49, (byte)0x43, (byte)0x00};
	protected static final int headerSize = cmdHeader.length;
	
	private BtCommunication mBtComm = MainActivity.getBtComm();

	private static int mTagMode = 0;		// 0:Gen2, 1:Gen2+RSSI, 2:ISO6B
	private byte mStatus;
	private String mStrStatus;
	protected CmdHead mCmdHead;
	protected ArrayList<Byte> mParam = new ArrayList<Byte>();
	protected byte[] mSendCmd = new byte[CMD_LENGTH];
	protected byte[] mResponse = new byte[RESPONSE_LENGTH];
	
	public CmdMti() {
		mParam.clear();
	}
	
	protected boolean composeCmd() {
		int cmdLength = 0;
		byte[] command;
		boolean bState;
		
		mSendCmd = Arrays.copyOfRange(cmdHeader, 0, 64);
		
		mSendCmd[headerSize] = mCmdHead.get1stCmd();
		mSendCmd[headerSize+1] = (byte)(mParam.size() + 2);
		
		for(int i = 0; i < mParam.size(); i++) {
			mSendCmd[headerSize+2+i] = mParam.get(i).byteValue();
		}
		
		cmdLength = headerSize + 2 + mParam.size();
		int Crc = ~Crc16.calculate(mSendCmd, cmdLength);
		mSendCmd[cmdLength] = (byte)((Crc & 0x0000ff00) >>> 8);
		mSendCmd[cmdLength+1] = (byte)((Crc & 0x000000ff));
		
		command = Arrays.copyOfRange(mSendCmd, 0, cmdLength + 2);
		bState = mBtComm.sendCmd(command);
		
		// #### log whole command for debug ####
		if(DEBUG) Log.d(TAG, "TX: " + strCmd(command));
		
		return bState;
	}

	
	public boolean checkStatus(int timeout) {
		boolean result = false;
		int i = 0;
		Response response; 
		do {
			response = mBtComm.getResponse(timeout);
		} while(response.equals(null) && i++ < 2);
		if(response.getStatus()) {
			mResponse = response.getData();
			if(DEBUG) Log.d(TAG, "RX: " + strCmd(mResponse));
		} else
			return false;
		
		mStatus = mResponse[headerSize+2];
		getStatus();
		
		if(mResponse[headerSize] == mSendCmd[headerSize] + 1) {
			if(mStatus == 0x00)
				result = true;
			else
				result = false;
		} else {
			result = false;
		}
		return result;
	}

/*	
	private byte[] getDataFromStream() {
		mResponse = mBtComm.getResponse();
		
		// #### log whole command for debug ####
		if(DEBUG) Log.d(TAG, "RX: " + strCmd(mResponse));
		return mResponse;
	}
*/
	
	public byte[] getResponse() {
		return mResponse;
	}
	
	public String responseData(int length) {
		String hexResult = "";

		for (int i = 0; i < length * 2; i++) {
			hexResult += ((mResponse[headerSize+i+4] < 0 || mResponse[headerSize+i+4] > 15)
						? Integer.toHexString(0xff & (int)mResponse[headerSize+i+4])
						: "0" + Integer.toHexString(0xff & (int)mResponse[headerSize+i+4]))
						+ (( i % 2 == 1) ? " " : "");
		}
		return hexResult.toUpperCase();
	}
	
	
	public String strCmd(byte[] BtoS) {
		String hexResult = "";

		for (int i = 0; i < BtoS.length; i++) {
			hexResult += ((BtoS[i] < 0 || BtoS[i] > 15)
						? Integer.toHexString(0xff & (int)BtoS[i])
						: "0" + Integer.toHexString(0xff & (int)BtoS[i]))
						+ ((i == BtoS.length) ? "" : " ");
		}
		return hexResult.toUpperCase();
	}
	

    public byte[] byteCmd(String StoB) {
    	String subStr;
    	int iLength = StoB.length() / 2;
    	byte[] bytes = new byte[iLength];
    	
        for (int i = 0; i < iLength; i++) {
        	subStr = StoB.substring(2 * i, 2 * i + 2);
        	bytes[i] = (byte)Integer.parseInt(subStr, 16);
        }
        return bytes;
    }

	public static void setTagMode(int tagmode) {
		mTagMode = tagmode;
	}
	
	public static int getTagMode() {
		return mTagMode;
	}

	public String getStatus() {
		switch(mStatus) {
			case (byte)0x00:
				mStrStatus = "RFID_STATUS_OK";
				break;
			
			case (byte)0x0E:
				mStrStatus = "RFID_ERROR_CMD_INVALID_DATA_LENGTH";
				break;
			case (byte)0x0F:
				mStrStatus = "RFID_ERROR_CMD_INVALID_PARAMETER";
				break;

			case (byte)0x0A:
				mStrStatus = "RFID_ERROR_SYS_CHANNEL_TIMEOUT";
				break;
			case (byte)0xFE:
				mStrStatus = "RFID_ERROR_SYS_SECURITY_FAILURE";
				break;
			case (byte)0xFF:
				mStrStatus = "RFID_ERROR_SYS_MODULE_FAILURE";
				break;
			
			case (byte)0xA0:
				mStrStatus = "RFID_ERROR_HWOPT_READONLY_ADDRESS";
				break;
			case (byte)0xA1:
				mStrStatus = "RFID_ERROR_HWOPT_UNSUPPORTED_REGION";
				break;

			case (byte)0x01:
				mStrStatus = "RFID_ERROR_18K6C_REQRN";
				break;
			case (byte)0x02:
				mStrStatus = "RFID_ERROR_18K6C_ACCESS";
				break;
			case (byte)0x03:
				mStrStatus = "RFID_ERROR_18K6C_KILL";
				break;
			case (byte)0x04:
				mStrStatus = "RFID_ERROR_18K6C_NOREPLY";
				break;
			case (byte)0x05:
				mStrStatus = "RFID_ERROR_18K6C_LOCK";
				break;
			case (byte)0x06:
				mStrStatus = "RFID_ERROR_18K6C_BLOCKWRITE";
				break;
			case (byte)0x07:
				mStrStatus = "RFID_ERROR_18K6C_BLOCKERASE";
				break;
			case (byte)0x08:
				mStrStatus = "RFID_ERROR_18K6C_READ";
				break;
			case (byte)0x09:
				mStrStatus = "RFID_ERROR_18K6C_SELECT";
				break;
			case (byte)0x20:
				mStrStatus = "RFID_ERROR_18K6C_EASCODE";
				break;

			case (byte)0x11:
				mStrStatus = "RFID_ERROR_18K6B_INVALID_CRC";
				break;
			case (byte)0x12:
				mStrStatus = "RFID_ERROR_18K6B_RFICREG_FIFO";
				break;
			case (byte)0x13:
				mStrStatus = "RFID_ERROR_18K6B_NO_RESPONSE";
				break;
			case (byte)0x14:
				mStrStatus = "RFID_ERROR_18K6B_NO_ACKNOWLEDGE";
				break;
			case (byte)0x15:
				mStrStatus = "RFID_ERROR_18K6B_PREAMBLE";
				break;
				
			case (byte)0x80:
				mStrStatus = "RFID_ERROR_6CTAG_OTHER_ERROR";
				break;
			case (byte)0x83:
				mStrStatus = "RFID_ERROR_6CTAG_MEMORY_OVERRUN";
				break;
			case (byte)0x84:
				mStrStatus = "RFID_ERROR_6CTAG_MEMORY_LOCKED";
				break;
			case (byte)0x8B:
				mStrStatus = "RFID_ERROR_6CTAG_INSUFFICIENT_POWER";
				break;
			case (byte)0x8F:
				mStrStatus = "RFID_ERROR_6CTAG_NONSPECIFIC_ERROR";
				break;
		}
		return mStrStatus;
	}
}
