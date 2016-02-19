package site.mwq.rmi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * 虚拟机迁移类，执行java程序是需要添加sudo
 * @author Email:qiuweimin@126.com
 * @date 2016年2月19日
 */
public class LibvirtSim {

	public static void migrate(String sourcePm,String destPm,String vm){
		String cmd = "python mig.py "+sourcePm+" "+destPm+" "+vm;
		
		Runtime r = Runtime.getRuntime();
		Process pro = null;
		
		try { 
			pro = r.exec(cmd);		
			pro.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 返回
	 * @return
	 */
	public static ArrayList<String> virshList(){
		ArrayList<String> vmNames = new ArrayList<String>();
		String cmd = "virsh list";
		
		Runtime r = Runtime.getRuntime();
		Process pro = null;
		
		try { 
			pro = r.exec(cmd);		//执行shell命令 java执行需要root权限的shell命令，用sudo java Test
			BufferedReader in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			String line = "";
			in.readLine();	//忽略前两行
			in.readLine();
			while((line=in.readLine()) != null){
				if(line.trim().equals("")){
					continue;
				}
				System.out.println(line);
				String[] strs = line.trim().split("\\s+");	//一定要先trim()再split()
				vmNames.add(strs[1]);
			}	
			in.close();
			pro.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(vmNames);
		return vmNames;
	}
	
	public static void main(String[] args) {
		migrate("pm1", "pm2", "vm1");
	}
}
