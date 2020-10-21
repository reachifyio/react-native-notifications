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

        if(BuildConfig.DEBUG) Log.d(LOGTAG, "New message from FCM: " + bundle);

        try {
            Context context = getApplicationContext();
            Log.d(LOGTAG, "Is app in foreground: " + isAppInForeground(context));

            if (isAppInForeground(context) || (bundle.get("Call-ID") == null)) {
                final IPushNotification notification = PushNotification.get(getApplicationContext(), bundle);
                notification.onReceived();
                return;
            }

            Intent intent = new Intent(context, RNNotificationsHeadlessService.class);
            intent.putExtra("message", message);
            ComponentName name = getApplicationContext().startService(intent);

            if (name != null) {
                HeadlessJsTaskService.acquireWakeLockNow(getApplicationContext());
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
