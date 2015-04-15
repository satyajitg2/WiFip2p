package com.wifi.background;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import com.wifi.chat.ChatClient;
import com.wifi.chat.ChatServer;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.widget.Toast;

public class SampleService extends Service {
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    // Random number generator
    private final Random mGenerator = new Random();
    IntentService intt;
    private NetworkConnection mNetwork;
	private HashMap<String, WifiP2pDevice> peerMap = new HashMap<String, WifiP2pDevice>();

	public static WifiP2pDevice hostWifiDevice;

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
    	SampleService getService() {
            // Return this instance of LocalService so clients can call public methods
    		System.out.println("TRACE SampleService getService");
            return SampleService.this;
        }
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		System.out.println("TRACE onBind SampleService");
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mNetwork = new NetworkConnection();
		System.out.println("TRACE SampleService onCreate");
	}


	
	@Override
	public void onDestroy() {
		mNetwork.resetNetwork();
		super.onDestroy();
	}

    /** method for clients */
    public int getRandomNumber() {
      return mGenerator.nextInt(100);
    }

	public NetworkConnection getmNetwork() {
		return mNetwork;
	}

	public void setmNetwork(NetworkConnection mNetwork) {
		this.mNetwork = mNetwork;
	}

	public void manageHostClient(String hostaddress, int port, String mac) {
		System.out.println("TRACE SampleService manageHostClient");
		// TODO Let the NetworkConnection create a client.
		mNetwork.manageHostClient(hostaddress, port, mac);
	}

	public boolean manageHostServer(String deviceAddress) {
		if ( hostWifiDevice != null ) {
			return mNetwork.manageHostServer(hostWifiDevice.deviceAddress);
		}
		return mNetwork.manageHostServer(deviceAddress);
	}

	public void sendDevice(String string) {
		System.out.println("TRACE SampleServe sendDevice String : "+string);
	}

	public void sendWifiDevice(WifiP2pDevice device) {
		this.hostWifiDevice = device;
		System.out.println("TRACE SampleServe sendWifiDevice: " + device.deviceName +" "+ device.deviceAddress + " " +device.describeContents());
	}

	public static WifiP2pDevice getHostWifiDevice() {
		return hostWifiDevice;
	}

	public void setPeers(HashMap<String, WifiP2pDevice> mapList) {
		if (peerMap.size() == 0) {
			peerMap = mapList;
			return;
		}

		Set<String> keyset = mapList.keySet();
		for (Iterator iterator = keyset.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			if (!peerMap.containsKey(string)) {
				//Add new device
				peerMap.put(string, mapList.get(string));
			}
		}
	}
	public HashMap<String, WifiP2pDevice> getPeerMap() {
		return peerMap;
	}

	public void sendClickEventToDevice(String deviceAddress, String msg, Handler handler) {
		mNetwork.sendClickEventToDevice(deviceAddress, msg, handler);
	}
}
