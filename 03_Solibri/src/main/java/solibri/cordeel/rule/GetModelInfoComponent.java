package solibri.cordeel.rule;

import com.solibri.smc.api.model.Component;

public class GetModelInfoComponent{
	private String discipline;
	private String project;
	private String modelName;
	
	public GetModelInfoComponent(Component component){
		modelName = component.getModel().getName();
		if (modelName.contains("-")) {
			String[] split = modelName.split("-");
			
			discipline = split[1];
			project = split[0];
		} else {
			discipline = "XXX";
			project = modelName;
		}
	}
	
	public String getDiscipline() {
		return discipline;
	}
	
	public String getProject() {
		return project;
	}
	
	public String getModelName() {
		return modelName;
	}
}