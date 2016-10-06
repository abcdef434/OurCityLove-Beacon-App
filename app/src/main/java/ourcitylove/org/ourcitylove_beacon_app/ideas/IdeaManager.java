package ourcitylove.org.ourcitylove_beacon_app.ideas;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;

/**
 * Created by Vegetable on 2016/10/3.
 */

public class IdeaManager {

    public static boolean hasBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null)
            return false;

        if (!mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.enable();

        return true;
    }

    public static void initBeaconService(Application app) {
//        Intent serviceIntent = new Intent(app, BeaconContentService.class);
//        serviceIntent.putExtra("server_ip", "ideas.iiibeacon.net"); // must provide
//        serviceIntent.putExtra("app_key", "5f76a9532df178b2e9b0e8cc1dfc13a34255f919"); // must provide
//        // detect_timer is not necessary, default will update every 3 minutes.
//        serviceIntent.putExtra("detect_timer", 120*1000); // in milliseconds, set to 2 minutes in this demo.
//        serviceIntent.putExtra("receiver", new BeaconReceiver(null, app)); // must provide
//        app.startService(serviceIntent);
    }
}
