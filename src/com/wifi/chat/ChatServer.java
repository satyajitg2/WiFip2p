package com.wifi.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Handler;
import android.util.Log;

public class ChatServer {
    private static final String TAG = "WiFip2p Chatserver";
	ServerSocket mServerSocket = null;
    Thread mThread = null;
    WifiP2pDevice myDevice;
    public String socketPeerKey;

    public ChatServer(Handler handler, WifiP2pDevice myDevice) {
    	this.myDevice = myDevice;
        mThread = new Thread(new ServerThread());
        mThread.start();
    }

    public void tearDown() {
        mThread.interrupt();
        try {
            mServerSocket.close();
        } catch (IOException ioe) {
            Log.e(TAG, "Error when closing server socket.");
        }
    }

    class ServerThread implements Runnable {

        @Override
        public void run() {
            try {
                mServerSocket = new ServerSocket(8988);
                while (!Thread.currentThread().isInterrupted()) {
                    Log.d(TAG, "ServerSocket Created, awaiting connection");
                    setSocket(mServerSocket.accept());
                    Log.d(TAG, "Connected.");
                    }
            } catch (IOException e) {
                Log.e(TAG, "Error creating ServerSocket: ", e);
                e.printStackTrace();
            }
        }
    }

	public void setSocket(Socket accept) {
		ChatSession session = new ChatSession(accept, myDevice.deviceName);
		
	}
}
