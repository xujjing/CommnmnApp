package com.streambus.commonmodule.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 用来做网络测试，实现ping的效果，需要在线程里面进行，不然会阻塞主线程
 * 
 * @author jcy
 */

public class TraceRoute {

	private static TraceRoute mTraceRoute;

	private TraceRoute() {

	}

	public static TraceRoute instance() {
		if (mTraceRoute == null) {
			synchronized (TraceRoute.class) {
				if (mTraceRoute == null) {
					mTraceRoute = new TraceRoute();
				}
			}
		}
		return mTraceRoute;
	}

	/**
	 * 耗时操作需要在子线程里面执行该方法
	 * @param ip 需要ping的ip地址
	 * @return ping指定IP的输出结果*/
	public synchronized String pingIP(String ip) {
		String res = "";
		try {
			Process p;
			// 一个地址ping四次
			String command = "ping -c 4 ";
			// 实际调用命令时 后面要跟上url地址
			p = Runtime.getRuntime().exec(command + ip);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String s;
			while ((s = stdInput.readLine()) != null) {
				res += s + "\n";
			}
			// 调用结束的时候 销毁这个资源
			p.destroy();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

}
