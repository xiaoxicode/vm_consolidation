package site.mwq.rmi;

/**
 * Description: 告警服务
 * 
 * @author Peter Wei
 * @version 1.0 2010-8-22
 */
public interface WarnService {

	/**
	 * 处理告警:告警来时的业务操作,实际操作是解析消息存库，然后界面Ajax定时刷新数据，获取实时告警展示
	 * 
	 * @param message
	 * @return
	 */
	public int dealWarn(String message);
}