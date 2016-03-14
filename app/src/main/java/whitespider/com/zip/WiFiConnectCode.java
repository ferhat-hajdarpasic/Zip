package whitespider.com.zip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.widget.AppCompatTextView;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import whitespider.com.ziptest.R;

public class WiFiConnectCode {
    private static final String TAG = "WiFiConnectCode";
    private final WiFiNetworksListView mWiFiNetworksListView;
    private MainActivity mActivity;
    private WiFiContent.WiFiItem wiFiItem;
    private NetworkChangedReceiver networkChangedReceiver;
    private ProgressBar connectProgressBar;

    public WiFiConnectCode(MainActivity activity) {
        mActivity = activity;
        connectProgressBar = (ProgressBar)activity.findViewById(R.id.wifiCollectProgressBar);
        mWiFiNetworksListView = (WiFiNetworksListView)activity.findViewById(R.id.device_list);
    }
    public void connect(WiFiContent.WiFiItem wiFiItem, String wiFiPassword, View view) {
        WifiManager mWifiManager = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
        final WifiInfo connectionInfo = mWifiManager.getConnectionInfo();
        if(connectionInfo.getSSID().equals(wiFiItem.ssid)) {
            Log.d(TAG, "Already connected to : " + connectionInfo);
        } else {
            connectProgressBar.setVisibility(View.VISIBLE);
            this.wiFiItem = wiFiItem;
            if (networkChangedReceiver != null) {
                mActivity.unregisterReceiver(networkChangedReceiver);
            }

            networkChangedReceiver = new NetworkChangedReceiver(this.wiFiItem, wiFiPassword, view);
            mActivity.registerReceiver(networkChangedReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
            mActivity.registerReceiver(networkChangedReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
            mWiFiNetworksListView.setConnectedWiFiItem(this.wiFiItem);

            disconnectAP();
        }
    }

    private void connectAP(WiFiContent.WiFiItem wiFiItem, String networkPass) {
        WifiManager wifiManager = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = convertToQuotedString(wiFiItem.ssid);
        config.preSharedKey = convertToQuotedString(networkPass); //String.format("\"{0}\"", networkPass);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

        int networkId = wifiManager.addNetwork(config);
        // Connect to network by disabling others.
        wifiManager.enableNetwork(networkId, true);
        wifiManager.saveConfiguration();
        wifiManager.reconnect();
    }

    protected static String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }

    protected static String removeQuotedString(String string) {
        if(string.startsWith("\"") && string.endsWith("\"") && string.length() >= 3) {
            return string.substring(1, string.length() - 1);
        } else {
            return string;
        }
    }

    public boolean disconnectAP() {
        WifiManager wifiManager = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            //remove the current network Id
            WifiInfo curWifi = wifiManager.getConnectionInfo();
            if (curWifi == null) {
                return false;
            }
            int curNetworkId = curWifi.getNetworkId();
            wifiManager.removeNetwork(curNetworkId);
            wifiManager.saveConfiguration();

            // remove other saved networks
            List<WifiConfiguration> netConfList = wifiManager.getConfiguredNetworks();
            if (netConfList != null) {
                Log.v(TAG, "remove configured network ids");
                for (int i = 0; i < netConfList.size(); i++) {
                    WifiConfiguration conf = netConfList.get(i);
                    wifiManager.removeNetwork(conf.networkId);
                }
            }
        }
        wifiManager.saveConfiguration();
        return true;
    }

    public void refreshConnectionDisplay(List<WiFiContent.WiFiItem> filteredWiFiNetworks) {
        boolean stateSet = false;
        WifiManager mWifiManager = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
        final WifiInfo connectionInfo = mWifiManager.getConnectionInfo();
        for (WiFiContent.WiFiItem wiFiItem : filteredWiFiNetworks) {
            final String connectionInfoSSID = removeQuotedString(connectionInfo.getSSID());
            final String ssid = wiFiItem.ssid;
            if(connectionInfoSSID.equals(ssid)) {
                mWiFiNetworksListView.setConnectedWiFiItem(wiFiItem);
                mWiFiNetworksListView.indicateConnectingState(connectionInfo);
                Log.d(TAG, "Connected to : " + connectionInfo);
            }
        }
    }

    private class NetworkChangedReceiver extends BroadcastReceiver {
        private static final String TAG = "NetworkChangedReceiver";
        private final WiFiContent.WiFiItem wiFiItem;
        private final String networkPass;
        private View view;
        public NetworkChangedReceiver(WiFiContent.WiFiItem wiFiItem, String networkPass, View view) {
            this.wiFiItem = wiFiItem;
            this.networkPass = networkPass;
            this.view = view;
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v("WifiReceiver", "onReceive() is calleld with " + intent);
            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                Log.v(TAG, "mWifiNetworkInfo: " + networkInfo.toString());
                Log.v(TAG, "mWifiNetworkInfo.getExtraInfo: " + networkInfo.getExtraInfo());
                WifiManager mWifiManager = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
                final WifiInfo connectionInfo = mWifiManager.getConnectionInfo();
                Log.d(TAG, "connectionInfo=" + connectionInfo);
                if(networkInfo.getExtraInfo().contains(wiFiItem.ssid)) {
                    mWiFiNetworksListView.indicateConnectingState(networkInfo);
                } else if(connectionInfo.getSSID().equals(wiFiItem.ssid)) {
                    mWiFiNetworksListView.indicateConnectingState(connectionInfo);
                }
                final NetworkInfo.State state = networkInfo.getState();
                if (state == NetworkInfo.State.CONNECTED) {
                    connectProgressBar.setVisibility(View.GONE);
                    connectProgressBar.setVisibility(View.INVISIBLE);
                } else if(state == NetworkInfo.State.DISCONNECTED) {
                    //Toast.makeText(WiFiConnectCode.this.mActivity, "Existing WiFi Disconnected", Toast.LENGTH_SHORT).show();
                    connectAP(wiFiItem, networkPass);
                } else {
                    int y = 90;
                }
            } else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
//                    if(connectionInfo.getBSSID() == null) {
//                        ((AppCompatTextView)view).setText(wiFiItem.ssid);
//                    }
                }
            } else {
                return;
            }
        }
    }
}
