package com.matter2media.crowdz.preciouscargo;

public class CrowdConfig 
{
	public	String	mCrowdName;
	public	String	mCrowdPassword;
	
	public void setCrowdNameAndPassword( String crowdName, String crowdPassword )
	{
		mCrowdName = crowdName;
		mCrowdPassword = crowdPassword;
	}
	
	public String getCrowdName()
	{
		return mCrowdName;
	}
	
	public String getCrowdPassword()
	{
		return mCrowdPassword;
	}
}
