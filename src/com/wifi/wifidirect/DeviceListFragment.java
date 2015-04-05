package com.wifi.wifidirect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * A ListFragment that displays available peers on discovery and requests the
 * parent activity to handle user interaction events
 */
public class DeviceListFragment extends ListFragment {

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();

	ProgressDialog progressDialog = null;
    View mContentView = null;
    private WifiP2pDevice device;
	private WiFiPeerListAdapter listAdapter;
 

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
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		//TODO if Client is CONNECTED SHOW DETAILS else request connection
		// Check the text view to see if connected OR check device status
		
		// If connected start some data transfer

	}

    /**
     * Array adapter for ListFragment that maintains WifiP2pDevice list.
     */
    public class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {

        private List<WifiP2pDevice> items = new ArrayList<WifiP2pDevice>();

        public void setItems(List<WifiP2pDevice> items) {
			this.items = items;
		}

        public WiFiPeerListAdapter(Context context, int textViewResourceId,
                List<WifiP2pDevice> objects) {
            super(context, textViewResourceId, objects);
            items = objects;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row_devices, null);
            }
            WifiP2pDevice device = items.get(position);
            if (device != null) {
                TextView top = (TextView) v.findViewById(R.id.device_name);
                TextView bottom = (TextView) v.findViewById(R.id.device_details);
                if (top != null) {
                    top.setText(device.deviceName);
                }
                if (bottom != null) {
                    bottom.setText(WiFiDirectActivity.getDeviceStatus(device.status));
                }
            }

            return v;

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
	
    /**
     * An interface-callback for the activity to listen to fragment interaction
     * events.
     */
    public interface DeviceListActionListener {
    	
        void connect(WifiP2pConfig config);

        void disconnect();
    }	
}
