package piazza;

import java.util.List;
import java.util.Map;

public interface IndividualGrade {
	public List<String> getSummaryStringRepresentation();
	public List<List<String>> getDetailedStringRepresentation();
	public Map<String, String> getMapRepresentation();
}
