//package gapModels;
//
//import geometry.Polygons;
//import interpolations.Piecewise;
//
//public class ForskaCalculator {
//
//
//	/** Prentice and Leemans 1990 (FORSKA) Equation 2: Light extinction at canopy depth.
//	 *  
//	 * @param i0 is the amount of photosynthetically active ratiation (PAR) above the canopy.  Default value is 400 micromols * m-2 * s-1 (Prentice and Leemans, 1990)
//	 * @param k extinction coefficient (0.4 by default in Prentice and Leemans, 1990)
//	 * @param leafAreaIndexAboveZ accumulated LAI above the canopy at depth Z.
//	 * @return estimate of light intensity in micromols * m-2 * s-1	 */
//	public static double forskaLightIntensityAtDepth(
//			double i0, double k, double leafAreaIndexAboveZ)
//	{
//		return i0 * Math.exp(-k * leafAreaIndexAboveZ);
//	}
//
//	/** Prentice and Leemans 1990 (FORSKA) Equation 2: light intensities at the
//	 *  ceilings of each cylinder layer
//	 * @param leafAreaIndexAboveDepths the leaf area index density contained in each slice
//	 * @param extinctionCoefficient 
//	 * @param canopyLightIntensity light intensity at the top of the canopy, usually 400 micromols m-2 s-1
//	 * @return a vector of light intensities for each slice of the cylinder */
//	public static double[] forskaLightIntensitiesDepthsCylinder(
//			double[] leafAreaIndexAboveDepths,
//			double extinctionCoefficient, double canopyLightIntensity)
//	{
//		double[] out = new double[leafAreaIndexAboveDepths.length];
//		double sum = 0d;
//		for(int i = 0; i < leafAreaIndexAboveDepths.length; i++){
//			sum += leafAreaIndexAboveDepths[i];
//			out[i] = forskaLightIntensityAtDepth(
//					canopyLightIntensity, extinctionCoefficient, sum);
//		}
//		return out;
//	}
//
//	/** Prentice and Leemans 1990 (FORSKA) Equation 3: assimilation reduction due to canopy depth<br><br> 
//	 * @param lightIntensityZ the light intensity available at canopy depth Z
//	 * @param extinctionCoefficient extinction coefficient (0.4 by default in Prentice and Leemans, 1990)
//	 * @param lightCompensationPoint the light intensity at which energy gained from photosynthesis balances energy lost via respiration for the species.
//	 * @param halfSaturationPoint the light intensity required for the photosynthetic rate to be half of the rate when light-saturated. 
//	 * @return A factor by which to reduce the potential assimilation via photosynthesis */
//	public static double forskaCylinderLightResponse(
//			double lightIntensityZ, double extinctionCoefficient, 
//			double lightCompensationPoint, double halfSaturationPoint)
//	{
//		double diff = lightIntensityZ - lightCompensationPoint;
//		return diff / (diff + halfSaturationPoint);
//	}
//
//	/** Prentice and Leemans 1990 (FORSKA) Equation 3a: assimilation reductions for cylinder layers
//	 * @param lightIntensities the light intensities, in microMol per square meter per second at the
//	 * 			ceilings of each cylinder layer.
//	 * @param extinctionCoefficient
//	 * @param lightCompensationPoint
//	 * @param halfSaturationPoint
//	 * @return A 1D array1a of light responses at the ceilings of each cylinder layer */
//	public static double[] forskaCylinderLightResponseLayers(
//			double[] lightIntensities, double extinctionCoefficient, 
//			double lightCompensationPoint, double halfSaturationPoint)
//	{
//		double[] out = new double[lightIntensities.length + 1];
//		double term1 = 0;
//
//		/* Evaluate the integral as a sum of parallelograms. */
//		for(int i = 0; i < lightIntensities.length + 1; i++){
//			term1 = lightIntensities[i] - lightCompensationPoint;
//			out[i] = term1 / (term1 + halfSaturationPoint);
//		}
//
//		return out;
//	}
//
//	/** Prentice and Leemans 1990 (FORSKA) Equation 4 - allometric asymptotic height from dbh. <br><br>
//	 *  
//	 *  This is the equation used in FORSKA (Leemans and Prentice, 1987; Meyer, 1950; Prentice and Leemans, 1990)
//	 * @param b1 breast height (usually 1.37 meters - 1.3 in Leemans and Prentice 1987)
//	 * @param maxHeight The maximum possible height, in meters, for the species
//	 * @param shape A shape parameter for how rapidly the height approaches the maximum height for the species.  <br>It is estimated
//	 * 			as (initial rate of increase in height when dbh = 0) / (maxHeight - b1) in Prentice and Leemans 1990 (FORSKA).
//	 * @param dbh The current dbh for the tree.
//	 * @return estimated height in meters.	 */
//	public static double forskaAllometricMitscherlichHeight(double b1, double maxHeight, double shape, double dbh){
//		double adjHeight = maxHeight - b1;
//		return b1 + (adjHeight) * (1d - Math.exp(-shape * dbh / adjHeight));
//	}
//
//	/** Prentice and Leemans 1990 (FORSKA) Equation 5a: sapwood maintenance cost for the tree canopy <br><br>
//	 * 
//	 * @param height current tree height
//	 * @param boleHeight height of the bole (stem height below the start of the canopy);
//	 * @param sapwoodMaintenanceCost in cm+2 m-2 year-1
//	 * @return cost	 */
//	public static double forskaAllometricMaintenenceCost(double height, double boleHeight, double sapwoodMaintenanceCost){
//		/* The sapwood maintenance cost is the definite integral of a linear
//		 * function of cost factor and height evaluated between the bole height
//		 * and the total tree height, which we can evaluate exactly: */
//
//		/* If maintenence were constant: */
//		double maintenanceCost = sapwoodMaintenanceCost * (height - boleHeight);
//
//
//
//		return maintenanceCost;
//	}
//
//	/** Prentice and Leemans 1990 (FORSKA) Equation 5: volume increment under ideal conditions <br><br>
//	 *  Integral converted to a sum with bin width z
//	 * 
//	 * @param verticalLeafAreaDensity a vector of the leaf area densities for each slice
//	 * @param potentialNetAssimilationInLayers a vector of the light potential net assimilation rates for each slice
//	 * @param growthScalingFactor in cm2 m-1 year-1
//	 * @param sapwoodMaintenanceCost in cm2 m-2 year-1
//	 * @return 
//	 * 
//	 * TODO unit tests
//	 */
//	public static double forskaAllometricMaxVolumeGrowthCylinder(
//			double height, double boleHeight,
//			double verticalLeafAreaDensity,  
//			double[] potentialNetAssimilationInLayers, 
//			double growthScalingFactor, double sapwoodMaintenanceCost)
//	{
//
//		/* The sapwood maintenance cost is the definite integral of a linear
//		 * function of cost factor and height evaluated between the bole height
//		 * and the total tree height, which we can evaluate exactly: */
//		double maintenenceCost = forskaAllometricMaintenenceCost(height, boleHeight, sapwoodMaintenanceCost); 
//
//		//				0.5 * sapwoodMaintenanceCost * 
//		//				(Math.pow(height, 2d) - Math.pow(boleHeight, 2d));
//
//		/* The net assimilation for the canopy layers is evaluated as a sum. */
//		double assimilationSum = 0d;
//		for(double layer : potentialNetAssimilationInLayers){
//			assimilationSum += layer;
//		}
//		return verticalLeafAreaDensity * (growthScalingFactor * assimilationSum - maintenenceCost);
//	}
//
//
//
//	/** Prentice and Leemans 1990 (FORSKA) Equation 6: allometric biomass of a tree
//	 * 
//	 * @param beta 0.03 kg cm-2 m -1 (Hytteborn 1975)
//	 * @param dbh
//	 * @param height
//	 * @return 
//	 */
//	public static double forskaAllometricTreeBiomass(double beta, double dbh, double height){
//		return beta * dbh * dbh * height;
//	}
//
//	/** Prentice and Leemans 1990 (FORSKA) Equation 7: leaf area increase as function of dbh
//	 * 
//	 * @param initialLeafAreaBasalAreaSlope
//	 * @param dbh
//	 * @param dbhIncrement
//	 * @param sapwoodTurnoverRate
//	 * @param leafArea
//	 * @return 
//	 */
//	public static double forskaAllometricLeafAreaIncrement(
//			double initialLeafAreaBasalAreaSlope, 
//			double dbh, double dbhIncrement, 
//			double sapwoodTurnoverRate, double leafArea)
//	{
//		return 2d * initialLeafAreaBasalAreaSlope * dbh * dbhIncrement - 
//				sapwoodTurnoverRate * leafArea;
//	}
//
//
//
//	/** Lidner et al. 1997 (FORSKA) Equation 5: diameter increment under ideal conditions
//	 * 
//	 * @param dbh current dbh
//	 * @param height current height
//	 * @param boleHeight height at which the crown begins
//	 * @param shape A shape parameter for how rapidly the height approaches the maximum height for the species for new saplings (initial rate of increase in height when dbh = 0) / (maxHeight - b1) in Prentice and Leemans 1990 (FORSKA).
//	 * @param maxHeight The maximum possible height, in meters, for the species
//	 * @param b1 breast height (usually 1.37 meters - 1.3 in Leemans and Prentice 1987)
//	 * @param verticalLeafAreaDensity vertical density of leaf area in m2 m-1
//	 * @param potentialNetAssimilationLayers vector of relative potential assimilation for the canopy layers occupied by the tree
//	 * @param growthScalingFactor in cm2 m-1 year-1
//	 * @param sapwoodMaintenenceCost in cm2 m-2 year-1
//	 * @return 
//	 * */
//	public static double forskaAllometricMaxDiameterIncrementCylinder(
//			double dbh, double height, double boleHeight,
//			double shape, double maxHeight, double b1,
//			double verticalLeafAreaDensity, 
//			double[] potentialNetAssimilationLayers, 
//			double growthScalingFactor, double sapwoodMaintenanceCost)
//	{
//		double volIncrement = forskaAllometricMaxVolumeGrowthCylinder(
//				height, boleHeight, verticalLeafAreaDensity, 
//				potentialNetAssimilationLayers, 
//				growthScalingFactor, sapwoodMaintenanceCost);
//
//		double fH = forskaAllometricMitscherlichHeightIncrement(b1, maxHeight, shape, dbh);
//
//		return volIncrement / (dbh * (2d * height + dbh * fH));
//	}
//
//	/** Lidner et al. 1997 (FORSKA) Equation 8: Allometric asymptotic height increment from dbh
//	 * @param b1 breast height (usually 1.37 meters - 1.3 in Leemans and Prentice 1987)
//	 * @param maxHeight The maximum possible height, in meters, for the species
//	 * @param shape A shape parameter for how rapidly the height approaches the maximum height for the species.  <br>It is estimated
//	 * 			as (initial rate of increase in height when dbh = 0) / (maxHeight - b1) in Prentice and Leemans 1990 (FORSKA).
//	 * @param dbh The current dbh for the tree.
//	 * @return estimated increment in meters */
//	public static double forskaAllometricMitscherlichHeightIncrement(
//			double b1, double maxHeight, double shape, double dbh)
//	{
//		double adjHeight = maxHeight - b1;
//		return shape * Math.exp(-shape * dbh / adjHeight);
//	}
//
////	/** TODO write description */
////	public static double logisticShadingInhibition(double slope, double midpoint, double basalArea){
////		return 1d / (1d + 
////				Math.exp(-slope * (basalArea - midpoint)));
////	}
//
//	/** Approximate the integral of light response through the crown given
//	 *  the light availabilities of each layer
//	 * 
//	 * @param lightResponses
//	 * @param integrationInterval
//	 * @param maxHeight
//	 * @param height
//	 * @param boleHeight
//	 * @return
//	 */
//	public static double forskaNetLightAssimilationLayers(
//			double[] lightResponses, double integrationInterval, 
//			double maxHeight, double height, double boleHeight)
//	{
//		double assimilationSum = 0d;
//		double deltaZ = integrationInterval;
//
//		double currentCeiling = maxHeight;
//		double currentFloor = maxHeight - integrationInterval;
//
//		double riemannSumLayer = 0d;
//		double lightResponseLayerTop = 0d;
//		double lightResponseLayerBottom = 0d;
//
//		/* Five possible scenarios within a layer:
//		 * 	1:	No part of the crown is in the layer.
//		 * 	2:	The top of the crown is above the top of the layer 
//		 * 			and the bole height is below the bottom of the layer.
//		 * 			Probably the most common scenario.
//		 * 	3:	The crown height is above the top of the layer
//		 * 			and the bole height is within the layer.
//		 * 	4:	The top of the crown is within the layer
//		 * 			and the bole height is below the bottom of the layer.
//		 * 	5:	The top of the crown and the bole height are within the layer. */
//
//		boolean crownAboveCeiling;
//		boolean crownAboveFloor;
//		boolean boleAboveFloor;
//		boolean boleBelowFloor;
//
//		for(int i = 0; i < lightResponses.length - 1; i++){
//			currentCeiling = maxHeight - integrationInterval * i;
//			currentFloor = currentCeiling - integrationInterval;
//
//			deltaZ = integrationInterval;
//
//			riemannSumLayer = 0d;
//
//			lightResponseLayerTop = lightResponses[i];
//			lightResponseLayerBottom = lightResponses[i + 1];
//
//			crownAboveCeiling = height >= currentCeiling;
//			crownAboveFloor = height > currentFloor;
//			boleAboveFloor = boleHeight >= currentFloor;
//			boleBelowFloor = boleHeight < currentFloor;
//
//			/* If the top of the crown is below the current floor, we ignore the layer. */
//			if(crownAboveFloor & (boleHeight < currentCeiling)){
//				if(crownAboveCeiling)
//				{
//
//					/* Scenario 2: No change needed*/
//
//					/* Scenario 3: */
//					if(boleAboveFloor)
//					{
//						lightResponseLayerBottom = Piecewise.linearInterpolation(
//								boleHeight, 
//								currentCeiling, lightResponses[i], 
//								currentFloor, lightResponses[i + 1]); 
////								UtilityCalculator.lineUnknownPoint(
////								currentCeiling, lightResponses[i], 
////								currentFloor, lightResponses[i + 1], 
////								boleHeight);
//						deltaZ = currentCeiling - boleHeight;
//					}
//				} else
//				{
//					lightResponseLayerTop = Piecewise.linearInterpolation(
//							height, 
//							currentCeiling, 
//							lightResponses[i], 
//							currentFloor, lightResponses[i + 1]); 
////					UtilityCalculator.lineUnknownPoint(
////							currentCeiling, lightResponses[i], 
////							currentFloor, lightResponses[i + 1], 
////							height);
//
//					/* Scenario 4: */
//					if(boleBelowFloor)
//					{
//						deltaZ = height - currentFloor;
//					} else
//
//						/* Scenario 5: */
//					{
//						lightResponseLayerBottom = Piecewise.linearInterpolation(
//								boleHeight, 
//								currentCeiling, lightResponses[i], 
//								currentFloor, lightResponses[i + 1]); 
////						UtilityCalculator.lineUnknownPoint(
////								currentCeiling, lightResponses[i], 
////								currentFloor, lightResponses[i + 1], 
////								boleHeight);
//						deltaZ = height - boleHeight;
//					}
//				}
//			} /* End scenarios. */
//
//			riemannSumLayer = Polygons.parallelogramArea(
//					lightResponseLayerTop, lightResponseLayerBottom, deltaZ);
//			assimilationSum += riemannSumLayer;
//		}
//		return assimilationSum;
//	}
//	
//}
