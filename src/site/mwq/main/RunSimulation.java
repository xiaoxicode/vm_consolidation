package site.mwq.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

import site.mwq.compare.RIAL;
import site.mwq.compare.Sandpiper;
import site.mwq.gene.IndComp;
import site.mwq.gene.Pop;
import site.mwq.utils.FileUtils;
import site.mwq.utils.Utils;

public class RunSimulation {
	
	public ArrayList<Double> consTimes = new ArrayList<Double>();

	public TreeMap<Integer,ArrayList<double[]>> migCntMap = new TreeMap<Integer,ArrayList<double[]>>();
	
	
	/**运行sand对比程序*/
	public void runSand(){
		System.out.println("-------分割线------");
		Sandpiper sand = new Sandpiper(DataSet.hostVmMap);
		double[] sandRes = sand.moveVm();
		FileUtils.printSand(sandRes);
	}
	
	/**运行rial对比程序*/
	public void runRial(){
		System.out.println("-------分割线------");
		RIAL rial = new RIAL(DataSet.hostVmMap);
		double[] rialRes = rial.move();
		FileUtils.printRial(rialRes);
	}
	
	
	public void runProgram(){
		DcCase.initCloudData();
		DcCase.runGeneProgram();
		
		Collections.sort(Pop.inds,new IndComp());
		
		//打印结果矩阵
		int index = 0;
		double migCnt = Utils.mc.objVal(Pop.inds.get(index));
		ArrayList<double[]> matrix = new ArrayList<double[]>();

		while(migCnt==0){
			matrix.add(null);	//为保证matrix下标和Pop.inds的下标对应，在matrix中加入null值
			index++;
			migCnt = Utils.mc.objVal(Pop.inds.get(index));
		}
		
		double[] sum = new double[5];
		
		int end = index+12;
		
		for(;index<=end;index++){
			double[] resIndex = Utils.getIndVal(Pop.inds.get(index));   //获取一个个体，对于所有目标的值
			Utils.disIndVal(Pop.inds.get(index));

			for(int j=1;j<resIndex.length;j++){		//累加每行的和，这个和用于归一化
				sum[j] += resIndex[j];
			}
			matrix.add(resIndex);
		}
		
		//归一化，同时选择累加值的最小值
		double minVal = Double.MAX_VALUE;
		int minIndex = -1;
		double[] weights = Utils.genWeights();
		
		for(int i=0;i<matrix.size();i++){
			if(matrix.get(i)==null){continue;}
			
			double[] row = matrix.get(i);
			
			double curVal = 0;
			
			//权重设置，将结果按照迁移次数和时间排序(首先比较顺序，其次比较迁移时间)，
			//确定帕累托集大小，为不同的偏好设置权重
			//这里的偏好从弱到强为：物理机数目 < 平衡度 < 通信代价 
			for(int j=2;j<row.length;j++){	
				row[j] /= sum[j];
				row[j] *= weights[j];		
				curVal += row[j];
			}
			if(curVal<minVal){
				minVal = curVal;
				minIndex = i;
			}
		}
		
		System.out.println("-----ours-----");
		Utils.disIndVal(Pop.inds.get(0));
		FileUtils.printOri(Utils.getIndVal(Pop.inds.get(0)));
		Utils.disIndVal(Pop.inds.get(minIndex));
		FileUtils.printGene(Utils.getIndVal(Pop.inds.get(minIndex)));
		//System.out.println("consolidation time: "+Pop.inds.get(minIndex).consolidationTime);
		consTimes.add(Pop.inds.get(minIndex).consolidationTime);
	
		runRial();
		runSand();
	}
	
	
	public static void main(String[] args) {
		RunSimulation runSimulation = new RunSimulation();
		
		DcCase.initCloudData();
		runSimulation.stepOne(10);
		
		System.out.println("consolidation time:"+runSimulation.consTimes);
	}
	
	
	/**
	 * 进行迭代计算
	 * @param times 迭代次数
	 */
	public void stepOne(int times){
		for(int i=0;i<times;i++){
			long s1 = System.currentTimeMillis();
			runProgram();
			long s2 = System.currentTimeMillis();
			
			System.out.println(i+"::::::::::::::: "+(s2-s1)/1000);
		}
	}
	
	/**
	 * 只对一个映射进行不断优化，
	 * @param times 优化次数
	 */
	public void stepTwo(int times){
		
		for(int i=0;i<times;i++){
			DcCase.runGeneProgram();
			
			Collections.sort(Pop.inds,new IndComp());

			int index = 0;
			double migCnt = Utils.mc.objVal(Pop.inds.get(index));
			while(migCnt==0){ 
				index++;
				migCnt = Utils.mc.objVal(Pop.inds.get(index));
			}
			
			int end = index+15;

			for(;index<=end;index++){
				double[] resIndex = Utils.getIndVal(Pop.inds.get(index));
				Utils.disIndVal(Pop.inds.get(index));
				int migCntTemp = (int)resIndex[0];
				if(migCntMap.containsKey(migCntTemp)){
					migCntMap.get(migCntTemp).add(resIndex);
				}else{
					ArrayList<double[]> tempList = new ArrayList<double[]>();
					tempList.add(resIndex);
					migCntMap.put(migCntTemp, tempList);
				}
			}
			System.out.println("###### "+i+" #######");
			Pop.inds.clear();
			Pop.children.clear();
		}

		calAvgPrint();
	}
	
	public void calAvgPrint(){
		for(int migCnt:migCntMap.keySet()){			//对于同一个迁移次数
			ArrayList<double[]> data = migCntMap.get(migCnt);
			double[] avg = new double[5];
			for(int i=0;i<data.size();i++){			//这个迁移次数对应的多个记录
				double[] record = data.get(i);		//一条记录
				for(int j=0;j<record.length;j++){
					avg[j] += record[j];
				}
			}
			System.out.print(migCnt+": ");
			for(int j=0;j<5;j++){		//求平均值
				avg[j] /= data.size();
				System.out.print(Utils.to2(avg[j])+" ");
			}
			FileUtils.write2file("step20.txt", avg);

			System.out.println();
		}
	}
	
}
