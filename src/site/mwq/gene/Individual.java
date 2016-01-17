package site.mwq.gene;

import java.util.ArrayList;

import site.mwq.model.Host;
import site.mwq.objects.Objs;

/**
 *基因算法中的个体，也即多目标优化算法的一个解 
 *也即一个染色体，
 *
 *Individual的equals方法：host编号相同，且host所包含的虚拟机相同
 *
 */
public class Individual {
	
	/**这个解的染色体*/
	public ArrayList<Host> hosts = null;
	
	/** nsga2算法中的Np，即支配这个解的解的个数*/
	public int nsgaNp;
	
	/** nsga2算法中的Rank*/
	public int nsgaRank;
	
	/**nsga2算法中，这个解所支配的解的集合*/
	public ArrayList<Individual> nsgaDoms; 
	
	/**nsga2算法中的拥挤距离*/
	public double nsgaCrowDis;
	
	/**此个体对各个优化目标的值，顺序按照Objs中给出的顺序*/
	public double[] objVals = new double[Objs.OBJNUM];
	
	public Individual(){
		this.hosts = new ArrayList<Host>();
	}
	
	/**
	 * 判断一个解是否支配(dominate)另一个解
	 * @param ind
	 * @return 1：支配, 0:无法比较，-1：参数支配调用者
	 */
	public int dominate(Individual ind){
		
		int greatNum = 0,lessNum = 0;
		for(int i=0;i<Objs.OBJNUM;i++){
			if(this.objVals[i]<ind.objVals[i]){
				lessNum++;
			}else if(this.objVals[i]>objVals[i]){
				greatNum++;
			}
		}
		//5个目标都比ind小
		if(lessNum==Objs.OBJNUM){
			return 1;
		}
		//5个目标都比ind大
		if(greatNum==Objs.OBJNUM){
			return -1;
		}
		return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hosts == null) ? 0 : hosts.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Individual other = (Individual) obj;
		if (hosts == null) {
			if (other.hosts != null)
				return false;
		} else if (!hosts.equals(other.hosts))
			return false;
		return true;
	}

}
