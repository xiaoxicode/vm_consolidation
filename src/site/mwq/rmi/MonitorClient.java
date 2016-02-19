package site.mwq.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
/**
 * Description: RMI客户端.
 * 
 * @author Peter Wei
 * @version 1.0 Feb 25, 2009
 */
public class MonitorClient {

	public RmiMonitorService monitorService;

	//public String ip = "localhost";
	public String ip = "192.168.1.17";

	public int port = 8889;

	public int interactive(int funindex, String param) {
		int result = 0;
		try {
			getMonitorService().interactive(funindex, param);
			result = 1;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return result;
	}

	public RmiMonitorService getMonitorService() {
		try {
			// 在RMI服务注册表中查找名称为RmiMonitorService的对象，并调用其上的方法
			monitorService = (RmiMonitorService) Naming.lookup("rmi://" + ip
					+ ":" + port + "/comm");

		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return monitorService;
	}

	public static void main(String args[]) throws RemoteException {
		MonitorClient client = new MonitorClient();
		System.out.println("发送告警信息:");
		String msg = "tsid=1022&devid=10001027&warnid=102&warntype=01&warnlevel=1&warnmsg=设备出错,请检查.";
		System.out.println(client.getValue(msg, "warnmsg"));
		client.interactive(1, msg);

	}

	public String getValue(String content, String key) {
		String value = "";

		int begin = 0, end = 0;
		begin = content.indexOf(key + "=");
		end = content.indexOf("&", begin);

		if (end == -1)
			end = content.length();
		value = content.substring(begin + key.length() + 1, end);
		return value;

	}
}
