package site.mwq.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * 监控接口
 * @author Email:qiuweimin@126.com
 * @date 2016年2月19日
 */
public interface ResMonitorService extends Remote {

	/**
	 * 获取资料利用率
	 * @return
	 * @throws RemoteException
	 */
	public Hashtable<Integer,double[]> getResUsage() throws RemoteException;
	
	
	/**
	 * 获取这个PM上正在的vm，返回虚拟机名
	 * @return
	 * @throws RemoteException
	 */
	public ArrayList<String> getVmNames() throws RemoteException;
	
}