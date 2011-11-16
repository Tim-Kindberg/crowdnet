package com.matter2media.crowdz.preciouscargo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.matter2media.crowdz.preciouscargo.R;

public class ConfigActivity extends Activity 
{
	public final static String 	DEFAULT_CROWD_NAME = "Precious";
	public final static String 	DEFAULT_CROWD_PASSWORD = "Cr0wdz77";
	public final static String 	DEFAULT_LAT = "55.659516";
	public final static String 	DEFAULT_LONG = "12.591619";
	public final static String 	DEFAULT_ZOOM = "17";
	EditText 			mYourNameEditText;
	EditText 			mCrowdNameEditText;
	EditText 			mCrowdPasswordEditText;
	EditText 			mLatEditText;
	EditText 			mLongEditText;
	EditText 			mZoomEditText;
	Button				mConfigButton;
	TextView 			mOutput;
	//ImageView			mMap;
    float 				mTouchX;
    float				mTouchY;
	SharedPreferences 	mSettings;
	CrowdNet			mCrowdNet;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.config);
        
        mCrowdNet = PreciousCargoActivity.getCrowdNet();

        mYourNameEditText = (EditText) findViewById(R.id.yourName);
        mCrowdNameEditText = (EditText) findViewById(R.id.crowdName);
        mCrowdPasswordEditText = (EditText) findViewById(R.id.crowdPassword);
        mLatEditText = (EditText) findViewById(R.id.mapLat);
        mLongEditText = (EditText) findViewById(R.id.mapLong);
        mZoomEditText = (EditText) findViewById(R.id.mapZoom);
        mConfigButton = (Button) findViewById(R.id.ok);
        mOutput = (TextView) findViewById(R.id.outputTextView);
        mOutput.setText( "" );
        
        mSettings = getSharedPreferences( getString( R.string.prefs ), Context.MODE_PRIVATE );
        mYourNameEditText.setText( mSettings.getString( getString( R.string.prefs_your_name ), "" ) );
        mCrowdNameEditText.setText( mSettings.getString( getString( R.string.prefs_crowd_name ), DEFAULT_CROWD_NAME ) );
        mCrowdPasswordEditText.setText( mSettings.getString( getString( R.string.prefs_crowd_password ), DEFAULT_CROWD_PASSWORD ) );
        mLatEditText.setText( mSettings.getString( getString( R.string.prefs_map_lat ), DEFAULT_LAT ) );
        mLongEditText.setText( mSettings.getString( getString( R.string.prefs_map_long ), DEFAULT_LONG ) );
        mZoomEditText.setText( mSettings.getString( getString( R.string.prefs_map_zoom ), DEFAULT_ZOOM ) );
        mConfigButton.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v) 
            {
                String yourName = mYourNameEditText.getText().toString().trim();
                String crowdName = mCrowdNameEditText.getText().toString().trim();
                String crowdPassword = mCrowdPasswordEditText.getText().toString().trim();
                String lat = mLatEditText.getText().toString().trim();
                String lng = mLongEditText.getText().toString().trim();
                String zoom = mZoomEditText.getText().toString().trim();
                if ( crowdName.length() > 0 && crowdPassword.length() > 0 )
                {
                	setCrowdNameAndPassword( yourName, crowdName, crowdPassword );
                	setMap( lat, lng, zoom );
                	report("Configuration updated");
                }
                else
                	report("Please enter a crowd name and password");
            }
        });

        //report("Enter crowd details above!" );

        /*
        mMap = (ImageView) this.findViewById(R.id.map);
        
        mMap.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View arg0, MotionEvent event) {

                float curX, curY;
                
                switch (event.getAction()) 
                {
                     case MotionEvent.ACTION_DOWN:
                        report("Down");
                        mTouchX = event.getX();
                        mTouchY = event.getY();
                        break;
                        
                    case MotionEvent.ACTION_MOVE:
                        //report("Move " + mTouchX + ", " + mTouchY);
                        curX = event.getX();
                        curY = event.getY();
                        mMap.scrollBy((int) (mTouchX - curX), (int) (mTouchY - curY));
                        mTouchX = curX;
                        mTouchY = curY;
                        break;
                        
                        
                    case MotionEvent.ACTION_UP:
                        break;
                        
                    default:
                        report("Unknown " + event.getAction());
                        break;
                }
                return true;
            }
        });
        */
     }
    
    public void setCrowdNameAndPassword( String yourName, String crowdName, String crowdPassword )
    {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString( getString( R.string.prefs_your_name ), yourName );
        editor.putString( getString( R.string.prefs_crowd_name ), crowdName );
        editor.putString( getString( R.string.prefs_crowd_password ), crowdPassword );
        editor.commit();
        mCrowdNet.setCrowd();
    }
    
    public void setMap( String lat, String lng, String zoom )
    {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString( getString( R.string.prefs_map_lat ), lat );
        editor.putString( getString( R.string.prefs_map_long ), lng );
        editor.putString( getString( R.string.prefs_map_zoom ), zoom );
        editor.commit();
    }
    
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.config_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.status_item:
        	Intent statusIntent = new Intent( this, StatusActivity.class );
        	startActivity( statusIntent );
            return true;
            
        case R.id.croak_item:
        	Intent croakIntent = new Intent( this, CroakActivity.class );
        	startActivity( croakIntent );
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

    
    public void report( String msg )
    {
    	mOutput.setText( msg );
    }
    
}