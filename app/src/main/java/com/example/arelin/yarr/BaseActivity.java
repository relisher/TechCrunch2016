package com.example.arelin.yarr;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import com.harman.everestelite.Bluetooth;
import com.harman.everestelite.BluetoothListener;
import com.harman.everestelite.HeadPhoneCtrl;


//import com.harman.everestelite.Log;


public class BaseActivity extends AppCompatActivity implements BluetoothListener
//        LightX.Delegate
//        Log.Delegate
{

    public static final String JBL_HEADSET_MAC_ADDRESS = "com.jbl.headset.mac_address";
    public static final String JBL_HEADSET_NAME = "com.jbl.headset.name";

    public static String DEVICENAME = "DEVICENAME";
    //public LightX mLightX;
    public boolean isConnected = false;
    boolean isNeedtoShowDashboard;
    boolean disconnected;
    // Bluetooth Delegate
    public boolean donSendCallback = true;
    private Bluetooth mBluetooth;
    public BluetoothDevice mBluetoothDevice;
    public static HeadPhoneCtrl headphCtrl;


//    public AppLightXDelegate getAppLightXDelegate() {
//        return appLightXDelegate;
//    }

    // Members and methods to support hardware
    //private AppLightXDelegate appLightXDelegate;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {


            case Bluetooth.REQUEST_ENABLE_BT: {
                if (resultCode == RESULT_CANCELED) {
                    showExitDialog("Unable to enable Bluetooth.");
                } else {
                    mBluetooth.discoverBluetoothDevices();
                }
            }
            break;
        }
    }

//    ConnectionChangeReceiver connectionChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        LightX.sEnablePacketDumps = true;
        //Log.sLogDelegate = this;


    }

    /**
     * <p>Initialize or reset library. Its used when app start or headphone timeout a command from app<p/>
     */
    private void initializeOResetLibrary() {
//        if (mLightX != null)
//            mLightX.close();
        if (mBluetooth != null)
            mBluetooth.close();
        mBluetoothDevice = null;
        try {
            Log.e("Selection screen", "LightXB as activity oncreate starts");
            mBluetooth = new Bluetooth(this, this, true);
            mBluetooth.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Members and methods to support hardware

    public synchronized void connect(BluetoothDevice bluetoothDevice) {
        try {
            if (mBluetooth == null || bluetoothDevice == null) {
                Log.d(TAG, "Connection and device both are null");
                return;
            }
            if (isConnected())
                return;


            try {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //Log.d(TAG, bluetoothDevice.getName() + " connecting...");
                mBluetooth.connect(bluetoothDevice);
                mBluetoothDevice = bluetoothDevice;
            } catch (Exception e) {
                Log.e(TAG, mBluetooth.deviceName(bluetoothDevice) + " connect() failed: " + e.getLocalizedMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean mIsConnectedPhysically;

    // advisory
    public synchronized boolean isConnected() {
        return mIsConnectedPhysically;
    }

    public synchronized boolean isConnectedLogically() {
        return mBluetoothDevice != null;

    }



    // Members and methods to support hardware

    private synchronized boolean shouldConnectToBluetoothDevice(BluetoothDevice bluetoothDevice) {
        String deviceMACAddress;
        String deviceName;
        String macAddressOfSavedJBLHeadset;
        boolean result = false;

        if (bluetoothDevice != null) {
            deviceMACAddress = bluetoothDevice.getAddress().toUpperCase();


        }

        return result;
    }

    public synchronized void disconnect() {


        mIsConnectedPhysically = false;

        if (mBluetoothDevice != null) {
            try {
                if (mBluetooth != null)
                    mBluetooth.disconnect(mBluetoothDevice);
            } catch (Exception e) {
            }
        }

        mBluetoothDevice = null;
    }

    public void showExitDialog(String message) {

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        donSendCallback = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (android.os.Build.MANUFACTURER.toLowerCase().contains("samsun")) {
            donSendCallback = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        donSendCallback = true;
    }

    @Override
    public void bluetoothAdapterChangedState(Bluetooth bluetooth, int currentState, int previousState) {
        if (currentState != BluetoothAdapter.STATE_ON) {
            Log.e(TAG, "The Bluetooth adapter is not enabled, cannot communicate with LightX device");
            // Could ask the user if it's ok to call bluetooth.enableBluetoothAdapter() here, otherwise abort
        }
    }

    @Override
    public void bluetoothDeviceBondStateChanged(Bluetooth bluetooth, BluetoothDevice bluetoothDevice, int currentState, int previousState) {
        //no need connect here
        //connect(bluetoothDevice);

    }

    String TAG = BaseActivity.class.getSimpleName();

    @Override
    public void bluetoothDeviceConnected(Bluetooth bluetooth, final BluetoothDevice bluetoothDevice, BluetoothSocket bluetoothSocket) {
        Log.e(TAG, "Connected");

        synchronized (this) {


            if (headphCtrl != null && headphCtrl.getSocket().equals(bluetoothSocket)) {
                Log.d(TAG, "bluetoothDeviceConnected() received for extant LightX/socket pair.  Ignoring.");
                headphCtrl.resetHeadPhoneCtrl(bluetoothSocket);
            } else {
                try {
                    headphCtrl.close();
                    headphCtrl = null;
                } catch (Exception e) {
                }
                headphCtrl = HeadPhoneCtrl.getInstance(this, bluetoothSocket);
            }
            mIsConnectedPhysically = true;
        }

        isConnected = true;


    }

    @Override
    public void bluetoothDeviceDisconnected(Bluetooth bluetooth, BluetoothDevice bluetoothDevice) {
        synchronized (this) {
            mIsConnectedPhysically = false;
        }
        headphCtrl = null;//This line is important, or if bluetootch disconnect, there may be null pointer error.
        isConnected = false;
        disconnected = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {


                Log.e(TAG, "Dissconnected");
//        if (Calibration.getCalibration() != null) {
//            Calibration.getCalibration().finish();
//        }
                Log.e(TAG, "Resetdisconnect " + resetTime);
                if (resetTime == 10 * 1000)
                    handlerDelayToast.postDelayed(runnableToast, 5 * 1000);
                --resetTime;
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, resetTime);
            }

            int resetTime = 10 * 1000;
            Handler handlerDelayToast = new Handler();
            Runnable runnableToast = new Runnable() {
                @Override
                public void run() {

                }
            };
            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (!isConnected) {
                        Log.e(TAG, "Resetdisconnect ");
                        initializeOResetLibrary();

                    }
                }
            };

            // LightX App Delegate

            public void bluetoothDeviceDiscovered(Bluetooth bluetooth, BluetoothDevice bluetoothDevice) {
                try {

                        connect(bluetoothDevice);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            public void bluetoothDeviceFailedToConnect(Bluetooth bluetooth, BluetoothDevice bluetoothDevice, Exception e) {
                String name = bluetooth.deviceName(bluetoothDevice);
                Log.e(TAG, "ACL Events");
                Log.d(TAG, name + " failed to connect, waiting passively: " + e.getLocalizedMessage());
            }


            protected void onDestroy() {

                /**
                 * Destroy all single instance object;
                 */
//        EQSettingManager eqSettingManager = EQSettingManager.getEQSettingManager(this);
//        if (eqSettingManager != null)
//            eqSettingManager = null;
//        try {
//            DeviceManager deviceManager = DeviceManager.getManager(MainActivity.getMainActivity());
//            if (deviceManager != null)
//                deviceManager = null;
//        } catch (Exception e) {
//            Log.e(BaseActivity.class.getSimpleName(), e.getMessage());
//        }
//
//        try {
//            ANControlManager anCcontrolManager = ANControlManager.getANCManager(this);
//            if (anCcontrolManager != null)
//                anCcontrolManager = null;
//        } catch (Exception e) {
//            Log.e(BaseActivity.class.getSimpleName(), e.getMessage());
//        }
                handler.removeCallbacks(runnable);
                Log.e("Selection screen", "LightXB destroy");
//        if (mLightX != null) mLightX.close();
                if (mBluetooth != null) mBluetooth.close();
                try {
                    Process.killProcess(Process.myPid());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


//    public void setAppLightXDelegate(AppLightXDelegate appLightXDelegate) {
//        this.appLightXDelegate = appLightXDelegate;
//    }

//    public LightX getLightX() {
//        return mLightX;
//    }

        });
    }

    @Override
    public void bluetoothDeviceDiscovered(Bluetooth bluetooth, BluetoothDevice bluetoothDevice) {

    }

    @Override
    public void bluetoothDeviceFailedToConnect(Bluetooth bluetooth, BluetoothDevice bluetoothDevice, Exception e) {

    }
}