package site.mwq.compare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import site.mwq.cloudsim.HostDc;
import site.mwq.cloudsim.VmCluster;
import site.mwq.cloudsim.VmDc;
import site.mwq.gene.Individual;
import site.mwq.main.DataSet;
import site.mwq.utils.Utils;

/**
 * 对比试验，比较经典的一个，2007年
 * @author Email:qiuweimin@126.com
 * @date 2016年1月23日
 */
public class Sandpiper {
	
	/**sandpiper只对一个解进行一次整合*/
	public Individual ind;
	
	//此Sandpiper所对应的数据结构
	public List<HostDc> hosts;
	public TreeMap<Integer,HashSet<Integer>> hostVmMap;		//host到vm的映射，均用id表示
	public Map<Integer,Integer> vmHostMap;					//vm到host的映射，均用id表示
	
	/**用在计算VSR时，防止分母为零*/
	public static double littleValue = 0.0001; 
	
	/**内存利用率阈值 */
	public static double memThreshold = 0.73;
	
	/**CPU利用率阈值 */
	public static double cpuThreshold = 0.73;
	
	/**网络利用率阈值 */
	public static double netThreshold = 0.73;
	
	public static int moveCnt = 0;
	/**
	 * Sandpiper的构造函数
	 * @param hostVmMap
	 */
	public Sandpiper(Map<Integer,HashSet<Integer>> hostVmMap){
		ind = new Individual(hostVmMap);
		this.hosts = ind.indHosts;
		this.hostVmMap = ind.hostVmMap;
		this.vmHostMap = ind.vmHostMap;
	}
	
	
	public void moveVm(){
		
		//首先将host按照volume降序排列
		Collections.sort(DataSet.hostIds,new HostVolComp(ind));
		 
		//将vm从volume最高的host移动到最低的host
		for(int hostId:DataSet.hostIds){
			
			HostDc sourceHost = hosts.get(hostId);
			
			if(sourceHost.isOverLoad()){	//这台host over load了
				
				//将这个host上的vm按照vsr排序
				ArrayList<Integer> vmIds = new ArrayList<Integer>(hostVmMap.get(hostId));
				Collections.sort(vmIds,new VmVsrComp());
				
				int vmIndex = 0;
				
				while(sourceHost.isOverLoad() && vmIndex<vmIds.size()){
				
					//最初选择第0个，即VSR最大的VM
					VmDc vm2Move = DataSet.vms.get(vmIds.get(vmIndex));
					int vmId = vm2Move.getId();
					
					boolean inserted = false;
					
					for(int targetHostId=hosts.size()-1;targetHostId>=0;targetHostId--){
						
						
						if(hosts.get(targetHostId).canHoldAndNotOverLoad(vm2Move)){
							
							//添加操作
							hosts.get(targetHostId).addVmUpdateResource(vmId);
							hostVmMap.get(targetHostId).add(vmId);
						
							//更新vm到host的映射
							vmHostMap.put(vmId, targetHostId);
							
							//移除操作
							hosts.get(hostId).removeVmUpdateResource(vmId);
							hostVmMap.get(hostId).remove(vmId);
							
							inserted = true;
							//将副本中的第一个（VSR最大的）vm移除，只删除，不增加vmIndex
							vmIds.remove(vmIndex);
							
							moveCnt++;
							break;
						}
							
					}//结束查找目的host循环
					
					if(!inserted){	//前一个VSR较大的VM没有被插入，选择次大的VSR
						vmIndex++;
					}
				}//源host负载高就循环
			}//判断host是否负载过高的if语句
			
			if(sourceHost.isOverLoad()){	//在进行迁移之后仍然负载过高，进行交换
			//	System.err.println("PM "+hostId+" 仍然负载过高,进行交换操作");
			
				ArrayList<Integer> vmIds = new ArrayList<Integer>(hostVmMap.get(hostId));
				Collections.sort(vmIds,new VmVsrComp());
				
				VmDc vm2Move = DataSet.vms.get(vmIds.get(0));

				boolean swaped = false;
				
				for(int i=hosts.size()-1;i>=0;i--){
					HostDc targetHost = hosts.get(i); 
					HashSet<Integer> vmsInTargetHost = hostVmMap.get(targetHost.getId());
					
					VmCluster  vc = new VmCluster();
					
					for(int vmId:vmsInTargetHost){
						VmDc vm = DataSet.vms.get(vmId);
						vc.addVm(vm);
						
						//执行交换操作
						if(sourceHost.canSwapVmWithCluster(vm2Move.getId(), vc)
								&& targetHost.canSwapClusterWithVm(vc, vm2Move.getId())){
							
							//sourceHost添加VmCluster, 目的host删除VmCluster中的每个vm
							for(int k=0;k<vc.vmIds.size();k++){
								
								int vmIdTmp = vc.vmIds.get(k);
								
								sourceHost.addVmUpdateResource(vmIdTmp); 
								hostVmMap.get(sourceHost.getId()).add(vmIdTmp);
								
								vmHostMap.put(vmIdTmp, sourceHost.getId());
								
								targetHost.removeVmUpdateResource(vmIdTmp);
								hostVmMap.get(targetHost.getId()).remove(vmIdTmp);
								
								moveCnt++;	//迁移次数加1
							}
							
							targetHost.addVmUpdateResource(vm2Move.getId()); 
							hostVmMap.get(targetHost.getId()).add(vm2Move.getId());
							
							vmHostMap.put(vm2Move.getId(), targetHost.getId());
							
							sourceHost.removeVmUpdateResource(vm2Move.getId());
							hostVmMap.get(sourceHost.getId()).remove(vm2Move.getId());
							
							moveCnt++;	//迁移次数加1
							swaped = true;
							//跳出组VmCluster循环
							break;
						}
						
					}
					
					if(swaped){	//已经进行交换操作，跳出循环
						break;
					}
				}
				
			}
			
		}
		
		for(int hostId:DataSet.hostIds){
			if(hosts.get(hostId).isOverLoad()){
				moveCnt += 1;
			}
		}
		
		System.out.println("comCost:"+Utils.cc.objVal(ind));
		System.out.println("pmCnt:"+Utils.pc.objVal(ind));
		System.out.println("moveCnt: "+moveCnt);
		System.out.println("balance:"+Utils.ba.objVal(ind));
		
//		Utils.disHostVmMap(hostVmMap);	测试正常
//		Utils.disVmHostMap(vmHostMap);
	}
	
	
}



/**
 * 用于将host按照Volume从大到小排序，只排序id
 * @author Email:qiuweimin@126.com
 * @date 2016年1月23日
 */
class HostVolComp implements Comparator<Integer>{

	private Individual ind;
	
	public HostVolComp(Individual ind){
		this.ind = ind;
	}
	
	@Override
	public int compare(Integer o1, Integer o2) {
		
		double vsr1 = ind.indHosts.get(o1).getVol();
		double vsr2 = ind.indHosts.get(o2).getVol();
		
		if(vsr1<vsr2){
			return 1;
		}else if(vsr1>vsr2){
			return -1;
		}
		return 0;
	}
}

/**
 * 将vmid按照VSR降序排列
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