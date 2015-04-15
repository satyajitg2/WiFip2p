package com.wifi.chat;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Handler;
import android.util.Log;

import com.wifi.chat.ChatSession.ChatSessionListener;

public class ChatServer implements ChatSessionListener {
    private static final String TAG = "TRACE WiFip2p Chatserver";
	ServerSocket mServerSocket = null;
    Thread mThread = null;
	private ChatSession session;
	private String deviceMACAddr;
    
    // Map with socketPeerKey(Peer MAC ID) and ChatSession handling the Peer client
    private HashMap<String, ChatSession> deviceChatSessionMap = new HashMap<String, ChatSession>();
	private HashMap<Socket, ChatSession> socketSesssionMap = new HashMap<Socket, ChatSession>();


    public ChatServer(String deviceAddress) {
    	System.out.println("TRACE ChatServer Constructor");
    	this.deviceMACAddr = deviceAddress;
        mThread = new Thread(new ServerThread());
        mThread.start();
    }

	public void tearDown() {
        mThread.interrupt();
        try {
        	mServerSocket.close();
        	Set<Socket> keySet = socketSesssionMap.keySet();
        	for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
				Socket socket = (Socket) iterator.next();
				ChatSession value = socketSesssionMap.get(socket);
				value.tearDown();
			}
        } catch (IOException ioe) {
            Log.e(TAG, "Error when closing server socket.");
        }
    }

    class ServerThread implements Runnable {

        @Override
        public void run() {
            try {
            	try {
                    mServerSocket = new ServerSocket(8988);
                    while (!Thread.currentThread().isInterrupted()) {
                        Log.d(TAG, "ServerSocket Created, awaiting connection");
                        setSocket(mServerSocket.accept(), deviceMACAddr);
                        Log.d(TAG, "TRACE Connected.");
                        }
            	} catch (BindException e) {
            		//TODO Handle if address is already in use.
            		e.printStackTrace();
            	}   
            } catch (IOException e) {
                Log.e(TAG, "Error creating ServerSocket: ", e);
                e.printStackTrace();
            }
        }
    }

	public void setSocket(Socket accept, String serverMacAddr) {
		System.out.println("TRACE setSocket for ChatSession " + accept.toString());
		session = new ChatSession(this, accept, serverMacAddr);
		// THIS MAP SHould be updated before we create instance for CHATSESSION as that calls registerPeerSession
		// Where the key is never found.
		// CHICKEN first or the EGG
		activateSocket(accept);

	}

	private void activateSocket(Socket accept) {
		socketSesssionMap.put(accept, session);
		session.initialise();		
	}

	@Override
	public boolean registerPeerSession(String socketPeerKey, Socket socketKey) {
		// TODO Register a client Peer Mac with socket.
		System.out.println("TRACE ChatServer registerPeerSession Peer MAC: "+ socketPeerKey + " earlier setSocket for ChatSession" + socketKey);
		if (!socketSesssionMap.containsKey(socketKey)) {
			System.out.println("TRACE ChatServer KEY NOT FOUND in socketSessionMap: " + socketKey);
			return false;
		}
		ChatSession value = socketSesssionMap.get(socketKey);
		deviceChatSessionMap.put(socketPeerKey, value);
		printMap(deviceChatSessionMap);
		return true;
	}

	private void printMap(HashMap<String, ChatSession> deviceChatSessionMap2) {
		System.out.println("TRACE printMap " + deviceChatSessionMap2);
		Set<String> keyset = deviceChatSessionMap2.keySet();
		for (Iterator iterator = keyset.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			if (deviceChatSessionMap2.get(string)!= null) {
				System.out.println("TRACE ChatServer MAP DATA " + string + " -> Session:  " + deviceChatSessionMap2.get(string).toString());
			}
		}
	}

	public HashMap<String, ChatSession> getDeviceChatSessionMap() {
		return deviceChatSessionMap;
	}
	
	public void setDeviceChatSessionMap(
			HashMap<String, ChatSession> deviceChatSessionMap) {
		this.deviceChatSessionMap = deviceChatSessionMap;
	}

	public void handleClickEvent(String deviceAddress, String msg, Handler handler) {
		System.out.println("TRACE ChatServer handleClickEvent "+ deviceChatSessionMap.containsKey(deviceAddress));
		
		if (deviceChatSessionMap.containsKey(deviceAddress)) {
			ChatSession sess = deviceChatSessionMap.get(deviceAddress);
			if (sess!= null) {
				sess.registerHandler(handler);
				System.out.println("TRACE ChatServer SEND MESSAGE ON CLICK");
				sess.sendMessage(msg);
			}
		}
	}
}
