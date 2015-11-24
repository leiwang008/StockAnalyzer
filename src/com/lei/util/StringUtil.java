package com.lei.util;
//Add in master
//Add in master
//Add in test-new
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
//Add in test-new
import org.safs.SAFSException;
import org.safs.StringUtils;

public class StringUtil extends StringUtils{
	public static final String DATE_PATTERN_1					= "MMM dd, h:mma z";
	public static final String TIME_PATTERN_1					= "h:mma z";
	public static final String TIME_PATTERN_2					= "h:mm";
	//test-new
	//add in test-new
	/**
	 * Remove the prefix from the original string, and return the result
	 * <p>Example:<p>
	 * original = "^filename=c:\file.txt" , prefix="^filename="
	 * the result will be "c:\file.txt"
	 * @param original
	 * @param prefix
	 * @return
	 */
	public static String removePrefix(String original,String prefix){
		if(original==null) return "";
		if(prefix==null) return original;
		if(original.equals(prefix)) return "";
		
		if(original.length()>prefix.length()){
			if(original.startsWith(prefix)){
				original = original.substring(prefix.length());
			}
		}
		return original;
	}

	/**
	 * Remove the suffix from the original string, and return the result
	 * <p>Example:<p>
	 * original = "^filename=c:\file.txt" , suffix="=c:\file.txt"
	 * the result will be "^filename"
	 * @param original
	 * @param suffix
	 * @return
	 */
	public static String removeSuffix(String original,String suffix){
		if(original==null) return "";
		if(suffix==null) return original;
		if(original.equals(suffix)) return "";
		
		if(original.length()>suffix.length()){
			if(original.endsWith(suffix)){				
				int l = original.length() - suffix.length();
				original = original.substring(0, l);
			}
		}
		return original;
	}

	/**
	 * Remove any leading and\or trailing double quotes.  
	 * <p>Example:<p>
	 * original = "c:\file.txt" (with quotes)
	 * the result will be 'c:\file.txt'  (no quotes at all)
	 * @param original
	 * @return
	 */
	public static String removeDoubleQuotes(String original){
		original = removePrefix(original, "\"");
		original = removeSuffix(original, "\"");
		return original;
	}
	
	/**
	 * @param value				A string represents an integer value.
	 * @return					An Integer converted from string
	 * @throws SAFSException
	 */
	public static Integer convertToInteger(String value){
		Integer result = null;
		
		try{
			result = Integer.parseInt(value);
		}catch (NumberFormatException e) {
		}
		
		return result;
	}

	/**
	 * @param value				A string represents an integer value.
	 * @return					An int converted from string
	 * @throws SAFSException
	 */
	public static int convertToPrimitiveInt(String value){
		int result = 0;
		Double intValue = convertToDouble(value);
		
		if(intValue!=null) result = intValue.intValue();
		
		return result;
	}
	
	/**
	 * @param value				A string represents an integer value.
	 * @return					A Double converted from string
	 * @throws SAFSException
	 */
	public static Double convertToDouble(String value){
		Double result = null;
		
		try{
			result = Double.parseDouble(value);
		}catch (NumberFormatException e) {
		}
		
		return result;
	}
	
	/**
	 * @param value				A string represents an integer value.
	 * @return					A double converted from string
	 * @throws SAFSException
	 */
	public static double convertToPrimitiveDouble(String value){
		double result = 0.0;
		Double doubleValue = convertToDouble(value);
		
		if(doubleValue!=null) result = doubleValue.doubleValue();
		
		return result;
	}
	
	public static Date convertStringToDate(String dateString, String pattern){
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		Date result = null;
		try {
			result = format.parse(dateString);
		} catch (ParseException e) {
		}
		
		return result;
	}
	
	public static String convertDateToString(Date date, String pattern){
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		String result = format.format(date);
		
		return result;
	}
	
	public static String convertToPercentage(double percentage){
		return getFormattedDoubleString(percentage)+"%";
	}
	
	public static String getFormattedDoubleString(double value){
		NumberFormat format = NumberFormat.getInstance();
		format.setMaximumFractionDigits(2);
		format.setMinimumFractionDigits(2);
		return format.format(value);
	}
}
