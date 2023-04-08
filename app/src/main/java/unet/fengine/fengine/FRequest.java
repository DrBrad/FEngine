package unet.fengine.fengine;

import unet.fengine.headers.RequestHeaders;

public class FRequest {

    protected RequestHeaders headers;
    protected String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36";

    public FRequest(){
        headers = new RequestHeaders();
        headers.add("User-Agent", userAgent);
    }

    public void setUserAgent(String userAgent){
        this.userAgent = userAgent;
        headers.add("User-Agent", userAgent);
    }

    public String getUserAgent(){
        return userAgent;
    }

    public void setMethod(RequestHeaders.Method method){
        headers.setMethod(method);

        if(method.equals(RequestHeaders.Method.POST)){
            if(!headers.containsKey("Content-Type")){
                headers.add("Content-Type", "application/x-www-form-urlencoded");
            }
        }
    }

    public void setNegotiationProtocol(String negotiationProtocol){
        headers.setNegotiationProtocol(negotiationProtocol);
    }

    public void addHeader(String key, String value){
        headers.add(key, value);
    }
}
