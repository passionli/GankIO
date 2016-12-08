//package com.liguang.imageloaderdemo.db;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import android.content.ContentResolver;
//import android.content.ContentValues;
//import android.database.Cursor;
//import android.net.Uri;
//import android.os.AsyncTask;
//
//import com.tplink.wearablecamera.app.UserContext;
//import com.tplink.wearablecamera.app.UserInfo;
//import com.tplink.wearablecamera.app.WearableCameraApplication;
//import com.tplink.wearablecamera.core.beans.DevInfo;
//import com.tplink.wearablecamera.core.beans.LatestDevInfo;
//import com.tplink.wearablecamera.core.beans.DevInfo.DevType;
//import com.tplink.wearablecamera.util.TPLog;
//
//public class DatabaseProxy {
//
//    private static final String TAG = DatabaseProxy.class.getSimpleName();
//
//    public static void loadUserInfo() {
//        TPLog.d(TAG, "loadUserInfo");
//        new AsyncTask<Void, Void, UserInfo>() {
//            @Override
//            protected UserInfo doInBackground(Void... params) {
//                UserInfo userInfo = null;
//                ContentResolver resolver = WearableCameraApplication
//                        .getInstance().getContentResolver();
//                Cursor cursor = resolver.query(
//                        GankIoContract.User.CONTENT_URI, null, null,
//                        null, null);
//                try {
//                    if (cursor.moveToFirst()) {
//                        String userId = cursor
//                                .getString(cursor
//                                        .getColumnIndex(GankIoContract.User.USER_ID));
//                        String userName = cursor
//                                .getString(cursor
//                                        .getColumnIndex(GankIoContract.User.USER_NAME));
//                        long updatedTime = cursor
//                                .getLong(cursor
//                                        .getColumnIndex(GankIoContract.User.UPDATED));
//                        userInfo = new UserInfo(userId, userName, updatedTime);
//                    }
//                } finally {
//                    cursor.close();
//                }
//
//                return userInfo;
//            }
//
//            @Override
//            protected void onPostExecute(UserInfo result) {
//                super.onPostExecute(result);
//                TPLog.d(TAG, "onPostExecute " + result);
//                if (result != null) {
//                    UserContext userContext = new UserContext(result);
//                    WearableCameraApplication.getInstance().setUserContext(
//                            userContext);
//                } else {
//                    TPLog.d(TAG, "UserContext is not set.");
//                }
//            }
//        }.execute();
//    }
//
//    public static void insertUser(final String userId, final String userName) {
//        new AsyncTask<Void, Void, Uri>() {
//            @Override
//            protected Uri doInBackground(Void... params) {
//                ContentResolver resolver = WearableCameraApplication
//                        .getInstance().getContentResolver();
//                ContentValues values = new ContentValues();
//                values.put(GankIoContract.User.USER_ID, userId);
//                values.put(GankIoContract.User.USER_NAME, userName);
//                values.put(GankIoContract.User.UPDATED,
//                        System.currentTimeMillis());
//                // write
//                Uri uri = resolver.insert(
//                        GankIoContract.User.CONTENT_URI, values);
//                return uri;
//            }
//
//            protected void onPostExecute(Uri result) {
//                if (result != null) {
//                    DatabaseProxy.loadUserInfo();
//                } else {
//
//                }
//            };
//        }.execute();
//    }
//
//    public static LatestDevInfo loadLatestDevInfo(UserContext userContext) {
//        LatestDevInfo latestDevInfo = new LatestDevInfo();
//
//        List<DevInfo> devInfos = loadLatestUsedDevice(userContext);
//        for (DevInfo devInfo : devInfos) {
//            if (devInfo.isSmartCharger() && latestDevInfo.dock == null) {
//                latestDevInfo.dock = devInfo;
//            } else if ((devInfo.isTcpCam() || (devInfo.isHttpCam()))
//                    && latestDevInfo.camera == null) {
//                latestDevInfo.camera = devInfo;
//            }
//
//            if (latestDevInfo.camera != null && latestDevInfo.dock != null) {
//                break;
//            }
//        }
//        return latestDevInfo;
//    }
//
//    public static List<DevInfo> loadLatestUsedDevice(UserContext userContext) {
//        List<DevInfo> result = new ArrayList<DevInfo>();
//        if (userContext == null) {
//            return result;
//        }
//
//        UserInfo userInfo = userContext.getUserInfo();
//        ContentResolver resolver = WearableCameraApplication.getInstance()
//                .getContentResolver();
//        Cursor cursor = resolver.query(GankIoContract.User
//                .buildDeviceDirUri(userInfo.getUserId()), null, null, null,
//                GankIoContract.Item.UPDATED + " desc ");
//        try {
//            if (cursor != null && cursor.moveToFirst()) {
//                do {
//                    DevInfo devInfo = new DevInfo();
//                    devInfo.ssid = cursor
//                            .getString(cursor
//                                    .getColumnIndex(GankIoContract.Item.DEVICE_SSID));
//                    devInfo.deviceId = cursor
//                            .getString(cursor
//                                    .getColumnIndex(GankIoContract.Item.DEVICE_ID));
//                    devInfo.btMac = cursor
//                            .getString(cursor
//                                    .getColumnIndex(GankIoContract.Item.DEVICE_BT_ADDR));
//                    devInfo.type = DevType
//                            .valueOf(cursor.getString(cursor
//                                    .getColumnIndex(GankIoContract.Item.DEVICE_TYPE)));
//                    devInfo.setMinusWlanMac(cursor.getString(cursor
//                            .getColumnIndex(GankIoContract.Item.DEVICE_WLAN_MAC)));
//                    devInfo.updatedTime = cursor
//                            .getLong(cursor
//                                    .getColumnIndex(GankIoContract.Item.UPDATED));
//                    devInfo.swVersion = cursor
//                            .getString(cursor
//                                    .getColumnIndex(GankIoContract.Item.DEVICE_SOFTWARE_VERSION));
//
//                    devInfo.name = cursor
//                            .getString(cursor
//                                    .getColumnIndex(GankIoContract.Item.DEVICE_NAME));
//                    devInfo.description = cursor
//                            .getString(cursor
//                                    .getColumnIndex(GankIoContract.Item.DEVICE_DESCRIPTION));
//                    devInfo.productId = cursor
//                            .getInt(cursor
//                                    .getColumnIndex(GankIoContract.Item.DEVICE_PRODUCT_ID));
//                    devInfo.hwId = cursor
//                            .getString(cursor
//                                    .getColumnIndex(GankIoContract.Item.DEVICE_HARDWARE_ID));
//                    devInfo.hwVersion = cursor
//                            .getString(cursor
//                                    .getColumnIndex(GankIoContract.Item.DEVICE_HARDWARE_VERSION));
//                    // TODO load other fields if necessary
//                    result.add(devInfo);
//                } while (cursor.moveToNext());
//            }
//        } finally {
//            if (cursor != null && !cursor.isClosed()) {
//                cursor.close();
//                cursor = null;
//            }
//        }
//
//        return result;
//    }
//}
