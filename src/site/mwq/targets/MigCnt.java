package site.mwq.targets;

import site.mwq.gene.Individual;
import site.mwq.main.DataSet;

/**
 *  总的需要迁移的VM数
 * @author E-mail:qiuweimin@126.com
 * @version 创建时间：2015年12月23日 上午11:53:40
 */
public class MigCnt implements ObjInterface {

	@Override
	public double objValue(Individual ind) {	//通过比较这个解与原始解得出迁移次数
		int cnt = 0;
		
		for(int i: ind.vmHostMap.keySet()){
			if(ind.vmHostMap.get(i) != DataSet.vmHostMap.get(i)){
				cnt++;
			}
		}
		
		return cnt;
	}

}
