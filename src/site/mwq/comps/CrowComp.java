package site.mwq.comps;

import java.util.Comparator;

import site.mwq.gene.Individual;

/**
 * 按照Individual的rank升序排列，然后安装拥挤距离降序排列
 * 
 * @author E-mail:qiuweimin@126.com
 * @version 创建时间：2015年12月24日 下午5:12:58
 */
public class CrowComp implements Comparator<Individual> {

	@Override
	public int compare(Individual o1, Individual o2) {
		
		if(o1.nsgaRank < o2.nsgaRank){				//rank升序
			return -1;
		}else if(o1.nsgaRank > o2.nsgaRank){
			return 1;
		}else{
			if(o1.nsgaCrowDis > o2.nsgaCrowDis){	//拥挤距离 降序
				return -1;
			}else if(o1.nsgaCrowDis < o2.nsgaCrowDis){
				return 1;
			}
		}
		
		return 0;
	}

}
