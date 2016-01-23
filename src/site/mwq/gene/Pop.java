package site.mwq.gene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeMap;

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
	/**TODO 变异概率,暂时调大一点 由0.08改为0.2*/
	public static double muProb = 0.2;
	
	/** 种群规模*/
	public static int N = 100;
	
	/**个体集合，可能包含重复的解*/
	public static ArrayList<Individual> inds = new ArrayList<Individual>();
	
	//子代
	public static ArrayList<Individual> children = new ArrayList<Individual>();
	
	/**
	 * 基因算法选择操作，锦标赛算法
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
	 * 将inds复制到children
	 */
	public static void copyParentToChild(){
		children.clear();
		for(Individual ind:inds){
			children.add(new Individual(ind.hostVmMap));
		}
	}
	
	/**
	 * 基因算法交叉操作，多点交叉，每个机架交叉一次，hostid每次加3
	 * TODO 两点交叉，多点交叉，待考虑，实现的是一点交叉
	 */
	public static void crossover(ArrayList<Individual> indsParam){
		
		for(int i=0;i<N;i++){
			if(Utils.random.nextDouble() < coProb){	//以一定概率进行交叉
				
				//1、随机选择两个个体
				int ind1 = Utils.random.nextInt(indsParam.size());	
				int ind2 = Utils.random.nextInt(indsParam.size());
				
				//2、选择交叉点，一点交叉
				int point1 = Utils.random.nextInt(DataSet.hosts.size());
				
				//3、进行交叉操作
				TreeMap<Integer,HashSet<Integer>> hostVmMap1 = indsParam.get(ind1).hostVmMap;
				TreeMap<Integer,HashSet<Integer>> hostVmMap2 = indsParam.get(ind2).hostVmMap;
				
				HashSet<Integer> vms1 = hostVmMap1.get(point1);
				HashSet<Integer> vms2 = hostVmMap2.get(point1);

				HashSet<Integer> map1Lost_2Dup = new HashSet<Integer>();		//1丢失的vm，也就是2多余的vm
				HashSet<Integer> map1Dup_2Lost = new HashSet<Integer>();		//1重复的vm，也就是2缺失的vm
				
				for(int j:vms1){  	//vms1中含有，vms2不含的为丢失
					if(!vms2.contains(j)){
						map1Lost_2Dup.add(j);
					}
				}
				
				for(int j:vms2){  	//vms1不含有，vms2含有的为重复
					if(!vms1.contains(j)){
						map1Dup_2Lost.add(j);
					}
				}
				
				//先交叉、再删重复、最后插入缺失的vm
				hostVmMap1.put(point1, new HashSet<Integer>(vms2));		//交叉，vms2放置到ind1 point1
				for(int vmId:vms1){										//移除原vm列表
					indsParam.get(ind1).hostInds.get(point1).removeVm(vmId);
				}
				for(int vmId:vms2){										//添加另一个host的vm列表
					indsParam.get(ind1).vmHostMap.put(vmId, point1);		//更新vm host映射
					indsParam.get(ind1).hostInds.get(point1).addVm(vmId);
				}

				
				//删除重复
				removeDeplicate(indsParam,point1,ind1,hostVmMap1,map1Dup_2Lost);
				
				//插入缺失，对于每个缺少的vm，寻找与之通信量最大的host
				insertLoss(indsParam,Integer.MAX_VALUE,ind1,hostVmMap1,map1Lost_2Dup);
				
				//先交叉、再删重复、最后插入缺失的vm
				hostVmMap2.put(point1, new HashSet<Integer>(vms1));		//交叉,vms1放置到ind2的point1上
				for(int vmId:vms2){
					indsParam.get(ind2).hostInds.get(point1).removeVm(vmId);
				}
				for(int vmId:vms1){
					indsParam.get(ind2).vmHostMap.put(vmId, point1);		//更新vm host映射
					indsParam.get(ind2).hostInds.get(point1).addVm(vmId);
				}

				
				//删除重复
				removeDeplicate(indsParam,point1,ind2,hostVmMap2,map1Lost_2Dup);
				
				//插入缺失，对于每个缺少的vm，寻找与之通信量最大的host
				insertLoss(indsParam,Integer.MAX_VALUE,ind2,hostVmMap2,map1Dup_2Lost);
				
			}
		}
		
	}
	
	/**
	 * 对一个host vm映射进行删除重复操作，同时更新host信息
	 * 
	 * @param point		交叉点，忽略交叉点
	 * @param indId		随机选择的个体编号，用于更新host数据
	 * @param hostVmMap	这个编号的host vm映射
	 * @param dupList	重复的虚拟机id
	 */
	public static void removeDeplicate(ArrayList<Individual> indsParam, int point,int indId,
			TreeMap<Integer,HashSet<Integer>> hostVmMap,HashSet<Integer> dupList){
		
		for(int hostId:hostVmMap.keySet()){
			if(hostId==point){		//忽略交叉点
				continue;
			}
			for(int dupId:dupList){
				if(hostVmMap.get(hostId).contains(dupId)){	//删除重复vm，从映射中删除，同时从更新host数据
					hostVmMap.get(hostId).remove(dupId);
					dupList.remove(dupList);
					indsParam.get(indId).hostInds.get(hostId).removeVm(dupId);
				}
			}
		}
	}
	
	
	/**
	 * 将缺失的vm插入到一个映射中，同时更改host数据
	 * 
	 * @param point 		要忽略的点（host）
	 * @param indId			个体编号，将丢失的解插入到这个个体中，用于检查host
	 * @param hostVmMap		这个个体的host vm映射，
	 * @param lossList		丢失的vm id
	 */
	public static boolean insertLoss(ArrayList<Individual> indsParam,int point, int indId,
			TreeMap<Integer,HashSet<Integer>> hostVmMap,HashSet<Integer> lossList){
		
		for(int loseId:lossList){	//缺失的vm

			Collections.sort(DataSet.hostIds, new HostCommComp(indsParam.get(indId), loseId));
			
			//按照通信量降序排列host，并将vm插入其中
			boolean inserted = false;
			for(int hostId:DataSet.hostIds){
				if(hostId==point){	//忽略点
					continue;
				}
				if(indsParam.get(indId).hostInds.get(hostId).canHold(DataSet.vms.get(loseId))){
					hostVmMap.get(hostId).add(loseId);
					indsParam.get(indId).hostInds.get(hostId).addVm(loseId);
					indsParam.get(indId).vmHostMap.put(loseId, hostId);
					inserted = true;
					break;
				}
			}
			if(!inserted){
				System.err.println("有一台vm未添加成功");
				System.exit(1);
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 基因算法变异操作，选择一个解，然后删除它上面的一个（几个）host
	 * point是要选择的点
	 */
	public static void mutation(ArrayList<Individual> indsParam){
		
		for(int i=0;i<N;i++){
			if(Utils.random.nextDouble()<muProb){
				
				int indId = Utils.random.nextInt(indsParam.size());	//随机选择个体
				
				//选择含有vm数最少的一台host，移除上面的vm，但是不能含vm数为0
				int minVmCnt = Integer.MAX_VALUE;
				int point = -1;
				
				for(int hostId:indsParam.get(indId).hostVmMap.keySet()){
					int vmCnt = indsParam.get(indId).hostVmMap.get(hostId).size();
					if(vmCnt<minVmCnt && vmCnt !=0){
						point = hostId;
						minVmCnt = vmCnt;
					}
				}
				HashSet<Integer> lossList = indsParam.get(indId).hostVmMap.get(point);
				
				//将point上的vm置空，同时更新host数据
				indsParam.get(indId).hostVmMap.put(point, new HashSet<Integer>());
				for(int j:lossList){
					indsParam.get(indId).hostInds.get(point).removeVm(j);
				}
				//将lossList插入到其他host上

				insertLoss(indsParam, point, indId, indsParam.get(indId).hostVmMap, lossList);
			}
		}
	}
}

/**
 * host比较器，将host id按照与指定vm通信量降序排列
 * 
 * @author Email:qiuweimin@126.com
 * @date 2016年1月22日
 */
class HostCommComp implements Comparator<Integer>{

	public int vmId;
	public Individual ind;		//并不修改这个ind，只作参考
	
	public HostCommComp(Individual ind,int vmId){
		this.vmId = vmId;
		this.ind = ind;
	}
	
	@Override
	public int compare(Integer hostId1, Integer hostId2) {	//将host的id按照与指定vm通信量降序排列
		
		int commHost1 = 0;
		int commHost2 = 0;
		
		for(int vmIdEnd:ind.hostVmMap.get(hostId1)){
			commHost1 += DataSet.comMatrix[vmId][vmIdEnd];
		}
		
		for(int vmIdEnd:ind.hostVmMap.get(hostId2)){
			commHost2 += DataSet.comMatrix[vmId][vmIdEnd];
		}
		
		if(commHost1<commHost2){
			return 1;
		}else if(commHost1>commHost2){
			return -1;
		}
		return 0;
	}
	
}
