package piazza;

import java.io.IOException;
import java.net.CookieStore;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import piazza.nlp.redux.exceptions.LoginFailedException;
import piazza.nlp.redux.exceptions.NotLoggedInException;

public class ANewPiazzaSession implements PiazzaSession {
	//String csrfToken = "RlRqmyFYK6qrGpjqQkNTTaYB";
	String csrfToken = "tvKMFb4arP2CVKup";
//	String cookie = "session_id=RlRqmyFYK6qrGpjqQkNTTaYB;";
	String cookie = "session_id=" + csrfToken; 


//	String cookie = "session_id=RlRqmyFYK6qrGpjqQkNTTaYB; _ga=GA1.1.86570036.1687269647; _ga_LV6WT3NEY7=GS1.1.1694648777.4.1.1694649051.0.0.0; AWSALB=Gh6SigrPVKHUIX8UBnmm0u7SQFD1X+3u9KID3Svbz+5lbdWcnE3hg83j4kLm/nFXjJD9lGx9zrPwPW5l2tQsyQSmk75xtaSEuoan9NJCUN95pSUakXFUv/er0obV; AWSALBCORS=Gh6SigrPVKHUIX8UBnmm0u7SQFD1X+3u9KID3Svbz+5lbdWcnE3hg83j4kLm/nFXjJD9lGx9zrPwPW5l2tQsyQSmk75xtaSEuoan9NJCUN95pSUakXFUv/er0obV; last_piaz_user=h68jepo6q4z3bk; piazza_session=2.eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzM4NCJ9.eyJkYXRhIjp7ImV4cGlyZXMiOjE2OTU4NjA1MTIsInBlcm0iOiIiLCJsb2dnaW5nX2luIjp0cnVlLCJjcmVhdGVkX2F0IjoxNjk0NjUwOTEyLCJ3aGVuIjoxNjk0NjUwODczLCJob21lIjoiLyIsInJlbWVtYmVyIjoib24iLCJzZXNzaW9uX3Rva2VuIjoic3RfaEZNczYwZFlsdFRRem02MXRveWEiLCJuaWRzIjoiazl6dnZsOXViYW82eGE6MTU7aDY4amRqa2dwcDkyc2o6MTU7aDY5aGtmcDZ6cDI1Nms6MTU7aGdkdmZkOHZqcWg2MzE6MTU7aGdkdms0YzBicG01ZTc6MTU7aHowOGxva3NuejUzazg6MTU7aHo0cTJqY2xoNGc3ZTI6MDtpZDdobnhjdW4xNTRhODoxNTtpZDdocm5oajNsaTM3ZjoxNTtpcXYwYnNiM3AyaTNjaDoxNTtpeHQ0ZmxjeHRmODRjcToxNTtqMjNudTdscXc2bjJkdToxNTtqNmR0MHVlam1sODI4ZDoxNTtqYzM1ZW53ajE2bDFmcjoxNTtqa3dzMGwwZ3ZjcjdpdDoxNTtqcWdrODI0MWU1bTY4ZToxNTtqcXIyYXo4ZTMwbDNsdToxNTtqdm9oeDRuc2RvNzcyMzoxNTtrNTVnbTZ1MGMzczNqdDoxNTtrZG9peWx3MnBnYjM4bDoxNTtrZGhvdjRzaW5iaDY4MzoxMDtrY3Z4d3R2Z2JtZTZ1ZjoxMDtraWtkYWZneXh3ZzJjMjoxNTtrb3NsaW5xMmtlMTJubDoxNTtrc2Yybzk4bWQwN2hsOjE1O2t4eDU5NGVvOGZsM3kyOjE1O2wwcjV0bW94bnYwN25zOjE1O2w0bzhnZXJhZzVoM2Q5OjE1O2w2bnVwMGRyMW1nN2NjOjE1O2xid25uMGx0YjN1NmVtOjEwO2xkdDMzM3gyYWNxNnlsOjE1O2xid25tY2F6N3pxNWlyOjEwO2pwZWI2a3l6NWk1MjdwOjEwO2p2djk5YjQwMW1mM3V2OjEwO2p6YW1lZTNtMTBoMmswOjU7azQ5MGNkczQ5cjYxaDE6MTA7a3g1MzZyd3M4YjAxbjoxMDtrcTZ6emtzdXBtaTNuMDoxMDtsM2pnbG1zenViejVhazoxMDtrYjcwdnVjZmJiZmNkOjEwO2wzamhqMHgzc2phNnVrOjU7bGkwcWs1NGxqMzgzNng6NTtsajRjd29sbmIwbjVjMzoxNTtsbGp2bDMyMThqdzJyNDoxNTsiLCJ0YWciOiIiLCJ1c2VyIjoiaDY4amVwbzZxNHozYmsiLCJlbWFpbCI6ImRld2FuQGNzLnVuYy5lZHUifSwibmJmIjoxNjk0NjUwOTEyLCJleHBpcmVzIjoxNjk1ODYwNTEyLCJpc3MiOiJwaWF6emEuY29tIn0.RjMWfLsgcSftnTIdz75vC3p_PHoQpomP1kFaqb_HAQ34wLzHKjmpVuuFOZg49A7vdhmr3d5s7xtBlSwsm6nXiV4itkv_ga-rKFToTq2d5FhngUdA_cS7uTRa3_rEU4dv"; 

	final String piazzaLogic = "https://piazza.com/logic/api";
	final String piazzaMain = "https://piazza.com/main/api";
	RequestConfig requestConfig = RequestConfig.custom().
			setCookieSpec(CookieSpecs.STANDARD).build();	 
	private BasicCookieStore cookieJar = new BasicCookieStore(); // TODO: had to change this from CookieStore to BasicCookieStore on 6/30/2025 for some reason
	private HttpClientBuilder builder = HttpClientBuilder.create().
			setDefaultRequestConfig(requestConfig).
			setDefaultCookieStore(cookieJar);
	private CloseableHttpClient httpClient = builder.build();  //创建http对象
	
	HttpContext context = new BasicHttpContext();
	private boolean loggedIn = false;
	
	public ANewPiazzaSession(String aCSRFToken, String aCookie) {
		csrfToken = aCSRFToken;
		cookie = aCookie;
	}
	
	public ANewPiazzaSession() {
		
	}
	
	private Map<String, Object> getCookies() {
		Map<String, Object> cookies = new HashMap<String, Object>();
		for (Cookie cookie : this.cookieJar.getCookies()) {
			cookies.put(cookie.getName(), cookie.getValue());
//			System.out.println(cookie.toString());
		}
		
		return cookies;
	}
	
	@Override
	public void login(String email, String password) throws ClientProtocolException, IOException, LoginFailedException {
		// TODO Auto-generated method stub
		String loginData = new JSONObject()
				.put("method", "user.login")
				.put("params", new JSONObject()
					.put("email", email)
					.put("pass", password)).toString();
		
		//System.out.println(loginData);
		
		HttpPost login = new HttpPost(piazzaLogic);    //创建请求方法实例，�?��?post请求，指定请求url
		
		login.setEntity(new StringEntity(loginData));  //�?��?请求�?�数(如需�?)
		
		login.setHeader("Accept", "application/json");
		login.setHeader("Content-type", "application/json");		
		login.setHeader("CSRF-Token", csrfToken);

//		request.setHeader("CSRF-Token", (String)this.getCookies().get("session_id"));
		login.setHeader("Cookie", cookie); 
		
		
		context.setAttribute(ClientContext.COOKIE_STORE, cookieJar);
		
		CloseableHttpResponse resp = httpClient.execute(login, context);  //调用HttpClient对象的execute(HttpUriRequest request)�?��?请求，该方法返回一个HttpResponse
		
		if (resp.getStatusLine().getStatusCode() != 200) {
			throw new LoginFailedException("Incorrect login credentials.");
		}
		
		System.out.println(EntityUtils.toString(resp.getEntity()));
		
		loggedIn = true;
	}
	
	public Map<String, Object> getResp(String data, String APIEndpt) throws NotLoggedInException, ClientProtocolException, IOException {
		if (!loggedIn) {
			throw new NotLoggedInException("You have not logged in");
		}
		
		HttpPost request = new HttpPost(APIEndpt);
		request.setEntity(new StringEntity(data));
		request.setHeader("Accept", "application/json");
		request.setHeader("Content-type", "application/json");
//		String csrfToken = "RlRqmyFYK6qrGpjqQkNTTaYB";
//		String cookie = "session_id=RlRqmyFYK6qrGpjqQkNTTaYB; _ga=GA1.1.86570036.1687269647; _ga_LV6WT3NEY7=GS1.1.1694648777.4.1.1694649051.0.0.0; AWSALB=Gh6SigrPVKHUIX8UBnmm0u7SQFD1X+3u9KID3Svbz+5lbdWcnE3hg83j4kLm/nFXjJD9lGx9zrPwPW5l2tQsyQSmk75xtaSEuoan9NJCUN95pSUakXFUv/er0obV; AWSALBCORS=Gh6SigrPVKHUIX8UBnmm0u7SQFD1X+3u9KID3Svbz+5lbdWcnE3hg83j4kLm/nFXjJD9lGx9zrPwPW5l2tQsyQSmk75xtaSEuoan9NJCUN95pSUakXFUv/er0obV; last_piaz_user=h68jepo6q4z3bk; piazza_session=2.eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzM4NCJ9.eyJkYXRhIjp7ImV4cGlyZXMiOjE2OTU4NjA1MTIsInBlcm0iOiIiLCJsb2dnaW5nX2luIjp0cnVlLCJjcmVhdGVkX2F0IjoxNjk0NjUwOTEyLCJ3aGVuIjoxNjk0NjUwODczLCJob21lIjoiLyIsInJlbWVtYmVyIjoib24iLCJzZXNzaW9uX3Rva2VuIjoic3RfaEZNczYwZFlsdFRRem02MXRveWEiLCJuaWRzIjoiazl6dnZsOXViYW82eGE6MTU7aDY4amRqa2dwcDkyc2o6MTU7aDY5aGtmcDZ6cDI1Nms6MTU7aGdkdmZkOHZqcWg2MzE6MTU7aGdkdms0YzBicG01ZTc6MTU7aHowOGxva3NuejUzazg6MTU7aHo0cTJqY2xoNGc3ZTI6MDtpZDdobnhjdW4xNTRhODoxNTtpZDdocm5oajNsaTM3ZjoxNTtpcXYwYnNiM3AyaTNjaDoxNTtpeHQ0ZmxjeHRmODRjcToxNTtqMjNudTdscXc2bjJkdToxNTtqNmR0MHVlam1sODI4ZDoxNTtqYzM1ZW53ajE2bDFmcjoxNTtqa3dzMGwwZ3ZjcjdpdDoxNTtqcWdrODI0MWU1bTY4ZToxNTtqcXIyYXo4ZTMwbDNsdToxNTtqdm9oeDRuc2RvNzcyMzoxNTtrNTVnbTZ1MGMzczNqdDoxNTtrZG9peWx3MnBnYjM4bDoxNTtrZGhvdjRzaW5iaDY4MzoxMDtrY3Z4d3R2Z2JtZTZ1ZjoxMDtraWtkYWZneXh3ZzJjMjoxNTtrb3NsaW5xMmtlMTJubDoxNTtrc2Yybzk4bWQwN2hsOjE1O2t4eDU5NGVvOGZsM3kyOjE1O2wwcjV0bW94bnYwN25zOjE1O2w0bzhnZXJhZzVoM2Q5OjE1O2w2bnVwMGRyMW1nN2NjOjE1O2xid25uMGx0YjN1NmVtOjEwO2xkdDMzM3gyYWNxNnlsOjE1O2xid25tY2F6N3pxNWlyOjEwO2pwZWI2a3l6NWk1MjdwOjEwO2p2djk5YjQwMW1mM3V2OjEwO2p6YW1lZTNtMTBoMmswOjU7azQ5MGNkczQ5cjYxaDE6MTA7a3g1MzZyd3M4YjAxbjoxMDtrcTZ6emtzdXBtaTNuMDoxMDtsM2pnbG1zenViejVhazoxMDtrYjcwdnVjZmJiZmNkOjEwO2wzamhqMHgzc2phNnVrOjU7bGkwcWs1NGxqMzgzNng6NTtsajRjd29sbmIwbjVjMzoxNTtsbGp2bDMyMThqdzJyNDoxNTsiLCJ0YWciOiIiLCJ1c2VyIjoiaDY4amVwbzZxNHozYmsiLCJlbWFpbCI6ImRld2FuQGNzLnVuYy5lZHUifSwibmJmIjoxNjk0NjUwOTEyLCJleHBpcmVzIjoxNjk1ODYwNTEyLCJpc3MiOiJwaWF6emEuY29tIn0.RjMWfLsgcSftnTIdz75vC3p_PHoQpomP1kFaqb_HAQ34wLzHKjmpVuuFOZg49A7vdhmr3d5s7xtBlSwsm6nXiV4itkv_ga-rKFToTq2d5FhngUdA_cS7uTRa3_rEU4dv"; 

		request.setHeader("CSRF-Token", csrfToken);

//		request.setHeader("CSRF-Token", (String)this.getCookies().get("session_id"));
		request.setHeader("Cookie", cookie); 
//				"session_id=RlRqmyFYK6qrGpjqQkNTTaYB; _ga=GA1.1.86570036.1687269647; _ga_LV6WT3NEY7=GS1.1.1694648777.4.1.1694649051.0.0.0; AWSALB=Gh6SigrPVKHUIX8UBnmm0u7SQFD1X+3u9KID3Svbz+5lbdWcnE3hg83j4kLm/nFXjJD9lGx9zrPwPW5l2tQsyQSmk75xtaSEuoan9NJCUN95pSUakXFUv/er0obV; AWSALBCORS=Gh6SigrPVKHUIX8UBnmm0u7SQFD1X+3u9KID3Svbz+5lbdWcnE3hg83j4kLm/nFXjJD9lGx9zrPwPW5l2tQsyQSmk75xtaSEuoan9NJCUN95pSUakXFUv/er0obV; last_piaz_user=h68jepo6q4z3bk; piazza_session=2.eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzM4NCJ9.eyJkYXRhIjp7ImV4cGlyZXMiOjE2OTU4NjA1MTIsInBlcm0iOiIiLCJsb2dnaW5nX2luIjp0cnVlLCJjcmVhdGVkX2F0IjoxNjk0NjUwOTEyLCJ3aGVuIjoxNjk0NjUwODczLCJob21lIjoiLyIsInJlbWVtYmVyIjoib24iLCJzZXNzaW9uX3Rva2VuIjoic3RfaEZNczYwZFlsdFRRem02MXRveWEiLCJuaWRzIjoiazl6dnZsOXViYW82eGE6MTU7aDY4amRqa2dwcDkyc2o6MTU7aDY5aGtmcDZ6cDI1Nms6MTU7aGdkdmZkOHZqcWg2MzE6MTU7aGdkdms0YzBicG01ZTc6MTU7aHowOGxva3NuejUzazg6MTU7aHo0cTJqY2xoNGc3ZTI6MDtpZDdobnhjdW4xNTRhODoxNTtpZDdocm5oajNsaTM3ZjoxNTtpcXYwYnNiM3AyaTNjaDoxNTtpeHQ0ZmxjeHRmODRjcToxNTtqMjNudTdscXc2bjJkdToxNTtqNmR0MHVlam1sODI4ZDoxNTtqYzM1ZW53ajE2bDFmcjoxNTtqa3dzMGwwZ3ZjcjdpdDoxNTtqcWdrODI0MWU1bTY4ZToxNTtqcXIyYXo4ZTMwbDNsdToxNTtqdm9oeDRuc2RvNzcyMzoxNTtrNTVnbTZ1MGMzczNqdDoxNTtrZG9peWx3MnBnYjM4bDoxNTtrZGhvdjRzaW5iaDY4MzoxMDtrY3Z4d3R2Z2JtZTZ1ZjoxMDtraWtkYWZneXh3ZzJjMjoxNTtrb3NsaW5xMmtlMTJubDoxNTtrc2Yybzk4bWQwN2hsOjE1O2t4eDU5NGVvOGZsM3kyOjE1O2wwcjV0bW94bnYwN25zOjE1O2w0bzhnZXJhZzVoM2Q5OjE1O2w2bnVwMGRyMW1nN2NjOjE1O2xid25uMGx0YjN1NmVtOjEwO2xkdDMzM3gyYWNxNnlsOjE1O2xid25tY2F6N3pxNWlyOjEwO2pwZWI2a3l6NWk1MjdwOjEwO2p2djk5YjQwMW1mM3V2OjEwO2p6YW1lZTNtMTBoMmswOjU7azQ5MGNkczQ5cjYxaDE6MTA7a3g1MzZyd3M4YjAxbjoxMDtrcTZ6emtzdXBtaTNuMDoxMDtsM2pnbG1zenViejVhazoxMDtrYjcwdnVjZmJiZmNkOjEwO2wzamhqMHgzc2phNnVrOjU7bGkwcWs1NGxqMzgzNng6NTtsajRjd29sbmIwbjVjMzoxNTtsbGp2bDMyMThqdzJyNDoxNTsiLCJ0YWciOiIiLCJ1c2VyIjoiaDY4amVwbzZxNHozYmsiLCJlbWFpbCI6ImRld2FuQGNzLnVuYy5lZHUifSwibmJmIjoxNjk0NjUwOTEyLCJleHBpcmVzIjoxNjk1ODYwNTEyLCJpc3MiOiJwaWF6emEuY29tIn0.RjMWfLsgcSftnTIdz75vC3p_PHoQpomP1kFaqb_HAQ34wLzHKjmpVuuFOZg49A7vdhmr3d5s7xtBlSwsm6nXiV4itkv_ga-rKFToTq2d5FhngUdA_cS7uTRa3_rEU4dv"); 

		//request.setHeader("CSRF-Token", (String)this.getCookies().get("session_id"));
		
		CloseableHttpResponse resp = httpClient.execute(request, context);
		
		if (resp.getStatusLine().getStatusCode() != 200)
			return null;
		
		String stringData = EntityUtils.toString(resp.getEntity());
		
//		System.out.println("STRING LENGTH: " + stringData.length());
//		System.out.println("MAX LENGTH ALLOWED: 16793600");
//		
		return new JSONObject(stringData).toMap();
	}
	
	public Map<String, Object> piazzaAPICall(String method, JSONObject params, String APIEndpt) throws ClientProtocolException, NotLoggedInException, IOException {
		String requestData = new JSONObject()
				.put("method", method)
				.put("params", params).toString();
		//System.out.println(cookieJar);
		return this.getResp(requestData, APIEndpt);
	}
}


//package piazza;
//
//import java.io.BufferedReader;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.PrintStream;
//import java.net.CookieStore;
//import java.nio.charset.StandardCharsets;
//import java.time.Duration;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.config.CookieSpecs;
//import org.apache.http.client.config.RequestConfig;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.client.protocol.ClientContext;
//import org.apache.http.cookie.Cookie;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.BasicCookieStore;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClientBuilder;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.protocol.BasicHttpContext;
//import org.apache.http.protocol.HttpContext;
//import org.apache.http.util.EntityUtils;
//import org.json.JSONObject;
//
//import piazza.nlp.redux.exceptions.LoginFailedException;
//import piazza.nlp.redux.exceptions.NotLoggedInException;
//
//public class ANewPiazzaSession implements PiazzaSession {
//	//String csrfToken = "RlRqmyFYK6qrGpjqQkNTTaYB";
//	String csrfToken = "tvKMFb4arP2CVKup";
////	String cookie = "session_id=RlRqmyFYK6qrGpjqQkNTTaYB;";
//	String cookie = "session_id=" + csrfToken; 
//
//
////	String cookie = "session_id=RlRqmyFYK6qrGpjqQkNTTaYB; _ga=GA1.1.86570036.1687269647; _ga_LV6WT3NEY7=GS1.1.1694648777.4.1.1694649051.0.0.0; AWSALB=Gh6SigrPVKHUIX8UBnmm0u7SQFD1X+3u9KID3Svbz+5lbdWcnE3hg83j4kLm/nFXjJD9lGx9zrPwPW5l2tQsyQSmk75xtaSEuoan9NJCUN95pSUakXFUv/er0obV; AWSALBCORS=Gh6SigrPVKHUIX8UBnmm0u7SQFD1X+3u9KID3Svbz+5lbdWcnE3hg83j4kLm/nFXjJD9lGx9zrPwPW5l2tQsyQSmk75xtaSEuoan9NJCUN95pSUakXFUv/er0obV; last_piaz_user=h68jepo6q4z3bk; piazza_session=2.eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzM4NCJ9.eyJkYXRhIjp7ImV4cGlyZXMiOjE2OTU4NjA1MTIsInBlcm0iOiIiLCJsb2dnaW5nX2luIjp0cnVlLCJjcmVhdGVkX2F0IjoxNjk0NjUwOTEyLCJ3aGVuIjoxNjk0NjUwODczLCJob21lIjoiLyIsInJlbWVtYmVyIjoib24iLCJzZXNzaW9uX3Rva2VuIjoic3RfaEZNczYwZFlsdFRRem02MXRveWEiLCJuaWRzIjoiazl6dnZsOXViYW82eGE6MTU7aDY4amRqa2dwcDkyc2o6MTU7aDY5aGtmcDZ6cDI1Nms6MTU7aGdkdmZkOHZqcWg2MzE6MTU7aGdkdms0YzBicG01ZTc6MTU7aHowOGxva3NuejUzazg6MTU7aHo0cTJqY2xoNGc3ZTI6MDtpZDdobnhjdW4xNTRhODoxNTtpZDdocm5oajNsaTM3ZjoxNTtpcXYwYnNiM3AyaTNjaDoxNTtpeHQ0ZmxjeHRmODRjcToxNTtqMjNudTdscXc2bjJkdToxNTtqNmR0MHVlam1sODI4ZDoxNTtqYzM1ZW53ajE2bDFmcjoxNTtqa3dzMGwwZ3ZjcjdpdDoxNTtqcWdrODI0MWU1bTY4ZToxNTtqcXIyYXo4ZTMwbDNsdToxNTtqdm9oeDRuc2RvNzcyMzoxNTtrNTVnbTZ1MGMzczNqdDoxNTtrZG9peWx3MnBnYjM4bDoxNTtrZGhvdjRzaW5iaDY4MzoxMDtrY3Z4d3R2Z2JtZTZ1ZjoxMDtraWtkYWZneXh3ZzJjMjoxNTtrb3NsaW5xMmtlMTJubDoxNTtrc2Yybzk4bWQwN2hsOjE1O2t4eDU5NGVvOGZsM3kyOjE1O2wwcjV0bW94bnYwN25zOjE1O2w0bzhnZXJhZzVoM2Q5OjE1O2w2bnVwMGRyMW1nN2NjOjE1O2xid25uMGx0YjN1NmVtOjEwO2xkdDMzM3gyYWNxNnlsOjE1O2xid25tY2F6N3pxNWlyOjEwO2pwZWI2a3l6NWk1MjdwOjEwO2p2djk5YjQwMW1mM3V2OjEwO2p6YW1lZTNtMTBoMmswOjU7azQ5MGNkczQ5cjYxaDE6MTA7a3g1MzZyd3M4YjAxbjoxMDtrcTZ6emtzdXBtaTNuMDoxMDtsM2pnbG1zenViejVhazoxMDtrYjcwdnVjZmJiZmNkOjEwO2wzamhqMHgzc2phNnVrOjU7bGkwcWs1NGxqMzgzNng6NTtsajRjd29sbmIwbjVjMzoxNTtsbGp2bDMyMThqdzJyNDoxNTsiLCJ0YWciOiIiLCJ1c2VyIjoiaDY4amVwbzZxNHozYmsiLCJlbWFpbCI6ImRld2FuQGNzLnVuYy5lZHUifSwibmJmIjoxNjk0NjUwOTEyLCJleHBpcmVzIjoxNjk1ODYwNTEyLCJpc3MiOiJwaWF6emEuY29tIn0.RjMWfLsgcSftnTIdz75vC3p_PHoQpomP1kFaqb_HAQ34wLzHKjmpVuuFOZg49A7vdhmr3d5s7xtBlSwsm6nXiV4itkv_ga-rKFToTq2d5FhngUdA_cS7uTRa3_rEU4dv"; 
//
//	final String piazzaLogic = "https://piazza.com/logic/api";
//	final String piazzaMain = "https://piazza.com/main/api";
//	RequestConfig requestConfig = RequestConfig.custom().
//			setCookieSpec(CookieSpecs.STANDARD).build();	 
//	private BasicCookieStore cookieJar = new BasicCookieStore(); // TODO: had to change this from CookieStore to BasicCookieStore on 6/30/2025 for some reason
//	private HttpClientBuilder builder = HttpClientBuilder.create().
//			setDefaultRequestConfig(requestConfig).
//			setDefaultCookieStore(cookieJar);
////	private CloseableHttpClient httpClient = builder.connectTimeout(Duration.ofSeconds(60)).build();  //创建http对象
////	private HttpClient httpClient = HttpClient.newBuilder()
////		    .connectTimeout(Duration.ofSeconds(60)) // Set connection timeout to 60 seconds
////		    .build();
//
////    int timeoutMillis = 30000; // 30 seconds
////
////    RequestConfig requestConfig = RequestConfig.custom()
////            .setConnectTimeout(timeoutMillis)
////            .setSocketTimeout(timeoutMillis)
////            .setConnectionRequestTimeout(timeoutMillis)
////            .build();
////
////    CloseableHttpClient httpClient = HttpClients.custom()
////            .setDefaultRequestConfig(requestConfig)
////            .build();
////	
////	RequestConfig globalConfig = RequestConfig.custom()
////	        .setCookieSpec(CookieSpecs.STANDARD) // Use the modern, standard spec
////	        .build();
////
////	CloseableHttpClient httpClient = HttpClients.custom()
////	        .setDefaultRequestConfig(globalConfig)
////	        .build();
//	
////	RequestConfig defaultRequestConfig = RequestConfig.custom()
////	        .setConnectTimeout(60000)        // 60 seconds to establish the connection
////	        .setSocketTimeout(60000)           // 60 seconds to wait for data
////	        .setConnectionRequestTimeout(60000) // 60 seconds to get a connection from the pool
////	        .setCookieSpec(CookieSpecs.STANDARD)
////	        .build();
//
//	// 2. Create the HttpClient with the default settings
////	CloseableHttpClient httpClient = HttpClients.custom()
//////	        .setDefaultRequestConfig(defaultRequestConfig)
////	        .build();
//
//	
////	int timeout = 5;
////	RequestConfig config = RequestConfig.custom()
////	  .setConnectTimeout(timeout * 1000)
////	  .setConnectionRequestTimeout(timeout * 1000)
////	  .setSocketTimeout(timeout * 1000).build();
////	CloseableHttpClient httpClient = 
////	  HttpClientBuilder.create().setDefaultRequestConfig(config).build();
////	
//	
//	
//	
//	
//	HttpContext context = new BasicHttpContext();
//	private boolean loggedIn = false;
//	
//	public ANewPiazzaSession(String aCSRFToken, String aCookie) {
//		csrfToken = aCSRFToken;
//		cookie = aCookie;
//	}
//	
//	public ANewPiazzaSession() {
//		
//	}
//	
//	private Map<String, Object> getCookies() {
//		Map<String, Object> cookies = new HashMap<String, Object>();
//		for (Cookie cookie : this.cookieJar.getCookies()) {
//			cookies.put(cookie.getName(), cookie.getValue());
////			System.out.println(cookie.toString());
//		}
//		
//		return cookies;
//	}
//	
//	@Override
//	public void login(String email, String password) throws ClientProtocolException, IOException, LoginFailedException {
//		// TODO Auto-generated method stub
//		String loginData = new JSONObject()
//				.put("method", "user.login")
//				.put("params", new JSONObject()
//					.put("email", email)
//					.put("pass", password)).toString();
//		
//		//System.out.println(loginData);
//		
//		HttpPost login = new HttpPost(piazzaLogic);    //创建请求方法实例，�?��?post请求，指定请求url
//		
//		login.setEntity(new StringEntity(loginData));  //�?��?请求�?�数(如需�?)
//		
//		login.setHeader("Accept", "application/json");
//		login.setHeader("Content-type", "application/json");		
//		login.setHeader("CSRF-Token", csrfToken);
//
////		request.setHeader("CSRF-Token", (String)this.getCookies().get("session_id"));
//		login.setHeader("Cookie", cookie); 
//		
//		
//		context.setAttribute(ClientContext.COOKIE_STORE, cookieJar);
//		
//		CloseableHttpResponse resp = httpClient.execute(login, context);  //调用HttpClient对象的execute(HttpUriRequest request)�?��?请求，该方法返回一个HttpResponse
//		
//		if (resp.getStatusLine().getStatusCode() != 200) {
//			throw new LoginFailedException("Incorrect login credentials.");
//		}
//		
////		System.out.println(EntityUtils.toString(resp.getEntity()));
//		
//		loggedIn = true;
//	}
//	
//	public Map<String, Object> getResp(String data, String APIEndpt) throws NotLoggedInException, ClientProtocolException, IOException {
//	    if (!loggedIn) {
//	        throw new NotLoggedInException("You have not logged in");
//	    }
//
//	    HttpPost request = new HttpPost(APIEndpt);
//
////	    RequestConfig requestConfig = RequestConfig.custom()
////	            .setConnectTimeout(60000) // 60 seconds
////	            .setConnectionRequestTimeout(60000) // 60 seconds
////	            .setSocketTimeout(60000) // 60 seconds
////	            .build();
////
////	    request.setConfig(requestConfig);
//
//	    request.setEntity(new StringEntity(data));
//	    request.setHeader("Accept", "application/json");
//	    request.setHeader("Content-type", "application/json");
//	    request.setHeader("CSRF-Token", csrfToken);
//	    request.setHeader("Cookie", cookie);
////	    request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
////	    request.setHeader("Accept-Encoding", "gzip, deflate, br");
//
//
//	    CloseableHttpResponse resp = httpClient.execute(request, context);
//
//	    if (resp.getStatusLine().getStatusCode() != 200) {
//	        resp.close();
//	        return null;
//	    }
//
//	    String stringData;
//	    HttpEntity entity = resp.getEntity();
//	    if (entity != null) {
//	        try (InputStream instream = entity.getContent()) {
//	            // Use a StringBuilder to efficiently build the string
//	            StringBuilder sb = new StringBuilder();
//	            // Wrap the InputStream in a reader, and the reader in a buffer
//	            BufferedReader reader = new BufferedReader(new InputStreamReader(instream, StandardCharsets.UTF_8));
//	            char[] buffer = new char[8192]; // 8K buffer
//	            int bytesRead;
//	            // Read from the buffer until the end of the stream
//	            while ((bytesRead = reader.read(buffer, 0, buffer.length)) != -1) {
//	                sb.append(buffer, 0, bytesRead);
//	            }
//	            stringData = sb.toString();
//	        }
//	    } else {
//	        stringData = "";
//	    }
//
//	    resp.close();
//
//	    // The redirection of System.out to a file is kept as in the original code.
//	    try (FileOutputStream fileOutputStream = new FileOutputStream("test_output.log");
//	         PrintStream filePrintStream = new PrintStream(fileOutputStream)) {
//	       
////	    	System.setOut(filePrintStream);
////	        System.out.println("STRING LENGTH: " + stringData.length());
////	        System.out.println("STRING:\n\n" + stringData);
//
////	        System.out.println("STRING SNIPPET:\n\n" + stringData.substring(15950, 16050));
//
//		    JSONObject d = new JSONObject(stringData);
////	        System.out.println("JSON DATA:\n\n" + d);
////	        System.out.println("JSON MAP:\n\n" + d.toMap());
//	        
//		    return d.toMap();
//
//	        
//	    }
//
//	    
//	    
//	}
//	
//	
////	public Map<String, Object> getResp(String data, String APIEndpt) throws NotLoggedInException, ClientProtocolException, IOException {
////		if (!loggedIn) {
////			throw new NotLoggedInException("You have not logged in");
////		}
////		
////		HttpPost request = new HttpPost(APIEndpt);
////		
////	    RequestConfig requestConfig = RequestConfig.custom()
////	            .setConnectTimeout(30000)        // 30 seconds
////	            .setConnectionRequestTimeout(30000) // 30 seconds
////	            .setSocketTimeout(30000)           // 30 seconds
////	            .build();
////
////	    request.setConfig(requestConfig);
////		
////	    
//////	    System.out.println("TEST PRINT: " + org.apache.http.client.config.CookieSpecs.class.getProtectionDomain().getCodeSource().getLocation());
////
////	    
////		
////		request.setEntity(new StringEntity(data));
////		request.setHeader("Accept", "application/json");
////		request.setHeader("Content-type", "application/json");
//////		String csrfToken = "RlRqmyFYK6qrGpjqQkNTTaYB";
//////		String cookie = "session_id=RlRqmyFYK6qrGpjqQkNTTaYB; _ga=GA1.1.86570036.1687269647; _ga_LV6WT3NEY7=GS1.1.1694648777.4.1.1694649051.0.0.0; AWSALB=Gh6SigrPVKHUIX8UBnmm0u7SQFD1X+3u9KID3Svbz+5lbdWcnE3hg83j4kLm/nFXjJD9lGx9zrPwPW5l2tQsyQSmk75xtaSEuoan9NJCUN95pSUakXFUv/er0obV; AWSALBCORS=Gh6SigrPVKHUIX8UBnmm0u7SQFD1X+3u9KID3Svbz+5lbdWcnE3hg83j4kLm/nFXjJD9lGx9zrPwPW5l2tQsyQSmk75xtaSEuoan9NJCUN95pSUakXFUv/er0obV; last_piaz_user=h68jepo6q4z3bk; piazza_session=2.eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzM4NCJ9.eyJkYXRhIjp7ImV4cGlyZXMiOjE2OTU4NjA1MTIsInBlcm0iOiIiLCJsb2dnaW5nX2luIjp0cnVlLCJjcmVhdGVkX2F0IjoxNjk0NjUwOTEyLCJ3aGVuIjoxNjk0NjUwODczLCJob21lIjoiLyIsInJlbWVtYmVyIjoib24iLCJzZXNzaW9uX3Rva2VuIjoic3RfaEZNczYwZFlsdFRRem02MXRveWEiLCJuaWRzIjoiazl6dnZsOXViYW82eGE6MTU7aDY4amRqa2dwcDkyc2o6MTU7aDY5aGtmcDZ6cDI1Nms6MTU7aGdkdmZkOHZqcWg2MzE6MTU7aGdkdms0YzBicG01ZTc6MTU7aHowOGxva3NuejUzazg6MTU7aHo0cTJqY2xoNGc3ZTI6MDtpZDdobnhjdW4xNTRhODoxNTtpZDdocm5oajNsaTM3ZjoxNTtpcXYwYnNiM3AyaTNjaDoxNTtpeHQ0ZmxjeHRmODRjcToxNTtqMjNudTdscXc2bjJkdToxNTtqNmR0MHVlam1sODI4ZDoxNTtqYzM1ZW53ajE2bDFmcjoxNTtqa3dzMGwwZ3ZjcjdpdDoxNTtqcWdrODI0MWU1bTY4ZToxNTtqcXIyYXo4ZTMwbDNsdToxNTtqdm9oeDRuc2RvNzcyMzoxNTtrNTVnbTZ1MGMzczNqdDoxNTtrZG9peWx3MnBnYjM4bDoxNTtrZGhvdjRzaW5iaDY4MzoxMDtrY3Z4d3R2Z2JtZTZ1ZjoxMDtraWtkYWZneXh3ZzJjMjoxNTtrb3NsaW5xMmtlMTJubDoxNTtrc2Yybzk4bWQwN2hsOjE1O2t4eDU5NGVvOGZsM3kyOjE1O2wwcjV0bW94bnYwN25zOjE1O2w0bzhnZXJhZzVoM2Q5OjE1O2w2bnVwMGRyMW1nN2NjOjE1O2xid25uMGx0YjN1NmVtOjEwO2xkdDMzM3gyYWNxNnlsOjE1O2xid25tY2F6N3pxNWlyOjEwO2pwZWI2a3l6NWk1MjdwOjEwO2p2djk5YjQwMW1mM3V2OjEwO2p6YW1lZTNtMTBoMmswOjU7azQ5MGNkczQ5cjYxaDE6MTA7a3g1MzZyd3M4YjAxbjoxMDtrcTZ6emtzdXBtaTNuMDoxMDtsM2pnbG1zenViejVhazoxMDtrYjcwdnVjZmJiZmNkOjEwO2wzamhqMHgzc2phNnVrOjU7bGkwcWs1NGxqMzgzNng6NTtsajRjd29sbmIwbjVjMzoxNTtsbGp2bDMyMThqdzJyNDoxNTsiLCJ0YWciOiIiLCJ1c2VyIjoiaDY4amVwbzZxNHozYmsiLCJlbWFpbCI6ImRld2FuQGNzLnVuYy5lZHUifSwibmJmIjoxNjk0NjUwOTEyLCJleHBpcmVzIjoxNjk1ODYwNTEyLCJpc3MiOiJwaWF6emEuY29tIn0.RjMWfLsgcSftnTIdz75vC3p_PHoQpomP1kFaqb_HAQ34wLzHKjmpVuuFOZg49A7vdhmr3d5s7xtBlSwsm6nXiV4itkv_ga-rKFToTq2d5FhngUdA_cS7uTRa3_rEU4dv"; 
////
////		request.setHeader("Accept-Encoding", "gzip, deflate, br");
////		request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
////
////		
////		request.setHeader("CSRF-Token", csrfToken);
////
//////		request.setHeader("CSRF-Token", (String)this.getCookies().get("session_id"));
////		request.setHeader("Cookie", cookie); 
//////				"session_id=RlRqmyFYK6qrGpjqQkNTTaYB; _ga=GA1.1.86570036.1687269647; _ga_LV6WT3NEY7=GS1.1.1694648777.4.1.1694649051.0.0.0; AWSALB=Gh6SigrPVKHUIX8UBnmm0u7SQFD1X+3u9KID3Svbz+5lbdWcnE3hg83j4kLm/nFXjJD9lGx9zrPwPW5l2tQsyQSmk75xtaSEuoan9NJCUN95pSUakXFUv/er0obV; AWSALBCORS=Gh6SigrPVKHUIX8UBnmm0u7SQFD1X+3u9KID3Svbz+5lbdWcnE3hg83j4kLm/nFXjJD9lGx9zrPwPW5l2tQsyQSmk75xtaSEuoan9NJCUN95pSUakXFUv/er0obV; last_piaz_user=h68jepo6q4z3bk; piazza_session=2.eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzM4NCJ9.eyJkYXRhIjp7ImV4cGlyZXMiOjE2OTU4NjA1MTIsInBlcm0iOiIiLCJsb2dnaW5nX2luIjp0cnVlLCJjcmVhdGVkX2F0IjoxNjk0NjUwOTEyLCJ3aGVuIjoxNjk0NjUwODczLCJob21lIjoiLyIsInJlbWVtYmVyIjoib24iLCJzZXNzaW9uX3Rva2VuIjoic3RfaEZNczYwZFlsdFRRem02MXRveWEiLCJuaWRzIjoiazl6dnZsOXViYW82eGE6MTU7aDY4amRqa2dwcDkyc2o6MTU7aDY5aGtmcDZ6cDI1Nms6MTU7aGdkdmZkOHZqcWg2MzE6MTU7aGdkdms0YzBicG01ZTc6MTU7aHowOGxva3NuejUzazg6MTU7aHo0cTJqY2xoNGc3ZTI6MDtpZDdobnhjdW4xNTRhODoxNTtpZDdocm5oajNsaTM3ZjoxNTtpcXYwYnNiM3AyaTNjaDoxNTtpeHQ0ZmxjeHRmODRjcToxNTtqMjNudTdscXc2bjJkdToxNTtqNmR0MHVlam1sODI4ZDoxNTtqYzM1ZW53ajE2bDFmcjoxNTtqa3dzMGwwZ3ZjcjdpdDoxNTtqcWdrODI0MWU1bTY4ZToxNTtqcXIyYXo4ZTMwbDNsdToxNTtqdm9oeDRuc2RvNzcyMzoxNTtrNTVnbTZ1MGMzczNqdDoxNTtrZG9peWx3MnBnYjM4bDoxNTtrZGhvdjRzaW5iaDY4MzoxMDtrY3Z4d3R2Z2JtZTZ1ZjoxMDtraWtkYWZneXh3ZzJjMjoxNTtrb3NsaW5xMmtlMTJubDoxNTtrc2Yybzk4bWQwN2hsOjE1O2t4eDU5NGVvOGZsM3kyOjE1O2wwcjV0bW94bnYwN25zOjE1O2w0bzhnZXJhZzVoM2Q5OjE1O2w2bnVwMGRyMW1nN2NjOjE1O2xid25uMGx0YjN1NmVtOjEwO2xkdDMzM3gyYWNxNnlsOjE1O2xid25tY2F6N3pxNWlyOjEwO2pwZWI2a3l6NWk1MjdwOjEwO2p2djk5YjQwMW1mM3V2OjEwO2p6YW1lZTNtMTBoMmswOjU7azQ5MGNkczQ5cjYxaDE6MTA7a3g1MzZyd3M4YjAxbjoxMDtrcTZ6emtzdXBtaTNuMDoxMDtsM2pnbG1zenViejVhazoxMDtrYjcwdnVjZmJiZmNkOjEwO2wzamhqMHgzc2phNnVrOjU7bGkwcWs1NGxqMzgzNng6NTtsajRjd29sbmIwbjVjMzoxNTtsbGp2bDMyMThqdzJyNDoxNTsiLCJ0YWciOiIiLCJ1c2VyIjoiaDY4amVwbzZxNHozYmsiLCJlbWFpbCI6ImRld2FuQGNzLnVuYy5lZHUifSwibmJmIjoxNjk0NjUwOTEyLCJleHBpcmVzIjoxNjk1ODYwNTEyLCJpc3MiOiJwaWF6emEuY29tIn0.RjMWfLsgcSftnTIdz75vC3p_PHoQpomP1kFaqb_HAQ34wLzHKjmpVuuFOZg49A7vdhmr3d5s7xtBlSwsm6nXiV4itkv_ga-rKFToTq2d5FhngUdA_cS7uTRa3_rEU4dv"); 
////
////		//request.setHeader("CSRF-Token", (String)this.getCookies().get("session_id"));
////		
////		CloseableHttpResponse resp = httpClient.execute(request, context);
////		
////		if (resp.getStatusLine().getStatusCode() != 200)
////			return null;
////		
////		String stringData = EntityUtils.toString(resp.getEntity());
////		
////        FileOutputStream fileOutputStream = new FileOutputStream("test_output.log");
////
////        // Create a PrintStream that writes to the FileOutputStream
////        PrintStream filePrintStream = new PrintStream(fileOutputStream);
////
////        // Redirect System.out to the new PrintStream
////        System.setOut(filePrintStream);
////		
////		
////		System.out.println("STRING LENGTH: " + stringData.length());
////		
//////		JSONObject test = new JSONObject(stringData);
//////		Map<String, Object> m = test.toMap();
//////		for (String k : m.get("result").keySet()) {
//////			System.out.println(k);
//////		}
////		
////		return new JSONObject(stringData).toMap();
////	}
//	
//	
////	public Map<String, Object> getResp(String data, String APIEndpt) throws NotLoggedInException, ClientProtocolException, IOException {
////	    if (!loggedIn) {
////	        throw new NotLoggedInException("You have not logged in");
////	    }
////
////	    HttpPost request = new HttpPost(APIEndpt);
////	    request.setEntity(new StringEntity(data));
////
////	    // Set Headers
////	    request.setHeader("Accept", "application/json");
////	    request.setHeader("Content-type", "application/json");
////	    request.setHeader("Accept-Encoding", "gzip, deflate, br");
////	    request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
////	    request.setHeader("CSRF-Token", csrfToken);
////	    request.setHeader("Cookie", cookie);
////	    CloseableHttpResponse resp = httpClient.execute(request, context);
////
////	    // --- START DEBUGGING LOGS ---
////	    System.out.println("Response Status: " + resp.getStatusLine().getStatusCode());
////
////	    // Log all response headers to see what the server sent back
////	    System.out.println("Response Headers: " + Arrays.toString(resp.getAllHeaders()));
////
////	    String stringData = EntityUtils.toString(resp.getEntity());
////	    System.out.println("### RECEIVED DATA LENGTH: " + stringData.length() + " ###");
////
////	    // Check if the received data itself looks like truncated JSON
////	    if (stringData.length() < 1000) {
////	        System.out.println("Received Data (first 1000 chars): " + stringData);
////	    } else {
////	        System.out.println("Received Data (first 500 chars): " + stringData.substring(0, 500));
////	        System.out.println("...");
////	        System.out.println("Received Data (last 500 chars): " + stringData.substring(stringData.length() - 500));
////	    }
////	    // --- END DEBUGGING LOGS ---
////
////	    resp.close();
////
////	    if (resp.getStatusLine().getStatusCode() != 200) {
////	        return null;
////	    }
////
////	    // Now, parse the data
////	    return new JSONObject(stringData).toMap();
////	}
//	
//	public Map<String, Object> piazzaAPICall(String method, JSONObject params, String APIEndpt) throws ClientProtocolException, NotLoggedInException, IOException {
//	String requestData = new JSONObject()
//			.put("method", method)
//			.put("params", params).toString();
//	//System.out.println(cookieJar);
//	return this.getResp(requestData, APIEndpt);
//}
//	
//	
//}
