package org.ourcitylove.sample;

import android.app.Application;

import com.karumi.dexter.Dexter;

import ourcitylove.org.ourcitylove_beacon_app.ideas.IdeaManager;

public class App extends Application{
    @Override
    public void onCreate() {
        super.onCreate();

        Dexter.initialize(this);
        if(IdeaManager.hasBluetooth())
            IdeaManager.initBeaconService(this);
    }

}
