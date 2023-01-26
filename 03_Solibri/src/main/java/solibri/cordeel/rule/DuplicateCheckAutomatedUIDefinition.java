package solibri.cordeel.rule;

import com.solibri.smc.api.checking.RuleResources;
import com.solibri.smc.api.ui.BorderType;
import com.solibri.smc.api.ui.UIComponent;
import com.solibri.smc.api.ui.UIContainer;
import com.solibri.smc.api.ui.UIContainerVertical;
import com.solibri.smc.api.ui.UILabel;
import com.solibri.smc.api.ui.UIRuleParameter;

class DuplicateCheckAutomatedUIDefinition {
	
	private final DuplicateCheckAutomated duplicateCheckAutomated;
	
	private final UIContainer uiDefinition;
	
	public DuplicateCheckAutomatedUIDefinition(DuplicateCheckAutomated duplicateCheckAutomated) {
		this.duplicateCheckAutomated = duplicateCheckAutomated;
		this.uiDefinition = createUIDefinition();
	}
	
	public UIContainer getDefinitionContainer() {
		return uiDefinition;
	}
	
	private UIContainer createUIDefinition() {
		
		RuleResources resources = RuleResources.of(duplicateCheckAutomated);
		
		UIContainer uiContainer = UIContainerVertical.create(resources.getString("UI.DuplicateCheckAutomated.TITLE"), 
				BorderType.LINE);
		
		uiContainer.addComponent(UILabel.create(resources.getString("UI.DuplicateCheckAutomated.DESCRIPTION")));
		
		uiContainer.addComponent(createFirstComponentFilterUIDefinition());
		
		uiContainer.addComponent(createSecondComponentFilterUIDefinition());
		
		return uiContainer;
	}
	
	private UIComponent createFirstComponentFilterUIDefinition() {
		UIContainer uiContainer = UIContainerVertical.create();
		uiContainer.addComponent(UIRuleParameter.create(duplicateCheckAutomated.rpComponentFilter));
		return uiContainer;
	}
	
	private UIComponent createSecondComponentFilterUIDefinition() {
		UIContainer uiContainer = UIContainerVertical.create();
		uiContainer.addComponent(UIRuleParameter.create(duplicateCheckAutomated.rpComponentFilterAccept));
		return uiContainer;
	}
}