package io.github.microsix.plane;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Arrays;

import bluetooth.BluetoothService;
import bluetooth.BluetoothUtility;
import bluetooth.Constant;
import bluetooth.DeviceListActivity;

public class PlaneMainActivity extends ActionBarActivity {

    private static final String TAG = "BluetoothMain";
    private static final Boolean DEBUG = true;

    // Name of the connected device
    private String mConnectedDeviceName = null;

    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;

    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothUtility mBluetoothUtility;

    // Member object for the chat services
//    private BluetoothService mBluetoothService = null;
    
    private Context mContext;
    
    private GameView vw;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Display display = getWindowManager().getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);
        GlobalData.setHeight(size.y);
        GlobalData.setWidth(size.x);

        setContentView(R.layout.activity_plane_main);

        RelativeLayout rtLayout = (RelativeLayout) findViewById(R.id.rtLayout);
        vw = new GameView(this);
        rtLayout.addView(vw);
            
        mContext = getApplicationContext();
        mBluetoothUtility = BluetoothUtility.getInstance();
        mBluetoothAdapter = mBluetoothUtility.getBluetoothAdapter();
        
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }
    
    void initBluetooth() {
        // Initialize the BluetoothChatService to perform bluetooth connections
//        mBluetoothService = new BluetoothService(this, mHandler);
        mBluetoothUtility.initBTService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        if (DEBUG) {
            Log.e(TAG, "++ On Start ++");
        }

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (mBluetoothUtility.requestEnableBT(this, mHandler)) {
            initBluetooth();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
//        if (mBluetoothUtility !=null) {
//            mBluetoothService.stop();
//        }
        mBluetoothUtility.gc();
        if (DEBUG) {
            Log.e(TAG, "++ On Destroy ++");
        }
    }

    // The Handler that gets information back from the BluetoothService
    private final Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case Constant.MESSAGE_STATE_CHANGE:
                if (DEBUG) {
                    Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                }
                switch (msg.arg1) {
                case BluetoothService.STATE_CONNECTED:
//                    setStatus(getString(R.string.title_connected_to,
//                            mConnectedDeviceName));
                    Toast.makeText(getApplicationContext(), msg.getData()
                            .getString(Constant.TOAST), Toast.LENGTH_SHORT);
                    vw.setConnectedStatus("Connected");
                    break;
                case BluetoothService.STATE_CONNECTING:
                    vw.setConnectedStatus("Connecting");
                    break;
                case BluetoothService.STATE_LISTEN:
                case BluetoothService.STATE_NONE:
                    vw.setConnectedStatus("STATE_NONE");
                    break;
                }
                break;
            case Constant.MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String writeMessage = new String(writeBuf);
//                mConversationArrayAdapter.add("Me: " + writeMessage);
//                mMsgView.setText(writeMessage);
                break;
            case Constant.MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                int[][] getboard = new int[11][11];
                getboard = mBluetoothUtility.Byte2Int(readBuf);
                mBluetoothUtility.setExChessBoard(getboard);
                String readMessage = Arrays.deepToString((getboard));
//                String readMessage = new String(readBuf, 0, msg.arg1);
                Log.i("sy__test", "MESSAGE_READ: " + msg.arg1+",readMessage = "+readMessage);
                Toast.makeText(getApplicationContext(),
                        "receive " + readMessage,
                        Toast.LENGTH_SHORT).show();
                vw.enableOnline();
                break;
            case Constant.MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(Constant.DEVICE_NAME);
                Toast.makeText(getApplicationContext(),
                        "Connected  to " + mConnectedDeviceName,
                        Toast.LENGTH_SHORT).show();
                break;
            case Constant.MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData()
                        .getString(Constant.TOAST), Toast.LENGTH_SHORT);
                break;
            }
        }
    };

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_plane_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
        
        Intent serverIntent;
        switch (item.getItemId()) {
        case R.id.secure_connect_scan: {
            // Launch the DeviceListActivity to see devices and do scan
            serverIntent = new Intent(this,DeviceListActivity.class);
            startActivityForResult(serverIntent, Constant.REQUEST_CONNECT_DEVICE_SECURE);
            return true;
        }
        case R.id.insecure_connect_scan: {
            // Launch the DeviceListActivity to see devices and do scan
            serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, Constant.REQUEST_CONNECT_DEVICE_INSECURE);
            return true;
        }
        case R.id.discoverable: {
            // Ensure this device is discoverable by others
//            if (mBluetoothAdapter == null) {
//                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//            }
            mBluetoothUtility.ensureDiscoverable(mContext);
//            ensureDiscoverable();
            return true;
        }
      //noinspection SimplifiableIfStatement
        case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.d(TAG, "onActivityResult " + requestCode);
        }
        switch (requestCode) {
        case Constant.REQUEST_CONNECT_DEVICE_SECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                mBluetoothUtility.connectDevice(data, true);
            }
            break;
        case Constant.REQUEST_CONNECT_DEVICE_INSECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                mBluetoothUtility.connectDevice(data, false);
            }
            break;
        case Constant.REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                initBluetooth();
            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving,
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

//    private void connectDevice(Intent data, boolean secure) {
//        // Get the device MAC address
//        String address = data.getExtras().getString(
//                DeviceListActivity.EXTRA_DEVICE_ADDRESS);
//        // Get the BluetoothDevice object
//        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
//        // Attempt to connect to the device
//        mBluetoothService.connect(device, secure);
//    }
    
//    public void ensureDiscoverable() {
//        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
//            Intent discoverableIntent = new Intent(
//                    BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//            discoverableIntent.putExtra(
//                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//            startActivity(discoverableIntent);
//        }
//    }
}
