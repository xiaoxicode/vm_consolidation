package site.mwq.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * 监控客户端，周期性检查各个PM负载
 * 
 * @author Email:qiuweimin@126.com
 * @date 2016年2月19日
 */
public class MonitorClient {

	private ResMonitorService resMonitorService;

	private final int port = 8888;
	
	/**
	 * 调用远程方法，并返回各个资源的利用率，
	 * 数组中存放的依次为cpu、men、net利用率
	 * @param funindex
	 * @param param
	 * @return
	 */
	public HashMap<String,Hashtable<Integer,double[]>> collectUsages() {
		
		HashMap<String,Hashtable<Integer,double[]>> res = new HashMap<String,Hashtable<Integer,double[]>>();
		
		//key为资源类型下标，0cpu,1mem,2net
		Hashtable<Integer,double[]> resUse = null;
		
		
//下面的注释是使用线程池的方法
//		ExecutorService pool = Executors.newFixedThreadPool(3);
//		
//		ArrayList<Future<Hashtable<Integer,double[]>>> resultFutures = new ArrayList<Future<Hashtable<Integer,double[]>>>();
//		
//		
//		for(String ip:AddressMap.pmIpName.keySet()){	
//			resultFutures.add(pool.submit(new CollectTask(ip)));
//		}
//		
//		ArrayList<Hashtable<Integer,double[]>> results = new ArrayList<Hashtable<Integer,double[]>>();
//		
//		for(Future<Hashtable<Integer,double[]>> future:resultFutures){
//			try {
//				results.add(future.get());
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			} catch (ExecutionException e) {
//				e.printStackTrace();
//			}
//		}
		
		try {
			for(String ip:AddressMap.pmIpName.keySet()){		//检查每个ip的资源利用情况，并放入hashMap
				
				resUse = getMonitorService(ip).getResUsage();
				res.put(ip, resUse);
				
				System.out.print(AddressMap.pmIpName.get(ip)+": ");
				
				System.out.print(resUse.get(0)[0]+" "+resUse.get(0)[1]+"; ");
				System.out.print(resUse.get(1)[0]+" "+resUse.get(1)[1]+"; ");
				System.out.print(resUse.get(2)[0]+" "+resUse.get(2)[1]+"; ");
				
				System.out.println();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return res;
	}

	public ResMonitorService getMonitorService(String ip) {
		
		try {
			// 在RMI服务注册表中查找名称为ResMonitorService的对象，并调用其上的方法
			resMonitorService = (ResMonitorService) Naming.lookup("rmi://" + ip + ":" + port + "/comm");

		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return resMonitorService;
	}

	/**
	 * 监控物理机资源率，并根据负载做出迁移决策
	 * @throws RemoteException 
	 */
	public void monitorRes() throws RemoteException{
		
		//key为ip，value为资源利用
		HashMap<String,Hashtable<Integer,double[]>> ipRes =  collectUsages();
		
		ArrayList<PM> pms = new ArrayList<PM>();
		for(String ip:ipRes.keySet()){
			pms.add(new PM(ip,ipRes.get(ip)));
		}
		
		//将PM按照volume降序排列
		Collections.sort(pms,new PMComparator());
		
		for(int i=0;i<pms.size();i++){
			
			//获取该物理机虚拟机资源利用信息
			ArrayList<String> vmNames = getMonitorService(pms.get(i).ip).getVmNames();
			
			System.out.println(pms.get(i).name+" "+vmNames);
			Hashtable<Integer,double[]> vmResUsage = null;
			ArrayList<VM> vms = new ArrayList<VM>();
			
			for(int j=0;j<vmNames.size();j++){
				if(vmNames.get(j).equals("vm3") || vmNames.get(j).equals("online_judge")){
					   continue;       //先不处理这两个虚拟机，链接不上
				}
				String vmIp = AddressMap.vmNameIp.get(vmNames.get(j));
				vmResUsage = getMonitorService(vmIp).getResUsage();
				vms.add(new VM(vmIp,vmNames.get(j),vmResUsage));
			}
			
			if(pms.get(i).isOverLoaded()){	//while(pms.get(i).isOverLoaded())   预测负载，并迁移
				
				//将vm按照VSR降序排列
				Collections.sort(vms,new VMComparator());
				
				//如果超过负载就迁移
				while(pms.get(i).isOverLoaded()){
					
					//从后面查找VM
					for(int k=pms.size()-1;k>=0 && k>i;k--){	
						if(pms.get(k).canHold(vms.get(0))){		//vm从VSR最大开始查找
							
							String sourcePm = pms.get(i).name;
							String destPm = pms.get(k).name;
							String vm = vms.get(0).name;
							
							System.out.println("migration...");
							System.out.println("source: "+sourcePm+" dest:"+destPm+" vm:"+vm);
							
							LibvirtSim.migrate(sourcePm, destPm, vm); 	//TODO 触发迁移
							pms.get(i).removeVm(vms.get(0));
							try {
								Thread.sleep(1000);			//等待1秒，然后再进行检测
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}
				
			}else{		//物理机资源未超过负载，检查虚拟机负载是否过高，是否可以进行resize操作
				
				for(int j=0;j<vms.size();j++){
					
					if(vms.get(j).name.equals("vm3") || vms.get(j).name.equals("online_judge")){
						continue;	//先不处理vm3这个虚拟机，链接不上
					}
					
					//虚拟机负载过高（TODO 此处只关心CPU负载，因为只用CpuCost.java产生CPU负载，实际逻辑要复杂）
					//1、要知道vm j所在的物理机，此处即为pms.get(i)
					//2、用这个物理机查看vm j的信息
					if(vms.get(j).isOverLoaded){	
						
						int[] cores = getMonitorService(pms.get(i).ip).getVmCurMaxCores(vms.get(j).name);
						
						int totalCoreNum = cores[0];
						int curCoreNum = cores[1];
						
						if(totalCoreNum>curCoreNum){	//还有未使用的核，虚拟机可以进行调整大小操作
							System.out.println("add vcpu for: "+vms.get(j).name+" from "+curCoreNum +" to "+totalCoreNum);
							LibvirtSim.virshSetvcpus(vms.get(j).name,totalCoreNum);
						}
						
					}
				}
				
			}
			
		}
		
	}
	
	//项目主函数
	public static void main(String args[]){
		MonitorClient client = new MonitorClient();
		
		while(true){
			
			try {
				
			//	client.monitor();	//监控
				Hashtable<Integer,double[]> res = client.getMonitorService("114.212.86.5").getResUsage();
			
				for(int i=0;i<3;i++){
					System.out.print(res.get(i)[0]+" "+res.get(i)[1]+"; ");
				}
				System.out.println();
			
				Thread.sleep(5000);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 获取资源利用率的任务
	 * @author Email:qiuweimin@126.com
	 * @date 2016年5月16日
	 */
	class CollectTask implements Callable<Hashtable<Integer,double[]>>{

		public String ip;
		
		public CollectTask(String ip){
			this.ip = ip;
		}
		
		@Override
		public Hashtable<Integer, double[]> call() throws Exception {
			Hashtable<Integer, double[]> resUse = getMonitorService(ip).getResUsage();
			return resUse;
		}
	}

}
