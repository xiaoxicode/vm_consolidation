package site.mwq.rmi;

import java.util.Hashtable;

public class VM {

	public String name;
	public String ip;
	
	public double vsr;
	public double volume;
	
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
			volume *= 1/(1-ratei);
		}
		
		vsr = volume/resUsed[2];
	}
	
}
