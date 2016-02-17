package whitespider.com.zip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class WiFiContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<WiFiItem> ITEMS = new ArrayList<WiFiItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, WiFiItem> ITEM_MAP = new HashMap<String, WiFiItem>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(WiFiItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.bssid, item);
    }

    private static WiFiItem createDummyItem(int position) {
        return new WiFiItem(String.valueOf(position), "Item " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class WiFiItem {
        public String bssid;
        public String ssid;
        public String capabilities;
        public String securityType;

        public WiFiItem(String bssid, String ssid, String capabilities) {
            this.bssid = bssid;
            this.ssid = ssid;
            this.capabilities = capabilities;
            if(capabilities.contains("WPA")) {
                this.securityType= "WPA";
            } else if(capabilities.contains("WEP")) {
                this.securityType= "WEP";
            } else {
                this.securityType= "Open";
            }
        }

        @Override
        public String toString() {
            return ssid;
        }
    }
}
