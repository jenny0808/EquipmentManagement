package com.catstudio.qrexample;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Button btn_scan;
    private TextView txt_url;
    private final static int REQUEST_ENABLE_BT = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            //Device does not support Bluetooth
            Toast.makeText(this, "This device does not support Bluetooth", Toast.LENGTH_SHORT).show();
        }
        if(!mBluetoothAdapter.isEnabled()){
            //Ask user to open the bluetooth authorize
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        final List<String> mArrayAdapter = new ArrayList<String>();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size()>0){
            // If there are paired devices
            for(BluetoothDevice device : pairedDevices){
                // Add he name and address to an array
                // adapter to show in a List
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
        // Create s BroadcastReceiver for ACTION_FOUND
        final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                //When find a device
                if(BluetoothDevice.ACTION_FOUND.equals(action)){
                    //Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
        };
        //Register the BroadcastReveiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        setContentView(R.layout.activity_main);
        this.btn_scan = (Button)findViewById(R.id.btn_scan);
        this.txt_url = (TextView) findViewById(R.id.txt_url);

        this.btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(MainActivity.this).initiateScan();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if(result!=null){
            String scanContent = result.getContents();
            String scanFormat = result.getFormatName();
            txt_url.setText(scanFormat+" \n"+scanContent);
        }else{
            Toast.makeText(getApplicationContext(), "nothing", Toast.LENGTH_LONG).show();
        }

    }
}
