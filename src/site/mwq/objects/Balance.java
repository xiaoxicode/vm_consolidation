package site.mwq.objects;

import site.mwq.gene.Individual;

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
		// TODO Auto-generated method stub
		return 0.2;
	}

}
