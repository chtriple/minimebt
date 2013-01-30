package com.mti.rfid.minime.bt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

@SuppressLint("HandlerLeak")
public class BtCommunication extends Application {
	private static final boolean DEBUG = false;
	private static final String TAG = "MINIMEBT";
	private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private BluetoothAdapter mBtAdapter;
	private BluetoothSocket mBtSocket;
	private static InputStream mBtInStream;
	private static OutputStream mBtOutStream;
	
	private AcceptThread acceptThread;
	private ConnectThread connectThread;

	private Response mResponse;
	private String mDevAddr = null;
	public  String msg;

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
				if(DEBUG) Log.d(TAG, "accept thread constructor success");
			} catch(IOException e) {
				Log.e(TAG, "caccept thread constructor fail");
			}
			mmServerSocket = tmp;
		}
		
		public void run() {
			mBtSocket = null;

			while(mBtSocket == null) {
				try {
					mBtSocket = mmServerSocket.accept();
					if(DEBUG) Log.d(TAG, "open server socket success");
				} catch(IOException e) {
					Log.e(TAG, "open server socket failed");
					break;
				}
	
				if(mBtSocket != null) {
					CreateStream(mBtSocket);
					try {
						mmServerSocket.close();
						if(DEBUG) Log.d(TAG, "close server socket success");
					} catch (IOException e) {
						Log.e(TAG, "close server socket fail");
					}
				}
			}
		}
	}
	
	private class ConnectThread extends Thread {
		private ConnectThread() {
			try {
				mBtSocket = mBtAdapter.getRemoteDevice(mDevAddr).createRfcommSocketToServiceRecord(SPP_UUID);
				if(DEBUG) Log.d(TAG, "connect thread constructor success");
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "connect Thread constructor fail");
			}
		}
		
		public void run() {
			mBtAdapter.cancelDiscovery();
			
			try {
				mBtSocket.connect();
				if(DEBUG) Log.d(TAG, "bluetooth socket connect success");
			} catch (IOException eConnection) {
				try {
					mBtSocket.close();
					Log.d(TAG, "bluetooth socket connect fail");
				} catch (IOException eClose) {
					Log.d(TAG, "bluetooth socket connect fail");
				}
				return;
			}
			CreateStream(mBtSocket);
		}
	}

	
	Handler btHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message inputMessage) {
			MainActivity.setConnectionStatus(true);
		}
	};
	
	
	/* ################## private methods #################### */
	private void CreateStream(BluetoothSocket socket) {
		try {
			mBtInStream = socket.getInputStream();
			mBtOutStream = socket.getOutputStream();
			btHandler.sendEmptyMessage(0);
			if(DEBUG) Log.d(TAG, "connected thread success");
		} catch (IOException e) {
			Log.e(TAG, "connected thread fail");
		}
	}
	

	/* ################## public methods #################### */
	public BluetoothAdapter getBtAdapter() {
		return mBtAdapter;
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
	
	public AcceptThread getAcceptThread() {
		return acceptThread;
	}
	
	public void createConnectThread() {
		connectThread = new ConnectThread();
		connectThread.start();
	}
	
	public ConnectThread getConnectThread() {
		return connectThread;
	}

	public void sendCmd(byte[] cmd) {
		try {
			synchronized(mBtInStream) {
				mBtInStream.skip(mBtInStream.available());
			}
			mBtOutStream.write(cmd);
		} catch (IOException e) {
			Log.w(TAG, "send command fail");
		}
	}

	public Response getResponse(int timeout) {
		final Thread responseThread = new Thread(new Runnable() {
			int bytes = 0;
			byte[] buffer = new byte[64];
			@Override
			public void run() {
				try {
					synchronized(mBtInStream) {
						bytes = mBtInStream.read(buffer);
					}
					mResponse = new Response(buffer, bytes);
				} catch (IOException e) {
					mResponse = null;
					Log.e(TAG, "get response fail");
				}
			}
		});

		synchronized(responseThread) {
			SystemClock.sleep(timeout / 2);
			responseThread.start();
			try {
				responseThread.wait(timeout / 2);
			} catch (InterruptedException e) {
				Log.e(TAG, "response thread wait fail");
			}
		}
		return mResponse;
	}
	
	public void disconnect() {
		try {
			if(mBtSocket.isConnected())
				mBtSocket.close();
		} catch (IOException e) {
			Log.d(TAG, "bluetooth socket close fail");
		} finally {
			MainActivity.checkConnectionStatus();
		}
	}

	public boolean checkConnectionStatus() {
        if(mBtAdapter.isEnabled())
        	try{
        		if(mBtSocket.isConnected()) {
            		if(DEBUG) Log.d(TAG, "check connection status: is connected");
        			return true;
        		}
        	} catch(Exception e) {
        		Log.w(TAG, "the connection of bluetooth socket is not available");
        	}
        return false;
	}
}
