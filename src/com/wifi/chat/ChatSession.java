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

public class ChatSession {
    private final String TAG = "TRACE ChatSession";
    private Socket mSocket;
	private String myDeviceName;
	//be:d1:d3:fd:af:4b -- Satyajit
	//da:3c:69:04:c2:e9 -- Jaya
	private static int MAC_LENGTH = 18;  
	public String socketPeerKey;
	private ChatServer chatServer;
	private Handler mUpdateHandler;
	
	private Thread mSendThread;
	private Thread mRecThread;
	
	public interface ChatSessionListener {
		public boolean registerPeerSession(String socketPeerKey, Socket socketKey);
	}
	
	
	public ChatSession(ChatServer chatServer, Socket socket, String serverMacAddr) {
		this.mSocket = socket;
		this.chatServer = chatServer;
		myDeviceName = serverMacAddr;
	}

	public void initialise() {
		Log.d(TAG, " initialise ChatClient");
        mSendThread = new Thread(new SendingThread());
        mSendThread.start();		
        mRecThread = new Thread(new ReceivingThread());
        mRecThread.start();

	}
	
    public void tearDown() {
    	mSendThread.interrupt();
    	mRecThread.interrupt();
        try {
            getSocket().close();
        } catch (IOException ioe) {
            Log.e(TAG, "Error when closing server socket.");
        }
    }	
	
    class SendingThread implements Runnable {

        BlockingQueue<String> mMessageQueue;
        private int QUEUE_CAPACITY = 10;
		private Thread mRecThread;

        public SendingThread() {
            mMessageQueue = new ArrayBlockingQueue<String>(QUEUE_CAPACITY);
            mMessageQueue.add("TRACE CHATSERVER SendingThread Constructor Message Queue MSG1.");
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String msg = mMessageQueue.take();
                    sendMessage(msg);
                } catch (InterruptedException ie) {
                    Log.d(TAG, "Message sending loop interrupted, exiting");
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
                        Log.d(TAG, "Read from the stream: " + msg);
                        updateMessages(msg, false);
                    } else {
                        Log.d(TAG, "The nulls! The nulls!");
                        break;
                    }
                }
                input.close();

            } catch (IOException e) {
                Log.e(TAG, "Server loop error: ", e);
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
    
	public String encryptMessage(String text) {
		if (myDeviceName == null) {
			System.out.println("TRACE ChatSession ********************* Device Name is NULL ****************************");
		}
		String msg = myDeviceName + text;
		System.out.println("TRACE ChatSession encryptMessage");
		return msg;
	}

	private String decryptMessage(String line) {
		try {
			String macAddr = line.substring(0, MAC_LENGTH - 1);
			System.out.println("TRACE ChatSession decryptMessage macAddr: '"+macAddr+"'");
			if (!macAddr.isEmpty()) {
				System.out.println("TRACE ChatSession decryptMessage " + macAddr );
				socketPeerKey = macAddr;
				chatServer.registerPeerSession(macAddr, mSocket);
			}
			return line.substring(MAC_LENGTH - 1);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return line;
	}

	public void registerHandler(Handler handler) {
		mUpdateHandler = handler;
	}
}
