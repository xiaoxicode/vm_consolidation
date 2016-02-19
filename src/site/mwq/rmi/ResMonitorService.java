package site.mwq.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 监控jiekou
 * @author Email:qiuweimin@126.com
 * @date 2016年2月19日
 */
public interface ResMonitorService extends Remote {

	/**
	 * 获取资料利用率
	 * @return
	 * @throws RemoteException
	 */
	public double[] getResUsage() throws RemoteException;
}