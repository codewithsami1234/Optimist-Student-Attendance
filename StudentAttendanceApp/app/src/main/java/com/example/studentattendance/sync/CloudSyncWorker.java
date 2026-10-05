package com.example.studentattendance.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.studentattendance.data.local.AppDatabase;
import com.example.studentattendance.utils.PreferenceManager;

public class CloudSyncWorker extends Worker {

    public CloudSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        PreferenceManager prefs = new PreferenceManager(context);
        AppDatabase db = AppDatabase.getInstance(context);
        PendingSyncStore store = new PendingSyncStore(new PrefsKvStore(context));
        RoomCacheStore cache = new RoomCacheStore(prefs, db);
        CloudSyncEngine engine = new CloudSyncEngine(context, store, cache, db);
        if (prefs.isLocalSession() || !prefs.isCloudSession()) {
            LocalNotificationPublisher.publishUnreadNow(context);
            return Result.success();
        }
        CloudSyncEngine.Outcome outcome = engine.sync();
        if (outcome == CloudSyncEngine.Outcome.RETRY) {
            return Result.retry();
        }
        return Result.success();
    }
}
