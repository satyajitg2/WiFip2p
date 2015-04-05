package com.wifi.chat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Pair;

import com.wifi.wifidirect.WiFiDirectActivity;

public class ChatClient {
	private Thread mThread;
	private PrintWriter outc;
	private BufferedReader inc;
	private Socket clientSocket;
	private static int MAC_LENGTH = 16;  
	public String socketPeerKey;
	private String hostname;
	private int mPort;
	private String myDeviceName;
	private WiFiDirectActivity activity;
	
	public ChatClient(String hostname, int mPort, WiFiDirectActivity wiFiDirectActivity) {
    	this.hostname = hostname;
    	this.mPort = mPort;
    	this.activity = wiFiDirectActivity;
    	//Once connected deviceName is unlikely to be null
    	this.myDeviceName = wiFiDirectActivity.myDevice.deviceAddress;
    	mThread = new Thread(new ServerThread());
        mThread.start();
    }
	
    class ServerThread implements Runnable {

		@Override
		public void run() {
			setUpClientCommunicationSocket();
			
		} 
	}
	public void setUpClientCommunicationSocket() {
		
		setUpClientSocket(hostname, mPort);
		performComm();
		
	}

	private void performComm() {
		//Send data over socket
		String text = "TRACE Message from Client ChatClient";
		outc.println(text);
		//Receive text from server
		try{
			//First text is the Server devicename
			String line = inc.readLine();
			if (myDeviceName == null) {
				//Setup the serverDeviceName
				myDeviceName = line;
				Pair<String, String> hostDeviceAddrPair = new Pair<String, String>(hostname, myDeviceName);
				//activity.deviceName(hostDeviceAddrPair);
			} 
			System.out.println(line);
		} catch (IOException e){
			System.out.println("Read failed");
		}		
	}

	private void setUpClientSocket(String serverHostName, int port) {
		System.out.println("setUpClientSocket" + serverHostName);
		//Create socket connection
		try{
			if (clientSocket == null) {
				clientSocket = new Socket(serverHostName, port);
			}
			if (outc == null) {
				outc = new PrintWriter((clientSocket).getOutputStream(), true);
			}
			if (inc == null ) {
				inc = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			}
			
		} catch (UnknownHostException e) {
			System.out.println("Unknown host: " + serverHostName);
		} catch  (IOException e) {
			System.out.println("No I/O");
		}
	}
	
	public interface ChatClientCallBack {
		public String deviceName(Pair<String, String> hostDeviceAddrPair);
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
