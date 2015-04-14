package com.wifi.chat;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wifi.background.NetworkConnection;

public class ChatClient {
    private final String CLIENT_TAG = "TRACE ChatClient";
    private final String TAG = "TRACE ChatClient";	
	private String hostname;
    private int PORT;
    private Socket mSocket;
    private Thread mSendThread;
    private Thread mRecThread;
	private NetworkConnection networkConnection;
	private String myDeviceName;
	private Handler mUpdateHandler;
	private PrintWriter outc;
	private BufferedReader inc;
	private static int MAC_LENGTH = 18;  
	public String socketPeerKey;
	private boolean isRegistered = false;
	
	public ChatClient(String hostServername, int mPort, NetworkConnection mNet, String mac) {
		System.out.println("TRACE ChatClient Constructor" + hostServername + mPort);
    	hostname = hostServername;
    	PORT = mPort;
    	networkConnection = mNet;
    	myDeviceName = mac;
    }
	
	public void initialise() {
		Log.d(TAG, " initialise ChatClient");
        mSendThread = new Thread(new SendingThread());
        mSendThread.start();		
	}
	
	public void sendMessage(String msg) {
		System.out.println("TRACE IN ChatClient sendMessage");

        try {
        	Socket socket = getSocket();
        	if (socket == null) {
        		Log.d(TAG, " sendMessage Socket is null, wtf?");
        	} else if (socket.getOutputStream() == null ){
        		Log.d(TAG, " sendMessaage Socket output stream is null, wtf?");
        	}
        	
            PrintWriter out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(getSocket().getOutputStream())), true);
            //out.println(msg);
            out.println(encryptMessage(msg));
            out.flush();
        	
        } catch (UnknownHostException e) {
            Log.d(TAG, "Unknown Host", e);
        } catch (IOException e) {
            Log.d(TAG, "I/O Exception", e);
        } catch (Exception e) {
            Log.d(TAG, "Error3", e);
        }
	}

	private Socket getSocket() {
        return mSocket;
    }

    private synchronized void setSocket(Socket socket) {
        Log.d(TAG, "setSocket being called.");
        if (socket == null) {
            Log.d(TAG, "Setting a null socket.");
        }
        if (mSocket != null) {
            if (mSocket.isConnected()) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    // TODO(satyajitsingh): Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        mSocket = socket;
    }

    class SendingThread implements Runnable {

        BlockingQueue<String> mMessageQueue;
        private int QUEUE_CAPACITY = 10;

        public SendingThread() {
            mMessageQueue = new ArrayBlockingQueue<String>(QUEUE_CAPACITY);
            mMessageQueue.add("TRACE SendingThread Constructor Message Queue MSG1.");
        }

        @Override
        public void run() {
            try {
                if (getSocket() == null) {
                    setSocket(new Socket(hostname, PORT));
                    Log.d(CLIENT_TAG, "Client-side socket initialized.");

                } else {
                    Log.d(CLIENT_TAG, "Socket already initialized. skipping!");
                }

                mRecThread = new Thread(new ReceivingThread());
                mRecThread.start();

            } catch (UnknownHostException e) {
                Log.d(CLIENT_TAG, "Initializing socket failed, UHE", e);
            } catch (IOException e) {
                Log.d(CLIENT_TAG, "Initializing socket failed, IOE.", e);
            }

            while (true) {
                try {
                    String msg = mMessageQueue.take();
                    sendMessage(msg);
                } catch (InterruptedException ie) {
                    Log.d(CLIENT_TAG, "Message sending loop interrupted, exiting");
                }
            }
        }
    }

    class ReceivingThread implements Runnable {

        @Override
        public void run() {

            BufferedReader input;
            try {
                input = new BufferedReader(new InputStreamReader(
                        mSocket.getInputStream()));
                while (!Thread.currentThread().isInterrupted()) {

                    String messageStr = null;
                    String msg = null;
                    try {
                    	messageStr = input.readLine();
                    	msg = decryptMessage(messageStr);
                    } catch (SocketException e) {
                    	e.printStackTrace();
                    }
                    if (msg != null) {
                        Log.d(CLIENT_TAG, "Read from the stream: " + msg);
                        updateMessages(msg, false);
                    } else {
                        Log.d(CLIENT_TAG, "The nulls! The nulls!");
                        break;
                    }
                }
                input.close();

            } catch (IOException e) {
                Log.e(CLIENT_TAG, "Server loop error: ", e);
            }
        }
    }
    
    public synchronized void updateMessages(String msg, boolean local) {
        Log.e(TAG, "Updating message: " + msg);

        if (local) {
            msg = "me: " + msg;
        } else {
            msg = "them: " + msg;
        }

        Bundle messageBundle = new Bundle();
        messageBundle.putString("msg", msg);

        Message message = new Message();
        message.setData(messageBundle);
        if (mUpdateHandler != null) {
        	mUpdateHandler.sendMessage(message);
        }
        String response = "AutoResponse back from ChatClient" + msg; 
    }

	public String encryptMessage(String text) {
		System.out.println("TRACE ChatClient encryptMessage");
		String msg = myDeviceName + text;
		return msg;
	}

	private String decryptMessage(String line) {
		try {
			String macAddr = line.substring(0, MAC_LENGTH - 1);
			//TODO Enable Header Marker for Error handling and Contingency
			//TODO Enable Reverse checking with peer device addr
			System.out.println("TRACE ChatClient decryptMessage macAddr: '"+macAddr+"'");

			if ((!isRegistered) && (!macAddr.isEmpty()) ) {
				socketPeerKey = macAddr;
				// TODO Update Socket Map for ChatClient
				System.out.println("TRACE setup ChatClient MAPs");
				isRegistered = networkConnection.registerPeerClient(hostname, socketPeerKey, mSocket);
			}
			return line.substring(MAC_LENGTH - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return line;
		
	}	
	
    

    public void tearDown() {
    	mSendThread.interrupt();
    	mRecThread.interrupt();
        try {
            getSocket().close();
        } catch (IOException ioe) {
            Log.e(CLIENT_TAG, "Error when closing server socket.");
        }
    }	
    
	private void cleanUpDescriptors() {
		if (outc!= null ) {
			outc.close();
		}
		
		try {
			if (inc != null ) {
				inc.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}		

    public interface ChatClientListener {
        boolean registerPeerClient(String hostName, String socketPeerKey, Socket socketKey);
    }
	public void registerHandler(Handler handler) {
		mUpdateHandler = handler;
	}
}
