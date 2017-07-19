package hgo.btprint4;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by hgode on 04.04.2014.
 */
public class btPrintFile {
    public btPrintFile(Context context, Handler handler)
    {
        log("btPrintFile()");
        _context=context;
        mHandler=handler;
        //_btMAC=sBTmac;
        //_sFile=sFileName;
        mState=STATE_IDLE;
        mAdapter=BluetoothAdapter.getDefaultAdapter();
        addText("btPrintFile initialized 1");
    }

    // init a new btPrint with a context for callbacks
    // the BT MAC as string and the file to be printed
    public btPrintFile(Handler handler, String sBTmac, String sFileName)
    {
        log("btPrintFile()");
        //_context=context;
        mHandler=handler;
        _btMAC=sBTmac;
        _sFile=sFileName;
        mState=STATE_IDLE;
        mAdapter=BluetoothAdapter.getDefaultAdapter();
        addText("btPrintFile initialized 2");
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        log("start");
        addText("start()");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        setState(STATE_IDLE);   //idle
        addText("start done.");
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        log("stop");
        addText("stop()");
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        setState(STATE_DISCONNECTED);
        addText("stop() done.");
    }

    public class MyRunnable implements Runnable{
        private BluetoothSocket socket=null;
        public MyRunnable(BluetoothSocket btSocket){
            this.socket=btSocket;
        }
        public void run(){

        }
    	/*// usage
    	Thread t = new Thread(new MyRunnable(parameter));
   		t.start();
    	*/
    }

    //vars
    // Debugging
    private static final String TAG = "btPrintFile";
    private static final boolean D = true;

    private Context _context=null;
    private String	_btMAC="";
    private String _sFile="";

    private final BluetoothAdapter mAdapter;
    private BluetoothDevice mDevice=null;

    private Handler mHandler=null;
    private int mState;

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private static final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Constants that indicate the current connection state
    public static final int STATE_IDLE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    public static final int STATE_DISCONNECTED = 4;  // now connected to a remote device

    // Message types sent from the BluetoothChatService Handler
//    public static final int MESSAGE_STATE_CHANGE = 1;
//    public static final int MESSAGE_READ = 2;
//    public static final int MESSAGE_WRITE = 3;
//    public static final int MESSAGE_DEVICE_NAME = 4;
//    public static final int MESSAGE_TOAST = 5;
//
//    // Key names received from the BluetoothChatService Handler
//    public static final String DEVICE_NAME = "device_name";
//    public static final String TOAST = "toast";

//    private final String msgState="STATE";


    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        addText(msgTypes.STATE, state);
        // Give the new state to the Handler so the UI Activity can update
//        Message msg = new Message();// mHandler.obtainMessage(_Activity.MESSAGE_DEVICE_NAME);
//        Bundle bundle = new Bundle();
//        bundle.putInt("STATE", state);
//        msg.setData(bundle);
//        mHandler.sendMessage(msg);
        //mHandler.obtainMessage(BluetoothChat.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    public String printESCP()
    {
        String message = "w(          FUEL CITY\r\n" +
                "       8511 WHITESBURG DR\r\n" +
                "      HUNTSVILLE, AL 35802\r\n" +
                "         (256)585-6389\r\n\r\n" +
                " Merchant ID: 1312\r\n" +
                " Ref #: 0092\r\n\r\n" +
                "w)      Sale\r\n" +
                "w( XXXXXXXXXXX4003\r\n" +
                " AMEX       Entry Method: Swiped\r\n\r\n\r\n" +
                " Total:               $    53.22\r\n\r\n\r\n" +
                " 12/21/12               13:41:23\r\n" +
                " Inv #: 000092 Appr Code: 565815\r\n" +
                " Transaction ID: 001194600911275\r\n" +
                " Apprvd: Online   Batch#: 000035\r\n\r\n\r\n" +
                "          Cutomer Copy\r\n" +
                "           Thank You!\r\n\r\n\r\n\r\n";
        return message;
    }
    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);
        addText("connecting to "+device);
        mDevice=device;
        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            addText("already connected. Disconnecting first");
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        addText("new connect thread started");
        setState(STATE_CONNECTING);
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                addText("createInsecureRfcommSocketToServiceRecord");
                tmp = device.createInsecureRfcommSocketToServiceRecord(UUID_SPP);
                //tmp = device.createRfcommSocketToServiceRecord(UUID_SPP);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // Start the service over to restart listening mode
                btPrintFile.this.start();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (btPrintFile.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(msgTypes.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            addText("write...");
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(msgTypes.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
            addText("write done");
        }

        public void cancel() {
            addText("cancel");
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(msgTypes.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(msgTypes.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }
    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        addText("connectionLost()");
        setState(STATE_DISCONNECTED);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(msgTypes.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(msgTypes.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }
    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        addText("connectionFailed()");
        setState(STATE_DISCONNECTED);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(msgTypes.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(msgTypes.TOAST, "Toast: connectionFailed");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }
    //helpers
    void addText(String s){
        Message msg = mHandler.obtainMessage(msgTypes.MESSAGE_INFO);
        Bundle bundle = new Bundle();
        bundle.putString(msgTypes.INFO , "INFO: " + s);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }
    void addText(String msgType, int state){
        // Give the new state to the Handler so the UI Activity can update
        msgTypes type;
        Message msg;
        Bundle bundle = new Bundle();
        if(msgType.equals(msgTypes.STATE)){
            msg = mHandler.obtainMessage(msgTypes.MESSAGE_STATE_CHANGE);// mHandler.obtainMessage(_Activity.MESSAGE_DEVICE_NAME);
        }
        else if(msgType.equals(msgTypes.DEVICE_NAME)){
            msg = mHandler.obtainMessage(msgTypes.MESSAGE_DEVICE_NAME);
        }
        else if(msgType.equals(msgTypes.INFO)){
            msg = mHandler.obtainMessage(msgTypes.MESSAGE_INFO);
        }
        else if(msgType.equals(msgTypes.TOAST)){
            msg = mHandler.obtainMessage(msgTypes.MESSAGE_TOAST);
        }
        else if(msgType.equals(msgTypes.READ)){
            msg = mHandler.obtainMessage(msgTypes.MESSAGE_READ);
        }
        else if(msgType.equals(msgTypes.WRITE)){
            msg = mHandler.obtainMessage(msgTypes.MESSAGE_WRITE);
        }
        else {
            msg = new Message();
        }
        //msg = mHandler.obtainMessage(msgTypes.MESSAGE_STATE_CHANGE);// mHandler.obtainMessage(_Activity.MESSAGE_DEVICE_NAME);
        //mHandler.obtainMessage(BluetoothChat.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
        bundle.putInt(msgType, state);
        msg.setData(bundle);
        msg.arg1=state;             //we can use arg1 or the bundle to provide additional information to the message handler
        mHandler.sendMessage(msg);
        Log.i(TAG, "addText: "+msgType+", state="+state);
    }
    void log(String msg){
        if(D) Log.d(TAG, msg);
    }

}
