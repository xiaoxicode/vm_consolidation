package site.mwq.resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

/**
 * 往文件中写入日志
 * @author Email:qiuweimin@126.com
 * @date 2016年2月19日
 */
public class Print {
	
	public static File file = null;
	public static PrintWriter pw = null;
	
	static{
		String path = System.getProperty("user.dir");
		file = new File(path+"/log.txt");
		
		try {
			pw = new PrintWriter(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void printLine(String str){
		pw.println(str);
		pw.flush();
	}
	
	
	public static void main(String[] args) {
		printLine("test");
	}
}
