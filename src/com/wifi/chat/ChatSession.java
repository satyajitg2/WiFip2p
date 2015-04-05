package com.wifi.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatSession {
    private final String TAG = "WifiP2p ChatSession";
    
    private Socket mSocket;
	PrintWriter outc = null;
	BufferedReader inc = null;
	private String myDeviceName;
	//be:d1:d3:fd:af:4b - SIZE = 17
	private static int MAC_LENGTH = 16;  
	public String socketPeerKey;
	
	
	
	public ChatSession(Socket socket, String deviceName) {
		this.mSocket = socket;
		this.myDeviceName = deviceName;
        //mSendThread = new Thread(new SendingThread());
        //mSendThread.start();
		setUpReadWrite();
		doWrite();
	}
	
	private void setUpReadWrite() {
		if (outc == null) {
			try {
				outc = new PrintWriter((mSocket).getOutputStream(), true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (inc == null ) {
			try {
				inc = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		

	}

	private void doWrite() {
		String text = "TRACE Message from ChatSession Message from Server...";
		
		outc.println(encryptMessage(text));
		//Receive text from server
		try{
			String line = inc.readLine();
			String msg = decryptMessage(line);
			System.out.println("-----" + msg);
		} catch (IOException e){
			System.out.println("Read failed");
		}	
	}

	public String encryptMessage(String text) {
		String msg = myDeviceName + text;
		return msg;
	}

	private String decryptMessage(String line) {
		String macAddr = line.substring(0, MAC_LENGTH - 1);
		if ((socketPeerKey == null) && (!socketPeerKey.equals(macAddr))) {
			socketPeerKey = macAddr;
			// TODO Update Socket Map for ChatClient
		}
		return line.substring(MAC_LENGTH);
		
	}
}
