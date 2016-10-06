package ourcitylove.org.ourcitylove_beacon_app;


import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.gson.Gson;

public class BeaconReceiver extends ResultReceiver {
    private static final String TAG = BeaconReceiver.class.getSimpleName();
    private static final Gson gson = new Gson();
    private final Application app;

    public BeaconReceiver(Handler handler, Application app) {
        super(handler);
        this.app = app;
    }

    @Override
    protected void onReceiveResult(int resultCode, final Bundle resultData) {
        if (resultCode == 300) {
            String beaconJson = resultData.getString("beaconJson");
            Log.d(TAG, "onReceiveResult: "+beaconJson);
            resultData.putSerializable(Keys.LABEL, Keys.IDEA);
//            AppBeacon beacon = gson.fromJson(beaconJson, AppBeacon.class);
//            triggerMsgs(getMatchedMsgs(beacon));
        }
    }

//    private SquidCursor<PushMsg> getMatchedMsgs(AppBeacon beacon) {
//        String dateString = new SimpleDateFormat(PushMsg.DATE_FORMAT, Locale.TAIWAN).format(new Date());
//
//        return app.sqlite.query(PushMsg.class,
//                Query.select(Arrays.asList(PushMsg.PROPERTIES))
//                        .innerJoin(BeaconPushMsg.TABLE, BeaconPushMsg.PUSH_MSG_ID.eq(PushMsg.ID))
//                        .innerJoin(Beacon.TABLE, Beacon.ID.eq(BeaconPushMsg.BEACON_ID))
//                        .where(
//                                Beacon.MAJOR.eq(beacon.getMajor().intValue())
//                            .and(Beacon.MINOR.eq(beacon.getMinor().intValue()))
//                            .and(PushMsg.STATUS.eq("上架"))
//                            .and(PushMsg.START_TIME.lte(dateString))
//                            .and(PushMsg.END_TIME.gte(dateString))
//                            .and(PushMsg.RECEIVED.isNull().or(PushMsg.RECEIVED.isFalse()))
//                        ));
//    }
}
