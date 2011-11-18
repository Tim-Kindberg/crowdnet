package com.matter2media.crowdnet.preciouscargo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.example.qberticus.quickactions.BetterPopupWindow;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.matter2media.crowdnet.CrowdNet;
import com.matter2media.crowdnet.Hold;
import com.matter2media.crowdz.preciouscargo.R;

/**
 * @author Tim Kindberg <tim@matter2media.com>
 *
 * @since Nov 18, 2011
 * 
 * The activity for viewing the state of the crowdnet on the map
 * 
 * TODO Enable user to:
 * 	set position
 * 	assume or drop role of hold/syncer
 *  pick hold to use
 *  pick holds to synchronise
 *  
 * TODO Provide complimentary view for those uncomfortable with maps, eg list of holds by distance 
 * 
 */
public class CrowdOSMapActivity extends Activity
{
	//TextView 		mOutput;
	private CrowdNet				mCrowdNet;
	private SharedPreferences 		mSettings;
	private MapView					mMapView;
	private MapController			mMapController;
	private ItemizedOverlay<OverlayItem> mMyHoldsOverlay;
	private Overlay 				mMyNewHoldOverlay;
	private ResourceProxy 			mResourceProxy;
	private GestureDetector 		mGestureDetector;
	private ArrayList<OverlayItem> 	mOverlayItems;
	private int						mMyHoldIndex;
	private Drawable				mMyHoldMarker;
	private Drawable				mHoldMarker;
	private final ItemizedIconOverlay.OnItemGestureListener mOnItemGestureListener
    	= new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                    Toast.makeText(
                    		CrowdOSMapActivity.this,
                                    "Item '" + item.mTitle + "' (index=" + index
                                                    + ") got single tapped up", Toast.LENGTH_LONG).show();
                    return true; // We 'handled' this event.
            }

            @Override
            public boolean onItemLongPress(final int index, final OverlayItem item) {
                    Toast.makeText(
                    		CrowdOSMapActivity.this,
                                    "Item '" + item.mTitle + "' (index=" + index
                                                    + ") got long pressed", Toast.LENGTH_LONG).show();
                    return false;
            }
    };

    private final BroadcastReceiver	mCrowdNetStatusReceiver 
    	= new BroadcastReceiver () 
    	{
			@Override
			public void onReceive(Context context, Intent intent) 
			{
	        	updateMap();
			} 
    	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.osmap);
        
        //mGestureDetector = new GestureDetector(this);
        
        mCrowdNet = PreciousCargoActivity.getCrowdNet();
        
        mSettings = getSharedPreferences( getString( R.string.prefs ), Context.MODE_PRIVATE );

        Resources res = getResources();
        mMyHoldMarker = res.getDrawable(R.drawable.marker_my_hold);
        mHoldMarker = res.getDrawable(R.drawable.marker_hold);

        mResourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
        
        mMapView = (MapView) findViewById(R.id.mapview);
        mMapView.setTileSource(TileSourceFactory.MAPNIK);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);
        mMapController = mMapView.getController();
        mMapController.setZoom(17);
        GeoPoint centrePoint = new GeoPoint(55.659516, 12.591619);
        mMapController.setCenter(centrePoint);

        mOverlayItems = new ArrayList<OverlayItem>();
        //mOverlayItems.add(new OverlayItem("ITU", "SampleDescription", point2));
        mMyHoldsOverlay = new ItemizedIconOverlay<OverlayItem>(mOverlayItems, mOnItemGestureListener, mResourceProxy);
        mMyNewHoldOverlay = new NewHoldOverlay( mResourceProxy);
        mMapView.getOverlays().add(this.mMyNewHoldOverlay);
        mMapView.getOverlays().add(this.mMyHoldsOverlay);
        
        mMyHoldIndex = -1;
        
     }

    private void updateMap()
    {
    	HashSet<Hold> holdsInRange = mCrowdNet.getHoldsInRange();
		Toast.makeText( CrowdOSMapActivity.this, "update map " + holdsInRange.size(), Toast.LENGTH_LONG).show();
		MediaPlayer player = MediaPlayer.create(this,
			    Settings.System.DEFAULT_NOTIFICATION_URI);
			player.start();

		mOverlayItems.clear();
    	for ( Hold hold : holdsInRange )
    	{
    		GeoPoint point = hold.getGeoPoint();
        	OverlayItem newHoldOverlayItem = new OverlayItem("My hold", "SampleDescription", point);
        	newHoldOverlayItem.setMarker(mHoldMarker);
    	}
		mMapView.getOverlays().remove(mMyHoldsOverlay);
		mMyHoldsOverlay = new ItemizedIconOverlay<OverlayItem>(mOverlayItems, mOnItemGestureListener, mResourceProxy);
	    mMapView.getOverlays().add(this.mMyHoldsOverlay);
	    mMapView.invalidate();
    }
    
    private void setMyHold( GeoPoint point )
    {
    	String msg;
    	if ( mMyHoldIndex >= 0 )
    	{
    		mOverlayItems.remove( mMyHoldIndex );
    		msg = "";
    	}
    	Drawable foo;
    	OverlayItem newHoldOverlayItem = new OverlayItem("My hold", "SampleDescription", point);
    	newHoldOverlayItem.setMarker(mMyHoldMarker);
    	mOverlayItems.add( newHoldOverlayItem );
    	mMyHoldIndex = mOverlayItems.indexOf( newHoldOverlayItem );
		Toast.makeText( CrowdOSMapActivity.this, "add hld " + point + " index " + mMyHoldIndex, Toast.LENGTH_LONG).show();
		mMapView.getOverlays().remove(mMyHoldsOverlay);
		mMyHoldsOverlay = new ItemizedIconOverlay<OverlayItem>(mOverlayItems, mOnItemGestureListener, mResourceProxy);
	    mMapView.getOverlays().add(this.mMyHoldsOverlay);
	    mMapView.invalidate();
	    mCrowdNet.setStateHold( point );
    }
    
    @Override
    public void onPause() 
    {
    	super.onPause();
    	unregisterReceiver( mCrowdNetStatusReceiver );
    }
    
    @Override
    public void onResume() 
    {
    	super.onResume();
        IntentFilter crowdNetUpdateFilter;
        crowdNetUpdateFilter = new IntentFilter( CrowdNet.CROWDNET_UPDATE );
        registerReceiver(mCrowdNetStatusReceiver , crowdNetUpdateFilter);
    	//updateMap();
    }
    
    @Override
    public void onDestroy() 
    {
    	super.onDestroy();
    }

    /*
    public void report( String msg )
    {
    	mOutput.setText( msg );
    }
    */
    
    class CrowdNetMarker extends Drawable
    {

		@Override
		public void draw(Canvas canvas) 
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public int getOpacity() 
		{
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void setAlpha(int alpha) 
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setColorFilter(ColorFilter cf) 
		{
			// TODO Auto-generated method stub
			
		}
    	
    }
    
    /*
     * Only for catching the longPress event.
     */
    class NewHoldOverlay extends Overlay
    {    	
		public NewHoldOverlay( ResourceProxy pResourceProxy ) 
		{
			super(pResourceProxy);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void draw(Canvas arg0, MapView arg1, boolean arg2) 
		{
			// TODO Auto-generated method stub
			
		}
		@Override
		public boolean onLongPress(final MotionEvent event, final MapView mapView) 
		{
			// Create a new marker at the location
			final Projection pj = mapView.getProjection();
			IGeoPoint igp = pj.fromPixels(event.getX(), event.getY());
			//Toast.makeText( CrowdOSMapActivity.this, "Hello " + igp.getLatitudeE6() + " " + igp.getLongitudeE6(), Toast.LENGTH_LONG).show();
			GeoPoint point = new GeoPoint( igp.getLatitudeE6()/1E6, igp.getLongitudeE6()/1E6);
			DemoPopupWindow dw = new DemoPopupWindow( mapView );
			//dw.showLikePopDownMenu( /*Math.round( event.getX() ), Math.round( event.getY() ) + 30 */ );
			dw.showLikeQuickAction( 0, 30 );
			setMyHold( point );

			switch ( mCrowdNet.getState() )
			{
				case CrowdNet.CROWDNET_STATE_NONE:
					break;
					
				case CrowdNet.CROWDNET_STATE_SYNC:
					//setMySync( point );
					break;
					
				case CrowdNet.CROWDNET_STATE_HOLD:
					setMyHold( point );
					break;
					
				default:
					break;
			}
            return true;
 		}
    	
    }
    
    /*
    class TKItemizedIconOverlay<Item extends OverlayItem> extends ItemizedIconOverlay<Item> 
    {

		public TKItemizedIconOverlay(
				Context pContext,
				List<Item> pList,
				org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener<Item> pOnItemGestureListener) {
			super(pContext, pList, pOnItemGestureListener);
			// TODO Auto-generated constructor stub
		}
    	
 		@Override
		public boolean onLongPress(final MotionEvent event, final MapView mapView) 
		{

            if ( !super.onLongPress(event, mapView) )
            {
            	Toast.makeText( CrowdOSMapActivity.this, "Hello", Toast.LENGTH_LONG).show();
            }
            return true;
 		}
    }
    */
    
	/**
	 * Extends {@link BetterPopupWindow}
	 * <p>
	 * Overrides onCreate to create the view and register the button listeners
	 * 
	 * @author qbert
	 * 
	 */
	private static class DemoPopupWindow extends BetterPopupWindow implements OnClickListener 
	{
		public DemoPopupWindow(View anchor) 
		{
			super(anchor);
		}

		@Override
		protected void onCreate() 
		{
			// inflate layout
			LayoutInflater inflater =
					(LayoutInflater) this.anchor.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			ViewGroup root = (ViewGroup) inflater.inflate(R.layout.popup_grid_layout, null);

			// setup button events
			for(int i = 0, icount = root.getChildCount() ; i < icount ; i++) 
			{
				View v = root.getChildAt(i);

				if(v instanceof TableRow) {
					TableRow row = (TableRow) v;

					for(int j = 0, jcount = row.getChildCount() ; j < jcount ; j++) 
					{
						View item = row.getChildAt(j);
						if(item instanceof Button) {
							Button b = (Button) item;
							b.setOnClickListener(this);
						}
					}
				}
			}

			// set the inflated view as what we want to display
			this.setContentView(root);
		}

		@Override
		public void onClick(View v) {
			// we'll just display a simple toast on a button click
			Button b = (Button) v;
			Toast.makeText(this.anchor.getContext(), b.getText(), Toast.LENGTH_SHORT).show();
			this.dismiss();
		}
	}

}