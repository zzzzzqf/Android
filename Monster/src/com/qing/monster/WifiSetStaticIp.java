package com.qing.monster;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WifiSetStaticIp {
	WifiConfiguration wifiConf = null;
	Context context = null;
	WifiManager wifiManager = null;
	WifiInfo connectionInfo = null;
	List<WifiConfiguration> configuredNetworks = null;

	WifiSetStaticIp(Context context) {
		wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		connectionInfo = wifiManager.getConnectionInfo();
		configuredNetworks = wifiManager.getConfiguredNetworks();
		for (WifiConfiguration conf : configuredNetworks) {
			if (conf.networkId == connectionInfo.getNetworkId()) {
		//	if (wifiSSID.equals(connectionInfo.getSSID())) {
				wifiConf = conf;
				System.out.println("wifi SSID---> " + wifiConf.SSID);
				break;
			}
		}
	}

	public static void setIpAssignment(String assign, WifiConfiguration wifiConf)
			throws SecurityException, IllegalArgumentException,
			NoSuchFieldException, IllegalAccessException {
		setEnumField(wifiConf, assign, "ipAssignment");
	}

	public static void setIpAddress(InetAddress addr, int prefixLength,
			WifiConfiguration wifiConf) throws SecurityException,
			IllegalArgumentException, NoSuchFieldException,
			IllegalAccessException, NoSuchMethodException,
			ClassNotFoundException, InstantiationException,
			InvocationTargetException {
		Object linkProperties = getField(wifiConf, "linkProperties");
		if (linkProperties == null)
			return;
		Class laClass = Class.forName("android.net.LinkAddress");
		Constructor laConstructor = laClass.getConstructor(new Class[] {
				InetAddress.class, int.class });
		Object linkAddress = laConstructor.newInstance(addr, prefixLength);

		ArrayList mLinkAddresses = (ArrayList) getDeclaredField(linkProperties,
				"mLinkAddresses");
		mLinkAddresses.clear();
		mLinkAddresses.add(linkAddress);
	}

	public static void setGateway(InetAddress gateway,
			WifiConfiguration wifiConf) throws SecurityException,
			IllegalArgumentException, NoSuchFieldException,
			IllegalAccessException, ClassNotFoundException,
			NoSuchMethodException, InstantiationException,
			InvocationTargetException {
		Object linkProperties = getField(wifiConf, "linkProperties");
		if (linkProperties == null)
			return;
		Class routeInfoClass = Class.forName("android.net.RouteInfo");
		Constructor routeInfoConstructor = routeInfoClass
				.getConstructor(new Class[] { InetAddress.class });
		Object routeInfo = routeInfoConstructor.newInstance(gateway);

		ArrayList mRoutes = (ArrayList) getDeclaredField(linkProperties,
				"mRoutes");
		mRoutes.clear();
		mRoutes.add(routeInfo);
	}

	public static void setDNS(InetAddress dns, WifiConfiguration wifiConf)
			throws SecurityException, IllegalArgumentException,
			NoSuchFieldException, IllegalAccessException {
		Object linkProperties = getField(wifiConf, "linkProperties");
		if (linkProperties == null)
			return;

		ArrayList<InetAddress> mDnses = (ArrayList<InetAddress>) getDeclaredField(
				linkProperties, "mDnses");
		mDnses.clear(); // or add a new dns address , here I just want to
						// replace DNS1
		mDnses.add(dns);
	}

	public static Object getField(Object obj, String name)
			throws SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {
		Field f = obj.getClass().getField(name);
		Object out = f.get(obj);
		return out;
	}

	public static Object getDeclaredField(Object obj, String name)
			throws SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {
		Field f = obj.getClass().getDeclaredField(name);
		f.setAccessible(true);
		Object out = f.get(obj);
		return out;
	}

	public static void setEnumField(Object obj, String value, String name)
			throws SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {
		Field f = obj.getClass().getField(name);
		f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
	}

	// After that, I can set setting and update WifiConfiguration for this SSID.

	public void QingSetIP() {

		try {
			setIpAssignment("STATIC", wifiConf); // or "DHCP" for dynamic
													// setting
			setIpAddress(InetAddress.getByName("192.168.1.159"), 24, wifiConf);
			setGateway(InetAddress.getByName("255.255.255.0"), wifiConf);
			setDNS(InetAddress.getByName("8.8.8.8"), wifiConf);
			wifiManager.updateNetwork(wifiConf); // apply the setting
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Edit: Sorry for I don't check for Android 3.x device that have
		// silmilar UI with Android 4.x. In Android 3.x, the gateway is storted
		// in mGateways of linkProperties. mGateways is Arraylist of type
		// InetAddress. Therefore, following should work in Android 3.x.

		// public static void setGateway(InetAddress gateway, WifiConfiguration
		// wifiConf)
		// throws SecurityException, IllegalArgumentException,
		// NoSuchFieldException, IllegalAccessException,ClassNotFoundException,
		// NoSuchMethodException, InstantiationException,
		// InvocationTargetException
		// {
		// Object linkProperties = getField(wifiConf, "linkProperties");
		// if(linkProperties == null)return;
		// ArrayList mGateways = (ArrayList)getDeclaredField(linkProperties,
		// "mGateways");
		// mGateways.clear();
		// mGateways.add(gateway);
		// }
		//

	}
}
