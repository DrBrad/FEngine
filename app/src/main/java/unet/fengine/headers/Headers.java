package unet.fengine.headers;

import java.util.HashMap;
import java.util.Map;

public class Headers {

    private Map<String, String> m = new HashMap<>();
    private int s;

    public Headers(){
    }

    public Headers(Map<String, String> m){
        this.m = m;
    }

    public void add(String k, String v){
        if(m.containsKey(k)){
            s -= m.get(k).getBytes().length;
            s += v.getBytes().length;

        }else{
            s += k.getBytes().length+v.getBytes().length+4;
        }
        m.put(k, v);
    }

    public String get(String k){
        return m.get(k);
    }

    public boolean containsKey(String k){
        return m.containsKey(k);
    }

    public void remove(String k){
        if(m.containsKey(k)){
            s -= k.getBytes().length+m.get(k).getBytes().length+4;
            m.remove(k);
        }
    }

    public Map<String, String> getMap(){
        return m;
    }

    public byte[] getBytes(){
        byte[] b = new byte[s+2];
        int p = 0;
        for(String k : m.keySet()){
            System.arraycopy(k.getBytes(), 0, b, p, k.getBytes().length);
            p += k.getBytes().length;
            b[p] = ':';
            b[p+1] = ' ';
            System.arraycopy(m.get(k).getBytes(), 0, b, p+2, m.get(k).getBytes().length);
            p += m.get(k).getBytes().length+4;
            b[p-2] = '\r';
            b[p-1] = '\n';
        }
        b[p] = '\r';
        b[p+1] = '\n';

        return b;
    }

    @Override
    public String toString(){
        return new String(getBytes());
    }
}
