package solibri.cordeel.rule;

import com.solibri.smc.api.checking.RuleResources;
import com.solibri.smc.api.ui.BorderType;
import com.solibri.smc.api.ui.UIComponent;
import com.solibri.smc.api.ui.UIContainer;
import com.solibri.smc.api.ui.UIContainerVertical;
import com.solibri.smc.api.ui.UILabel;
import com.solibri.smc.api.ui.UIRuleParameter;

class DataCheckBasisILSAutomatedUIDefinition {
	
	private final DataCheckBasisILSAutomated dataCheckBasisILSAutomated;
	
	private final UIContainer uiDefinition;
	
	public DataCheckBasisILSAutomatedUIDefinition(DataCheckBasisILSAutomated dataCheckBasisILSAutomated) {
		this.dataCheckBasisILSAutomated = dataCheckBasisILSAutomated;
		this.uiDefinition = createUIDefinition();
	}
	
	public UIContainer getDefinitionContainer() {
		return uiDefinition;
	}
	
	private UIContainer createUIDefinition() {
		
		RuleResources resources = RuleResources.of(dataCheckBasisILSAutomated);
		
		UIContainer uiContainer = UIContainerVertical.create(resources.getString("UI.DataCheckBasisILSAutomated.TITLE"), BorderType.LINE);
		
		uiContainer.addComponent(UILabel.create(resources.getString("UI.DataCheckBasisILSAutomated.DESCRIPTION")));
		
		uiContainer.addComponent(createFirstComponentFilterUIDefinition());
		
		return uiContainer;
	}
	
	private UIComponent createFirstComponentFilterUIDefinition() {
		UIContainer uiContainer = UIContainerVertical.create();
		uiContainer.addComponent(UIRuleParameter.create(dataCheckBasisILSAutomated.rpComponentFilter));
		return uiContainer;
	}
}