package site.mwq.targets;

import site.mwq.gene.Individual;
import site.mwq.utils.Utils;

/**
 * 衡量负载的均衡程度
 * @author E-mail:qiuweimin@126.com
 * @version 创建时间：2015年12月23日 上午11:53:54
 */
public class Balance implements ObjInterface {

	
	/**
	 * 在实现的时候说明是Balance目标
	 */
	@Override
	public double objVal(Individual ind) {
		double balance = 0;
		
		double avgMem = Utils.getAvgMem(ind);
		double avgCpu = Utils.getAvgCpu(ind);
		
		double cpuDiff = 0;
		double memDiff = 0;
		
		for(int i=0;i<ind.indHosts.size();i++){
			
			if(ind.hostVmMap.get(i).size()==0){
				continue;
			}
			
			cpuDiff = ind.indHosts.get(i).getCpuRate()-avgCpu;
			memDiff = ind.indHosts.get(i).getMemRate()-avgMem;
			
			balance += Math.sqrt(cpuDiff*cpuDiff+memDiff*memDiff);
		}
		
		return ((int)(balance*10))/10.0;
	}


}
