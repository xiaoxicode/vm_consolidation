package site.mwq.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Temp {

	public static String path = System.getProperty("user.dir");
	
	public static File file = null;
	
	public static BufferedReader reader = null;
	
	static{
		file = new File(path+"/temp.txt");
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	public void handle(){
		ArrayList<Double>  migCnts = new ArrayList<Double>();
		ArrayList<Double>  pmCnts = new ArrayList<Double>();
		ArrayList<Double>  bans = new ArrayList<Double>();
		ArrayList<Double>  migTimes = new ArrayList<Double>();

		try {
			String line = reader.readLine();
			
			while(line!=null){
				String[] datas = line.split("\\s+");
				migCnts.add(Double.parseDouble(datas[0].split(":")[1]));
				pmCnts.add(Double.parseDouble(datas[1].split(":")[1]));
				bans.add(Double.parseDouble(datas[3].split(":")[1]));
				migTimes.add(Double.parseDouble(datas[4].split(":")[1]));
				
				line = reader.readLine();
			}
			
			System.out.println(migCnts);
			System.out.println(pmCnts);
			System.out.println(bans);
			System.out.println(migTimes);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	public static void main(String[] args) {
		Temp t = new Temp();
		t.handle();
	}

}
