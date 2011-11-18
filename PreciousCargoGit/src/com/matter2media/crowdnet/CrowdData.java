package com.matter2media.crowdnet;

/**
 * @author Tim Kindberg <tim@matter2media.com>
 *
 * @since Nov 16, 2011
 * 
 * An item of data sent over the crowdnet
 * 
 * Data is an object with a MIME type
 * Data is sent on a 'topic' -- a stream identifier for managing publish/subscribe
 * 
 * 
 */
public class CrowdData
{
	/* topic used to discriminate between streams of data */
	private Topic 	mTopic;
	/* Unique, agestamped id */
	private Id 		mId;
	/* MIME type of data */
	private String	mMimeType;
	/* the data */
	private Object	mData;
	/* if 'copiable', a synchronisation between holds will leave two copies. if false, synchronisation will move the data to the other hold */
	private boolean	mCopiable;
	
	public CrowdData(String topic, boolean makesEarlierDataObsolete, String origin, long sequence, String mimeType, Object data, boolean copiable )
	{
		mTopic = new Topic( topic, makesEarlierDataObsolete );
		mId = new Id( origin, sequence );
		mMimeType = mimeType;
		mData = data;
		mCopiable = copiable;
	}
	
	public CrowdData(String topic, String origin, long sequence, String mimeType, Object data )
	{
		this( topic, false, origin, sequence, mimeType, data, true );
	}
	
	public boolean isCopiable()
	{
		return mCopiable;
	}
	
	public boolean makesEarlierDataObsolete()
	{
		return mTopic.mEarlierDataObsolete;
	}
	
	public String getTopic()
	{
		return mTopic.mName;
	}
	
	public Id getId()
	{
		return mId;
	}
	
	public String getOrigin()
	{
		return mId.mOrigin;
	}
	
	public long getSequence()
	{
		return mId.mSequence;
	}
	
	public long getAgeInSeconds()
	{
		return mId.getAgeInSeconds();
	}
	
	public long updateAge()
	{
		return mId.getAgeInSeconds();
	}
	
	public void setToLocalAgeReference()
	{
		mId.setToLocalClock();
	}
	
	/**
	 * @return
	 */
	public Object getData()
	{
		return mData;
	}

		public class Topic 
		{
			/* topic names need to be globally unique, e.g. com.matter2media.crowdnet.crowdtwitter.tweets */
			public String 	mName;
			/* if 'mEarlierDataObsolete', delete older data on this topic already in the hold -- don't use different values for same topic name! */
			public boolean	mEarlierDataObsolete;
			
			public Topic( String name, boolean earlierDataObsolete )
			{
				mName = name;
				mEarlierDataObsolete = earlierDataObsolete;
			}
		}
	
		public class Id extends AgeStamp implements Comparable<Id>
		{
			public String 	mOrigin;
			public long		mSequence;
			
			public Id( String origin, long sequence )
			{
				mOrigin = origin;
				mSequence = sequence;
				this.setAge(0);
			}

		public boolean equals( Object o )
		{
			if ( o instanceof Id )
			{
				Id i = (Id)o;
				return mOrigin.equals( i.mOrigin ) && mSequence == i.mSequence;
			}
			else
				return false;
		}
		
		/*
		 * Order *most recent first* as follows
		 *  * Items from same origin according to sequence number
		 *  * Items from different origins according to age in seconds
		 *  
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(Id i) 
		{
			if ( i.equals( this ) ) return 0;
			else
				if ( mOrigin.equals( i.mOrigin ) )
				{
					return ( mSequence < i.mSequence ? 1 : -1 );
				}
				else
					if ( mAgeInSeconds < i.mAgeInSeconds ) return 1;
					else
						if ( mAgeInSeconds == i.mAgeInSeconds ) 
							return 0;
						else
							return -1;
		}
	}

}
