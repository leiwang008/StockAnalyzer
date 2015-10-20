package com.lei.natives;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;

public interface WebProxyDetector extends StdCallLibrary{
	WebProxyDetector INSTANCE = (WebProxyDetector) Native.loadLibrary("WebProxyDetectorDll", WebProxyDetector.class);
	
	String getProxyServerName();
	int getProxyServerPort();
}
