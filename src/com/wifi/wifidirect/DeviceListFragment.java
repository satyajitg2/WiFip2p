package com.wifi.wifidirect;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wifi.background.NetworkConnection;
import com.wifi.background.ServiceManager;
import com.wifi.chat.ChatServer;

/**
 * A ListFragment that displays available peers on discovery and requests the
 * parent activity to handle user interaction events
 */
public class DeviceListFragment extends ListFragment {

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();

	ProgressDialog progressDialog = null;
    View mContentView = null;
	private WiFiPeerListAdapter listAdapter;
	private ServiceManager mServiceManager;
	static int i = 0;

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listAdapter = new WiFiPeerListAdapter(getActivity(), R.layout.row_devices, peers);
        this.setListAdapter(listAdapter);
    }

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.device_list, null);
        return mContentView;
    }
	
	private Handler handlerDeviceList = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (isVisible()) {
				String chatLine = msg.getData().getString("msg");
                Toast.makeText(getActivity(), "Chat Message : " + chatLine ,Toast.LENGTH_SHORT).show();
				//addChatLine(chatLine);
			}
		}
	};

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		//TODO if Client is CONNECTED SHOW DETAILS else request connection
		// Check the text view to see if connected OR check device status
		// If connected start some data transfer
		WifiP2pDevice device = (WifiP2pDevice) getListAdapter().getItem(position);
		DeviceListActionListener activity = (DeviceListActionListener) getActivity();
		if ((device.status != 0) && (device.status != 1)) {
			activity.connectDevice(device.deviceAddress);
		}
		
		System.out.println("TRACE DeviceListFragment onListClick : " + device.deviceAddress);
		if (mServiceManager!= null && (device.status == 0)) {
			//Send a click event to device addr
			String msg = "TRACE Message from clickevent to device " + device.deviceAddress + " " + i++ ;
			System.out.println("TRACE Sending message to Device : " + device.deviceAddress);
			mServiceManager.sendClickEventToDevice(device.deviceAddress, msg, handlerDeviceList);
			
			//TODO Once the Device is connected, start a P2P Chat Fragment.
			((DeviceListActionListener) activity).startP2PChat(device);
		}
	}


	@Override
	public void onPause() {
		super.onPause();
	}
    /**
     * Array adapter for ListFragment that maintains WifiP2pDevice list.
     */
    public class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {

        private List<WifiP2pDevice> items = new ArrayList<WifiP2pDevice>();
		private Context context;

        public void setItems(List<WifiP2pDevice> items) {
			this.items = items;
		}

        public WiFiPeerListAdapter(Context context, int textViewResourceId,
                List<WifiP2pDevice> objects) {
            super(context, textViewResourceId, objects);
            items = objects;
            this.context = context;
        }
        

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row_devices, null);
            }
            WifiP2pDevice itemDev = items.get(position);
            if (itemDev != null) {
                TextView top = (TextView) v.findViewById(R.id.device_name);
                TextView bottom = (TextView) v.findViewById(R.id.device_details);
                ImageView image = (ImageView) v.findViewById(R.id.icon);
                if (image != null && (itemDev.status == 0)) {
                	//Set Green if CONNECTED
					image.setBackgroundColor(Color.rgb(10, 255, 50) );
                }
                if (top != null) {
                    top.setText(itemDev.deviceName);
                }
                if (bottom != null) {
                    bottom.setText(ServiceManager.getDeviceStatus(itemDev.status));
                }
            }

            return v;

        }
        
        @Override
        public WifiP2pDevice getItem(int position) {
        	return items.get(position);
        }
    }


 
     public void clearPeers() {
        peers.clear();
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    }

	public void setPeers(List<WifiP2pDevice> peers) {
		this.peers = peers;
		if (listAdapter != null) {
			listAdapter.setItems(peers);
			listAdapter.notifyDataSetChanged();
		}
	}

    /**
     * An interface-callback for the activity to listen to fragment interaction
     * events.
     */
    public interface DeviceListActionListener {
    	
        void connect(WifiP2pConfig config);
        void connectDevice(String deviceAddress);
        void disconnect();
        void startP2PChat(WifiP2pDevice device);
    }



	public void setServiceManager(ServiceManager mSerManager) {
		mServiceManager = mSerManager;
	}	
}
