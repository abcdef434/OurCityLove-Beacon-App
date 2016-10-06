package ourcitylove.org.ourcitylove_beacon_app;

import android.Manifest;
import android.content.Context;
import android.location.Location;

import com.karumi.dexter.Dexter;

import org.greenrobot.eventbus.Subscribe;

import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;
import ourcitylove.org.ourcitylove_beacon_app.hinet.HinetManager;
import rx.Observable;
import rx.Subscriber;

@SuppressWarnings("unused")
public class LocationManager {
    private static final double EARTH_RADIUS = 3958.75;
    private static final int METER_CONVERSION = 1609;

    private SmartLocation.LocationControl lc;

    public LocationParams locParams;
    public String permissionMsg;
    public boolean log;
    public LocationGooglePlayServicesProvider lp;

    public LocationManager(LocationParams locParams, String permissionMsg, boolean log) {
        this.locParams = locParams;
        this.permissionMsg = permissionMsg;
        this.log = log;
        this.lp = new LocationGooglePlayServicesProvider();
        this.lp.setCheckLocationSettings(true);
    }

    public Observable<Location> lastAndUpdate(Context context) {
        return lastAndUpdate(context, false);
    }

    public Observable<Location> lastAndUpdate(Context context, boolean oneFix) {
        return HinetManager.loc.last(context)
                .concatWith(HinetManager.loc.update(context, oneFix))
                .filter(loc -> loc !=null );
//        return Observable.concat(update, last)
//                .filter(location -> location != null);
    }

    public Observable<Location> last(Context context) {
        return Observable.just(new SmartLocation.Builder(context)
                .logging(log).build().location(lp)
                .getLastLocation());

    }
    
    public Observable<Location> update(Context context) {
        return update(context, false);
    }

    public Observable<Location> update(Context context, boolean oneFix) {
        return Observable.create(subscriber -> {
            if (Dexter.isRequestOngoing()) return;
            Dexter.checkPermission(
                    RationalePermissionListener.Builder.with(context)
                            .withRationaleMsg(permissionMsg)
                            .withRunOnGranted(() -> startUpdateLocation(subscriber, context, oneFix))
                            .build(),
                    Manifest.permission.ACCESS_FINE_LOCATION
            );
        });
    }

    private void startUpdateLocation(Subscriber<? super Location> subscriber, Context context, boolean oneFix) {
        lc = new SmartLocation.Builder(context)
                .logging(log).build().location(lp).config(locParams);
        if (oneFix) lc = lc.oneFix();
        lc.start(subscriber::onNext);
    }

    public void stop() {
        if (lc != null) lc.stop();
    }

    public static class Builder {

        private LocationParams locParams;
        private String permissionMsg;
        private boolean log;

        public Builder() {
            this.locParams = new LocationParams.Builder()
                    .setAccuracy(LocationAccuracy.LOW)
                    .setDistance(0f)
                    .setInterval(5000).build();
        }

        public Builder setLocParams(LocationParams locParams) {
            this.locParams = locParams;
            return this;
        }

        public Builder setPermissionMsg(String permissionMsg) {
            this.permissionMsg = permissionMsg;
            return this;
        }

        public Builder setLog(boolean log) {
            this.log = log;
            return this;
        }

        public LocationManager build() {
            return new LocationManager(locParams, permissionMsg, log);
        }
    }

    public static double getDistance(double lat_a, double lng_a, double lat_b, double lng_b) {

        double latDiff = Math.toRadians(lat_b - lat_a);
        double lngDiff = Math.toRadians(lng_b - lng_a);
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2)
                + Math.cos(Math.toRadians(lat_a))
                * Math.cos(Math.toRadians(lat_b)) * Math.sin(lngDiff / 2)
                * Math.sin(lngDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c * METER_CONVERSION;
    }

    public static String getDisplayDistance(Context context, Double fDistance) {
        if(fDistance == null || fDistance <= 0 || fDistance>5000000) return null;

        boolean far = fDistance >= 1000;
        String unit = context.getString(far ? R.string.unit_kilometer : R.string.unit_meter);
        String format = far ? "%.1f%s" : "%.0f%s";
        if (far) fDistance /= 1000;
        return String.format(format, fDistance, unit);
    }
}
