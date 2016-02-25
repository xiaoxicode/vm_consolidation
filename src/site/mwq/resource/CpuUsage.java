package site.mwq.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * 采集CPU使用率
 * @author Email:qiuweimin@126.com
 * @date 2016年2月19日
 */
public class CpuUsage {

	private static CpuUsage INSTANCE = new CpuUsage();
	
	public static CpuUsage getInstance(){
		return INSTANCE;
	}
	
	public String[] getCpuData(Runtime r){
		
		Process pro = null;
		String[] res = null;
		String command = "cat /proc/stat";
		
		try {
			pro = r.exec(command);		//执行shell命令
			BufferedReader in1 = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			String line = null;
			while((line=in1.readLine()) != null){	
				if(line.startsWith("cpu")){
					line = line.trim();
					res = line.split("\\s+"); 
					if(res[0].equals("cpu")){
						Print.printLine(line);
						break;
					}
				}						
			}	
			in1.close();
			pro.destroy();
		} catch (IOException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
		}	
		return res;
	}
	
	/**
	 * 采集CPU总时间，使用的CPU时间，下标0为CPU总时间，下标1为使用的CPU时间
	 * @param args
	 * @return double数组，长度为2
	 */
	public double[] getResUsage() {
		
		double[] cpuUsage = new double[2];
		long idleCpuTime1 = 0, totalCpuTime1 = 0;	//分别为系统启动后空闲的CPU时间和总的CPU时间
		long idleCpuTime2 = 0, totalCpuTime2 = 0;

		////
		Runtime runtime = Runtime.getRuntime();
		String[] data1 = getCpuData(runtime);			//第一次测数据
		
		for(int i=1;i<data1.length;i++){
			totalCpuTime1 += Long.parseLong(data1[i]);
		}
		idleCpuTime1 = Long.parseLong(data1[4]);
		
		try {
			Thread.sleep(1000);				/**等待时间**/
		} catch (InterruptedException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
		}
		
		String[] data2 = getCpuData(runtime);		//第二次测数据
		for(int i=1;i<data2.length;i++){
			totalCpuTime2 += Long.parseLong(data2[i]);
		}
		idleCpuTime2 = Long.parseLong(data2[4]);
		////
		
		cpuUsage[0] = totalCpuTime2 - totalCpuTime1;
		cpuUsage[1] = (totalCpuTime2 - totalCpuTime1) - (idleCpuTime2 - idleCpuTime1);
		
		return cpuUsage;
	}

	/**
	 * main方法，用于测试
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		while(true){
			System.out.println(CpuUsage.getInstance().getResUsage());
			Thread.sleep(5000);		
		}
	}
}