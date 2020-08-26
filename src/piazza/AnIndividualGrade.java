package piazza;

import java.util.List;
import java.util.Map;

public class AnIndividualGrade implements IndividualGrade {

	private List<String> summaryString;
	private List<List<String>> detailedString;
	private Map<String, String> map;
	
	public AnIndividualGrade(List<String> summaryString, List<List<String>> detailedString, Map<String, String> map) {
		this.summaryString = summaryString;
		this.detailedString = detailedString;
		this.map = map;
	}
	
	@Override
	public List<String> getSummaryStringRepresentation() {
		return summaryString;
	}

	@Override
	public List<List<String>> getDetailedStringRepresentation() {
		return detailedString;
	}

	@Override
	public Map<String, String> getMapRepresentation() {
		return map;
	}
}
