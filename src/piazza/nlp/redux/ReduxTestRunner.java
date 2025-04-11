package piazza.nlp.redux;

import java.util.List;
import java.util.Map;

import piazza.APiazzaClass;

public class ReduxTestRunner {

    public static String getLatestContent(Map<String, Object> item) {	
		Map<String, Object> latestElement = getLatestElement(item);
		return (String) latestElement.get("content");
    }
    public static Map<String, Object> getLatestElement(Map<String, Object> item) {
  		List<Map<String, Object>> historyList = (List<Map<String, Object>>) item.get("history");
  		Map<String, Object> latestElement = historyList.get(0);
  		return latestElement;
      }
	
	public static void main(String[] args) {
		
		String email = System.getenv("PIAZZA_EMAIL");
		String password = System.getenv("PIAZZA_PASSWORD");
		String classID = System.getenv("PIAZZA_CLASS_ID");
		String apiKey = System.getenv("OPENAI_API_KEY");;
		String defaultModel = System.getenv("DEFAULT_GPT_MODEL");
		String lastRun = System.getenv("LAST_RUN");
	
		APiazzaForum pf = new APiazzaForum(classID, email, password);
		//pf.getPost(53);
//		pf.getPost("m0mypf6bnkm4fc");
//		pf.getPost("m0qadc69kex6fj"); // to=m0qadc69kex6fj
//		pf.getFeed();
//		System.out.println(pf.getPost("m72prlqjhxgj5").getTags());
		System.out.println(pf.getPost(291).getAllData());

//		pf.searchPosts("null pointer");
		
		//System.out.println(aClass.getPost("m6ie32r77ki5y0"));
		
		
	}

}

