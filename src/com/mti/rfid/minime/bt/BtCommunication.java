package com.mti.rfid.minime.bt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
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
	private static final boolean DEBUG = true;
	private static final String TAG = "MINIMEBT";
	private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private BluetoothAdapter mBtAdapter;
	private BluetoothSocket mBtSocket;
	private static InputStream mBtInStream;
	private static OutputStream mBtOutStream;
	
	private AcceptThread acceptThread;
	private ConnectThread connectThread;
	private ConnectedThread connectedThread;

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
					connectedThread = new ConnectedThread(mBtSocket);
					connectedThread.start();
					try {
						mmServerSocket.close();
						if(DEBUG) Log.d(TAG, "close server socket success");
					} catch (IOException e) {
						Log.e(TAG, "close server socket fail");
					}
				}
			}
		}
		
		public void cancel() {
			try {
				mmServerSocket.close();
				if(DEBUG) Log.d(TAG, "close server socket success");
			} catch(IOException e) {
				Log.e(TAG, "close server socket fail");
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
			
			connectedThread = new ConnectedThread(mBtSocket);
			connectedThread.start();
		}
		
		public void cancel() {
			try {
				mBtSocket.close();
				if(DEBUG) Log.d(TAG, "bluetooth socket connect success");
			} catch (IOException e) {
				e.printStackTrace();
				Log.d(TAG, "bluetooth socket connect fail");
			}
		}
	}
	
	
	private class ConnectedThread extends Thread {
		private ConnectedThread(BluetoothSocket socket) {
			
			try {
				mBtInStream = socket.getInputStream();
				mBtOutStream = socket.getOutputStream();
				btHandler.sendEmptyMessage(0);
				Log.w(TAG, "bluetooth socket : " + String.valueOf(mBtSocket.isConnected()));
				Log.w(TAG, "bluetooth remove device : " + mBtSocket.getRemoteDevice().getName());
				if(DEBUG) Log.d(TAG, "connected thread success");
			} catch (IOException e) {
				Log.e(TAG, "connected thread fail");
			}
		}
		
		public void run() {
/*
			byte[] buffer = new byte[64];
			int bytes = 0;

			while(true) {
				try {
					if(DEBUG) Log.d(TAG, "spp receiver before");
					bytes = mBtInStream.read(buffer);
					
					if(DEBUG) Log.d(TAG, String.format("%2x %2x %2x", buffer[0], buffer[1], buffer[2]));
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
*/
		}
	}
	
	
	public void disconnect() {
		mBtInStream = null;
		mBtOutStream = null;
//		acceptThread = new AcceptThread();
//		acceptThread.start();
		Log.e(TAG, "disconnect");
	}

	Handler btHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message inputMessage) {
			MainActivity.setConnectionStatus(true);
		}
	};
	
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

	public void sendCmd(byte[] cmd) {
		try {
			mBtInStream.skip(mBtInStream.available());
			mBtOutStream.write(cmd);
			if(DEBUG) Log.d(TAG, "send command success");
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "send command fail");
		}
	}

	private Response response;
	public Response getResponse(int timeout) {
		final Thread responseThread = new Thread(new Runnable() {
			int bytes = 0;
			byte[] buffer = new byte[64];
			@Override
			public void run() {
				try {
					bytes = mBtInStream.read(buffer);
					response = new Response(buffer, bytes);
					if(DEBUG) Log.d(TAG, "get response success");
				} catch (IOException e) {
					response = null;
					Log.e(TAG, "get response fail");
				}
			}
		});

		TimerTask checkTask = new TimerTask() {
			public void run() {
				if(!responseThread.isAlive())
					responseThread.interrupt();
			}
		};
		

		synchronized(responseThread) {
			SystemClock.sleep(timeout / 2);
			responseThread.start();
//			Timer timer = new Timer();
//			timer.schedule(checkTask, 50, 50);
			try {
				responseThread.wait(timeout / 2);
//				timer.cancel();
			} catch (InterruptedException e) {
				Log.e(TAG, "thread wait exception");
			}
		}
		return response;
	}
	
	public boolean checkConnectionStatus() {
        if(mBtAdapter.isEnabled())
        	try{
        		Log.w(TAG, "bluetooth remove device : " + mBtSocket.getRemoteDevice().getName());
        		if(mBtSocket.isConnected())
//        		if(true)
        			return true;
        	} catch(Exception e) {
        		Log.e(TAG, "the connection of bluetooth socket is not available");
        	}
        return false;
	}
}
