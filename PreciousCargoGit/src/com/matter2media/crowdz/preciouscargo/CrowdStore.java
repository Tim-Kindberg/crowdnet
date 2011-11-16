package com.matter2media.crowdz.preciouscargo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CrowdStore
{
	private HashMap<String,List<CrowdData>>	mTheData;
	
	public CrowdStore()
	{
		mTheData = new HashMap<String,List<CrowdData>>();
	}
	
	public void addData( CrowdData data )
	{
		List<CrowdData> list = mTheData.get( data.getOrigin() );
		if ( list == null )
		{
			list = new ArrayList<CrowdData>();
			mTheData.put( data.getOrigin(), list );
		}
		list.add( data );
	}
	
    public CrowdData[] getObjectsSince()
    {
    	return new CrowdData[1];
    }
    
	public HashMap<String,Long> getSequences()
	{
		HashMap<String,Long> seqMap = new HashMap<String,Long>();
		for (List<CrowdData> crowdDataList : mTheData.values()) 
		{
			CrowdData data = crowdDataList.get( crowdDataList.size() - 1 );
			seqMap.put( data.getOrigin(), new Long( data.getSequence() ) );
		}
		return seqMap;
	}
	
	public HashMap<String,Long> diffSequences( HashMap<String,Long> otherSequences )
	{
		HashMap<String,Long> mySequences = getSequences();
		for ( String origin : otherSequences.keySet() ) 
		{
			Long mySequence = mySequences.get( origin );
			if ( mySequence == null )
			{
				mySequences.put( origin, otherSequences.get( origin ) );
			}
			else
			{
				mySequences.put( origin, new Long( otherSequences.get( origin ).longValue() - mySequence.longValue() ));
			}
		}
		return mySequences;
	}
}
