FEngine
========

This is an android library that lets you connect to web servers.

Supports
-----
This library supports the following methods
- [x] POST
- [x] GET
- [x] HEAD
- [x] DELETE
- [x] PUT

This library supports the following protocols
- [x] SSL | HTTPS
- [x] HTTP

This library supports the following transfer encoding methods
- [x] chunked
- [x] default
- [ ] gzip - (You can wrap the input stream onResponse)
- [ ] deflate - (You can wrap the input stream onResponse)
- [ ] compress - (You can wrap the input stream onResponse)
- [ ] brotli - (You can wrap the input stream onResponse)

[[https://github.com/DrBrad/FEngine/blob/main/fengine.png|alt=Diagram]]

Usage
-----
Here are some examples of how to use the FEngine library.

**Creating a GET call to the server**
```Java
Context context = getContext();
FSocket socket = new FSocket(context, "http://BLANK.com/PATH", new FSocketCallback(){
    @Override
    public void onResponse(FRequest request, FResponse response, InputStream in)throws Exception {
        if(response.getStatusCode() == 200){
            StringBuilder b = new StringBuilder();

            byte[] buf = new byte[4096];
            int l;
            while((l = in.read(buf)) > 0){
                b.append(new String(buf, 0, l));
            }
            Log.e("response", b.toString());
        }
    }

    @Override
    public void onException(Exception e){
        e.printStackTrace();
    }
});

socket.connect();
/*
IF YOU WISH TO HANDLE ASYNC USE:

int DEFAULT_THREAD_POOL_SIZE = 4;
ExecutorService executor = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
CastContext.getSharedInstance(getApplicationContext(), executor);
socket.async(executor);
*/
```

**Creating a POST call to the server**
```Java
byte[] post = "POST MESSAGE".getBytes();
Context context = getContext();
FSocket socket = new FSocket(context, "http://BLANK.com/PATH", new FSocketCallback(){
    @Override
    public void onRequest(FRequest request, OutputStream out)throws Exception {
        out.write(post);
        out.flush();
    }

    @Override
    public void onResponse(FRequest request, FResponse response, InputStream in)throws Exception {
        if(response.getStatusCode() == 200){
            StringBuilder b = new StringBuilder();

            byte[] buf = new byte[4096];
            int l;
            while((l = in.read(buf)) > 0){
                b.append(new String(buf, 0, l));
            }
            Log.e("response", b.toString());
        }
    }

    @Override
    public void onException(Exception e){
        e.printStackTrace();
    }
});

socket.getRequest().setMethod(RequestHeaders.Method.POST);
socket.getRequest().addHeader("Content-Length", post.length+"");
socket.connect();
```
