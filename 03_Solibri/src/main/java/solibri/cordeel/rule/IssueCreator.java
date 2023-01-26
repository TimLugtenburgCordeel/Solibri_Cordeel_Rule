package solibri.cordeel.rule;


import com.solibri.smc.api.checking.Result;
import com.solibri.smc.api.checking.ResultCategory;
import com.solibri.smc.api.checking.ResultFactory;

public class IssueCreator {
	private Result result;
	
	public IssueCreator(String categorieName, String categorieDescription, String issueName, String issueDescription, ResultFactory resultFactory) {
		
		// Create category for issue
		ResultCategory baseCategorie = resultFactory.createCategory("Solibri API clash rule", "Solibri API clash rule");
		ResultCategory statusError = resultFactory.createCategory(categorieName, categorieDescription, baseCategorie );
		
		// Create result
		result = resultFactory.create(issueName, issueDescription).withCategory(statusError);
	}
	
	// Return the result
	public Result getIssue() {
		return result;
	}
}