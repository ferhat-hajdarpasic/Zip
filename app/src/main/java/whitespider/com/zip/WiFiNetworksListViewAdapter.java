package whitespider.com.zip;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
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
    private WiFiContent.WiFiItem mConnectedWiFiItem;
    private String connectingState;

    public WiFiNetworksListViewAdapter(Context context, int resource, ArrayList<WiFiContent.WiFiItem> wiFiItems) {
        super(context, resource, wiFiItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view1 = super.getView(position, convertView, parent);
        final TextView view = (TextView) view1;
        WiFiContent.WiFiItem thisItem = getItem(position);
        if(this.mConnectedWiFiItem != null) {
            if((connectingState != null) && thisItem.equals(mConnectedWiFiItem)) {
                view.setText(Html.fromHtml(mConnectedWiFiItem.ssid + " - <font color=\"#E0711C\">" + connectingState + "</font>"));
            }
        }
        Log.d(TAG, "mConnectedWiFiItem=" + view.getText().toString());
        return view;
    }

    @Override
    public void clear() {
        super.clear();
    }

    public void setConnectedWiFiItem(WiFiContent.WiFiItem wiFiItem) {
        this.mConnectedWiFiItem = wiFiItem;
        this.notifyDataSetChanged();
    }

    public void setConnectingState(String connectingState) {
        this.connectingState = connectingState;
        this.notifyDataSetChanged();
    }
}
