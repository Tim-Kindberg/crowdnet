package com.matter2media.crowdnet;

import java.net.Socket;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpClientConnection;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.util.EntityUtils;

import android.util.Log;

/**
 * @author Tim Kindberg <tim@matter2media.com>
 * 
 * @since Nov 16, 2011
 *
 * Client of a crowdserver (hold) at another node
 * 
 */
public class CrowdServerClient 
{
	private HttpParams 			mParams;
	private BasicHttpProcessor 	mHttpproc;
    private HttpHost 			mHost;
	
	public CrowdServerClient()
	{
        mParams = new BasicHttpParams();
        HttpProtocolParams.setVersion(mParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(mParams, "UTF-8");
        HttpProtocolParams.setUserAgent(mParams, "HttpComponents/1.1");
        HttpProtocolParams.setUseExpectContinue(mParams, true);		

        mHttpproc = new BasicHttpProcessor();
        // Required protocol interceptors
        mHttpproc.addInterceptor(new RequestContent());
        mHttpproc.addInterceptor(new RequestTargetHost());
        // Recommended protocol interceptors
        mHttpproc.addInterceptor(new RequestConnControl());
        mHttpproc.addInterceptor(new RequestUserAgent());
        mHttpproc.addInterceptor(new RequestExpectContinue());
        
        mHost = new HttpHost("192.168.43.1", 8080);
	}
	
	public String getStoreContents()
	{
        HttpRequestExecutor httpexecutor = new HttpRequestExecutor();

        HttpContext context = new BasicHttpContext(null);

        DefaultHttpClientConnection conn = new DefaultHttpClientConnection();

        context.setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
        context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, mHost);

        String responseString = null;
        
        try 
        {
            Socket socket = new Socket(mHost.getHostName(), mHost.getPort());
            conn.bind(socket, mParams);

            BasicHttpRequest request = new BasicHttpRequest("GET", "/pull");
            request.setParams(mParams);
            httpexecutor.preProcess(request, mHttpproc, context);
            HttpResponse response = httpexecutor.execute(request, conn, context);
            response.setParams(mParams);
            httpexecutor.postProcess(response, mHttpproc, context);

            responseString = "Response: " + response.getStatusLine() + " value " + EntityUtils.toString(response.getEntity());
        } 
        catch ( Exception ex )
        {
        	Log.v("CrowdNet", "Problem in CrowdServerClient " + ex );
        }
        finally 
        {
            try { conn.close(); } catch ( Exception foo ) {}
        }
        
        return responseString;
	}
}

