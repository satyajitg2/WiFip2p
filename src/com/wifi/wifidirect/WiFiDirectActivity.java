package com.wifi.wifidirect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
import com.wifi.chat.ChatClient;
import com.wifi.chat.ChatClient.ChatClientCallBack;
import com.wifi.chat.ChatServer;
import com.wifi.chat.P2PChatFragment;

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

	//********************* GENERIC ROLE*************************//	
    //Update Peers available when Peers Change
	private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
	
	//Peer MAC -> Device, updated when Peers Change
	private HashMap<String, WifiP2pDevice> peerMap = new HashMap<String, WifiP2pDevice>();

	//********************* SERVER ROLE*************************//
	// Map for clients, MAC Addresss --> Peer Device acting as my clients.
	private HashMap<String, WifiP2pDevice> myClientDeviceMap = new HashMap<String, WifiP2pDevice>();


	
	//********************* CLIENT ROLE*************************//	
	//Peer MAC --> HostName of server if connected
	private HashMap<String, String> peerHostNameMap = new HashMap<String, String>();
	
	
	//Peer server MAC addrs --> Server Peer device
	private HashMap<String, WifiP2pDevice> myServerDeviceMap = new HashMap<String, WifiP2pDevice>();
	
	//Map of my clients for each Host Addr, ChatClient
	private HashMap<String, ChatClient> mHostClients = new HashMap<String, ChatClient>();
	
	
	
	
	
	private WifiP2pInfo info;
	public WifiP2pDevice myDevice;
	ContentResolver contentResolver;
	private MediaManager mMedia;

	private Handler handlerGroupOwner = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String chatLine = msg.getData().getString("msg");
			//addChatLine(chatLine);
		}
	};
	private ChatServer chatServer;
	private ChatClient chatClient;;

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        //Start the service
	  	Intent intent = new Intent(this, SampleService.class);
	  	startService(intent);        
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment(), "PlaceHolderFragment").commit();
		}
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        initiateDiscovery();
        
		contentResolver = getContentResolver();
        mMedia = new MediaManager(contentResolver);
        new WifiAsyncTask(getApplicationContext(),contentResolver, mMedia).execute();
        
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
            case R.id.connect_dev:
            	if (isWifiP2pEnabled && (!peers.isEmpty())) {
            		List<WifiP2pDevice> list = new ArrayList<WifiP2pDevice>();
            		for (WifiP2pDevice device : peers) {
						if (device.status == WifiP2pDevice.CONNECTED) {
							list.add(device);
						}
					}
            		//TODO show this list on p2p devices
            		DeviceListFragment frag = (DeviceListFragment) getFragmentManager().findFragmentById(R.id.container);
            		frag.setPeers(list);
            	}
            case R.id.show_devices:
            	if (isWifiP2pEnabled && (!peers.isEmpty())) {
            		List<WifiP2pDevice> list = peers;
            		List<WifiP2pDevice> client = new ArrayList<WifiP2pDevice>();
            		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
						WifiP2pDevice wifiP2pDevice = (WifiP2pDevice) iterator.next();
						if (wifiP2pDevice.status != WifiP2pDevice.CONNECTED){
				            WifiP2pConfig config = new WifiP2pConfig();
				            config.deviceAddress = wifiP2pDevice.deviceAddress;
				            config.wps.setup = WpsInfo.PBC;
							connect(config);
						} else {
							Toast.makeText(WiFiDirectActivity.this, "Connected Device: " + wifiP2pDevice.deviceName , Toast.LENGTH_SHORT).show();
							client.add(wifiP2pDevice);
						}
            		}
                    DeviceListFragment fragment = new DeviceListFragment();
            		FragmentTransaction transaction = getFragmentManager().beginTransaction();
            		transaction.replace(R.id.container, fragment, "DeviceListFragment");
            		transaction.addToBackStack(null);
            		transaction.commit();
            		fragment.setPeers(peers);     
            	}            	
            case R.id.file_tx:
            		//TODO We are already connected, so can establish a chatSession
            		P2PChatFragment fragment = new P2PChatFragment();
              		FragmentTransaction transaction = getFragmentManager().beginTransaction();
              		transaction.replace(R.id.container, fragment, "P2PChatFragment");
              		transaction.addToBackStack(null);
              		transaction.commit();            		
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

	public void connect(final WifiP2pConfig config) {
		// Unique Device Address 
		String macAddr = config.deviceAddress;
		
        manager.connect(channel, config, new ActionListener() {
            @Override
            public void onSuccess() {
            	Toast.makeText(WiFiDirectActivity.this, "Connect successfull.", Toast.LENGTH_SHORT).show();
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            	String deviceAddress = config.deviceAddress;
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
        String hostaddress = info.groupOwnerAddress.getHostAddress();
        
        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        if (info.groupFormed && info.isGroupOwner) {
			//new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text)).execute();
        	startSocketServer();
        } else if (info.groupFormed) {
        	// Device is a client.
            // The other device acts as the client. In this case, we enable the
            // get file button.
        	if (mHostClients.get(hostaddress) == null) {
        		chatClient = new ChatClient(hostaddress, 8988, this);
        		mHostClients.put(hostaddress, chatClient);
        	}
        }
	}

	private void startSocketServer() {
		// Socket server is already running.
		if (chatServer == null) {
			chatServer = new ChatServer(handlerGroupOwner, myDevice);
		}
	}

	public void updateThisDevice(WifiP2pDevice device) {
		this.myDevice = device;
		System.out.println("public void updateThisDevice(WifiP2pDevice device "+ device.deviceAddress + " "+ device.deviceAddress.length());
		PlaceholderFragment frag = (PlaceholderFragment) getFragmentManager().findFragmentByTag("PlaceHolderFragment");
		if (frag != null && (device != null)) {
			frag.my_name_view.setText(device.deviceName);
			frag.my_status_view.setText(getDeviceStatus(device.status));
		}
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

	@Override
	public void onPeersAvailable(WifiP2pDeviceList peerList) {
		peers.clear();
        peers.addAll(peerList.getDeviceList());

		StringBuilder availablePeers = new StringBuilder();
		for (Iterator iterator = peers.iterator(); iterator.hasNext();) {
			WifiP2pDevice wifiP2pDevice = (WifiP2pDevice) iterator.next();
			availablePeers.append(myDevice.deviceName);
			peerMap.put(wifiP2pDevice.deviceAddress, wifiP2pDevice);
		}
        Toast.makeText(WiFiDirectActivity.this, "onPeersAvailable Peers found -  " + availablePeers + peerList.getDeviceList().size() ,Toast.LENGTH_SHORT).show();
        if (peers.size() == 0) {
            Log.d(WiFiDirectActivity.TAG, "No devices found");
            return;
        }
		
		// CAUTION NEVER CALL CONNECT in onPeersAvailable Callback to create LOOPS
		// TODO Incase a device is connected start data transfer or SYNC
	}

	private void traversePeersAndConnect(List<WifiP2pDevice> peers2, DeviceListFragment fragment) {
		for (WifiP2pDevice wifiP2pDevice : peers2) {
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = wifiP2pDevice.deviceAddress;
            config.wps.setup = WpsInfo.PBC;
            //connect(config);
		}
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

	public void p2pGroupInfo(WifiP2pGroup p2pGroupInfo) {
		Collection<WifiP2pDevice> clientList = p2pGroupInfo.getClientList();
		System.out.println("public void p2pGroupInfo(WifiP2pGroup p2pGroupInfo) " + p2pGroupInfo.toString());
		System.out.println(p2pGroupInfo.getOwner());
		
		// If this device is the group Owner. I have a new client connected.
		
		if ( (p2pGroupInfo.getOwner() != null) && 
				!myServerDeviceMap.containsKey(p2pGroupInfo.getOwner().deviceAddress)) {
			myServerDeviceMap.put(p2pGroupInfo.getOwner().deviceAddress, p2pGroupInfo.getOwner());
		}
		
		if (p2pGroupInfo.isGroupOwner()) {
			//I am the group Owner, one of these clients is my new client.
			for (Iterator iterator = clientList.iterator(); iterator.hasNext();) {
				WifiP2pDevice wifiP2pDevice = (WifiP2pDevice) iterator.next();
				myClientDeviceMap.put(wifiP2pDevice.deviceAddress, wifiP2pDevice);
			}
		} 
	}

/*	@Override
	public String deviceName(Pair<String, String> hostDeviceAddrPair) {
		System.out.println("ChatClient Server hostName " + hostDeviceAddrPair.first);
		System.out.println("ChatClient Server deviceName " + hostDeviceAddrPair.second);
		//TODO Update the Mapping with HostName addr with MAC Address of client.
		if (peerMap.containsKey(hostDeviceAddrPair.second)) {
			String hostName = peerHostNameMap.get(hostDeviceAddrPair.second);
			if (hostName == null) {
				peerHostNameMap.put(hostDeviceAddrPair.second, hostDeviceAddrPair.first);
			}
		}
		return null;
	}*/

	
}
