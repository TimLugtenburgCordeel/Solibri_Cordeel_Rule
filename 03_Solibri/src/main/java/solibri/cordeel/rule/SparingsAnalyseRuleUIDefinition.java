package solibri.cordeel.rule;

import com.solibri.smc.api.checking.RuleResources;
import com.solibri.smc.api.ui.BorderType;
import com.solibri.smc.api.ui.UIComponent;
import com.solibri.smc.api.ui.UIContainer;
import com.solibri.smc.api.ui.UIContainerVertical;
import com.solibri.smc.api.ui.UILabel;
import com.solibri.smc.api.ui.UIRuleParameter;

class SparingsAnalyseRuleUIDefinition {
	
	private final SparingsAnalyseRule sparingsAnalyseRule;
	
	private final UIContainer uiDefinition;
	
	public SparingsAnalyseRuleUIDefinition(SparingsAnalyseRule sparingsAnalyseRule) {
		this.sparingsAnalyseRule = sparingsAnalyseRule;
		this.uiDefinition = createUIDefinition();
	}
	
	public UIContainer getDefinitionContainer() {
		return uiDefinition;
	}
	
	private UIContainer createUIDefinition() {
		
		RuleResources resources = RuleResources.of(sparingsAnalyseRule);
		
		UIContainer uiContainer = UIContainerVertical.create(resources.getString("UI.SparingsAnalyseRule.TITLE"), 
				BorderType.LINE);
		
		uiContainer.addComponent(UILabel.create(resources.getString("UI.SparingsAnalyseRule.DESCRIPTION")));
		
		uiContainer.addComponent(createFirstComponentFilterUIDefinition());
		
		uiContainer.addComponent(createSecondComponentFilterUIDefinition());
		
		uiContainer.addComponent(createThirtComponentFilterUIDefinition());
		
		return uiContainer;
	}
	
	private UIComponent createFirstComponentFilterUIDefinition() {
		UIContainer uiContainer = UIContainerVertical.create();
		uiContainer.addComponent(UIRuleParameter.create(sparingsAnalyseRule.rpComponentFilter));
		return uiContainer;
	}
	
	private UIComponent createSecondComponentFilterUIDefinition() {
		UIContainer uiContainer = UIContainerVertical.create();
		uiContainer.addComponent(UIRuleParameter.create(sparingsAnalyseRule.rpComponentFilterMEP));
		return uiContainer;
	}
	
	private UIComponent createThirtComponentFilterUIDefinition() {
		UIContainer uiContainer = UIContainerVertical.create();
		uiContainer.addComponent(UIRuleParameter.create(sparingsAnalyseRule.rpComponentFilterVoid));
		return uiContainer;
	}

}