package site.mwq.gene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import site.mwq.cloudsim.HostDc;
import site.mwq.cloudsim.VmDc;
import site.mwq.main.DataSet;
import site.mwq.utils.Utils;

/**
 * 种群，解的集合
 * @author E-mail:qiuweimin@126.com
 * @version 创建时间：2015年12月22日 下午7:22:17
 */
public class Pop {

	/**交叉概率*/
	public static double coProb = 0.85;
	/**TODO 变异概率,暂时调大一点 由0.08改为0.65*/
	public static double muProb = 0.65;
	
	/** 种群规模*/
	public static int N = 100;
	
	/**个体集合，可能包含重复的解*/
	public static ArrayList<Individual> inds = new ArrayList<Individual>();
	
	//子代
	public static ArrayList<Individual> children = new ArrayList<Individual>();
	
	/**
	 * 将inds复制到children，在初始化种群的时候调用
	 * 只被调用一次
	 */
	public static void copyParentToChild(){
		children.clear();
		for(Individual ind:inds){
			children.add(new Individual(ind.hostVmMap));
		}
	}
	
	/**
	 * 基因算法选择操作，锦标赛算法，经过选择，选出一代子代
	 * 随机选择两个个体，选择较好的那一个解,
	 * 在使用此方法之前，需要知道个体的rank和拥挤距离
	 */
	public static void select(){
		int indId1,indId2;
		
		for(int i=0;i<N;i++){
			indId1 = Utils.random.nextInt(inds.size());
			indId2 = Utils.random.nextInt(inds.size());
			
			if(inds.get(indId1).betterThan(inds.get(indId2))){
				children.add(new Individual(inds.get(indId1).hostVmMap));
			}else{
				children.add(new Individual(inds.get(indId2).hostVmMap));
			}
			
		}
		
	}
	
	/**
	 * 基因算法的交叉操作，执行一点交叉，以一定概率‘重新放置’编号不同的虚拟机，目的物理机为
	 * 通信代价最大的物理机
	 * 
	 * 2016/03/16更新
	 * @param indsParam 种群
	 */
	public static void crossoverVersion2(ArrayList<Individual> indsParam){
		for(int i=0;i<N;i++){
			if(Utils.random.nextDouble() < coProb){	//以一定概率进行交叉
				
				//1、随机选择两个个体
				int ind1Index = Utils.random.nextInt(indsParam.size());	
				int ind2Index = Utils.random.nextInt(indsParam.size());
				
				//2、选择交叉点，一点交叉
				int point1 = Utils.random.nextInt(DataSet.hosts.size());
				
				//3、进行交叉操作
				Individual ind1 = indsParam.get(ind1Index);
				Individual ind2 = indsParam.get(ind2Index);

				HashSet<Integer> vms1 = ind1.hostVmMap.get(point1);
				HashSet<Integer> vms2 = ind2.hostVmMap.get(point1);
			
				//3.1 计算在point1点，放置不同的虚拟机
				HashSet<Integer> map1Diff = new HashSet<Integer>();		//1丢失的vm，也就是2多余的vm
				HashSet<Integer> map2Diff = new HashSet<Integer>();		//1重复的vm，也就是2缺失的vm
				
				for(int j:vms1){  	//vms1中含有，vms2不含
					if(!vms2.contains(j)){
						map1Diff.add(j);
					}
				}
				
				for(int j:vms2){  	//vms2含有，vms1不含有，
					if(!vms1.contains(j)){
						map2Diff.add(j);
					}
				}
				
				//3.2 
				//在host1中（以一定概率）重新放置map1Diff中的虚拟机
				//在host2中（以一定概率）重新放置map2Diff中的虚拟机
				double rand = Utils.random.nextDouble();
				if(rand < 0.5){
					if(map1Diff.size()!=0){
						remapDiff(ind1,map1Diff,point1);
					}
					
					if(map2Diff.size()!=0){
						remapDiff(ind2,map2Diff,point1);
					}
				}
			
			}//具体交叉操作结束
			
		}//交叉操作结束，一共进行了N次
	}
	
	/**
	 * 在一个个体中，对一些放置相异的虚拟机进行重新放置，选择与其通信代价最大的物理机
	 * @param ind			个体
	 * @param diffVms		与另一个个体放置不同的虚拟机集合
	 * @param oriHostId	虚拟机所在的原来的位置，即物理机编号
	 */
	private static void remapDiff(Individual ind,HashSet<Integer> diffVms,int oriHostId){

		List<HostDc> hosts = ind.indHosts;
		TreeMap<Integer,HashSet<Integer>> hostVmMap = ind.hostVmMap;		
		Map<Integer,Integer> vmHostMap = ind.vmHostMap;
		
		//供排序用的hostId
		ArrayList<Integer> hostIds = new ArrayList<Integer>();
		for(int i=0;i<DataSet.hosts.size();i++){
			hostIds.add(i);
		}
		
		for(int vmId:diffVms){
			
			//将所有host id按照通信量排序
			Collections.sort(hostIds,new HostCommComp(ind, vmId));
			
			for(int i=0;i<hostIds.size();i++){
				
				//发现一台可以容纳此vm的host
				
				HostDc host = hosts.get(i);
				VmDc vm = DataSet.vms.get(vmId);
				
				if(host.canHold(vm)){
					//更新vm host映射
					Utils.removeVm(hosts.get(oriHostId), hostVmMap, vmId);
					Utils.addVm(host, hostVmMap, vmId, vmHostMap);
					break;
				}
			}
			
		}
		
	}
	
	/**
	 * 变异操作的第二个版本
	 * @param indsParam
	 */
	public static void mutationVersion2(ArrayList<Individual> indsParam){
		
		for(int i=0;i<N;i++){

			if(Utils.random.nextDouble()<muProb){
				
				int indId = Utils.random.nextInt(indsParam.size());	//随机选择个体

				//1、计算这个个体中每个host的负载
				Collections.sort(DataSet.hostIds, new HostLoadCom(indsParam.get(indId)));
				
				double prob = Utils.random.nextDouble();
				
				//执行relievePm的概率为0.1，执行loadBalance的概率为0.2
				if(prob < 0.15){
					ReleasePm(indsParam.get(indId), DataSet.hostIds);
				}else if (prob < 0.5){
					loadBalance(indsParam.get(indId), DataSet.hostIds);
				}

				reduceComm(indsParam.get(indId), DataSet.hostIds);
			}

		}
	}
	
	/**
	 * 选择负载最低的物理机，并将其上虚拟机进行迁移，迁移的目的物理机为与其通信代价最大的物理机。
	 * 修正：迁移的目的物理机为负载同样低的物理机
	 * 
	 * 2016/3/16 
	 * @param ind	种群个体
	 * @param hostIds 按照负载排好序（负载升序）的 主机id
	 */
	private static void ReleasePm(Individual ind,ArrayList<Integer> hostIds){
		List<HostDc> hosts = ind.indHosts;
		TreeMap<Integer,HashSet<Integer>> hostVmMap = ind.hostVmMap;
		
		//1.选择源物理机
		int sourceHostId = hostIds.get(0);
		HashSet<Integer> vmIds = new HashSet<Integer>(hostVmMap.get(sourceHostId));
		
		//2.尝试迁移其上面的虚拟机，目的物理机从第二台物理机开始选
		for(int vmId:vmIds){
			for(int i=1;i<hostIds.size();i++){
				int targetId = hostIds.get(i);
				
				if(hosts.get(targetId).canHold(DataSet.vms.get(vmId))){
					
					//改变映射关系
					Utils.removeVm(hosts.get(sourceHostId), hostVmMap, vmId);
					Utils.addVm(hosts.get(targetId), hostVmMap, vmId, ind.vmHostMap);
					break;
				} 
			}
		}//所有虚拟机迁移结束
		
	}
	
	/**
	 * 从负载最高的物理机上选择 "一台" 虚拟机并将其迁移到负载最低的物理机
	 * @param ind 个体
	 * @param hostIds 按照负载排好序（负载升序）的主机id
	 */
	private static void loadBalance(Individual ind,ArrayList<Integer> hostIds){
		List<HostDc> hosts = ind.indHosts;
		TreeMap<Integer,HashSet<Integer>> hostVmMap = ind.hostVmMap;
		
		//1.找到源物理机，lastIndex是hostIds的下标，并不是真正的hostId
		int lastIndex = hostIds.size()-1;
		
		//从hostIds的后面开始往前找，因为hostIds是按照负载升序排列的
		//while循环的目的是找一个不为空的物理机，
		//因为在HostLoadCom比较类中，将空物理机的负载设置为了最大值
		while(hostVmMap.get(hostIds.get(lastIndex)).size()==0){
			lastIndex--;
		}
		
		int sourceHostId = hostIds.get(lastIndex);
		
		HashSet<Integer> vmIds = hostVmMap.get(sourceHostId);
		
		//2.TODO 选择 "一台" 虚拟机进行迁移，（先随机选择一台吧，有时间可以考虑优化）
		int vmId = 0;
		for(int id:vmIds){
			vmId = id;
			break;
		}
		
		//3.选择目的物理机，因为负载是从低到高开始排序，所以从0开始找
		int hostIdIndex = 0;
		while(!(hosts.get(hostIds.get(hostIdIndex)).canHold(DataSet.vms.get(vmId))) && hostIdIndex<hostIds.size()){
			hostIdIndex++;
		}
		
		//没有能容纳的，不迁移了
		if(hostIdIndex==hostIds.size()){
			return;
		}
		
		int targetHostId = hostIds.get(hostIdIndex);
		
		//4.更新映射以及资源
		Utils.removeVm(hosts.get(sourceHostId), hostVmMap, vmId);
		Utils.addVm(hosts.get(targetHostId), hostVmMap, vmId, ind.vmHostMap);
		
	}
	
	/**
	 * 此方法在于减小数据中心中的通信代价，计算每台虚拟机除本机之外（与另外物理机的）的通信代价，
	 * 选择通信量代价最大的vm进行迁移
	 *
	 * @param ind
	 * @param hostIds
	 */
	private static void reduceComm(Individual ind,ArrayList<Integer> hostIds){

		//下标为虚拟机id，值为该虚拟机对应的最大的通信
		double[] maxComCost = new double[ind.vmHostMap.size()];
		int[] maxComHostId = new int[maxComCost.length];
		
		//用个两层循环好了，第一层虚拟机Id，对每个vm找与其通信代价最大的host
		for(int vmId:ind.vmHostMap.keySet()){
			
			int vmInHost = ind.vmHostMap.get(vmId);
			
			double maxCost = 0;
			int targetHostId = 0;
			

			//第二层，所有的物理机Id
			for(int hostId:ind.hostVmMap.keySet()){
				if(hostId==vmInHost
						|| !(ind.indHosts.get(hostId).canHold(DataSet.vms.get(vmId)))){
					continue;	//忽略当前所在的主机和不能容纳此vm的主机
				}
				
				//当前物理机与这个虚拟机的通信代价
				double curCost = 0;
				int cost,dis;
				HashSet<Integer> vmsInThisHost = ind.hostVmMap.get(hostId);
				for(int vmInThisHost:vmsInThisHost){
					
					if(DataSet.comMatrix[vmId][vmInThisHost]==0){continue;}
					
					dis = Utils.vmDistance(ind, vmId, vmInThisHost);
					cost = DataSet.comMatrix[vmId][vmInThisHost];
					
					switch(dis){
					case 0: break;
					case 1: curCost += cost; break;
					case 3: curCost += (cost<<1)+cost; break;
					case 5: curCost += (cost<<2)+cost;
					}
				}
				
				if(curCost>maxCost){
					maxCost = curCost;
					targetHostId = hostId;
				}
			}
			
			maxComCost[vmId] = maxCost;
			maxComHostId[vmId] = targetHostId;
		}
		
		//然后找最大的进行迁移
		double maxBenefit = 0;
		int maxVmTarget = 0;
		
		double secBenefit = 0;
		int secVmTarget = 0;
		
		for(int i=0;i<maxComCost.length;i++){
			if(maxComCost[i]>secBenefit){
				if(maxComCost[i]>maxBenefit){	//大于最大值
					maxVmTarget = i;
					maxBenefit = maxComCost[i];
				}else{							//大于次大值，小于最大值
					secVmTarget = i;
					secBenefit = maxComCost[i];
				}
			}
		}
		int hostIdTarget = maxComHostId[maxVmTarget];
		int hostIdSource = ind.vmHostMap.get(maxVmTarget);
		
		Utils.removeVm(ind.indHosts.get(hostIdSource), ind.hostVmMap, maxVmTarget);
		Utils.addVm(ind.indHosts.get(hostIdTarget), ind.hostVmMap, maxVmTarget, ind.vmHostMap);
		
		int secHostTarget = maxComHostId[secVmTarget];
		int secHostSource = ind.vmHostMap.get(secVmTarget);
		
		Utils.removeVm(ind.indHosts.get(secHostSource), ind.hostVmMap, secVmTarget);
		Utils.addVm(ind.indHosts.get(secHostTarget), ind.hostVmMap, secVmTarget, ind.vmHostMap);
		
	}

}//类结束括号

/**
 * host比较器，将host id按照与指定vm通信代价(非量) 降序排列
 * 
 * @author Email:qiuweimin@126.com
 * @date 2016年1月22日
 */
class HostCommComp implements Comparator<Integer>{

	public int vmId;
	public Individual ind;		//并不修改这个ind，只作参考
	
	public double[] comCosts;
	
	public HostCommComp(Individual ind,int vmId){
		this.vmId = vmId;
		this.ind = ind;
		this.comCosts = new double[ind.hostVmMap.size()];
		computeCom();
	}
	
	//统一计算虚拟机与各个物理机的通信量
	private void computeCom(){
		TreeMap<Integer,HashSet<Integer>> hostVmMap = ind.hostVmMap;
		
		int dis,cost;
		for(int i=0;i<comCosts.length;i++){
			double comm = 0;
			for(int destId:hostVmMap.get(i)){
				
				if(DataSet.comMatrix[vmId][destId]==0){continue;}
				
				dis = Utils.vmDistance(ind, vmId, destId);
				cost = DataSet.comMatrix[vmId][destId];
				
				switch(dis){
					case 0: break;
					case 1: comm += cost; break;
					case 3: comm += (cost<<1)+cost; break;
					case 5: comm += (cost<<2)+cost;
				}
			}
			comCosts[i] = comm;
		}
	}
	
	@Override
	public int compare(Integer hostId1, Integer hostId2) {	//将host的id按照与指定vm通信量降序排列
		
		if(comCosts[hostId1] < comCosts[hostId2]){
			return 1;
		}else if(comCosts[hostId1] > comCosts[hostId2]){
			return -1;
		}
		
		return 0;
	}
	
}

/**
 * 将一组host的id按照负载 升序 排列
 * @author Email:qiuweimin@126.com
 * @date 2016年3月16日
 */
class HostLoadCom implements Comparator<Integer>{

	double[] loads = null;
	List<HostDc> hosts = null;
	Individual ind = null;
	
	double avgMem = 0;
	double avgCpu = 0;
	
	/**
	 * 
	 * @param ind 个体
	 * @param increase	是否为升序排列
	 */
	public HostLoadCom(Individual ind){ 
		this.hosts = ind.indHosts;
		this.ind = ind;
		this.loads = new double[hosts.size()];
		
		//计算平均资源利用率
		this.avgCpu = Utils.getAvgCpu(ind);
		this.avgMem = Utils.getAvgMem(ind);
		
		computeLoad();
		
	}
	
	private void computeLoad(){
		double mul = 0;
		for(int i=0;i<hosts.size();i++){
			HostDc host = hosts.get(i);
			if(ind.hostVmMap.get(host.getId()).size()!=0){	//至少包含一台虚拟机
				mul = (1-host.getCpuRate())*(1-host.getMemRate());
				loads[i] = 1/mul;
			}else{											//一台虚拟机都不包含，设置其为最大值，以此忽略此台主机
				loads[i] = Double.MAX_VALUE;
			}
		}
	}
	
	@Override
	public int compare(Integer o1, Integer o2) {
		
		if(loads[o1] > loads[o2]){
			return 1;
		}else if(loads[o1] < loads[o2]){
			return -1;
		}
		
		return 0;
	}
}


///**
// * 基因算法交叉操作，多点交叉，每个机架交叉一次，hostid每次加3
// * 两点交叉，多点交叉，待考虑，实现的是一点交叉
// */
//public static void crossover(ArrayList<Individual> indsParam){
//	
//	for(int i=0;i<N;i++){
//		if(Utils.random.nextDouble() < coProb){	//以一定概率进行交叉
//			
//			//1、随机选择两个个体
//			int ind1Index = Utils.random.nextInt(indsParam.size());	
//			int ind2Index = Utils.random.nextInt(indsParam.size());
//			
//			//2、选择交叉点，一点交叉
//			int point1 = Utils.random.nextInt(DataSet.hosts.size());
//			
//			//3、进行交叉操作
//			TreeMap<Integer,HashSet<Integer>> hostVmMap1 = indsParam.get(ind1Index).hostVmMap;
//			TreeMap<Integer,HashSet<Integer>> hostVmMap2 = indsParam.get(ind2Index).hostVmMap;
//			
//			HashSet<Integer> vms1 = hostVmMap1.get(point1);
//			HashSet<Integer> vms2 = hostVmMap2.get(point1);
//
//			HashSet<Integer> map1Lost_2Dup = new HashSet<Integer>();		//1丢失的vm，也就是2多余的vm
//			HashSet<Integer> map1Dup_2Lost = new HashSet<Integer>();		//1重复的vm，也就是2缺失的vm
//			
//			for(int j:vms1){  	//vms1中含有，vms2不含的为丢失
//				if(!vms2.contains(j)){
//					map1Lost_2Dup.add(j);
//				}
//			}
//			
//			for(int j:vms2){  	//vms1不含有，vms2含有的为重复
//				if(!vms1.contains(j)){
//					map1Dup_2Lost.add(j);
//				}
//			}
//			
//			//先交叉、再删重复、最后插入缺失的vm
//			hostVmMap1.put(point1, new HashSet<Integer>(vms2));		//交叉，vms2放置到ind1 point1
//			
//			for(int vmId:vms1){										//移除原vm列表
//				indsParam.get(ind1Index).indHosts.get(point1).removeVmUpdateResource(vmId);
//			}
//			for(int vmId:vms2){										//添加另一个host的vm列表
//				indsParam.get(ind1Index).vmHostMap.put(vmId, point1);		//更新vm host映射
//				indsParam.get(ind1Index).indHosts.get(point1).addVmUpdateResource(vmId);
//			}
//
//			
//			//删除重复
//			removeDeplicate(indsParam,point1,ind1Index,hostVmMap1,map1Dup_2Lost);
//			
//			//插入缺失，对于每个缺少的vm，寻找与之通信量最大的host
//			insertLoss(indsParam,Integer.MAX_VALUE,ind1Index,hostVmMap1,map1Lost_2Dup);
//			
//			//先交叉、再删重复、最后插入缺失的vm
//			hostVmMap2.put(point1, new HashSet<Integer>(vms1));		//交叉,vms1放置到ind2的point1上
//			for(int vmId:vms2){
//				indsParam.get(ind2Index).indHosts.get(point1).removeVmUpdateResource(vmId);
//			}
//			for(int vmId:vms1){
//				indsParam.get(ind2Index).vmHostMap.put(vmId, point1);		//更新vm host映射
//				indsParam.get(ind2Index).indHosts.get(point1).addVmUpdateResource(vmId);
//			}
//
//			
//			//删除重复
//			removeDeplicate(indsParam,point1,ind2Index,hostVmMap2,map1Lost_2Dup);
//			
//			//插入缺失，对于每个缺少的vm，寻找与之通信量最大的host
//			insertLoss(indsParam,Integer.MAX_VALUE,ind2Index,hostVmMap2,map1Dup_2Lost);
//			
//		}
//	}
//	
//}





///**
// * 将缺失的vm插入到一个映射中，同时更改host数据
// * 
// * @param point 		要忽略的点（host）
// * @param indId			个体编号，将丢失的解插入到这个个体中，用于检查host
// * @param hostVmMap		这个个体的host vm映射，
// * @param lossList		丢失的vm id
// */
//private static boolean insertLoss(ArrayList<Individual> indsParam,int point, int indId,
//		TreeMap<Integer,HashSet<Integer>> hostVmMap,HashSet<Integer> lossList){
//	
//	for(int loseId:lossList){	//缺失的vm
//
//		
//		ArrayList<Integer> hostIds = new ArrayList<Integer>();
//		for(int i=0;i<hostVmMap.size();i++){
//			hostIds.add(i);
//		}
//		
//		Collections.sort(hostIds, new HostCommComp(indsParam.get(indId), loseId));
//
//		//按照通信量降序排列host，并将vm插入其中
//		boolean inserted = false;
//		for(int hostId:DataSet.hostIds){
//			if(hostId==point){	//忽略点
//				continue;
//			}
//			if(indsParam.get(indId).indHosts.get(hostId).canHold(DataSet.vms.get(loseId))){
//				hostVmMap.get(hostId).add(loseId);
//				indsParam.get(indId).indHosts.get(hostId).addVmUpdateResource(loseId);
//				indsParam.get(indId).vmHostMap.put(loseId, hostId);
//				inserted = true;
//				break;
//			}
//		}
//		if(!inserted){
//			System.err.println("有一台vm未添加成功");
//			System.exit(1);
//			return false;
//		}
//	}
//	
//	return true;
//}
//
///**
// * 基因算法变异操作，选择一个解，然后删除它上面的一个（几个）host
// * point是要选择的点
// */
//public static void mutation(ArrayList<Individual> indsParam){
//	
//	for(int i=0;i<N;i++){
//		if(Utils.random.nextDouble()<muProb){
//			
//			int indId = Utils.random.nextInt(indsParam.size());	//随机选择个体
//			
//			//选择含有vm数最少的一台host，移除上面的vm，但是不能含vm数为0
//			int minVmCnt = Integer.MAX_VALUE;
//			int point = -1;
//			
//			for(int hostId:indsParam.get(indId).hostVmMap.keySet()){
//				int vmCnt = indsParam.get(indId).hostVmMap.get(hostId).size();
//				if(vmCnt<minVmCnt && vmCnt !=0){
//					point = hostId;
//					minVmCnt = vmCnt;
//				}
//			}
//			HashSet<Integer> lossList = indsParam.get(indId).hostVmMap.get(point);
//			
//			//将point上的vm置空，同时更新host数据
//			indsParam.get(indId).hostVmMap.put(point, new HashSet<Integer>());
//			for(int j:lossList){
//				indsParam.get(indId).indHosts.get(point).removeVmUpdateResource(j);
//			}
//			//将lossList插入到其他host上
//
//			insertLoss(indsParam, point, indId, indsParam.get(indId).hostVmMap, lossList);
//		}
//	}
//}


///**
// * 对一个host vm映射进行删除重复操作，同时更新host信息
// * 
// * @param point		交叉点，忽略交叉点
// * @param indId		随机选择的个体编号，用于更新host数据
// * @param hostVmMap	这个编号的host vm映射
// * @param dupList	重复的虚拟机id
// */
//private static void removeDeplicate(ArrayList<Individual> indsParam, int point,int indId,
//		TreeMap<Integer,HashSet<Integer>> hostVmMap,HashSet<Integer> dupList){
//	
//	for(int hostId:hostVmMap.keySet()){
//		if(hostId==point){		//忽略交叉点
//			continue;
//		}
//		for(int dupId:dupList){
//			if(hostVmMap.get(hostId).contains(dupId)){	//删除重复vm，从映射中删除，同时从更新host数据
//				hostVmMap.get(hostId).remove(dupId);
//				dupList.remove(dupList);
//				indsParam.get(indId).indHosts.get(hostId).removeVmUpdateResource(dupId);
//			}
//		}
//	}
//}