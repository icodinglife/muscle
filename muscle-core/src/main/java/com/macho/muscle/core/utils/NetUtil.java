package com.macho.muscle.core.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetUtil {

    public static String selfIpAddr() throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();
        return addr.getHostAddress();
    }
}
