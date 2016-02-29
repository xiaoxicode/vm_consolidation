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
	 * 获取资源利用，key为资源编号，0cpu,1mem,2net
	 * value为二维数组，表示资源总量和使用量
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
	
	/**
	 * 获取一台虚拟机的最大核数和当前核数，下标0为最大核数，下标1为当前核数
	 * @param vmName
	 * @return
	 * @throws RemoteException
	 */
	public int[] getVmCurMaxCores(String vmName) throws RemoteException;
	
}