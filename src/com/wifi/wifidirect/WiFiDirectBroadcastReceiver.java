package com.wifi.wifidirect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.wifi.background.DootService;
import com.wifi.background.ServiceManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private Channel channel;
    private WiFiDirectActivity activity;
    private DootService service;

    /**
     * @param manager WifiP2pManager system service
     * @param channel Wifi p2p channel
     * @param activity activity associated with the receiver
     */
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel, WiFiDirectActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
        this.service = activity.getmSerManager().getmService();
    }

    /*
     * (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
     * android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled
                activity.setIsWifiP2pEnabled(true);
            } else {
                activity.setIsWifiP2pEnabled(false);
            }
            
            activity.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
            
            
            Log.d(WiFiDirectActivity.TAG, "P2P state changed - " + state);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (manager != null) {
                manager.requestPeers(channel, (PeerListListener) activity);
            }
            Log.d(WiFiDirectActivity.TAG, "P2P peers changed");
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (manager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            
            if (networkInfo.isConnected()) {
                // we are connected with the other device, request connection
                // info to find group owner IP
                manager.requestConnectionInfo(channel, activity);
            } else {
                // It's a disconnect
            	//activity.resetData();
            }
            Log.d(WiFiDirectActivity.TAG, "P2P Connection changed, network connectivity is " + networkInfo.isConnected());
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
        	WifiP2pDevice thisDevice = (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
        	System.out.println("TRACE WifiBroadcast this device changed action");
        	deviceChangedAction(thisDevice, context, intent);
        		// Request Connection Info
        	getConnectedPeers(context,intent);
        	
            activity.updateThisDevice(thisDevice);
            //
        	//TODO Do something when this device changes action.
            Log.d(WiFiDirectActivity.TAG, "P2P Host Device changed action. ");
        } 
    }

	private void deviceChangedAction(WifiP2pDevice thisDevice, Context context, Intent intent) {
		System.out.println("TRACE WifiBroadcastReceiver deviceChangedAction " + ServiceManager.getDeviceStatus(thisDevice.status));
		if (thisDevice.status == WifiP2pDevice.CONNECTED) {
			manager.requestGroupInfo(channel, (GroupInfoListener) service);
		}
	}

	private void getConnectedPeers(Context context, Intent intent) {
		// TODO Auto-generated method stub
        NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        WifiP2pGroup groupInfo = (WifiP2pGroup) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);
        
        if (networkInfo != null && ( networkInfo.isConnected() )) {
        	Log.d(WiFiDirectActivity.TAG, "getConnectedPeers Network is connected.");
        }
        if (groupInfo != null ) {
        	System.out.println("TRACE ********************************************" +groupInfo.getNetworkName());
        	System.out.println("TRACE ********************************************" +groupInfo.getInterface());
        	System.out.println("TRACE ********************************************" +groupInfo.getOwner());
        	System.out.println("TRACE ********************************************" +groupInfo.getClientList().size());
        	
        	Collection<WifiP2pDevice> col = groupInfo.getClientList();
        	List<WifiP2pDevice> list = new ArrayList<WifiP2pDevice>();
        	list.addAll(col);
        	
        	for (WifiP2pDevice wifiP2pDevice : list) {
				System.out.println("TRACE ******************************************** BroadCast Devilist: " + wifiP2pDevice.deviceName + " " + wifiP2pDevice.deviceAddress + " " + wifiP2pDevice.status);
			}
        }
        
        
		
		
	}

	private void checkConnectedPeers(Intent intent) {
		//        	checkConnectedPeers(intent);          

        NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        if (networkInfo.isConnected()) {
        	intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);
        }		
	}
}
