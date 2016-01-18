package site.mwq.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import site.mwq.cloudsim.HostDc;

public class Factory {
 
	//Host description
	public static final int peDefaultMips = 1000;			//一个cpu的计算能力
	public static final int peNumOfHost = 8;				//每个host的核数
	public static final int ramOfHost = 4096;				//host内存（MB）
	public static final long storageOfHost = 1000000; 		//host storage	(MB)
	public static final int bwOfHost = 10000;				//host 带宽
	
	//DatacenterCharacteristics
	public static final String arch = "x86"; 				// system architecture
	public static final String os = "Linux"; 				// operating system
	public static final String vmm = "Xen";
	public static final double time_zone = 10.0; 			// time zone this resource located
	public static final double cost = 3.0; 					// the cost of using processing in this resource
	public static final double costPerMem = 0.05; 			// the cost of using memory in this resource
	public static final double costPerStorage = 0.001; 		// the cost of using storage in this resource
	public static final double costPerBw = 0.0;				// the cost of using bw in this resource

	//VM description
	public static final int mipsOfVm = 1000;
	public static final long sizeOfVm = 10000; 				// image size (MB)
	public static final int ramOfVm = 512; 					// vm memory (MB), 最好（512，1024，1536,2048）选一个
	public static final long bwOfVm = 1000;					// 带宽
	public static final int pesNumberOfVm = 1; 				// number of cpus，最好（1-3随机）
	
	
	////////自增Id/////////////
	public static int peId = 0;
	public static int hostId = 0;
	public static int vmId = 0;
	
	
	/**随机数生成器*/
	public static Random random = new Random(System.currentTimeMillis());
	
	/**
	 * 创建Pe（CPU核）list
	 * @param num	要创建的个数
	 * @param mips	每个Pe的处理能力
	 * @return
	 */
	public static List<Pe> createPes(int num,int mips){
		
		List<Pe> pes = new ArrayList<Pe>();
		
		for(int i=0;i<num;i++){
			pes.add(new Pe(peId++,new PeProvisionerSimple(mips)));
		}
		
		return pes;
	}
	
	/**
	 * 创建Pe（CPU核）list，此方法使用默认的mips数值
	 * @param num	要创建的个数
	 * @return pe list
	 */
	public static List<Pe> createPes(int num){
		
		List<Pe> pes = new ArrayList<Pe>();
		
		for(int i=0;i<num;i++){
			pes.add(new Pe(peId++,new PeProvisionerSimple(peDefaultMips)));
		}
		
		return pes;
	}
	
	/**
	 * 使用默认参数创建Host，每个Host都是同质的，创建的host都是自定义Host
	 * @param num	创建host的个数
	 * @return List<Host> host列表
	 */
	public static List<HostDc> createHost(int num){
		List<HostDc> hosts = new ArrayList<HostDc>();
		
		for(int i=0;i<num;i++){
			List<Pe> pes = createPes(peNumOfHost); 
			hosts.add(new HostDc(
					hostId++, 
					new RamProvisionerSimple(ramOfHost),
					new BwProvisionerSimple(bwOfHost),
					storageOfHost,
					pes, 
					new VmSchedulerTimeShared(pes)));
		}
		
		return hosts;
	}
	
	/**
	 * 创建DatacenterCharacteristics，比如系统架构、操作系统、VMM等
	 * @param hosts
	 * @return
	 */
	public static DatacenterCharacteristics createDcCharacteristics(List<HostDc> hosts){
	
		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hosts, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);
		
		return characteristics;
	}
	
	/**
	 * 随机产生一定数量 Vm ，内存从[512，1024，1536,2048]中选一个。
	 * pe数量从 1-4 随机
	 * @param num 要生成的Vm数量
	 * @return List 
	 */
	public static List<Vm> createVmsRandomly(int num,int brokerId){
		List<Vm> vms = new ArrayList<Vm>();
		
		int[] mems = {512,1024,1536,2048};
		int[] pes = {1,2,3,4};
		
		for(int i=0;i<num;i++){
			
			int memsId = random.nextInt(4);
			int pesId = random.nextInt(4);
			
			Vm vm = new Vm(vmId++, brokerId, mipsOfVm, pes[pesId], mems[memsId], 
					bwOfVm, sizeOfVm, vmm, new CloudletSchedulerTimeShared());

			vms.add(vm);
		}
		
		return vms;
	}
	
	
}
