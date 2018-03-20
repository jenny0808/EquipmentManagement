package com.catstudio.qrexample;

import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.util.Set;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    private Button btn_scan;
    private TextView txt_url;
    private Event_Button event_button;
    private Event_ListView event_listView;
    private Event_Bluetooth event_bluetooth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.btn_scan = (Button)findViewById(R.id.btn_scan);
        this.txt_url = (TextView) findViewById(R.id.txt_url);
        this.btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(MainActivity.this).initiateScan();
            }
        });

                initializeBluetooth();
                initializeButtonEvent();
                initializeListViewEvent();

    }


    private void initializeButtonEvent()
    {
        event_button = new Event_Button();
    }

    private void initializeListViewEvent()
    {
        event_listView = new Event_ListView();
    }

    private void initializeBluetooth()
    {
        event_bluetooth = new Event_Bluetooth();
    }


    private class Event_Button implements OnClickListener
    {
        private Button btn_showConnectedDevices;
        private Button btn_searchDevices;

        public Event_Button()
        {
            btn_showConnectedDevices = (Button) findViewById(R.id.btn_showConnectedDevices);
            btn_searchDevices = (Button) findViewById(R.id.btn_searchDevices);

            btn_showConnectedDevices.setOnClickListener(this);
            btn_searchDevices.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            switch(v.getId())
            {
                case R.id.btn_showConnectedDevices:
                    showConnectedDevice();
                    break;
                case R.id.btn_searchDevices:
                    searchDevice();
                    break;
            }
        }

        private void showConnectedDevice()
        {
            event_listView.showConnectedDeviceName();
        }

        private void searchDevice()
        {
            event_bluetooth.searchBluetooth();
        }
    }


    private class Event_ListView implements OnItemClickListener
    {
        private ListView listView_showDevice;
        private ArrayAdapter<String> deviceName;

        public Event_ListView()
        {
            listView_showDevice = (ListView) findViewById(R.id.listView_showDevice);
            listView_showDevice.setOnItemClickListener(this);
        }

        private void showConnectedDeviceName()
        {
            deviceName = event_bluetooth.getConnectedDeviceName();
            listView_showDevice.setAdapter(deviceName);
        }

        private void showSearchDeviceName()
        {
            deviceName = event_bluetooth.deviceName;
            listView_showDevice.setAdapter(deviceName);
        }

        @Override

        //when pressed the bluetooth device.
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id)
        {
                String itemString = parent.getItemAtPosition(position).toString();
                Toast.makeText(MainActivity.this, "device name:" + itemString, Toast.LENGTH_SHORT).show();
        }
    }

    private class Event_Bluetooth extends BroadcastReceiver
    {
        private BluetoothAdapter bluetoothAdapter;
        private ArrayAdapter<String> deviceName = new ArrayAdapter<String>
                (MainActivity.this, android.R.layout.simple_list_item_1);
        private Set<BluetoothDevice> pairedDevice;

        public Event_Bluetooth()
        {
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(this, filter);

            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


            if (bluetoothAdapter == null)
            {
                Toast.makeText(MainActivity.this, "���˸m�L�Ū�", Toast.LENGTH_LONG).show();
                finish();
            }
            if (bluetoothAdapter.isEnabled() == false)
            {
                openBluetooth();
            }
        }

        private void openBluetooth()
        {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            startActivityForResult(intent, 1);
        }

        private ArrayAdapter<String> getConnectedDeviceName()
        {
            pairedDevice = bluetoothAdapter.getBondedDevices();
            deviceName.clear();
            if (pairedDevice.size() > 0)
            {
                for (BluetoothDevice device : pairedDevice)
                {
                    this.deviceName.add(device.getName());
                }
            }
            return this.deviceName;
        }

        private void searchBluetooth()
        {
            deviceName.clear();
            bluetoothAdapter.cancelDiscovery();
            bluetoothAdapter.startDiscovery();
        }

        private void openDiscover(int time)
        {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, time);
            startActivity(intent);
        }

        private void closeBluetoothDevice()
        {
            bluetoothAdapter.disable();
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {

            // TODO Auto-generated method stub

            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND) == true)
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                System.out.println(device.getAddress());
                if(device.getName()!=null){
                    deviceName.add(device.getName());
                }
                event_listView.showSearchDeviceName();
            }
            else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED))
            {
                if (bluetoothAdapter.getState() == bluetoothAdapter.STATE_OFF)
                {
                    openBluetooth();
                }
            }
        }

    }

    @Override
    protected void onDestroy()
    {
        // TODO Auto-generated method stub
        super.onDestroy();

        if (event_bluetooth.bluetoothAdapter!=null && event_bluetooth.bluetoothAdapter.isEnabled()==true)
        {
            event_bluetooth.bluetoothAdapter.cancelDiscovery();
        }
        unregisterReceiver(event_bluetooth);
    }

    @Override

    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if(result!=null){
            String scanContent = result.getContents();
            String scanFormat = result.getFormatName();
            txt_url.setText(scanFormat+" \n"+scanContent);
        }else{
            Toast.makeText(getApplicationContext(), "nothing", Toast.LENGTH_LONG).show();
        }
// TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode==1)
        {
            if (resultCode==RESULT_CANCELED)
            {
                Toast.makeText(this, "canceled", Toast.LENGTH_LONG).show();
                finish();
            }
            else if (resultCode==RESULT_OK)
            {
                event_listView.showConnectedDeviceName();
            }
        }
    }
}
