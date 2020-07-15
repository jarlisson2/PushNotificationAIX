package com.jdl.PushNotification;

import android.app.Activity;
import android.content.Context;

import com.google.appinventor.components.annotations.androidmanifest.ActionElement;
import com.google.appinventor.components.annotations.androidmanifest.ActivityElement;
import com.google.appinventor.components.annotations.androidmanifest.CategoryElement;
import com.google.appinventor.components.annotations.androidmanifest.IntentFilterElement;
import com.google.appinventor.components.annotations.androidmanifest.MetaDataElement;
import com.google.appinventor.components.annotations.androidmanifest.ReceiverElement;
import com.google.appinventor.components.annotations.androidmanifest.ServiceElement;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesActivities;
import com.google.appinventor.components.annotations.UsesBroadcastReceivers;
import com.google.appinventor.components.annotations.UsesInfoMetaData;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.annotations.UsesServices;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.util.YailDictionary;
import com.google.appinventor.components.runtime.util.YailList;

import com.onesignal.OneSignal.GetTagsHandler;
import com.onesignal.OneSignal.PostNotificationResponseHandler;
import com.onesignal.OneSignal;
import com.onesignal.OSNotification;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OSPermissionSubscriptionState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@DesignerComponent(version = 1, description = "OneSignal Receiver <br> Developed by Jarlisson", category = ComponentCategory.EXTENSION, nonVisible = true, iconName = "aiwebres/notification.png", helpUrl = "https://github.com/jarlisson2/PushNotificationAIX")
// If you want to compile, use my fork
// https://github.com/jarlisson2/appinventor-sources/tree/edi1
@UsesInfoMetaData(metaDataElements = { @MetaDataElement(name = "com.google.android.gms.version", value = "12451000") })
@UsesServices(services = { @ServiceElement(name = "com.onesignal.GcmIntentService"),
        @ServiceElement(name = "com.onesignal.GcmIntentJobService", permission = "android.permission.BIND_JOB_SERVICE"),
        @ServiceElement(name = "com.onesignal.RestoreJobService", permission = "android.permission.BIND_JOB_SERVICE"),
        @ServiceElement(name = "com.onesignal.RestoreKickoffJobService", permission = "android.permission.BIND_JOB_SERVICE"),
        @ServiceElement(name = "com.onesignal.SyncService", stopWithTask = "true"),
        @ServiceElement(name = "com.onesignal.SyncJobService", permission = "android.permission.BIND_JOB_SERVICE"),
        @ServiceElement(name = "com.onesignal.NotificationRestoreService") })
@UsesActivities(activities = {
        @ActivityElement(name = "com.google.android.gms.common.api.GoogleApiActivity", theme = "@android:style/Theme.Translucent.NoTitleBar", exported = "false"),
        @ActivityElement(name = "com.onesignal.PermissionsActivity", theme = "@android:style/Theme.Translucent.NoTitleBar"), })
@UsesBroadcastReceivers(receivers = {
        @ReceiverElement(name = "com.onesignal.GcmBroadcastReceiver", permission = "com.google.android.c2dm.permission.SEND", intentFilters = {
                @IntentFilterElement(priority = "999", actionElements = {
                        @ActionElement(name = "com.google.android.c2dm.intent.RECEIVE") }, categoryElements = {
                                @CategoryElement(name = "com.jdl.push.Push") }) }),
        @ReceiverElement(name = "com.onesignal.NotificationOpenedReceiver"),
        @ReceiverElement(name = "com.onesignal.BootUpReceive", intentFilters = { @IntentFilterElement(actionElements = {
                @ActionElement(name = "android.intent.action.ACTION_BOOT_COMPLETED"),
                @ActionElement(name = "android.intent.action.BOOT_COMPLETED"),
                @ActionElement(name = "android.intent.action.QUICKBOOT_POWERON") }, categoryElements = {
                        @CategoryElement(name = "com.jdl.push.Push") }) }),
        @ReceiverElement(name = "com.onesignal.UpgradeReceiver", intentFilters = {
                @IntentFilterElement(actionElements = {
                        @ActionElement(name = "android.intent.action.MY_PACKAGE_REPLACED") }) }), })
@UsesPermissions(permissionNames = "android.permission.INTERNET, com.google.android.c2dm.permission.RECEIVE, android.permission.WAKE_LOCK, android.permission.VIBRATE, android.permission.ACCESS_NETWORK_STATE, android.permission.RECEIVE_BOOT_COMPLETED, com.sec.android.provider.badge.permission.READ, com.sec.android.provider.badge.permission.WRITE, com.htc.launcher.permission.READ_SETTINGS, com.htc.launcher.permission.UPDATE_SHORTCUT, com.sonyericsson.home.permission.BROADCAST_BADGE, com.sonymobile.home.permission.PROVIDER_INSERT_BADGE, com.anddoes.launcher.permission.UPDATE_COUNT, com.majeur.launcher.permission.UPDATE_BADGE, com.huawei.android.launcher.permission.CHANGE_BADGE, com.huawei.android.launcher.permission.READ_SETTINGS, com.huawei.android.launcher.permission.WRITE_SETTINGS, android.permission.READ_APP_BADGE, com.oppo.launcher.permission.READ_SETTINGS, com.oppo.launcher.permission.WRITE_SETTINGS, me.everything.badger.permission.BADGE_COUNT_READ, me.everything.badger.permission.BADGE_COUNT_WRITE")
@UsesLibraries("OneSignal.jar")
@SimpleObject(external = true)

public class PushNotification extends AndroidNonvisibleComponent {
    public Activity activity;
    public Context context;

    public PushNotification(final ComponentContainer container) {
        super(container.$form());
        context = (Context) container.$context();

    }

    private class NotificationReceivedHandler implements OneSignal.NotificationReceivedHandler {
        @Override
        public void notificationReceived(final OSNotification notification) {
            final String id = notification.payload.notificationID;
            final String title = notification.payload.title;
            final String body = notification.payload.body;
            NotificationReceived(id == null ? "" : id, title == null ? "" : title, body == null ? "" : body);

        }
    }

    private class NotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
        @Override
        public void notificationOpened(final OSNotificationOpenResult openedResult) {
            final String id = openedResult.notification.payload.notificationID;
            final String title = openedResult.notification.payload.title;
            final String body = openedResult.notification.payload.body;
            NotificationOpened(id == null ? "" : id, title == null ? "" : title, body == null ? "" : body);

        }
    }

    @DesignerProperty(defaultValue = "", editorType = "string")
    @SimpleProperty(description = "One Signal App ID", userVisible = false)
    public final void AppId(final String appId) {
        OneSignal.init(context, "1234567", appId, new NotificationOpenedHandler(), new NotificationReceivedHandler());
    }

    @SimpleProperty(description = "Gets the subscription id.")
    public final String GetUserId() {
        try {
            final OSPermissionSubscriptionState permissionSubscriptionState = OneSignal
                    .getPermissionSubscriptionState();
            final OSPermissionSubscriptionState oSPermissionSubscriptionState = permissionSubscriptionState;
            if (permissionSubscriptionState.getSubscriptionStatus().getUserId() == null) {
                return "-1";
            }
            return oSPermissionSubscriptionState.getSubscriptionStatus().getUserId();
        } catch (final Exception e) {
            return "-1";
        }
    }

    @SimpleProperty(description = "Enable vibration when receiving notification. (DEFAULT: TRUE)")
    public final void EnableVibrate(final boolean enable) {
        OneSignal.enableVibrate(enable);
    }

    @SimpleProperty(description = "Play sound when receiving notification. (DEFAULT: TRUE)")
    public final void EnableSound(final boolean enable) {
        OneSignal.enableSound(enable);
    }

    @SimpleProperty(description = "Setting to control how OneSignal notifications will be shown when one is received while your app is in focus. (DEFAULT: 1)")
    public final void SetInFocusDisplaying(final int focus) {
        OneSignal.setInFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification);
    }

    @SimpleFunction(description = "Clear onesignal notifications.")
    public final void ClearOneSignalNotifications() {
        OneSignal.clearOneSignalNotifications();
    }

    @SimpleFunction(description = "Send the tags.")
    public final void SendTags(final YailDictionary tags) {
        try {
            JSONObject tagsJson;
            tagsJson = new JSONObject(tags.toString());
            OneSignal.sendTags(tagsJson);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    @SimpleFunction(description = "Gets the tags, returning it in \"TagsAvailable\".")
    public final void GetTags() {
        OneSignal.getTags(new GetTagsHandler() {
            @Override
            public void tagsAvailable(final JSONObject tags) {
                try {
                    TagsAvailable(YailDictionary.makeDictionary(toMap(tags)));
                } catch (final JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @SimpleFunction(description = "Deletes one or more tags that were previously set on a user.")
    public final void DeleteTags(final YailList tags) {
        try {
            final String[] tagsList = tags.toStringArray();
            final Collection<String> list = new ArrayList<String>();
            for (final String tag : tagsList)
                list.add(tag);
            OneSignal.deleteTags(list);
        } catch (final Throwable t) {
            t.printStackTrace();
        }

    }

    @SimpleFunction(description = "Send a notification.")
    public final void PostNotification(final YailDictionary notification) {
        try {
            final JSONObject jo = new JSONObject(notification.toString());
            OneSignal.postNotification(jo, new PostNotificationResponseHandler() {
                @Override
                public void onSuccess(final JSONObject response) {
                    try {
                        OnSuccess(YailDictionary.makeDictionary(toMap(response)));
                    } catch (final JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(final JSONObject response) {
                    try {
                        OnFailure(YailDictionary.makeDictionary(toMap(response)));
                    } catch (final JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    @SimpleEvent(description = "Returns the tags.")
    public void TagsAvailable(final YailDictionary tags) {
        EventDispatcher.dispatchEvent(this, "TagsAvailable", tags);
    }

    @SimpleEvent(description = "Event triggered when receiving any notification.")
    public void NotificationReceived(final String id, final String title, final String payload) {
        EventDispatcher.dispatchEvent(this, "NotificationReceived", id, title, payload);
    }

    @SimpleEvent(description = "Event triggered when opening the notification.")
    public void NotificationOpened(final String id, final String title, final String payload) {
        EventDispatcher.dispatchEvent(this, "NotificationOpened", id, title, payload);
    }

    @SimpleEvent(description = "Returns if successful in sending the message.")
    public void OnSuccess(final YailDictionary response) {
        EventDispatcher.dispatchEvent(this, "OnSuccess", response);
    }

    @SimpleEvent(description = "Returns if there was an error sending the message.")
    public void OnFailure(final YailDictionary response) {
        EventDispatcher.dispatchEvent(this, "OnFailure", response);
    }

    public static Map<Object, Object> toMap(final JSONObject jsonobj) throws JSONException {
        final Map<Object, Object> map = new HashMap<Object, Object>();
        final Iterator<String> keys = jsonobj.keys();
        while (keys.hasNext()) {
            final String key = keys.next();
            Object value = jsonobj.get(key);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(final JSONArray array) throws JSONException {
        final List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

}