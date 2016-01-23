package site.mwq.sandpiper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;

import site.mwq.cloudsim.VmDc;
import site.mwq.gene.Individual;
import site.mwq.main.DataSet;
import site.mwq.targets.ComCost;
import site.mwq.targets.PmCnt;

/**
 * 对比试验，比较经典的一个，2007年
 * @author Email:qiuweimin@126.com
 * @date 2016年1月23日
 */
public class Sandpiper {
	
	/**sandpiper只对一个解进行一次整合*/
	public Individual ind;
	
	/**用在计算VSR时，防止分母为零*/
	public static double littleValue = 0.0001; 
	
	/**内存利用率阈值 */
	public static double memThreshold = 0.7;
	
	/**CPU利用率阈值 */
	public static double cpuThreshold = 0.7;
	
	/**网络利用率阈值 */
	public static double netThreshold = 0.7;
	
	public static int moveCnt = 0;
	/**
	 * Sandpiper的构造函数
	 * @param hostVmMap
	 */
	public Sandpiper(Map<Integer,HashSet<Integer>> hostVmMap){
		ind = new Individual(hostVmMap);
	}
	
	
	public void moveVm(){
		
		//首先将host按照vsr降序排列
		Collections.sort(DataSet.hostIds,new HostVsrComp(ind));
		 
		//将vm从vsr最高的host移动到最低的host
		for(int hostId:DataSet.hostIds){
			if(ind.hostInds.get(hostId).isOverLoad()){	//这台host over load了
				
				//将这个host上的vm按照vsr排序
				ArrayList<Integer> vmIds = new ArrayList<Integer>(ind.hostVmMap.get(hostId));
				Collections.sort(vmIds,new VmVsrComp());
				
				int i = 0;
				
				while(ind.hostInds.get(hostId).isOverLoad()){
				
					VmDc vm2Move = DataSet.vms.get(vmIds.get(i));
					
					boolean inserted = false;
					
					for(int j=DataSet.hosts.size()-1;j>=0;j--){		//j是host编号
						if(ind.hostInds.get(j).canHoldAndNotOverLoad(vm2Move)){
							ind.hostInds.get(j).addVm(vm2Move.getId());
							ind.hostVmMap.get(j).add(vm2Move.getId());
							ind.vmHostMap.put(vm2Move.getId(), j);
							inserted = true;
							moveCnt++;
						}
							
					}
					
					if(!inserted){
						System.err.println("sandpiper,一台vm没有添加成功。");
						break;
					}
				}
				
			}
		}
		
		for(int hostId:DataSet.hostIds){
			if(ind.hostInds.get(hostId).isOverLoad()){	
				moveCnt += 2;
			}
		}
		
		ComCost cc = new ComCost();
		System.out.println("comCost:"+cc.objVal(ind));
		PmCnt pc = new PmCnt();
		System.out.println("pmCnt:"+pc.objVal(ind));
		System.out.println("moveCnt: "+moveCnt);
		
	}
	
	
	
}

/**
 * 用于将host安装vsr从大到小排序，只排序id
 * @author Email:qiuweimin@126.com
 * @date 2016年1月23日
 */
class HostVsrComp implements Comparator<Integer>{

	private Individual ind;
	
	public HostVsrComp(Individual ind){
		this.ind = ind;
	}
	
	@Override
	public int compare(Integer o1, Integer o2) {
		
		double vsr1 = ind.hostInds.get(o1).getVsr();
		double vsr2 = ind.hostInds.get(o2).getVsr();
		
		if(vsr1<vsr2){
			return 1;
		}else if(vsr1>vsr2){
			return -1;
		}
		return 0;
	}
}

/**
 * 将vmid按照vsr降序排列
 * @author Email:qiuweimin@126.com
 * @date 2016年1月23日
 */
class VmVsrComp implements Comparator<Integer>{

	@Override
	public int compare(Integer o1, Integer o2) {

		double vsr1 = DataSet.vms.get(o1).getVsr();
		double vsr2 = DataSet.vms.get(o2).getVsr();
		
		if(vsr1<vsr2){
			return 1;
		}else if(vsr1>vsr2){
			return -1;
		}
		return 0;
	}
	
}