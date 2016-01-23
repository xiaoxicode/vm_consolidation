package site.mwq.main;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import site.mwq.cloudsim.HostDc;
import site.mwq.cloudsim.VmDc;
import site.mwq.utils.Utils;

public class Factory {
 
	//Host description
	public static final int peDefaultMips = 1000;			//一个cpu的计算能力
	public static final int peNumOfHost = 16;				//每个host的核数
//	public static final int ramOfHost = 4096;				//host内存（MB）
	public static final int ramOfHost = 8192;				//host内存（MB）
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
	public static final long sizeOfVm = 10000; 				// image size (MB)，硬盘大小
//	public static final int ramOfVm = 512; 					// vm memory (MB), 最好（512，1024，1536,2048）选一个
	public static final long bwOfVm = 1000;					// 带宽
//	public static final int pesNumberOfVm = 1; 				// number of cpus，最好（1-3随机）
	
	
	////////自增Id/////////////
	public static int peId = 0;
	public static int hostId = 0;
	public static int vmId = 0;
	
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
			DataSet.hostIds.add(hostId);	//追加hostId到数据集合中
			
			hosts.add(new HostDc(
					hostId++, 
					new RamProvisionerSimple(ramOfHost),
					new BwProvisionerSimple(bwOfHost),
					storageOfHost,
					pes, 
					new VmSchedulerTimeShared(pes)));
		}
		
		//同时添加到数据集合中
		DataSet.hosts = hosts;
		
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
	 * 随机产生一定数量 Vm ，内存从从512到2018随机产生
	 * pe数量从 1-4 随机
	 * @param num 要生成的Vm数量
	 * @return List 
	 */
	public static List<VmDc> createVmsRandomly(int num,int brokerId){
		List<VmDc> vms = new ArrayList<VmDc>();
		
//		int[] mems = {512,1024,1536,2048};

		//vm使用的内存，改为从512到2018随机产生
		int memUsed = 0;
		int[] pes = {1,2,3,4};
		
		for(int i=0;i<num;i++){
			
			int pesId = Utils.random.nextInt(4);
			memUsed = 512+Utils.random.nextInt(1536);	//随机产生内存，范围512-2048
			
			VmDc vm = new VmDc(vmId++, brokerId, mipsOfVm, pes[pesId], memUsed, 
					bwOfVm, sizeOfVm, vmm, new CloudletSchedulerTimeShared());
			vms.add(vm);
		}
		
		return vms;
	}
	
	
	public static List<VmDc> copyVmsWithAnotherId(List<VmDc> oldVms,int brokerId){
		List<VmDc> vms = new ArrayList<VmDc>();

		for(int i=0;i<oldVms.size();i++){
			VmDc oldVm = oldVms.get(i);
			VmDc newVm = new VmDc(oldVm.getId(),brokerId,mipsOfVm,oldVm.getNumberOfPes(),oldVm.getRam(),
					bwOfVm,sizeOfVm,vmm,new CloudletSchedulerTimeShared());
			vms.add(newVm);
		}
		
		return vms;
	}
	
	
}
