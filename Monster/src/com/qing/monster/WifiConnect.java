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
    
//���弸�ּ��ܷ�ʽ��һ����WEP��һ����WPA������û����������
    public enum WifiCipherType
    {
  	  WIFICIPHER_WEP,WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
    }
	
//���캯��
	public WifiConnect(WifiManager wifiManager)
	{
	  this.wifiManager = wifiManager;
	  wifiLock = createWifiLock("qing's wifi lock", WifiManager.WIFI_MODE_FULL_HIGH_PERF);
	}
	
//��wifi����
     private boolean OpenWifi()
     {
    	 boolean bRet = true;
         if (!wifiManager.isWifiEnabled())
         {
       	  bRet = wifiManager.setWifiEnabled(true);  
         }
         return bRet;
     }
//�ر�wifi����
     private boolean CloseWifi()
     {
    	 boolean bRet = true;
         if (wifiManager.isWifiEnabled())
         {
       	  bRet = wifiManager.setWifiEnabled(false);  
         }
         return bRet;
     }
    
//�ṩһ���ⲿ�ӿڣ�����Ҫ���ӵ�������
     public boolean Connect(String SSID, String Password, WifiCipherType Type)
     {
        if(!this.OpenWifi())
    	{
    		 return false;
    	}
//����wifi������Ҫһ��ʱ��(�����ֻ��ϲ���һ����Ҫ1-3������)������Ҫ�ȵ�wifi
//״̬���WIFI_STATE_ENABLED��ʱ�����ִ����������
        while(wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING )
        {
        	 try{
     //Ϊ�˱������һֱwhileѭ��������˯��100�����ڼ�⡭��
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
     
    //�鿴��ǰ�Ƿ�Ҳ���ù��������
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
     
     //����wifilock�Ĺ��ܣ�����ϵͳ��������£���wifi�رա�
     /**
 	 * 
 	 * @param lockName
 	 *            ��������
 	 * @param lockType
 	 * 
 	 *            WIFI_MODE_FULL == 1 <br/>
 	 *            ɨ�裬�Զ��ĳ���ȥ����һ���������ù��ĵ�<br />
 	 *            WIFI_MODE_SCAN_ONLY == 2 <br/>
 	 *            ֻʣ��ɨ��<br />
 	 *            WIFI_MODE_FULL_HIGH_PERF = 3 <br/>
 	 *            �ڵ�һ��ģʽ�Ļ����ϣ������������<br />
 	 * @return wifiLock
 	 */
 	public WifiLock  createWifiLock(String lockName, int lockType)
 	{
 		wifiLock = wifiManager.createWifiLock(lockType, lockName);
 		 return wifiLock;
 	}
 	
	/**
	 * ������
	 */
	public void lockWifi()
	{
		wifiLock.acquire();
	}
	
	/**
	 * �ͷ���
	 */
	public void releaseLock()
	{
		if (wifiLock.isHeld())
		{
			wifiLock.release();
		}
	}
	/**
	 * �ж�wifi�����Ƿ����
	 * 
	 * @return
	 */
	public boolean isHeld()
	{
		return wifiLock.isHeld();
	}
}
