package com.matter2media.crowdz.preciouscargo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.matter2media.crowdz.preciouscargo.R;

public class HelpActivity extends Activity 
{
	//TextView 		mOutput;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        
        //mOutput = (TextView) findViewById(R.id.outputTextView);

        //report("Hello" );
        
     }
    
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.help_menu, menu);
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
            
        case R.id.config_item:
        	Intent helpIntent = new Intent( this, HelpActivity.class );
        	startActivity( helpIntent );
            return true;
            
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    */
    
    /*
    public void report( String msg )
    {
    	mOutput.setText( msg );
    }
    */
}