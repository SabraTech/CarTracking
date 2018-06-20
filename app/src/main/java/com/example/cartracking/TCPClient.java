package com.example.cartracking;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TCPClient {
    private static String TAG = "TCPClient";

    private boolean receiveThreadRunning = false;
    private long startTime = 0L;

    private Socket connectionSocket;

    // Runnable for receiving data
    private ReceiveRunnable receiveRunnable;

    // Thread to execute the Runnables
    private Thread receiveThread;

    private String serverIp;
    private int serverPort;

    private String response;

    public boolean isConnected(){
        return connectionSocket != null && connectionSocket.isConnected() && !connectionSocket.isClosed();
    }

    public void Connect(String ip, int port){
        this.serverIp = ip;
        this.serverPort = port;
        new Thread(new ConnectRunnable()).start();
    }

    public String ReceiveData(){
        startReceiving();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }

    public void Disconnect(){
        stopThread();
        try{
            connectionSocket.close();
            Log.d(TAG, "Disconnected!");
        } catch (IOException e){
            Log.e(TAG, e.getMessage());
        }
    }

    private void stopThread(){
        if(receiveThread != null){
            receiveThread.interrupt();
        }
    }

    private void startReceiving(){
        receiveRunnable = new ReceiveRunnable(connectionSocket);
        receiveThread = new Thread(receiveRunnable);
        receiveThread.start();
    }

    public class ReceiveRunnable implements Runnable{
        private Socket socket;
        private BufferedReader reader;

        public ReceiveRunnable(Socket server){
            socket = server;
            try{
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            }catch (Exception e){
                Log.e(TAG, e.getMessage());
            }
        }

        @Override
        public void run() {
            Log.d(TAG, "Receiving started");
            while(!Thread.currentThread().isInterrupted() && isConnected()){
                if(!receiveThreadRunning){
                    receiveThreadRunning = true;
                }
                startTime = System.currentTimeMillis();
                try{
                    final String data = reader.readLine();
                    long time = System.currentTimeMillis() - startTime;
                    Log.d(TAG, "Data received! Took: " + time + " ms");
                    Log.d(TAG, "Response: " + response);
                    response = data;
                    stopThread();
                } catch (Exception e){
                    // error
                    Log.e(TAG, e.getMessage());
                    Disconnect();
                }

            }
            receiveThreadRunning = false;
            Log.d(TAG, "Receiving stopped");
        }
    }

    public class ConnectRunnable implements Runnable{

        @Override
        public void run() {
            try{
                Log.d(TAG, "Connecting...");
                InetAddress serverAddress = InetAddress.getByName(serverIp);
                startTime = System.currentTimeMillis();

                connectionSocket = new Socket();

                // Start connecting to the server with 5000ms timeout
                connectionSocket.connect(new InetSocketAddress(serverAddress, serverPort), 5000);

                long time = System.currentTimeMillis() - startTime;
                Log.d(TAG, "Connected! duration: " + time + " ms");
            }catch (Exception e){
                Log.e(TAG, e.getMessage());
            }
            Log.d(TAG, "Connection thread stopped");
        }
    }
}
