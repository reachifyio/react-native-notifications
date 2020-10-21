package com.wix.reactnativenotifications.fcm;

import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.os.SystemClock;
import android.os.Bundle;
import android.util.Log;
import android.app.ActivityManager;

import com.facebook.react.bridge.*;
import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.common.LifecycleState;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.wix.reactnativenotifications.BuildConfig;
import com.wix.reactnativenotifications.core.notification.IPushNotification;
import com.wix.reactnativenotifications.core.notification.PushNotification;
import com.wix.reactnativenotifications.fcm.RNNotificationsHeadlessService;
import com.wix.reactnativenotifications.RNNotificationsModule;

import java.util.*;

import static com.wix.reactnativenotifications.Defs.LOGTAG;

/**
 * Instance-ID + token refreshing handling service. Contacts the FCM to fetch the updated token.
 *
 * @author amitd
 */
public class FcmInstanceIdListenerService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message){
        Bundle bundle = message.toIntent().getExtras();

        // Set id if it doesn't exist
        if (bundle.get("id") == null) {
            bundle.putInt("id", (int) SystemClock.uptimeMillis());
        }

        Log.d(LOGTAG, "New message from FCM: " + bundle);

        try {
            Context context = getApplicationContext();
            Boolean isVoip = bundle.get("Call-ID") != null;
            Boolean isSilent = !isVoip && (bundle.get("title") == null) && (bundle.get("body") == null);

            // Handle notification normally if app is running at all
            if (RNNotificationsModule.active || !(isVoip || isSilent)) {
                final IPushNotification notification = PushNotification.get(getApplicationContext(), bundle);
                notification.onReceived();
                return;
            }

            // App is not running - start a headless task
            Intent intent = new Intent(context, RNNotificationsHeadlessService.class);
            intent.putExtra("message", message);

            if (isVoip) {
              ComponentName name = getApplicationContext().startService(intent);

              if (name != null) {
                  HeadlessJsTaskService.acquireWakeLockNow(getApplicationContext());
              }
              return;
            }
            
            if (isSilent) {
              ComponentName name = getApplicationContext().startForegroundService(intent);
            }
        } catch (IPushNotification.InvalidNotificationException e) {
            // An FCM message, yes - but not the kind we know how to work with.
            if(BuildConfig.DEBUG) Log.v(LOGTAG, "FCM message handling aborted", e);
        }
    }

    public static boolean isAppInForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) return false;
    
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) return false;
    
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
          if (
            appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
              && appProcess.processName.equals(packageName)
          ) {
            ReactContext reactContext;
    
            try {
              reactContext = (ReactContext) context;
            } catch (ClassCastException exception) {
              // Not react context so default to true
              return true;
            }
    
            return reactContext.getLifecycleState() == LifecycleState.RESUMED;
          }
        }
    
        return false;
    }
}
