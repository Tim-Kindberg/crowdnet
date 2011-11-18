package com.matter2media.crowdnet;

import java.util.Date;
 
/**
 * @author Tim Kindberg <tim@matter2media.com>
 *
 * @since Nov 18, 2011
 *  
 * Record how long ago something happened. Needs to be refreshed before transferring to another node.
 * Rationale for use instead of timestamp: no synchronised clocks!
 * Protocol for use:
 * 	Initialise when create an agestamped object
 *  Just before transfer to another node, call makeAgeCurrent()
 *  Upon arrival at another node, call setToLocalClock()
 * Obviously, if the transfer took, say, ten seconds, then the age at the new node will be incorrect by that amount
 * 
 * TODO: enable adjustment where the transfer time has been estimated
 * 
 */
public class AgeStamp 
{
	long	mAgeInSeconds;
	long	mWhenAgeCalculated;
	
	public long getAgeInSeconds()
	{
		return makeAgeCurrent( );
	}
	
	public long makeAgeCurrent( )
	{
		long nowInSeconds = new Date().getTime()/1000;
		mAgeInSeconds += ( nowInSeconds - mWhenAgeCalculated );
		mWhenAgeCalculated = nowInSeconds;
		return mWhenAgeCalculated;
	}
	
	/*
	 * On transfer to a new machine (with its own clock)
	 * Assumes someone called makeAgeCurrent() at the other end -- and transfer time negligible.
	 */
	public void setToLocalClock(  )
	{
		mWhenAgeCalculated = new Date().getTime()/1000;
	}
	
	public void setAge( long age )
	{
		mAgeInSeconds = age;
		mWhenAgeCalculated = new Date().getTime()/1000;
	}
}
