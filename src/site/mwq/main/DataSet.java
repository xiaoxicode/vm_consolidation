package site.mwq.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.cloudbus.cloudsim.Host;

/**
 * 数据集合，所有数据中心中的数据都保存在这里
 * @author Email:qiuweimin@126.com
 * @date 2016年1月18日
 */
public class DataSet {

	
	public static List<Host> hosts = new ArrayList<Host>();
	
	/**key为hostId，value为包含的vm，用vmId表示*/
	public static Map<Integer,ArrayList<Integer>> hostVmMap = new TreeMap<Integer,ArrayList<Integer>>();
	
	/**
	 * 初始化数据集合，用host数和vm数
	 * @param hostNum
	 * @param vmNum
	 */
	public static void init(int hostNum,int vmNum){
		for(int i=0;i<hostNum;i++){
			hostVmMap.put(i, new ArrayList<Integer>());
		}
	}
}
