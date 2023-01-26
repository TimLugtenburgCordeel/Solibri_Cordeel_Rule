package solibri.cordeel.rule;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import com.solibri.geometry.linearalgebra.Vector3d;
import com.solibri.smc.api.SMC;
import com.solibri.smc.api.checking.FilterParameter;
import com.solibri.smc.api.checking.OneByOneRule;
import com.solibri.smc.api.checking.Result;
import com.solibri.smc.api.checking.ResultFactory;
import com.solibri.smc.api.checking.RuleParameters;
import com.solibri.smc.api.filter.AABBIntersectionFilter;
import com.solibri.smc.api.filter.ComponentFilter;
import com.solibri.smc.api.ifc.IfcEntityType;
import com.solibri.smc.api.intersection.Intersection;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.ui.UIContainer;

public final class DuplicateCheckAutomated extends OneByOneRule {

	final FilterParameter rpComponentFilter = this.getDefaultFilterParameter();
	
	private final RuleParameters params = RuleParameters.of(this);
	
	// Create second filter to filter out component where a duplicate is accepted. 
	// Perhaps the element on the project base point. 
	
	final FilterParameter rpComponentFilterAccept = params.createFilter("rpComponentFilter2");
	
	private final DuplicateCheckAutomatedUIDefinition uiDefinition = new DuplicateCheckAutomatedUIDefinition(this);
	
	@Override
	public Collection<Result> check(Component component, ResultFactory resultFactory) {
		
		// Get another filter of the same elements. This will already find a duplicate.
		ComponentFilter components = rpComponentFilter.getValue();
		ComponentFilter componentfilter = AABBIntersectionFilter.ofComponentBounds(component).and(components);
		Collection<Component> elements = SMC.getModel().getComponents(componentfilter);
		
		// Get another filter of the same elements. This will already find a duplicate.
		ComponentFilter componentsAccepted = rpComponentFilterAccept.getValue();
		Collection<Component> elementsAccepted = SMC.getModel().getComponents(componentsAccepted);
	
		if (!checkAccept(component,elementsAccepted)) {
			for (Component element : elements ) {
				if(doublureCheck(component, element, resultFactory)){
					// Add standard report data for the report.
					String checkName = "Doublure";
					
					// Try to get bat id.
					Optional<String> componentIdOptional = component.getBATID();
					String componentId = "";
					if (componentIdOptional.isPresent()) {
						componentId = component.getBATID().get();
					}
					
					// Try to get bat id.
					Optional<String> elementIdOptional = element.getBATID();
					String elementId = "";
					if (elementIdOptional.isPresent()) {
						elementId = element.getBATID().get();
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
					report.add(componentId);
					
					String resultDescription = MessageFormat.format("Het component {0} is doublure 1 van de 2 verwijderen.",component.getName());
					String resultName = MessageFormat.format("Het component {0} is een doublure.",component.getName());
					String ruleName = MessageFormat.format( "{0} - Doublures.", disciplineShortName);
					return Collections.singleton(writeError( ruleName, resultName, resultDescription, report, resultFactory));
				}
			}
		}

		// Return the results.
		return Collections.emptyList();
		
	}
	
	private boolean checkAccept(Component component, Collection<Component> elementsAccepted) {
		for (Component target : elementsAccepted) {
			if (component.getGUID().equals(target.getGUID())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean doublureCheck(Component component, Component element, ResultFactory resultFactory) {
		
		// First check if it's not the same component in memory because when it is.
		// Its exact the same that not an duplicate.
		boolean doublureCheck = true;
		
		if (component.equals(element)) {
			return false;
		}
		
		// Check if there is an intersection this some volume. Because if the is no intersection its not a duplicate.
		Set<Intersection> intersections = component.getIntersections(element);
		for (Intersection intersection : intersections) {
			if (intersection.getVolume() > 0.0) {
				// There is an intersection step out of iteration and continue.
				doublureCheck = true;
				continue;
			} else {
				doublureCheck = false;
			}
		}
		
		if (doublureCheck) {
			// In this case we need to examen further. First compare the location and size of boundingbox.
			// The measurements are roundoff to human scale. 0.1 mm.
			
			if (!compareMeasurements(component, element)){
				// The measurements are not the same it's just an intersection.
				return false;
			}
		}else {
			return false;
		}
		
		// In this care the measurements are the same. Now we check the data.
		if (!compareData(component, element)) {
			// The data is not the same it's just an intersection.
			return false;
		}
		
		// Return the true it is an duplicate!!.
		return true;
	}
	
	private boolean compareMeasurements(Component component , Component element) {
		boolean check = false;
		
		// Get vector 3 for component and element
		Vector3d componentVector = component.getGlobalCoordinate().get();
		Vector3d elementVector = element.getGlobalCoordinate().get();
		
		if (
				doubleCompare(componentVector.getX(), elementVector.getX()) &&
				doubleCompare(componentVector.getY(), elementVector.getY()) &&
				doubleCompare(componentVector.getZ(), elementVector.getZ()) &&
				doubleCompare(component.getBoundingBoxHeight().getAsDouble(), element.getBoundingBoxHeight().getAsDouble()) &&
				doubleCompare(component.getBoundingBoxWidth().getAsDouble(), element.getBoundingBoxWidth().getAsDouble()) &&
				doubleCompare(component.getBoundingBoxLength().getAsDouble(), element.getBoundingBoxLength().getAsDouble())
			) {
			check = true;
		}	
		
		// Compare the double values.
		return check;
	}
	
	private boolean doubleCompare(double doub1, double doub2) {
		double doub1calc1 = doub1 * 10000;
		double doub1calc2 = doub2 * 10000;
		double doub1calcstep1 = Math.round(doub1calc1);
		double doub1calcstep2 = Math.round(doub1calc2);
		double doubmm1 = doub1calcstep1 / 10000;
		double doubmm2 = doub1calcstep2 / 10000;
		
		return Double.compare(doubmm1 ,doubmm2) == 0 ;
	}
	
	private boolean compareData(Component component , Component element) {
		boolean nameCheck = false;
		boolean IFCCheck = false;

		Optional<String> compName = component.getTypeName();
		Optional<String> elementName = element.getTypeName();
		
		if (compName.isPresent() && elementName.isPresent()) {
			if (compName.get().equals(elementName.get())) {
				nameCheck = true;
			}
		}

		Optional<IfcEntityType> compIFCEntitie = component.getIfcEntityType();
		Optional<IfcEntityType> elementIFCEntitie = element.getIfcEntityType();
		
		if (compIFCEntitie.isPresent() && elementIFCEntitie.isPresent()) {
			if (compIFCEntitie.get().equals(elementIFCEntitie.get())) {
				IFCCheck = true;
			}
		}
		
		if (nameCheck && IFCCheck) {
			return true;
		} else {
			return false;
		}
	}
	
	private Result writeError(String resultCategorieName, String resultName, String resultDescription ,  ArrayList<String> report, ResultFactory resultFactory) {
		
		// Add fail data to report
		report.add("false");
		report.add(resultDescription);
		
		// Create write report element and write the report
		WriteReport writeReport = new WriteReport(report);
		writeReport.writeToFile("C:\\_Karspeldreef\\14_XML_automation\\03_CSV\\211005_Karspeldreef_Doublure_check_log.csv" , "Datum/Tijd,Check,Poject nummer,Discipline,Model naam,Element Id, Element Id,Test geslaagd,Melding");
		
		// Create issue and return issue.
		IssueCreator issueCreator = new IssueCreator(resultCategorieName,resultCategorieName,resultName,resultDescription,resultFactory);
		return issueCreator.getIssue();
	}
	
	@Override
	public UIContainer getParametersUIDefinition() {
		return uiDefinition.getDefinitionContainer();
	}
	
}
	
	