//package piazza.nlp.redux.piazza;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.util.Map;
//
//
//import org.apache.http.client.ClientProtocolException;
//import org.json.JSONObject;
//
//import piazza.PiazzaSession;
//import piazza.nlp.redux.exceptions.LoginFailedException;
//import piazza.nlp.redux.exceptions.NotLoggedInException;
//
//public class APiazzaSessionRedux implements PiazzaSession {
//
//	protected final String CSRF_TOKEN = "tvKMFb4arP2CVKup";
//	protected final String COOKIE = "session_id=" + CSRF_TOKEN; 
//	protected final String PIAZZA_ENDPOINT = "https://piazza.com/logic/api";
//	
//    protected HttpClient client = HttpClient.newHttpClient();
//    protected ObjectMapper objectMapper = new ObjectMapper();
//
//	
//	public APiazzaSessionRedux() {
////		this.client = HttpClient.newHttpClient();
////        this.request = HttpRequest.newBuilder()
////              .uri(URI.create(PIAZZA_ENDPOINT))
////              .build();
//	}
//	
//	
//	@Override
//	public void login(String username, String password)
//			throws ClientProtocolException, IOException, LoginFailedException {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public Map<String, Object> getResp(String data, String apiEndpoint)
//			throws NotLoggedInException, ClientProtocolException, IOException {
//		
//		HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(apiEndpoint))
//                .header("Content-Type", "application/json") // Assuming JSON content
//                .POST(HttpRequest.BodyPublishers.ofString(data))
//                .build();
//            
//        try {
//            // 2. Request the response body as an InputStream to handle large data
//            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
//
//            if (response.statusCode() == 200) {
//                // 3. Use a streaming parser (Jackson) to read directly from the input stream
//                // This avoids loading the entire response into memory and prevents truncation.
//                try (InputStream responseBody = response.body()) {
//                    return objectMapper.readValue(responseBody, MyDataObject.class);
//                }
//            } else {
//                System.err.println("Request failed with status code: " + response.statusCode());
//                // Optionally, you could read the error stream here
//                return null;
//            }
//
//        } catch (Exception e) {
//            System.err.println("An error occurred during the API call.");
//            e.printStackTrace();
//            return null;
//        }
//		
//		
//		
//	}
//
//	@Override
//	public Map<String, Object> piazzaAPICall(String method, JSONObject params, String APIEndpt)
//			throws ClientProtocolException, NotLoggedInException, IOException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//}
