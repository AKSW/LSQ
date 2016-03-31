package org.aksw.simba.benchmark.log.operations;

import java.util.HashMap;
import java.util.Map;

public class DateConverter {
	public static String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
	public static Map<String,String> MONTHS_TO_INDEX = new HashMap<String,String>();
	
	static{
		for(int i=0; i<MONTHS.length; i++){
			String index = Integer.toString(i+1);
			if(i<9){
				index = "0"+index;
			}
			MONTHS_TO_INDEX.put(MONTHS[i], index);
		}
	}
	
	public static String convertDate(String bad) throws DateParseException{
		if(bad.length()!=25){
			throw new DateParseException("Expected input of length 26, not "+bad+" ("+bad.length()+")");
		}
		
		String date = bad.substring(0,2);
		String month = bad.substring(3,6);
		String year = bad.substring(7,11);
		String time = bad.substring(12,20);
		String tzSignAndHour = bad.substring(20,23);
		String tzMinute = bad.substring(23,25);

		String monthId = MONTHS_TO_INDEX.get(month);
		if(monthId==null){
			throw new DateParseException("Unrecognised month "+month);
		}
		
		return year+"-"+monthId+"-"+date+"T"+time+tzSignAndHour+":"+tzMinute;
	}
	
	public static void main(String[] args) throws DateParseException{
		System.err.println(convertDate("17/Sep/2014:21:58:41+0100"));
	}
	
	public static class DateParseException extends Exception{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		;
		
		DateParseException(String msg){
			super(msg);
		}
	}
}