package solibri.cordeel.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.solibri.smc.api.SMC;
import com.solibri.smc.api.checking.FilterParameter;
import com.solibri.smc.api.checking.OneByOneRule;
import com.solibri.smc.api.checking.Result;
import com.solibri.smc.api.checking.ResultCategory;
import com.solibri.smc.api.checking.ResultFactory;
import com.solibri.smc.api.checking.RuleParameters;
import com.solibri.smc.api.filter.AABBIntersectionFilter;
import com.solibri.smc.api.filter.ComponentFilter;
import com.solibri.smc.api.intersection.Intersection;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.ui.UIContainer;

public final class SparingsAnalyseRule extends OneByOneRule {
	
	/*
	 * Invoke this class to add basic rule parameters
	 */
	private final RuleParameters params = RuleParameters.of(this);

	final FilterParameter rpComponentFilter = this.getDefaultFilterParameter();
	
	/*
	 * Create 2 filter parameters 1 for filtering MEP elements and 1 for filtering void elements
	 */

	final FilterParameter rpComponentFilterMEP = params.createFilter("rpComponentFilter2");

	final FilterParameter rpComponentFilterVoid = params.createFilter("rpComponentFilter3");

	private final SparingsAnalyseRuleUIDefinition uiDefinition = new SparingsAnalyseRuleUIDefinition(this);

	@Override
	public Collection<Result> check(Component component, ResultFactory resultFactory) {

		/*
		 * Create the component filter to only filter the MEP that clash with the walls
		 * and only the voids that clash with the walls. This way the detailed check will be efficient
		 */
		
		ComponentFilter mepsCheck = rpComponentFilterMEP.getValue();
		ComponentFilter mepsComponentFilter = AABBIntersectionFilter.ofComponentBounds(component).and(mepsCheck);
		Collection<Component> meps = SMC.getModel().getComponents(mepsComponentFilter);
		
		ComponentFilter voidsCheck = rpComponentFilterVoid.getValue();
		ComponentFilter voidsComponentFilter = AABBIntersectionFilter.ofComponentBounds(component).and(voidsCheck);
		Collection<Component> voids = SMC.getModel().getComponents(voidsComponentFilter);
		
		/*
		 * Create variables to set in the checking iteration
		 */
		
		boolean isClash = true;
		boolean isVoid = false;
		boolean correctStatus = false;
		double volumeComponentMep = 0.0;
		Collection<Result> results = new ArrayList<>();
		ArrayList<Component> voidCollection = new ArrayList<>();
		
		/*
		 * Start iterating the MEPs check if there is an collision with the current element perhaps a wall. 
		 * If there is an collision it could be an clash and set variable to is Clash true.
		 * We should also get the volume of the clash. So we can check the volume of the collision of the void.
		 */
		
		for (Component mep : meps) {
			
			/*
			 * If there is an intersection we get the volume of the intersection if there is no intersection
			 * the volume will be 0.0.
			 */
			voidCollection = new ArrayList<>();
			volumeComponentMep = getVolumeComponentMep(component, mep);
	
			if(volumeComponentMep > 0.0) {
				/*
				 * If there is a collision it could be a clash and set variable to is Clash true. 
				 * And start iterating the voids and collect the voids that intersect with the MEP. 
				 * And the intersection is big enough to be correct
				 */
				isClash = true;
				isVoid = false;
				for (Component _void : voids) {
					if(checkMEPVoid(mep, _void, volumeComponentMep)) {
							
						/*
						 * Now we have only the clashes left that are intersecting all wall, MEP and void with a some kind of volume. This is not jet a clash.
						 * So set is clash to false. And check the status of the void. If the status of the void is not approved it is an clash.
						 */
						isClash = false;
						isVoid = true;
						voidCollection.add(_void);
					}
				}
			}
			
			/*
			 * If there is an clash and no void we report the clash
			 */
			if (isClash) {
				results.addAll(clashCheck(component, mep, resultFactory));
			}else if(!isClash & isVoid){
				/*
				 * If there is a void check the status of all the voids. If one has the correct status there is nothing to report.
				 */
				
				correctStatus = false;
				for (Component _void : voidCollection) {
					if(_void.getObjectType().toString().contains(":Goedgekeurd")) {
						correctStatus = true;
					}
				}
				if (!correctStatus) {
					results.addAll(statusCheck(component, mep, voidCollection, resultFactory));
				}
			}
		}

		return results;
	}

	@Override
	public UIContainer getParametersUIDefinition() {
		return uiDefinition.getDefinitionContainer();
	}
	
	/*
	 * This method will return the volume of the intersection if the is an intersection and else it will return 0.0
	 */

	private double getVolumeComponentMep(Component component, Component mep ){
		
		double returnValue = 0.0;
		
		Set<Intersection> intersections = component.getIntersections(mep);
		
		for (Intersection intersection : intersections) {
			if (!intersection.isEmpty()) {
				
				returnValue = intersection.getVolume();
			}
		}
		return returnValue;
	}
	
	private boolean checkMEPVoid(Component source, Component taget, double volumeComponentMep) {
		
		Set<Intersection> intersections = source.getIntersections(taget);
		
		for (Intersection intersection : intersections) {
			if (intersection.getVolume() > (volumeComponentMep * 0.9) ) {
				return true;
			}
		}
			
		return false;
	}
	
	private Collection<Result> clashCheck(Component component, Component mep, ResultFactory resultFactory) {

		final double transparency = 0.5;

		Collection<Result> results = new ArrayList<>();

		Set<Intersection> intersections = component.getIntersections(mep);

		for (Intersection intersection : intersections) {
			if (intersection.isEmpty()) {
				continue;
			} else {
				String name = splitAndGetName(component) + " & " + splitAndGetName(mep) + " geen sparing.";
				String description = "Dit gaat niet helemaal goed " + component.getName() + " en " + mep.getName() + " clashen en er is geen sparing opgegeven.";
				
				ResultCategory baseCategorie = resultFactory.createCategory("Solibri API clash rule", "Solibri API clash rule");
				ResultCategory geenSparing = resultFactory.createCategory("Sparing ontbreekt", "Er is geen sparing opgegeven.", baseCategorie );

				Result result = resultFactory
					.create(name, description)
					.withInvolvedComponent(component)
					.withCategory(geenSparing)
					.withVisualization(visualization -> {
						visualization.addComponent(component, transparency);
						visualization.addComponent(mep, transparency);
					});

				results.add(result);
			}
		}

		return results;
	}

	private Collection<Result> statusCheck(Component component, Component mep, ArrayList<Component> voids, ResultFactory resultFactory) {

		final double transparency = 0.5;

		Collection<Result> results = new ArrayList<>();

		Set<Intersection> intersections = component.getIntersections(mep);

		for (Intersection intersection : intersections) {
			if (intersection.isEmpty()) {
				continue;
			} else {
				String stringNames = "";
				for (Component _void : voids) {
					stringNames += splitAndGetName(_void);
					stringNames += " , ";
				}
				String name = splitAndGetName(component) + " & " + splitAndGetName(mep) + " status error : " + stringNames;
				String description = "Dit gaat niet helemaal goed " + component.getName() + " en " + mep.getName() + " en " + voids.get(0).getName();
				
				ResultCategory baseCategorie = resultFactory.createCategory("Solibri API clash rule", "Solibri API clash rule");
				ResultCategory statusError = resultFactory.createCategory("Sparing Error", "Sparing heeft de verkeerde status.", baseCategorie );

				Result result = resultFactory
					.create(name, description)
					.withInvolvedComponent(component)
					.withCategory(statusError)
					.withVisualization(visualization -> {
						visualization.addComponent(component, transparency);
						visualization.addComponent(mep, transparency);
						for (Component _void : voids) {
							visualization.addComponent(_void, transparency);
						}
					});

				results.add(result);
			}
		}
		return results;
	}
	
	private String splitAndGetName(Component component) {
		String returnString = component.getName().split("\\)")[1];
		return returnString;
	}
	
}