package com.mti.rfid.minime.bt;

public final class Crc16 {
	private static final int poly = 0x1021;
	private static final int[] crcTable = new int[256];

	static {
	    for(int i = 0; i < 256; i++) {
	        int fcs = 0;
	        int d = i << 8;
	        for (int k = 0; k < 8; k++) {
	            if (((fcs ^ d) & 0x8000) != 0)
	                fcs = (fcs << 1) ^ poly;
	            else
	                fcs = (fcs << 1);
	            d <<= 1;
	            fcs &= 0xffff;
	        }
	        crcTable[i] = fcs;
	    }
    }

	public static int calculate(byte[] bytes, int length) {
		int work = 0xffff;
		
		for(int i = 0; i < length; i++)
			work = (crcTable[(bytes[i] ^ (work >>> 8)) & 0xff ] ^ (work << 8)) & 0xffff;
		
		return work;
	}
	
	public static boolean check(byte[] bytes, int length) {
		return (calculate(bytes, length) == 0x1d0f);
	}
}
