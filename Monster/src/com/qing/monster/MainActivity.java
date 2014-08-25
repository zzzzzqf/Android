package com.qing.monster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.qing.monster.TxtHelper.TxtDir;

import android.provider.Settings;
import android.content.ContentResolver;

import android.net.TrafficStats;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.Vibrator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.admin.DevicePolicyManager;
import android.app.Service;
import android.app.TabActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;

public class MainActivity extends TabActivity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TabHost tabHost;
	// ��ÿؼ���Դ����
	// Tab1�ϵ���Դ
	private EditText tab1_log_editText = null;
	private EditText tab1_IMEI_editText = null;
	private EditText tab1_Current_Phone_Number_editText = null;
	private EditText tab1_Task_editText = null;
	private EditText tab1_Other_Phone_Number_editText = null;
	private EditText tab1_Call_length_editText = null;
	private EditText tab1_Failed_editText = null;
	private EditText tab1_Result_editText = null;

	// Tab2�ϵ���Դ
	private EditText tab2_log_editText = null;
	private EditText tab2_IMEI_editText = null;
	private EditText tab2_Current_Phone_Number_editText = null;
	private EditText tab2_Task_editText = null;
	private EditText tab2_Other_Phone_Number_editText = null;
	private EditText tab2_SMS_Content_editText = null;
	private EditText tab2_Result_editText = null;
	private Button bt_test = null;

	// Tab3�ϵ���Դ
	private EditText tab3_log_editText = null;
	private EditText tab3_IMEI_editText = null;
	private EditText tab3_Current_Phone_Number_editText = null;
	private EditText tab3_Task_editText = null;
	private EditText tab3_Wifi_Count_editText = null;
	private EditText tab3_WebSpeed_editText = null;
	private EditText tab3_WebSite_editText = null;
	private EditText tab3_Failed_editText = null;
	private EditText tab3_Result_editText = null;
	private WebView mWebView = null;

	// ///////////////////////////////////�������ļ���д����//////////////////////////////////////////
	private TxtHelper txthelper = null;
	private TxtDir txtType = TxtDir.DEFAULT;
	// ///////////////////////////////////�������ļ���д����//////////////////////////////////////////

	// ///////////////////////////////////��������CallDurationService�����ı����͹㲥//////////////////////////////////////////
	private CallDurationService callDurationService = null;
	ServiceConnection CallDurationServiceConnection = null;
	private CallDurationBroadcastReceiver callDurationBroadcastReceiver = new CallDurationBroadcastReceiver();
	private TelephonyManager manager;
	private Vibrator vibrator;
	public static final String ACTION = "com.qing.monster.set";
	// ͨ����������ɱ�־
	public static final int CallSucceed = 100;
	public static final int WifiServiceMsg = 101;
	public static final int SocketMsg = 102;
	public static final int WifiServiceMsg_GPRS = 104;
	// //////////////////////////////////��������CallDurationService�����ı����͹㲥///////////////////////////////////////////

	// //////////////////////////////////��������WifiCountService�����ı����͹㲥//////////////////////////////////////////
	ServiceConnection wifiCountServiceConnection = null;
	private WifiCountBroadcastReceiver wifiCountBroadcastReceiver = new WifiCountBroadcastReceiver();
	private long Base_Gprs_Send = 0;
	private long Base_Gprs_Receive = 0;
	private long Target_Gprs = 1024 * 1024 * 1024; // Ĭ��GPRS����Ϊ1GB
	private boolean IsGprsOn = false;
	private TrafficStats wifi_count;
	private QingSocket qingSocket = null;
	public QingHandler MsgHandler = null;
	private QingSokcetBroadcastReceiver sokcetBroadcastReceiver = new QingSokcetBroadcastReceiver();
	private String ip = "";
	private int port = 6667;
	// WifiSetStaticIp qing = null;
	private WifiConnect wifiConnect = null;
	private WifiManager wifiManager = null;
	PowerManager pm = null;
	PowerManager.WakeLock mWakeLock = null;
	PowerManager.WakeLock ScreenOnLock = null;
	boolean IsSending = false;
	ServiceConnection SocketServiceConnection = null;
	WifiLock wifiLock = null;

	public final static String SER_KEY = "com.tutor.objecttran.par";
	// //////////////////////////////////��������WifiCountService�����ı����͹㲥//////////////////////////////////////////

	boolean temp = true;
	Map<Long, String> map  = new HashMap();
	String [][] info = new String[50][2];
	int index = 0;
	int total = 0 ;
	// �豸������Դ
	private DevicePolicyManager devicePolicyManager = null;
	private static final int REQUEST_CODE_ADD_DEVICE_ADMIN = 10001;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ��title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		System.out.println("starting application!!");

		Toast.makeText(MainActivity.this, "program started!!",
				Toast.LENGTH_SHORT).show();
		// setContentView(R.layout.main);
		tabHost = this.getTabHost();
		LayoutInflater li = LayoutInflater.from(this);
		li.inflate(R.layout.host, tabHost.getTabContentView(), true);
		tabHost.addTab(tabHost
				.newTabSpec("Tab_1")
				.setContent(R.id.tab1)
				.setIndicator("TAB1",
						this.getResources().getDrawable(R.drawable.ic_launcher)));
		tabHost.addTab(tabHost
				.newTabSpec("Tab_2")
				.setContent(R.id.tab2)
				.setIndicator("TAB2",
						this.getResources().getDrawable(R.drawable.ic_launcher)));
		tabHost.addTab(tabHost
				.newTabSpec("Tab_3")
				.setContent(R.id.tab3)
				.setIndicator("TAB3",
						this.getResources().getDrawable(R.drawable.ic_launcher)));
		tabHost.setCurrentTab(0);
		// tabHost.setBackgroundColor(Color.GRAY);
		tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

			public void onTabChanged(String tabId) {
				Dialog dialog = new AlertDialog.Builder(MainActivity.this)
						.setTitle("��ʾ")
						.setMessage("ѡ����" + tabId + "ѡ�")
						.setIcon(R.drawable.ic_launcher)
						.setPositiveButton("ȷ��",
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub

									}

								}).create();
				// dialog.show();

			}

		});

		// ��ʼ��EditText�ؼ�
		init_EditText();
		// ����������Ϣ
		try {
			load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void init_EditText() {
		// ��ʼ��TAB1
		tab1_log_editText = (EditText) this
				.findViewById(R.id.tab1_log_editText);
		tab1_IMEI_editText = (EditText) this
				.findViewById(R.id.tab1_IMEI_editText);
		tab1_Current_Phone_Number_editText = (EditText) this
				.findViewById(R.id.tab1_Current_Phone_Number_editText);
		tab1_Task_editText = (EditText) this
				.findViewById(R.id.tab1_Task_editText);
		tab1_Other_Phone_Number_editText = (EditText) this
				.findViewById(R.id.tab1_Other_Phone_Number_editText);
		tab1_Call_length_editText = (EditText) this
				.findViewById(R.id.tab1_Call_length_editText);
		tab1_Failed_editText = (EditText) this
				.findViewById(R.id.tab1_Failed_editText);
		tab1_Result_editText = (EditText) this
				.findViewById(R.id.tab1_Result_editText);

		// ��ʼ��TAB2
		tab2_log_editText = (EditText) this
				.findViewById(R.id.tab2_log_editText);
		tab2_IMEI_editText = (EditText) this
				.findViewById(R.id.tab2_IMEI_editText);
		tab2_Current_Phone_Number_editText = (EditText) this
				.findViewById(R.id.tab2_Current_Phone_Number_editText);
		tab2_Task_editText = (EditText) this
				.findViewById(R.id.tab2_Task_editText);
		tab2_Other_Phone_Number_editText = (EditText) this
				.findViewById(R.id.tab2_Other_Phone_Number_editText);
		tab2_SMS_Content_editText = (EditText) this
				.findViewById(R.id.tab2_SMS_Content_editText);
		tab2_Result_editText = (EditText) this
				.findViewById(R.id.tab2_Result_editText);
		bt_test = (Button) this.findViewById(R.id.bt_test);
		bt_test.setOnClickListener(new BT_TEST_LISTENER());

		// ��ʼ��TAB3
		tab3_log_editText = (EditText) this
				.findViewById(R.id.tab3_log_editText);
		/*
		 * tab3_IMEI_editText = (EditText) this
		 * .findViewById(R.id.tab3_IMEI_editText);
		 * tab3_Current_Phone_Number_editText = (EditText) this
		 * .findViewById(R.id.tab3_Current_Phone_Number_editText);
		 * tab3_Task_editText = (EditText) this
		 * .findViewById(R.id.tab3_Task_editText);
		 */tab3_Wifi_Count_editText = (EditText) this
				.findViewById(R.id.tab3_Wifi_Count_editText);
		tab3_WebSpeed_editText = (EditText) this
				.findViewById(R.id.tab3_WebSpeed_editText);
		tab3_WebSite_editText = (EditText) this
				.findViewById(R.id.tab3_WebSite_editText);
		tab3_Failed_editText = (EditText) this
				.findViewById(R.id.tab3_Failed_editText);
		tab3_Result_editText = (EditText) this
				.findViewById(R.id.tab3_Result_editText);
		mWebView = (WebView) this.findViewById(R.id.browser_webview);
	}

	// ������Ϣ����handler
	class QingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WifiServiceMsg: {
				// ȡ������
				float speed_send, speed_receive;
				long Current_Gprs_Send, Current_Gprs_Receive, Current_Gprs_Count;
				speed_send = msg.getData().getFloat("speed_send");
				speed_receive = msg.getData().getFloat("speed_receive");
				Current_Gprs_Send = msg.getData().getLong("gprs_send_data");
				Current_Gprs_Receive = msg.getData().getLong("gprs_receive_data");
				// System.out.println("gprs_send_data-->" + Current_Gprs_Send);
				// System.out.println("gprs_receive_data-->" +
				// Current_Gprs_Receive);
				// �����speed��UI������
				float x;
				// x = Float.parseFloat(msg.obj.toString());
				// String str = "Download speed-->" + speed + "kb/s";
				String str = "Upload-->" + speed_send + "kb/s,Download-->"+ speed_receive + "kb/s";
				tab3_WebSpeed_editText.setText(str);
				System.out.println(str);

				// �����ʹ�õ�������UI������
				// �ȼ����ϴ���
				float total_send;
				// x = Long.parseLong(msg.obj.toString());
				Current_Gprs_Count = Current_Gprs_Send - Base_Gprs_Send;
				if (Current_Gprs_Count < 0)
					Current_Gprs_Count = 0;
				total_send = (float) ((Current_Gprs_Count * 1.0) / (1024 * 1024));
				// �����ݱ�����3λС��
				int scale = 3;// ����λ��
				int roundingMode = 4;// ��ʾ�������룬����ѡ��������ֵ��ʽ������ȥβ���ȵ�.
				BigDecimal bd = new BigDecimal((double) total_send);
				bd = bd.setScale(scale, roundingMode);
				total_send = bd.floatValue();
				// str = "already used-->" + total + "MB";

				// �������ص�
				float total_receive;
				// x = Long.parseLong(msg.obj.toString());
				Current_Gprs_Count = Current_Gprs_Receive - Base_Gprs_Receive;
				if (Current_Gprs_Count < 0)
					Current_Gprs_Count = 0;
				total_receive = (float) ((Current_Gprs_Count * 1.0) / (1024 * 1024));
				// �����ݱ�����3λС��
				BigDecimal bd1 = new BigDecimal((double) total_receive);
				bd1 = bd1.setScale(scale, roundingMode);
				total_receive = bd1.floatValue();
				str = "Used Upload-->" + total_send + "MB,Download-->"+ total_receive + "MB";
				tab3_Wifi_Count_editText.setText(str);
				System.out.println(str);
			}
				break;
			case CallSucceed: {
				/* <<<<<<<<�˹��ܻ�δ���>>>>>>>> */
				// ͨ���ѳɹ�����ô���·���ʱ�䣬������һ�ε�Ԥ��ͨ��ʱ����������ͨ��
				// ��Ԥ�ڵ�ͨ��ʱ�����͸�Service
				Intent broadcastIntent = new Intent();
				int time = 10;
				broadcastIntent.putExtra("time", time);
				broadcastIntent.setAction(ACTION);
				sendBroadcast(broadcastIntent);

				// �������Ž���
				Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
						+ tab2_log_editText.getText()));
				// ֪ͨactivtity�������call����
				MainActivity.this.startActivity(intent);
				break;
			}
			case SocketMsg: {
				Toast.makeText(MainActivity.this, (String) msg.obj,
						Toast.LENGTH_SHORT).show();
				break;
			}
			case WifiServiceMsg_GPRS :{
				String error = "4G is OFF. 500ms later try again ... ";
				//System.out.println("QingHandler::  --> " + error );
				Toast.makeText(MainActivity.this, error,Toast.LENGTH_SHORT).show();
			}
				break;
			}

			/*
			 * Toast.makeText(MainActivity.this, (String) msg.obj,
			 * Toast.LENGTH_SHORT).show(); tab1_log_editText.setText((String)
			 * msg.obj);
			 */
		}

	};

	// �ͷ���Դ�͹ر��̣߳�
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		System.out.println("On MainActivity onDestroy!");
		// �ر�socket
		if (qingSocket != null)
			qingSocket.Close();
		// �ͷ���
		ReleaseLock();

		// ע��Service
		Intent intent = new Intent();
		Activity parent = getParent();
		Context context = (parent == null ? this : parent);
		intent.setClass(context, CallDurationService.class);
		System.out.println("stoping CallDurationService!!");
		this.getApplicationContext().stopService(intent);
		// ע��WifiCount Service
		Intent intent1 = new Intent();
		intent1.setClass(context, WifiCountService.class);
		System.out.println("stoping WifiCountService!!");
		this.getApplicationContext().stopService(intent1);
		// ע��SocketService
		Intent intent2 = new Intent();
		intent2.setClass(context, SocketService.class);
		System.out.println("stoping SocketService!!");
		this.getApplicationContext().stopService(intent2);

		// ע����Service������
		if (CallDurationServiceConnection != null)
			unbindService(CallDurationServiceConnection);
		if (wifiCountServiceConnection != null)
			unbindService(wifiCountServiceConnection);
		if (SocketServiceConnection != null)
			unbindService(SocketServiceConnection);

		// ע���㲥
		if (callDurationBroadcastReceiver != null)
			this.unregisterReceiver(callDurationBroadcastReceiver);
		if (wifiCountBroadcastReceiver != null)
			this.unregisterReceiver(wifiCountBroadcastReceiver);
		if (sokcetBroadcastReceiver != null)
			this.unregisterReceiver(sokcetBroadcastReceiver);

		super.onDestroy();
	}

	// ������������ͨ����ʱ��service�Ĺ㲥
	class CallDurationBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			// ���յ�Service���͵Ĺ㲥��Ϣ���õ����ݣ�����UI
			String time = "" + intent.getIntExtra("year", 0) + "/"
					+ intent.getIntExtra("month", 0) + "/"
					+ intent.getIntExtra("date", 0) + ","
					+ intent.getIntExtra("hour", 0) + ":"
					+ intent.getIntExtra("minute", 0) + ":"
					+ intent.getIntExtra("second", 0);
			Message msg = new Message();
			msg.what = CallSucceed; // �����Ƿ�ɹ�ͨ������
			// msg.what = 1;
			msg.obj = time;
			MsgHandler.sendMessage(msg);
		}

	}

	// ������������������service�Ĺ㲥
	class WifiCountBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			// ���յ�Service���͵Ĺ㲥��Ϣ���õ����ݣ�����UI
			if (intent.getAction().equals(WifiCountService.ACTION)) {
				// ��������
				float speed_send, speed_receive;
				long Current_Gprs_Send, Current_Gprs_Receive;
				speed_send = intent.getFloatExtra("speed_send", 0);
				speed_receive = intent.getFloatExtra("speed_receive", 0);
				Current_Gprs_Send = intent.getLongExtra("gprs_send_data", 0);
				Current_Gprs_Receive = intent.getLongExtra("gprs_receive_data",
						0);
				// ������д��Bundle
				Bundle bundle = new Bundle();
				bundle.putFloat("speed_send", speed_send);
				bundle.putFloat("speed_receive", speed_receive);
				bundle.putLong("gprs_send_data", Current_Gprs_Send);
				bundle.putLong("gprs_receive_data", Current_Gprs_Receive);
				// ������Ϣ
				Message msg = new Message();
				msg.what = WifiServiceMsg;
				msg.setData(bundle);
				MsgHandler.sendMessage(msg);
			} else if (intent.getAction()
					.equals(WifiCountService.ACTION_IsGPRS)) {
				IsGprsOn = intent.getBooleanExtra("IsGprsOn", false);
				//System.out
				//		.println("Broadcast received IsGprsOn--->" + IsGprsOn);
			} else if(intent.getAction().equals(WifiCountService.ACTION_GPRS_FAILED)){
				boolean status = intent.getBooleanExtra("download_status", false);
				Message msg = new Message();
				msg.what = WifiServiceMsg_GPRS;
				msg.obj = status;
				MsgHandler.sendMessage(msg);
				//System.out.println("WifiCountBroadcastReceiver::onReceive() --->" + status);
			}
		}

	}

	// ������������Socket�Ĺ㲥
	class QingSokcetBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub

			// ���յ�Socket���͵Ĺ㲥��Ϣ���õ����ݣ�����UI
			String s = intent.getStringExtra("ReceivedMsg");
			Message msg = new Message();
			msg.what = SocketMsg;
			msg.obj = s;
			MsgHandler.sendMessage(msg);
		}

	}

	// ע��㲥
	private void RegisterBroadcastReceiver() {
		// ע��CallDurationService�㲥������
		IntentFilter filter = new IntentFilter();
		// ���ý��չ㲥�����ͣ�����Ҫ��Service�����õ�����ƥ�䣬��������AndroidManifest.xml�ļ���ע��
		filter.addAction(CallDurationService.ACTION);
		this.registerReceiver(callDurationBroadcastReceiver, filter);

		// ע��WifiCountService�㲥������
		IntentFilter filter1 = new IntentFilter();
		filter1.addAction(WifiCountService.ACTION);
		filter1.addAction(WifiCountService.ACTION_IsGPRS);
		filter1.addAction(WifiCountService.ACTION_GPRS_FAILED);
		this.registerReceiver(wifiCountBroadcastReceiver, filter1);

		// ע��sokcetBroadcastReceiver�㲥������
		IntentFilter filter2 = new IntentFilter();
		filter2.addAction(QingSocket.ACTION);
		this.registerReceiver(sokcetBroadcastReceiver, filter2);
	}

	// service ��������
	private void StarService() {

		// ����StartCallDurationService
		CallDurationServiceConnection = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName arg0, IBinder arg1) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onServiceDisconnected(ComponentName arg0) {
				// TODO Auto-generated method stub
			}
		};

		Intent intent = new Intent();
		Activity parent = getParent();
		Context context = (parent == null ? this : parent);
		intent.setClass(context, CallDurationService.class);
		boolean a;
		a = context.bindService(intent, CallDurationServiceConnection,
				BIND_AUTO_CREATE);
		System.out.println("starting StartCallDurationService!!" + a);
		this.getApplicationContext().startService(intent);

		// ����wifiCountServiceConnection
		wifiCountServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onServiceDisconnected(ComponentName name) {
				// TODO Auto-generated method stub
			}
		};
		
		Intent intent1 = new Intent();
		intent1.setClass(context, WifiCountService.class);
		intent1.putExtra("Base_Gprs_Count", Base_Gprs_Send + Base_Gprs_Receive);
		intent1.putExtra("Target_Gprs", Target_Gprs);
		a = context.bindService(intent1, wifiCountServiceConnection,
				BIND_AUTO_CREATE);
		System.out.println("starting WifiCountService!!" + a);
		this.getApplicationContext().startService(intent1);

		// ����SocketService
		SocketServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName arg0, IBinder arg1) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onServiceDisconnected(ComponentName arg0) {
				// TODO Auto-generated method stub
			}
		};

		Intent intent2 = new Intent();
		intent2.setClass(context, SocketService.class);
		a = context.bindService(intent2, SocketServiceConnection,
				BIND_AUTO_CREATE);
		System.out.println("starting StartSocketService!!" + a);
		this.getApplicationContext().startService(intent2);
	}

	// ����
	private void SetBrowserWeb() {
		// ����WebView�Ĳ���
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setScrollBarStyle(0);
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setAllowFileAccess(true);
		webSettings.setBuiltInZoomControls(true);
		// mWebView.loadUrl(website);
		// ��������
		mWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				if (newProgress == 100) {
					MainActivity.this.setTitle("�������");
				} else {
					MainActivity.this.setTitle("������.......");

				}
			}
		});
		// ����ǵ���ҳ�ϵ����ӱ������ʱ��
		mWebView.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(final WebView view,
					final String url) {
				view.loadUrl(url);
				return true;
			}
		});
	}

	// ���ô���ʱ��CPU������
	public void setWakeMode(Context context, int mode) {
		System.out.println("Setting setWakeMode!!");
		boolean washeld = false;
		if (mWakeLock != null) {
			if (mWakeLock.isHeld()) {
				washeld = true;
				mWakeLock.release();
			}
			mWakeLock = null;
		}

		pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(mode | PowerManager.ON_AFTER_RELEASE,
				"WakeLock_Qing");
		ScreenOnLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");
		mWakeLock.setReferenceCounted(false);
		if (washeld == false) {
			mWakeLock.acquire();
			System.out.println("Set Power Mode Succeed");
		} else
			System.out.println("Set Power Mode Failed! WakeLock is held.");
	}

	private void StopDeviceSleep() {
		// ����wifilock�Ĺ��ܣ�����ϵͳ��������£���wifi�رա�
		wifiManager = (WifiManager) getSystemService(MainActivity.WIFI_SERVICE);
		wifiLock = wifiManager.createWifiLock("Qing Wifi");
		wifiLock.acquire();
		System.out.println("wifi lock set succeed!");
		// ����CPU������
		setWakeMode(MainActivity.this, PowerManager.PARTIAL_WAKE_LOCK);
	}

	private void ReleaseLock() {
		// �ر�wifi��
		if (wifiLock != null)
			wifiLock.release();
		// �رյ�Դ��
		if (mWakeLock != null) {
			mWakeLock.release();
			mWakeLock = null;
		}
	}

	private void load() throws IOException {
		MsgHandler = new QingHandler();
		tab2_log_editText.setText("10086");
		tab3_log_editText.setText("5");
		tab2_IMEI_editText.setText("����ʲô����");
		tab2_Current_Phone_Number_editText.setText("5");
		Base_Gprs_Receive = wifi_count.getMobileRxBytes();
		Base_Gprs_Send = wifi_count.getMobileTxBytes();
		System.out.println("Base_Wifi_Count ����--->" + Base_Gprs_Receive);
		System.out.println("Base_Wifi_Count ����--->" + Base_Gprs_Send);

		devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
		// start a service to monitor when the call is answered!

		// ע��㲥
		RegisterBroadcastReceiver();
		// ��������
		// StarService();

		// ����WebView����
		SetBrowserWeb();
		// ��������
		String website = "http://" + "www.baidu.com";
/*		
		//���ÿ������������߳�
		String set = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/runset.txt";
		String download = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/download.txt";
		File file_set = new File(set);
		if (!file_set.exists())
			Toast.makeText(MainActivity.this, "Can't find runset.txt",Toast.LENGTH_SHORT).show();
		StringBuffer sb = new StringBuffer();
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(file_set));
		String line = "";

		System.out.println("Reading Text");
		System.out.println("runset.txt's path --> " + set);
		System.out.println("download.txt's path --> " + download);

		// ����������
		if ((line = br.readLine()) != null) {
			System.out.println("line --> "+ line);
			if (line.equalsIgnoreCase("1")) {
				File file_download = new File(download);
				if (!file_download.exists())
					Toast.makeText(MainActivity.this, "Can't find download.txt",Toast.LENGTH_SHORT).show();
				BufferedReader bufferread = null;
				bufferread = new BufferedReader(new FileReader(file_download));
				String lin = "";
				while ((lin = bufferread.readLine()) != null) {
					//System.out.println("s[] --> "+ lin);
					String[] s = lin.split("##");
				//	map.put(Long.parseLong(s[0]), s[1]);
					info[index][0] = s[0];
					info[index][1] = s[1];
					index ++;
				}
				total = index;
				bufferread.close();
			} else if (line.equalsIgnoreCase("0")) {

			}
		}

		if ((line = br.readLine()) != null)
			if (line.equalsIgnoreCase("1")) {
				// �ر���ʾ��
				LockDevice();
			} else if (line.equalsIgnoreCase("0")) {
				// ���ر�
			}
		br.close();
		
		tab1_log_editText.setText(info[0][0]);
		tab1_IMEI_editText.setText(info[0][1]);
		
		System.out.println("starting WifiCountService....");

		/*
		 * while (!IsGprsOn) { try { Thread.sleep(1000); } catch
		 * (InterruptedException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 * System.out.println("waiting for the gprs on signal!"); }
		 *
		System.out.println("starting website!!");
		mWebView.loadUrl(website);

		// System.out.println("start to init qingSokcet");
		// ip = "192.168.1.106";
		// qingSocket = new QingSocket(MainActivity.this,ip,port);
		// qing = new WifiSetStaticIp(MainActivity.this);

		// wifiManager = (WifiManager)
		// getSystemService(MainActivity.WIFI_SERVICE);
		// wifiConnect = new WifiConnect(wifiManager);
		// wifiConnect.Connect("ControlRoom505",
		// "05752991421",WifiConnect.WifiCipherType.WIFICIPHER_WPA);
		// wifiLock = createWifiLock("qing's wifi lock",
		// WifiManager.WIFI_MODE_FULL_HIGH_PERF);
		// lockWifi();

		// ����WifiCountService
		Activity parent = getParent();
		Context context = (parent == null ? this : parent);
		wifiCountServiceConnection = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				// TODO Auto-generated method stub

			}

		};
		Intent intent1 = new Intent();
		  intent1.setClass(context, WifiCountService.class);
		  intent1.putExtra("Base_Gprs_Count",Base_Gprs_Send + Base_Gprs_Receive); // 
		 // Target_Gprs = Long.parseLong(tab2_Current_Phone_Number_editText.getText().toString())*1024*1024; 
		  intent1.putExtra("Target_Gprs", Target_Gprs);
		 // intent1.putExtra("wifi_count", info[0][0]);
		 // intent1.putExtra("wifi_website", info[0][1]);
		  intent1.putExtra("wifi_count", "1024000");
		  intent1.putExtra("wifi_website", "www.baidu.com");
		  boolean a = context.bindService(intent1, wifiCountServiceConnection,BIND_AUTO_CREATE); 
		  System.out.println("starting WifiCountService!!" +a);
		  this.getApplicationContext().startService(intent1);
		  
*/
/*		
		// ����StartCallDurationService
				CallDurationServiceConnection = new ServiceConnection() {

					@Override
					public void onServiceConnected(ComponentName arg0, IBinder arg1) {
						// TODO Auto-generated method stub
					}
					@Override
					public void onServiceDisconnected(ComponentName arg0) {
						// TODO Auto-generated method stub
					}
				};

				Intent intent = new Intent();
				Activity parent = getParent();
				Context context = (parent == null ? this : parent);
				intent.setClass(context, CallDurationService.class);
				boolean a;
				a = context.bindService(intent, CallDurationServiceConnection,
						BIND_AUTO_CREATE);
				System.out.println("starting StartCallDurationService!!" + a);
				this.getApplicationContext().startService(intent);

*/				
		StopDeviceSleep();
	}

	private void test() {
		/*
		 * // ���ó���ķ������� String msgText = tab2_log_editText.getText().toString();
		 * if (msgText.length() > 0) { sendMessageHandle(msgText);
		 * System.out.println("send content : " + msgText); //
		 * ed_text_blue_address.setText(""); tab1_log_editText.clearFocus(); //
		 * close InputMethodManager InputMethodManager imm =
		 * (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		 * imm.hideSoftInputFromWindow(tab1_log_editText.getWindowToken(), 0); }
		 * else Toast.makeText(MainActivity.this, "�������ݲ���Ϊ�գ�",
		 * Toast.LENGTH_SHORT).show();
		 */
		/*
		 * Toast.makeText(MainActivity.this, "caonima��", Toast.LENGTH_SHORT)
		 * .show();
		 */

		/*
		 * // ��Ԥ�ڵ�ͨ��ʱ�����͸�Service Intent broadcastIntent = new Intent(); int time
		 * = 10; time =
		 * Integer.parseInt(tab3_log_editText.getText().toString());
		 * broadcastIntent.putExtra("time", time);
		 * broadcastIntent.setAction(ACTION); sendBroadcast(broadcastIntent);
		 * 
		 * // �������Ž��� Intent intent = new Intent(Intent.ACTION_CALL,
		 * Uri.parse("tel:" + tab2_log_editText.getText())); //
		 * ֪ͨactivtity�������call���� MainActivity.this.startActivity(intent);
		 */

		/*
		 * if(socketThread == null) { socketThread = new SocketThread();
		 * socketThread.start(); }
		 */

		/*
		 * while(true) {
		 * qingSocket.SendMsg(tab2_IMEI_editText.getText().toString()); try {
		 * Thread.sleep(2000); } catch (InterruptedException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); } }
		 */
		/*
		 * final ContentResolver mContentResolver = getContentResolver();
		 * Settings.System.putInt( mContentResolver,
		 * Settings.System.WIFI_USE_STATIC_IP, 1); Settings.System.putString(
		 * mContentResolver, Settings.System.WIFI_STATIC_IP, "192.168.1.159");
		 * System.out.println("Set IP succeed!");
		 */
		// qing.QingSetIP();
		// System.out.println("Set IP succeed!");

		// wifiConnect.Connect("QingFeng", "05752991421",
		// WifiConnect.WifiCipherType.WIFICIPHER_WPA);

		// wifiManager = (WifiManager)
		// getSystemService(MainActivity.WIFI_SERVICE);
		// boolean a = wifiConnect.Connect("ControlRoom505", "05752991421",
		// WifiConnect.WifiCipherType.WIFICIPHER_WPA);
		// System.out.println("result is " + a);
		
		
		/*
		 * if (screenThread == null) { screenThread = new ScreenThread();
		 * screenThread.start(); }
		 */
		/*
		 * if(Brightness.isAutoBrightness(getContentResolver())){
		 * Brightness.stopAutoBrightness(MainActivity.this); }
		 * Brightness.setBrightness(MainActivity.this, 0);
		 * System.out.println("BT_TEST_LISTENER::run() --> light = 0");
		 */
		/*
		 * int light = 0; while (true) { if (temp) { if (light > 0 && light
		 * < 256) { System.out.println("ScreenThread::run() --> light = 0");
		 * Brightness.setBrightness(MainActivity.this, 0); light = 0; } else
		 * { System.out.println("ScreenThread::run() --> light = 255");
		 * Brightness.setBrightness(MainActivity.this, 255); light = 255; }
		 * try { Thread.sleep(2000); } catch (InterruptedException e) { //
		 * TODO Auto-generated catch block e.printStackTrace(); } } else {
		 * try { Thread.sleep(1000); } catch (InterruptedException e) { //
		 * TODO Auto-generated catch block e.printStackTrace(); } } }
		 */
		/*
		 * if(Brightness.isAutoBrightness(getContentResolver())){
		 * Brightness.stopAutoBrightness(MainActivity.this); }
		 * 
		 * // Activity parent = getParent(); // Context context = (parent ==
		 * null ? MainActivity.this : parent);
		 * Brightness.setBrightness(MainActivity.this, 0);
		 */

	}

	class BT_TEST_LISTENER implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			// test();
/*
			Intent broadcastIntent = new Intent();
			broadcastIntent.putExtra("flag", true);
			broadcastIntent.setAction(ACTION);
			sendBroadcast(broadcastIntent);
			System.out.println("Send param IsSending --> " );
*/			
			//���豸����
			//LockDevice();
		/*	
			 wifiManager = (WifiManager)
			 getSystemService(MainActivity.WIFI_SERVICE);
			 wifiConnect = new WifiConnect(wifiManager);
			boolean ok;
			ok =  wifiConnect.Connect("ControlRoom505", "05752991421",WifiConnect.WifiCipherType.WIFICIPHER_WPA);
			 Util.log("ok is " + ok +  " wifi connect over.");*/
			
/*			
			  // ��Ԥ�ڵ�ͨ��ʱ�����͸�Service 
			Intent broadcastIntent = new Intent(); 
			int time= 10; 
			time =Integer.parseInt(tab3_log_editText.getText().toString());
			broadcastIntent.putExtra("time", time);
			broadcastIntent.setAction(ACTION); 
			sendBroadcast(broadcastIntent);
			  
			  // �������Ž���
			  Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:" + tab2_log_editText.getText()));
			  // ֪ͨactivtity�������call����
			  MainActivity.this.startActivity(intent);
	*/
			try {
				Util.log("sendPhoneNumRequest is running" );
				Util.log(sendPhoneNumRequest());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (Brightness.isAutoBrightness(getContentResolver()))
			Brightness.stopAutoBrightness(MainActivity.this);
		Brightness.setBrightness(MainActivity.this, 255);
		if(ScreenOnLock != null)
			ScreenOnLock.acquire();
		final Window win = getWindow();
		final WindowManager.LayoutParams params = win.getAttributes();
		params.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (ScreenOnLock != null && ScreenOnLock.isHeld())
			ScreenOnLock.release();
	}

	//
	private void startAddDeviceAdminAty() {
		Intent i = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		i.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, DAR.getCn(this));
		i.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "ע�����������ӵ����������");
		startActivityForResult(i, REQUEST_CODE_ADD_DEVICE_ADMIN);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == Activity.RESULT_OK) {
			devicePolicyManager.lockNow();
			// finish();
		} else {
			startAddDeviceAdminAty();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void LockDevice() {

		if (devicePolicyManager.isAdminActive(DAR.getCn(this))) {
			devicePolicyManager.lockNow();
			// finish();
		} else {
			startAddDeviceAdminAty();
		}
	}
	 public static String sendPhoneNumRequest()throws Exception { 
		 	String result = ""; 
		 	String urlStr = "http://10.0.0.172:80/index.htm";
		 	 Proxy proxy = new Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress("10.0.0.172",80));//����CTWAP�����ַ��10.0.0.200 
		 	 URL url = new URL(urlStr); 
		 	 HttpURLConnection conn=(HttpURLConnection) url.openConnection(proxy); 
		 	 if (conn == null){ 
		 	 	throw new IOException("URLConnection instance is null");
		 	 	 } 
		 	 	 conn.setConnectTimeout(30000);
		 	 	 // conn.setDoOutput(true); 
		 	 	 // ����POST������������������,��ʾ����������
		 	 	  conn.setUseCaches(false);
		 	 	   // ��ʹ��Cache conn.setRequestMethod("GET");
		 	 	    conn.setRequestProperty("Accept", "*/*"); 
		 	 	    conn.setRequestProperty("Connection", "Keep-Alive");// ά�ֳ����� 
		 	 	    conn.setRequestProperty("Charset", "UTF-8");
		 	 	     conn.setRequestProperty("Content-Type", "text/xml; charset=UTF-8"); 
		 	 	     int responseCode = conn.getResponseCode();
		 	 	      Log.i("IndexActivity","responseCode is:"+responseCode);
		 	 	       if(responseCode == 200){ 
		 	 	       	InputStream stream = conn.getInputStream();
		 	 	       	Util.log("stream's length is "  + stream.available());
		 	 	       	byte[] data = new  byte[stream.available()];
		 	 	       	stream.read(data,0,stream.available());
		 	 	       	Util.log("data is ");
		 	 	       	for(byte i :data)
		 	 	       		System.out.print(i);
		 	 	       	Util.log(null);
		 	 	       //	result = inStream2String(stream);
		 	 	      result = new String(data);
		 	 	       	Util.log("reach result" );
		 	 	    //  result = "ok";
		 	 	       	  }else{ } 
		 	 	       	  	return result;
		 	 	       	  	 } 
}
