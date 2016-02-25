package site.mwq.rmi;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * RPC服务端
 * @author Email:qiuweimin@126.com
 * @date 2016年2月19日
 */
public class RmiServer {

	public String ip = "localhost";

	public int port = 8888;

	/*** 启动RMI注册服务，并注册远程对象*/
	public void init() {
		
		try {
			LocateRegistry.createRegistry(port);
			ResMonitorService comm = new ResMonitorServiceImpl();
			Naming.bind("//" + ip + ":" + port + "/comm", comm);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		RmiServer rmiServer = new RmiServer();
		System.out.println("资源收集服务初始化");
		rmiServer.init();

	}
}