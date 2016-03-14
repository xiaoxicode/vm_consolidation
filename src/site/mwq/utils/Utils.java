package site.mwq.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import site.mwq.cloudsim.HostDc;
import site.mwq.cloudsim.VmDc;
import site.mwq.gene.Individual;
import site.mwq.gene.Pop;
import site.mwq.main.DataSet;
import site.mwq.targets.Balance;
import site.mwq.targets.ComCost;
import site.mwq.targets.MigCnt;
import site.mwq.targets.PmCnt;

public class Utils {
	
	//求目标值用的
	public static MigCnt mc = new MigCnt();
	public static PmCnt pc = new PmCnt();
	public static ComCost cc = new ComCost();
	public static Balance ba = new Balance();
	
	
	/**随机数生成器，项目所有的随机数均由其生成*/
	public static Random random = new Random(System.currentTimeMillis());
	
	
	/**
	 * 获得两台物理机之间的距离（相隔的交换机的个数）
	 * 该系统物理机架构参考 ComCost类
	 * 使用三层树形交换机架构，分为核心层、聚集层、接入层、机架、物理机
	 * 从上到下，交换机和物理机数目为 2 3 6 18
	 * 
	 * 同一个物理机距离为			0
	 * 同一个机架交换机距离为		1
	 * 同一个聚集交换机距离为		3
	 * 否则距离为				5
	 * @param pmi
	 * @param pmj
	 * @return
	 */
	public static int getPmDis(int pmIdi,int pmIdj){ 
		
		if(pmIdi==pmIdj){	//物理机编号相等，返回0
			return 0;
		}
		
		pmIdi	/= 3;
		pmIdj	/= 3;
		
		if(pmIdi==pmIdj){	//机架编号相等，返回1
			return 1;
		}
		
		pmIdi /= 2;
		pmIdj /= 2;
		
		if(pmIdi==pmIdj){	//聚集层交换机编号相等，返回3，否则返回5
			return 3;
		}
		
		return 5;
	}
	
	
	/**
	 * 打印矩阵
	 * @param matrix
	 */
	public static void disMatrix(int[][] matrix){
		int row = matrix.length;
		int col = matrix[0].length;
		
		for(int i=0;i<row;i++){
			for(int j=0;j<col;j++){
				System.out.print(matrix[i][j]+" ");
			}
			System.out.println();
		}
	}
	
	/**
	 * 打印一个个体的host的资源剩余情况
	 * @param ind
	 */
	public static void disHostAvail(Individual ind){
		for(HostDc host:ind.indHosts){
			System.out.println("Id:"+host.getId()+", mem:"+host.getMemAvail()+", pe:"+host.getPeAvail()+", bw:"+host.getNetAvail());
		}
	}
	
	/**
	 * 打印host vm映射数组
	 * @param hostVmMap
	 */
	public static void disHostVmMap(Map<Integer,HashSet<Integer>> hostVmMap){
		for(int hostId:hostVmMap.keySet()){ 
			System.out.println(hostId+" : "+hostVmMap.get(hostId));
		}
	}
	
	/**
	 * 打印host vm映射数组
	 * @param hostVmMap
	 */
	public static void disHostVmMapDetail(Map<Integer,ArrayList<Integer>> hostVmMap){
		for(int hostId:hostVmMap.keySet()){ 
			System.out.print(hostId+": ");
			System.out.print("pe "+DataSet.hosts.get(hostId).getPeList().size()+" ");
			System.out.print("mem "+DataSet.hosts.get(hostId).getRam()+" ");
			
			List<Integer> vms = hostVmMap.get(hostId);
			System.out.print("[");
			for(int vmId:vms){
				System.out.print(vmId+": pe "+DataSet.vms.get(vmId).getNumberOfPes()+" ");
				System.out.print("mem "+DataSet.vms.get(vmId).getRam()+"; ");
			}
			System.out.println(" ]");
		}
	}
	
	/**
	 * 打印vm host映射数组
	 * @param vmHostMap
	 */
	public static void disVmHostMap(Map<Integer,Integer> vmHostMap){
		for(int vmId:vmHostMap.keySet()){ 
			System.out.println(vmId+" : "+vmHostMap.get(vmId));
		}
	}
	
	
	/**
	 * 打印每个vm的详细信息
	 * @param vms
	 */
	public static void disVms(List<VmDc> vms){
		
		int width = 10;
		
		for(int i=0;i<vms.size();i++){
			
			System.out.println(vms.get(i).getId()+"："+vms.get(i).getNumberOfPes()+","+vms.get(i).getRam());
			
			if(i%width == 0){
				//System.out.println();
			}
		}
	}
	
	
	/**
	 * 打印数组
	 * @param objs
	 */
	public static void disArray(double[] objs){
		for(int i=0;i<objs.length;i++){
			System.out.print(objs[i]+" ");
		}
	}
	
	/**
	 * 打印整个种群的host vm映射
	 */
	public static void disPopu(){ 
		for(int i=0;i<Pop.inds.size();i++){
			
			disHostVmMap(Pop.inds.get(i).hostVmMap);
			System.out.println("------------");
		}
		
		System.out.println("Population size:"+Pop.inds.size());
	}
	
	
	/**
	 * 计算一个个体的各个目标值
	 * @param ind
	 */
	public static void disIndVal(Individual ind){
		System.out.println("migCnt:"+mc.objVal(ind)+"  comcost:"+cc.objVal(ind)+"  pmCnt:"+pc.objVal(ind)+" ban:"+ba.objVal(ind));
	}
}
