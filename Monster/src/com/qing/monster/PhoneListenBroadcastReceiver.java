package com.qing.monster;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

//创建来电号码广播
	public class PhoneListenBroadcastReceiver extends BroadcastReceiver{
		//来电号码变量
	    private static boolean mIncomingFlag = false;
	    private static String mIncomingNumber = null;
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	//System.out.println("Received calling State!!");
	        //Dial number;
	        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
	        	System.out.println("Received calling State!!------->ACTION_NEW_OUTGOING_CALL");
	            ;//这个是拨号的时候采用的到的，所以这里没用
	        } else {
	        	//System.out.println("Received calling State!!------->NOT !!ACTION_NEW_OUTGOING_CALL");
	            //Get call;
	            TelephonyManager tManager = (TelephonyManager) context  .getSystemService(Service.TELEPHONY_SERVICE);
        switch (tManager.getCallState()) {
            //有电话打进来
            case TelephonyManager.CALL_STATE_RINGING:
                //mIncomingNumber就是来电号码
                mIncomingNumber = intent.getStringExtra("incoming_number");
              //  Toast.makeText(context, mIncomingNumber,
              //          Toast.LENGTH_LONG).show();
                System.out.println("The Incoming number is : " + mIncomingNumber);
                break;
           default:break;
        }
    }
}
}
