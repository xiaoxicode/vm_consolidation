package site.mwq.rmi;


/**
 * Description: 告警服务
 * 
 * @author Peter Wei
 * @version 1.0 2010-8-22
 */
public class WarnServiceImpl implements WarnService {

	public int dealWarn(String message) {
		// 告警处理方法
		System.out.println("已接收网络告警");
// …
		return 1;
	}

}