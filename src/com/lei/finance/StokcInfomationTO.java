package com.lei.finance;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.lei.util.StringUtil;

public class StokcInfomationTO {
	private String stockID;
	private double lastPrice;
	private double priceChange;
	private double priceChangePercentage;
	private Date   lastDate;


	private Map<String,String> tokensMap;//This map will contains pair like (t,600900) (l,6.69)

	private static final String BEGIN_TOKEN_1 = "[ {";
	private static final String BEGIN_TOKEN_2 = "[{";
	private static final String END_TOKEN_1 = "} ]";
	private static final String END_TOKEN_2 = "}]";
	private static final String COMMA_SEPARATOR = ",";
	private static final String COLON_SEPARATOR = ":";

	private static final String KEY_STOCK_ID					= "t";
	private static final String KEY_STOCK_MARKET				= "e";
	private static final String KEY_STOCK_LAST_PRICE			= "l";
	private static final String KEY_STOCK_LAST_TIME				= "ltt";
	private static final String KEY_STOCK_LAST_DATE				= "lt";
	private static final String KEY_STOCK_LAST_CHAGE			= "c";
	private static final String KEY_STOCK_LAST_CHAGE_PERCENT	= "cp";

	public StokcInfomationTO(){
		tokensMap = new HashMap<String,String>();
	}

	//The following string is the answer of request http://www.google.com/finance/info?client=ig&q=600050
	// // [ { "id": "697061" ,"t" : "600050" ,"e" : "SHA" ,"l" : "6.69" ,"l_cur" : "CN¥6.69" ,"ltt":"1:10PM CST" ,"lt" : "Nov 18, 1:10PM CST" ,"c" : "+0.11" ,"cp" : "1.67" ,"ccol" : "chg" } ]
	//we should analyze this string to get the useful info
	public StokcInfomationTO(String stockInfo){
		this();
		//remove the starting and ending tokens from the stock infomation string.
		int beginIndex = -1;
		int endIndex = -1;
		if(stockInfo.indexOf(BEGIN_TOKEN_1)>-1){
			beginIndex = stockInfo.indexOf(BEGIN_TOKEN_1)+BEGIN_TOKEN_1.length();
		}else if(stockInfo.indexOf(BEGIN_TOKEN_2)>-1){
			beginIndex = stockInfo.indexOf(BEGIN_TOKEN_2)+BEGIN_TOKEN_2.length();
		}
		if(stockInfo.indexOf(END_TOKEN_1)>-1){
			endIndex = stockInfo.indexOf(END_TOKEN_1);
		}else if(stockInfo.indexOf(END_TOKEN_2)>-1){
			endIndex = stockInfo.indexOf(END_TOKEN_2);
		}

		stockInfo = stockInfo.substring(beginIndex, endIndex);
		StringTokenizer st = new StringTokenizer(stockInfo,COMMA_SEPARATOR);
		while(st.hasMoreElements()){
			String field = st.nextToken();//field is a string like "id": "697061"
			int colonPosition = field.indexOf(COLON_SEPARATOR);
			String key = StringUtil.removeDoubleQuotes(field.substring(0,colonPosition).trim());
			String value = StringUtil.removeDoubleQuotes(field.substring(colonPosition+COLON_SEPARATOR.length()).trim());
//			System.out.println(key+" : "+value);
			tokensMap.put(key, value);
		}

		this.stockID = tokensMap.get(KEY_STOCK_ID);
		this.lastPrice = StringUtil.convertToPrimitiveDouble(tokensMap.get(KEY_STOCK_LAST_PRICE));
		this.priceChange = StringUtil.convertToPrimitiveDouble(tokensMap.get(KEY_STOCK_LAST_CHAGE));
		this.priceChangePercentage = StringUtil.convertToPrimitiveDouble(tokensMap.get(KEY_STOCK_LAST_CHAGE_PERCENT));
		this.lastDate	= StringUtil.convertStringToDate(tokensMap.get(KEY_STOCK_LAST_TIME), StringUtil.TIME_PATTERN_1);
	}

	/**
	 * @param lastDate
	 * @param lastPrice
	 * @param priceChange
	 * @param stockID
	 */
	public StokcInfomationTO(Date lastDate, double lastPrice, double priceChange, String stockID) {
		this();
		this.lastDate = lastDate;
		this.lastPrice = lastPrice;
		this.priceChange = priceChange;
		this.stockID = stockID;
	}

	/**
	 * @return the stockID
	 */
	public String getStockID() {
		return stockID;
	}

	/**
	 * @param stockID the stockID to set
	 */
	public void setStockID(String stockID) {
		this.stockID = stockID;
	}
	/**
	 * @return the lastPrice
	 */
	public double getLastPrice() {
		return lastPrice;
	}
	/**
	 * @param lastPrice the lastPrice to set
	 */
	public void setLastPrice(double lastPrice) {
		this.lastPrice = lastPrice;
	}
	/**
	 * @return the priceChange
	 */
	public double getPriceChange() {
		return priceChange;
	}
	/**
	 * @param priceChange the priceChange to set
	 */
	public void setPriceChange(double priceChange) {
		this.priceChange = priceChange;
	}
	/**
	 * @return the lastDate
	 */
	public Date getLastDate() {
		return lastDate;
	}
	/**
	 * @param lastDate the lastDate to set
	 */
	public void setLastDate(Date lastDate) {
		this.lastDate = lastDate;
	}

	/**
	 * @return the priceChangePercentage
	 */
	public double getPriceChangePercentage() {
		return priceChangePercentage;
	}

	/**
	 * @param priceChangePercentage the priceChangePercentage to set
	 */
	public void setPriceChangePercentage(double priceChangePercentage) {
		this.priceChangePercentage = priceChangePercentage;
	}

	public String toString(){
		StringBuffer result = new StringBuffer();

//		result.append("Time: "+StringUtil.convertDateToString(lastDate, StringUtil.TIME_PATTERN_2)+"\t");
		result.append("ID: "+stockID+"\t");
		result.append("Last Price: "+StringUtil.getFormattedDoubleString(lastPrice)+"\t");
		result.append("Change: "+StringUtil.getFormattedDoubleString(priceChange)+"\t");
		result.append("Percentage: "+StringUtil.convertToPercentage(priceChangePercentage));

		return result.toString();
	}
}
