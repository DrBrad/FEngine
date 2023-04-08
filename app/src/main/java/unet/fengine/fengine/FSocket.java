package unet.fengine.fengine;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.Socket;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import unet.fengine.cookie.CookieStore;

public class FSocket {

    private Socket socket;
    private InputStream in;
    private OutputStream out;

    private String url;
    private FRequest request;
    private FResponse response;

    private FSocketCallback callback;

    private CookieManager cookieManager;

    private int maxRedirects = 3, redirectCount = 0;

    public FSocket(Context context, String url, FSocketCallback callback){
        this.url = url;
        this.callback = callback;
        request = new FRequest();

        CookieStore cookieStore = new CookieStore(context);
        cookieManager = new CookieManager(cookieStore, CookiePolicy.ACCEPT_ALL);
        //CookieHandler.setDefault(cookieManager);
    }

    public void connect(){
        try{
            URI url = new URI(this.url);
            request.headers.setPath(url.getPath()+"?"+url.getQuery());
            request.headers.add("Host", url.getHost());

            if(url.getScheme().equals("https")){
                SocketFactory socketFactory = SSLSocketFactory.getDefault();
                socket = socketFactory.createSocket(url.getHost(), (url.getPort() > 0) ? url.getPort() : 443);
                request.headers.add("Connection", "close");

            }else{
                socket = new Socket(url.getHost(), (url.getPort() > 0) ? url.getPort() : 80);
            }

            in = socket.getInputStream();
            out = socket.getOutputStream();

            List<HttpCookie> cookies = cookieManager.getCookieStore().get(url);
            if(cookies.size() > 0){
                String builder = "";
                for(HttpCookie cookie : cookies){
                    builder += cookie.getName()+"="+cookie.getValue()+"; ";
                }
                request.headers.add("Cookie", builder);
            }

            out.write(request.headers.getBytes());

            callback.onRequest(request, out);
            if(socket.isClosed()){
                return;
            }

            response = new FResponse();
            response.headers.parse(in);

            if(response.headers.containsKey("Set-Cookie")){
                cookies = HttpCookie.parse(response.headers.get("Set-Cookie"));
                if(cookies != null){
                    for(HttpCookie cookie : cookies){
                        cookie.setDomain(url.getHost());
                        cookieManager.getCookieStore().add(url, cookie);
                    }
                }
            }

            if(response.getStatusCode() > 299 && response.getStatusCode() < 400 && maxRedirects > redirectCount){
                redirectCount++;

                if(response.headers.containsKey("Location")){
                    String location = response.headers.get("Location");

                    if(!location.startsWith("http")){
                        location = url.getScheme()+"://"+url.getHost()+((location.charAt(0) == '/') ? "" : "/")+location;
                    }

                    if(callback.onRedirect(request, response, location)){
                        this.url = location;
                        close();
                        connect();
                        return;
                    }
                }else{
                    throw new IOException("Server gave status code: \""+response.getStatusCode()+"\" but gave no \"Location header\".");
                }
            }

            if(socket.isClosed()){
                return;
            }

            //Log.e("info", response.headers.toString());
            /*
            * Transfer-Encoding: chunked
            * Transfer-Encoding: gzip
            * Transfer-Encoding: deflate
            * Transfer-Encoding: compress
            * */

            if(response.headers.containsKey("Transfer-Encoding")){
                if(response.headers.get("Transfer-Encoding").contains("chunked")){
                    callback.onResponse(request, response, new ChunkedInputStream(in));
                    close();
                    return;
                }
            }

            callback.onResponse(request, response, in);

        }catch(Exception e){
            e.printStackTrace();
            callback.onException(e);

        }finally{
            close();
        }
    }

    public void async(ExecutorService executor){
        executor.submit(new Runnable(){
            @Override
            public void run(){
                connect();
            }
        });
    }

    public void close(){
        try{
            if(!socket.isInputShutdown()){
                socket.shutdownInput();
            }

            if(!socket.isOutputShutdown()){
                socket.shutdownOutput();
            }

            socket.close();
        }catch(IOException e){
        }
    }

    public void setCookieManager(CookieManager cookieManager){
        this.cookieManager = cookieManager;
    }

    public FRequest getRequest(){
        return request;
    }

    public void setMaxRedirects(int maxRedirects){
        this.maxRedirects = maxRedirects;
    }

    public String getURL(){
        return url;
    }
}
