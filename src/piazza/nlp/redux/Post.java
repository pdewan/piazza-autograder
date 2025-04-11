package piazza.nlp.redux;

import java.util.List;
import java.util.Map;

public interface Post {

	public Map<String, Object> getAllData();
	public String getID();
	public List<String> getTags();

	// TODO
	
}
