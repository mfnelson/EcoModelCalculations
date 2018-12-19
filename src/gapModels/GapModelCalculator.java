package gapModels;

/** A collection of equations used in many of the gap models of forest tree
 * population dynamics and succession.<br><br>
 * 
 * References:<br><ul>
 * <li> Botkin, D. B., J. F. Janak, and J. R. Wallis. 1972a. Rationale, limitations, and assumptions of a northeastern forest growth simulator. IBM Journal of Research and Development 16:101â€“116. </li>
 * <li> Botkin, D. B., J. F. Janak, and J. R. Wallis. 1972b. Some Ecological Consequences of a Computer Model of Forest Growth. Journal of Ecology 60:849â€“872.</li>
 * <LI> Ker, J. W., and J. H. G. Smith. “Advantages of the Parabolic Expression of Height-Diameter Relationships.” The Forestry Chronicle 31, no. 3 (September 1, 1955): 236–46. https://doi.org/10.5558/tfc31236-3. </li>
 * <li> Leemans, R., and I. C. Prentice. 1987. Description and Simulation of Tree-Layer Composition and Size Distributions in a Primaeval Picea-Pinus Forest. Vegetatio 69:147â€“156. </li>
 * <li> Lindner, M., R. SievÃ¤nen, and H. Pretzsch. 1997. Improving the simulation of stand structure in a forest gap model. Forest Ecology and Management 95:183â€“195. </li>
 * <li> Meyer, H. A. “A Mathematical Expression for Height Curves.” Journal of Forestry 38 (1940): 415–20. </li>
 * <li> Moore, A. D. 1989. On the maximum growth equation used in forest gap simulation models. Ecological Modelling 45:63â€“67. </li>
 * <li> Prentice, I. C., and R. Leemans. 1990. Pattern and Process and the Dynamics of Forest Structure: A Simulation Approach. Journal of Ecology 78:340â€“355. </li>
 * </ul>

 * @author michaelfrancenelson
 *
 */
public class GapModelCalculator {

	
	
	
	
	
	private static double basalAreaConstant = Math.PI * 1E-4;


	
	/** The equation used in the fortran code to increment the stand biomass.
	 * 
	 * @param dbh the tree's dbh.
	 * @param biomassParamA value in fortran code: 0.1193
	 * @param biomassParamB value in fortran code: 2.393
	 * @return biomass of a tree
	 */
	public static double foretStandBiomassFortran(double dbh, double biomassParamA, double biomassParamB)
	{
		return biomassParamA * Math.pow(dbh, biomassParamB);
	}
	
	
	/** An intermediate in the growth increment calculations in the fortran code. 
	 * TODO: I know I've seen this equation elsewhere, need to find the reference for it...*/
	public static double foretFortranGR(double b1_centimeters, double b2, double b3)
	{ 
		double b1 = b1_centimeters;
		double term1 = b1 + 0.25 * b2 * b2 / b3;
		double term2 = 0.5 * b2 / b3;
		return term1 * term2;
//				
//		double quot = 1.0 / b3;
//		double gr = (b1_centimeters + 0.25 * Math.pow(b2, 2.0) / b3) * (0.5 * b2 / b3); 
//		return gr;
	}
	
	/** TODO: I know I've seen this equation elsewhere, need to find the reference for it...*/
	public static double foretFortranSizePenalty(double b1_centimeters, double dbh, double b2, double b3, double gr)
	{
		double b1 = b1_centimeters;
		double dbhB2 = b2 * dbh;
		double dbh2 = dbh * dbh;
		double dbh3 = dbh2 * dbh;
		double dbhSqB3 = b3 * dbh * dbh;
		double numerator = 1.0 -(b1 * dbh + b2 * dbh2 - b3 * dbh3) / gr;
		double denominator = 2 * b1 + 3.0 * dbhB2 - 4.0 * dbhSqB3;
		return 1.0 - numerator / denominator;
	}
	
	public static double foretFortranMaxDBHIncrement(double g, double dbh, double b1_centimeters, double b2, double b3)
	{
		return g * dbh * foretFortranSizePenalty(
				b1_centimeters, dbh, b2, b3, 
				foretFortranGR(b1_centimeters, b2, b3));
	}
	
	/** Formula form the fortran code. <br>
	 * TODO: 
	 * @param b2
	 * @param b3
	 * @param dbh
	 * @return height (in meters?)
	 */
	public static double foretHeightFortran(double b2, double b3, double dbh)
	{
		return dbh * (b2 - b3 * dbh) / 100.0 + 1.0;
	}
	
	
	/**  TODO equation from sollins et al 1973 as referenced for table 1 in Shugart and west 1977
	 * 
	 * @param intercept
	 * @param slope
	 * @param dbh
	 * @return
	 */
	public static double allometricTreeBiomassSollins(double intercept, double slope, double dbh){
		return intercept + slope * dbh;
	}
	
	
	/** Allometric parabolic height<br>
	 * <br> Botkin et al 1972 equation 2
	 * <br> Kerr and Smith 1955<br>
	 * NOTE:  Units are critical, b1 must be in meters and dbh must be in cm.
	 * @param b1 breast height in m (considered 1.37 meters in most cases)
	 * @param b2 estimated parameter
	 * @param b3 estimated parameter
	 * @param dbh current diameter of the individual tree in centimeters.
	 * @return estimated height in meters	 */
	public static double allometricPolynomialHeight(double b1, double b2, double b3, double dbh)
	{
		return b1 + dbh * (b2 - b3 * dbh) / 100d;
	}

	/** Prentice and Leemans 1990 (FORSKA) Equation 4 - allometric asymptotic height from dbh. <br>
	 * Leemans and Prentice 1987<br>Prentice and Leemans 1990<br>Meyer 1940<br>
	 * @param b1 breast height (usually 1.37 meters - 1.3 in Leemans and Prentice 1987)
	 * @param maxHeight The maximum possible height, in meters, for the species
	 * @param shape A shape parameter for how rapidly the height approaches the maximum height for the species.  <br>It is estimated
	 * 			as (initial rate of increase in height when dbh = 0) / (maxHeight - b1) in Prentice and Leemans 1990 (FORSKA).
	 * @param dbh The current dbh for the tree.
	 * @return estimated height in meters.	 */
	public static double allometricAsymptoticHeight(double b1, double maxHeight, double shape, double dbh){
		double adjHeight = maxHeight - b1;
		return b1 + (adjHeight) * (1d - Math.exp(-shape * dbh / adjHeight));
	}
	
	/** TODO write description */
	public static double logisticShadingInhibition(double slope, double midpoint, double basalArea){
		return 1d / (1d + Math.exp(-slope * (basalArea - midpoint)));
	}

	/** Botkin et al. 1972 (JaBoWa) Equation 5: allometric maximum dbh increment <br><br>
	 * NOTE:  Units are critical here because the parameters b2, and b3 are scaled
	 * so that dbh and dbhMax are given in centimeters while height and heightMax are in meters.<br>
	 * 
	 * @param g species-specific growth parameter
	 * @param dbh current tree diameter at breast height in cm
	 * @param dbhMax maximum possible dbh for the tree species in cm
	 * @param heightMax maximum possible height for the tree species in meters
	 * @param b1_meters breast height in meters (usually 1.37)
	 * @param b2 allometric parameter
	 * @param b3 allometric parameter
	 * @return maximum potential diameter growth increment in cm,
	 * 			without environmental limitations or competition. */
	public static double allometricMaxDiameterIncrementDiskJabowa(
			double g, 
			double dbh, double height,
			double dbhMax, double heightMax, 
			double b1_meters, double b2, double b3)
	{
		return  (g * dbh) * 
				(1d - (dbh * height) / (dbhMax * heightMax)) /
				(20 * b1_meters + 3d * b2 * dbh - 4d * b3 * Math.pow(dbh, 2d));
	}
	
	/** Moore (1989) Equation 7: 
	 * 
	 * @param g species-specific growth parameter
	 * @param dbh current tree diameter at breast height in cm
	 * @param heightMax maximum possible height for the tree species in meters
	 * @param b1_meters breast height in meters (usually 1.37)
	 * @param b2 allometric parameter
	 * @param b3 allometric parameter
	 * @return */
	public static double allometricMaxDiameterIncrementDiskMoore(
			double g, 
			double dbh, double height,
			double heightMax, 
			double b1_meters, double b2, double b3)
	{
		return  (g * dbh) * 
				(1d - (height / heightMax)) /
				(20 * b1_meters + 3d * b2 * dbh - 4d * b3 * Math.pow(dbh, 2d));
	}

	/** Botkin et al. 1972b (JaBoWa) Equation A4: calculation of growth parameter G. <br>
	 *  G is chosen so that the tree reaches about 2/3 its maximum dbh at half of maxAge
	 * @param ageMax in years
	 * @param dbhMax in cm
	 * @param heightMax in meters
	 * @return g growth parameter (cm / year) */
	public static double growthParameterG_Jabowa(int ageMax, double dbhMax, double heightMax)
	{
		heightMax *= 100.0;
		double a = 1.0 - 137.0 / heightMax;
		double aHalf = a / 2.0;
		double aSq = Math.pow(a, 2.0);
		double sqrt1 = Math.sqrt(aSq + 4.0 * a);
		double dSq = Math.pow(dbhMax, 2.0);
		
		double term1 = 4.0 * heightMax / (double) ageMax;
		
		double term2 = Math.log(4.0 * dbhMax - 2.0);
		
		double term3 = aHalf * Math.log( (9.0/4.0 + aHalf) / (4.0 * dSq + 2.0 * a * dbhMax - a));
		double term4 = (a + aSq / 2.0) / sqrt1; 
		
		double term5a = 3.0 + a - sqrt1;
		double term5b = 3.0 + a + sqrt1;
		
		double term6a = 4.0 * dbhMax + a + sqrt1;
		double term6b = 4.0 * dbhMax + a - sqrt1;

		double term5 = term5a / term5b;
		double term6 = term6a / term6b;

		return term1 * (term2 + term3 - term4 * Math.log(term5 * term6));
	}
	
	/** Approximation of basal area for a tree assuming a circular stem.
	 * @param dbh_cm diameter at breast height (in cm).
	 * @return basal area in square meters. */
	public static double basalArea(double dbh_cm)
	{
//		double sqCmPerSqM = 10000.0;
//		double sqMperSqCm = 1E-4;
		return basalAreaConstant * Math.pow(dbh_cm * 0.5, 2d);
	}
	
	/** The yearly mortality rate required for a given percentage of trees to survive a specified number of years.
	 *  <br> Botkin et al. 1972 (JaBoWa) Equation 12a 
	 * @param survivalPercent What percentage of trees should survive during the period?
	 * @param survivalPeriodLength How long is the interval for which annual mortality is calculated. */
	public static double yearlyExponentialSurvivalRate(double survivalPercent, int survivalPeriodLength){
		return Math.exp(Math.log(survivalPercent) / survivalPeriodLength);
	}

	/** The yearly survival rate required for a given percentage of trees to survive a specified number of years.
	 *  <br> Botkin et al. 1972 (JaBoWa) Equation 12a 
	 * @param survivalPercent What percentage of trees should survive during the period?
	 * @param survivalPeriodLength How long is the interval for which annual mortality is calculated. */
	public static double yearlyExponentialMortalityRate(double survivalPercent, int survivalPeriodLength){
		return 1.0 - yearlyExponentialSurvivalRate(survivalPercent, survivalPeriodLength);
	}

	/** Botkin et al. 1972 (JaBoWa) Equation 3a: Allometric parameter b2 for calculation of height from dbh. <br><br>
	 * NOTE:  the choice of units here is critical: centimeters for max dbh and meters for max height.
	 * @param b1 breast height in meters (considered 1.37 meters in most cases)
	 * @param heightMax maximum possible height for the tree species in meters
	 * @param dbhMax maximum possible dbh for the species in cm
	 * @return b2 */
	public static double allometricHeightParameterB2(
			double b1, double heightMax, double dbhMax)
	{
		return 200.0 * (heightMax - b1) / dbhMax;
	}

	/** Botkin et al. 1972 (JaBoWa) Equation 3b: allometric parameter b3 for calculation of height from dbh. <br><br>
	 * NOTE:  the choice of units here is critical: centimeters for max dbh and meters for max height.
	 * @param b1 breast height in meters (considered 1.37 meters in most cases)
	 * @param heightMax maximum possible height for the tree species in meters
	 * @param dbhMax maximum possible dbh for the species in cm
	 * @return b3 */
	public static double allometricHeightParameterB3(
			double b1, double heightMax, double dbhMax){
		return 100.0 * (heightMax - b1) / Math.pow(dbhMax, 2.0);
	}
	
	/** Botkin et al. 1972 (JaBoWa) Equation 8: light response curve<br><br>
	 * c1 &times (1 - e<sup>-c2 &times (AL - c3)</sup>) <br>
	 * NOTE:  The original JaBoWa specification allowed light response to be greater than 1.
	 * @param availableLight
	 * @param c1 light response constant 1, originally 1.0 (2.24) for shade tolerant (intolerant) plants
	 * @param c2 light response constant 2, originally 4.64 (1.136) for shade tolerant (intolerant) plants
	 * @param c3 light response constant 2, originally 0.05 (0.08) for shade tolerant (intolerant) plants
	 * @return photosynthetic rate	 */
	public static double exponentialLightResponseCurve(double availableLight, double c1, double c2, double c3){
		double response = c1 * (1.0 - Math.exp(-c2 * (availableLight - c3)));
		return Math.max(0d, response);
	}
	
	/** Botkin et al. 1972 (JaBoWa) Equation 10: parabolic annual temperature effect on growth.
	 * @param speciesDegreeDaysMin the minimum degree days required by this species
	 * @param speciesDegreeDaysMax the maximum degree days tolerated by this species 
	 * @param currentYearDegreeDays the degree days occurring in this year;
	 * @return a scalar for the temperature effect on growth between 1 (no effect) 
	 * 			to 0 (total inhibition)	 */
	public static double parabolicDegreeDayTemperatureEffect(double speciesDegreeDaysMin, double speciesDegreeDaysMax, double currentYearDegreeDays) {
		return Math.max(0d, 4.0 * (currentYearDegreeDays - speciesDegreeDaysMin) * 
				(speciesDegreeDaysMax - currentYearDegreeDays) /
				Math.pow(speciesDegreeDaysMax - speciesDegreeDaysMin, 2.0));
	}
	
	/** Leaf area as calculated in Jabowa and FORET <br>
	 * 
	 *  Jabowa used k = 45.  Foret used k = 1. <br>
	 *  Foret used leafAreaParamA = 1.9283295E-4, leafAreaParamB = 2.129. <br>
	 *  Jabowa used leafAreaParamB = 2.0. <br>
	 * @param dbh
	 * @param leafAreaParamA  Jabowa called this parameter 'c'.
	 * @param leafAreaParamB 
	 * @param k scalar factor to convert leaf weight to projected leaf area. The original JaBoWa model used 45.
	 * @return	 */
	public static double allometricLeafArea(double dbh, double leafAreaParamA, double leafAreaParamB, double k)	{
		return leafAreaParamA * Math.pow(dbh, leafAreaParamB) / k;
	}
	
	/** Botkin et al. 1972 (JaBoWa) Equation 11: simple linear crowding growth reduction.  
	 * @param currentBasalArea is the sum of the basal areas of all trees on the patch.
	 * @param maxBasalArea is the maximum allowed basal area on the patch.
	 * @return a decrement factor for growth between 0 (totally crowde.0) and 1 (totally empty)*/
	public static double linearCrowdingCompetition(double currentBasalArea, double maxBasalArea) {
		return Math.max(0.0,  Math.min(1.0, 1.0 - currentBasalArea / maxBasalArea));
	}

	public static double logisticCrowdingCompetition(double currentBasalArea, double maxBasalArea, double steepness, double midpoint)
	{
		double x = Math.min(1.0, currentBasalArea / maxBasalArea);
		return 1 - 1.0 / (1.0 + Math.exp(-steepness * (x - midpoint)));
	}
	
	
	/** Botkin et al. 1972 (JaBoWa) Equation 7: The 'disk' model for light availability.<br><br>
	 * 	Derived from the Beer-Lambert law
	 *  LA = &phi; &times e<sup>-k &times SLA</sup>
	 * @param phi annual insolation in appropriate units, 1 by default in original JaBoWa
	 * @param k tuning parameter, 1/6000 in original JaBoWa
	 * @param sla the sum of the leaf areas of all trees taller than this one
	 * @return an estimate of light available to the tree for photosynthesis. */
	public static double diskLightAvailability(double phi, double k, double sla){
		return phi * Math.exp(-k * sla);
	}
	
	/** Botkin et al. 1972 (JaBoWa) Equation 9: a site's degree days. <br><br>
	 * NOTE: The return value is in the same units as the arguments. <br>
	 * NOTE: All arguments must be in the same units (degrees C or F).
	 * @param julyMeanTemp
	 * @param januaryMeanTemp
	 * @param baseTemp
	 * @return the site's degree day estimate	 */
	public static double jaBoWaDegreeDaysTwoPointEstimate(double julyMeanTemp, double januaryMeanTemp, double baseTemp){

		double tDiff = baseTemp - (julyMeanTemp + januaryMeanTemp) / 2d;
		double diff  = julyMeanTemp - januaryMeanTemp;
		double term1 = diff / (2.0 * Math.PI);
		double term2 = tDiff / 2d;
		double term3 = Math.pow(tDiff, 2) / (Math.PI * diff);

		return 365.0 * (term1 - term2 + term3);
	}

	/**
	 * @param elev1 elevation of the base station
	 * @param elev2 elevation of the cell
	 * @param rate rate is change in degrees per 1000 of whatever units elev1 and elev2 are in.
	 * @return
	 */
	public static double jaBoWaTemperatureDecrement(double elev1, double elev2, double rate){
		return (elev2 - elev1) * rate;
	}

	/** Calculate a temperature for a site given its difference in elevation
	 * from a base station, the base station's temperature, and a rate of change
	 * per 1000 meters.
	 * @param baseTemp temperature at the base station
	 * @param elevationDifference difference in elevation between base station and site
	 * @param lapseRate rate at which temperature changes (in degrees C)
	 *  per 1000 meter increase in elevation
	 * @return
	 */
	public static double jaBoWaLapseTemperature(double baseTemp, double elevationDifference, double lapseRate){
		return baseTemp - lapseRate * (elevationDifference / 1000.0);
	}
}
