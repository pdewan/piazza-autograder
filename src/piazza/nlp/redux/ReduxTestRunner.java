package piazza.nlp.redux;

import java.util.List;
import java.util.Map;

import piazza.APiazzaClass;
import piazza.nlp.redux.piazza.APiazzaForum;

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
	
		APiazzaForum pf = new APiazzaForum("TestPiazzaCourse", classID, email, password);
		//pf.getPost(53);
//		pf.getPost("m0mypf6bnkm4fc");
//		pf.getPost("m0qadc69kex6fj"); // to=m0qadc69kex6fj
//		pf.getFeed();
//		System.out.println(pf.getPost("m72prlqjhxgj5").getTags());

//		pf.searchPosts("null pointer");
		
		//System.out.println(aClass.getPost("m6ie32r77ki5y0"));

		//System.out.println(pf.getPost(175).getAllData());
		System.out.println(pf.getAllUsers().get(1).getID());
		System.out.println(pf.getUsers(new String[] {"jzk5vujhfp6pa", "ky4w3gvue3fbc"}));
		
	}

}

/*

curl --location 'https://us-east-1.aws.data.mongodb-api.com/app/rest-api-vsfoo/endpoint/add_log?db=studies&collection=dewan-eclipse' \
--header 'Content-Type: application/json' \
--data '{
    "body": {
      "password": "sYCUBa*shZKU4F-yxHrTk8D7FHo4xbBBV.-BK!-L",
      "course_id": "Dewan-Localcheck",
      "created_timestamp": "2023-10-03 13:28:35.389820",
      "log": {},
      "log_id": "test-id-two",
      "log_type": "Jupyter",
      "machine_id": "0x7800cc8a84a",
      "session_id": "None",
      "timestamp": "2023-10-17 11:33:49.416230"
    }
}'


curl --location 'https://us-east-1.aws.data.mongodb-api.com/app/rest-api-vsfoo/endpoint/add_log?db=studies&collection=piazza-post' \
--header 'Content-Type: application/json' \
--data '{
    "body": {
      "password": "sYCUBa*shZKU4F-yxHrTk8D7FHo4xbBBV.-BK!-L",
      "course_id": "Fall2025-Comp533",
      "created_timestamp": "2023-10-03 13:28:35.389820",
      "log": {
        "user_id": "sam-123",
        "post": []
      },
      "log_id": "piazza-post-1232144jj;k;jk",
      "log_type": "Piazza",
      "machine_id": "sam-pc",
      "session_id": "Exercise_5"
    }
}'

curl --location 'https://us-east-1.aws.data.mongodb-api.com/app/rest-api-vsfoo/endpoint/add_log?db=studies&collection=piazza-post' \
--header 'Content-Type: application/json' \
--data '{
    "body": {
      "password": "sYCUBa*shZKU4F-yxHrTk8D7FHo4xbBBV.-BK!-L",
      "course_id": "Mediated-Agent-Test",
      "log": {
        "user_id": "USER_ID",
        "post": POST_JSON
      },
      "log_id": "POST_ID",
      "log_type": "Piazza",
      "machine_id": "PIAZZA_USER_EMAIL",
      "session_id": "Exercise_5"
    }
}'


don't unpack JSON


log_id is primary key (can use piazza ID)
log_type -> Piazza
machine_id -> user id or anonymous
session_id -> exercise? or whatever
timestamps are optional
can be creative with log:
log:
	user_id:
	user_name:
	post_number:
make password an environment var

send sam a quick email to see if it was added

when you use API addPost with same log_id, will update in-place
	log will be overwritten
	can just put full JSON as log

*/