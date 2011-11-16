package com.matter2media.crowdz.preciouscargo;

import java.util.Date;

public class AgedData 
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
	public void setToLocalAgeReference(  )
	{
		mWhenAgeCalculated = new Date().getTime()/1000;
	}
	
	public void setAge( long age )
	{
		mAgeInSeconds = age;
		mWhenAgeCalculated = new Date().getTime()/1000;
	}
}
