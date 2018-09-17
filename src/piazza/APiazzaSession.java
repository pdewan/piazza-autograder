package piazza;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class APiazzaSession implements PiazzaSession {
	
	final String piazzaLogic = "https://piazza.com/logic/api";
	final String piazzaMain = "https://piazza.com/main/api";
	
	private CookieStore cookieJar = new BasicCookieStore();
	private HttpClientBuilder builder = HttpClientBuilder.create().setDefaultCookieStore(cookieJar);
	private CloseableHttpClient httpClient = builder.build();  //创建http对象

	private boolean loggedIn = false;
	
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
		
		System.out.println(loginData);
		
		HttpPost login = new HttpPost(piazzaLogic);    //创建请求方法实例，发送post请求，指定请求url
		
		login.setEntity(new StringEntity(loginData));  //发送请求参数(如需要)
		
		login.setHeader("Accept", "application/json");
		login.setHeader("Content-type", "application/json");
		
		CloseableHttpResponse resp = httpClient.execute(login);  //调用HttpClient对象的execute(HttpUriRequest request)发送请求，该方法返回一个HttpResponse
		
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
		request.setHeader("CSRF-Token", (String)this.getCookies().get("session_id"));
		
		CloseableHttpResponse resp = httpClient.execute(request);
		
		if (resp.getStatusLine().getStatusCode() != 200)
			return null;
		
		String stringData = EntityUtils.toString(resp.getEntity());
		
		return new JSONObject(stringData).toMap();
	}
	
	public Map<String, Object> piazzaAPICall(String method, JSONObject params, String APIEndpt) throws ClientProtocolException, NotLoggedInException, IOException {
		String requestData = new JSONObject()
				.put("method", method)
				.put("params", params).toString();
		
		return this.getResp(requestData, APIEndpt);
	}
}
