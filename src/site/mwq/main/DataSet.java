package site.mwq.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import site.mwq.cloudsim.HostDc;
import site.mwq.cloudsim.VmDc;

/**
 * 数据集合，所有数据中心中的数据都保存在这里
 * 包含初始解
 * 注意，这个类中保存的信息都是固定不变的
 * 
 * @author Email:qiuweimin@126.com
 * @date 2016年1月18日
 */
public class DataSet {
	
	//保存host与vm的配置信息
	public static List<HostDc> hosts = new ArrayList<HostDc>();
	public static List<VmDc> vms = new ArrayList<VmDc>();
	
	//初始的vm与host映射关系
	public static Map<Integer,ArrayList<Integer>> hostVmMap;		//host到vm的映射，均用id表示
	public static Map<Integer,Integer> vmHostMap;					//vm到host的映射，均用id表示
	
	public static void init(int hostNum,int vmNum){
		
		hostVmMap = new TreeMap<Integer,ArrayList<Integer>>();
		for(int i=0;i<hostNum;i++){
			hostVmMap.put(i, new ArrayList<Integer>());
		}
		vmHostMap = new TreeMap<Integer,Integer>();
	}
}
