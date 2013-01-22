package com.mti.rfid.minime.bt;

import java.util.ArrayList;

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
	
	private BtCommunication mBtComm = MainActivity.getBtComm();

	
	private byte mStatus;
	private String mStrStatus;
	protected CmdHead mCmdHead;
	protected ArrayList<Byte> mParam = new ArrayList<Byte>();
	protected byte[] mSendCmd = new byte[CMD_LENGTH];
	protected byte[] mResponse = new byte[RESPONSE_LENGTH];
	
	public CmdMti() {
		mParam.clear();
	}

	
	protected void composeCmd() {
		int cmdLength = 0;
		byte[] byteStr = new byte[4];
		
		mSendCmd[0] = 0x4d;
		mSendCmd[1] = 0x54;
		mSendCmd[2] = 0x49;
		mSendCmd[3] = 0x43;
		mSendCmd[4] = 0x00;
		
		mSendCmd[5] = mCmdHead.get1stCmd();
		mSendCmd[6] = (byte)(mParam.size() + 2);
		
		for(int i = 0; i < mParam.size(); i++) {
			mSendCmd[i+7] = mParam.get(i).byteValue();
			cmdLength = i + 8;
		}

		int Crc = calculate_crc(mSendCmd, cmdLength);
		mSendCmd[cmdLength] = (byte)((Crc & 0x0000ff00) >>> 8);
		mSendCmd[cmdLength+1] = (byte)((Crc & 0x000000ff));
		
		mBtComm.sendCmd(mSendCmd);
		
		// #### log whole command for debug ####
		if(DEBUG) Log.d(TAG, "TX: " + strCmd(mSendCmd));
	}

	
	public boolean checkStatus() {
		boolean result = false;
		
		getDataFromStream();
		mStatus = mResponse[7];
		getStatus();
		
		if(mResponse[0] == mSendCmd[0] + 1) {
			if(mStatus == 0x00)
				result = true;
			else
				result = false;
		} else {
			result = false;
		}
		return result;
	}

	
	private byte[] getDataFromStream() {
//		mResponse = mBtComm.getResponse();
		
		// #### log whole command for debug ####
		if(DEBUG) Log.d(TAG, "RX: " + strCmd(mResponse));
		return mResponse;
	}

	
	public byte[] getResponse() {
		return mResponse;
	}
	
	public String responseData(int length) {
		String hexResult = "";

		for (int i = 0; i < length * 2; i++) {
			hexResult += ((mResponse[i + 4] < 0 || mResponse[i + 4] > 15)
						? Integer.toHexString(0xff & (int)mResponse[i + 4])
						: "0" + Integer.toHexString(0xff & (int)mResponse[i + 4]))
						+ (( i % 2 == 1) ? " " : "");
		}
		return hexResult.toUpperCase();
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
    
	private int calculate_crc(byte[] bytes, int cmdLength) {
       int[] table = {
           0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301, 0x03C0, 0x0280, 0xC241,
           0xC601, 0x06C0, 0x0780, 0xC741, 0x0500, 0xC5C1, 0xC481, 0x0440,
           0xCC01, 0x0CC0, 0x0D80, 0xCD41, 0x0F00, 0xCFC1, 0xCE81, 0x0E40,
           0x0A00, 0xCAC1, 0xCB81, 0x0B40, 0xC901, 0x09C0, 0x0880, 0xC841,
           0xD801, 0x18C0, 0x1980, 0xD941, 0x1B00, 0xDBC1, 0xDA81, 0x1A40,
           0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01, 0x1DC0, 0x1C80, 0xDC41,
           0x1400, 0xD4C1, 0xD581, 0x1540, 0xD701, 0x17C0, 0x1680, 0xD641,
           0xD201, 0x12C0, 0x1380, 0xD341, 0x1100, 0xD1C1, 0xD081, 0x1040,
           0xF001, 0x30C0, 0x3180, 0xF141, 0x3300, 0xF3C1, 0xF281, 0x3240,
           0x3600, 0xF6C1, 0xF781, 0x3740, 0xF501, 0x35C0, 0x3480, 0xF441,
           0x3C00, 0xFCC1, 0xFD81, 0x3D40, 0xFF01, 0x3FC0, 0x3E80, 0xFE41,
           0xFA01, 0x3AC0, 0x3B80, 0xFB41, 0x3900, 0xF9C1, 0xF881, 0x3840,
           0x2800, 0xE8C1, 0xE981, 0x2940, 0xEB01, 0x2BC0, 0x2A80, 0xEA41,
           0xEE01, 0x2EC0, 0x2F80, 0xEF41, 0x2D00, 0xEDC1, 0xEC81, 0x2C40,
           0xE401, 0x24C0, 0x2580, 0xE541, 0x2700, 0xE7C1, 0xE681, 0x2640,
           0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101, 0x21C0, 0x2080, 0xE041,
           0xA001, 0x60C0, 0x6180, 0xA141, 0x6300, 0xA3C1, 0xA281, 0x6240,
           0x6600, 0xA6C1, 0xA781, 0x6740, 0xA501, 0x65C0, 0x6480, 0xA441,
           0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01, 0x6FC0, 0x6E80, 0xAE41,
           0xAA01, 0x6AC0, 0x6B80, 0xAB41, 0x6900, 0xA9C1, 0xA881, 0x6840,
           0x7800, 0xB8C1, 0xB981, 0x7940, 0xBB01, 0x7BC0, 0x7A80, 0xBA41,
           0xBE01, 0x7EC0, 0x7F80, 0xBF41, 0x7D00, 0xBDC1, 0xBC81, 0x7C40,
           0xB401, 0x74C0, 0x7580, 0xB541, 0x7700, 0xB7C1, 0xB681, 0x7640,
           0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101, 0x71C0, 0x7080, 0xB041,
           0x5000, 0x90C1, 0x9181, 0x5140, 0x9301, 0x53C0, 0x5280, 0x9241,
           0x9601, 0x56C0, 0x5780, 0x9741, 0x5500, 0x95C1, 0x9481, 0x5440,
           0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00, 0x9FC1, 0x9E81, 0x5E40,
           0x5A00, 0x9AC1, 0x9B81, 0x5B40, 0x9901, 0x59C0, 0x5880, 0x9841,
           0x8801, 0x48C0, 0x4980, 0x8941, 0x4B00, 0x8BC1, 0x8A81, 0x4A40,
           0x4E00, 0x8EC1, 0x8F81, 0x4F40, 0x8D01, 0x4DC0, 0x4C80, 0x8C41,
           0x4400, 0x84C1, 0x8581, 0x4540, 0x8701, 0x47C0, 0x4680, 0x8641,
           0x8201, 0x42C0, 0x4380, 0x8341, 0x4100, 0x81C1, 0x8081, 0x4040,
       };

           int crc = 0x0000;
           for (int i = 0; i < cmdLength; i++) {
               crc = (crc >>> 8) ^ table[(crc ^ bytes[i]) & 0xff];
           }
           return crc;
	}
    
	// #### delay ####
	protected void delay(int milliSecond) {
		try{
			Thread.sleep(milliSecond);
		} catch (InterruptedException e) {}
	}

}
