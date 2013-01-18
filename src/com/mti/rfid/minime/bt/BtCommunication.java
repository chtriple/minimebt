package com.mti.rfid.minime.bt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class BtCommunication extends Application {
	private static boolean DEBUG = true;
	private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final String TAG = "MINIMEBT";

	private static BluetoothAdapter mBtAdapter;
	private static BluetoothSocket mBtSocket;
	private static InputStream mBtInStream;
	private static OutputStream mBtOutStream;
	
	private static AcceptThread acceptThread;
	private static ConnectThread connectThread;
	private static ConnectedThread connectedThread;

	private static boolean sppConnected = false;
	private static String mDevAddr = null;
	public static String msg;

	public BtCommunication() {
    	mBtAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	
	/* ########################## thread ############################# */
	private class AcceptThread extends Thread {
		private final BluetoothServerSocket mmServerSocket;
		
		private AcceptThread() {
			BluetoothServerSocket tmp = null;
			try {
				tmp = mBtAdapter.listenUsingRfcommWithServiceRecord("spp", SPP_UUID);
			} catch(IOException e) {
				e.printStackTrace();
			}
			mmServerSocket = tmp;
		}
		
		public void run() {
			mBtSocket = null;

			while(true) {
				if(DEBUG) Log.d(TAG, "open server socket.");
				try {
					mBtSocket = mmServerSocket.accept();
				} catch(IOException e) {
					e.printStackTrace();
					Log.w(TAG, "server socket accept failed.");
					break;
				}
	
				if(mBtSocket != null) {
					connectedThread = new ConnectedThread(mBtSocket);
					connectedThread.start();
					try {
						mmServerSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
						Log.w(TAG, "accept fail");
					}
				}
			}
		}
		
		public void cancel() {
			try {
				mmServerSocket.close();
			} catch(IOException e) {
				e.printStackTrace();
				Log.e(TAG, "close sever socket failed");
			}
		}
	}
	
	private class ConnectThread extends Thread {
		private ConnectThread() {
			try {
				mBtSocket = mBtAdapter.getRemoteDevice(mDevAddr).createRfcommSocketToServiceRecord(SPP_UUID);
				if(DEBUG) Log.d(TAG, "connect thread constructor successed");
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "connect Thread constructor failed");
			}
		}
		
		public void run() {
			mBtAdapter.cancelDiscovery();
			
			try {
				mBtSocket.connect();
			} catch (IOException eConnection) {
				try {
					mBtSocket.close();
					eConnection.printStackTrace();
				} catch (IOException eClose) {
					eClose.printStackTrace();
				}
				return;
			}
			
			connectedThread = new ConnectedThread(mBtSocket);
			connectedThread.start();
		}
		
		public void cancel() {
			try {
				mBtSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private class ConnectedThread extends Thread {
		private ConnectedThread(BluetoothSocket socket) {
			
			try {
				mBtInStream = socket.getInputStream();
				mBtOutStream = socket.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(DEBUG) Log.d(TAG, "inStream: " + mBtInStream.toString() + ", outStream: " + mBtOutStream.toString());
		}
		
		public void run() {
			byte[] buffer = new byte[64];
			int bytes = 0;
			
			while(true) {
				try {
					bytes = mBtInStream.read(buffer);
					if(DEBUG) Log.d(TAG, "spp receiver");
					if(bytes > 0) {
						msg = new String(buffer, 0, bytes, "ascii") + "\n";
					}
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "spp receiver disconnect");
					disconnect();
					break;
				}
			}
		}
	}
	
	
	public void disconnect() {
		sppConnected = false;
		mBtInStream = null;
		mBtOutStream = null;
//		acceptThread = new AcceptThread();
//		acceptThread.start();
		Log.e(TAG, "disconnect");
	}

	
	/* ################## public methods #################### */
	public BluetoothAdapter getBtAdapter() {
		return mBtAdapter;
	}
	
	public OutputStream getOutputStream() {
		return mBtOutStream;
	}
	
	public void devSelection(String devAddr) {
		mDevAddr = devAddr;
	}
	
	public String getMsg() {
		return msg;
	}
	
	public void createAcceptThread() {
        acceptThread = new AcceptThread();
        acceptThread.start();
	}
	
	public void cancelAcceptThread() {
		acceptThread.cancel();
		acceptThread = null;
	}
	
	public AcceptThread getAcceptThread() {
		return acceptThread;
	}
	
	
	public void createConnectThread() {
		connectThread = new ConnectThread();
		connectThread.start();
	}
	
	public void cancelConnectThread() {
		connectThread.cancel();
		connectThread = null;
	}
	
	public ConnectThread getConnectThread() {
		return connectThread;
	}
}
