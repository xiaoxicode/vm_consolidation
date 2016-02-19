package site.mwq.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Description: 实时显示RMI服务接口.
 * 
 * RMI接口必须扩展接口java.rmi.Remote
 * 
 * @author Peter Wei
 * @version 1.0 Feb 25, 2009
 */
public interface RmiMonitorService extends Remote {
	/**
	 * 实时显示对外接口
	 * 
	 * @param funindex
	 *            功能号
	 * @param param
	 *            键名列表，也就是实际传输的内容
	 * @return
	 * @throws RemoteException
	 *             远程接口方法必须抛出java.rmi.RemoteException
	 */
	public int interactive(int funindex, String param) throws RemoteException;
}