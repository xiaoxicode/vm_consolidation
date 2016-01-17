package site.comps;

import gene.Individual;

import java.util.Comparator;

import objects.ObjInterface;

/**
 * 适用于各个目标函数的比较器，构造函数指明使用的目标
 * 按目标函数的值升序排列
 * @author E-mail:qiuweimin@126.com
 * @version 创建时间：2015年12月23日 下午1:05:41
 */
public class ObjComp implements Comparator<Individual>{

	public ObjInterface obj = null;
	
	public int objIndex;
	
	public ObjComp(ObjInterface obj){
		this.obj = obj;
		System.out.println("比较器构造函数被调用");
	}
	
	public ObjComp(int i){
		this.objIndex = i;
	}
	
	/**
	 * 升序排列，构造函数传入要比较的目标的id
	 */
	@Override
	public int compare(Individual o1, Individual o2) {
		
		double o1v = o1.objVals[objIndex];
		double o2v = o2.objVals[objIndex];
		
		if(o1v>o2v){
			return 1;
		}else if(o1v<o2v){
			return -1;
		}
		return 0;
	}

}
