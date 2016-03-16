package whitespider.com.zip;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ferhat on 3/12/2016.
 */
public class WiFiNetworksListViewAdapter extends ArrayAdapter<WiFiContent.WiFiItem> {
    private static final String TAG = "WiFiNet...ViewAdapter";
    private WiFiContent.WiFiItem mselectedWiFiItem;
    private String connectingState;

    public WiFiNetworksListViewAdapter(Context context, int resource, ArrayList<WiFiContent.WiFiItem> wiFiItems) {
        super(context, resource, wiFiItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final TextView view = (TextView) super.getView(position, convertView, parent);
        WiFiContent.WiFiItem thisItem = getItem(position);

        WifiManager mWifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        final WifiInfo connectionInfo = mWifiManager.getConnectionInfo();
        final String connectionInfoSSID = WiFiConnectCode.removeQuotedString(connectionInfo.getSSID());
        final String ssid = thisItem.ssid;
        if(connectionInfoSSID.equalsIgnoreCase(ssid)) {
            connectingState = connectionInfo.getSupplicantState().name();
            this.mselectedWiFiItem = thisItem;
        }

        if(this.mselectedWiFiItem != null) {
            if((connectingState != null) && thisItem.equals(mselectedWiFiItem)) {
                view.setText(Html.fromHtml(mselectedWiFiItem.ssid + " - <font color=\"#E0711C\">" + connectingState + "</font>"));
            }
        }
        Log.d(TAG, "mselectedWiFiItem=" + view.getText().toString());
        return view;
    }

    @Override
    public void clear() {
        super.clear();
    }

    public void setSelectedWiFiItem(WiFiContent.WiFiItem wiFiItem) {
        this.mselectedWiFiItem = wiFiItem;
        this.connectingState = null;
        this.notifyDataSetChanged();
    }

    public void setConnectingState(String connectingState) {
        this.connectingState = connectingState;
        this.notifyDataSetChanged();
    }
}
