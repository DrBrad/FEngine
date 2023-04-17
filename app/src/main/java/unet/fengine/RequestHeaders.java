package unet.fengine.headers;

public class RequestHeaders extends Headers {

    private Method method = Method.GET;
    private byte[] path = {
            '/'
    };
    private byte[] negotiationProtocol = {
            'H',
            'T',
            'T',
            'P',
            '/',
            '1',
            '.',
            '1'
        };

    public void setMethod(Method method){
        this.method = method;
    }

    public void setPath(String path){
        this.path = path.getBytes();
    }

    public void setNegotiationProtocol(String negotiationProtocol){
        this.negotiationProtocol = negotiationProtocol.getBytes();
    }

    @Override
    public byte[] getBytes(){
        byte[] h = super.getBytes();

        byte[] b = new byte[h.length+method.name().getBytes().length+path.length+negotiationProtocol.length+4];
        int p = method.name().getBytes().length;
        System.arraycopy(method.name().getBytes(), 0, b, 0, p);
        b[p] = 0x20;
        System.arraycopy(path, 0, b, p+1, path.length);
        p += path.length;
        b[p+1] = 0x20;
        System.arraycopy(negotiationProtocol, 0, b, p+2, negotiationProtocol.length);
        p += negotiationProtocol.length+2;
        b[p] = '\r';
        b[p+1] = '\n';

        System.arraycopy(h, 0, b, p+2, h.length);

        return b;
    }

    public enum Method {
        GET,
        HEAD,
        DELETE,
        POST,
        PUT
    }
}
