package site.mwq.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;


/**
 * 监控客户端，周期性检查各个PM负载
 * 
 * @author Email:qiuweimin@126.com
 * @date 2016年2月19日
 */
public class MonitorClient {

	public ResMonitorService resMonitorService;

	public String ip = "localhost";
	//public String ip = "192.168.1.17";

	public int port = 8888;

	/**
	 * 调用远程方法，并返回各个资源的
	 * @param funindex
	 * @param param
	 * @return
	 */
	public int collectUsages() {
		double[] result = null;
		try {
			result = getMonitorService().getResUsage();
			
			for(int i=0;i<result.length;i++){
				System.out.print(result[i]+" - ");
			}
			System.out.println();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public ResMonitorService getMonitorService() {
		
		try {
			// 在RMI服务注册表中查找名称为ResMonitorService的对象，并调用其上的方法
			resMonitorService = (ResMonitorService) Naming.lookup("rmi://" + ip + ":" + port + "/comm");

		} catch (NotBoundException | MalformedURLException | RemoteException e) {
			e.printStackTrace();
		}
		return resMonitorService;
	}

	public static void main(String args[]) throws RemoteException {
		MonitorClient client = new MonitorClient();
		
		while(true){
			client.collectUsages();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
