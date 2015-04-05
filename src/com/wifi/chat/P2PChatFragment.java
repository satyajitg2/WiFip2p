package com.wifi.chat;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.wifi.wifidirect.R;


public class P2PChatFragment extends Fragment {

    private TextView mStatusView;
    private Handler mUpdateHandler;
	private View mContentView;

    public static final String TAG = "P2PChat";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_chat, null);
        mStatusView = (TextView) mContentView.findViewById(R.id.chat_status);
        mUpdateHandler = new Handler() {
        	@Override
        	public void handleMessage(Message msg) {
        		String chatLine = msg.getData().getString("msg");
        		addChatLine(chatLine);
        	}
        };
        // Create ChatConnection with Server and Client
        
        //Create ChatClient if non owner and IP and port is known
        
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

    public void clickSend(View v) {
        EditText messageView = (EditText) mContentView.findViewById(R.id.chat_Input);
        if (messageView != null) {
            String messageString = messageView.getText().toString();
            if (!messageString.isEmpty()) {
                //mConnection.sendMessage(messageString);
            }
            messageView.setText("");
        }
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
}
