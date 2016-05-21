package site.mwq.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Step2 {

	public String path = System.getProperty("user.dir");
	
	File file = null;
	BufferedReader reader = null;

	public void printFirst20(){
		file = new File(path+"/step20.txt");
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			
			ArrayList<Integer> migCnt = new ArrayList<Integer>();
			ArrayList<Double> migTime = new ArrayList<Double>();
			ArrayList<Integer> pmCnt = new ArrayList<Integer>();
			ArrayList<Double> ban = new ArrayList<Double>();
			ArrayList<Integer> comCost = new ArrayList<Integer>();

			
			for(int i=0;i<30&&line!=null;i++){
				
				String[] datas = line.split("\\s+");
				migCnt.add((int)Double.parseDouble(datas[1]));
				migTime.add(Double.parseDouble(datas[2]));
				pmCnt.add((int)Double.parseDouble(datas[3]));
				ban.add(Double.parseDouble(datas[4]));
				comCost.add((int)Double.parseDouble(datas[5]));
				
				line = reader.readLine();
			}
			
			System.out.println("migCnt: "+migCnt);
			System.out.println("migTime: "+migTime);
			System.out.println("pmCnt: "+pmCnt);
			System.out.println("ban: "+ban);
			System.out.println("comCost: "+comCost);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		Step2 step2 = new Step2();
		step2.printFirst20();
	}

}
