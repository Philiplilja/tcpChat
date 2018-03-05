
package TCPChat.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

import org.json.JSONObject;



public final class QuoteHandler {

		static private String quote;
		static private int thisDay;
		
		//sets the day
		public static void initialize(){
			thisDay = getDay();
			try {
				quote = newQuote();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				quote = "qotd is not available for the moment";
			}
		}	
		
		//check if it's a new day
		static private void updateDay(){
			if (thisDay != getDay()) {
				thisDay = getDay();
				try {
					quote = newQuote();
				} catch (IOException e) {
					e.printStackTrace();
					quote = "Quote of the day is not available";
				}
			}
		}
		

	
		//What quote it is that day
		static private String newQuote() throws IOException{
			 	URL url = new URL("http://quotesondesign.com/api/3.0/api-3.0.json");
		
		        
		        URLConnection urlc = url.openConnection();
		        
		        
		        urlc.setDoOutput(true);
		        urlc.setAllowUserInteraction(false);

		        BufferedReader m_Breader = 
		        		new BufferedReader
		        		(new InputStreamReader(urlc.getInputStream()));
		        String l = null;
		        String response = "";
		        while ((l=m_Breader.readLine())!=null) {
		          
		            response += l;
		            
		        }
		        m_Breader.close();

		        JSONObject jObject  = new JSONObject(response);
		        String quote = jObject.getString("quote"); 
		        return quote;
		        
			
		}
	
		static private int getDay(){
			Calendar c = Calendar.getInstance();
			return c.get(Calendar.DAY_OF_WEEK);
		}
		
		public synchronized static String getQuote(){
			updateDay();			
			return quote;
		}
	
}
