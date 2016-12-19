import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import com.lei.GetText;


public class CalendarTest {
    public static void main(String[] args) {
    	HttpURLConnection con =null;

    	try{
    		ProxyDetector pd = ProxyDetector.getInstance();
    		if(pd.proxyDetected()){
    			System.out.println("Host: "+pd.getHostname()+"  ;  Port: "+pd.getPort());
    			System.setProperty("http.proxyHost",pd.getHostname());
  	  			System.setProperty("http.proxyPort",String.valueOf(pd.getPort()));
    		}

    		GetText bundle = new GetText(GetText.DEFAULT_BUNDLE_NAME, Locale.ENGLISH);

    		String proxyServer = bundle.text("key.http.proxy.server");
    		String proxyPort = bundle.text("key.http.proxy.port");

			System.setProperty("http.proxyHost", proxyServer);
	  		System.setProperty("http.proxyPort", proxyPort);

    		String urlstring = "http://sww.sas.com";

    		urlstring = "http://www.google.com";

  	  		URL feedUrl = new URL(urlstring);


  	  		con = (HttpURLConnection) feedUrl.openConnection();

  	  		//If the url need user and password
//  	  		sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
//  	  		String encodedUserPwd = encoder.encode("sbjlwa:Xudongsun3685".getBytes());
//  	  		con.setRequestProperty("Authorization", "Basic " + encodedUserPwd);

  	  		BufferedReader input = new BufferedReader(new InputStreamReader(con.getInputStream()));
  			StringBuffer htmlContent = new StringBuffer();
  			String tmp = input.readLine();
  			while(tmp!=null){
  				htmlContent.append(tmp);
  				tmp = input.readLine();
  			}
//
  			System.out.println(htmlContent.toString());

//  	  	CalendarService myService = new CalendarService("exampleCo-exampleApp-1.0");
//    		//myService.setUserCredentials("root@gmail.com", "pa$$word");
//			myService.setUserCredentials("leiwang008@yahoo.com.cn", "Xudongsun3685");
//
//			URL feedUrl = new URL("http://www.google.com/calendar/feeds/default/allcalendars/full");
//			CalendarFeed resultFeed = myService.getFeed(feedUrl,CalendarFeed.class);
//
//			System.out.println("Your calendars:");
//			System.out.println();
//
//			for (int i = 0; i < resultFeed.getEntries().size(); i++) {
//				CalendarEntry entry = resultFeed.getEntries().get(i);
//				System.out.println("\t" + entry.getTitle().getPlainText());
//			}

    	}catch(Exception e){
    		e.printStackTrace();
    	}

    }

}
