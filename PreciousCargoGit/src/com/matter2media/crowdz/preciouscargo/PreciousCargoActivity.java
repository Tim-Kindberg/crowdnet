package com.matter2media.crowdz.preciouscargo;

import java.lang.reflect.Method;
import java.util.List;

import com.matter2media.crowdz.preciouscargo.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PreciousCargoActivity extends Activity 
{
 
	private TextView 			mOutput;
	private EditText 			mSendText;
	private Button				mSendButton;
	public static CrowdNet		mCrowdNet;
	private BroadcastReceiver 	mCrowdNetStatusReceiver;
	
    private ServiceConnection mCrowdNetConnection = new ServiceConnection() 
    {

		public void onServiceConnected(ComponentName className, IBinder binder) 
		{
			mCrowdNet = ((CrowdNet.CrowdNetServiceBinder) binder).getService();
			report("CrowdNet connected");
		}

		public void onServiceDisconnected(ComponentName className) {
			mCrowdNet = null;
		}
	};

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mOutput = (TextView) findViewById(R.id.outputTextView);
        mSendText = (EditText)findViewById(R.id.croakText);
        mSendButton = (Button)findViewById(R.id.sendButton);
        
        mSendButton.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v) 
            {
                croakTheMessage( mSendButton.getText() );
            }
        });

        mCrowdNetStatusReceiver = new BroadcastReceiver() 
        {
			@Override
			public void onReceive(Context context, Intent intent) 
			{
				CrowdData[] crowdData = mCrowdNet.getObjectsSince();
				
			}
        	
        };
        
        try 
        {
        	if ( mCrowdNet == null )
        	{
        		doBindService();
        	}
        	// Let (single) CrowdNet instance get what it needs from the shared prefs
        	//startService(new Intent( this, CrowdNet.class));
        }
        catch ( Exception ex )
        {
        	report("Problem creating crowdnet: " + ex );
        }
    }
    
	void doBindService() 
	{
		bindService(new Intent(this, CrowdNet.class), mCrowdNetConnection,
				Context.BIND_AUTO_CREATE);
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.map_item:
        	Intent mapIntent = new Intent( this, CrowdOSMapActivity.class );
        	startActivity( mapIntent );
            return true;
            
        case R.id.status_item:
        	Intent statusIntent = new Intent( this, StatusActivity.class );
        	startActivity( statusIntent );
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
    
    public static CrowdNet getCrowdNet()
    {
    	return mCrowdNet;
    }
    
    public void croakTheMessage( CharSequence msg )
    {
    	String toSend = msg.toString();
    	toSend.trim();
    	if ( toSend.length() > 0 ) mCrowdNet.sendString( msg.toString() );
    }
    
    public void report( String msg )
    {
    	mOutput.setText( msg );
    }
    
}