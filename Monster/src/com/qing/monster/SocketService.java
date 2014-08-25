package com.qing.monster;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.text.format.Time;

public class SocketService extends Service {
	public static final String ACTION = "com.qing.monster.SocketService";
	private QingSocket qingSocket = null;
	private String ip = "";
	private int port = 6667;
	private SendTestThread sendTestThread = null;
	private boolean IsSendingFlag = false;
	private SocketBroadcastReceiver socketBroadcastReceiver = new SocketBroadcastReceiver();
	// 震动器
	private Vibrator vibrator;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		System.out.println("On SokcetService ---> On Bind");
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		// 注册广播接收器
		IntentFilter filter = new IntentFilter();
		// 设置接收广播的类型，这里要和Service里设置的类型匹配，还可以在AndroidManifest.xml文件中注册
		filter.addAction(MainActivity.ACTION);
		this.registerReceiver(socketBroadcastReceiver, filter);
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		System.out.println("On SokcetService ---> On Create");
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		// 关闭socket
		if (qingSocket != null)
			qingSocket.Close();
		if (sendTestThread != null) {
			sendTestThread.interrupt();
			sendTestThread = null;
		}
		System.out.println("On SokcetService ---> On Destroy");
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		System.out.println("On SokcetService ---> On StartCommand");
		System.out.println("start to init qingSokcet");
		//ip = "192.168.1.102";
		if (qingSocket == null) {
			qingSocket = new QingSocket(SocketService.this, ip, port);
			// sendTestThread = new SendTestThread("qingfeng Sokcet");
		} else
			System.out.println("Socket is already inited");
		return super.onStartCommand(intent, flags, startId);
	}

	private class SendTestThread extends Thread {

		private String msg = null;

		SendTestThread(String msg) {
			System.out.println("SendTestThread init" + msg);
			this.msg = msg;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			System.out.println("SendTestThread is in run()");
			while (IsSendingFlag) {
				//Time t = new Time(); // or Time t=new Time("GMT+8");
				Time t = new Time("GMT+8"); 
				// 加上Time Zone资料。
				t.setToNow(); // 取得系统时间。
				int year = t.year;
				int month = (t.month + 1) % 12;
				//System.out.println("month --> " + t.month);
				int date = t.monthDay;
				int hour = (t.hour + 8) % 24; // 0-23
				int minute = t.minute;
				int second = t.second;
				String send  = null;
				send = msg +year + "/" + month + "/" + date + "," + hour + "-"
						+ minute + "-" + second;
				//qingSocket.startPing();
				//qingSocket.SendHeartBeat();
				qingSocket.SendMsg(send);
				//vibrator.vibrate(100);
				System.out.println(send);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println("SendTestThread is finished");
			sendTestThread = null;
		}

	}

	// 创建用来接收启动发送信号的广播
	class SocketBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			boolean IsSending;
			IsSending = intent.getBooleanExtra("StartSending", false);
			System.out.println("Received param IsSending --> " + IsSending);
			Message msg = new Message();
			msg.obj = IsSending;
			SocketMsgHandler.sendMessage(msg);
		}

	}

	// 创建消息处理handler
	private Handler SocketMsgHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			IsSendingFlag = (Boolean) msg.obj;
			System.out.println("Received param SocketMsgHandler --> " + IsSendingFlag);
			if (IsSendingFlag && sendTestThread == null) {
				System.out.println("Trying to Start Thread!!");
				sendTestThread = new SendTestThread("qingfeng_Sokcet ");
				sendTestThread.start();
			}
		}

	};
}
