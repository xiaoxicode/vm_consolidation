package site.mwq.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;


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
	 * 由资源利用率计算负载，计算公式为：
	 * 1/(1-cpu) * 1/(1-mem) * 1/(1-net)
	 * @param loads
	 * @return
	 */
	public double volume(double[] loads){
		double res = 1;
		
		for(int i=0;i<loads.length;i++){
			res *= 1/(1-loads[i]);
		}
		
		return res;
	}
	
	
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
			
			if(pms.get(i).isOverLoaded()){	//while(pms.get(i).isOverLoaded())   预测负载，并迁移
				
				ArrayList<String> vmNames = getMonitorService(pms.get(i).ip).getVmNames();
				
				Hashtable<Integer,double[]> vmResUsage = null;
				
				ArrayList<VM> vms = new ArrayList<VM>();
				
				for(int j=0;j<vmNames.size();j++){
					
					String vmIp = AddressMap.vmNameIp.get(vmNames.get(i));
					vmResUsage = getMonitorService(vmIp).getResUsage();
					vms.add(new VM(vmIp,vmNames.get(i),vmResUsage));
				}
				
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
							System.out.println("source:"+sourcePm+" dest:"+destPm+" vm:"+vm);
							
							LibvirtSim.migrate(sourcePm, destPm, vm); 	//TODO 触发迁移
							pms.get(i).removeVm(vms.get(0));
							try {
								Thread.sleep(1000);			//等待8秒，然后再进行检测
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
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
					System.out.print(res.get(i)[0]+" "+res.get(i)[0]+"; ");
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

}
