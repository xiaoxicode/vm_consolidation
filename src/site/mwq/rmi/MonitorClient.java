package site.mwq.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


/**
 * 监控客户端，周期性检查各个PM负载
 * 
 * @author Email:qiuweimin@126.com
 * @date 2016年2月19日
 */
public class MonitorClient {

	public ResMonitorService resMonitorService;

	/**key为ip，value为对应的主机名*/
	public HashMap<String,String> ipName = null;
	
	public final int port = 8888;
	public final double resThreshold = 0.5;			//阈值，设置为0.5
	
	
	public MonitorClient(){
		ipName = new HashMap<String,String>();
		ipName.put("114.212.82.69", "server004");
		ipName.put("114.212.84.70", "server005");
	}
	
	/**
	 * 调用远程方法，并返回各个资源的利用率，
	 * 数组中存放的依次为cpu、men、net利用率
	 * @param funindex
	 * @param param
	 * @return
	 */
	public HashMap<String,double[]> collectUsages() {
		
		HashMap<String,double[]> res = new HashMap<String,double[]>();
		
		double[] result = null;
		try {
			for(String ip:ipName.keySet()){		//检查每个ip的资源利用情况，并放入hashMap
				
				result = getMonitorService(ip).getResUsage();
				res.put(ip, result);
				
				System.out.print(ipName.get(ip)+" ");
				for(int i=0;i<result.length;i++){
					System.out.print(result[i]+"  ");
				}
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
	 * 监控各个物理机资源利用，并根据负载做出迁移决策
	 * @throws RemoteException 
	 */
	public void monitor() throws RemoteException{
		
		
		HashMap<String,double[]> loads = collectUsages();		//RPC调用
		
		HashSet<String> overLoads = new HashSet<String>();	//负载过高的ip
		HashSet<String> unOverLoads = new HashSet<String>();	//负载不过高的ip

		for(String ip:loads.keySet()){
			int cnt = 0;
			for(double load:loads.get(ip)){
				if(load>resThreshold){
					overLoads.add(ipName.get(ip));
					System.out.println(ipName.get(ip)+" over load !!");
					break;
				}
				cnt++;
			}
			if(cnt==3){
				System.out.println(ipName.get(ip)+" is not over load");
				unOverLoads.add(ipName.get(ip));
			}
		}
		
		//迁移吧，目前仅考虑两台，将一台pm上的虚拟机迁移到另一台pm
		if(overLoads.size()!=0 && unOverLoads.size()!=0){
			String source = "";
			String dest = "";
			for(String pmName:overLoads){
				source = pmName; break;
			}
			for(String pmName:unOverLoads){
				dest = pmName; break;
			}
			
			ArrayList<String> vmNames = getMonitorService(source).getVmNames(); //RPC调用 
			
			System.out.println(vmNames);
			
			System.out.println("migration...");
			System.out.println("source:"+source+" dest:"+dest+" vm:"+vmNames.get(0));
			
			LibvirtSim.migrate(source, dest, vmNames.get(0)); 	//TODO 触发迁移
			
			try {
				Thread.sleep(8000);			//等待8秒，然后再进行检测
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
	//项目主函数
	public static void main(String args[]){
		MonitorClient client = new MonitorClient();
		
		while(true){
			
			try {
				
				client.monitor();	//监控
				Thread.sleep(5000);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

}
