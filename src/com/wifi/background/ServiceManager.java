package com.wifi.background;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.animation.BounceInterpolator;

import com.wifi.background.SampleService.LocalBinder;
import com.wifi.wifidirect.WiFiDirectActivity;

/**
 * @author satyajit singh
 * ServiceManager serves as an interface to connect to SampleService.
 * This server as an interface to any activity and its context to connect 
 * and interact with the service.  
 */
public class ServiceManager {
	SampleService mService;
	boolean mBound;
	private Context mContext;
	private Handler handlerMess;
	private WiFiDirectActivity activity;
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			System.out.println("TRACE onServiceConnected");
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBound = false;
		}
	};
	
	public ServiceManager(Context context) {
		mContext = context;

	}

	public static String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }


	public void onActivityStartBindService(){
        // Bind to LocalService
		System.out.println("TRACE onActivityStartBindService");

        Intent intent = new Intent(mContext, SampleService.class);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	public void onActivityStopUnbind() {
		if (mBound) {	
			mContext.unbindService(mConnection);
		}
	}
	
	public void callFromActivity() {
		System.out.println("TRACE callFromActivity" + mBound);
		if (mBound) {
			int num = mService.getRandomNumber();
			sendMessage(num);
		}
	}

	private void sendMessage(int num) {
		Message msg = Message.obtain();
		Bundle data = new Bundle();
		data.putInt("Bundle1", num);
		msg.setData(data);
		handlerMess.dispatchMessage(msg);		
	}

	public void registerCallback(Handler handlerGroupOwner) {
		handlerMess = handlerGroupOwner;
	}

	public void manageHostClient(String hostaddress, int port,
			WiFiDirectActivity wiFiDirectActivity, String mac) {
		//TODO Instruct service networkManager to manage a client for this device.
		activity = wiFiDirectActivity;
		if (mBound) {
			mService.manageHostClient(hostaddress, port, mac);
		}
	}

	public void manageHostServer(String deviceAddress) {
		// TODO Auto-generated method stub
		boolean response = mService.manageHostServer(deviceAddress);
	}

	public void sendDeviceData(WifiP2pDevice device) {
		//mService.registerReceiver(receiver, filter)
		if (mBound) {
			mService.sendDevice(device.toString());
			mService.sendWifiDevice(device);
		}
	}
	public WifiP2pDevice getHostDevice() {
		if (mBound) {
			return mService.getHostWifiDevice();
		}
		return null;
	}
	public NetworkConnection getNetworkManager() {
		if (mBound) {
			return mService.getmNetwork();
		}
		return null;
	}

	public void setDevicePeersMap(HashMap<String, WifiP2pDevice> peerMap) {
		if (mBound) {
			mService.setPeers(peerMap);
		}
	}

	public void sendClickEventToDevice(String deviceAddress, String msg, Handler handler) {
		if (mBound) {
			mService.sendClickEventToDevice(deviceAddress, msg, handler);
		}
	}
	
	public HashMap<String, WifiP2pDevice> getPeerMap () {
		if (mBound) {
			return mService.getPeerMap();
		}
		return new HashMap<String, WifiP2pDevice>();
	}

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(WiFiDirectActivity.TAG, e.toString());
            return false;
        }
        return true;
    }
	
}
