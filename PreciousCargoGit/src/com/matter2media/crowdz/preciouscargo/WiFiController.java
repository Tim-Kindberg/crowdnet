package com.matter2media.crowdz.preciouscargo;

import java.lang.reflect.Method;
import java.util.List;

import org.osmdroid.util.GeoPoint;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.EditText;
import android.widget.Toast;


public class WiFiController
{
	public static final int		WIFI_STATE_NONE = 0;
	public static final int		WIFI_STATE_AP = 1;
	public static final int		WIFI_STATE_SCAN = 2;
	public static final int		WIFI_STATE_CONNECTED = 3;
	
	private	CrowdNet 			mCrowdNet;
	private WifiManager 		mWiFiManager;
	private WifiReceiver 		mWiFiReceiver;
	private int					mState;
	private String				mMACaddress;
	private String 				mApSSID;
	private String 				mApPassword;
	private String				mConnectedApBSSID;

	public WiFiController( CrowdNet crowdNet )
	{
		mCrowdNet = crowdNet;
        mWiFiManager = (WifiManager) mCrowdNet.getSystemService(Context.WIFI_SERVICE);
        mWiFiReceiver = new WifiReceiver();
        mState = WIFI_STATE_NONE;
        mConnectedApBSSID = null;
	}
	    
    public void enableWiFiMulticast()
    {
        WifiManager.MulticastLock multicastLock = mWiFiManager.createMulticastLock("CrowdNet");
        multicastLock.acquire();
    }
    
	public void send( String data )
	{
		
	}
	
	public void shutdown()
	{
		try { mCrowdNet.unregisterReceiver( this.mWiFiReceiver ); } catch ( Exception e ) {}
	}
	
	public void turnWiFiOff()
	{
		mWiFiManager.setWifiEnabled( false );
		mConnectedApBSSID = null;
	}
	
	public void turnWiFiOn()
	{
		mWiFiManager.setWifiEnabled( true );
	}
	
	public void setWiFiAP(String ssid, String password, boolean enabled)
	{
        WifiConfiguration wifiConfiguration = new WifiConfiguration(); 

        wifiConfiguration.SSID = ssid;
        wifiConfiguration.preSharedKey  = password;
        wifiConfiguration.hiddenSSID = false;
        wifiConfiguration.status = enabled ? WifiConfiguration.Status.ENABLED : WifiConfiguration.Status.DISABLED;   

        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);

        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);           
        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

        boolean foundApMethod = false;
        try
        {
        	
        	Method[] wmMethods = mWiFiManager.getClass().getDeclaredMethods();
        	
        	for( Method method: wmMethods )
        	{
        		//report(method.getName());
        		if ( method.getName().equals("setWifiApEnabled") )
        		{
        			foundApMethod = true;
        			method.invoke(mWiFiManager, wifiConfiguration, enabled);
        			mApSSID = ssid;
        			mApPassword = password;
        			break;
        		}
        		//int netty = wifi.addNetwork(_wifi_configuration);
        		//wifi.enableNetwork(netty, true);
        	}
        }
        catch ( Exception ex )
        {
        	report("Problem " + ex );
        }
        if ( !foundApMethod )
        	report("ApMethod not found");
    }

	public void setWiFiAPOff( )
	{
		setWiFiAP( mApSSID, mApPassword, false );
	}
	
	public void setToScan()
	{
		switch ( mState )
		{
			case WIFI_STATE_NONE:
				break;
				
			case WIFI_STATE_AP:
				setWiFiAPOff( );
				break;
				
			case WIFI_STATE_SCAN:
				return;
				
			case WIFI_STATE_CONNECTED:
				break;
		}
		mWiFiManager.setWifiEnabled( true );
		mCrowdNet.registerReceiver( mWiFiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) );
	    mWiFiManager.startScan();   
		mState = WIFI_STATE_SCAN;
	}

	public String getWiFiManagerMethods()
	{
		String methodString = "";
		Method[] wmMethods = mWiFiManager.getClass().getDeclaredMethods();
    	for( Method method: wmMethods )
    	{
    		methodString += method.getName() + "\n";
    	}
    	return methodString;
	}
	
   private void handleScan( List<ScanResult> wifiList )
   {
       mCrowdNet.handleScan( wifiList );
   }
   
   public void connectToAp( String bssid, String password )
   {
	   WifiInfo wiFiInfo = mWiFiManager.getConnectionInfo();
	   String existingConnectionBSSID = null;
	   if ( wiFiInfo != null && wiFiInfo.getNetworkId() >= 0 )
	   {
		   if ( wiFiInfo.getBSSID().equals( bssid ) ) 
		   {
			   return;
		   }
		   else
		   {
			   existingConnectionBSSID = wiFiInfo.getBSSID();
		   }
	   }
       // Find highest priority & any existing config for this BSSID
	   List<WifiConfiguration> existingConfigs = mWiFiManager.getConfiguredNetworks();
	   int maxPriority = -1;
	   int existingBssidConfigId = -1;
       for ( WifiConfiguration wifiConfig : existingConfigs )
       {
    	   if ( wifiConfig.priority > maxPriority ) maxPriority = wifiConfig.priority;
    	   if ( wifiConfig.BSSID != null )
    	   {
    		   if ( wifiConfig.BSSID.equals(bssid) )
	    	   {
	    		   existingBssidConfigId = wifiConfig.networkId;
	    	   }
    		   else 
    		   {
    			   	if ( existingConnectionBSSID != null && wifiConfig.BSSID.equals(existingConnectionBSSID) )
    			   	{
    			   		// Otherwise seems to want always to reconnect to existing even if not highest priority
    			   		wifiConfig.priority = -1;
    			   		mWiFiManager.updateNetwork( wifiConfig );
    			   	}
    		   }
    	   }
       }
       WifiConfiguration wc = new WifiConfiguration();
       wc.BSSID = bssid;
       wc.preSharedKey = "\"" + password + "\"";
       wc.status = WifiConfiguration.Status.ENABLED;
       wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
       wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
       wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
       wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
       wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
       wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
       wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
       wc.priority = maxPriority + 1;
       
       // connect to and enable the connection
       if ( existingConnectionBSSID != null ) mWiFiManager.disconnect();
       if ( existingBssidConfigId < 0 )
       {
    	   int netId = mWiFiManager.addNetwork(wc);
           mWiFiManager.enableNetwork(netId, true);
       }
       else
       {
    	   wc.networkId = existingBssidConfigId;
    	   mWiFiManager.updateNetwork(wc);
           mWiFiManager.enableNetwork( existingBssidConfigId, true );
       }
       mConnectedApBSSID = bssid;
       mWiFiManager.setWifiEnabled(true);
       
	   while ( ( wiFiInfo = mWiFiManager.getConnectionInfo() ) == null || wiFiInfo.getNetworkId() < 0)
	   {
		   try { Thread.currentThread().sleep( 100 ); } catch ( Exception e ) {}
	   }
   }
   
   public void disconnect()
   {
	   if ( mConnectedApBSSID != null ) mWiFiManager.disconnect();
   }
   
   /*
    * Only works when WiFi on?
    */
   public String getMACaddress()
   {
	   /*
	   if ( mMACaddress == null )
	   {
		   turnWiFiOn();
		   WifiInfo wifiInf = mWiFiManager.getConnectionInfo();
		   turnWiFiOff();
		   mMACaddress = wifiInf.getMacAddress();
	   }
	   */
	   return "56789"; //mMACaddress;
   }
   
   private void report( String msg )
   {
	   mCrowdNet.report( msg );
   }

   class WifiReceiver extends BroadcastReceiver 
   {
       public void onReceive(Context c, Intent intent) 
       {
       	report("onReceive");
       	handleScan( mWiFiManager.getScanResults() );
       }
   }


}
