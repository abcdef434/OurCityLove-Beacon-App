package ourcitylove.org.ourcitylove_beacon_app.hinet;

import android.Manifest;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.util.Log;

import com.cht.beacon.notify.BeaconNotifyLib;
import com.cht.beacon.notify.Interface.IInitListener;
import com.cht.beacon.notify.Module.BeaconLibBuilder;
import com.cht.beacon.notify.Module.BeaconLibModule;
import com.cht.beacon.notify.Module.ManifestParser;
import com.karumi.dexter.Dexter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Iterator;
import java.util.List;

import ourcitylove.org.ourcitylove_beacon_app.LocationManager;
import ourcitylove.org.ourcitylove_beacon_app.RationalePermissionListener;

import static android.content.Context.SENSOR_SERVICE;

public class HinetManager implements SensorEventListener2 {
    private static final String TAG = HinetManager.class.getSimpleName();
    private static final int BEACON_SCAN_PERIOD = 1000;
    private static final int BEACON_SCAN_PAUSE = 1000;

    private final SensorManager sensorManager;
    private final Application app;
    private volatile BeaconNotifyLib singletonInstance;

    private float[] mGravity;
    private float[] mGeomagnetic;
    private int sensorEventSkip;

    private float currentAzimuth;
    private long currentPositionId;

    public static LocationManager loc;
    public HinetManager(Context context, String APPLICATION_ID) {
        this.app = (Application)context.getApplicationContext();

        sensorManager = (SensorManager) this.app.getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);

        EventBus.getDefault().register(this);

        loc = new LocationManager.Builder().setLog(true).build();

        // TODO: 2016/4/29 notify user this app need bluetooth to fully function
        if (BluetoothAdapter.getDefaultAdapter() == null) return;

        // TODO: why does CHT SDK need read phone state to work?
        Dexter.checkPermission(RationalePermissionListener.Builder.with(app)
                .withRunOnGranted(()->enableLocation(APPLICATION_ID)).build(),
                android.Manifest.permission.READ_PHONE_STATE);

    }

    public void unregister() {
        sensorManager.unregisterListener(this);
        EventBus.getDefault().unregister(this);
    }

    private void enableLocation(final String APPLICATION_ID) {
        loc.lastAndUpdate(this.app, true)
                .subscribe(loc -> {
                    Log.d(TAG, "BeaconManager: " + loc.toString());
                    AsyncTask.execute(()-> initCHTLib(this.app, APPLICATION_ID));
                });
    }

    private void initCHTLib(Context context, final String APPLICATION_ID) {
        String licenseKey = "3fb8e5754d3745bbb3c022e76fc8a94d625a2c0691144cc7a";
        BeaconNotifyLib sBeaconNotifyLib =
                with(context, APPLICATION_ID, licenseKey)
                .setBeaconScanPeriod(BEACON_SCAN_PERIOD, BEACON_SCAN_PAUSE, BEACON_SCAN_PERIOD, BEACON_SCAN_PAUSE)
                .init();
        sBeaconNotifyLib
                .setInitListener(new IInitListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "CHT Beacon Library init successfully");
                    }

                    @Override
                    public void onStop() {
                        Log.d(TAG, "CHT Beacon Library init Stop");
                    }

                    @Override
                    public void onError(int errorType, String errorMsg) {
                        Log.d(TAG, "CHT Beacon Library init error: " + errorMsg);
                    }
                });
    }

    private BeaconNotifyLib with(Context context, String applicationId, String licenseKey) {
        if(singletonInstance == null) {
            synchronized(BeaconNotifyLib.class) {
                if(singletonInstance == null) {
                    Context applicationContext = context.getApplicationContext();
                    List modules = (new ManifestParser(applicationContext)).parse();
                    BeaconLibBuilder builder = new BeaconLibBuilder(applicationContext, applicationId, licenseKey);
                    Iterator var7 = modules.iterator();

                    while(var7.hasNext()) {
                        BeaconLibModule module = (BeaconLibModule)var7.next();
                        module.applyOptions(applicationContext, builder);
                    }

                    singletonInstance = builder.build();
                }
            }
        }

        return singletonInstance;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (sensorEventSkip++ < 10) return;
        setAzimuth(event);
        sensorEventSkip = 0;
    }

    private void setAzimuth(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;

        if (mGravity == null || mGeomagnetic == null) return;

        float R[] = new float[9];
        float I[] = new float[9];
        if (!SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)) return;

        float orientation[] = new float[3];
        SensorManager.getOrientation(R, orientation);
        currentAzimuth = (float) (Math.toDegrees(orientation[0]) + 360) % 360;
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onPositionBeaconEvent(PositionBeaconEvent event) {
//        SquidCursor<PushMessage> c = app.db.query(PushMessage.class, Query.select());
//        while (c.moveToNext()) {
//            PushMessage msg = new PushMessage(c);
//            Log.d(TAG, String.format("%s:%s - %s", msg.getName(), msg.getStartTime(), msg.getEndTime()));
//        }

        if (event.sBeaconId == currentPositionId) return;
        currentPositionId = event.sBeaconId;
//        Beacon beacon = this.app.db.getMatchBeacon(event.sBeaconId, event.RDistance, currentAzimuth);
//        if (beacon == null) return;
        Log.d(TAG, String.format("Found beacon data matching id:%d, distance:%d, azimuth:%f",
                event.sBeaconId, event.RDistance, currentAzimuth));
    }

//    private void sendNotification(PushMessage msg, boolean vibrate) {
//        // TODO: 2016/8/15 block by query
//        boolean receiveEnvironmentMsg = App.pref.getBoolean(Keys.PREF_ENVIRONMENT, true);
//        boolean receivePromoteMsg = App.pref.getBoolean(Keys.PREF_PROMOTE, true);
//        boolean isEnvironmentMsg = msg.getCategory().equals(環境.ordinal());
//        if (isEnvironmentMsg && !receiveEnvironmentMsg) return;
//        if (!isEnvironmentMsg && !receivePromoteMsg) return;
//
//        String title = msg.getName();
//        String description = msg.getDescription();
//        if (msg.getCategory() == 環境.ordinal())
//            title = app.getString(R.string.you_are_at, title);
////        if (msg.getCategory() == 遊戲.ordinal()) {
////            title = app.getString(R.string.app_name);
////            description = app.getString(R.string.notify_discount_token);
////        }
//        sendNotification(app, createClickIntent(msg),
//                (int) msg.getId(), title, description, vibrate);
//        Log.d(TAG, "triggerMessages: notification sent");
//    }

//    private Intent createClickIntent(PushMessage msg) {
//        Intent clickIntent = new Intent(app, MainActivity.class);
//        clickIntent.putExtra(Keys.PUSH_MESSAGE_UID, msg.getUId());
//        return clickIntent;
//    }

    @Override
    public void onFlushCompleted(Sensor sensor) {

    }

    public static class PositionBeaconReceiver extends BroadcastReceiver {

        private final String TAG = PositionBeaconReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                //=== 這個事件感測的 Beacon 的ID @ ===//
                long beaconId = intent.getExtras().getLong("BeaconId");
                //=== 這個事件感測的 Beacon 所在地圖中的位置 @ ===//
                double beaconPositionGpsX = intent.getExtras().getDouble("BeaconGpsX");
                double beaconPositionGpsY = intent.getExtras().getDouble("BeaconGpsY");
                //=== 這個事件的資訊 @ ===//
                long sBeaconId = intent.getExtras().getLong("SBeaconID");
                int RDistance = intent.getExtras().getInt("RDistance");
                int RSSI = intent.getExtras().getInt("RSSI");
                //===
                String userDefinedData = intent.getExtras().getString("UserDefineData");

                Log.d(TAG, String.format("CHT triggered position beacon event\nid:%d, (x,y):(%f,%f), sBeaconId:%d, distance:%d, RSSI:%d, user defined data:%s",
                        beaconId, beaconPositionGpsX, beaconPositionGpsY, sBeaconId, RDistance, RSSI, userDefinedData));

                EventBus.getDefault().post(new PositionBeaconEvent(sBeaconId, RDistance));
            }
        }
    }

    public static class PositionBeaconEvent {
        public final long sBeaconId;
        public final int RDistance;

        public PositionBeaconEvent(long sBeaconId, int RDistance) {
            this.sBeaconId = sBeaconId;
            this.RDistance = RDistance;
        }
    }
}
