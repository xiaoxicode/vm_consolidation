package site.mwq.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class PrintData {
	
	public static String path = System.getProperty("user.dir");
	
	public static File geneFile = null;
	public static File sandFile = null;
	public static File rialFile = null;
	public static File oriFile = null;
	
	
	public static BufferedReader geneReader = null;
	public static BufferedReader sandReader = null;
	public static BufferedReader rialReader = null;
	public static BufferedReader oriReader = null;
	
	
	static{
		geneFile = new File(path+"/gene.txt");
		sandFile = new File(path+"/sand.txt");
		rialFile = new File(path+"/rial.txt");
		oriFile = new File(path+"/original.txt");
		
		try {
			geneReader = new BufferedReader(new FileReader(geneFile));
			sandReader = new BufferedReader(new FileReader(sandFile));
			rialReader = new BufferedReader(new FileReader(rialFile));
			oriReader = new BufferedReader(new FileReader(oriFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void readDatas(BufferedReader reader,String algo){
		ArrayList<Double> migCnts = new ArrayList<Double>();
		ArrayList<Double> pmCnts = new ArrayList<Double>();
		ArrayList<Double> comCosts = new ArrayList<Double>();
		ArrayList<Double> bans = new ArrayList<Double>();
		ArrayList<Double> migTimes = new ArrayList<Double>();
		
		//读数据
		try {
			String line = reader.readLine();
			
			while(line != null){
				String[] datas = line.split("\\s+");
				
				migCnts.add(Double.parseDouble(datas[0]));
				pmCnts.add(Double.parseDouble(datas[1]));
				comCosts.add(1.1859522E7-Double.parseDouble(datas[2]));
				bans.add(Double.parseDouble(datas[3]));
				migTimes.add(Double.parseDouble(datas[4]));
				
				line = reader.readLine();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Collections.sort(migCnts);
		Collections.sort(pmCnts);
		Collections.sort(comCosts);
		Collections.sort(bans);
		Collections.sort(migTimes);
		
		System.out.println("#########"+algo);
		
//		System.out.print("migCnt:"+avg(migCnts)+" "+stdDev(migCnts,avg(migCnts))+"  -  ");
//		System.out.print("pmCnts:"+avg(pmCnts)+" "+stdDev(pmCnts,avg(pmCnts))+"  -  ");
//		System.out.print("comCosts:"+avg(comCosts)+" "+stdDev(comCosts,avg(comCosts))+"  -  ");
//		System.out.print("bans:"+avg(bans)+" "+stdDev(bans,avg(bans))+"  -  ");
//		System.out.print("migTimes:"+avg(migTimes)+" "+stdDev(migTimes,avg(migTimes))+"  -  ");
		
		System.out.print("migCnt:"+avg(migCnts)+"  -  ");
		System.out.print("pmCnts:"+avg(pmCnts)+"  -  ");
		System.out.print("comCosts:"+avg(comCosts)+"  -  ");
		System.out.print("bans:"+avg(bans)+"  -  ");
		System.out.print("migTimes:"+avg(migTimes)+"  -  ");

//		printPercent(migCnts,"migCnt");
//		printPercent(pmCnts,"pmCnts");
//		printPercent(comCosts,"comCosts");
//		printPercent(bans,"bans");
//		printPercent(migTimes,"migTimes");
		System.out.println();
	}
	
	public static void printPercent(ArrayList<Double> datas,String meter){
		System.out.println("--------- "+meter+": ");
		System.out.print(nPercent(datas,1)+" ");
		System.out.print(nPercent(datas,5)+" ");
		System.out.print(nPercent(datas,9)+" "); 
		System.out.println();
	}
	
	/**
	 * 返回平均值
	 * @param datas
	 * @return
	 */
	public static double avg(ArrayList<Double> datas){
		double sum = 0;
		
		for(int i=0;i<datas.size();i++){
			sum += datas.get(i);
		}
		double avg = sum/datas.size();
		
		return ((double)(long)(avg)*10)/10;
	}
	
	/**
	 * 返回标准差
	 * @return
	 */
	public static double stdDev(ArrayList<Double> datas,double avg){
		
		double sum = 0;
		for(int i=0;i<datas.size();i++){
			sum += Math.pow(datas.get(i)-avg, 2);
		}
		
		sum /= datas.size();
		sum = Math.sqrt(sum);
		
		return ((double)(long)(sum)*10)/10;
	}
	
	/**
	 * 
	 * @param datas 以排序数据集
	 * @param n	 
	 * @return
	 */
	public static double nPercent(ArrayList<Double> datas,int n){
		
		int len = datas.size();
		int interval = len/10;	//均分10份
		
		int start = (n-1)*interval;
		int end = start + interval;
		double sum = 0;
		for(int i=start;i<end;i++){
			sum += datas.get(i);
		}
		return ((double)(long)(sum/interval)*10)/10;
	}
	
	public static void main(String[] args) {
		readDatas(geneReader,"gene");
		readDatas(sandReader,"sand");
		readDatas(rialReader,"rial");
		readDatas(oriReader,"ori");
	}

}
