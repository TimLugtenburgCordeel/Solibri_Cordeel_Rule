package solibri.cordeel.rule;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;

import com.solibri.smc.api.checking.FilterParameter;
import com.solibri.smc.api.checking.OneByOneRule;
import com.solibri.smc.api.checking.Result;
import com.solibri.smc.api.checking.ResultFactory;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.model.Property;
import com.solibri.smc.api.model.PropertySet;
import com.solibri.smc.api.ui.UIContainer;

public class DataCheckBasisILSAutomated extends OneByOneRule {
	
	final FilterParameter rpComponentFilter = this.getDefaultFilterParameter();

	private final DataCheckBasisILSAutomatedUIDefinition uiDefinition = new DataCheckBasisILSAutomatedUIDefinition(this);
	

	@Override
	public Collection<Result> check(Component component, ResultFactory resultFactory) {
		
		// Add standard report data for the report.
		String checkName = "BIM Basis ILS";
		
		// Try to get bat id.
		Optional<String> elementIdOptional = component.getBATID();
		String elementId = "";
		if (elementIdOptional.isPresent()) {
			elementId = component.getBATID().get();
		}
		
		// Get model information from the component.
		GetModelInfoComponent getModelInfoComponent = new GetModelInfoComponent(component);
		String modelName = getModelInfoComponent.getModelName();
		String projectId = getModelInfoComponent.getProject();
		String disciplineShortName = getModelInfoComponent.getDiscipline();
		
		// Add data to the report.
		ArrayList<String> report = new ArrayList<String>();
		report.add(checkName);
		report.add(projectId);
		report.add(disciplineShortName);
		report.add(modelName);
		report.add(elementId);
		
		// Check the property set
		PropertySet propertySetCordeel = checkPset(component, "Cordeel Element");
		
		if (propertySetCordeel == null) {
			// Property set is not present. Return error
			String resultDescription = MessageFormat.format("Het component {0} is niet voorzien van de propertieset 'Cordeel Element'. Graag de propertyset toevoegen bij het exporteren van de IFC.",component.getName());
			String resultName = MessageFormat.format("Het component {0} is niet voorzien van de propertieset 'Cordeel Element'.",component.getName());
			String ruleName = MessageFormat.format( "{0} - Geen propertyset Cordeel Element.", disciplineShortName);
			return Collections.singleton(writeError( ruleName, resultName, resultDescription, report, resultFactory));
		}
		
		String NLSFBValue = checkParam(component, propertySetCordeel, "00_NL-SfB code");
		
		if (NLSFBValue == "") {
			// NL-SFB code is not present on the component. Return error.
			String resultDescription = MessageFormat.format("Het component {0} is niet voorzien van NL-SfB codering. Deze code graag toevoegen en juist mappen in de propertyset 'Cordeel Element'.",component.getName());
			String resultName = MessageFormat.format("Het component {0} is niet voorzien van NL-SfB codering.",component.getName());
			String ruleName = MessageFormat.format( "{0} - NL-SfB codering is niet aanwezig op element.", disciplineShortName);
			return Collections.singleton(writeError( ruleName, resultName, resultDescription, report, resultFactory));
		}
		
		if (!checkNLSFBValue(NLSFBValue)) {
			// NL-SFB code is not present on the component. Return error.
			String resultDescription = MessageFormat.format("Het component {0} heeft niet viercijferige NL-SfB codering.",component.getName());
			String resultName = MessageFormat.format("Het component {0} is voorzien van NL-Sfb codering maar is niet viercijferig. Dit moet worden aangepast conform BIM basis ILS",component.getName());
			String ruleName = MessageFormat.format( "{0} - NL-SfB codering is niet vier cijferig.", disciplineShortName);
			return Collections.singleton(writeError( ruleName, resultName, resultDescription, report, resultFactory));
		}
		
		// Get the component basic data: type from Solibri & discipline.
		String componentType = component.getComponentType().name();
		String componentDiscipline = component.getDisciplineName().get();
		HashMap<String, List<String>> collection  = null;
			
		// First check the discipline of the model.
		if (componentDiscipline.equals("Architectural")) {
			collection = ReadCSV("C:\\_Karspeldreef\\14_XML_automation\\03_CSV\\Architectural - Cordeel - 1.00 - NL_SfB.csv");
		}else if(componentDiscipline.equals("Structural")) {
			// Check based on the Structural hashmap if the element is correct coded.
			collection = ReadCSV("C:\\_Karspeldreef\\14_XML_automation\\03_CSV\\Structural - Cordeel - 1.00 - NL_SfB.csv");
		}
		
		// Check values based on discipline.
		if (collection != null) {
			if(checkCompTypeAccept(componentType,collection)) {
				if (!checkCompValueAccept(componentType,collection,NLSFBValue)) {
					// IFC entitie is not accepted on ifc entitie.
					String resultDescription = MessageFormat.format("Het component {0} is voorzien van NL-Sfb codering maar de past niet bij de ifc entiteit.",component.getName());
					String resultName = MessageFormat.format("Het component {0} is op basis van IFC entiteit voorzien van verkeerde NL-SfB codering",component.getName());
					String ruleName = MessageFormat.format( "{0} - NL-SfB codering past niet bij IFC entiteit.", disciplineShortName);
					return Collections.singleton(writeError( ruleName, resultName, resultDescription, report, resultFactory));
				}
			} else {
				// IFC entitie is not accepted.
				String resultDescription = MessageFormat.format("Het component {0} is voorzien van een ifc entiteit dat niet is toegestaan. {1}",component.getName(), componentType);
				String resultName = MessageFormat.format("Het component {0} is niet voorzien van een onjuist ifc entiteit.",component.getName());
				String ruleName = MessageFormat.format( "{0} - Element is voorzien van onjuist Ifc Entiteit.", disciplineShortName);
				return Collections.singleton(writeError( ruleName, resultName, resultDescription, report, resultFactory));
			}	
		}
		
		return Collections.emptyList();
		
	}
	
	private HashMap<String, List<String>> ReadCSV(String filename) {
		
		HashMap<String, List<String>> collection = new HashMap<String, List<String>>();
		
		try {
			// Get the file and a scanner going
			File file = new File(filename);
			Scanner reader = new Scanner(file);
			
			// Read each line
			while (reader.hasNextLine()) {
				
				// Split line by csv ",".
				String[] data = reader.nextLine().split(",");
				String keyToSet = data[0];
				String dataToSet = data[1];
				
				// Get the key from the list. 
				List<String> stringList = collection.get(keyToSet);
				
				// Check if the key is present if not make key and value add to Hashmap
				if (stringList == null) {
					stringList = new ArrayList<String>();
					stringList.add(dataToSet);
					collection.put(keyToSet, stringList);
				} else {
					
				// If the key is present add value to the list.
					if(!stringList.contains(dataToSet)) {
						stringList.add(dataToSet);
					}
				}
			}
			
			// Close reader and return collection.
			reader.close();
			return collection;
			
			// If there is an error just return the empty collection.
		} catch (IOException e) {
			return collection;
		}
	}
	
	private PropertySet checkPset(Component component, String string) {
		// Create empty property set to collect the right set.
		PropertySet propertySetCordeel = null;
		
		// Get all the property sets from the component.
		Collection<PropertySet> componentPSets = component.getPropertySets();
		
		// Check all the property sets if the name equals the name we are looking for.
		// Just collect the set.
		for (PropertySet componentPSet : componentPSets) {
			if( componentPSet.getName().equals(string)) {
				propertySetCordeel = componentPSet;
			}
		}
		
		// Return property set if not found the property set is null.
		return propertySetCordeel;
	}
	
	private Result writeError(String resultCategorieName, String resultName, String resultDescription ,  ArrayList<String> report, ResultFactory resultFactory) {
		
		// Add fail data to report
		report.add("false");
		report.add(resultDescription);
		
		// Create write report element and write the report
		WriteReport writeReport = new WriteReport(report);
		writeReport.writeToFile("C:\\_Karspeldreef\\14_XML_automation\\03_CSV\\211005_Karspeldreef_Basis_ILS_check_log.csv" , "Datum/Tijd,Check,Poject nummer,Discipline,Model naam,Element Id,Test geslaagd,Melding");
		
		// Create issue and return issue.
		IssueCreator issueCreator = new IssueCreator(resultCategorieName,resultCategorieName,resultName,resultDescription,resultFactory);
		return issueCreator.getIssue();
	}
	
	private String checkParam(Component component, PropertySet propertyset, String string) {
		// Create empty property value to collect property value.
		String propertyValue = "";
		
		// Get all the properties from the property set
		Collection<Property<?>> properties = propertyset.getProperties();
		
		// If the name matches 
		for (Property<?> propertie : properties) {
			if (propertie.getName().trim().equals(string)) {
				propertyValue = propertie.getValueAsString();
			}
		}
		
		// Return property value if if the value is not found return nothing.
		return propertyValue;
	}
	
	private boolean checkNLSFBValue(String string) {
		// Create boolean false to return if check fails
		boolean check = false;
		
		// Check the length of the string. Check if it matches NLsfb patron. 
		if (string.length() == 5) {
			if (string.matches("[0-9]{2}[.][0-9]{2}")) {
				check = true;
			}
		}
		
		// Return check.
		return check;
	}
	
	private boolean checkCompTypeAccept(String componentType, HashMap<String, List<String>> hashMap ) {
		// Create boolean false to return if check fails
		boolean check = false;
		
		// Check if hashmap contains key
		String test = componentType.toLowerCase();
		if (hashMap.containsKey(test)) {
			check = true;
		}
		
		// Return check.
		return check;
	}
	
	private boolean checkCompValueAccept(String componentType, HashMap<String, List<String>> hashMap, String sfbValue ) {
		// Create boolean false to return if check fails
		boolean check = false;
		
		// Get the key values from hashmap and check if it begins with on of the accepted values.
		if (hashMap.containsKey(componentType.toLowerCase())) {
			for (String value : hashMap.get(componentType.toLowerCase())) {
				if (sfbValue.startsWith(value)) {
					check = true;
				}
			}
		}
		
		// Return check.
		return check;
	}
	
	@Override
	public UIContainer getParametersUIDefinition() {
		return uiDefinition.getDefinitionContainer();
	}
}