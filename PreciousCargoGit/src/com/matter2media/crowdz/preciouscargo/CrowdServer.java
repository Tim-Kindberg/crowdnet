package com.matter2media.crowdz.preciouscargo;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import android.content.Context;

public class CrowdServer  implements Runnable
{
	public final String 		HOME_PATTERN = "*";
	
	private CrowdNet			mCrowdNet;
	private CrowdStore			mCrowdStore;
	private ServerSocket		mServerSocket;
	private int 				mServerPort = 8080;
	private volatile boolean	mRunning;
	private Thread				mMyThread;
	private Context				mContext;
	private BasicHttpProcessor	mHttpProc;
	private BasicHttpContext	mHttpContext;
	private HttpService			mHttpService;
	private HttpRequestHandlerRegistry	mHttpRegistry;
	
	public CrowdServer( CrowdNet crowdNet, int serverPort )
	{
		mCrowdNet = crowdNet;
		mCrowdStore = new CrowdStore();
       	mRunning = false;
       	mContext = mCrowdNet.getApplicationContext();
        try
        {
    		mHttpProc = new BasicHttpProcessor();
    		mHttpContext = new BasicHttpContext();

    		mHttpProc.addInterceptor(new ResponseDate());
    		mHttpProc.addInterceptor(new ResponseServer());
    		mHttpProc.addInterceptor(new ResponseContent());
    		mHttpProc.addInterceptor(new ResponseConnControl());

    		mHttpService = new HttpService(mHttpProc,
    		    new DefaultConnectionReuseStrategy(), new DefaultHttpResponseFactory());

    		mHttpRegistry = new HttpRequestHandlerRegistry();

    		mHttpService.setHandlerResolver( mHttpRegistry );
    		
    		mHttpRegistry.register(HOME_PATTERN, new CrowdCommandHandler( mContext ));

    		mServerPort = serverPort;
        	mServerSocket = new ServerSocket( mServerPort, 50, null );
        	mServerSocket.setReuseAddress( true );
         }
        catch ( Exception ex )
        {
        	
        }
	}
	
	public void startServer()
	{
		mMyThread = new Thread( this );
       	mRunning = true;
       	mMyThread.start();
	}
	
	public void stopServer()
	{
       	mRunning = false;
	}
	
	public void addData( CrowdData data )
	{
		mCrowdStore.addData(data);
	}

    public CrowdData[] getObjectsSince()
    {
    	return mCrowdStore.getObjectsSince();
    }
    
	@Override
	public void run() 
	{
		while ( mRunning )
		{
			DefaultHttpServerConnection serverConnection = null;
	    	//mCrowdNet.report("Accepting...");
			try
			{
				Socket socket = mServerSocket.accept();
				
		    	//mCrowdNet.report("Accepted...");

				// TO_DO: put remainder in worker thread
				serverConnection = new DefaultHttpServerConnection();

				serverConnection.bind(socket, new BasicHttpParams());

				mHttpService.handleRequest(serverConnection, mHttpContext);
			}
			catch ( Exception e )
			{
		    	mCrowdNet.report("problem handling http request: " + e );
			}
			finally
			{
				if ( serverConnection != null ) try { serverConnection.shutdown(); } catch ( Exception allyUninteresting ) {}
			}
		}
	}

	public void shutdown() 
	{
       	mRunning = false;
       	mMyThread = null;
        if ( mServerSocket != null ) try { mServerSocket.close(); } catch ( Exception e ) {}
	}
	
	class CrowdCommandHandler implements HttpRequestHandler 
	{
		private Context context = null;

		public CrowdCommandHandler(Context context) 
		{
			this.context = context;
		}

		@Override
		public void handle(	HttpRequest request, 
							HttpResponse response,
							HttpContext httpContext) throws HttpException, IOException 
		{
            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
            String target = request.getRequestLine().getUri();
            String resource = URLDecoder.decode(target);
            
            if (method.equals("GET"))
            {
            	response.setStatusCode(HttpStatus.SC_OK);
            	if ( resource.equals("status") )
            	{
                   	StringEntity body = new StringEntity("You asked to GET " + resource);
                	response.setEntity(body);            		
            	}
            	else if ( resource.startsWith("pull/") )
            	{
            		
            	}
            	else
            	{
                	response.setStatusCode(HttpStatus.SC_NOT_FOUND);
            	}
            	
             }
            else if ( method.equals("POST") ) 
            {
            	if ( resource.startsWith("push/") )
            	{
            		response.setStatusCode(HttpStatus.SC_OK);
            	}
            	else
            	{
                	response.setStatusCode(HttpStatus.SC_NOT_FOUND);
            	}
            	
            	
            	StringEntity body = new StringEntity("You asked to POST " + resource );
            	response.setEntity(body);
            }
            else
            {
                throw new MethodNotSupportedException(method + " method not supported"); 
            }
		}

		public Context getContext() 
		{
			return context;
		}
	}
}


