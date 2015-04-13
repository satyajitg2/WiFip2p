package com.wifi.background;

import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.os.Handler;

import com.wifi.chat.ChatClient;
import com.wifi.chat.ChatClient.ChatClientListener;
import com.wifi.chat.ChatServer;
import com.wifi.chat.ChatSession;

/**
 * @author satyajit
 * This Class is a component of the Service. Service delegates all network management function here.
 */
public class NetworkConnection implements ChatClientListener{
	private ChatServer chatServer;
	private String serverDevice;

	HashMap<String, ChatClient> clientMap = new HashMap<String, ChatClient>();
	private HashMap<String, ChatClient> clientMacMap = new HashMap<String, ChatClient>();
	
	public NetworkConnection() {

	}
	public void manageHostClient(String hostaddress, int port, String mac) {
		ChatClient chatClient = new ChatClient(hostaddress, 8988, this, mac);
		activateSocket(hostaddress, chatClient);
	}
	private void activateSocket(String host, ChatClient chatClient) {
		// CHICKEN and EGG problem with Strict Ordering
		clientMap.put(host, chatClient);
		chatClient.initialise();
	}

	@Override
	public boolean registerPeerClient(String hostName, String socketPeerKey,
			Socket socketKey) {
		System.out.println("TRACE registerPeerClient host: " + hostName + " socket mac:" + socketPeerKey+ " socket: " + socketKey.toString());
		clientMap.get(hostName);
		clientMacMap.put(socketPeerKey, clientMap.get(hostName));
		return true;
	}


	public boolean manageHostServer(String deviceAddress) {
		// TODO One device can have only one server. Create this as a singleton.
		if (chatServer == null) {
			chatServer = new ChatServer(deviceAddress);
			this.serverDevice = deviceAddress;
			return true;
		}
		return true;
	}
	
	public void resetNetwork() {
		// TODO Clean up ChatServer Object and ChatClient.
		chatServer.tearDown();
		cleanUpClient();
	}

	public void cleanUpClient() {
		Set<String> set = clientMacMap.keySet();
		for (Iterator iterator = set.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			ChatClient cli = clientMacMap.get(string);
			cli.tearDown();
		}
		clientMacMap.clear();
		clientMap.clear();
	}

	
	public HashMap<String, ChatSession> getChatServerSession() {
		return chatServer.getDeviceChatSessionMap();
		
	}
	public HashMap<String, ChatClient> getClientMap() {
		return clientMap;
	}
	public void setClientMap(HashMap<String, ChatClient> clientMap) {
		this.clientMap = clientMap;
	}
	
	public ChatServer getChatServer() {
		return chatServer;
	}
	public void setChatServer(ChatServer chatServer) {
		this.chatServer = chatServer;
	}

	public HashMap<String, ChatClient> getClientMacMap() {
		System.out.println("TRACE Network ClientMacMap size: " + clientMacMap.size());
		printMap(clientMacMap);
		return clientMacMap;
	}



	private static void printMap(HashMap<String, ChatClient> map) {
		if (map != null) {
			Set<String> keyset = map.keySet();
			if (keyset!=null) {
				for (Iterator iterator = keyset.iterator(); iterator.hasNext();) {
					String string = (String) iterator.next();
					System.out.println("TRACE print MAP: " + string + " val:  " + map.get(string));
					
				}
			}
		}
	}

	public void sendClickEventToDevice(String deviceAddress, String msg, Handler handler) {
		System.out.println("TRACE NetworkConnection " + clientMacMap.containsKey(deviceAddress));
		if (!clientMacMap.containsKey(deviceAddress)) {
			chatServer.handleClickEvent(deviceAddress, msg, handler);
		} else {
			ChatClient client = clientMacMap.get(deviceAddress);
			client.registerHandler(handler);
			System.out.println("TRACE NetworkConnection performComm and then SEND MESSAGE ON CLICK");
			client.sendMessage(msg);
		}
	}

}
