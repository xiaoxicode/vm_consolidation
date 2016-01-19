package site.mwq.gene;

import java.util.ArrayList;

/**
 * 种群，解的集合
 * @author E-mail:qiuweimin@126.com
 * @version 创建时间：2015年12月22日 下午7:22:17
 */
public class Population {

	/**交叉概率*/
	public static double coProb = 0.85;
	/**变异概率*/
	public static double muProb = 0.08;
	
	/** 种群规模*/
	public static int N = 1000;
	
	/**个体集合，可能包含重复的解*/
	public ArrayList<Individual> inds = null;
	
	public Population(){
		inds = new ArrayList<Individual>();
	}

	/**
	 * 基因算法选择操作，锦标赛算法
	 * 随机选择两个个体，选择较好的那一个解
	 */
	public void select(){
		//TODO 选择操作
	}
	
	/**
	 * 基因算法交叉操作
	 */
	public void crossover(){
		//TODO 交叉操作
	}
	
	/**
	 * 基因算法变异操作
	 */
	public void mutation(){
		//TODO 变异操作
	}
}
