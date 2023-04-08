package unet.fengine.fengine;

import java.io.InputStream;
import java.io.OutputStream;

public class FSocketCallback {

    public void onRequest(FRequest request, OutputStream out)throws Exception {
    }

    public void onResponse(FRequest request, FResponse response, InputStream in)throws Exception {
    }

    public boolean onRedirect(FRequest request, FResponse response, String newLocation){
        return true;
    }

    public void onException(Exception e){
    }
}
