package whitespider.com.zip;

import android.app.Activity;
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
import android.widget.CheckedTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import whitespider.com.ziptest.R;

/**
 * Created by ferhat on 2/18/2016.
 */
public class WiFiConnectCode {
    private static final String TAG = "WiFiConnectCode";
    private Activity mActivity;
    private WiFiContent.WiFiItem wiFiItem;
    private NetworkChangedReceiver networkChangedReceiver;
    private ProgressBar connectProgressBar;
    private boolean connected;
    private boolean isConnected;

    public WiFiConnectCode(Activity activity) {
        mActivity = activity;
        connectProgressBar = (ProgressBar)activity.findViewById(R.id.wifiCollectProgressBar);
    }
    public void connect(WiFiContent.WiFiItem wiFiItem, String wiFiPassword, View view) {
        connectProgressBar.setVisibility(View.VISIBLE);
        this.wiFiItem = wiFiItem;
        String networkSSID = this.wiFiItem.ssid;

        if(networkChangedReceiver != null) {
            mActivity.unregisterReceiver(networkChangedReceiver);
        }

        networkChangedReceiver = new NetworkChangedReceiver(networkSSID, wiFiPassword, view);
        mActivity.registerReceiver(networkChangedReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
        disconnectAP();
    }

    private void connectAP(String networkSSID, String networkPass) {
        WifiManager wifiManager = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = convertToQuotedString(networkSSID);
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
                    WifiConfiguration conf = new WifiConfiguration();
                    conf = netConfList.get(i);
                    wifiManager.removeNetwork(conf.networkId);
                }
            }
        }
        wifiManager.saveConfiguration();
        return true;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return isConnected;
    }

    private class NetworkChangedReceiver extends BroadcastReceiver {
        private static final String TAG = "NetworkChangedReceiver";
        private final String networkSSID;
        private final String networkPass;
        private boolean isDisconnected = false;
        private View view;
        public NetworkChangedReceiver(String networkSSID, String networkPass, View view) {
            this.networkSSID = networkSSID; //wiFiItem.ssid;
            this.networkPass = networkPass;
            this.view = view;
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v("WifiReceiver", "onReceive() is calleld with " + intent);
            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo networkInfo =
                        (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                Log.v(TAG, "mWifiNetworkInfo: " + networkInfo.toString());
                final NetworkInfo.State state = networkInfo.getState();
                if (state == NetworkInfo.State.CONNECTED) {
                    if(isDisconnected) {
                        isConnected = true;
                        isDisconnected = false;
                        connectProgressBar.setVisibility(View.GONE);
                        connectProgressBar.setVisibility(View.INVISIBLE);
                        ((AppCompatTextView)view).setText(
                                Html.fromHtml(
                                networkSSID + " - <font color=\"#E0711C\">Connected</font>"));

                    } else {
                        int y = 99;
                    }
                } else if(state == NetworkInfo.State.DISCONNECTED) {
                    Toast.makeText(WiFiConnectCode.this.mActivity, "Existing WiFi Disconnected", Toast.LENGTH_SHORT).show();
                    isDisconnected = true;
                    connectAP(networkSSID, networkPass);
                }
            } else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int mWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN);
            }
            else {
                return;
            }
        }
    }
}
