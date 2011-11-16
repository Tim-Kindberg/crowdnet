package com.matter2media.crowdz.preciouscargo;

public class CrowdData extends AgedData
{
	private boolean	mCopiable;
	private String 	mOrigin;
	private long 	mSequence;
	private String	mMimeType;
	private byte[]	mData;
	
	public CrowdData(String origin, long sequence, String mimeType, byte[] data, boolean copiable )
	{
		mCopiable = copiable;
		mOrigin = origin;
		mSequence = sequence;
		mMimeType = mimeType;
		mData = data;
		this.setAge(0);
	}
	
	public CrowdData(String origin, long sequence, String mimeType, byte[] data )
	{
		this( origin, sequence, mimeType, data, true );
	}
	
	public String getOrigin()
	{
		return mOrigin;
	}
	
	public long getSequence()
	{
		return mSequence;
	}
}
