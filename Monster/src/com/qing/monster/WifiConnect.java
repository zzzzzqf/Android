package com.qing.monster;

/*
*  WifiConnect.java
*  Author: qingfeng
*/

import java.util.List;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;

public class WifiConnect {

    WifiManager wifiManager;
    WifiLock wifiLock = null;
    
//定义几种加密方式，一种是WEP，一种是WPA，还有没有密码的情况
    public enum WifiCipherType
    {
  	  WIFICIPHER_WEP,WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
    }
	
//构造函数
	public WifiConnect(WifiManager wifiManager)
	{
	  this.wifiManager = wifiManager;
	  wifiLock = createWifiLock("qing's wifi lock", WifiManager.WIFI_MODE_FULL_HIGH_PERF);
	}
	
//打开wifi功能
     private boolean OpenWifi()
     {
    	 boolean bRet = true;
         if (!wifiManager.isWifiEnabled())
         {
       	  bRet = wifiManager.setWifiEnabled(true);  
         }
         return bRet;
     }
//关闭wifi功能
     private boolean CloseWifi()
     {
    	 boolean bRet = true;
         if (wifiManager.isWifiEnabled())
         {
       	  bRet = wifiManager.setWifiEnabled(false);  
         }
         return bRet;
     }
    
//提供一个外部接口，传入要连接的无线网
     public boolean Connect(String SSID, String Password, WifiCipherType Type)
     {
        if(!this.OpenWifi())
    	{
    		 return false;
    	}
//开启wifi功能需要一段时间(我在手机上测试一般需要1-3秒左右)，所以要等到wifi
//状态变成WIFI_STATE_ENABLED的时候才能执行下面的语句
        while(wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING )
        {
        	 try{
     //为了避免程序一直while循环，让它睡个100毫秒在检测……
           	  Thread.currentThread();
			  Thread.sleep(100);
           	}
           	catch(InterruptedException ie){
           }
        }
       
    WifiConfiguration wifiConfig = this.CreateWifiInfo(SSID, Password, Type);
		//
    	if(wifiConfig == null)
		{
    	       return false;
		}
        WifiConfiguration tempConfig = this.IsExsits(SSID);
        if(tempConfig != null)
        {
        	wifiManager.removeNetwork(tempConfig.networkId);
        	Util.log("wifi " + SSID + " is existed.");
        }
        
      int netID = wifiManager.addNetwork(wifiConfig);
      Util.log("netID = " + netID);
    	//boolean bRet = wifiManager.enableNetwork(netID, false);  
    	boolean bRet = wifiManager.enableNetwork(netID, true);  
		return bRet;
     }
     
    //查看以前是否也配置过这个网络
     private WifiConfiguration IsExsits(String SSID)
     {
    	 List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
    	    for (WifiConfiguration existingConfig : existingConfigs) 
    	    {
    	    	//Util.log(existingConfig.toString());
    	      if (existingConfig.SSID.equals("\""+SSID+"\""))
    	      {
    	    	  System.out.println("Control is existed.");
    	          return existingConfig;
    	      }
    	    }
    	 return null; 
     }
     
     private WifiConfiguration CreateWifiInfo(String SSID, String Password, WifiCipherType Type)
     {
     	WifiConfiguration config = new WifiConfiguration();  
         config.allowedAuthAlgorithms.clear();
         config.allowedGroupCiphers.clear();
         config.allowedKeyManagement.clear();
         config.allowedPairwiseCiphers.clear();
         config.allowedProtocols.clear();
     	config.SSID = "\"" + SSID + "\"";  
     	if(Type == WifiCipherType.WIFICIPHER_NOPASS)
     	{
     		 config.wepKeys[0] = "";
     		 config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
     		 config.wepTxKeyIndex = 0;
     	}
     	if(Type == WifiCipherType.WIFICIPHER_WEP)
     	{
     		config.preSharedKey = "\""+Password+"\""; 
     		config.hiddenSSID = true;  
     	    config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
     	    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
     	    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
     	    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
     	    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
     	    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
     	    config.wepTxKeyIndex = 0;
     	}
     	if(Type == WifiCipherType.WIFICIPHER_WPA)
     	{
     		Util.log("setting things.");
     	config.preSharedKey = "\""+Password+"\"";
     	config.hiddenSSID = true;  
     /*	config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);  
     	config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);                        
     	config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);                        
     	config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);                   
     	config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);   */   
     	config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
     	config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
	    //netConfig.preSharedKey = "PWD";
     	config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
     	config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
     	config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
     	config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
     	config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
     	config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
     	config.status = WifiConfiguration.Status.ENABLED;  
     	}
     	else
     	{
     		return null;
     	}
     	return config;
     }
     
     //增加wifilock的功能，避免系统待机情况下，将wifi关闭。
     /**
 	 * 
 	 * @param lockName
 	 *            锁的名称
 	 * @param lockType
 	 * 
 	 *            WIFI_MODE_FULL == 1 <br/>
 	 *            扫描，自动的尝试去连接一个曾经配置过的点<br />
 	 *            WIFI_MODE_SCAN_ONLY == 2 <br/>
 	 *            只剩下扫描<br />
 	 *            WIFI_MODE_FULL_HIGH_PERF = 3 <br/>
 	 *            在第一种模式的基础上，保持最佳性能<br />
 	 * @return wifiLock
 	 */
 	public WifiLock  createWifiLock(String lockName, int lockType)
 	{
 		wifiLock = wifiManager.createWifiLock(lockType, lockName);
 		 return wifiLock;
 	}
 	
	/**
	 * 加上锁
	 */
	public void lockWifi()
	{
		wifiLock.acquire();
	}
	
	/**
	 * 释放锁
	 */
	public void releaseLock()
	{
		if (wifiLock.isHeld())
		{
			wifiLock.release();
		}
	}
	/**
	 * 判断wifi的锁是否持有
	 * 
	 * @return
	 */
	public boolean isHeld()
	{
		return wifiLock.isHeld();
	}
}
