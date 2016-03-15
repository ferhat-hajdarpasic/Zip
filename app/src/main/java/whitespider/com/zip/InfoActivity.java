package whitespider.com.zip;

import android.os.Bundle;
import android.app.Activity;
import android.text.Html;
import android.widget.TextView;

import whitespider.com.ziptest.R;

public class InfoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        TextView textView = (TextView)findViewById(R.id.infoTextView);
        textView.setText(Html.fromHtml(
                "When selected it shall show the following text: Instructions for using this application " +
                        "<ol>" +
                        "<li> Ensure the HydroTaps Wi-Fi unit has been put into hotspot mode </li>" +
                        "<li>Connect the phone/tablet to the HydroTaps Wi-Fi hotspot using the credentials shown on the LCD screen<li>" +
                        "<li>Select an available SSID that the HydroTap will connect to<li>" +
                        "<li>Enter the corresponding password<li>" +
                        "<li>Enter the proxy details if needed<li>" +
                        "</ol>"
        ));
    }

}
