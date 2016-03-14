package whitespider.com.zip;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

public class WiFiNetworksListView extends android.widget.ListView {
    private WiFiNetworksListViewAdapter arrayAdapter;

    public WiFiNetworksListView(Context context) {
        super(context);
        init(context);
    }

    public WiFiNetworksListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WiFiNetworksListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public WiFiNetworksListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        arrayAdapter =
                new WiFiNetworksListViewAdapter(context, android.R.layout.simple_list_item_activated_1,
                        new ArrayList<WiFiContent.WiFiItem>());
        setAdapter(arrayAdapter);
    }

    public void clear() {
        arrayAdapter.clear();
    }

    public void refreshFromScanResult(List<WiFiContent.WiFiItem> filteredWiFiNetworks) {
        arrayAdapter.clear();
        arrayAdapter.addAll(filteredWiFiNetworks);
    }

    @NonNull
    public List<WiFiContent.WiFiItem> getFilteredNetworks(List<ScanResult> scanResults, String wiFiNamePrefix) {
        List<WiFiContent.WiFiItem> filteredWiFiNetworks = new ArrayList<>();
        for (int i = 0; i < scanResults.size(); i++) {
            final ScanResult scanResult = scanResults.get(i);
            if (scanResult.SSID.toLowerCase().startsWith(wiFiNamePrefix.toLowerCase())) {
                WiFiContent.WiFiItem item = new WiFiContent.WiFiItem(scanResult.BSSID, scanResult.SSID, scanResult.capabilities);
                filteredWiFiNetworks.add(item);
            }
        }
        return filteredWiFiNetworks;
    }

    public void indicatedConnectedWiFi(WiFiContent.WiFiItem wiFiItem) {
    }

    public void indicateConnectingState(WifiInfo connectionInfo) {
        arrayAdapter.setConnectingState(connectionInfo.getSupplicantState().toString());
    }

    public void indicateConnectingState(NetworkInfo networkInfo) {
        arrayAdapter.setConnectingState(networkInfo.getState().name());
    }

    public void setConnectedWiFiItem(WiFiContent.WiFiItem wiFiItem) {
        arrayAdapter.setConnectedWiFiItem(wiFiItem);
    }
}
