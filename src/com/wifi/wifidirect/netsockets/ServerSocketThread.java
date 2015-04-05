package com.wifi.wifidirect.netsockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;



public class ServerSocketThread {
	private ServerSocket server;
	private Socket client;
	private BufferedReader in;
	private PrintWriter out;
	private Socket clientSocket;

    ServerSocket mServerSocket = null;
    Thread mThread = null;
	private Handler mUpdateHandler;

    public ServerSocketThread(Handler handler) {
    	mUpdateHandler = handler;
        mThread = new Thread(new ServerThread());
        mThread.start();
    }

    class ServerThread implements Runnable {

		@Override
		public void run() {
			setUpServerCommunicationSocket();
			
		} 
	}
	
	public void setUpServerCommunicationSocket() {
		// TODO We are connected to start sending and receiving data
		if (server == null) {
			  try{
				    server = new ServerSocket(4321); 
				  } catch (IOException e) {
				    System.out.println("Could not listen on port 4321");
				  }
			if ( client == null ) {
				try{
				    client = server.accept();
				  } catch (IOException e) {
				    System.out.println("Accept failed: 4321");
				  }
			}
		}
		try{
			if (in == null) {
				in = new BufferedReader(new InputStreamReader(client.getInputStream()));  
			}
			if (out == null) {
				out = new PrintWriter(client.getOutputStream(), true);

			}
		}	   
		catch (IOException e) {
			System.out.println("Read failed");
		}	
		readWriteToSocket();
	}

	private void acceptSocket() {
		  try{
			    server = new ServerSocket(4321); 
			  } catch (IOException e) {
			    System.out.println("Could not listen on port 4321");
			  }
	}

	public void setupSocket(){
		try{
		    client = server.accept();
		  } catch (IOException e) {
		    System.out.println("Accept failed: 4321");
		  }
	}
	
	public void readWriteToSocket() {
	        try{
	          String line = in.readLine();
	          //Toast.makeText(WifiActivity.this, "Listening to Socket ------------------: " + line,Toast.LENGTH_SHORT).show();
	          updateMessage(line);
	          //Send data back to client 
	          
	          out.println("--------Message from server "+ line);
	        } catch (IOException e) {
	          System.out.println("Read failed");
	        }
	}

	public synchronized void updateMessage(String line) {
        Bundle messageBundle = new Bundle();
        messageBundle.putString("msg", line);

        Message message = new Message();
        message.setData(messageBundle);
		
		mUpdateHandler.sendMessage(message);
		
	}
	private void setUpReadWrite() {
		try{
			if (in == null) {
				in = new BufferedReader(new InputStreamReader(client.getInputStream()));  
			}
			if (out == null) {
				out = new PrintWriter(client.getOutputStream(), true);

			}
		}	   
		catch (IOException e) {
			System.out.println("Read failed");
		}		
	}

}
