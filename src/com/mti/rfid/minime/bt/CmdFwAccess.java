package com.mti.rfid.minime.bt;

public class CmdFwAccess {
	
	public enum ModuleId {
		FirmwareId((byte)0x00),
		HardwareId((byte)0x01),
		OEMCfgId((byte)0x02),
		OEMCfgUpdateId((byte)0x03);
		
		private byte bModuleId;
		
		ModuleId(byte bModuleId) {
			this.bModuleId = bModuleId;
		}
	}
	
	
	/************************************************************
	 *					RFID_MacGetModuleID						*
	 ************************************************************/
	static final class RFID_MacGetModuleID extends CmdMti {
		public RFID_MacGetModuleID() {
			mCmdHead = CmdHead.RFID_MacGetModuleID;
		}

		public static RFID_MacGetModuleID newInstance() {
			return new RFID_MacGetModuleID(); 
		}

		public boolean setCmd(ModuleId moduleId) {
			mParam.add(moduleId.bModuleId);

			super.composeCmd();
			return true;
		}
	}
	
	
	/************************************************************
	 *					RFID_MacGetDebugValue					*
	 ************************************************************/
	static final class RFID_MacGetDebugValue extends CmdMti {
		public RFID_MacGetDebugValue() {
			mCmdHead = CmdHead.RFID_MacGetDebugValue;
		}

		public static RFID_MacGetDebugValue newInstance() {
			return new RFID_MacGetDebugValue(); 
		}

		public boolean setCmd() {
			super.composeCmd();
			return true;
		}
	}
	
	
	/************************************************************
	 *				RFID_MacBypassWriteRegister					*
	 ************************************************************/
	static final class RFID_MacBypassWriteRegister extends CmdMti {
		public RFID_MacBypassWriteRegister() {
			mCmdHead = CmdHead.RFID_MacBypassWriteRegister;
		}

		public static RFID_MacBypassWriteRegister newInstance() {
			return new RFID_MacBypassWriteRegister(); 
		}

		public boolean setCmd(byte regAddress, byte[] regData) {
			mParam.add(regAddress);
			for(byte register : regData)
				mParam.add(register);

			super.composeCmd();
			return true;
		}
	}
	
	
	/************************************************************
	 *				RFID_MacBypassReadRegister					*
	 ************************************************************/
	static final class RFID_MacBypassReadRegister extends CmdMti {
		public RFID_MacBypassReadRegister() {
			mCmdHead = CmdHead.RFID_MacBypassReadRegister;
		}

		public static RFID_MacBypassReadRegister newInstance() {
			return new RFID_MacBypassReadRegister(); 
		}

		public boolean setCmd(byte regAddress) {
			mParam.add(regAddress);

			super.composeCmd();
			return true;
		}
	}
	
	
	/************************************************************
	 *					RFID_MacWriteOemData					*
	 ************************************************************/
	static final class RFID_MacWriteOemData extends CmdMti {
		public RFID_MacWriteOemData() {
			mCmdHead = CmdHead.RFID_MacWriteOemData;
		}

		public static RFID_MacWriteOemData newInstance() {
			return new RFID_MacWriteOemData(); 
		}

		public boolean setCmd(int oemCfgAddress, byte oemCfgData) {
			if(oemCfgAddress < 0x0080 || oemCfgAddress > 0x07ff) {
				// error
				return false;
			} else {
				for(int i = 0; i < 2; i++)
					mParam.add((byte)(oemCfgAddress >> i * 8));
				mParam.add(oemCfgData);
	
				super.composeCmd();
			}
			return true;
		}
	}
	
	
	/************************************************************
	 *					RFID_MacReadOemData						*
	 ************************************************************/
	static final class RFID_MacReadOemData extends CmdMti {
		public RFID_MacReadOemData() {
			mCmdHead = CmdHead.RFID_MacReadOemData;
		}

		public static RFID_MacReadOemData newInstance() {
			return new RFID_MacReadOemData(); 
		}

		public boolean setCmd(int oemCfgAddress) {
			if(oemCfgAddress < 0x0000 || oemCfgAddress > 0x1fff) {
				// error
				return false;
			} else {
				for(int i = 1; i >= 0; i--)
					mParam.add((byte)(oemCfgAddress >> i * 8));
	
				super.composeCmd();
			}
			delay(50);
			return checkStatus();
		}
		
		public byte getData() {
			return mResponse[headerSize+3];
		}
	}
	
	
	/************************************************************
	 *					RFID_MacSoftReset						*
	 ************************************************************/
	static final class RFID_MacSoftReset extends CmdMti {
		public RFID_MacSoftReset() {
			mCmdHead = CmdHead.RFID_MacSoftReset;
		}

		public static RFID_MacSoftReset newInstance() {
			return new RFID_MacSoftReset(); 
		}

		public boolean setCmd() {
			super.composeCmd();
			return true;
		}
	}
	
	
	/************************************************************
	 *					RFID_MacEnterUpdateMode					*
	 ************************************************************/
	static final class RFID_MacEnterUpdateMode extends CmdMti {
		public RFID_MacEnterUpdateMode() {
			mCmdHead = CmdHead.RFID_MacEnterUpdateMode;
		}

		public static RFID_MacEnterUpdateMode newInstance() {
			return new RFID_MacEnterUpdateMode();
		}

		public boolean setCmd() {
			super.composeCmd();
			return true;
		}
	}

}
