package com.qing.monster;

import java.io.IOException;


import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;

public class WifiCountService extends Service {
	public static final String ACTION = "com.qing.monster.WifiCountService";
	public static final String ACTION_IsGPRS = "com.qing.monster.WifiCountService1";
	public static final String ACTION_GPRS_FAILED = "com.qing.monster.WifiCountService2";
	public static boolean DOWNLOAD = true;
	private WifiCountThread wifiThread = null;
	private TrafficStats wifi_count;
	// 管理wifi和GPRS的开关
	private NetworkManager netWorkManager = null;
	private long Base_Gprs_Send = 0;
	private long Target_Gprs = 0;
	private long wifi_toUse = 0;
	private String wifi_website;

	// 震动器
	private Vibrator vibrator;
	// 广播类
	private downloadBroadcastReceiver donwBroadcastReceiver = new downloadBroadcastReceiver();

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		System.out.println("onBind");
		Base_Gprs_Send = arg0.getLongExtra("Base_Gprs_Count", 0);
		Target_Gprs = arg0.getLongExtra("Target_Gprs", 0);
		wifi_toUse = Long.parseLong(arg0.getStringExtra("wifi_count"));
		wifi_website = arg0.getStringExtra("wifi_website");
		// activity = (MainActivity)
		// arg0.getSerializableExtra(MainActivity.SER_KEY);
		System.out.println("Target_Gprs--->" + Target_Gprs);
		System.out.println("Base_Gprs_Send--->" + Base_Gprs_Send);
		// qing = WifiCountService.activity;
		Message msg = new Message();
		msg.obj = true;
		ServiceMsgHandler.sendMessage(msg);
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		System.out.println("onCreate");

		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

		if (wifiThread == null) {
			netWorkManager = new NetworkManager(WifiCountService.this);
			wifiThread = new WifiCountThread();
			wifiThread.start();
		} else
			System.out.println("wifiThread is already started...");
		// 注册广播接收器
		IntentFilter filter = new IntentFilter();
		// 设置接收广播的类型，这里要和Service里设置的类型匹配，还可以在AndroidManifest.xml文件中注册
		filter.addAction(MainActivity.ACTION);
		this.registerReceiver(donwBroadcastReceiver, filter);
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		// System.out.println("onDestroy");
		this.unregisterReceiver(donwBroadcastReceiver);
		if (wifiThread != null) {
			wifiThread.interrupt();
			wifiThread = null;
		}
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		System.out.println("onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	private class WifiCountThread extends Thread {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			// 先关闭WIFI 启动GPRS
			// StartGPRS();
			long pre_send, next_send;
			long pre_receive, next_receive;
			float speed_send, speed_receive;
			boolean IsCount = true;
			while (IsCount) {
				pre_receive = wifi_count.getMobileRxBytes(); // 下载的数据
				pre_send = wifi_count.getMobileTxBytes(); // 上传的数据

				// System.out.println("pre_receive-->" + pre_receive);
				// System.out.println("pre_send-->"+pre_send);
				// 如果超过了预设的流量值 那么关闭GPRS开关。
				if (pre_receive + pre_send - Base_Gprs_Send >= wifi_toUse && wifi_toUse != 0) {
					System.out.println("pre_receive-->" + pre_receive);
					System.out.println("pre_send-->" + pre_send);
					System.out.println("Base_Gprs_Send-->" + Base_Gprs_Send);
					System.out.println("Target_Gprs-->" + Target_Gprs);
					GRPSFinished();
					System.out.println("GPRS run over~~");
					IsCount = false;
				}
				else if (MultiDownloader.a == 3){
					System.out.println("once over!");
					Message msg = new Message();
					msg.obj = true;
					ServiceMsgHandler.sendMessage(msg);
					MultiDownloader.a = 0;
				}

				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				next_receive = wifi_count.getMobileRxBytes(); // 下载的数据
				next_send = wifi_count.getMobileTxBytes(); // 上传的数据
				Intent intent = new Intent();
				speed_send = (next_send - pre_send) / 1024 * 10 / 3;
				speed_receive = (next_receive - pre_receive) / 1024 * 10 / 3;
				intent.putExtra("speed_send", speed_send);
				intent.putExtra("speed_receive", speed_receive);
				intent.putExtra("gprs_send_data", next_send);
				intent.putExtra("gprs_receive_data", next_receive);
				// System.out.println("service speed ---> " + speed);
				// 设置发送广播的类型，可以随便写一个
				intent.setAction(ACTION);
				sendBroadcast(intent);
			}
		}

	}

	// 打开GPRS，关闭wifi
	public void StartGPRS() {
		DOWNLOAD = true;
		System.out.println("starting StartGPRS!!");
		netWorkManager.toggleWiFi(false);
		try {
			netWorkManager.toggleGprs(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Intent intent = new Intent();
		intent.putExtra("IsGprsOn", true);
		intent.setAction(ACTION_IsGPRS);
		sendBroadcast(intent);
	}

	// 关闭GPRS，打开wifi
	public void StopGPRS() {
		DOWNLOAD = false;
		netWorkManager.toggleWiFi(true);
		try {
			netWorkManager.toggleGprs(false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Intent intent = new Intent();
		intent.putExtra("IsGprsOn", false);
		intent.setAction(ACTION_IsGPRS);
		sendBroadcast(intent);
	}

	private void GRPSFinished() {

		StopGPRS();
		vibrator.vibrate(500);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		// 获取电源管理器对象
		PowerManager.WakeLock wl = pm.newWakeLock(
				PowerManager.ACQUIRE_CAUSES_WAKEUP
						| PowerManager.FULL_WAKE_LOCK, "bright");
		// 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
		wl.acquire();
		// 点亮屏幕

		KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		// 得到键盘锁管理器对象
		KeyguardLock kl = km.newKeyguardLock("unLock");
		// 参数是LogCat里用的Tag
		kl.disableKeyguard();
		// 解锁

		/*
		 * 这里写程序的其他代码
		 */

		kl.reenableKeyguard();
		// 重新启用自动加锁
		wl.release();
		// 释放

	}

	// 创建用来接收预期通话时长的广播
	class downloadBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			boolean flag = intent.getBooleanExtra("flag", true);
			Message msg = new Message();
			msg.obj = flag;
			ServiceMsgHandler.sendMessage(msg);
			// System.out.println("msg received. flag is " + flag);
		}

	}

	// 创建消息处理handler
	private Handler ServiceMsgHandler = new Handler() {

		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
			if ((Boolean) msg.obj) {
				System.out.println("wifi_website --> " + wifi_website);
				//MultiDownloader qing = new MultiDownloader(wifi_website, 3);
				MultiDownloader qing = new MultiDownloader();
				try {
					if (qing.start() == false) {
						System.out.println("qing.start() is false " );
						Intent intent = new Intent();
						intent.putExtra("download_status", false);
						intent.setAction(ACTION_GPRS_FAILED);
						sendBroadcast(intent);
						StartGPRS();
						int n = 0;
						while (!netWorkManager.isMobileConnected()) {
							try {
								n = n + 500;
								Thread.sleep(500);
								if (n > 5000) {
									StartGPRS();
									n = 0;
								}
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						qing.start();
						// System.out.println("Handler::handleMessage() MultiDownloader is failed "
						// );
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

	};

}
