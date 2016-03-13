package site.mwq.rmi;

import java.util.HashMap;

/**
 * 映射配置文件
 * @author Email:qiuweimin@126.com
 * @date 2016年2月26日
 */
public class AddressMap {
	
	
	/**key为ip，value为对应的主机名*/
	public static HashMap<String,String> pmIpName = null;
	public static HashMap<String,String> vmNameIp = null;
	
	static{
		pmIpName = new HashMap<String,String>();
		pmIpName.put("114.212.82.69", "server004");	 	//物理机 ip和名字映射
		pmIpName.put("114.212.84.70", "server005");
		
		vmNameIp = new HashMap<String,String>();
		vmNameIp.put("vm1", "114.212.87.238");			//虚拟机名字和ip映射
		vmNameIp.put("vm2", "114.212.81.64");
		vmNameIp.put("vm3", "114.212.81.14");
	}
}
