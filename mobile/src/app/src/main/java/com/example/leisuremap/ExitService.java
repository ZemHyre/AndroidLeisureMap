package com.example.leisuremap;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import java.util.ArrayList;

public class ExitService extends Service {
    ArrayList<String> IDs;
    ArrayList<String> Types;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        IDs = intent.getStringArrayListExtra("ID");
        Types = intent.getStringArrayListExtra("Type");
        return  START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        System.out.println("onTaskRemoved called");
        super.onTaskRemoved(rootIntent);
        this.stopSelf();
    }
}
