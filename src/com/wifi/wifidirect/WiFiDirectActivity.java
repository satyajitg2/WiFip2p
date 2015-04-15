package com.wifi.wifidirect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.wifi.background.SampleService;
import com.wifi.background.ServiceManager;
import com.wifi.chat.ChatClient;
import com.wifi.chat.P2PChatFragment;
import com.wifi.wifidirect.DeviceListFragment.DeviceListActionListener;

/**
 * An activity that uses WiFi Direct APIs to discover and connect with available
 * devices. WiFi Direct APIs are asynchronous and rely on callback mechanism
 * using interfaces to notify the application of operation success or failure.
 * The application should also register a BroadcastReceiver for notification of
 * WiFi state related events.
 */
public class WiFiDirectActivity extends Activity implements ChannelListener, PeerListListener, ConnectionInfoListener, DeviceListActionListener {

    public static final String TAG = " TRACE Doot";
    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

    private final IntentFilter intentFilter = new IntentFilter();
    private Channel channel;
    private BroadcastReceiver receiver = null;

	private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
	private HashMap<String, WifiP2pDevice> peerMap = new HashMap<String, WifiP2pDevice>();
	public static WifiP2pDevice hostWifiDevice;
	ContentResolver contentResolver;
	private ServiceManager mSerManager;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		Intent intent = new Intent(this, SampleService.class);
		startService(intent);
        mSerManager = new ServiceManager(getApplicationContext());
        
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        initiateDiscovery();

        if (savedInstanceState == null) {
			//getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment(), "PlaceHolderFragment").commit();
        	addDeviceListFragment();
		}
        
		contentResolver = getContentResolver();
        //mMedia = new MediaManager(contentResolver);
        //new WifiAsyncTask(getApplicationContext(),contentResolver, mMedia).execute();
    }
    
	@Override
	public void onPeersAvailable(WifiP2pDeviceList peerList) {
		peers.clear();
        peers.addAll(peerList.getDeviceList());

		StringBuilder availablePeers = new StringBuilder();
		for (Iterator iterator = peers.iterator(); iterator.hasNext();) {
			WifiP2pDevice wifiP2pDevice = (WifiP2pDevice) iterator.next();
			availablePeers.append(wifiP2pDevice.deviceName);
			peerMap.put(wifiP2pDevice.deviceAddress, wifiP2pDevice);
		}
		mSerManager.setDevicePeersMap(peerMap);
        
        if (peers.size() == 0) {
            Log.d(WiFiDirectActivity.TAG, "No devices found");
    		Toast.makeText(WiFiDirectActivity.this, "Peers not found -  " + availablePeers + peerList.getDeviceList().size() ,Toast.LENGTH_SHORT).show();
            return;
        }
        notifyFragmentDataChange();
	}    
	
	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		Log.d(WiFiDirectActivity.TAG, "TRACE Callback onConnectionInfoAvailable info data: " + info.groupOwnerAddress + " " + info.groupFormed  );

        // The owner IP is now known.
        String hostaddress = info.groupOwnerAddress.getHostAddress();
    	String mac = null;
		if (mSerManager.getHostDevice() != null) {
			 mac = mSerManager.getHostDevice().deviceAddress;
		}

        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        if (info.groupFormed && info.isGroupOwner) {
			//new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text)).execute();
        	startSocketServer(mac);
        } else if (info.groupFormed) {
        	// Device is a client.
            // The other device acts as the client. In this case, we enable the
            // get file button.
        	startHostClient(hostaddress, 8988, this, mac);
        }
	}


	private void startHostClient(String hostaddress, int port,	WiFiDirectActivity wiFiDirectActivity, String mac) {
		// TODO Manage device as a Client.
		mSerManager.manageHostClient(hostaddress, port, wiFiDirectActivity, mac);
	}

	private void startSocketServer(String deviceAddress) {
		// TODO Manager device as a Server.
		mSerManager.manageHostServer(deviceAddress);
	}
	
    @Override
    protected void onStart() {
    	super.onStart();
    	mSerManager.onActivityStartBindService();

    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	mSerManager.onActivityStopUnbind();
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
    protected void onDestroy() {
    	//TODO Enable better handling and cleaning.
    	Intent intent = new Intent(this, SampleService.class);
    	stopService(intent);
    	super.onDestroy();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);
        return true;
    }

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.atn_direct_enable:
                if (manager != null && channel != null) {
                    // Since this is the system wireless settings activity, it's
                    // not going to send us a result. We will be notified by
                    // WiFiDeviceBroadcastReceiver instead.
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
                initiateDiscovery();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void connectDevice(String deviceAddr) {
		// For a connect all, request without checking status. 
		// TODO Check individual device is connected, not possible using
		// device.isCONNECTED as this returns true even connected to one device.
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = deviceAddr;
        config.wps.setup = WpsInfo.PBC;
		connect(config);
    }
    
    public void addDeviceListFragment() {
        DeviceListFragment fragmentList = new DeviceListFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        fragmentList.setServiceManager(mSerManager);
		transaction.add(R.id.container, fragmentList, "DeviceListFragment");
		transaction.commit();
		fragmentList.setPeers(peers);     
    	return;
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

    @Override
	public void connect(final WifiP2pConfig config) {
		
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

    @Override
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
        	Log.d(TAG, "Channel lost. Trying again ");
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

	public void updateThisDevice(WifiP2pDevice device) {
		if (device == null) {
			Log.d(TAG, " updateThisDevice Device is null");
			return;
		}
		hostWifiDevice = device;
		Log.d(TAG, " WiFiDirectActivity updateThisDevice " + device.deviceAddress + device.status ); 
		Toast.makeText(WiFiDirectActivity.this, "Host Device Updated",Toast.LENGTH_SHORT).show();
		if (device != null) {
			mSerManager.sendDeviceData(device);
		}
		
	}
	
	private void notifyFragmentDataChange() {
		Log.d(TAG, " WiFiDirectActivity notifyFragmentDataChange ");
		DeviceListFragment fragment = (DeviceListFragment) getFragmentManager().findFragmentByTag("DeviceListFragment");
        if (fragment != null) {
        	HashMap<String, WifiP2pDevice> map = mSerManager.getPeerMap();
        	List<WifiP2pDevice> list = new ArrayList<WifiP2pDevice>();
        	Set<String> keyset = map.keySet();
        	for (String key : keyset) {
        		list.add(map.get(key));
			}
        	fragment.setPeers(list);
        }
	}

	@Override
	public void startP2PChat(WifiP2pDevice device) {
		Log.d(TAG, " WiFiDirectActivity startP2PChat ");
		P2PChatFragment fragment = new P2PChatFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        fragment.setDevice(device);
        fragment.setServiceManager(mSerManager);
		transaction.replace(R.id.container, fragment, "P2PChatFragment");
		transaction.commit();
	}

}
