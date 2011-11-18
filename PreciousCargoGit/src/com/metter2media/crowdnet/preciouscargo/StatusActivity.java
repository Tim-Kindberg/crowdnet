package com.metter2media.crowdnet.preciouscargo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.matter2media.crowdnet.CrowdNet;
import com.matter2media.crowdz.preciouscargo.R;

/**
 * @author Tim Kindberg <tim@matter2media.com>
 *
 * @since Nov 16, 2011
 * 
 * Simple interface to control user's role and see info about network inscan range
 * 
 * TODO Eliminate this activity in favour of the map (and/or other more helpful interfaces to crowdnet) 
 * 
 */
public class StatusActivity extends Activity 
{
	private	TextView 			mOutput;
	private	RadioGroup			mRoleGroup;
	private	CrowdNet			mCrowdNet;
	private	Thread				mUpdateStatusThread;
	private volatile boolean	mPaused;
	private Handler				mCrowdNetStatusHandler;
	private BroadcastReceiver	mCrowdNetStatusReceiver;
	private int					mPollCount;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status);
        
        mCrowdNet = PreciousCargoActivity.getCrowdNet();
         
        mOutput = (TextView) findViewById(R.id.outputTextView);
        mRoleGroup = (RadioGroup) findViewById(R.id.radioroles);
        
        mRoleGroup.setOnCheckedChangeListener( new OnCheckedChangeListener()
        	{

				@Override
				public void onCheckedChanged(RadioGroup radioGroup, int checkedId) 
				{
					if ( radioGroup.equals( mRoleGroup ) )
					{
						try
						{
							switch( checkedId )
							{
								case R.id.radio_role_none:
									mCrowdNet.setStateNone();
									break;
									
								case R.id.radio_role_hold:
									//report( mCrowdNet.getWiFiManagerMethods() );
									mCrowdNet.setStateHold();
									break;
									
								case R.id.radio_role_sync:
									mCrowdNet.setStateSync();
									break;
									
								default:
									break;	
							}
						}
						catch ( Exception ex )
						{
							report("Problem setting state: " + ex);
						}
					}
				}
        	
        	}
        );
        
        mCrowdNetStatusReceiver = new BroadcastReceiver() 
        {
			@Override
			public void onReceive(Context context, Intent intent) 
			{
	           	 if ( !mPaused ) showStatus();
			}
        	
        };
        
        // Not used any more - neither is the polling thread that sends messages
        mCrowdNetStatusHandler = new Handler() 
        { 
             public void handleMessage(Message msg) 
             { 
            	 if ( !mPaused ) showStatus();
             } 
        }; 
        
        mPollCount = 0;
     }

    @Override
    public void onPause() 
    {
    	super.onPause();
    	unregisterReceiver(mCrowdNetStatusReceiver);
    	//stopStatusUpdates();
    }
    
    @Override
    public void onResume() 
    {
    	super.onResume();
    	report("onResume");
        IntentFilter crowdNetUpdateFilter;
        crowdNetUpdateFilter = new IntentFilter( CrowdNet.CROWDNET_UPDATE );
        registerReceiver(mCrowdNetStatusReceiver , crowdNetUpdateFilter);
        setRadioButtonState();
    	//startStatusUpdates();
    }
    
    @Override
    public void onDestroy() 
    {
    	super.onDestroy();
    }
    
    private void setRadioButtonState()
    {
        switch ( mCrowdNet.getState() )
        {
        case CrowdNet.CROWDNET_STATE_NONE:
        	((RadioButton) findViewById(R.id.radio_role_none)).setChecked(true);
        	break;
    	
        case CrowdNet.CROWDNET_STATE_SYNC:
        	((RadioButton) findViewById(R.id.radio_role_sync)).setChecked(true);
        	break;
    	
        case CrowdNet.CROWDNET_STATE_HOLD:
        	((RadioButton) findViewById(R.id.radio_role_hold)).setChecked(true);
        	break;
    	
        default:
        	break;
    	
        }
    }
    
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.status_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.croak_item:
        	Intent croakIntent = new Intent( this, CroakActivity.class );
        	startActivity( croakIntent );
            return true;
            
        case R.id.config_item:
        	Intent configIntent = new Intent( this, ConfigActivity.class );
        	startActivity( configIntent );
            return true;
            
       case R.id.help_item:
        	Intent helpIntent = new Intent( this, HelpActivity.class );
        	startActivity( helpIntent );
            return true;
            
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	*/
    
    public void showStatus()
    {
		/*
		 MediaPlayer player = MediaPlayer.create(this,
			    Settings.System.DEFAULT_NOTIFICATION_URI);
			player.start();
		 */
    	report( "(" + mPollCount++ + ") " + mCrowdNet.getStateAsString() );
    	//report( "(" + mPollCount++ + ") " + "I'm just a dummy" );
    }
    
    public void report( String msg )
    {
    	mOutput.setText( msg );
    }
    
    private void startStatusUpdates()
    {
        mUpdateStatusThread = new Thread( new Runnable()
        {

			@Override
			public void run() 
			{
				//Toast.makeText(StatusActivity.this , "Thread begins...", Toast.LENGTH_SHORT).show();
				while( !mPaused )
				{
					// Wasted hours debugging problem that turned out to be
					// because following line was before loop -- don't re-use Messages!
					Message dummy = mCrowdNetStatusHandler.obtainMessage();
					mCrowdNetStatusHandler.sendMessage(dummy);
					try
					{
						Thread.currentThread().sleep(2000);
					}
					catch ( Exception uninteresting ) {}
				}
				//Toast.makeText(StatusActivity.this , "Thread over...", Toast.LENGTH_SHORT).show();
			}
        	
        }
        );
        mPaused = false;
        mUpdateStatusThread.start();
    }
    
    private void stopStatusUpdates()
    {
        mPaused = true;
        mUpdateStatusThread.interrupt();
    }
}