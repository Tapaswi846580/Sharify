package com.tapaswi.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;


public class WifiP2pBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;

    public WifiP2pBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MainActivity mainActivity) {
        this.manager = manager;
        this.channel = channel;
        this.mainActivity = mainActivity;
    }

    private WifiP2pManager.Channel channel;
    private MainActivity mainActivity;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                //This says that wifi is On
                //activity.setIsWifiP2pEnabled(true);
            } else {
                //This says Wifi is off
                //activity.setIsWifiP2pEnabled(false);
            }
        }else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // The peer list has changed! We should probably do something about
            // that.
            if(manager != null){
                manager.requestPeers(channel,mainActivity.peerListListener);
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            // Connection state changed! We should probably do something about
            // that.
            if(manager == null){
                return;
            }
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if(networkInfo.isConnected()){
                manager.requestConnectionInfo(channel, mainActivity.connectionInfoListener);
            }else{
                //Show the message that device is disconnected
                Toast.makeText(context, "Device Disconnected", Toast.LENGTH_SHORT).show();
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {


        }
    }
}
