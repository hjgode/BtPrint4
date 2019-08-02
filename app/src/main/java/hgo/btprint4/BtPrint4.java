package hgo.btprint4;

import android.Manifest;
import android.app.Activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class BtPrint4 extends Activity  {

    btPrintFile btPrintService = null;
    // Layout Views
//    private TextView mTitle;
    private EditText mRemoteDevice;
    Button mConnectButton;
    // Debugging
    private static final String TAG = "btprint";
    private static final boolean D = true;

    Context m_context=null;
    Activity thisActivity=null;
    private static final int REQUEST_WRITE = 112;
    private static final int REQUEST_BTADMIN = 113;
    private static final int REQUEST_BT = 114;
    private static final int REQUEST_LOCATION = 115;

    TextView mLog = null;
    Button mBtnExit = null;
    Button mBtnScan = null;
    Button mBtnBrowseForFile = null;

    Button mBtnSelectFile;
    TextView mTxtFilename;
    Button mBtnPrint;
    PrintFileXML printFileXML = null;
    ArrayList<PrintFileDetails> printFileDetailses;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Intent request codes for demo files list
    private static final int REQUEST_SELECT_DEMO_FILE = 3;
    // Intent request codes for file browser
    private static final int REQUEST_SELECT_FILE = 4;

    BluetoothAdapter mBluetoothAdapter = null;

    View _view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //show
        super.onCreate(savedInstanceState);
        setContentView(R.layout.btprint_main);

        m_context=this.getApplicationContext();
        thisActivity=BtPrint4.this;
        checkPermissions();

        // CRASHES!
//        // Set up the window layout
//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
//        setContentView(R.layout.btprint_main);
//
//        // Set up the custom title
//        mTitle = (TextView) findViewById(R.id.title_left_text);
//        mTitle.setText(R.string.app_name);
//        mTitle = (TextView) findViewById(R.id.title_right_text);

        mLog = (TextView) findViewById(R.id.log);
        mLog.setMovementMethod(ScrollingMovementMethod.getInstance());

        mRemoteDevice = (EditText) findViewById(R.id.remote_device);
        mRemoteDevice.setText(R.string.bt_default_address);
        /* does not work to hide keypad
        mRemoteDevice.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //hide keyboard on lost focus
                if(!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mRemoteDevice.getWindowToken(), 0);
                }
            }
        });
        */

        //connect button
        mConnectButton = (Button) findViewById(R.id.buttonConnect);
        mConnectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToDevice();
            }
        });

        addLog("btprint2 started");

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //exit button
        mBtnExit = (Button) findViewById(R.id.button1);
        mBtnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
                return;
            }
        });

        //scan button
        mBtnScan = (Button) findViewById(R.id.button_scan);
        mBtnScan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startDiscovery();
            }
        });

        //select demo file button
        mBtnSelectFile = (Button) findViewById(R.id.btnSelectFile);
        mBtnSelectFile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startFileList();
            }
        });

        //browse for file button
        mBtnBrowseForFile=(Button)findViewById(R.id.btn_BrowseForFile);
        mBtnBrowseForFile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startFileBrowser();
            }
        });

        mTxtFilename=(TextView)findViewById(R.id.txtFileName);
        mTxtFilename.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startFileList();
            }
        });

        mBtnPrint = (Button) findViewById(R.id.btnPrintFile);
        mBtnPrint.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                printFile();
            }
        });
        //setupComm();
        //list files
        AssetFiles assetFiles = new AssetFiles(this);

        //read file descriptions
        readPrintFileDescriptions();
    }


    @Override
    public void onStart() {
        super.onStart();
        if (D) Log.e(TAG, "++ ON START ++");

        if (mBluetoothAdapter != null) {
            // If BT is not on, request that it be enabled.
            // setupChat() will then be called during onActivityResult
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                // Otherwise, setup the comm session
            } else {
                if (btPrintService == null)
                    setupComm();
                addLog("starting print service...");//if (mChatService == null) setupChat();
            }
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (D) Log.e(TAG, "+ ON RESUME +");
        addLog("onResume");
        /*
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
              // Start the Bluetooth chat services
              mChatService.start();
            }
        }
        */
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if (D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (btPrintService != null) btPrintService.stop();
        if (D) Log.e(TAG, "--- ON DESTROY ---");
    }

    void readPrintFileDescriptions() {
        InputStream inputStream = null;
        try {
            inputStream = this.getAssets().open("demofiles.xml");
            printFileXML = new PrintFileXML(inputStream);
            //now assign the array of known print files and there details
            printFileDetailses = printFileXML.printFileDetails;
        } catch (IOException e) {
            Log.e(TAG, "Exception in readPrintFileDescriptions: " + e.getMessage());
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        }
    }

    byte[] escpQuery() {
        byte[] buf;
        String sBuf = "?{QST:HW}";
        ByteBuffer buf2;
        Charset charset = Charset.forName("UTF-8");
        buf2 = charset.encode(sBuf);
        buf2.put(0, (byte) 0x1B);
        return buf2.array();
    }

    byte[] fpQuery() {
        String sBuf = "?SYSHEALTH$\n";
        ByteBuffer buf2;
        Charset charset = Charset.forName("UTF-8");
        buf2 = charset.encode(sBuf);
        return buf2.array();
    }
    /**
     * this will print a file to the printer
     */
    void printFile() {
        String fileName = mTxtFilename.getText().toString();
//        if (!fileName.endsWith("prn")) {
//            myToast("Not a prn file!", "Error");
//            return; //does not match file pattern for a print file
//        }
        if (btPrintService.getState() != btPrintFile.STATE_CONNECTED) {
            myToast("Please connect first!", "Error");
            //PROBLEM: this Toast does not work!
            //Toast.makeText(this, "please connect first",Toast.LENGTH_LONG);
            return; //does not match file pattern for a print file
        }

        //do a query if escp
        if (fileName.startsWith("escp")) {
            byte[] bufQuery = escpQuery();
            btPrintService.write(bufQuery);
        }
        //do another query if FP
        if (fileName.startsWith("fp")) {
            byte[] bufQuery = fpQuery();
            btPrintService.write(bufQuery);
            /*
            //possible answers to ?SYSHEALTH$
            mError.put(400, "Operational" );
            mError.put(401, "Out of paper" );
            mError.put(402, "Door open" );
            */
        }

        if (mTxtFilename.length() > 0) {
            //TODO: add code
            InputStream inputStream = null;
            ByteArrayInputStream byteArrayInputStream;
            Integer totalWrite = 0;
            StringBuffer sb = new StringBuffer();
            try {
                //TODO: test if this is a storage file or a Assets resource file?
                if(fileName.startsWith("/")){
                    inputStream = new FileInputStream(fileName);
                    addLog("Using regular file: '"+fileName+"'");
                }
                else {
                    inputStream = this.getAssets().open(fileName);
                    addLog("Using demo file: '"+fileName+"'");
                }

                byte[] buf = new byte[2048];
                int readCount = 0;
                do {
                    readCount = inputStream.read(buf);
                    if (readCount > 0) {
                        totalWrite += readCount;
                        byte[] bufOut = new byte[readCount];
                        System.arraycopy(buf, 0, bufOut, 0, readCount);
                        btPrintService.write(bufOut);
                    }
                } while (readCount > 0);
                inputStream.close();
                addLog(String.format("printed " + totalWrite.toString() + " bytes"));
            } catch (IOException e) {
                Log.e(TAG, "Exception in printFile: " + e.getMessage());
                addLog("printing failed!");
                //Toast.makeText(this, "printing failed!", Toast.LENGTH_LONG);
                myToast("Printing failed","Error");
            }
        } else {
            addLog("no demo file");
            //Toast.makeText(this, "no demo file", Toast.LENGTH_LONG);
            myToast("No demo file selected!","Error");
        }
    }

    void myToast(String sInfo, String sTitle){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) findViewById(R.id.toast_layout_root));

        //assign a better icon
        try {
            ImageView imageView = (ImageView) layout.findViewById(R.id.toast_image);
            Resources resources = getResources();

            if (sTitle.startsWith("Error"))
                imageView.setImageDrawable(resources.getDrawable(R.drawable.exclamation));
            else
                imageView.setImageDrawable(resources.getDrawable(R.drawable.information));

        }catch(Exception e){
            Log.e(TAG, "can not assign toast image: "+ e.getMessage());
        }

        ((TextView) layout.findViewById(R.id.toast_text)).setText(sInfo);
        TextView txt = (TextView)layout.findViewById(R.id.toast_text);
        if(sInfo.length()>10) {
            float textSize=txt.getTextSize(); //size in pixels
            textSize=textSize/2;
            txt.setTextSize(textSize);
        }
        ((TextView) layout.findViewById(R.id.toast_title)).setText(sTitle);
        Toast toast = new Toast(getBaseContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }
    void myToast(String sInfo){
//        Message msg;
//        Bundle bundle=new Bundle();
//        msg = mHandler.obtainMessage(msgTypes.MESSAGE_TOAST);
//        bundle.putString(msgTypes.TOAST, sInfo);
//        msg.setData(bundle);
//        mHandler.sendMessage(msg);
        myToast(sInfo, "Information");
    }

    private void ensureDiscoverable() {
        if (D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    public void addLog(String s) {
        Log.d(TAG, s);
        mLog.append(s + "\r\n");
        // [http://stackoverflow.com/questions/3506696/auto-scrolling-textview-in-android-to-bring-text-into-view]
        // find the amount we need to scroll.  This works by
        // asking the TextView's internal layout for the position
        // of the final line and then subtracting the TextView's height
        int scrollAmount=0;
        try {
            scrollAmount = mLog.getLayout().getLineTop(mLog.getLineCount()) - mLog.getHeight();
        }catch(NullPointerException e){
            scrollAmount=0;
        }
        // if there is no need to scroll, scrollAmount will be <=0
        if (scrollAmount > 0)
            mLog.scrollTo(0, scrollAmount);
        else
            mLog.scrollTo(0, 0);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    boolean bDiscoveryStarted = false;
    void startDiscovery() {
        if (bDiscoveryStarted)
            return;
        bDiscoveryStarted = true;
        // Launch the DeviceListActivity to see devices and do scan
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    boolean bFileBrowserStarted=false;
    void startFileBrowser(){
        if(bFileBrowserStarted)
            return;
        bFileBrowserStarted=true;
//        Intent fileBrowserIntent = new Intent(this, FileexplorerActivity.class);
//        //Intent fileBrowserIntent = new Intent(this, FileBrowserActivity.class);
//        startActivityForResult(fileBrowserIntent, REQUEST_SELECT_DEMO_FILE);
        Intent intent1 = new Intent(this, FileChooser.class);
        startActivityForResult(intent1, REQUEST_SELECT_FILE);
    }

    boolean bFileListStared = false;
    void startFileList() {
        if (bFileListStared)
            return;
        bFileListStared = true;
        Intent fileListerIntent = new Intent(this, DemoListActivity.class);
        startActivityForResult(fileListerIntent, REQUEST_SELECT_DEMO_FILE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuScan:
                startDiscovery();
                return true;
            case R.id.mnuDiscoverable:
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            case R.id.mnuFilelist:
                startFileList();
                return true;
        }
        return false;
    }

    void printESCP() {
        if (btPrintService != null) {
            if (btPrintService.getState() == btPrintFile.STATE_CONNECTED) {
                String message = btPrintService.printESCP();
                byte[] buf = message.getBytes();
                btPrintService.write(buf);
                addLog("ESCP printed");
            }
        }
    }

    private void setupComm() {
        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        Log.d(TAG, "setupComm()");
        btPrintService = new btPrintFile(this, mHandler);
        if (btPrintService == null)
            Log.e(TAG, "btPrintService init() failed");
/*
        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
                sendMessage(message);
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
        */
    }

    // The Handler that gets information back from the btPrintService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case msgTypes.MESSAGE_STATE_CHANGE:
                    Bundle bundle = msg.getData();
                    int status = bundle.getInt("state");
                    if (D)
                        Log.i(TAG, "handleMessage: MESSAGE_STATE_CHANGE: " + msg.arg1);  //arg1 was not used! by btPrintFile
                    setConnectState(msg.arg1);
                    switch (msg.arg1) {
                        case btPrintFile.STATE_CONNECTED:
                            addLog("connected to: " + mConnectedDeviceName);
                            mConversationArrayAdapter.clear();
                            Log.i(TAG, "handleMessage: STATE_CONNECTED: " + mConnectedDeviceName);
                            break;
                        case btPrintFile.STATE_CONNECTING:
                            addLog("connecting...");
                            Log.i(TAG, "handleMessage: STATE_CONNECTING: " + mConnectedDeviceName);
                            break;
                        case btPrintFile.STATE_LISTEN:
                            addLog("connection ready");
                            Log.i(TAG, "handleMessage: STATE_LISTEN");
                            break;
                        case btPrintFile.STATE_IDLE:
                            addLog("STATE_NONE");
                            Log.i(TAG, "handleMessage: STATE_NONE: not connected");
                            break;
                        case btPrintFile.STATE_DISCONNECTED:
                            addLog("disconnected");
                            Log.i(TAG, "handleMessage: STATE_DISCONNECTED");
                            break;
                    }
                    break;
                case msgTypes.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case msgTypes.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    addLog("recv>>>" + readMessage);
                    break;
                case msgTypes.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(msgTypes.DEVICE_NAME);
                    //Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    myToast(mConnectedDeviceName, "Connected");
                    Log.i(TAG, "handleMessage: CONNECTED TO: " + msg.getData().getString(msgTypes.DEVICE_NAME));
                    //printESCP();
                    updateConnectButton(false);

                    break;
                case msgTypes.MESSAGE_TOAST:
//                    Toast toast = Toast.makeText(getApplicationContext(), msg.getData().getString(msgTypes.TOAST), Toast.LENGTH_SHORT);//.show();
//                    toast.setGravity(Gravity.CENTER,0,0);
//                    toast.show();
                    myToast(msg.getData().getString(msgTypes.TOAST));
                    Log.i(TAG, "handleMessage: TOAST: " + msg.getData().getString(msgTypes.TOAST));
                    addLog(msg.getData().getString(msgTypes.TOAST));
                    break;
                case msgTypes.MESSAGE_INFO:
                    addLog(msg.getData().getString(msgTypes.INFO));
                    //mLog.append(msg.getData().getString(msgTypes.INFO));
                    //mLog.refreshDrawableState();
                    String s = msg.getData().getString(msgTypes.INFO);
                    if (s.length() == 0)
                        s = String.format("int: %i" + msg.getData().getInt(msgTypes.INFO));
                    Log.i(TAG, "handleMessage: INFO: " + s);
                    break;
            }
        }
    };

    void connectToDevice() {
        String remote = mRemoteDevice.getText().toString();
        if (remote.length() == 0)
            return;
        if (btPrintService.getState() == btPrintFile.STATE_CONNECTED) {
            btPrintService.stop();
            setConnectState(btPrintFile.STATE_DISCONNECTED);
            return;
        }

        String sMacAddr = remote;
        if (sMacAddr.contains(":") == false && sMacAddr.length() == 12)
        {
            // If the MAC address only contains hex digits without the
            // ":" delimiter, then add ":" to the MAC address string.
            char[] cAddr = new char[17];

            for (int i=0, j=0; i < 12; i += 2)
            {
                sMacAddr.getChars(i, i+2, cAddr, j);
                j += 2;
                if (j < 17)
                {
                    cAddr[j++] = ':';
                }
            }

            sMacAddr = new String(cAddr);
        }
/*
        //BT address is either 12 hex chars or
        //6 pairs of hex chars with colons in between
        if(remote.length()==12){
            //insert colons
            newRemote=  remote.substring(0,2) + ":" + remote.substring(2,2) + ":" + remote.substring(4,2) + ":" +
                        remote.substring(6,2) + ":" + remote.substring(8,2) + ":" +remote.substring(10,2);

        }
        else
            newRemote=remote;
*/
        BluetoothDevice device;
        try {
            device = mBluetoothAdapter.getRemoteDevice(sMacAddr);
        }catch (Exception e){
            myToast("Invalid BT MAC address");
            device=null;
        }

        if (device != null) {
            addLog("connecting to " + sMacAddr);
            btPrintService.connect(device);
        } else {
            addLog("unknown remote device!");
        }
    }

    void connectToDevice(BluetoothDevice _device) {
        if (_device != null) {
            addLog("connecting to " + _device.getAddress());
            btPrintService.connect(_device);
        } else {
            addLog("unknown remote device!");
        }
    }


    //handles the scan devices and file list activity (dialog)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_SELECT_FILE:
                if (resultCode == RESULT_OK) {
                    String curFileName = data.getStringExtra("GetFileName");
                    String curPath = data.getStringExtra("GetPath");
                    if(!curPath.endsWith("/")) {
                        curPath += "/";
                    }
                    String fullName=curPath+curFileName;
                    mTxtFilename.setText(fullName);
                    addLog("Filebrowser Result_OK: '"+fullName+"'");
                }
                bFileBrowserStarted=false;
                break;
            case REQUEST_SELECT_DEMO_FILE:
                addLog("onActivityResult: requestCode==REQUEST_SELECT_DEMO_FILE");
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    addLog("resultCode==OK");
                    // Get the device MAC address
                    String file = data.getExtras().getString(DemoListActivity.EXTRA_FILE_NAME);
                    addLog("onActivityResult: got file=" + file);
                    if (printFileXML != null) {
                        PrintFileDetails details = printFileXML.getPrintFileDetails(file);
                        addLog("printfile is:\n" + details.toString());
                        //Toast.makeText(this, "You selected:\n" + details.toString(), Toast.LENGTH_LONG);
                        myToast("Demo File:\n"+details.toString());
                    }

                    mTxtFilename.setText(file);
                    //mRemoteDevice.setText(device.getAddress());
                    // Attempt to connect to the device
                }
                bFileListStared = false;
                break;
            case REQUEST_CONNECT_DEVICE:
                addLog("onActivityResult: requestCode==REQUEST_CONNECT_DEVICE");
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    addLog("resultCode==OK");
                    // Get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    addLog("onActivityResult: got device=" + address);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    mRemoteDevice.setText(device.getAddress());
                    // Attempt to connect to the device
                    addLog("onActivityResult: connecting device...");
                    //btPrintService.connect(device);
                    connectToDevice(device);
                }
                bDiscoveryStarted = false;
                break;
            case REQUEST_ENABLE_BT:
                addLog("requestCode==REQUEST_ENABLE_BT");
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "onActivityResult: resultCode==OK");
                    // Bluetooth is now enabled, so set up a chat session
                    Log.i(TAG, "onActivityResult: starting setupComm()...");
                    setupComm();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "onActivityResult: BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    void setConnectState(Integer iState) {
        switch (iState) {
            case btPrintFile.STATE_CONNECTED:
                updateConnectButton(true);
                break;
            case btPrintFile.STATE_DISCONNECTED:
                updateConnectButton(false);
                break;
            case btPrintFile.STATE_CONNECTING:
                addLog("connecting...");
                break;
            case btPrintFile.STATE_LISTEN:
                addLog("listening...");
                break;
            case btPrintFile.STATE_IDLE:
                addLog("state none");
                break;
            default:
                addLog("unknown state var " + iState.toString());
        }
    }

    void updateConnectButton(boolean bConnected){
        if(bConnected) {
            mConnectButton.setText(R.string.button_disconnect_text);
            mConnectButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.disconnectme, 0, 0, 0);
        }
        else{
            mConnectButton.setText(R.string.button_connect_text);
            mConnectButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.connectme, 0,0,0);
        }
    }

    void test(){
        boolean bFileOK=false;
//        Port.Write(new byte[]{27,119, 37}, 0, 3); //select font with 37, 0x25, '%'
//        Port.Write(new byte[]{27,72, 2}, 0, 3);   //multiply Font height by 2
        byte[] printing = new byte[]{
                27,119,37,
                27,72,2,
            32,32,13,10,32,32,13,10,32,32,13,10,32,32,13,10,32,32,13,10,32,32,13,10,32,32,13,10,32,32,13,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,77,117,100,101,105,98,32,72,97,100,100,97,100,32,38,32,83,111,110,115,32,67,111,46,13,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,13,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,-31,-88,-54,-24,-47,-55,32,-42,-47,-22,-56,-22,-55,32,-57,-63,-88,-49,-55,32,-41,-56,-88,-63,-55,13,10,32,32,50,48,49,56,45,49,49,45,48,53,32,49,49,58,49,56,58,51,52,58,-54,-88,-47,-22,-81,32,-57,-28,-31,-88,-54,-24,-47,-55,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,73,78,86,45,50,50,52,48,45,48,48,48,48,48,52,58,-47,-30,-17,32,-57,-28,-31,-88,-54,-24,-47,-55,13,10,32,32,50,48,49,56,45,49,49,45,48,53,32,49,49,58,49,56,58,51,52,58,-54,-88,-47,-22,-81,32,-57,-28,-54,-24,-43,-22,-5,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,-57,-28,-27,-44,-51,-24,-70,32,-57,-28,-54,-52,-88,-47,-22,-55,58,-57,-28,-63,-27,-22,-5,13,10,32,32,49,55,50,56,57,56,58,-47,-27,-46,32,-57,-28,-63,-27,-22,-5,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,-63,-27,-88,-14,58,-63,-26,-24,-57,-14,32,-57,-28,-63,-27,-22,-5,13,10,32,32,58,-47,46,-41,46,-67,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,58,-57,-28,-47,-30,-17,32,-57,-28,-42,-47,-22,-56,-10,32,-28,-28,-63,-27,-22,-5,13,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,-26,-40,-27,-10,32,-57,-51,-27,-49,58,-57,-45,-17,32,-57,-28,-27,-26,-49,-24,-87,13,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,58,84,101,109,112,101,114,97,116,117,114,101,13,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,58,72,117,109,105,100,105,116,121,13,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,58,83,104,111,99,107,13,10,32,32,13,10,32,32,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,13,10,32,32,32,32,-57,-28,-30,-22,-27,-55,32,-56,-63,-49,32,32,32,32,32,-26,-45,-56,-55,32,32,32,-57,-28,-50,-43,-17,32,32,32,-57,-28,-45,-63,-47,32,32,-57,-28,-29,-27,-22,-55,32,32,32,32,-57,-28,-47,-27,-46,32,32,32,32,32,32,32,32,32,32,32,-24,-43,-70,32,-57,-28,-27,-88,-49,-13,13,10,32,32,32,32,32,32,32,-57,-28,-42,-47,-22,-56,-55,32,32,-57,-28,-42,-47,-22,-56,-55,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,13,10,32,32,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,13,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,-57,-28,-27,-56,-22,-63,-88,-86,13,10,32,32,32,32,32,32,32,32,49,57,46,49,50,54,32,32,32,32,32,49,54,32,37,32,32,32,49,46,56,51,50,32,32,32,57,46,49,54,48,32,32,50,46,48,48,48,48,32,32,32,88,80,70,53,49,49,32,32,32,32,32,32,32,-56,-41,-88,-41,-88,32,32,-56,-88,-28,-31,-28,-31,-5,13,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,47,32,-17,-32,-38,32,55,56,32,32,-47,-22,-56,-29,32,-47,-88,-51,-28,-57,13,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,50,48,32,-55,-56,-51,45,67,65,82,84,79,78,13,10,32,32,32,32,32,32,32,45,45,45,45,45,45,45,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,45,45,45,45,45,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,13,10,32,32,32,32,32,32,32,32,49,57,46,49,50,54,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,50,46,48,48,48,48,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,-57,-28,-27,-52,-27,-24,-63,13,10,13,10,32,32,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,45,13,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,49,56,46,51,50,48,32,32,32,32,32,58,32,32,32,32,32,32,32,32,-10,-28,-29,-28,-57,32,-63,-24,-27,-52,-27,-28,-57,13,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,49,46,56,51,50,32,32,32,32,32,32,58,32,32,32,32,32,32,32,-86,-88,-27,-24,-43,-50,-28,-57,32,-63,-24,-27,-52,-27,13,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,48,46,48,48,48,32,32,32,32,32,32,58,32,32,32,32,32,32,32,32,-21,-24,-47,-63,-28,-57,32,-86,-88,-27,-24,-43,-50,13,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,50,46,54,51,56,32,32,32,32,32,32,58,32,32,32,32,32,32,32,32,32,-55,-56,-22,-47,-42,-28,-57,32,-55,-27,-22,-30,13,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,49,57,46,49,50,54,32,32,32,32,32,58,32,32,32,32,32,32,32,32,32,32,-55,-27,-22,-30,-28,-57,32,-10,-31,-88,-43,13,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,13,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,-44,-29,-47,-57,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,13,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,13,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,13,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,46,46,46,46,46,46,46,46,46,46,46,46,46,46,46,46,46,32,32,46,46,46,46,46,46,46,46,46,46,46,46,46,46,46,46,46,32,32,46,46,46,46,46,46,46,46,46,46,46,46,46,46,46,46,46,13,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,-57,-28,-27,-44,-47,-70,32,32,32,32,32,32,32,32,32,32,32,32,-57,-28,-63,-27,-22,-5,32,32,32,32,32,32,32,32,32,32,32,-27,-49,-22,-47,32,-57,-28,-27,-56,-22,-63,-88,-86,13,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,13,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,13,10,32,32,32,32,32,32,32,32,32,32,32,32,32,-28,-54,-31,-88,-49,-3,32,-54,-28,-70,32,-25,-48,-13,32,-57,-28,-24,-47,-30,-55,32,-22,-52,-87,32,-57,-14,32,-54,-24,-42,-63,32,-54,-51,-86,32,-51,-47,-57,-47,-55,32,49,53,32,-49,-47,-52,-55,32,-27,-58,-24,-22,-55,13,10,32,13,10,32,13,10,32,13,10,32,13,10,32,13,10,32,13,10 };
        StringBuilder sb = new StringBuilder(printing.length * 5);
        for(byte b:printing){
            sb.append(String.format("0x%02x,", b));
        }
        System.out.println( sb.toString() );

        File root = Environment.getExternalStorageDirectory();
        FileOutputStream f=null;
        try {
            f = new FileOutputStream(new File(root, "arabic_pc864.prn"));
        }catch(FileNotFoundException ex){
            Toast.makeText(m_context, "Exception in getExternalStorage: "+ex.getMessage(), Toast.LENGTH_LONG );
            Log.e(TAG, "Exception in getExternalStorage: "+ex.getMessage());
        }
        try{
            f.write(printing,0,printing.length);
            f.flush();
            f.close();
            bFileOK=true;
        }catch(IOException ex){
            Toast.makeText(m_context, "Exception in f.write(): "+ex.getMessage(), Toast.LENGTH_LONG );
            Log.e(TAG, "Exception in f.write(): "+ex.getMessage());
        }

        if(bFileOK){
            try {
                //////////////////ENCODING CONVERT
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(root + "/arabic_pc864.prn"), "cp864"));
                StringBuilder total = new StringBuilder();
                for (String line; (line = reader.readLine()) != null; ) {
                    total.append(line).append("\r\n");
                }
                //write as windows-1256
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(root+ "/arabic_pc864_cp1256.prn"), "windows-1256"));
                writer.write(total.toString());
                writer.flush();
                writer.close();
            }catch(IOException ex){

            }
        }
    }
    void checkPermissions(){
        if (    m_context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                m_context.checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                m_context.checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                m_context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            requestPermissions(new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_WRITE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission granted: WRITE_EXTERNAL_STORAGE");
                    //do here
                    test(); //just a test byte array

                } else {
                    Toast.makeText(m_context, "The app was not allowed to write in your storage", Toast.LENGTH_LONG).show();
                }
            }
            case REQUEST_BT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //do here
                    Log.i(TAG, "Permission granted: BLUETOOTH");
                } else {
                    Toast.makeText(m_context, "The app was not allowed to use Bluetooth", Toast.LENGTH_LONG).show();
                }
            }
            case REQUEST_BTADMIN: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //do here
                    Log.i(TAG, "Permission granted: BLUETOOTH_ADMIN");
                } else {
                    Toast.makeText(m_context, "The app was not allowed to manage Bluetooth", Toast.LENGTH_LONG).show();
                }
            }
            case REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //do here
                    Log.i(TAG, "Permission granted: ACCESS_COARSE_LOCATION");
                } else {
                    Toast.makeText(m_context, "The app was not allowed to use Coarse Location", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}