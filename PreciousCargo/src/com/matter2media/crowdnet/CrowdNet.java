package com.matter2media.crowdnet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.osmdroid.util.GeoPoint;

import com.matter2media.crowdz.preciouscargo.R;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Tim Kindberg <tim@matter2media.com>
 * 
 * @since Nov 16, 2011
 *
 * The Android service responsible for transmitting data bwteen this and other nodes
 * 
 * See http://www.java2s.com/Open-Source/Android/Location/wifispy/com/synthable/wifispy/services/WifiSpyService.java.htm
 * 
 */
public class CrowdNet extends Service implements LocationListener
{
	public final static String 	DEFAULT_CROWD_NAME = "Precious";
	public final static String 	DEFAULT_CROWD_PASSWORD = "Cr0wdz77";

	public static final String CROWDNET_UPDATE = "com.matter2media.crowdz.preciouscargo.CROWDNET_UPDATE";
	
	public static final int		CROWDNET_STATE_NONE = 0;
	public static final int		CROWDNET_STATE_SYNC = 1;
	public static final int		CROWDNET_STATE_HOLD = 3;
	
	private WiFiController		mWiFiController;
	private CrowdServer			mCrowdServer;
	private int					mAPcount;
	private int					mScanCount;
	private SharedPreferences 	mSettings;
	private int					mState;
	private long				mSequence;
	private String 				mLocationProvider;
	private String				mMyUniqueId;
	private Hold				mThisHold;
	private HashMap<String, Hold>		mHolds; // All the Holds we have heard of, by BSSID. Some may no longer be visible (in scan range)
	private SyncTask			mSyncTask;


    /**
     * Class for clients to access. Because (in this prototype) this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class CrowdNetServiceBinder extends Binder 
    {
        public CrowdNet getService() 
        {
           	report("CrowdNet getService");
            return CrowdNet.this ;
        }
    }

    private final IBinder mBinder = new CrowdNetServiceBinder();

    @Override
    public IBinder onBind(Intent intent) 
    {
        return mBinder;
    }

    @Override
    public void onCreate()
    {
        mHolds = new HashMap<String, Hold>();
        mSettings = getSharedPreferences( getString( R.string.prefs ), Context.MODE_PRIVATE );
        mWiFiController = new WiFiController( this );
        mMyUniqueId = new DeviceUuidFactory( this ).getDeviceUuid().toString();
        mCrowdServer = new CrowdServer( this, 8080 );
       	mCrowdServer.startServer();
       	report("Started CrowdServer...");
        mState = CROWDNET_STATE_NONE;
        mSequence = mSettings.getLong( getString( R.string.prefs_crowd_sequence ), 0 );
        mSyncTask = null;
        setCrowd();
    }

    @Override
    public void onStart(Intent intent, int startId) 
    {
    	report("CrowdNet Service is starting...");
    	mCrowdServer.startServer();
    	//startCrowdNet();
    }

    @Override
    public void onDestroy() 
    {
        super.onDestroy();
        mWiFiController.shutdown();
        mCrowdServer.shutdown();
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putLong( getString( R.string.prefs_crowd_sequence ), mSequence );
        report("CrowdNet Service has stopped");
        editor.commit();
    }

    @Override
    public void onRebind(Intent intent) 
    {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) 
    {
        return super.onUnbind(intent);
    }
    
    public synchronized void notifyHandlers()
    {
        Intent intent = new Intent( CROWDNET_UPDATE );
        sendBroadcast(intent);
    }

    public List<CrowdData> getObjectsSince()
    {
    	return mCrowdServer.getObjectsSince();
    }
    
    /*
     * Advice that the crowd parameters may have changed
     */
    public void setCrowd()
    {
    	
    }
    
	public void sendCrowdData( CrowdData crowdData )
	{
		mCrowdServer.addData( crowdData );
	}
	
	public void sendString( String topic, String data )
	{
		mCrowdServer.addData( new CrowdData( topic, mMyUniqueId, mSequence++, "text/plain", data ) );
		notifyHandlers();
	}
	
	public void dataFromServer( CrowdData crowdData )
	{
		
	}
	
	public String getCrowdName()
	{
		return mSettings.getString( getString( R.string.prefs_crowd_name ), DEFAULT_CROWD_NAME );
	}
	
	public String getCrowdPassword()
	{
		return mSettings.getString( getString( R.string.prefs_crowd_password ), DEFAULT_CROWD_PASSWORD );
	}
	
	public String getYourName()
	{
		return mSettings.getString( getString( R.string.prefs_your_name ), "" );
	}
	
	public String getCrowdStateForHoldName()
	{
		return "0000";
	}
	
   /*
    * State management
	*/
	public int getState()
	{
		return mState;
	}
	
	public String getStateAsString()
	{
		switch( mState )
		{
			case CROWDNET_STATE_NONE:
				return "crowdnet inactive";
				
			case CROWDNET_STATE_SYNC:
				return "after " + mScanCount + " scans crowdnet seeing " + mAPcount + " APs of which " + getHoldsInRange().size() + " are holds";
				
			case CROWDNET_STATE_HOLD:
				return "crowdnet hold is called " + mThisHold.getHoldName();
			
			default:
				return "crowdnet state unknown";
			
		}
		
	}
	
	public void setStateNone()
	{
		mWiFiController.setStateNone();
		mState = CROWDNET_STATE_NONE;
	}
	
	public void setStateHold( )
	{
		setStateHold( null );
	}
	
	public void setStateHold( GeoPoint point )
	{
		//mWiFiManager.setWifiEnabled( true );
		if ( point == null )
		{
			Criteria criteria = new Criteria();
			criteria.setAccuracy( Criteria.ACCURACY_FINE );
			criteria.setAltitudeRequired( false );
			LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
			mLocationProvider = lm.getBestProvider( criteria, true );
			if ( mLocationProvider != null )
			{
				lm.requestLocationUpdates( mLocationProvider, 10000L, 500.0f, this);
			}
			setWiFiAP( null );
		}
		else
		{
			setWiFiAP( point );
		}
		mState = CROWDNET_STATE_HOLD;
	}
	
	public void setStateSync()
	{
		mWiFiController.setToScan();
		mState = CROWDNET_STATE_SYNC;
	}
	
	public void doSync( SyncTask task )
	{
		/*
		 * TODO queue of syncTasks
		 */
		if ( mState == CROWDNET_STATE_SYNC )
		{
			if ( mSyncTask == null || mSyncTask.isFinished() )
			{
				mSyncTask = task;
				mSyncTask.doSync();
			}
		}
	}
	
	private void setWiFiAP( GeoPoint point )
	{
		String holdName = getHoldName( point );
		try
		{
			mThisHold = new Hold( this.mMyUniqueId, holdName );
			mWiFiController.setWiFiAP( holdName, getCrowdPassword(), true );
		}
		catch ( Exception ex )
		{
			report("Problem setting hold " + ex);
		}
	}
	
	public String getHoldName( GeoPoint point )
	{
		return point == null ? HoldName.getDefaultHoldName() : HoldName.getHoldName( point, getCrowdStateForHoldName(), getYourName() );
	}
	

   public void handleScan( List<ScanResult> wifiList )
   {
		if ( mState == CROWDNET_STATE_SYNC )
		{
		   ++mScanCount;
		   mAPcount = wifiList.size();
		   
		   // Guilty until proved innocent
		   for (Hold hold : mHolds.values()) 
		   {
			    hold.setNotInRange();
		   }
		   
	       for (int i = 0; i < wifiList.size(); i++)
	       {
	       	   String bssid = wifiList.get(i).BSSID;
	       	   String ssid = wifiList.get(i).SSID;
	    	   Hold already = mHolds.get( bssid );
	    	   try
	    	   {
		    	   if ( already == null )
		           {
		      		   Hold hold = new Hold( bssid, ssid );
		      		   hold.setInRange();
		      		   mHolds.put( bssid, hold );
		           }
		    	   else
		    	   {
		         	   already.setInRange( ssid );
		    	   }     
	    	   }
	    	   catch ( HoldName.NotAHoldNameException ex )
	    	   {
	    		   if ( already != null ) mHolds.remove( bssid );
	    	   }
	       }
	       notifyHandlers();
		}
   }

   public HashSet<Hold> getHoldsInRange()
   {
	   HashSet<Hold> holdsInRange = new HashSet<Hold>();
	   for (Hold hold : mHolds.values()) 
	   {
		    if ( hold.isInRange() ) holdsInRange.add( hold );
	   }
	   return holdsInRange;
   }
   
   public void report( String msg )
   {
	   Log.v("CrowdNet", msg );
   }

   @Override
   public void onLocationChanged( Location location ) 
   {
	   if ( location.getAccuracy() < 500.0 )
	   {
		   GeoPoint p = new GeoPoint( location.getLatitude(),location.getLongitude() );
		   setWiFiAP( p );
	   }
	   else
	   {
		   setWiFiAP( null );
	   }
   }

   @Override
   public void onProviderDisabled(String provider) 
   {
   	// TODO Auto-generated method stub
   	
   }

   @Override
   public void onProviderEnabled(String provider) 
   {
   	// TODO Auto-generated method stub
   	
   }

   @Override
   public void onStatusChanged(String provider, int status, Bundle extras) 
   {
   	// TODO Auto-generated method stub
   	
   }

   /*
    * WiFi management
	*/
   private void enableWiFiMulticast()
   {
   		mWiFiController.enableWiFiMulticast();
   }

}
