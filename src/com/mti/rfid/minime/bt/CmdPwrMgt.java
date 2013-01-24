package com.mti.rfid.minime.bt;

public class CmdPwrMgt {


	public enum PowerState {
		Ready((byte)0x00),
		Standby((byte)0x01),
		Sleep((byte)0x02);
		
		private byte bPowerState;
		
		PowerState(byte bPowerState) {
			this.bPowerState = bPowerState;
		}
	}

	
	/************************************************************
	 **					RFID_PowerEnterPowerState				*
	 ************************************************************/
	static final class RFID_PowerEnterPowerState extends CmdMti {
		public RFID_PowerEnterPowerState() {
			mCmdHead = CmdHead.RFID_PowerEnterPowerState;
		}

		public static RFID_PowerEnterPowerState newInstance() {
			return new RFID_PowerEnterPowerState(); 
		}

		public boolean setCmd(PowerState powerState) {
			mParam.clear();
			mParam.add(powerState.bPowerState);

			composeCmd();
			return checkStatus(200);
		}
	}

	
	/************************************************************
	 **					RFID_PowerSetIdleTime					*
	 ************************************************************/
	static final class RFID_PowerSetIdleTime extends CmdMti {
		public RFID_PowerSetIdleTime() {
			mCmdHead = CmdHead.RFID_PowerSetIdleTime;
		}

		public static RFID_PowerSetIdleTime newInstance() {
			return new RFID_PowerSetIdleTime(); 
		}

		public boolean setCmd(PowerState powerState) {
			mParam.clear();
			mParam.add(powerState.bPowerState);

			composeCmd();
			return checkStatus(200);
		}
	}

	
	/************************************************************
	 **					RFID_PowerGetIdleTime					*
	 ************************************************************/
	static final class RFID_PowerGetIdleTime extends CmdMti {
		public RFID_PowerGetIdleTime() {
			mCmdHead = CmdHead.RFID_PowerGetIdleTime;
		}

		public static RFID_PowerGetIdleTime newInstance() {
			return new RFID_PowerGetIdleTime(); 
		}

		public boolean setCmd(PowerState powerState) {
			mParam.clear();
			mParam.add(powerState.bPowerState);

			composeCmd();
			return checkStatus(200);
		}
	}
}
