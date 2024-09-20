package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;

import piazza.LoginFailedException;
import piazza.NotLoggedInException;
import piazza.nlp.AGPTClass;

public class GPTTester {
	
	public static void main(String[] argv) throws IOException {
		
		// load config
		BufferedReader configReader = new BufferedReader(new FileReader("config.json"));
		
		String text = "";
		String line = configReader.readLine();
		while (line != null) {
			text = text + line;
			line = configReader.readLine();
		}
		
		JSONObject config = new JSONObject(text);
		String apiKey = config.getString("openai_api_key");
		String defaultModel = config.getString("default_gpt_model");
		
		AGPTClass gptTest = new AGPTClass(apiKey, defaultModel);
		//String testPrompt = "hello, how are you? Can you tell me what's a Fibonacci Number?";
		String testPrompt = "You are a Teaching Assistant for an upper-level Computer Science course. Please explain the possible sources of this error: I will come by at 5pm to ask questions about nothing";
		//		"<p>I will come by at 5pm to ask questions about  nothing</p>";
				
		System.out.println(gptTest.makeCall(testPrompt));
		
		configReader.close();
	}

	
}
