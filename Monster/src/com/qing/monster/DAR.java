package com.qing.monster;


import android.app.admin.DeviceAdminReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class DAR extends DeviceAdminReceiver {

        public static ComponentName getCn(Context context){
                return new ComponentName(context, DAR.class);
        }

        @Override
        public void onEnabled(Context context, Intent intent) {
                // TODO Auto-generated method stub
                super.onEnabled(context, intent);
        }

        @Override
        public void onDisabled(Context context, Intent intent) {
                // TODO Auto-generated method stub
                super.onDisabled(context, intent);
        }
}