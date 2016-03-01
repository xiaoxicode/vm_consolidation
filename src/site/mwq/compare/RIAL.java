package site.mwq.compare;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import site.mwq.cloudsim.HostDc;
import site.mwq.gene.Individual;

/**
 * 2014 infocom
 * @author Email:qiuweimin@126.com
 * @date 2016年2月29日
 */
public class RIAL {
	/**RIAL只对一个解进行一次整合*/
	public Individual ind;
	
	//此RIAL所对应的数据结构，即某一时刻的映射状态
	public List<HostDc> hosts;
	public TreeMap<Integer,HashSet<Integer>> hostVmMap;		//host到vm的映射，均用id表示
	public Map<Integer,Integer> vmHostMap;					//vm到host的映射，均用id表示
	
	public static int moveCnt = 0;
	
	/**分别表示cpu,mem,net资源的权重，对于每个负载过高的物理机，这三个值是不一样的*/
	public double w1k,w2k,w3k;
	
	/**wt是虚拟机通信量Tij相对于资源利用的权重*/
	public double wt = 0.5;
	
	/**wd是性能下降的权重*/
	public double wd = 0.3;
	
	/**需要迁移的虚拟机集合*/
	public ArrayList<Integer> needMigVms;
	
	/**负载过高的物理机集合*/
	public ArrayList<Integer> heavyPms; 
	
	/**负载不高的物理机集合*/
	public ArrayList<Integer> lightPms;
	
	/**
	 * RIAL的构造函数
	 * @param hostVmMap
	 */
	public RIAL(Map<Integer,HashSet<Integer>> hostVmMap){
		ind = new Individual(hostVmMap);
		this.hosts = ind.indHosts;
		this.hostVmMap = ind.hostVmMap;
		this.vmHostMap = ind.vmHostMap;
		
		this.needMigVms = new ArrayList<Integer>();
		this.heavyPms = new ArrayList<Integer>();
		this.lightPms = new ArrayList<Integer>();
	}
	
	/**
	 * 将host划分为负载过高的集合与负载不高的集合
	 */
	public void divideHosts(){
		
		for(HostDc host:hosts){
			if(host.isOverLoad()){
				heavyPms.add(host.getId());
			}else{
				lightPms.add(host.getId());
			}
		}
	}
	
	/**对负载过高的PM进行迁移操作*/
	public void move(){
		
		for(int hostId:heavyPms){	//遍历每个负载过高的物理机
			
			HostDc host = hosts.get(hostId);
			//1、给每种资源设定资源利用率
			
			if(host.getCpuRate()>Sandpiper.cpuThreshold){
				
			}
			
			
		}
		
	}
	
}
