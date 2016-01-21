package site.mwq.gene;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import site.mwq.main.DataSet;
import site.mwq.utils.Utils;

/**
 * 种群，解的集合
 * @author E-mail:qiuweimin@126.com
 * @version 创建时间：2015年12月22日 下午7:22:17
 */
public class Population {

	/**交叉概率*/
	public static double coProb = 0.85;
	/**TODO 变异概率,暂时调大一点 由0.08改为0.2*/
	public static double muProb = 0.2;
	
	/** 种群规模*/
	public static int N = 1000;
	
	/**个体集合，可能包含重复的解*/
	public static ArrayList<Individual> inds = new ArrayList<Individual>();
	
	//子代
	public static ArrayList<Individual> children = new ArrayList<Individual>();
	
	/**
	 * 基因算法选择操作，锦标赛算法
	 * 随机选择两个个体，选择较好的那一个解
	 */
	public static void select(){
		for(int i=0;i<N;i++){
			
		}
	}
	
	/**
	 * 基因算法交叉操作，多点交叉，每个机架交叉一次，hostid每次加3
	 * TODO 两点交叉，多点交叉，待考虑，实现的是一点交叉
	 */
	public static void crossover(){
		
		for(int i=0;i<N;i++){
			if(Utils.random.nextDouble() < coProb){	//以一定概率进行交叉
				
				//1、随机选择两个个体
				int ind1 = Utils.random.nextInt(inds.size());	
				int ind2 = Utils.random.nextInt(inds.size());
				
				//2、选择交叉点，两点交叉
				int point1 = Utils.random.nextInt(DataSet.hosts.size());
				
				//3、进行交叉操作
				TreeMap<Integer,HashSet<Integer>> hostVmMap1 = inds.get(ind1).hostVmMap;
				TreeMap<Integer,HashSet<Integer>> hostVmMap2 = inds.get(ind2).hostVmMap;
				
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
				hostVmMap1.put(point1, new HashSet<Integer>(vms2));		//交叉
				
				//删除重复
				removeDeplicate(point1,ind1,hostVmMap1,map1Dup_2Lost);
				
				//插入缺失，对于每个缺少的vm，寻找与之通信量最大的host
				insertLoss(Integer.MAX_VALUE,ind1,hostVmMap1,map1Lost_2Dup);
				
				
				
				//先交叉、再删重复、最后插入缺失的vm
				hostVmMap2.put(point1, new HashSet<Integer>(vms1));		//交叉
				
				//删除重复
				removeDeplicate(point1,ind2,hostVmMap2,map1Lost_2Dup);
				
				//插入缺失，对于每个缺少的vm，寻找与之通信量最大的host
				insertLoss(Integer.MAX_VALUE,ind2,hostVmMap2,map1Dup_2Lost);
				
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
	public static void removeDeplicate(int point,int indId,
			TreeMap<Integer,HashSet<Integer>> hostVmMap,HashSet<Integer> dupList){
		
		for(int hostId:hostVmMap.keySet()){
			if(hostId==point){
				continue;
			}
			for(int dupId:dupList){
				if(hostVmMap.get(hostId).contains(dupId)){	//删除重复物理机，从映射中删除，同时从更新host数据
					hostVmMap.get(hostId).remove(dupId);
					inds.get(indId).hostInds.get(hostId).removeVm(dupId);
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
	public static void insertLoss(int point, int indId,
			TreeMap<Integer,HashSet<Integer>> hostVmMap,HashSet<Integer> lossList){
		
		for(int loseId:lossList){	//缺失的vm
			long commMax = Long.MIN_VALUE;			//同时保存最大值和次大值，当最大值host放不下时，选用第二大值
			long commSecond = Long.MIN_VALUE;
			long temp = 0;
			int maxHostId = -1;
			int secondHostId = -1;
			for(int hostId:hostVmMap.keySet()){	//每台host
				
				if(hostId==point){
					continue;
				}
				
				for(int vmId:hostVmMap.get(hostId)){	//累加每个host上的每个vm通信量
					temp += DataSet.comMatrix[loseId][vmId];
				}
				if(temp	> commMax){			//先将最大值更新为第二大值，更新第一大值
					commSecond = commMax;
					secondHostId = maxHostId;
					commMax = temp;
					maxHostId = hostId;
				}else if(temp>commSecond){	//只更新第二大值
					commSecond = temp;
					secondHostId = hostId;
				}
			}
			//放置
			if(inds.get(indId).hostInds.get(maxHostId).canHold(DataSet.vms.get(loseId))){
				hostVmMap.get(maxHostId).add(loseId);
				inds.get(indId).hostInds.get(maxHostId).addVm(loseId);
			}else if(inds.get(indId).hostInds.get(secondHostId).canHold(DataSet.vms.get(loseId))){
				hostVmMap.get(secondHostId).add(loseId);
				inds.get(indId).hostInds.get(secondHostId).addVm(loseId);
			}else{
				System.err.println("oh shit，找了两个都放不下。");
			}
		}
	}
	
	/**
	 * 基因算法变异操作，选择一个解，然后删除它上面的一个（几个）host
	 */
	public static void mutation(){
		
		for(int i=0;i<N;i++){
			if(Utils.random.nextDouble()<muProb){
				
				int indId = Utils.random.nextInt(inds.size());	//随机选择个体
				
				//TODO 变异操作。应该不是随机选择一点，而是选择含vm比较少的点
				int point = Utils.random.nextInt(DataSet.hosts.size());
				
				HashSet<Integer> lossList = inds.get(indId).hostVmMap.get(point);
				
				//将point上的vm置空，同时更新host数据
				inds.get(indId).hostVmMap.put(point, new HashSet<Integer>());
				for(int j:lossList){
					inds.get(indId).hostInds.remove(DataSet.vms.get(j));
				}
				//将lossList插入到其他host上
				insertLoss(point, indId, inds.get(indId).hostVmMap, lossList);
			}
		}
	}
}
