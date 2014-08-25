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
	// �㲥��ACTION
	public static final String ACTION = "com.qing.Monster.CallDurationService";
	// �����࣬��������ͨ�����ᵼ���������δ֪������
	private ITelephony iTelephony;
	// �绰״̬������
	private TelephonyManager manager;
	// ����Logcat���߳�
	private MonitorThread monitorThread = null;
	// ����
	private Vibrator vibrator;
	// Ԥ�ڵ�ͨ��ʱ��
	private int WantedCallDuration = 0;
	// �㲥��
	private SetWantedCallDurationBroadcastReceiver setWantedCallDurationBroadcastReceiver = new SetWantedCallDurationBroadcastReceiver();

	
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		System.out.println("service onCreate starting");
		
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		// vibrator.vibrate(1000);
		manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		
		System.out.println("service onCreate manager.listen");
		
/*		//��ȡ�������
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
		// ע��㲥������
		IntentFilter filter = new IntentFilter();
		// ���ý��չ㲥�����ͣ�����Ҫ��Service�����õ�����ƥ�䣬��������AndroidManifest.xml�ļ���ע��
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
			 * "caonima"); //���÷��͹㲥�����ͣ��������дһ�� intent1.setAction(ACTION);
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

	// ��ͨ��
	class MonitorThread extends Thread {
		// ����
		Vibrator mVibrator;
		// �绰����
		TelephonyManager telManager;

		public MonitorThread(Vibrator mVibrator, TelephonyManager telManager) {
			// public MonitorThread(TelephonyManager telManager) {
			this.mVibrator = mVibrator;
			this.telManager = telManager;
		}

		@Override
		public void run() {

			System.out.println("thread is starting!!!");
			// ��ȡ��ǰ����״̬
			int callState = telManager.getCallState();
			phoner();
			Log.i("TestService", "��ʼ.........."+ Thread.currentThread().getName());
			// ��¼���ſ�ʼʱ��
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
				//���ֻ����Ա�ʾService������
				mVibrator.vibrate(100);
				while ((str = bufferedreader.readLine()) != null) {
				
					if(str.contains("GET_CURRENT_CALLS"))
						Util.log(str);
					if (str.contains("GET_CURRENT_CALLS")
							&& str.contains("DIALING")) {
						// ��DIALING��ʼ�����Ѿ�����ALERTING�����״�DIALING
						if (!isAlert || dialingStart == 0) {
							// ��¼DIALING״̬����ʱ��
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
						// ����ǹؼ�,����һ��DIALING״̬��ʱ��,�뵱ǰ��ALERTING���ʱ����1.5�����ϲ�����20�����ڵĻ�
						// ��ô��Ϊ�´ε�ACTIVE״̬Ϊͨ����ͨ.
						if (temp > 1500 && temp < 20000) {
							enableVibrator = true;
							Log.i("TestService", "���ʱ��....." + temp + "....."
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
						// ����Time Zone���ϡ�

						t.setToNow(); // ȡ��ϵͳʱ�䡣
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
						// ���÷��͹㲥�����ͣ��������дһ��
						intent.setAction(ACTION);
						sendBroadcast(intent);
						//���WantedCallDuration=0�Ļ�����ô���Ҷϵ绰
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
				Log.i("TestService", "����.........."
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

	// ������������Ԥ��ͨ��ʱ���Ĺ㲥
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

	// ������Ϣ����handler
	private Handler ServiceMsgHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			WantedCallDuration = (Integer) msg.obj;
		}

	};
	
}
