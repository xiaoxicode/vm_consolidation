package site.mwq.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * 资源监控服务实现
 * @author Email:qiuweimin@126.com
 * @date 2016年2月19日
 */
public class ResMonitorServiceImpl extends UnicastRemoteObject implements
		ResMonitorService {

	private static final long serialVersionUID = -3771656108378649574L;

	public static final int SUCCSS = 1;

	public static final int FAIL = 0;

	public LocalResCollector collector = new LocalResCollector();
 
	/**
	 * 必须定义构造方法，因为要抛出RemoteException异常
	 * @throws RemoteException
	 */
	public ResMonitorServiceImpl() throws RemoteException {
		super();
	}

	/**
	 * 获取资源利用率，返回double数组
	 */
	public double[] getResUsage() throws RemoteException {

		double[] res = null;
		
		res = collector.collectUsage();
		
		return res;
	}

}