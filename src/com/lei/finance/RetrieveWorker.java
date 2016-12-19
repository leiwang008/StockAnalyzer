package com.lei.finance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import com.lei.util.StringUtil;

public class RetrieveWorker extends Thread{

	public static final int	DEFAULT_INTERVAL_SECOND		= 30;
	public static final double	DEFAULT_ALERT_PERCENT	= 0;

	/**
	 * The URL string used to query the current stock information.<br>
	 * Example of URL: http://www.google.com/finance/info?q=600317<br>
	 */
	private String stockQueryURL;
	/**
	 * The interval time in second to query a stock's information. The default is 30 seconds.<br>
	 */
	private int 	intervalTime=DEFAULT_INTERVAL_SECOND;
	/**
	 * The threshold in percentage to alert. This value is between 0 and 100.<br>
	 * If this value is 2, which means if the stock's price change is bigger than 2%, user will be alert.<br>
	 */
	private double	alertPercent=DEFAULT_ALERT_PERCENT;

	/**
	 * The object to hold the stock's information.
	 */
	private StokcInfomationTO stockTO;
	private double	lastPrice;
	private boolean priceChanged;
	private double max;
	private double min;
	private StockTest stockTest;

	public final String PRICE_DOWN = "Down";
	public final String PRICE_UP = "Up";

	/**
	 * The field pleaseWait is used to control the pause of a thread.
	 */
	private boolean pleaseWait = false;
	/**
	 * The field pleaseWait is used to kill a thread, that means to stop a thread.
	 */
	private boolean pleaseStop = false;
	/**
	 * The field mailSended is used to mark if the alert email has been sent or not
	 * If the alert email has been sent, we should change this field to true so that
	 * the alert email will not be sent again.
	 * If you want to receive again the alert email, you should reset this field to
	 * false by the button buttonSendEmail of StockTest
	 * @see StockTest
	 */
	private boolean mailSended = true;

	public RetrieveWorker(String stockURL){
		this.stockQueryURL = stockURL;
		this.lastPrice = 0.0;
		this.priceChanged = false;
		this.max = 0.00;
		this.min = 100000.00;
	}

	public RetrieveWorker(String stockURL,int intervalTime){
		this.stockQueryURL = stockURL;
		this.intervalTime = intervalTime;
		this.lastPrice = 0.0;
		this.priceChanged = false;
		this.max = 0.00;
		this.min = 100000.00;
	}

	public RetrieveWorker(String stockURL,int intervalTime, double alertPercent){
		this.stockQueryURL = stockURL;
		this.intervalTime = intervalTime;
		this.lastPrice = 0.0;
		this.priceChanged = false;
		this.max = 0.00;
		this.min = 100000.00;
		this.alertPercent = alertPercent;
	}

	public RetrieveWorker(String stockURL,Integer intervalTime, Double alertPercent){
		this.stockQueryURL = stockURL;
		this.lastPrice = 0.0;
		this.priceChanged = false;
		this.max = 0.00;
		this.min = 100000.00;
		if(intervalTime!=null)	this.intervalTime = intervalTime.intValue();
		if(alertPercent!=null)this.alertPercent = alertPercent.doubleValue();
	}

	public StockTest getStockTest() {
		return stockTest;
	}

	public void setStockTest(StockTest stockTest) {
		this.stockTest = stockTest;
	}

	public synchronized double getAlertPercent() {
		return alertPercent;
	}

	public synchronized void setAlertPercent(double alertPercent){
		this.alertPercent = alertPercent;
	}

	public synchronized int getIntervalTime() {
		return intervalTime;
	}

	public synchronized void setIntervalTime(int intervalTime) {
		this.intervalTime = intervalTime;
	}

	public synchronized StokcInfomationTO getStockInfo() {
		return stockTO;
	}

	public synchronized void setStockInfo(StokcInfomationTO stockInfo) {
		this.stockTO = stockInfo;
	}

	public synchronized boolean getPriceChanged() {
		return priceChanged;
	}

	public synchronized boolean isPleaseWait() {
		return pleaseWait;
	}

	public synchronized void setPleaseWait(boolean pleaseWait) {
		this.pleaseWait = pleaseWait;
	}

	public synchronized boolean isPleaseStop() {
		return pleaseStop;
	}
	public synchronized void setPleaseStop(boolean pleaseStop) {
		this.pleaseStop = pleaseStop;
	}

	public synchronized boolean isMailSended() {
		return mailSended;
	}

	public synchronized void setMailSended(boolean mailSended) {
		this.mailSended = mailSended;
	}

	public void run(){
		BufferedReader input = null;
		long beginTime = new Date().getTime();
		long endTime = beginTime;

		while (true) {
			//Check the property pleaseStop, if true, stop this thread.
			if(this.isPleaseStop()){
				break;
			}

			synchronized(this){
				if(isPleaseWait()){
					try {
						wait();
					} catch (InterruptedException e) {
						System.err.println("InterruptedException occured.");
					}
				}
			}

			try {
				URL request = new URL(stockQueryURL);
				input = new BufferedReader(new InputStreamReader(request.openStream()));

				StringBuffer htmlContent = new StringBuffer();
				String tmp = input.readLine();
				while (tmp != null) {
					htmlContent.append(tmp);
					tmp = input.readLine();
				}
				stockTO = new StokcInfomationTO(htmlContent.toString());
				double currentPrice = stockTO.getLastPrice();
				if(this.lastPrice!=currentPrice){
					priceChanged = true;
					String upOrDown = (currentPrice>lastPrice) ? PRICE_UP: PRICE_DOWN;
					lastPrice = currentPrice;
					if(currentPrice>max) max = currentPrice;
					if(currentPrice<min) min = currentPrice;
					double d = stockTO.getPriceChangePercentage();
					if(d<0)	d = -d;
					if(d>=alertPercent){
						endTime = new Date().getTime();
						long secondsPassed = (endTime - beginTime)/1000;
						beginTime = endTime;
						//1. We will show the stock current status on the output console
						String info = stockTO.toString()+"\t" +upOrDown+"\t"+secondsPassed+" s passed "+"\tmin: "+StringUtil.getFormattedDoubleString(min)+"\tmax: "+StringUtil.getFormattedDoubleString(max);
//						System.out.println(info);
//						Toolkit.getDefaultToolkit().beep();
						StockTest stock = this.getStockTest();
						if(stock!=null){
							stock.appendTextAreaStockInfo(info);
						}
						//2. We may send a sms to our mobile phone.

						//3. We may send a email to our mail box.
						synchronized(this){
							if(!isMailSended()){
								stockTest.sendMail("Alert Stock!", info);
								setMailSended(true);
								//After we send an alert email, we set the field mailSended to true of this worker
								//If the selected stock on stockTest is the stock this worker work for, we should
								//modify the button state buttonSendEmail to true.
								if(stock!=null){
									String selectedStock = stock.getSelectedStockNumber();
									if(stockTO.getStockID().equals(selectedStock)){
										stock.updateButtonState(selectedStock);
									}
								}
							}
						}
					}
				}else{
					priceChanged = false;
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (input != null)
						input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			try {
				Thread.sleep(intervalTime*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
