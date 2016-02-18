package whitespider.com.zip;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import whitespider.com.ziptest.R;

public class MainActivity extends AppCompatActivity implements IConfigureWiFiActivity  {

    private ListView mDeviceListView;
    private Button mBtnScan = null;
    private ProgressBar wifiCollectProgressBar;
    private ArrayAdapter<WiFiContent.WiFiItem> arrayAdapter;
    private WiFiConnectCode connectCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDeviceListView = (ListView) findViewById(R.id.device_list);
        mBtnScan = (Button)findViewById(R.id.btn_scan);
        wifiCollectProgressBar = (ProgressBar)findViewById(R.id.wifiCollectProgressBar);

        mDeviceListView.setOnItemClickListener(mDeviceClickListener);
        BroadcastReceiver mWifiScanReceiver = new WiFiBroadcastReceiver(this);

        mBtnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arrayAdapter.clear();
                WifiManager wifiManager = (WifiManager) MainActivity.this.getSystemService(Context.WIFI_SERVICE);
                wifiManager.startScan();
                wifiCollectProgressBar.setVisibility(View.VISIBLE);
                connectCode.setConnected(false);
            }
        });
        arrayAdapter =
                new ArrayAdapter<WiFiContent.WiFiItem>(this,
                        android.R.layout.simple_list_item_activated_1, new ArrayList<WiFiContent.WiFiItem>());
        mDeviceListView.setAdapter(arrayAdapter);

        connectCode = new WiFiConnectCode(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_config_wifi) {
            startActivity(new Intent(this, ConfigureWiFiActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Listener for device list
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
            // Log.d(TAG,"item click");
            //mBtnScan.setEnabled(false);
            WiFiContent.WiFiItem item = (WiFiContent.WiFiItem) parent.getItemAtPosition(pos);
            connect(item, view);
        }
    };

    private void connect(WiFiContent.WiFiItem wiFiItem, View view) {
        showCollectPasswordDialog(wiFiItem, view);
    }

    protected void showCollectPasswordDialog(final WiFiContent.WiFiItem wiFiItem, final View view) {

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.password_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.passwordEdittext);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        connectCode.connect(wiFiItem, editText.getText().toString(), view);
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    @Override
    public void refreshWiFiList(List<ScanResult> mScanResults) {
        if(!connectCode.isConnected()) {
            arrayAdapter.clear();

            String wiFiNamePrefix = getStringPreference(R.string.wifi_name_prefix);
            List<WiFiContent.WiFiItem> wiFiNetworks = new ArrayList<WiFiContent.WiFiItem>();
            for (int i = 0; i < mScanResults.size(); i++) {
                final ScanResult scanResult = mScanResults.get(i);
                if (scanResult.SSID.toLowerCase().startsWith(wiFiNamePrefix.toLowerCase())) {
                    WiFiContent.WiFiItem item = new WiFiContent.WiFiItem(scanResult.BSSID, scanResult.SSID, scanResult.capabilities);
                    wiFiNetworks.add(item);
                }
            }

            arrayAdapter.addAll(wiFiNetworks);
            // mBtnScan.setEnabled(true);
            wifiCollectProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    public String getStringPreference(int preferenceId) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String preferenceValue = this.getResources().getString(preferenceId);
        return sharedPrefs.getString(preferenceValue, "");
    }
}
