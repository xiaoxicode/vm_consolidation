package site.mwq.utils;

import java.util.List;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

public class Utils {
	
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
