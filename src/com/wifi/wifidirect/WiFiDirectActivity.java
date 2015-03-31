/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wifi.wifidirect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An activity that uses WiFi Direct APIs to discover and connect with available
 * devices. WiFi Direct APIs are asynchronous and rely on callback mechanism
 * using interfaces to notify the application of operation success or failure.
 * The application should also register a BroadcastReceiver for notification of
 * WiFi state related events.
 */
public class WiFiDirectActivity extends Activity implements ChannelListener, PeerListListener, ConnectionInfoListener {

    public static final String TAG = "wifidirect";
    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

    private final IntentFilter intentFilter = new IntentFilter();
    private Channel channel;
    private BroadcastReceiver receiver = null;

	private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
	private WifiP2pInfo info;
	private WifiP2pDevice device;

    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
        

        // add necessary intent values to be matched.
        //WIFI_P2P_CONNECTION_CHANGED_ACTION
        //WIFI_P2P_DISCOVERY_CHANGED_ACTION
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        initiateDiscovery();
    }

    /** register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);
        return true;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.atn_direct_enable:
                if (manager != null && channel != null) {

                    // Since this is the system wireless settings activity, it's
                    // not going to send us a result. We will be notified by
                    // WiFiDeviceBroadcastReceiver instead.

                    //startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                	startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    
                } else {
                    Log.e(TAG, "channel or manager is null");
                }
                return true;

            case R.id.atn_direct_discover:
                if (!isWifiP2pEnabled) {
                    Toast.makeText(WiFiDirectActivity.this, R.string.p2p_off_warning, Toast.LENGTH_SHORT).show();
                    return true;
                }
/*                final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                        .findFragmentById(R.id.frag_list);
                fragment.onInitiateDiscovery();*/
                //initiateDiscovery();
                return true;
            case R.id.connect_dev:
            	if (isWifiP2pEnabled && (!peers.isEmpty())) {
/*            		for (Iterator iterator = peers.iterator(); iterator.hasNext();) {
    						WifiP2pDevice wifiP2pDevice = (WifiP2pDevice) iterator.next();
    						if (wifiP2pDevice.status != 0){
    		                    WifiP2pConfig config = new WifiP2pConfig();
    		                    config.deviceAddress = wifiP2pDevice.deviceAddress;
    		                    config.wps.setup = WpsInfo.PBC;
    		                    Toast.makeText(WiFiDirectActivity.this, "Connecting.......... " + wifiP2pDevice.deviceName , Toast.LENGTH_SHORT).show();
    		                    connect(config);
    						}
                		}*/
                    /*ConnectedDevicesFragment deviceFrag = (ConnectedDevicesFragment) getFragmentManager().findFragmentById(R.id.device_list_fragment);
                    deviceFrag.setPeers(peers);
                    deviceFrag.notifyPeers();
                    */
            		
            	}
            case R.id.show_devices:
            	if (isWifiP2pEnabled && (!peers.isEmpty())) {
            		for (Iterator iterator = peers.iterator(); iterator.hasNext();) {
						WifiP2pDevice wifiP2pDevice = (WifiP2pDevice) iterator.next();
						if (wifiP2pDevice.status == 0){
							Toast.makeText(WiFiDirectActivity.this, "Connected Device: " + wifiP2pDevice.deviceName , Toast.LENGTH_SHORT).show();
						}
            		}
            	}            	
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void initiateDiscovery() {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(WiFiDirectActivity.this, "Discovery Initiated Successful",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(WiFiDirectActivity.this, "Discovery Failed : " + reasonCode,
                        Toast.LENGTH_SHORT).show();
            }
        });		
	}

	public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new ActionListener() {

            @Override
            public void onSuccess() {
            	Toast.makeText(WiFiDirectActivity.this, "Connect successfull.", Toast.LENGTH_SHORT).show();
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(WiFiDirectActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void disconnect() {
        manager.removeGroup(channel, new ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);

            }

            @Override
            public void onSuccess() {
            }

        });
    }

    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this, "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void cancelDisconnect() {

        /*
         * A cancel abort request by user. Disconnect i.e. removeGroup if
         * already connected. Else, request WifiP2pManager to abort the ongoing
         * request
         */
        if (manager != null) {
                manager.cancelConnect(channel, new ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(WiFiDirectActivity.this, "Aborting connection",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(WiFiDirectActivity.this,"Connect abort request failed. Reason Code: " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
        }

    }

	public void resetData() {
		Toast.makeText(WiFiDirectActivity.this, "Reset Data",Toast.LENGTH_SHORT).show();
		
	}

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
        this.info = info;

        // The owner IP is now known.
        
        boolean groupOwner = info.isGroupOwner;
        // InetAddress from WifiP2pInfo struct.
        String hostaddress = info.groupOwnerAddress.getHostAddress();
        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        if (info.groupFormed && info.isGroupOwner) {
            //new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text)).execute();
        } else if (info.groupFormed) {
            // The other device acts as the client. In this case, we enable the
            // get file button.
        	
        }
        // hide the connect button
        //connectedDevices.put(info, value)
	}

	public void updateThisDevice(WifiP2pDevice device) {
		this.device = device;
		String deviceStatus = getDeviceStatus(device.status);
		String deviceAddr = device.deviceAddress;
		String deviceInfo = device.toString();
		StringBuilder connectedDevices = new StringBuilder();
		for (Iterator iterator = peers.iterator(); iterator.hasNext();) {
			WifiP2pDevice wifiP2pDevice = (WifiP2pDevice) iterator.next();
			connectedDevices.append(device.deviceName);
		}
		Toast.makeText(WiFiDirectActivity.this, device.deviceName + "  " + deviceStatus + " TO:  " + connectedDevices,Toast.LENGTH_SHORT).show();
		Log.d(WiFiDirectActivity.TAG, deviceAddr + deviceStatus + deviceInfo);
	}
    private static String getDeviceStatus(int deviceStatus) {
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

	@Override
	public void onPeersAvailable(WifiP2pDeviceList peerList) {
		peers.clear();
        peers.addAll(peerList.getDeviceList());
/*        ConnectedDevicesFragment deviceFrag = (ConnectedDevicesFragment) getFragmentManager().findFragmentById(R.id.device_list_fragment);
        deviceFrag.getListAdapter().setItems(peers);
        deviceFrag.getListAdapter().notifyDataSetChanged();*/
		StringBuilder availablePeers = new StringBuilder();
		for (Iterator iterator = peers.iterator(); iterator.hasNext();) {
			WifiP2pDevice wifiP2pDevice = (WifiP2pDevice) iterator.next();
			availablePeers.append(device.deviceName);
		}
        Toast.makeText(WiFiDirectActivity.this, "onPeersAvailable Peers found -  " + availablePeers + peerList.getDeviceList().size() ,Toast.LENGTH_SHORT).show();
        if (peers.size() == 0) {
            Log.d(WiFiDirectActivity.TAG, "No devices found");
            return;
        }
        DeviceListFragment fragment = new DeviceListFragment();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.container, fragment, "DeviceListFragment");
		transaction.addToBackStack(null);
		transaction.commit();
		fragment.setPeers(peers);     
		
		// TODO We have the list of devices now try and connect all and store them in a connected list of devices to be show
		// in a new list of connected devices, all connected devices will be shown on that Fragment aka ConnectedFragment
		// and available peers on DiscoveryFragment
		
	}
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		private TextView my_name_view;
		private TextView my_status_view;
		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,false);
			my_name_view = (TextView) rootView.findViewById(R.id.my_name);
			my_status_view = (TextView) rootView.findViewById(R.id.my_status);
			setDeviceStatus();
			return rootView;
		}
	    public void setDeviceStatus() {
	        my_name_view.setText("Waiting for device name...");
	        my_status_view.setText("Status retrieving...");
	    }
	}

	
}
