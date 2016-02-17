package whitespider.com.zip.command;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by ferhat on 30/11/2015.
 */
public class ConfigureWiFiCommand extends MotionCommand {
    private static final String TAG = ConfigureWiFiCommand.class.getSimpleName();
    public int SecurityType;
    public String Domain;
    public String Password;

    public byte[] getBytes()
    {
        byte[] payload = (SecurityType + "\0" + Domain.trim() + "\0" + Password.trim()).getBytes();
        byte[] msg = new byte[] {(byte) 'h', (byte) (2 + payload.length)};

        byte[] result = concat(msg, payload);
        return result;
    }

    private byte[] concat(byte[] msg, byte[] payload) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(msg);
            baos.write(payload);
            return baos.toByteArray();
        } catch (IOException e) {
            Log.e(TAG, "Error concatenating two arrays.", e);
            e.printStackTrace();
            throw new RuntimeException("Error concatenating two arrays.", e);
        }
    }
}
