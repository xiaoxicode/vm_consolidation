package site.mwq.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


/**
 * Description: 实时显示RMI接口实现.
 * 
 * 实现RMI接口及远程方法（继承UnicastRemoteObject）
 * 
 * @author Peter Wei
 * @version 1.0 Feb 25, 2009
 */
public class RmiMonitorServiceImpl extends UnicastRemoteObject implements
		RmiMonitorService {

	private static final long serialVersionUID = -3771656108378649574L;

	public static final int SUCCSS = 1;

	public static final int FAIL = 0;

	public WarnService warnService;

	/**
	 * 必须定义构造方法，因为要抛出RemoteException异常
	 * 
	 * @throws RemoteException
	 */
	public RmiMonitorServiceImpl() throws RemoteException {
		super();
	}

	public int interactive(int funindex, String param) throws RemoteException {

		int result = FAIL;
		switch (funindex) {
		// 告警
		case (1): {

			// warnService = (WarnService) AppContext.getAppContext().getBean(
			// "warn.warnService");
			// 实际应用是从Spring应用中获取告警Service,如上代码
			warnService = new WarnServiceImpl();
			// 网络告警的业务操作
			warnService.dealWarn(param);
			result = SUCCSS;
		}
			break;
		case (2):
			// do other biz
			break;
		}
		// ......

		return result;
	}

	public WarnService getWarnService() {
		return warnService;
	}

	public void setWarnService(WarnService warnService) {
		this.warnService = warnService;
	}

}