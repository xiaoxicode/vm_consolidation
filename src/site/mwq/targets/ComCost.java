package site.mwq.targets;

import site.mwq.gene.Individual;

/**
 * 给出一个解的通信代价，这个好像不好给啊
 * @author Email:qiuweimin@126.com
 * @version 创建时间：2015年12月23日 上午11:53:16
 */
public class ComCost implements ObjInterface {

	@Override
	public double objValue(Individual ind) {
		
		return 0.1;
	}

}
