package com.matter2media.crowdnet;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * @author Tim Kindberg <tim@matter2media.com>
 * 
 * @since Nov 16, 2011
 * 
 * Store of CrowdData in a hold
 * 
 * TO_DO: use SQLLite? This is for prototype and can't scale very far!
 *
 */
public class CrowdStore
{
	/*
	 * All the crowd data: for each origin of data, a list of items produced by that origin (most recent last)
	 */
	private HashMap<String,List<CrowdData>>		mTheDataByOrigin;
	
	/*
	 * All the crowd data ordered most recent first
	 */
	private TreeMap<CrowdData.Id, CrowdData>	mTheDataByTime;
	
	public CrowdStore()
	{
		mTheDataByOrigin = new HashMap<String,List<CrowdData>>();
		mTheDataByTime = new TreeMap<CrowdData.Id, CrowdData>();
	}
	
	public boolean addData( CrowdData data )
	{
		CrowdData existing = mTheDataByTime.get( data.getId() );
		if ( existing == null )
		{
			// Update the age to be related to local clock
			// (assumes age is correct)
			data.setToLocalAgeReference();
			mTheDataByTime.put( data.getId(), data );
			List<CrowdData> listByOrigin = mTheDataByOrigin.get( data.getOrigin() );
			if ( listByOrigin == null )
			{
				listByOrigin = new ArrayList<CrowdData>();
				mTheDataByOrigin.put( data.getOrigin(), listByOrigin );
			}
			listByOrigin.add( data );
			return true;
		}
		return false;
	}
	
	public void removeData( CrowdData crowdData )
	{
		CrowdData existing = mTheDataByTime.get( crowdData.getId() );
		if ( existing != null )
		{
			mTheDataByTime.remove( crowdData.getId() );
			List<CrowdData> listByOrigin = mTheDataByOrigin.get( crowdData.getOrigin() );
			listByOrigin.remove( crowdData );
		}
	}
	
    public final List<CrowdData> getObjectsSince()
    {
    	List<CrowdData> list = new ArrayList<CrowdData>();
    	for ( CrowdData crowdData: mTheDataByTime.values() )
    	{
    		list.add( crowdData );
    	}
    	return list;
    }
	
    public String exportObjectsSince()
    {
    	Gson gson = new Gson();
    	// Ensure we send out up-to-date ages
    	for ( CrowdData crowdData: mTheDataByTime.values() )
    	{
    		crowdData.updateAge();
    	}
    	String json = gson.toJson( mTheDataByTime );
    	// Objects that are designated as uncopiable are being moved somewhere else and must disappear
    	clearExportedUncopiables();
    	return json;
    }
	
    public int importObjects( String json )
    {
    	Gson gson = new Gson();
    	Map<String,Boolean> topicMap = new HashMap<String, Boolean>();
    	// See GSON documentation: this is their recommended way of managing types!
    	Type collectionType = new TypeToken<TreeMap<CrowdData.Id, CrowdData>>(){}.getType();
    	TreeMap<CrowdData.Id, CrowdData> theData = gson.fromJson(json, collectionType);
    	int addedCount = 0;
    	Boolean fals = new Boolean(false);
    	for ( CrowdData crowdData: theData.values() )
    	{
    		if ( addData( crowdData ) )
    		{
    			++addedCount;
    			if ( crowdData.makesEarlierDataObsolete() ) topicMap.put( crowdData.getTopic(), fals  );
    		}
    	}
    	cleanStoreOfRedundancies( topicMap );
    	return addedCount;
    }
    
    /*
     * Objects that are designated as uncopiable are being moved somewhere else and must disappear
     */
    private void clearExportedUncopiables()
    {
    	for ( CrowdData crowdData: mTheDataByTime.values() )
    	{
    		if ( !crowdData.isCopiable() ) removeData( crowdData );
    	}
    }
    
    /*
     * For the given topics remove all entries after the first (latest)
     * The map is initialised to point from a given topic to false, as a flag to say we haven't encountered the topic yet
     * This gets set to true once we've found the first instance.
     * NB Iterator makes it OK for us to remove items
     */
    private void cleanStoreOfRedundancies( Map<String,Boolean> topicMap )
    {
		Iterator<CrowdData> i = mTheDataByTime.values().iterator();
		Boolean tru = new Boolean( true );
		while ( i.hasNext() )
    	{
			CrowdData crowdData = i.next();
    		String topic = crowdData.getTopic();
    		if ( topicMap.get( topic ).booleanValue() )
    		{
     		   i.remove();
    		}
    		else
    		{
    			topicMap.put(  topic, tru );
    		}
    	}
    }
    
	/*
	 * Maybe.....
	 * 
	public HashMap<String,Long> getSequences()
	{
		HashMap<String,Long> seqMap = new HashMap<String,Long>();
		for (List<CrowdData> crowdDataList : mTheDataByOrigin.values()) 
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
	 */
}
