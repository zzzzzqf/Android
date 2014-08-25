package com.qing.monster;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

//�����������㲥
	public class PhoneListenBroadcastReceiver extends BroadcastReceiver{
		//����������
	    private static boolean mIncomingFlag = false;
	    private static String mIncomingNumber = null;
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	//System.out.println("Received calling State!!");
	        //Dial number;
	        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
	        	System.out.println("Received calling State!!------->ACTION_NEW_OUTGOING_CALL");
	            ;//����ǲ��ŵ�ʱ����õĵ��ģ���������û��
	        } else {
	        	//System.out.println("Received calling State!!------->NOT !!ACTION_NEW_OUTGOING_CALL");
	            //Get call;
	            TelephonyManager tManager = (TelephonyManager) context  .getSystemService(Service.TELEPHONY_SERVICE);
        switch (tManager.getCallState()) {
            //�е绰�����
            case TelephonyManager.CALL_STATE_RINGING:
                //mIncomingNumber�����������
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
