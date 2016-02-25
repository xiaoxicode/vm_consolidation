package site.mwq.rmi;

import java.util.Hashtable;

import site.mwq.resource.CpuUsage;
import site.mwq.resource.MemUsage;
import site.mwq.resource.NetUsage;

/**
 * 本地资源利用数据收集器
 * @author Email:qiuweimin@126.com
 * @date 2016年2月19日
 */
public class LocalResCollector {

	private CpuUsage cpuUsage = CpuUsage.getInstance();
	private MemUsage memUsage = MemUsage.getInstance();
	private NetUsage netUsage = NetUsage.getInstance();
	
	/**key为资源编号，0cpu,1mem,2net，value为资源利用率*/
	private Hashtable<Integer,double[]> resTable = null;
	
	
	public Hashtable<Integer,double[]> collectUsage() { 
		
		System.out.println("现在去收集信息...");
		
		resTable = new Hashtable<Integer,double[]>();
		
		Thread cpuThread = new Thread(new Runnable(){		//线程1，去获取cpu利用率
			@Override
			public void run() {
				resTable.put(0, cpuUsage.getResUsage());
			}
		});
		
		Thread memThread = new Thread(new Runnable(){		//线程2，去获取mem利用率
			@Override
			public void run() {
				resTable.put(1, memUsage.getResUsage());
			}
		});
		
		Thread netThread = new Thread(new Runnable(){		//线程3，去获取net利用率
			@Override
			public void run() {
				resTable.put(2, netUsage.getResUsage());
			}
		});
		
		try {
			cpuThread.start();		//先执行子线程的start()方法，再执行子线程的join方法
			memThread.start();
			netThread.start();

			cpuThread.join();		//主线程等子线程结束
			memThread.join();
			netThread.join();
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return resTable;
	}

}