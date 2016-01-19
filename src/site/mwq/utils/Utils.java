package site.mwq.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

import site.mwq.cloudsim.VmDc;
import site.mwq.main.DataSet;

public class Utils {
	
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
	 * 打印host vm映射数组
	 * @param hostVmMap
	 */
	public static void disHostVmMap(Map<Integer,ArrayList<Integer>> hostVmMap){
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
	 * 打印每个vm所在的Host，这个函数目前报NullPointerException
	 * @param vms
	 */
	public static void disVms(List<Vm> vms){
		
		int width = 10;
		
		for(int i=0;i<vms.size();i++){
			
			System.out.print(vms.get(i).getId()+"："+vms.get(i).getHost().getId());
			
			if(i%width == 0){
				System.out.println();
			}
		}
	}
	
	/**
	 * 此方法目前同样不可用
	 * @param dc
	 */
	public static void disDC(Datacenter dc){
		List<Host> hosts = dc.getHostList();
		int width = 10;
		
		for(int i=0;i<hosts.size();i++){
			
			System.out.print("###Test###"+hosts.get(i).getVmList());
			
			if(i%width == 0){
				System.out.println();
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
}
