package site.mwq.main;

import java.util.ArrayList;
import java.util.Collections;

import site.mwq.compare.RIAL;
import site.mwq.compare.Sandpiper;
import site.mwq.gene.IndComp;
import site.mwq.gene.Pop;
import site.mwq.utils.FileUtils;
import site.mwq.utils.Utils;

public class RunSimulation {
	
	public ArrayList<Double> consTimes = new ArrayList<Double>();

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
		
		int end = index+15;
		
		for(;index<=end;index++){
			double[] resIndex = Utils.getIndVal(Pop.inds.get(index));
			Utils.disIndVal(Pop.inds.get(index));

			for(int j=0;j<resIndex.length;j++){
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
			for(int j=1;j<row.length;j++){	
				row[j] /= sum[j];
				row[j] *= weights[j];     			//将各项权重累加
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
		
		for(int i=0;i<10;i++){
			long s1 = System.currentTimeMillis();
			runSimulation.runProgram();
			long s2 = System.currentTimeMillis();
			
			System.out.println(i+": "+(s2-s1)/1000);
		}
		
		System.out.println("consolidation time:"+runSimulation.consTimes);
	}

}
