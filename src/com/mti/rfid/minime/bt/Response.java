package com.mti.rfid.minime.bt;

import java.util.Arrays;


public class Response {
	private int iLength;
	private byte[] baData;
	
	public Response(byte[] data, int length) {
		baData = Arrays.copyOfRange(data, 0, length);
		iLength = length;
	}
	
	
	public boolean getStatus() {
		return Crc16.check(baData, iLength);
	}
	
	public byte[] getData() {
		return baData;
	}
	
}
