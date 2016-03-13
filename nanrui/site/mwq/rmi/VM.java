package site.mwq.rmi;

import java.util.Hashtable;

public class VM {

	public String name;
	public String ip;
	
	public double vsr;
	public double volume;
	
	/**是否处于负载过高状态*/
	public boolean isOverLoaded = false;
	
	/**虚拟机的资源使用阈值，用来对虚拟机进行resize操作*/
	private final double resThreshold = 0.8;			//阈值，设置为0.55
	
	/**下标从0到3分别表示cpu/mem/net的资源总量，下同*/
	public double[] resTotal = new double[3];
	
	/**负载*/
	public double[] resUsed = new double[3];
	
	public VM(String vmIp,String vmName,Hashtable<Integer, double[]> vmResUsage) {

		this.ip = vmIp;
		this.name = vmName;
		
		volume = 1;
		
		for(int i=0;i<vmResUsage.size();i++){
			resTotal[i] = vmResUsage.get(i)[0];
			resUsed[i] = vmResUsage.get(i)[1];
		
			double ratei = resUsed[i]/resTotal[i];
			
//			if(i==0){
//				System.out.println(ratei);
//			}
			if(ratei>resThreshold){
				this.isOverLoaded = true;
			}
			
			volume *= 1/(1-ratei);
		}
		
		vsr = volume/resUsed[2];
	}
	
}
