package ourcitylove.org.ourcitylove_beacon_app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import java.util.UUID;

/**
 * Created by Vegetable on 2016/10/5.
 */

public class Notification {
    private static Notification me;
    private static Context Context;
    private static String ContentTitle = "";
    private static String ContentText = "";
    @ColorRes private static int Color = R.color.colorPrimary;
    @DrawableRes private static int SmallIcon;
    private static Intent mIntent;

    private static int Id;



    public static Notification init(Context ctx){
        me = me==null ? me=new Notification().init(ctx):me;
        me.Context = ctx;
        return me;
    }


    public static Notification setContentTitle(String contentTitle) {
        me.ContentTitle = contentTitle;
        return me;
    }

    public static Notification setContentText(String contentText) {
        me.ContentText = contentText;
        return me;
    }

    public static Notification setColor(int color) {
        me.Color = color;
        return me;
    }

    public static Notification setSmallIcon(int smallIcon) {
        me.SmallIcon = smallIcon;
        return me;
    }

    public static Notification setmIntent(Intent mIntent) {
        me.mIntent = mIntent;
        return me;
    }

    public static Notification setId(int id) {
        me.Id = id;
        return me;
    }

    public static void build(){
        mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(me.Context, UUID.randomUUID().hashCode(), me.mIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        android.app.Notification notification = new NotificationCompat.Builder(me.Context)
                .setContentTitle(me.ContentTitle)
                .setContentText(me.ContentText)
                .setColor(ContextCompat.getColor(me.Context, Color))
                .setSmallIcon(me.SmallIcon)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(null)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) me.Context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(me.Id, notification);
    }
}
