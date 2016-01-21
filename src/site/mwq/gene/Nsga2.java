package site.mwq.gene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import site.mwq.targets.Objs;

/**
 * 实现NSGA2算法
 * @author E-mail:qiuweimin@126.com
 * @version 创建时间：2015年12月22日 下午8:00:30
 */
public class Nsga2 {

	/**元素按照dominant和拥挤距离排序
	 * 每一个ArrayList为一个rank，内部按照拥挤距离排序
	 * */
	public ArrayList<ArrayList<Individual>> indRanks = null;
	
	
	/**
	 * 计算种群中，每个个体对于每个目标的值，存储在个体的objVals数组中
	 * 此方法一定先于dominant方法调用
	 * @param pop
	 */
	public void calculateObj(){
		for(int i=0;i<Population.inds.size();i++){
			
			Individual ind = Population.inds.get(i);
			for(int j=0;j<ind.objVals.length;j++){	//对ind调用每个目标函数
				ind.objVals[j] = Objs.OBJS[j].objValue(ind);
			}
		}
	}
	
	/**
	 * Nsga2排序，论文中的算法实现
	 * @param pop 种群
	 * @return HashMap的key为rank，value为rank对应的Individual集合
	 */
	public static ArrayList<ArrayList<Individual>> fastNonDominateSort(ArrayList<Individual> inds){
	
		ArrayList<ArrayList<Individual>> rankedInds = new ArrayList<ArrayList<Individual>>();
		
		ArrayList<Individual> sameRankInds = new ArrayList<Individual>();
		
		for(int i=0;i<inds.size();i++){
			Individual ind_p = inds.get(i);
			ind_p.nsgaDoms = new ArrayList<Individual>();
			ind_p.nsgaNp = 0;
			
			for(int j=0;j<inds.size();j++){
				Individual ind_q = inds.get(j);
				
				int dominate = ind_p.dominate(ind_q);
				if(dominate==1){			//p dominate q，将q加入p的主导集合
					ind_p.nsgaDoms.add(ind_q);
				}else if(dominate==-1){		// q dominate p,暂时不将p加入q的主导集合集合，只是增加p的rank
					ind_p.nsgaNp += 1;
				}
			}
			
			if(ind_p.nsgaNp==0){
				ind_p.nsgaRank = 1;
				sameRankInds.add(ind_p);
			}
		}
		rankedInds.add(sameRankInds);
		
		ArrayList<Individual> sameRanks = sameRankInds;		//TODO 引用类型直接赋值，排错
		int i=1;
		while(!sameRanks.isEmpty()){						//while循环条件，第i层集合不为空
			ArrayList<Individual> i1ranks = new ArrayList<Individual>();
			
			for(int j=0;j<sameRanks.size();j++){			//对第i层集合中每个个体
				
				ArrayList<Individual> doms = sameRanks.get(j).nsgaDoms;
				for(Individual ind:doms){
					ind.nsgaNp -= 1;
					if(ind.nsgaNp==0){
						ind.nsgaRank = i+1;
						i1ranks.add(ind);
					}
				}
			}
			i += 1;
			rankedInds.add(i1ranks);
			sameRanks = i1ranks;
		}
		
		return rankedInds;
	}
	
	/**
	 * 计算同一个Rank中每个元素的拥挤距离
	 * @param inds
	 */
	public void crowingDistanceAssign(ArrayList<Individual> inds){
		int len = inds.size();
		for(int i=0;i<len;i++){
			inds.get(i).nsgaCrowDis = 0;
		}
		
		for(int i=0;i<Objs.OBJNUM;i++){
			
			Collections.sort(inds,new ObjectComparator(i));		//TODO 验证排序，根据特定目标值对总个体升序排序
			
			double objImin =inds.get(0).objVals[i];		 	//目标的最小值
			double objImax = inds.get(len-1).objVals[i];  	//目标的最大值
			
			inds.get(0).nsgaCrowDis = Double.MAX_VALUE;
			inds.get(len-1).nsgaCrowDis = Double.MAX_VALUE;
			
			double denominator = objImax-objImin;			//分母
			for(int j=1;j<len-1;j++){						//因为是升序排列，所以是距离是正值
				inds.get(j).nsgaCrowDis += (inds.get(j+1).objVals[i]-inds.get(j-1).objVals[i])/denominator;
			}
		}
	}
	
	/**
	 * nsga2的主算法，从父代和子代中选出大小为父代的种群
	 * @param parent	父代
	 * @param children	子代
	 * @return	下一代
	 */
	public ArrayList<Individual> nsgaMain(ArrayList<Individual> parent,ArrayList<Individual> children){
		
		ArrayList<Individual> nextGeneration = new ArrayList<Individual>();
		
		parent.addAll(children);				//合并为一个集合
		ArrayList<ArrayList<Individual>> rankedInds = fastNonDominateSort(parent);
		
		int i=0;
		
		//首先合并低Rank的解集合
		while(nextGeneration.size()+rankedInds.get(i).size()<Population.N){
			nextGeneration.addAll(rankedInds.get(i));
			i++;
		}
		
		crowingDistanceAssign(rankedInds.get(i));
		Collections.sort(rankedInds.get(i),new CrowdComp());
		
		int j=0;
		while(nextGeneration.size()<Population.N){
			nextGeneration.add(rankedInds.get(i).get(j));
			j++;
		}
		
		return nextGeneration;
	}
	
}


/**
 * 按照Individual的rank升序排列，然后安装拥挤距离降序排列
 * 
 * @author E-mail:qiuweimin@126.com
 * @version 创建时间：2015年12月24日 下午5:12:58
 */
class CrowdComp implements Comparator<Individual> {

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