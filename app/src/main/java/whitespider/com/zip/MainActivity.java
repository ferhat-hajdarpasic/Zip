package whitespider.com.zip;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import whitespider.com.ziptest.R;

public class MainActivity extends AppCompatActivity implements IConfigureWiFiActivity  {

    private WiFiNetworksListView mWiFiNetworksListView;
    private Button mBtnScan = null;
    private ProgressBar wifiCollectProgressBar;
    private WiFiConnectCode connectCode;
    private WiFiBroadcastReceiver mWifiScanReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWiFiNetworksListView = (WiFiNetworksListView) findViewById(R.id.device_list);
        mBtnScan = (Button)findViewById(R.id.btn_scan);
        wifiCollectProgressBar = (ProgressBar)findViewById(R.id.wifiCollectProgressBar);

        mWiFiNetworksListView.setOnItemClickListener(mDeviceClickListener);
        mWifiScanReceiver = new WiFiBroadcastReceiver(this, this);

        mBtnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });

        connectCode = null; //new WiFiConnectCode(this);
        startScan();
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
        if (id == R.id.action_config_wifi) {
            startActivity(new Intent(this, ConfigureWiFiActivity.class));
            return true;
        }

        if (id == R.id.action_info) {
            startActivity(new Intent(this, InfoActivity.class));
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

        Resources res = getResources();
        final String key = getKey(R.string.zip_saved_password);
        String password = PreferenceManager.getDefaultSharedPreferences(this).getString(key, "");
        editText.setText(password);
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                saveNewWifiPasswordValue(editText);
                connectCode.connect(wiFiItem, editText.getText().toString(), view);
            }
        }).setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private boolean saveNewWifiPasswordValue(TextView savedPassword) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        final String key = getKey(R.string.zip_saved_password);
        editor.putString(key, savedPassword.getText().toString());
        return editor.commit();
    }

    @NonNull
    private String getKey(int zip_config_password) {
        Resources res = getResources();
        return res.getString(zip_config_password);
    }

    @Override
    public void refreshWiFiList(List<ScanResult> scanResults) {
        String wiFiNamePrefix = getStringPreference(R.string.wifi_name_prefix);
        List<WiFiContent.WiFiItem> filteredWiFiNetworks = mWiFiNetworksListView.getFilteredNetworks(scanResults, wiFiNamePrefix);
        mWiFiNetworksListView.refreshFromScanResult(filteredWiFiNetworks);
        connectCode.refreshConnectionDisplay(filteredWiFiNetworks);
        stopScan();
    }

    public String getStringPreference(int preferenceId) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String preferenceValue = this.getResources().getString(preferenceId);
        return sharedPrefs.getString(preferenceValue, "");
    }

    public void startScan() {
        mBtnScan.setEnabled(false);
        mWiFiNetworksListView.clear();
        WifiManager wifiManager = (WifiManager) MainActivity.this.getSystemService(Context.WIFI_SERVICE);
        boolean success = wifiManager.setWifiEnabled(true);
        if(success) {
            success = wifiManager.disconnect();
            if(success) {
                mWifiScanReceiver.startScan();
                wifiCollectProgressBar.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Could not disconnect from current WiFi", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Could not start WiFi", Toast.LENGTH_SHORT).show();
        }
    }

    public void stopScan() {
        mBtnScan.setEnabled(true);
        ProgressBar wifiCollectProgressBar = (ProgressBar)findViewById(R.id.wifiCollectProgressBar);
        wifiCollectProgressBar.setVisibility(View.INVISIBLE);
        mWifiScanReceiver.stopScan();
    }
}
