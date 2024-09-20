package piazza.nlp;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONObject;

import piazza.LoginFailedException;
import piazza.NotLoggedInException;

public class AGPTClass {

	private String apiKey;
	private String defaultModel;

	public AGPTClass(String apiKey, String defaultModel) {
			this.apiKey = apiKey;
			this.defaultModel = defaultModel;;
	}
	
	// https://platform.openai.com/docs/api-reference/chat/create
	public String makeCall(String prompt, String model, String endpoint) throws IOException {
		
		// set up connection
		URL obj = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setRequestProperty("Content-Type", "application/json");

//        // The request body
//        String body = "{\"model\": \"" + model + "\", \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}]}";
//        
//        
//        // TODO: find a way to strip markdown (instead of just avoiding it)
//        body = body.replaceAll("\n", "\\\\n").replace("&#34;", "\\\"").replaceAll("&#43;", "+").replaceAll("/", "\\\\/");
        JSONObject test = new JSONObject();
        test.put("model", model);
        JSONArray test2 = new JSONArray();
        JSONObject test3 = new JSONObject();
        test3.put("role", "user");
        test3.put("content", prompt);
        test2.put(test3);
        test.put("messages", test2);
        
        String body = test.toString();
//        System.out.println(body);
        
        connection.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
        writer.write(body);
        writer.flush();
        writer.close();

        // Response from ChatGPT
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        String line;

        StringBuffer response = new StringBuffer();

        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        br.close();
        
//        System.out.println("Full GPT response:\n");
//        System.out.println(response);

        // calls the method to extract the message.
//        System.out.println("Extracted version:\n");
        String message = extractMessageFromJSONResponse(response.toString());
//        System.out.println(message);
        return message;
		
	}
	
	public String makeCall(String prompt) throws IOException {
		return makeCall(prompt, this.defaultModel, "https://api.openai.com/v1/chat/completions");
	}
	
	public static String extractMessageFromJSONResponse(String response) {
		JSONObject responseObj = new JSONObject(response);
		JSONArray choicesArr = responseObj.getJSONArray("choices");
		JSONObject messageObj = choicesArr.getJSONObject(choicesArr.length()-1).getJSONObject("message");
		return messageObj.getString("content");
   }

	
	
   
}
