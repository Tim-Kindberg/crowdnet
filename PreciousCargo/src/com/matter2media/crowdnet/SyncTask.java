/**
 * Crowdnet
 * Licence information here
 *
 */
package com.matter2media.crowdnet;

import android.widget.Toast;

/**
 * @author Tim Kindberg <tim@matter2media.com>
 *
 * @since Nov 17, 2011
 * 
 * A class to manage the Syncing of state between two holds
 * 
 */
public class SyncTask
{
	public final int STATE_INITIALISED = 	0;
	public final int STATE_PULLED1 = 		1;
	public final int STATE_PUSHED2 = 		1;
	public final int STATE_PUSHED1 = 		2;
	
	private int		mState;
	private Hold	mHold1;
	private Hold	mHold2;
	
	public SyncTask( Hold hold1, Hold hold2 )
	{
		mState = STATE_INITIALISED;
	}
	
	public void doSync()
	{
		
	}
	
	public int getState()
	{
		return mState;
	}
	
	public boolean isFinished()
	{
		return false;
	}
	
	public void onConnectedTo( String bssid )
	{
		if ( bssid.equals( mHold1.mBSSID ) )
		{
			
		}
		else
			if ( bssid.equals( mHold2.mBSSID ) )
			{
				
			}
	}
	
	   /*
	    * Synchronisation operations
	    */
	/*
	   public String extractData( Hold from )
	   {
		   if ( mWiFiController.connectToAp( from.mBSSID, this.getCrowdPassword() ) )
		   {
			   String result = new CrowdServerClient().getStoreContents();
			   mWiFiController.disconnect();
			   return result;
		   }
		   report("extractData Could not connect to AP");
		   return null;
	   }
	   
	   	      		   
	      		   if ( !extracted )
	      		   {
	      			   extracted = true;
	      			   String result = extractData( hold );  
		      		   Toast.makeText(this , "Extracted " + result, Toast.LENGTH_LONG).show();
	      		   }

	   */

}
