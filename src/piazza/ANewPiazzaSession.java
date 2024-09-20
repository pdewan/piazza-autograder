package piazza;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
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
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

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
	private CookieStore cookieJar = new BasicCookieStore();
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
