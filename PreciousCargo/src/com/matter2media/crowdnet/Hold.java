package com.matter2media.crowdnet;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.GeoPoint;

import com.matter2media.crowdnet.HoldName.NotAHoldNameException;

/**
 * @author Tim Kindberg <tim@matter2media.com>
 * 
 * @since Nov 16, 2011
 *
 * Record of a hold (which may or may not be current)
 * 
 */
public class Hold extends AgeStamp
{
	public String 		mBSSID;
	private HoldName 	mHoldName;
	private boolean		mInScanRange;
	List<Hold>			mSyncs;
	
	public Hold( String bssid, String holdName ) throws NotAHoldNameException
	{
		setAge(0);
		mHoldName = new HoldName( holdName );
		mBSSID = bssid;
	}
	
	public GeoPoint getGeoPoint()
	{
		return new GeoPoint( mHoldName.getLatE6(), mHoldName.getLongE6() );
	}
	
	public void setInRange( String holdName ) throws NotAHoldNameException
	{
		mHoldName = new HoldName( holdName );
		setInRange();
	}
	
	public boolean isInRange( )
	{
		return mInScanRange;
	}
	
	public void setInRange( )
	{
		setAge(0);
		mInScanRange = true;
	}
	
	public void setNotInRange()
	{
		mInScanRange = false;
	}
	
	public void setSyncRecord( Hold syncedWith )
	{
		if ( mSyncs == null )
		{
			mSyncs = new ArrayList<Hold>();
			mSyncs.add( syncedWith );
		}
		else
		{
			int where = mSyncs.indexOf( syncedWith );
			if ( where < 0 )
			{
				mSyncs.add( syncedWith );
			}
			else
			{
				Hold already = mSyncs.get( where );
				already.updateWith( syncedWith );
			}
		}
	}
	
	public boolean equals( Object other )
	{
		return ((Hold)other).mBSSID == mBSSID;
	}
	
	public void updateWith( Hold other )
	{
		long othersAge = other.getAgeInSeconds();
		if ( getAgeInSeconds() > othersAge )
		{
			setHoldName( other.getHoldName() );
			setAge( othersAge );
		}
	}
	
	public HoldName getHoldName()
	{
		return mHoldName;
	}
	
	public void setHoldName( HoldName holdName )
	{
		mHoldName = holdName;
	}
}
