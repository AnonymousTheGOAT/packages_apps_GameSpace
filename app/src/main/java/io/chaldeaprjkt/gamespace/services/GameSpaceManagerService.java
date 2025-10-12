/*
 * SPDX-FileCopyrightText: 2025 the AxionAOSP Project
 * SPDX-FileCopyrightText: 2025 crDroid Android Project
 * SPDX-FileCopyrightText: 2025 TheParasiteProject
 * SPDX-License-Identifier: Apache-2.0
 */

package io.chaldeaprjkt.gamespace.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.PreferenceManager;

import io.chaldeaprjkt.gamespace.R;
import io.chaldeaprjkt.gamespace.utils.ExtensionsKt;
import io.chaldeaprjkt.gamespace.utils.GameModeUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GameSpaceManagerService extends Service {
    private static final String TAG = "GameSpaceManagerService";

    private static final String THREAD_NAME = "GameSpaceManagerThread";
    private static final String GAME_LIST_SETTING = Settings.System.GAMESPACE_GAME_LIST;
    private static final Set<String> VALID_MODES = Set.of("1", "2", "3");
    private static final String MODE_STANDARD = "1";

    private static final String NOTIFICATION_CHANNEL_ID = "gamespace_notif_channel";

    private static final String KEY_ENABLE = "gamespace_enable";

    private Handler mBackgroundHandler;
    private HandlerThread mHandlerThread;
    private PackageManager mPackageManager;
    private LocalBinder mBinder;
    private PackageChangeReceiver mPackageChangeReceiver;
    private NotificationChannel mChannel;
    private NotificationManager mNotificationManager;
    private ContentResolver mContentResolver;
    private GameListObserver mGameListObserver;
    private GameModeUtils mGameModeUtils;
    private SharedPreferences mPrefs;

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
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        mHandlerThread = new HandlerThread(THREAD_NAME);
        mHandlerThread.start();
        mBackgroundHandler = new Handler(mHandlerThread.getLooper());

        mBinder = new LocalBinder();
        mPackageManager = getPackageManager();
        mPackageChangeReceiver = new PackageChangeReceiver();
        mGameModeUtils = new GameModeUtils(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
        filter.addDataScheme("package");
        registerReceiver(mPackageChangeReceiver, filter);

        mGameListObserver = new GameListObserver(mBackgroundHandler);

        mContentResolver = getContentResolver();
        mContentResolver.registerContentObserver(
                Settings.System.getUriFor(GAME_LIST_SETTING),
                false,
                mGameListObserver,
                UserHandle.USER_ALL);

        sanitizeGameList();

        mChannel =
                new NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        getString(R.string.app_name),
                        NotificationManager.IMPORTANCE_LOW);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        createNotificationChannel();
    }

    @Override
    public void onDestroy() {
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
        mBinder = null;

        mHandlerThread.quitSafely();
        mHandlerThread = null;
        mBackgroundHandler = null;

        super.onDestroy();
    }

    private class PackageChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mPrefs == null) {
                try {
                    mPrefs =
                            PreferenceManager.getDefaultSharedPreferences(
                                    GameSpaceManagerService.this);
                } catch (RuntimeException e) {
                    return;
                }
            }
            if (!mPrefs.getBoolean(KEY_ENABLE, true)) {
                return;
            }

            String packageName = intent.getData().getSchemeSpecificPart();
            if (packageName == null) return;

            mBackgroundHandler.post(
                    () -> {
                        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
                            handlePackageAdded(packageName);
                        } else if (Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(intent.getAction())) {
                            handlePackageRemoved(packageName);
                        }
                    });
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

    private String getCurrentList() {
        return Settings.System.getStringForUser(
                mContentResolver, GAME_LIST_SETTING, UserHandle.USER_CURRENT);
    }

    private boolean isGame(String packageName) {
        try {
            ApplicationInfo appInfo =
                    mPackageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            return appInfo != null && appInfo.category == ApplicationInfo.CATEGORY_GAME;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void addToGameSpace(String packageName) {
        final String currentList = getCurrentList();

        Map<String, String> gameMap = new HashMap<>();

        if (currentList != null && !currentList.isEmpty()) {
            String[] entries = currentList.split(";");
            for (String entry : entries) {
                String[] parts = entry.split("=");
                if (parts.length == 2 && isValidMode(parts[1])) {
                    gameMap.put(parts[0], parts[1]);
                }
            }
        }

        if (!gameMap.containsKey(packageName)) {
            gameMap.put(packageName, MODE_STANDARD);
            String updatedList = serializeGameMap(gameMap);
            Settings.System.putStringForUser(
                    mContentResolver, GAME_LIST_SETTING, updatedList, UserHandle.USER_CURRENT);
            sendGameAddedNotification(packageName);
        }

        mGameModeUtils.setupBatteryMode(!gameMap.isEmpty());
    }

    private void removeFromGameSpace(String packageName) {
        final String currentList = getCurrentList();

        if (currentList == null || currentList.isEmpty()) return;

        Map<String, String> gameMap = new HashMap<>();
        String[] entries = currentList.split(";");

        for (String entry : entries) {
            String[] parts = entry.split("=");
            if (parts.length == 2 && isValidMode(parts[1]) && !parts[0].equals(packageName)) {
                gameMap.put(parts[0], parts[1]);
            }
        }

        String updatedList = serializeGameMap(gameMap);
        Settings.System.putStringForUser(
                mContentResolver, GAME_LIST_SETTING, updatedList, UserHandle.USER_CURRENT);

        mGameModeUtils.setupBatteryMode(!gameMap.isEmpty());
    }

    private boolean isValidMode(String modeStr) {
        return VALID_MODES.contains(modeStr);
    }

    private void sanitizeGameList() {
        final String currentList = getCurrentList();

        if (currentList == null || currentList.isEmpty()) return;

        Map<String, String> gameMap = new HashMap<>();
        String[] entries = currentList.split(";");

        for (String entry : entries) {
            int firstEquals = entry.indexOf('=');
            if (firstEquals > 0 && firstEquals < entry.length() - 1) {
                String key = entry.substring(0, firstEquals).trim();
                String value = entry.substring(firstEquals + 1).split("[^0-9]", 2)[0].trim();
                if (isValidMode(value)) {
                    gameMap.put(key, value);
                }
            }
        }

        String sanitizedList = serializeGameMap(gameMap);
        Settings.System.putStringForUser(
                mContentResolver, GAME_LIST_SETTING, sanitizedList, UserHandle.USER_CURRENT);
    }

    private String serializeGameMap(Map<String, String> gameMap) {
        Set<String> result = new HashSet<>();
        for (Map.Entry<String, String> entry : gameMap.entrySet()) {
            result.add(entry.getKey() + "=" + entry.getValue());
        }
        return String.join(";", result);
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
        mChannel.setDescription("Game Mode Service Notifications");
        mChannel.enableVibration(true);
        mChannel.enableLights(true);
        mChannel.setBlockable(true);
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
        final String finalAppName = appName;

        Notification.Builder notification =
                new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.gamespace_new_game_added, finalAppName))
                        .setPriority(NotificationManager.IMPORTANCE_LOW)
                        .setAutoCancel(true);

        LauncherActivityInfo activity = ExtensionsKt.getAppInfoForPackage(this, packageName);
        if (activity != null) {
            Intent intent = new Intent();
            intent.setComponent(activity.getComponentName());
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                            | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

            PendingIntent activityIntent =
                    PendingIntent.getActivityAsUser(
                            this,
                            packageName.hashCode(),
                            intent,
                            PendingIntent.FLAG_IMMUTABLE,
                            null,
                            UserHandle.CURRENT);

            notification.setContentIntent(activityIntent);
        }

        mNotificationManager.notify(packageName.hashCode(), notification.build());
    }
}
