package rashmitha.com.vchef;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class CodePage extends AppCompatActivity {

    BroadcastReceiver sms_receiver ;
    private static final int  SMS_PERM_CONST = 1;
    String[] perm = new String[] { Manifest.permission.SEND_SMS};

    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_page);

        address = "00:21:13:01:F9:3E";  //
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");

        new ConnectBT().execute();

        sms_receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //sms_read(context,intent);
                Toast.makeText(context, "Inside SMS Read", Toast.LENGTH_SHORT).show();


                Bundle bundle = intent.getExtras();
                Object[] objarr = (Object[]) bundle.get("pdus");

                if (ActivityCompat.checkSelfPermission(CodePage.this,
                        Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CodePage.this, perm, SMS_PERM_CONST);
                }

                for (int i = 0; i < objarr.length; i++) {
                    SmsMessage smsMsg;
                    smsMsg = SmsMessage.createFromPdu((byte[]) objarr[i]);
                    String sender = smsMsg.getDisplayOriginatingAddress();
                    //pointR = smsToInt(smsMsg);
                    //String smsNumber = smsMsg.getOriginatingAddress();

                    // if(sender.equals("IM-WAYSMS")) {
                    char[] sms_text = smsMsg.getMessageBody().trim().toCharArray();
                    int p = Character.getNumericValue(sms_text[3]);

                    if (p == 1) {
                        Toast.makeText(context, "Payment Done", Toast.LENGTH_SHORT).show();


                        if (btSocket!=null)
                        {
                            try
                            {
                                Toast.makeText(context, "Inside BT Socket", Toast.LENGTH_SHORT).show();

                                btSocket.getOutputStream().write("1".getBytes());
                            }
                            catch (IOException e)
                            {
                                Toast.makeText(context, "BT Error", Toast.LENGTH_SHORT).show();
                            }
                        }

                        else {
                            Toast.makeText(context, "BTSocket Issue!", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(context, "Else: " + p, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        registerReceiver(sms_receiver,filter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {

            case SMS_PERM_CONST :
            {
                // Get the default instance of SmsManager
                SmsManager smsManager = SmsManager.getDefault();
                // Send a text based SMS
                //smsManager.sendTextMessage(phoneNumber, null, smsBody, sentPendingIntent, deliveredPendingIntent);
            }
        }
    }


    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(CodePage.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                Toast.makeText(CodePage.this, "Connection Failed. Is it a SPP Bluetooth? Try again.", Toast.LENGTH_SHORT).show();
                finish();
            }
            else
            {
                Toast.makeText(CodePage.this, "Connected.", Toast.LENGTH_SHORT).show();
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

}
