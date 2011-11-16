package com.matter2media.crowdz.preciouscargo;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.matter2media.crowdz.preciouscargo.R;

public class CrowdMapActivity extends MapActivity 
{
	//TextView 		mOutput;
	MapView					mMapView;
	ArrayList<OverlayItem>	mOverlays = new ArrayList<OverlayItem>();	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        
        mMapView = (MapView) findViewById(R.id.mapview);
        mMapView.setBuiltInZoomControls(true);
        
        //mOutput = (TextView) findViewById(R.id.outputTextView);

        //report("Hello" );
        
     }
    
    @Override
    protected boolean isRouteDisplayed() 
    {
        return false;
    }    
    
    /*
    public void report( String msg )
    {
    	mOutput.setText( msg );
    }
    */
}