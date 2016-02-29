package site.mwq.rmi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * 资源监控服务实现
 * @author Email:qiuweimin@126.com
 * @date 2016年2月19日
 */
public class ResMonitorServiceImpl extends UnicastRemoteObject implements
		ResMonitorService {

	private static final long serialVersionUID = -3771656108378649574L;

	public static final int SUCCSS = 1;

	public static final int FAIL = 0;

	public LocalResCollector collector = new LocalResCollector();
 
	/**
	 * 必须定义构造方法，因为要抛出RemoteException异常
	 * @throws RemoteException
	 */
	public ResMonitorServiceImpl() throws RemoteException {
		super();
	}

	@Override
	public Hashtable<Integer,double[]> getResUsage() throws RemoteException {

		Hashtable<Integer,double[]> res = null;
		
		res = collector.collectUsage();
		
		return res;
	}

	@Override
	public ArrayList<String> getVmNames() throws RemoteException {
		
		ArrayList<String> res = LibvirtSim.virshList();
		
		System.out.println("获得虚拟机列表："+res);
		
		return res;
	}

	@Override
	public int[] getVmCurMaxCores(String vmName) throws RemoteException {
		
		int curCoreNum = 0;
		int totalCoreNum = 0;
		int[] cores = new int[2];
		
		String cmd = "./virshDumpxml.sh "+vmName;	
		
		Runtime runtime = Runtime.getRuntime();
		Process pro = null;
		
		try { 
			pro = runtime.exec(cmd);		//执行shell命令 java执行需要root权限的shell命令，用sudo java Test
			BufferedReader in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			String line = "";
			while((line=in.readLine()) != null){
				
				if(line.indexOf("vcpu") != -1){
					
					int ts = line.indexOf(">")+1;
					int te = ts;
					while(line.charAt(te)!='<'){
						te++;
					}
					totalCoreNum = Integer.parseInt(line.substring(ts,te));
					 
					if(line.indexOf("current")!=-1){
						int cs = line.indexOf("current")+9;
						int ce = cs;
						while(line.charAt(ce)!='\''){
							ce++;
						}
						curCoreNum = Integer.parseInt(line.substring(cs,ce));
						
					}else{ 
						curCoreNum = totalCoreNum;
					}
					
				}
				
			}	
			in.close();
			pro.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		cores[0] = totalCoreNum;
		cores[1] = curCoreNum;
		
		return cores;
	}

}