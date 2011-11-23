package com.matter2media.crowdnet;

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
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.widget.EditText;
import android.widget.Toast;


/**
 * @author Tim Kindberg <tim@matter2media.com>
 *
 * @since Nov 17, 2011
 * 
 * A class used to control the WiFi state, used by a CrowdNet
 * 
 */
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
        mConnectedApBSSID = null;
        IntentFilter intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction( WifiManager.WIFI_STATE_CHANGED_ACTION );
        intentFilter.addAction( WifiManager.NETWORK_STATE_CHANGED_ACTION );
		mCrowdNet.registerReceiver( mWiFiReceiver, intentFilter );
		mConnectedApBSSID = currentlyConnectedBSSID();
        mState = ( mConnectedApBSSID == null ? WIFI_STATE_NONE : WIFI_STATE_CONNECTED );
		report ("WiFiController: isWiFiAPOn() returns " + isWiFiAPOn() );
	}
	    
    public void setModeScanOnly()
    {
        WifiLock lock = mWiFiManager.createWifiLock( WifiManager.WIFI_MODE_SCAN_ONLY, "CrowdNet" );
        if ( !lock.isHeld() ) lock.acquire();
    }
    
    public void setModeFull()
    {
        WifiLock lock = mWiFiManager.createWifiLock( WifiManager.WIFI_MODE_FULL, "CrowdNet" );
        if ( !lock.isHeld() ) lock.acquire();
    }
    
    public void enableWiFiMulticast()
    {
        WifiManager.MulticastLock multicastLock = mWiFiManager.createMulticastLock("CrowdNet");
        multicastLock.acquire();
    }
    
    public int getWifiState()
    {
    	return this.mWiFiManager.getWifiState();
    }
    
    public boolean isWifiEnabled()
    {
    	return this.mWiFiManager.isWifiEnabled();
    }
    
    public String currentlyConnectedBSSID()
    {
    	String bssid;
    	WifiInfo wifiInfo = this.mWiFiManager.getConnectionInfo();
    	if ( wifiInfo != null && ( bssid = wifiInfo.getBSSID() ) != null )
    		return bssid;
    	return null;
    }
    
	public void shutdown()
	{
		setStateNone();
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
		if ( currentlyConnectedBSSID() != null )
		{
			if ( !enabled ) return;
			mWiFiManager.disconnect();
			mState = WIFI_STATE_AP;
			mApSSID = ssid;
			mApPassword = password;
			return;
		}
		
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
        			report("Set AP for " + ssid + " " + password + " enabled " + enabled );
        			foundApMethod = true;
        			method.invoke(mWiFiManager, wifiConfiguration, enabled);
        			mApSSID = ssid;
        			mApPassword = password;
        			mState = WIFI_STATE_AP;
        			report ("isWiFiAPOn() returns " + isWiFiAPOn() );
        			break;
        		}
        		//int netty = wifi.addNetwork(_wifi_configuration);
        		//wifi.enableNetwork(netty, true);
        	}
        }
        catch ( Exception ex )
        {
        	report("Problem setting AP mode " + ex );
        }
        if ( !foundApMethod )
        	report("ApMethod not found");
    }

	public boolean isWiFiAPOn()
	{
		/*
		 * TODO needs to throw exception if no such method
		 */
		try
		{
			Method method = mWiFiManager.getClass().getMethod( "getWifiApConfiguration" );
			WifiConfiguration config = (WifiConfiguration)method.invoke( mWiFiManager );
			return ( config != null && config.status == WifiConfiguration.Status.ENABLED );
		}
		catch ( Exception e )
		{
			
		}
		return false;
	}
	
	public void setWiFiAPOff( )
	{
		setWiFiAP( mApSSID, mApPassword, false );
		report ("setWiFiAPOff: isWiFiAPOn() returns " + isWiFiAPOn() );
	}
	
	public void setStateNone()
	{
		switch ( mState )
		{
			case WIFI_STATE_NONE:
				return;
				
			case WIFI_STATE_AP:
				setWiFiAPOff( );
				break;
				
			case WIFI_STATE_SCAN:
				return;
				
			case WIFI_STATE_CONNECTED:
				break;
		}
		mState = WIFI_STATE_NONE;
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
		if ( isWifiEnabled() ) 
			mWiFiManager.startScan();  
		else
			if ( this.getWifiState() != WifiManager.WIFI_STATE_ENABLING )
				mWiFiManager.setWifiEnabled( true );
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
   
   public boolean connectToAp( String bssid, String password )
   {
	   WifiInfo wiFiInfo = mWiFiManager.getConnectionInfo();
	   String existingConnectionBSSID = null;
	   if ( wiFiInfo != null && wiFiInfo.getNetworkId() >= 0 )
	   {
		   if ( wiFiInfo.getBSSID().equals( bssid ) ) 
		   {
			   return true;
		   }
		   else
		   {
			   existingConnectionBSSID = wiFiInfo.getBSSID();
		   }
	   }
       // Find highest priority & any existing config for this BSSID
	   List<WifiConfiguration> existingConfigs = mWiFiManager.getConfiguredNetworks();
	   int maxPriority = -1000;
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
       if ( existingConnectionBSSID != null ) try { mWiFiManager.disconnect(); } catch ( Exception e ) {}

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
       
       int maximumPolls = 15;
	   while ( --maximumPolls >= 0 && ( ( wiFiInfo = mWiFiManager.getConnectionInfo() ) == null || wiFiInfo.getNetworkId() < 0 ) )
	   {
		   try { Thread.currentThread().sleep( 200 ); } catch ( Exception e ) {}
	   }
	   
	   return ( ( wiFiInfo = mWiFiManager.getConnectionInfo() ) != null && wiFiInfo.getNetworkId() >= 0 );
   }
   
   public void disconnect()
   {
	   if ( mConnectedApBSSID != null ) 
	   {
		   try 
		   { 
			   mWiFiManager.disconnect(); 
			   mConnectedApBSSID = null;
		   } 
		   catch ( Exception e ) {}
	   }
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
    	   if ( intent.getAction().equals(  WifiManager.SCAN_RESULTS_AVAILABLE_ACTION ))
    	   {
    		   /*
    		    * TODO: CONTROL SCAN FREQUENCY
    		    */
    		   handleScan( mWiFiManager.getScanResults() );
    	   }
    	   else
        	   if ( intent.getAction().equals(  WifiManager.WIFI_STATE_CHANGED_ACTION ))
        	   {
        		   int state = intent.getIntExtra( WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN );
        		   switch ( state )
        		   {
        		   		case WifiManager.WIFI_STATE_DISABLED:
        		   			break;
        		   			
        		   		case WifiManager.WIFI_STATE_DISABLING:
        		   			break;
        		   			
        		   		case WifiManager.WIFI_STATE_ENABLED:
        		   			switch ( mState )
        		   			{
        		   				case WIFI_STATE_NONE:
        		   					break;
        		   					
        		   				case WIFI_STATE_AP:
        		   					break;
        		   					
        		   				case WIFI_STATE_SCAN:
        		   					mWiFiManager.startScan();
        		   					break;
        		   					
        		   				case WIFI_STATE_CONNECTED:
        		   					break;
        		   			}
        		   			break;
        		   			
        		   		case WifiManager.WIFI_STATE_ENABLING:
        		   			break;
        		   			
         			   default:
         				   break;
        		   }
        	   }
        	   else
               	   if ( intent.getAction().equals(  WifiManager.NETWORK_STATE_CHANGED_ACTION ))
               	   {
               		   	NetworkInfo info = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
						if (info.getState().equals(NetworkInfo.State.CONNECTED))
						{
						}
						else if ( info.getState().equals(NetworkInfo.State.DISCONNECTED) )
						{
							   mConnectedApBSSID = null;
							if ( mState == WIFI_STATE_AP )
							{
								setWiFiAP( mApSSID, mApPassword, true ); 
				       			report ("isWiFiAPOn() returns " + isWiFiAPOn() );
							}
						}
              	   }
       }
   }


}
