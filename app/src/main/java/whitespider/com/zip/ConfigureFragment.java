package whitespider.com.zip;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import whitespider.com.ziptest.R;

public class ConfigureFragment extends Fragment implements IConfigureWiFiActivity {
    public static final String CONFIGURE_WIFI_EVENT = "configure-wifi-event";
    public static final String CONFIGURE_WFI_RESULT_MESSAGE = "CONFIGURE_WFI_RESULT_MESSAGE";
    private ArrayAdapter<WiFiContent.WiFiItem> arrayAdapter;
    private ListView wiFiDomainsListView;
    private TextView hiddenDomain;
    private EditText wiFiPassword;
    private RadioButton radioButtonSecurityTypeOpen;
    private RadioButton radioButtonSecurityTypeWep;
    private RadioButton radioButtonSecurityTypeWpa;
    private Button submitButton;
    private int selectedPosition = -1;
    private BroadcastReceiver broadcastReceiver;
    private ProgressBar wifiCollectProgressBar;
    private ProgressBar broadcastProgressBar;

    private OnFragmentInteractionListener mListener;

    public ConfigureFragment() {
        // Required empty public constructor
    }

    public static ConfigureFragment newInstance(String param1, String param2) {
        ConfigureFragment fragment = new ConfigureFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_configure, container, false);
        WiFiBroadcastReceiver mWifiScanReceiver = new WiFiBroadcastReceiver(getActivity(), this);
        mWifiScanReceiver.startScan();
        arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_checked);
        wiFiDomainsListView = (ListView)view.findViewById(R.id.wiFiDomainsListView);
        hiddenDomain = (TextView) view.findViewById(R.id.editHiddenDomainText);
        createAndInitConfigPassword(view);
        radioButtonSecurityTypeOpen = (RadioButton)view.findViewById(R.id.securityTypeOpen);
        radioButtonSecurityTypeWep = (RadioButton)view.findViewById(R.id.securityTypeWep);
        radioButtonSecurityTypeWpa = (RadioButton)view.findViewById(R.id.securityTypeWpa);
        submitButton = (Button)view.findViewById(R.id.submitButton);
        wifiCollectProgressBar = (ProgressBar)view.findViewById(R.id.wifiCollectProgressBar);
        broadcastProgressBar = (ProgressBar)view.findViewById(R.id.broadcastProgressBar);

        wiFiDomainsListView.setAdapter(arrayAdapter);

        hiddenDomain.clearFocus();

        wiFiDomainsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPosition = position;
                //hiddenDomain.clearFocus();
                final WiFiContent.WiFiItem item = arrayAdapter.getItem(position);
                if (item.securityType != null) {
                    switch (item.securityType) {
                        case "Open":
                            radioButtonSecurityTypeOpen.setChecked(true);
                            radioButtonSecurityTypeWep.setChecked(false);
                            radioButtonSecurityTypeWpa.setChecked(false);
                            break;
                        case "WPA":
                            radioButtonSecurityTypeOpen.setChecked(false);
                            radioButtonSecurityTypeWep.setChecked(false);
                            radioButtonSecurityTypeWpa.setChecked(true);
                            break;
                        case "WEP":
                            radioButtonSecurityTypeOpen.setChecked(false);
                            radioButtonSecurityTypeWep.setChecked(true);
                            radioButtonSecurityTypeWpa.setChecked(false);
                            break;
                        case "Other":
                            radioButtonSecurityTypeOpen.setChecked(false);
                            radioButtonSecurityTypeWep.setChecked(false);
                            radioButtonSecurityTypeWpa.setChecked(false);
                            break;
                    }
                }
//                hiddenDomain.setText("");
                checkEnabledConfiguration();

            }
        });
        radioButtonSecurityTypeWpa.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                hiddenDomain.clearFocus();
                checkEnabledConfiguration();
            }
        });
        radioButtonSecurityTypeWep.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                hiddenDomain.clearFocus();
                checkEnabledConfiguration();
            }
        });
        radioButtonSecurityTypeOpen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                hiddenDomain.clearFocus();
                checkEnabledConfiguration();
            }
        });
        hiddenDomain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkEnabledConfiguration();
            }
        });
        hiddenDomain.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (hiddenDomain.getText().toString().isEmpty()) {
                        radioButtonSecurityTypeOpen.setChecked(false);
                        radioButtonSecurityTypeWep.setChecked(false);
                        radioButtonSecurityTypeWpa.setChecked(false);
                    }
                    //listView.setSelection(-1);
                }
            }
        });
        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkEnabledConfiguration();
            }
        };

        view.findViewById(R.id.checkBoxShowPassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordOnClick((CheckBox)v);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit(v);
            }
        });
        wiFiPassword.addTextChangedListener(textWatcher);
        hiddenDomain.addTextChangedListener(textWatcher);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                broadcastProgressBar.setVisibility(View.GONE);
                String message = intent.getStringExtra(CONFIGURE_WFI_RESULT_MESSAGE);
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                broadcastReceiver,
                new IntentFilter(CONFIGURE_WIFI_EVENT));
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void createAndInitConfigPassword(View view) {
        wiFiPassword = (EditText) view.findViewById(R.id.editPasswordText);
        final String key = getKey(R.string.zip_config_password);
        String password = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(key, "");
        wiFiPassword.setText(password);
    }

    @NonNull
    private String getKey(int zip_config_password) {
        Resources res = getResources();
        return res.getString(zip_config_password);
    }

    private boolean saveNewConfigPasswordValue(TextView wiFiPassword) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = settings.edit();
        Resources res = getResources();
        final String key = getKey(R.string.zip_config_password);
        editor.putString(key, wiFiPassword.getText().toString());
        return editor.commit();
    }

    @Override
    public void refreshWiFiList(List<ScanResult> mScanResults) {
        arrayAdapter.clear();
        String wiFiNamePrefix = getStringPreference(R.string.wifi_name_prefix, "ZIPD-");
        List<WiFiContent.WiFiItem> wiFiNetworks = new ArrayList<WiFiContent.WiFiItem>();
        for(int i = 0; i < mScanResults.size(); i++){
            final ScanResult scanResult = mScanResults.get(i);
            Log.i("BSSID", scanResult.BSSID + ". ");
            Log.i("SSID", scanResult.SSID);
            Log.i("capabilities", scanResult.capabilities);
            if(!scanResult.SSID.toLowerCase().startsWith(wiFiNamePrefix.toLowerCase())) {
                WiFiContent.WiFiItem item = new WiFiContent.WiFiItem(scanResult.BSSID, scanResult.SSID, scanResult.capabilities);
                wiFiNetworks.add(item);
            }
        }

        arrayAdapter.addAll(wiFiNetworks);
        wifiCollectProgressBar.setVisibility(View.INVISIBLE);
        wiFiDomainsListView.setVisibility(View.VISIBLE);
    }

    private void checkEnabledConfiguration() {
        final CharSequence text = hiddenDomain.getText();
        final CharSequence text1 = wiFiPassword.getText();
        final boolean checked = radioButtonSecurityTypeOpen.isChecked();
        final boolean checked1 = radioButtonSecurityTypeWep.isChecked();
        final boolean checked2 = radioButtonSecurityTypeWpa.isChecked();
        boolean enableButton =
                (selectedPosition != -1 || text.toString().trim().length() > 0) &&
                        (text1.toString().trim().length() > 0) &&
                        (checked || checked1 || checked2);
        this.submitButton.setEnabled(enableButton);
    }

    public void showPasswordOnClick(CheckBox checkBox) {
        if(checkBox.isChecked()) {
            wiFiPassword.setInputType(InputType.TYPE_CLASS_TEXT);
        } else {
            wiFiPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        wiFiPassword.setSelection(wiFiPassword.getText().length());
    }

    public void submit(View view) {
        final Intent intent = new Intent(getActivity(), TcpCommunicationIntentService.class);
        String domain = hiddenDomain.getText().toString().trim();
        if(domain.isEmpty()) {
            domain = arrayAdapter.getItem(selectedPosition).ssid;
        }
        intent.putExtra(TcpCommunicationIntentService.DOMAIN_KEY, domain);
        final String password = wiFiPassword.getText().toString();
        intent.putExtra(TcpCommunicationIntentService.PASSWORD_KEY, password);

        int securityType = 0;
        if(radioButtonSecurityTypeOpen.isChecked()) {
            securityType = 0;
        } else if (radioButtonSecurityTypeWpa.isChecked()) {
            securityType = 1;
        } else if (radioButtonSecurityTypeWep.isChecked()) {
            securityType = 2;
        }
        intent.putExtra(TcpCommunicationIntentService.SECURITY_TYPE_KEY, securityType);

        getActivity().startService(intent);

        broadcastProgressBar.setVisibility(View.VISIBLE);

        saveNewConfigPasswordValue(wiFiPassword);
    }

    public String getStringPreference(int preferenceId, String defaultValue) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String preferenceValue = getKey(preferenceId);
        return sharedPrefs.getString(preferenceValue, defaultValue);
    }
}
