package com.matter2media.crowdnet;

import org.osmdroid.util.GeoPoint;

/**
 * @author Tim Kindberg <tim@matter2media.com>
 * 
 * @since Nov 16, 2011
 *
 * The data in a hold's SSID
 * 
 */
public class HoldName 
{
	public static final String DELIM = "Y"; // Be careful not to use punctuation - will break some phones !
	private String 		mName;
	private int			mLat;
	private int 		mLong;
	private long 		mState;
	private String 		mTag;
	
   public HoldName( String name ) throws NotAHoldNameException
   {
	   if ( name == null ) throw new NotAHoldNameException( "No name" );
	   String[] parts = name.split( DELIM, 4 );
	   if ( parts.length != 4 )  throw new NotAHoldNameException( "Semicolons = " + parts.length );
	   try
	   {
		   mLat = Integer.parseInt( parts[0] );
		   mLong = Integer.parseInt( parts[1] );
		   mState = Long.parseLong( parts[2], 16 );
		   mTag = parts[3];
	   }
	   catch ( Exception ex )
	   {
		   throw new NotAHoldNameException( "Wrong component type" );
	   }
	   mName = name;
   }
   	   
   public static String getDefaultHoldName()
   {
	   return "0" + DELIM + "0" + DELIM + "0" + DELIM;
   }
      
   public static String getHoldName( GeoPoint p, String state, String tag )
   {
	   return p.getLatitudeE6() + DELIM + p.getLongitudeE6() + DELIM + state  + DELIM + tag;
   }

   public int getLatE6()
   {
	   return mLat;
   }

   public int getLongE6()
   {
	   return mLong;
   }

   public long geState()
   {
	   return mState;
   }

   public String getTag()
   {
	   return mTag;
   }
   
   public String toString()
   {
	   return mName;
   }
   
   public class NotAHoldNameException extends Exception
   {
   	   public NotAHoldNameException( String msg )
   	   {
   		   super( msg );
   	   }
   }

}
