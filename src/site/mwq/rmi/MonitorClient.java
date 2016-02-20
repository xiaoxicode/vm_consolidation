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

	public String ip = "localhost";
	//public String ip = "192.168.1.17";
	
	/**key为ip，value为对应的主机名*/
	public HashMap<String,String> ipName = null;
	
	public final int port = 8888;
	public final double resThreshold = 0.7;
	
	
	public MonitorClient(){
		ipName = new HashMap<String,String>();
		ipName.put("192.168.137.134", "pm2");
		ipName.put("192.168.137.135", "pm1");
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
				
				for(int i=0;i<result.length;i++){
					System.out.print(result[i]+" - ");
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

		} catch (NotBoundException | MalformedURLException | RemoteException e) {
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
		
		HashSet<String> overLoadIps = new HashSet<String>();	//负载过高的ip
		HashSet<String> unOverLoadIps = new HashSet<String>();	//负载不过高的ip

		for(String ip:loads.keySet()){
			int cnt = 0;
			for(double load:loads.get(ip)){
				if(load>resThreshold){
					overLoadIps.add(ip);
					break;
				}
				cnt++;
			}
			if(cnt==3){
				unOverLoadIps.add(ip);
			}
		}
		
		//迁移吧，目前仅考虑两台，将一台pm上的虚拟机迁移到另一台pm
		if(overLoadIps.size()!=0 && unOverLoadIps.size()!=0){
			String sourceIp = "";
			String destIp = "";
			for(String ip:overLoadIps){
				sourceIp = ip; break;
			}
			for(String ip:unOverLoadIps){
				destIp = ip; break;
			}
			
			ArrayList<String> vmNames = getMonitorService(sourceIp).getVmNames(); //RPC调用
			
			LibvirtSim.migrate(sourceIp, destIp, vmNames.get(0)); 	//TODO 触发迁移
			
			try {
				Thread.sleep(120000);			//等待两分钟，然后再进行检测
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
