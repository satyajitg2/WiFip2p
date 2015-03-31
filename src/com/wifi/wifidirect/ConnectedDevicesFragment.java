package com.wifi.wifidirect;

import java.util.ArrayList;
import java.util.List;

import android.app.ListFragment;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ConnectedDevicesFragment extends ListFragment {
    View mContentView = null;
    private WifiP2pDevice device;
	private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        DeviceListAdapter listAdapter = new DeviceListAdapter(getActivity(), R.layout.row_devices, peers );
        this.setListAdapter(listAdapter);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.device_list, null);
        return mContentView;
    }
    
    public class DeviceListAdapter extends ArrayAdapter<WifiP2pDevice> {

        private List<WifiP2pDevice> items = new ArrayList<WifiP2pDevice>();
 
    	private Context context;

        /**
         * @param context
         * @param textViewResourceId
         * @param objects
         */
        public DeviceListAdapter(Context context, int textViewResourceId, List<WifiP2pDevice> objects) {
            super(context, textViewResourceId, objects);
            this.context = context;
            items = objects;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) context.getSystemService(
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
                    bottom.setText(getDeviceStatus(device.status));
                }
            }

            return v;

        }

        public String getDeviceStatus(int deviceStatus) {
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

    }


    public List<WifiP2pDevice> getPeers() {
		return peers;
	}
	public void setPeers(List<WifiP2pDevice> peers) {
		this.peers = peers;
	}
	public void notifyPeers() {
		((DeviceListAdapter) getListAdapter()).notifyDataSetChanged();
	}
}
