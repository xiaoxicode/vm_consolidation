package site.mwq.rmi;

import java.util.Hashtable;

public class PM {
	public String name;
	public String ip;
	
	public double volume;
	
	private final double resThreshold = 0.55;			//阈值，设置为0.55
	public boolean isOverLoaded = false;
	
	/**下标从0到3分别表示cpu/mem/net的资源总量，下同*/
	public double[] resTotal = new double[3];
	
	/**负载*/
	public double[] resUsed = new double[3];
	
	/**
	 * 构造函数，初始化数据
	 * @param ip
	 * @param resUsage
	 */
	public PM(String ip,Hashtable<Integer,double[]> resUsage){
		this.ip = ip;
		this.name = AddressMap.pmIpName.get(ip);
		
		volume = 1;
		
		for(int i=0;i<3;i++){
			resTotal[i] = resUsage.get(i)[0];
			resUsed[i] = resUsage.get(i)[1];
			
			double ratei = resUsed[i]/resTotal[i];
			if(ratei>resThreshold){
				isOverLoaded = true;
			}
			
			volume *= 1/(1-ratei);
		}
	}
	
	/**
	 * 检查是否有资源超过利用率
	 * @return
	 */
	public boolean isOverLoaded(){
		for(int i=0;i<3;i++){
			
			double ratei = resUsed[i]/resTotal[i];
			if(ratei>resThreshold){
				isOverLoaded = true;
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 判断一个pm是否能接收一台虚拟机且不超过各个资源使用率阈值
	 * 
	 * @return
	 */
	public boolean canHold(VM vm){
		
		for(int i=0;i<resUsed.length;i++){
			if((resUsed[i]+vm.resUsed[i])/resTotal[i]>resThreshold){
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 移除（迁移）一台vm，减掉其使用的资源量
	 * @param vm
	 */
	public void removeVm(VM vm){
		
		for(int i=0;i<resUsed.length;i++){
			resUsed[i] -= vm.resUsed[i];
		}
	}
	
}
