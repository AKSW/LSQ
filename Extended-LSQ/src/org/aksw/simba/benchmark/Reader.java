package org.aksw.simba.benchmark;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.openrdf.query.MalformedQueryException;
/**
 * This is just a dummy class we used for reading various files 
 * @author Saleem
 *
 */
@SuppressWarnings("unused")
public class Reader {

	public static void main(String[] args) throws IOException, MalformedQueryException {
		String dirStr = "D:/QueryLogs/SWDF/";
		File dir = new File(dirStr);
		File[] listOfQueryLogs = dir.listFiles();
		int count = 0;
	 	BufferedWriter bw= new BufferedWriter(new FileWriter("D:/QueryLogs/SWDF-Test/swdf-test.txt"));
		for (File queryLogFile : listOfQueryLogs)
		{
			System.out.println(queryLogFile.getName()+ ": in progress...");
			
			File catalinaOut = new File(System.getProperty("catalina.base"), queryLogFile.getName());
			
			BufferedReader br = new BufferedReader(new FileReader(dirStr+catalinaOut));
			String line;
			while ((line = br.readLine()) != null   )
			{
				if(count > 2026180)
			//	{
			//		System.out.println(line);
			//	}
				 bw.write(line+"\n");
			//	System.out.println(line);
				count++;
			}
			System.out.println(count);
		   //bw.write(line+"\n");
	 	br.close();
		}
		
		bw.close();
	}
}