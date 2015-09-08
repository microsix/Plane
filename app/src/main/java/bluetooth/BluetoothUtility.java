package bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import io.github.microsix.plane.R;

public class BluetoothUtility {
    
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothService mBluetoothService = null;
    
    public int[][] exChessBoard;
    
    private static class BTUtilHolder {
        private static final BluetoothUtility INSTANCE = new BluetoothUtility();
    }
    
    public int[][] getExChessBoard() {
        return exChessBoard;
    }
    public void setExChessBoard(int[][] exChessBoard) {
        this.exChessBoard = exChessBoard;
        exChessBoard = new int[11][11];
    }
    
    public BluetoothUtility() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    
    public static final BluetoothUtility getInstance() {
        return BTUtilHolder.INSTANCE;
    }
    
    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }
    
    public boolean initBTService(Context context,Handler mHandler) {
     // Initialize the BluetoothChatService to perform bluetooth connections
        mBluetoothService = new BluetoothService(context, mHandler);
        return true;
    }
    
    public boolean requestEnableBT(Activity activity, Handler mHandler) {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableIntent, Constant.REQUEST_ENABLE_BT);
            return false;
            // Otherwise, setup the chat session
        } else if (mBluetoothService == null) {
            // TODO apply layout
            initBTService(activity.getApplicationContext(), mHandler);
        }
        return true;
    }
    
    public void sendMessage(Context context, String message) {
        // Check that we're actually connected before trying anything
        if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(context, R.string.not_connected, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        /* Check that there's actually something to send */
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mBluetoothService.write(send);
            // Reset out string buffer to zero and clear the edit text field
//            mOutStringBuffer.setLength(0);
//            Log.i("sy__test", "mOutStringBuffer = " + mOutStringBuffer);
//            mOutEditText.setText(mOutStringBuffer);
        }
    }
    public boolean sendMessage(Context context, int[][] message) {
        // Check that we're actually connected before trying anything
        if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(context, R.string.not_connected, Toast.LENGTH_SHORT)
            .show();
            return false;
        }
        
        byte[] send = int2byte(message);
        mBluetoothService.write(send);
        
        return true;
    }
    
    /**
     * Makes this device discoverable.
     */
    public void ensureDiscoverable(Context mContext) {
        if (mBluetoothAdapter!=null && mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            discoverableIntent.putExtra(
                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            mContext.startActivity(discoverableIntent);
        }
    }
    
    public void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras().getString(
                DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mBluetoothService.connect(device, secure);
    }
    
    public void gc() {
        if (mBluetoothService !=null) {
            mBluetoothService.stop();
        }
    }
    
    public static byte[] int2byte(int[][] data) {
        Log.i("sy__test", "int2byte : "+data.length);
        ByteBuffer byteBuffer = ByteBuffer.allocate((data.length) * (data.length));
        byte[] target = new byte[data.length * 4];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                byteBuffer.put((byte) data[i][j]);
            }
        }
        target = byteBuffer.array();
        return target;
    }
    
    public static int[][] Byte2Int(byte[] data){    
        IntBuffer intBuffer = IntBuffer.allocate(11);
        int target[][] = new int[intBuffer.limit()][intBuffer.limit()]; 
        Log.i("sy__test", "intBuffer.limit() = "+intBuffer.limit());
        int j=0;
        for (int i = 0; i < 121; i++) {
            Log.i("sy__test", "data.length = "+data.length+", data[i]= "+data[i]);
                intBuffer.put(data[i]);
//                intBuffer = ByteBuffer.wrap(data).asIntBuffer();
                if ((i+1)%11 == 0) {
                    target[j] = intBuffer.array().clone();
                    j++;
                    intBuffer.clear();
                }
        }
        return target;
    }
}
