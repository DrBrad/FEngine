package unet.fengine.fengine;

import java.util.Map;

import unet.fengine.headers.ResponseHeaders;

public class FResponse {

    protected ResponseHeaders headers;

    public FResponse(){
        headers = new ResponseHeaders();
    }

    public int getStatusCode(){
        return headers.getStatusCode().getValue();
    }

    public String getStatusText(){
        return headers.getStatusCode().getDescription();
    }

    public String getHeader(String key){
        return headers.get(key);
    }

    public boolean containsHeader(String key){
        return headers.containsKey(key);
    }

    public Map<String, String> getAllHeaders(){
        return headers.getMap();
    }
}
