package unet.fengine.headers;

import java.io.IOException;
import java.io.InputStream;

public class ResponseHeaders extends Headers {

    private byte[] p;
    private StatusCode c;

    public String getNegotiatedProtocol(){
        return new String(p);
    }

    public StatusCode getStatusCode(){
        return c;
    }

    public void parse(InputStream in)throws IOException {
        /**
         * HTTP/1.1  |  200  |  OK
        **/

        byte[] buf = new byte[1024];
        int b, i = 0;
        short n = 0;

        while((b = in.read()) > 0){
            buf[i] = (byte) b;
            if(n < 2 && b == 0x20){
                byte[] z = new byte[i];
                System.arraycopy(buf, 0, z, 0, z.length);

                switch(n){
                    case 0:
                        p = z;
                        break;

                    case 1:
                        int y = 0;
                        for(i = 0; i < z.length; i++){
                            y = 10*y + (z[i]-'0');
                        }
                        //System.out.println(y);
                        c = StatusCode.getByValue(y);
                        //response.statusCode = APIResponse.StatusCode.getByValue(Integer.parseInt(new String(z)));
                        break;
                }

                i = 0;
                n++;
                continue;
            }

            if(b == 0x0D){
                if((byte) in.read() == 0x0A){
                    break;
                }else{
                    //THROW EXCEPTION - Illegal character after return
                }
            }
            i++;
        }

        /**
         * Host: 127.0.0.1
        **/

        i = 0;
        n = 0;
        boolean c = false;
        byte[] k = null;

        while((b = in.read()) > 0){
            buf[i] = (byte) b;

            switch(n){
                case 0:
                    if(b == 0x3A){
                        k = new byte[i];
                        System.arraycopy(buf, 0, k, 0, k.length);
                        i = 0;
                        n++;
                        c = true;
                    }
                    break;

                case 1:
                    if(b != 0x20 && b != '\t'){ //  SPACE / TAB CHECK
                        buf[0] = (byte) b;
                        i = 1;
                        n++;
                        c = true;
                    }
                    break;

                case 2:
                    if(b == 0x0D){
                        if((byte) in.read() == 0x0A){
                            byte[] v = new byte[i];
                            System.arraycopy(buf, 0, v, 0, v.length);
                            //Log.e("info", new String(k)+" "+new String(v));
                            add(new String(k), new String(v));
                            i = 0;
                            n = 0;
                            c = true;

                        }else{
                            //THROW EXCEPTION - Illegal character after return
                        }
                    }
                    break;
            }

            if(c){
                c = false;
            }else{
                if(buf[i] == 0x0D){
                    if((byte) in.read() == 0x0A){
                        break;
                    }else{
                        //THROW EXCEPTION - Illegal character after return
                    }
                }

                i++;
            }
        }
        //in.skip(4);
    }

    public enum StatusCode {
        //1xx: Informational
        CONTINUE(100, "Continue"),
        SWITCHING_PROTOCOLS(101, "Switching Protocols"),
        PROCESSING(102, "Processing"),
        EARLY_HINTS(103, "Early Hints"),

        //2xx: Success
        OK(200, "OK"),
        CREATED(201, "Created"),
        ACCEPTED(202, "Accepted"),
        NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),
        NO_CONTENT(204, "No Content"),
        RESET_CONTENT(205, "Reset Content"),
        PARTIAL_CONTENT(206, "Partial Content"),
        MULTI_STATUS(207, "Multi-Status"),
        ALREADY_REPORTED(208, "Already Reported"),
        IM_USED(226, "IM Used"),

        //3xx: Redirection
        MULTIPLE_CHOICES(300, "Multiple Choice"),
        MOVED_PERMANENTLY(301, "Moved Permanently"),
        FOUND(302, "Found"),
        SEE_OTHER(303, "See Other"),
        NOT_MODIFIED(304, "Not Modified"),
        USE_PROXY(305, "Use Proxy"),
        TEMPORARY_REDIRECT(307, "Temporary Redirect"),
        PERMANENT_REDIRECT(308, "Permanent Redirect"),

        //4xx: Client Error
        BAD_REQUEST(400, "Bad Request"),
        UNAUTHORIZED(401, "Unauthorized"),
        PAYMENT_REQUIRED(402, "Payment Required"),
        FORBIDDEN(403, "Forbidden"),
        NOT_FOUND(404, "Not Found"),
        METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
        NOT_ACCEPTABLE(406, "Not Acceptable"),
        PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
        REQUEST_TIMEOUT(408, "Request Timeout"),
        CONFLICT(409, "Conflict"),
        GONE(410, "Gone"),
        LENGTH_REQUIRED(411, "Length Required"),
        PRECONDITION_FAILED(412, "Precondition Failed"),
        REQUEST_TOO_LONG(413, "Payload Too Large"),
        REQUEST_URI_TOO_LONG(414, "URI Too Long"),
        UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
        REQUESTED_RANGE_NOT_SATISFIABLE(416, "Range Not Satisfiable"),
        EXPECTATION_FAILED(417, "Expectation Failed"),
        MISDIRECTED_REQUEST(421, "Misdirected Request"),
        UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),
        LOCKED(423, "Locked"),
        FAILED_DEPENDENCY(424, "Failed Dependency"),
        TOO_EARLY(425, "Too Early"),
        UPGRADE_REQUIRED(426, "Upgrade Required"),
        PRECONDITION_REQUIRED(428, "Precondition Required"),
        TOO_MANY_REQUESTS(429, "Too Many Requests"),
        REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"),
        UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons"),

        //5xx: Server Error
        INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
        NOT_IMPLEMENTED(501, "Not Implemented"),
        BAD_GATEWAY(502, "Bad Gateway"),
        SERVICE_UNAVAILABLE(503, "Service Unavailable"),
        GATEWAY_TIMEOUT(504, "Gateway Timeout"),
        HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),
        VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiates"),
        INSUFFICIENT_STORAGE(507, "Insufficient Storage"),
        LOOP_DETECTED(508, "Loop Detected"),
        NOT_EXTENDED(510, "Not Extended"),
        NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required");

        private final int value;
        private final String description;

        StatusCode(int value, String description){
            this.value = value;
            this.description = description;
        }

        public int getValue(){
            return value;
        }

        public String getDescription(){
            return description;
        }

        @Override
        public String toString(){
            return value+" "+description;
        }

        public static StatusCode getByValue(int value){
            for(StatusCode status : values()){
                if(status.value == value){
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid status code: " + value);
        }
    }
}
