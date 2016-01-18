package site.mwq.targets;

import site.mwq.gene.Individual;

/**
 * 给出种群的一个个体（多目标优化一个解）所使用的物理机数量
 * @author E-mail:qiuweimin@126.com
 * @version 创建时间：2015年12月23日 上午11:53:03
 */
public class PmNum implements ObjInterface {

	@Override
	public double objValue(Individual ind) {
		return 0.5;
	}

}
