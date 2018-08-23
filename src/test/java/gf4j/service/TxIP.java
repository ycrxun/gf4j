package gf4j.service;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class TxIP {

    @Test
    public void ip() throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();
        String ip = addr.getHostAddress();
        System.out.println(ip);
    }
}
