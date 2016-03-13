package site.mwq.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 采集内存使用率
 * @author Email:qiuweimin@126.com
 * @date 2016年2月19日
 */
public class MemUsage{

	private static MemUsage INSTANCE = new MemUsage();
	
	public static MemUsage getInstance(){
		return INSTANCE;
	}
	
	/**
	 * 采集内存使用
	 * @param args
	 * @return double[0]总内存，double[1]内存使用量
	 */
	public double[] getResUsage() {
		double[] memUsage = new double[2];
		
		Process pro = null;
		Runtime r = Runtime.getRuntime();
		String command = "cat /proc/meminfo";

		try {
			pro = r.exec(command);
			BufferedReader in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			String line = null;
			int count = 0;
			long totalMem = 0, freeMem = 0;
 			while((line=in.readLine()) != null){	
				String[] memInfo = line.split("\\s+");
				if(memInfo[0].startsWith("MemTotal")){
					totalMem = Long.parseLong(memInfo[1]);
				}
				if(memInfo[0].startsWith("MemFree")){
					freeMem = Long.parseLong(memInfo[1]);
				}
				memUsage[0] = totalMem;
				memUsage[1] = totalMem-freeMem;
				if(++count == 2){
					break;
				}				
			}
			in.close();
			pro.destroy();
		} catch (IOException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
		}	
		return memUsage;
	}
	
	/**
	 * main方法，用于测试
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		while(true){
			System.out.println(MemUsage.getInstance().getResUsage());
			Thread.sleep(5000);
		}
	}
}