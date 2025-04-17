/*
 * Copyright (C) 2025 the AxionAOSP Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.chaldeaprjkt.gamespace.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;

import java.util.HashSet;
import java.util.Set;

public class GameSpaceManagerService extends Service {
    private static final String TAG = "GameSpaceManagerService";
    private static final String GAME_LIST_SETTING = "gamespace_game_list";
    private static final String NOTIFICATION_CHANNEL_ID = "gamespace_notif_channel";
    private static final String NOTIFICATION_CHANNEL_NAME = "GameSpace";

    private Handler mHandler;
    private PackageManager mPackageManager;
    private LocalBinder mBinder;
    private PackageChangeReceiver mPackageChangeReceiver;
    private NotificationChannel mChannel;
    private NotificationManager mNotificationManager;
    private ContentResolver mContentResolver;
    private GameListObserver mGameListObserver;

    public class LocalBinder extends Binder {
        public GameSpaceManagerService getService() {
            return GameSpaceManagerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        mBinder = new LocalBinder();
        mHandler = new Handler(Looper.getMainLooper());
        mPackageManager = getPackageManager();
        mPackageChangeReceiver = new PackageChangeReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
        filter.addDataScheme("package");
        registerReceiver(mPackageChangeReceiver, filter);

        mGameListObserver = GameListObserver(mHandler);

        mContentResolver = getContentResolver();
        mContentResolver.registerContentObserver(
                Settings.System.getUriFor(GAME_LIST_SETTING), false, mGameListObserver);

        sanitizeGameList();

        mChannel =
                new NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        NOTIFICATION_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        createNotificationChannel();
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);

        mNotificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID);
        mNotificationManager.cancelAll();
        mNotificationManager = null;
        mChannel = null;

        unregisterReceiver(mPackageChangeReceiver);
        mContentResolver.unregisterContentObserver(mGameListObserver);
        mGameListObserver = null;
        mPackageChangeReceiver = null;
        mContentResolver = null;
        mPackageManager = null;
        mHandler = null;
        mBinder = null;
        super.onDestroy();
    }

    private class PackageChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String packageName = intent.getData().getSchemeSpecificPart();
            if (packageName == null) return;

            if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
                handlePackageAdded(packageName);
            } else if (Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(intent.getAction())) {
                handlePackageRemoved(packageName);
            }
        }
    }

    private void handlePackageAdded(String packageName) {
        if (isGame(packageName)) {
            addToGameSpace(packageName);
        }
    }

    private void handlePackageRemoved(String packageName) {
        removeFromGameSpace(packageName);
    }

    private boolean isGame(String packageName) {
        try {
            ApplicationInfo appInfo =
                    mPackageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            return (appInfo.category == ApplicationInfo.CATEGORY_GAME);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void addToGameSpace(String packageName) {
        String currentList = Settings.System.getString(cr, GAME_LIST_SETTING);
        Set<String> updatedSet = new HashSet<>();
        boolean alreadyExists = false;

        if (currentList != null && !currentList.isEmpty()) {
            String[] entries = currentList.split(";");
            for (String entry : entries) {
                String[] parts = entry.split("=");
                if (parts.length == 2 && isValidMode(parts[1])) {
                    if (parts[0].equals(packageName)) {
                        alreadyExists = true;
                    }
                    updatedSet.add(parts[0] + "=" + parts[1]);
                }
            }
        }

        if (!alreadyExists) {
            updatedSet.add(packageName + "=2");
            String updatedList = String.join(";", updatedSet);
            Settings.System.putString(mContentResolver, GAME_LIST_SETTING, updatedList);
            sendGameAddedNotification(packageName);
        }
    }

    private void removeFromGameSpace(String packageName) {
        String currentList = Settings.System.getString(mContentResolver, GAME_LIST_SETTING);

        if (currentList == null || currentList.isEmpty()) return;

        Set<String> updatedSet = new HashSet<>();
        String[] entries = currentList.split(";");

        for (String entry : entries) {
            String[] parts = entry.split("=");
            if (parts.length == 2 && isValidMode(parts[1])) {
                if (!parts[0].equals(packageName)) {
                    updatedSet.add(parts[0] + "=" + parts[1]);
                }
            }
        }

        String updatedList = String.join(";", updatedSet);
        Settings.System.putString(mContentResolver, GAME_LIST_SETTING, updatedList);
    }

    private boolean isValidMode(String modeStr) {
        return modeStr.equals("1") || modeStr.equals("2") || modeStr.equals("3");
    }

    private void sanitizeGameList() {
        String currentList = Settings.System.getString(mContentResolver, GAME_LIST_SETTING);
        if (currentList == null || currentList.isEmpty()) return;

        Set<String> sanitizedSet = new HashSet<>();
        String[] entries = currentList.split(";");

        for (String entry : entries) {
            int firstEquals = entry.indexOf('=');
            if (firstEquals > 0 && firstEquals < entry.length() - 1) {
                String key = entry.substring(0, firstEquals).trim();
                String value = entry.substring(firstEquals + 1).split("[^0-9]", 2)[0].trim();
                if (isValidMode(value)) {
                    sanitizedSet.add(key + "=" + value);
                }
            }
        }

        String sanitizedList = String.join(";", sanitizedSet);
        Settings.System.putString(mContentResolver, GAME_LIST_SETTING, sanitizedList);
    }

    private class GameListObserver extends ContentObserver {
        public GameListObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            sanitizeGameList();
        }
    }

    private void createNotificationChannel() {
        mChannel.setDescription("GameSpace Notifications");
        mChannel.enableVibration(true);
        mChannel.enableLights(true);
        mNotificationManager.createNotificationChannel(mChannel);
    }

    private void sendGameAddedNotification(String packageName) {
        String appName;
        try {
            appName =
                    mPackageManager
                            .getApplicationLabel(mPackageManager.getApplicationInfo(packageName, 0))
                            .toString();
        } catch (PackageManager.NameNotFoundException e) {
            appName = packageName;
        }

        Notification notification =
                new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                        .setContentTitle(NOTIFICATION_CHANNEL_NAME)
                        .setContentText(appName + " added to GameSpace")
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                        .setAutoCancel(true)
                        .build();

        mNotificationManager.notify(packageName.hashCode(), notification);
    }
}
