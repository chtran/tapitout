package com.example.android.beam;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ReceiverPollingService extends Service {
	
	Thread t;
	
	@Override
	public void onCreate() {
		
	}

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
    }

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
