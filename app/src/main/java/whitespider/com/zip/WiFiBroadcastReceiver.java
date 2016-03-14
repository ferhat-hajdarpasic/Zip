package whitespider.com.zip;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class WiFiBroadcastReceiver extends BroadcastReceiver {
    private Activity activity;
    public WiFiBroadcastReceiver(Activity activity) {
        this.activity = activity;
    }

    public void startScan() {
        activity.registerReceiver(this, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        WifiManager mWifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        mWifiManager.startScan();
    }

    public void stopScan() {
        activity.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context c, Intent intent) {
        if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            WifiManager mWifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
            List<ScanResult> mScanResults = mWifiManager.getScanResults();
            Log.i("", "Number Of Wifi connections :" + mScanResults.size());
            List<WiFiContent.WiFiItem> wiFiNetworks = new ArrayList<WiFiContent.WiFiItem>();
            for(int i = 0; i < mScanResults.size(); i++){
                final ScanResult scanResult = mScanResults.get(i);
                Log.i("BSSID", scanResult.BSSID + ". ");
                Log.i("SSID", scanResult.SSID);
                Log.i("capabilities", scanResult.capabilities);
                WiFiContent.WiFiItem item = new WiFiContent.WiFiItem(scanResult.BSSID, scanResult.SSID, scanResult.capabilities);
                wiFiNetworks.add(item);
            }
            ((IConfigureWiFiActivity)activity).refreshWiFiList(mScanResults);
        }
    }
}
