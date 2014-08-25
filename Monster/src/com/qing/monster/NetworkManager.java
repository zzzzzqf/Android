package com.qing.monster;

import java.lang.reflect.Method;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;

public class NetworkManager {

	private Context context;
	private ConnectivityManager connManager;

	public NetworkManager(Context context) {
		this.context = context;
		connManager = (ConnectivityManager) this.context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	/**
	 * @return �����Ƿ����ӿ���
	 */
	public boolean isNetworkConnected() {

		NetworkInfo networkinfo = connManager.getActiveNetworkInfo();

		if (networkinfo != null) {
			return networkinfo.isConnected();
		}

		return false;
	}

	/**
	 * @return wifi�Ƿ����ӿ���
	 */
	public boolean isWifiConnected() {

		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (mWifi != null) {
			return mWifi.isConnected();
		}

		return false;
	}

	/**
	 * ��wifi���ܷ�������ʱ��mobile�Ż�������
	 * 
	 * @return GPRS�Ƿ����ӿ���
	 */
	public boolean isMobileConnected() {

		NetworkInfo mMobile = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (mMobile != null) {
			return mMobile.isConnected();
		}
		return false;
	}

	/**
	 * GPRS���翪�� ����ConnectivityManager��hide�ķ���setMobileDataEnabled ���Կ����͹ر�GPRS����
	 * 
	 * @param isEnable
	 * @throws Exception
	 */
	public void toggleGprs(boolean isEnable) throws Exception {
		Class<?> cmClass = connManager.getClass();
		Class<?>[] argClasses = new Class[1];
		argClasses[0] = boolean.class;

		// ����ConnectivityManager��hide�ķ���setMobileDataEnabled�����Կ����͹ر�GPRS����
		Method method = cmClass.getMethod("setMobileDataEnabled", argClasses);
		method.invoke(connManager, isEnable);
	}

	/**
	 * WIFI���翪��
	 * 
	 * @param enabled
	 * @return �����Ƿ�success
	 */
	public boolean toggleWiFi(boolean enabled) {
		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		return wm.setWifiEnabled(enabled);

	}

	/**
	 * 
	 * @return �Ƿ��ڷ���ģʽ
	 */
	public boolean isAirplaneModeOn() {
		// ����ֵ��1ʱ��ʾ���ڷ���ģʽ
		int modeIdx = Settings.System.getInt(context.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, 0);
		boolean isEnabled = (modeIdx == 1);
		return isEnabled;
	}

	/**
	 * ����ģʽ����
	 * 
	 * @param setAirPlane
	 */
	public void toggleAirplaneMode(boolean setAirPlane) {
		Settings.System.putInt(context.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, setAirPlane ? 1 : 0);
		// �㲥����ģʽ�źŵĸı䣬����Ӧ�ĳ�����Դ���
		// �����͹㲥ʱ���ڷǷ���ģʽ�£�Android 2.2.1�ϲ��Թر���Wifi,���ر�������ͨ������(��GMS/GPRS��)��
		// �����͹㲥ʱ���ڷ���ģʽ�£�Android 2.2.1�ϲ����޷��رշ���ģʽ��
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		// intent.putExtra("Sponsor", "Sodino");
		// 2.3���Ժ������ô�״̬�������һֱ��������Ӫ�̶��������
		intent.putExtra("state", setAirPlane);
		context.sendBroadcast(intent);
	}
}
