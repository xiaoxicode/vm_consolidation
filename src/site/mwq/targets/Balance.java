package site.mwq.targets;

import site.mwq.gene.Individual;
import site.mwq.main.DataSet;

/**
 * 衡量负载的均衡程度
 * @author E-mail:qiuweimin@126.com
 * @version 创建时间：2015年12月23日 上午11:53:54
 */
public class Balance implements ObjInterface {

	
	/**
	 * 在实现的时候说明是Balance目标，用于验证程序正确性
	 */
	@Override
	public double objValue(Individual ind) {
		double balance = 0;
		
		double avgMem = getAvgMem(ind);
		double avgCpu = getAvgPe(ind);
		
		for(int i=0;i<ind.hostInds.size();i++){
			balance += Math.sqrt(Math.pow(ind.hostInds.get(i).getPeRate()-avgCpu, 2)+
					Math.pow(ind.hostInds.get(i).getMemRate()-avgMem, 2));
		}
		
		return balance;
	}

	/**
	 * 平均内存利用率
	 * @return
	 */
	private double getAvgMem(Individual ind) {
		double res = 0;
		for(int i=0;i<DataSet.hosts.size();i++){
			res += DataSet.hosts.get(i).getMemRate();
		}
		res /= DataSet.hosts.size();
		return res;
	}

	/**
	 * 获得平均CPU利用率
	 * @return
	 */
	private double getAvgPe(Individual ind) {
		double res = 0;
		for(int i=0;i<ind.hostInds.size();i++){
			res += ind.hostInds.get(i).getPeRate();
		}
		res /= ind.hostInds.size();
		return res;
	}

}