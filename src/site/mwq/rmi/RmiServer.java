package site.mwq.rmi;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;


/**
 * Description: RMI服务端.
 * 
 * @author Peter Wei
 * @version 1.0 Feb 25, 2009
 */
public class RmiServer {

	public String ip = "localhost";

	public int port = 8889;

	/**
	 * 启动RMI注册服务，并注册远程对象.实际应用中是在Spring初始化并启动
	 */
	public void init() {
		try {
			LocateRegistry.createRegistry(port);
			// 创建一个远程对象
			RmiMonitorService comm = new RmiMonitorServiceImpl();
			Naming.bind("//" + ip + ":" + port + "/comm", comm);
		} catch (RemoteException e) {
			System.out.println("创建远程对象发生异常！" + e.toString());
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			System.out.println("发生重复绑定对象异常！" + e.toString());
			e.printStackTrace();
		} catch (MalformedURLException e) {
			System.out.println("发生URL畸形异常！" + e.toString());
			e.printStackTrace();
		}
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public static void main(String[] args) {
		// 实际应用中是在Spring初始化并启动
		RmiServer rmiServer = new RmiServer();
		System.out.println("RMI服务初始化:");
		rmiServer.init();

	}
}