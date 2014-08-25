package com.qing.monster;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

import com.android.internal.telephony.ITelephony;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

public class CallDurationService extends Service {
	// 广播的ACTION
	public static final String ACTION = "com.qing.Monster.CallDurationService";
	// 发射类，用来结束通话，会导致来电号码未知的问题
	private ITelephony iTelephony;
	// 电话状态管理类
	private TelephonyManager manager;
	// 监视Logcat的线程
	private MonitorThread monitorThread = null;
	// 震动器
	private Vibrator vibrator;
	// 预期的通话时长
	private int WantedCallDuration = 0;
	// 广播类
	private SetWantedCallDurationBroadcastReceiver setWantedCallDurationBroadcastReceiver = new SetWantedCallDurationBroadcastReceiver();

	
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		System.out.println("service onCreate starting");
		
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		// vibrator.vibrate(1000);
		manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		
		System.out.println("service onCreate manager.listen");
		
/*		//获取来电号码
		manager.listen(new PhoneStateListener(){

			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				// TODO Auto-generated method stub
				super.onCallStateChanged(state, incomingNumber);
				//System.out.println("service onCreate manager.listen --- > onCallStateChanged");
				switch (state) {
				case TelephonyManager.CALL_STATE_IDLE:
					System.out.println("service onCreate manager.listen --- > CALL_STATE_IDLE");
					break;
				case TelephonyManager.CALL_STATE_RINGING:
					System.out.println("service onCreate manager.listen --- > CALL_STATE_RINGING");
					System.out.println("phone num is ---> "
							+ incomingNumber);
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					System.out.println("service onCreate manager.listen --- > CALL_STATE_OFFHOOK");
					break;
				default:
					System.out.println("NO SUCH PHONE STATE!!");
				}
			}
			
		}, PhoneStateListener.LISTEN_CALL_STATE);
*/
		// 注册广播接收器
		IntentFilter filter = new IntentFilter();
		// 设置接收广播的类型，这里要和Service里设置的类型匹配，还可以在AndroidManifest.xml文件中注册
		filter.addAction(MainActivity.ACTION);
		this.registerReceiver(setWantedCallDurationBroadcastReceiver, filter);
		System.out.println("service onCreate ending");
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		// manager = (TelephonyManager)
		// intent.getSerializableExtra("TelManager");
		// vibrator = (Vibrator) intent.getSerializableExtra("Vibrator");
		if (monitorThread == null) {
			/*
			 * Intent intent1 = new Intent(); intent1.putExtra("test",
			 * "caonima"); //设置发送广播的类型，可以随便写一个 intent1.setAction(ACTION);
			 * sendBroadcast(intent1);
			 */
			monitorThread = new MonitorThread(vibrator, manager);
			monitorThread.start();
		} else
			System.out.println("monitorThread is already started...");
		System.out.println("service onStartCommand");

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		this.unregisterReceiver(setWantedCallDurationBroadcastReceiver);
		System.out.println("service onDestroy");
		if (monitorThread != null) {
			monitorThread.interrupt();
			monitorThread = null;
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub

		System.out.println("service onBind");
		return null;
	}

	// 接通震动
	class MonitorThread extends Thread {
		// 振动器
		Vibrator mVibrator;
		// 电话服务
		TelephonyManager telManager;

		public MonitorThread(Vibrator mVibrator, TelephonyManager telManager) {
			// public MonitorThread(TelephonyManager telManager) {
			this.mVibrator = mVibrator;
			this.telManager = telManager;
		}

		@Override
		public void run() {

			System.out.println("thread is starting!!!");
			// 获取当前话机状态
			int callState = telManager.getCallState();
			phoner();
			Log.i("TestService", "开始.........."+ Thread.currentThread().getName());
			// 记录拨号开始时间
			long threadStart = System.currentTimeMillis();
			Process process;
			InputStream inputstream;
			BufferedReader bufferedreader;
			System.out.println("before try!!!");
			try {
				System.out.println("into ing try!!!");
				process = Runtime.getRuntime().exec("logcat -v time -b radio");
				inputstream = process.getInputStream();
				InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
				bufferedreader = new BufferedReader(inputstreamreader);
				String str = "";
				long dialingStart = 0;
				boolean enableVibrator = false;
				boolean isAlert = false;
				System.out.println("before while!!!");
				str = bufferedreader.readLine();
				System.out.println("bufferedreader.readLine()------>" + str);
				//震动手机，以表示Service已启动
				mVibrator.vibrate(100);
				while ((str = bufferedreader.readLine()) != null) {
				
					if(str.contains("GET_CURRENT_CALLS"))
						Util.log(str);
					if (str.contains("GET_CURRENT_CALLS")
							&& str.contains("DIALING")) {
						// 当DIALING开始并且已经经过ALERTING或者首次DIALING
						if (!isAlert || dialingStart == 0) {
							// 记录DIALING状态产生时间
							dialingStart = System.currentTimeMillis();
							isAlert = false;
							Util.log(str);
						}
						continue;
					}
					if (str.contains("GET_CURRENT_CALLS")
							&& str.contains("ALERTING") && !enableVibrator) {
						Util.log(str);
						long temp = System.currentTimeMillis() - dialingStart;
						isAlert = true;
						// 这个是关键,当第一次DIALING状态的时间,与当前的ALERTING间隔时间在1.5秒以上并且在20秒以内的话
						// 那么认为下次的ACTIVE状态为通话接通.
						if (temp > 1500 && temp < 20000) {
							enableVibrator = true;
							Log.i("TestService", "间隔时间....." + temp + "....."
									+ Thread.currentThread().getName());
						}
						
						continue;
					}
					if (str.contains("GET_CURRENT_CALLS")
							&& str.contains("ACTIVE") && enableVibrator) {
						mVibrator.vibrate(100);
						Util.log(str);
						Util.log("\ncall is accepted...\n");
						Time t = new Time(); // or Time t=new Time("GMT+8");
						// 加上Time Zone资料。

						t.setToNow(); // 取得系统时间。
						int year = t.year;
						int month = t.month + 1;
						int date = t.monthDay;
						int hour = t.hour; // 0-23
						int minute = t.minute;
						int second = t.second;

						System.out
								.println("call is accepted!!---------------------------------------------------->"
										+ year
										+ "/"
										+ month
										+ "/"
										+ date
										+ ","
										+ hour + "-" + minute + "-" + second);
						Intent intent = new Intent();
						intent.putExtra("year", year);
						intent.putExtra("month", month);
						intent.putExtra("date", date);
						intent.putExtra("hour", hour);
						intent.putExtra("minute", minute);
						intent.putExtra("second", second);
						// 设置发送广播的类型，可以随便写一个
						intent.setAction(ACTION);
						sendBroadcast(intent);
						//如果WantedCallDuration=0的话，那么不挂断电话
						if (WantedCallDuration != 0) {
							sleep(WantedCallDuration * 1000);
							iTelephony.endCall();
						}
						mVibrator.vibrate(100);
						enableVibrator = false;
						dialingStart = 0;
						// break;
					}
				}//while end
				System.out.println("TestService is terminated!!");
				Log.i("TestService", "结束.........."
						+ Thread.currentThread().getName());
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("in Exception!!!");
			}

		}
	}

	public void phoner() {
		manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		Class<TelephonyManager> c = TelephonyManager.class;
		Method getITelephonyMethod = null;
		try {
			getITelephonyMethod = c.getDeclaredMethod("getITelephony",
					(Class[]) null);
			getITelephonyMethod.setAccessible(true);
			iTelephony = (ITelephony) getITelephonyMethod.invoke(manager,
					(Object[]) null);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	// 创建用来接收预期通话时长的广播
	class SetWantedCallDurationBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int time = intent.getIntExtra("time", 0);
			Message msg = new Message();
			msg.obj = time;
			ServiceMsgHandler.sendMessage(msg);
			Util.log("received time = "  + time );
		}

	}

	// 创建消息处理handler
	private Handler ServiceMsgHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			WantedCallDuration = (Integer) msg.obj;
		}

	};
	
}
