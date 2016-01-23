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
import site.mwq.targets.ComCost;
import site.mwq.targets.MigCnt;
import site.mwq.targets.PmCnt;

public class Utils {
	
	//求目标值用的
	private static MigCnt mc = new MigCnt();
	private static PmCnt pc = new PmCnt();
	private static ComCost cc = new ComCost();
	
	/**随机数生成器，项目所有的随机数均由其生成*/
	public static Random random = new Random(System.currentTimeMillis());
	
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
		for(HostDc host:ind.hostInds){
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
		System.out.println("migCnt:"+mc.objVal(ind)+"  comcost:"+cc.objVal(ind)+"  pmCnt:"+pc.objVal(ind));
	}
}
