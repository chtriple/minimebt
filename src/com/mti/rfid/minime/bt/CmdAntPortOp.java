package com.mti.rfid.minime.bt;

public class CmdAntPortOp {

	public enum Mask {
		NoSpecificValue((byte)0x00),
		RssiScan((byte)0x01),
		ReflectedPowerScan((byte)0x02),
		AddFrequency((byte)0x04),
		ClearChannelList((byte)0x08);
		
		private byte bMask;
		
		Mask(byte bMask) {
			this.bMask = bMask;
		}
	}
	
	public enum Modulation {
		Disable((byte)0x00),
		Enable((byte)0x01);
		
		private byte bModulation;
		
		Modulation(byte bModulation) {
			this.bModulation = bModulation;
		}
	}
	
	public enum Operation {
		Disable((byte)0x00),
		Enable((byte)0x01);
		
		private byte bOperation;
		
		Operation(byte bOperation) {
			this.bOperation = bOperation;
		}
	}
	
	
	public enum State {
		PowerOff((byte)0x00),
		PowerOn((byte)0xFF);
		
		private byte bState;
		
		State(byte bState) {
			this.bState = bState;
		}
	}
	
	/************************************************************
	 **				RFID_AntennaPortSetPowerLevel				*
	 ************************************************************/
	static final class RFID_AntennaPortSetPowerLevel extends CmdMti {
		public RFID_AntennaPortSetPowerLevel(){
			mCmdHead = CmdHead.RFID_AntennaPortSetPowerLevel;
		};

		public static RFID_AntennaPortSetPowerLevel newInstance() {
			return new RFID_AntennaPortSetPowerLevel(); 
		}

		public boolean setCmd(byte powerLevel) {
			mParam.add(powerLevel);
			composeCmd();
			delay(200);

			return checkStatus();
		}
	}
	
	
	/************************************************************
	 **				RFID_AntennaPortGetPowerLevel				*
	 ************************************************************/
	static final class RFID_AntennaPortGetPowerLevel extends CmdMti {
		public RFID_AntennaPortGetPowerLevel() {
			mCmdHead = CmdHead.RFID_AntennaPortGetPowerLevel;
		}

		public static RFID_AntennaPortGetPowerLevel newInstance() {
			return new RFID_AntennaPortGetPowerLevel(); 
		}

		public boolean setCmd() {
			composeCmd();
			delay(200);
			
			return checkStatus();
		}
		
		public int getPowerLevel() {
			return mResponse[headerSize+3];
		}
	}
	
	
	/************************************************************
	 *				RFID_AntennaPortSetFrequency				*
	 ************************************************************/
	static final class RFID_AntennaPortSetFrequency extends CmdMti {
		public RFID_AntennaPortSetFrequency() {
			mCmdHead = CmdHead.RFID_AntennaPortSetFrequency;
		}

		public static RFID_AntennaPortSetFrequency newInstance() {
			return new RFID_AntennaPortSetFrequency(); 
		}

		public boolean setCmd(Mask mask,int freq, byte rssi, byte[] padding) {
			mParam.add(mask.bMask);
			processFreq(freq);
			mParam.add(rssi);
			processPadding(padding);

			composeCmd();
			return true;
		}

		private void processFreq(int freq) {
			for(int i = 0; i < 3; i++)
				mParam.add((byte)(freq >> i * 8));
		}
		
		private void processPadding(byte[] padding) {
			for(int i = 0; i < padding.length; i++)
				mParam.add(padding[i]);
		}
	}
	
	
	/************************************************************
	 *				RFID_AntennaPortSetOperation				*
	 ************************************************************/
	static final class RFID_AntennaPortSetOperation extends CmdMti {
		public RFID_AntennaPortSetOperation() {
			mCmdHead = CmdHead.RFID_AntennaPortSetOperation;
		}

		public static RFID_AntennaPortSetOperation newInstance() {
			return new RFID_AntennaPortSetOperation(); 
		}

		public boolean setCmd(Modulation modulation, Operation operation) {
			mParam.add(modulation.bModulation);
			mParam.add(operation.bOperation);

			composeCmd();
			return true;
		}
	}
	
	
	/************************************************************
	 *				RFID_AntennaPortCtrlPowerState				*
	 ************************************************************/
	static final class RFID_AntennaPortCtrlPowerState extends CmdMti {
		public RFID_AntennaPortCtrlPowerState() {
			mCmdHead = CmdHead.RFID_AntennaPortCtrlPowerState;
		}

		public static RFID_AntennaPortCtrlPowerState newInstance() {
			return new RFID_AntennaPortCtrlPowerState(); 
		}

		public boolean setCmd(State state) {
			mParam.add(state.bState);

			composeCmd();
			return true;
		}
	}
	
	
	/************************************************************
	 *				RFID_AntennaPortTransmitPattern				*
	 ************************************************************/
	static final class RFID_AntennaPortTransmitPattern extends CmdMti {
		public RFID_AntennaPortTransmitPattern() {
			mCmdHead = CmdHead.RFID_AntennaPortTransmitPattern;
		}

		public static RFID_AntennaPortTransmitPattern newInstance() {
			return new RFID_AntennaPortTransmitPattern(); 
		}

		public boolean setCmd(int dwellTime) {
			processDwellTime(dwellTime);

			composeCmd();
			return true;
		}

		private void processDwellTime(int dwellTime) {
			for(int i = 1; i >= 0; i--)
				mParam.add((byte)(dwellTime >> i * 8));
		}
	}
	
	
	/************************************************************
	 *				RFID_AntennaPortTransmitPulse				*
	 ************************************************************/
	static final class RFID_AntennaPortTransmitPulse extends CmdMti {
		public RFID_AntennaPortTransmitPulse() {
			mCmdHead = CmdHead.RFID_AntennaPortTransmitPulse;
		}

		public static RFID_AntennaPortTransmitPulse newInstance() {
			return new RFID_AntennaPortTransmitPulse(); 
		}

		public boolean setCmd(int dwellTime) {
			processDwellTime(dwellTime);

			composeCmd();
			return true;
		}

		private void processDwellTime(int dwellTime) {
			for(int i = 1; i >= 0; i--)
				mParam.add((byte)(dwellTime >> i * 8));
		}
	}
}
