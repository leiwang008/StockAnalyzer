package com.lei.natives;

public class TestProxyDetector {
	public static void main(String[] args){
		int serverport = WebProxyDetector.INSTANCE.getProxyServerPort();
		String serverhost = WebProxyDetector.INSTANCE.getProxyServerName();
		
		System.out.println("proxy server host is "+serverhost);
		System.out.println("proxy server port is "+serverport);
	}
}
