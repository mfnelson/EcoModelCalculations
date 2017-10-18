/**
 * 
 */
package forClim;

/** A collection of calculations relating to the water submodel of ForClim. <br>
 *  Equations are mostly adaptations of calculations in the Water.cs source
 *  for ForClim v.3.  Code line references refer to that file.
 * @author michaelfrancenelson
 *
 */
public class ForClimWaterCalculator {

	/*************************************************************************
	 * 
	 * The following methods are adapted from
	 * Update() in Water.cs
	 * 
	 *************************************************************************/
	
	/** Adapted from code in Water.cs Update(), lines 213 - 216
	 * 
	 * @param k1 heat index multiplier
	 * @param umT vector of actual monthly mean temperatures
	 * @param k2 heat index exponent
	 * @return kHi a heat index for the year
	 */
	public static double heatIndex(double k1, double[] umT, double k2){
		double kHi = 0d;
		for (int m = 0; m < 12; m++) kHi += Math.pow(Math.max(0.0, k1 * umT[m]), k2);
		return kHi;
	}
	
	/** Adapted from code in Water.cs Update(), line 217
	 * @param k3 potential evapotranspiration coefficient
	 * @param k4 potential evapotranspiration coefficient
	 * @param k5 potential evapotranspiration coefficient
	 * @param k6 potential evapotranspiration coefficient
	 * @return kC empirical exponent value
	 */
	public static double empiricalExponentValue(double kHi, double k2, double k3, double k4, double k5, double k6){
		return ((k3 * kHi + k4) * kHi + k5) * kHi + k6;
	}
	
	
	
	/*************************************************************************
	 * 
	 * The following methods are adapted from
	 * Output() in Water.cs
	 * 
	 *************************************************************************/
	
	
	/** ForClim v3.0 equation for drought index 
	 * 
	 * Adapted from code in Water.cs Output(), lines 231 - 251
	 *  
	 * @param uT monthly mean temperatures
	 * @param kDTT the base temperature, above which degree days start to accumulate
	 * @param uDr monthly drought indices
	 * @param startMonth first month of the active season (January = 0).
	 * @param endMonth last month of the active season (December = 11).
	 * @return uDr yearly drought index between 0 and 1
	 */
	public static double droughtIndex(double[] uT, double kDTT, double[] uDr, int startMonth, int endMonth){
		int count = 0;
		double uDrAn = 0d;
		for(int m = startMonth; m < endMonth; m++){
			if(uT[m] > kDTT){
				count++;
				uDrAn += uDr[m];
			}
		}
		if(count != 0) return uDrAn / count; else return 0d;
	}

	/**
	 * 
	 * Adapted from code in Water.cs Output(), lines 253 - 261
	 * 
	 * @param uAET vector of monthly actual evapotranspiration values
	 * @param startMonth first month of the active season (January = 0).
	 * @param endMonth last month of the active season (December = 11).
	 * @return  uAET yearly sum of actual evapotranspiration
	 */
	public static double actualEvapotranspiration(double[] uAET, int startMonth, int endMonth){
		double uAETOut = 0d;
		for(int m = startMonth; m < endMonth; m++){
			uAETOut += uAET[m];
		}
		return uAETOut;
	}
	
	
	/*************************************************************************
	 * 
	 * The following methods are adapted from
	 * UpdateAuxillaryVariables in Water.cs
	 * 
	 *************************************************************************/
	
	/** ForClim v3.0.0 equation 3.5 <br>
	 * 
	 *  Adapted from Water.cs UpdateAuxilliaryVariables() Line 287
	 * 
	 * 	Maximum potential evapotranspiration given temperature conditions
	 * @param kPM Potential evapotranspiration multiplier
	 * @param kPMod Fractional PET change with incident solar radiation
	 * @param kLatPtr correction factor for sun angle and day length
	 * @param kHi heat index
	 * @param tM long term monthly mean temperatures
	 * @param kC 'an empirical function' //TODO check out the Bugmann and Cramer 1998 reference
	 * @return gPET the potential amount of water lost to evapotranspiration in a cell (in mm???)
	 */
	public static double potentialEvaptrans(double kPM, double kPMod, double kLatPtr, 
			double kHi, double tM, double kC){
		return kPM * kPMod * kLatPtr * Math.pow(10d * Math.max(tM, 0d) / kHi, kC);
	}
	
	/** ForClimv3.0 helper function for the water submodel
	 * 
	 *  Adapted from Water.cs UpdateAuxilliaryVariables() Line 288
	 *  
	 * @param kIcpt fraction of intercepted precipitation
	 * @param uP precipitation in cm for the month
	 * @param gPET potential evapotranspiration for the month //TODO What are the units?
	 * @return gPi intercepted precipitation. //TODO What are the units? */
	public static double interceptedPrecip(double kIcpt, double uP, double gPET){
		return Math.min(kIcpt * uP, gPET);
	}

	/**
	 * 
	 * Adapted from Water.cs UpdateAuxilliaryVariables() Line 289
	 * 
	 * @param uP precipitation in cm for the month
	 * @param gPi intercepted precipitation //TODO what are the units?
	 * @return gPs infiltrated precipitation //TODO what are the units
	 */
	public static double infiltratedPrecipitation(double uP, double gPi){
		return uP - gPi;
	}
	
	
	/** ForClim v3.0 adjustment to the water supply function.
	 * 
	 * Adapted from Water.cs UpdateAuxilliaryVariables() Line 290
	 * 
	 * 	This equation differs from equation 3.2 and is the
	 *  equation that appears in the C# code with the comment:
	 *  "correction of drought index initiated by Paul Henne"
	 * @param kBS bucket size in centimeters
	 * @param kCw maximum evapotranspiration from saturated soil under high demand in cm
	 * @param sM monthly soil moisture in mm
	 * @return gS soil water supply */
	public static double waterSupplyBucket(double kBS, double kCw, double sM){
		return Math.min(kBS,  kCw) * sM / kBS;
	}
	

	/** ForClim v3.0.0 equation 3.4
	 * 
	 * Adapted from Water.cs UpdateAuxilliaryVariables() Line 291
	 * 
	 * @param gPET potential evapotranspiration
	 * @param gPi intercepted precipitation
	 * @return gD evaporative demand from the soil
	 */
	public static double evaporativeDemand(double gPET, double gPi){
		return gPET - gPi;
	}

	
	/** ForClim v3.0 manual equation 3.1
	 * 
	 * Adapted from Water.cs UpdateAuxilliaryVariables() Line 292
	 * 
	 * @param gS supply of water from the soil
	 * @param gD evaporative demand from the soil
	 * @return gE soil evapotranspiration
	 */
	public static double monthlyEvapotrans(double gS, double gD){
		return Math.min(gS, gD);
	}

	
	/** ForClim v3.0 modification of manual equation 3.3
	 * 	
	 * Adapted from Water.cs UpdateAuxilliaryVariables() Line 298
	 * 
	 * As modified in the C# code with the comment:
	 * "Avoiding negative soil moistures"
	 * @param sM monthly soil moisture in mm
	 * @param gPs infiltrating precipitation
	 * @param gE evapotranspiration
	 * @param kBS bucket size (cm)
	 * @return sM soil moisture in mm for the next month
	 */
	public static double waterBalanceBuckets(double sM, double gPs, double gE, double kBS){
		return Math.max(0, Math.min(sM + gPs - gE, kBS));
	}
	
	/** ForClim v. 3.0 helper function for equations 3.6, 3.7
	 * 
	 * Adapted from Water.cs UpdateAuxilliaryVariables() Line 305
	 * 
	 * @param gE monthly evapotranspiration
	 * @param gD monthly evaporative demand
	 * @return uDr drought index for the month
	 */
	public static double droughtIndexMonth(double gE, double gD){
		if(gD != 0) return 1d - gE / gD; else return 0;
	}

	
	

	
	
	/* TODO working here.  Need to reconcile all of the equations 
	 * in this class with corresponding C# code. */


	
	

}
