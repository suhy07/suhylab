package suhylab.suhy.lanchat;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class IPUtil {

    public static String GetLocalIP(Context context){//获取本机IPv4地址

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address))
                    {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        }
        catch (SocketException ex){
            ex.printStackTrace();
        }
        return null;
    }
    public static String GetStringFromInputStream(InputStream inputStream){
        String string="";
        String buffer="";
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
        try {
            while((buffer=bufferedReader.readLine())!=null) {
                string+=buffer;
                }

        } catch (IOException e) {
                e.printStackTrace();
        }
        return string;
    }
}
