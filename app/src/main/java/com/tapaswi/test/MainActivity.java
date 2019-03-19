package com.tapaswi.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final IntentFilter intentFilter = new IntentFilter();
    static WifiP2pManager.Channel channel;
    static WifiP2pManager manager;
    ListView listView;
    Button btnDiscover;
    ProgressBar progressBar;
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();

    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    static WifiP2pConfig config;
    static WifiP2pDevice device;

    BroadcastReceiver receiver;

    WifiManager wifiManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnDiscover = (Button)  findViewById(R.id.btnDiscover);
        listView = (ListView) findViewById(R.id.deviceList);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        // Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        //getting an instance of the WifiP2pManager
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this,getMainLooper(),null);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
        wifiManager.setWifiEnabled(true);
        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //This will only initiate peer discovery
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        //Set connection status to "discovering peers.."
                        progressBar.setVisibility(View.VISIBLE);
                        Toast.makeText(MainActivity.this, "discovering peers..", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        progressBar.setVisibility(View.GONE);
                        //Set connection status to "failed to discover peers.."
                        Toast.makeText(MainActivity.this, "failed to discovering peers..", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                device = deviceArray[i];
                config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
//                Intent intent = new Intent(MainActivity.this,SecondActivity.class);
//                startActivity(intent);
                manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(),"Connected to "+device.deviceName,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(),"Connection Failed",Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    @Override
    public void onBackPressed() {
        wifiManager.setWifiEnabled(false);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    //    registering the BroadcastReceiver with the intent values to be matched
    @Override
    protected void onResume() {
        super.onResume();
        receiver = new WifiP2pBroadcastReceiver(manager,channel,this);
        registerReceiver(receiver,intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    //Fetching the list of peers

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if(!peerList.getDeviceList().equals(peers)){
                peers.clear();
                peers.addAll(peerList.getDeviceList());
                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
                int count=0;
                for(WifiP2pDevice device : peerList.getDeviceList()){
                    deviceNameArray[count] = device.deviceName;
                    deviceArray[count] = device;
                    count++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,deviceNameArray);
                listView.setAdapter(adapter);
            }

            if (peers.size() == 0) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "No device found", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){
                Toast.makeText(MainActivity.this, "Host", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this,SecondActivity.class);
                intent.putExtra("who","server");
                startActivity(intent);

            }else if(wifiP2pInfo.groupFormed){
                Toast.makeText(MainActivity.this, "Client", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this,SecondActivity.class);
                intent.putExtra("who","client");
                intent.putExtra("InetAddress",groupOwnerAddress);
                startActivity(intent);
            }
        }
    };
}
