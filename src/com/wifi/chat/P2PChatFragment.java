package com.wifi.chat;

import android.app.Fragment;
import android.content.DialogInterface.OnClickListener;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wifi.background.ServiceManager;
import com.wifi.wifidirect.R;


public class P2PChatFragment extends Fragment {

    private TextView mStatusView;
    private Handler mUpdateHandler;
	private View mContentView;
	private ServiceManager mService;
	private WifiP2pDevice device;
	private Button mSendButtion;
    public static final String TAG = "P2PChatFragment";
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_chat, null);
        mStatusView = (TextView) mContentView.findViewById(R.id.chat_status);
        mSendButtion = (Button) mContentView.findViewById(R.id.chat_send_btn);
        mSendButtion.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		        EditText messageView = (EditText) mContentView.findViewById(R.id.chat_Input);
		        if (messageView != null) {
		            String messageString = messageView.getText().toString();
		            if (!messageString.isEmpty()) {
		                //mConnection.sendMessage(messageString);
		            	mService.sendClickEventToDevice(device.deviceAddress, messageString, mUpdateHandler);

		            }
		            messageView.setText("");
		        }
			}
		});
        mUpdateHandler = new Handler() {
        	@Override
        	public void handleMessage(Message msg) {
        		System.out.println("TRACE P2PChatFragment handleMessage");
        		String chatLine = msg.getData().getString("msg");
        		addChatLine(chatLine);
        	}
        };
       
        return mContentView;
    }

    public void clickMedia(View v) {
    	
    }
    
    
	public void clickConnect(View v) {
		
/*        NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
        if (service != null) {
            Log.d(TAG, "Connecting.");
            mConnection.connectToServer(service.getHost(),
                    service.getPort());
        } else {
            Log.d(TAG, "No service to connect to!");
        }*/
    }

    public void addChatLine(String line) {
        mStatusView.append("\n" + line);
    }
    
    @Override
    public void onStop() {
    	super.onStop();    	
    	/*        if (mNsdHelper != null) {
        mNsdHelper.stopDiscovery();
    }*/
    }
    @Override
    public void onStart() {
    	super.onStart();
    	/*        if (mNsdHelper != null) {
        mNsdHelper.discoverServices();
    }*/    	
    }

    @Override
	public void onDestroyView() {
        //mNsdHelper.tearDown();
        //mConnection.tearDown();
        super.onDestroy();
    }

	public void setServiceManager(ServiceManager mSerManager) {
		mService = mSerManager;
	}

	public void setDevice(WifiP2pDevice device) {
		this.device = device;
	}
}
