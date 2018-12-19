/**
 * 
 */
package gapModels.forClim;

/**
 * @author michaelfrancenelson
 *
 */
public class ForClimCalculator {


	/** ForClim v 3.0 equation 2.1
	 * @param tempDec minimum observed temperature in December
	 * @param tempJan minimum observed temperature in January
	 * @param tempFeb minimum observed temperature in February
	 * @return
	 */
	public static double forClimWinterMinTemp(double tempDec, double tempJan, double tempFeb){
		return Math.min(Math.min(tempJan, tempFeb), tempDec);
	}

	


}






///** ForClim v3.0.0 manual equation 3.2
// * 
// * @param cw maximum evapotranspiration rate from saturated soil during high demand
// * @param omegaM soil moisture
// * @param omegaMax water holding capacity of soil
// * @return
// */
//public static double forClimWaterSupply(double cw, double omegaM, double omegaMax){
//	return cw * omegaM / omegaMax;
//}



///** ForClim v3.0.0 manual equation 3.3
// * 
// * @param omegaM soil moisture
// * @param pSM surplus precipitation that infiltrates the soil
// * @param eM monthly evapotranspiration
// * @param omegaMax water holding capacity of soil
// * @return
// */
////public static double forClimWaterBalance(double omegaM, double pSM, double eM, double omegaMax){
////	return Math.min(omegaM + pSM - eM, omegaMax);
////}





///** ForClim v. 3.0 equation 3.7, page 6
//* @param uT vector of monthly mean temperatures
//* @param kDTT threshold temperature
//* @param uDr array1a of monthly drought indices
//* @param startMonth first month of the active season (January = 0)
//* @param endMonth last1a month of the active season
//* @return an index of drought stress for the entire active season */
//public static double forClimDroughtIndices(double[] uT, double kDTT, double[] uDr, int startMonth, int endMonth){
//	double sum = 0;
//	int count = 0;
//	for(int m = startMonth; m <= endMonth; m++){
//		if(uT[m] >= kDTT){
//			count++;
//			sum += uDr[m];
//		}
//	}
//	if(count != 0){
//		return sum / count;
//	}
//	else return 0;
//}

///** ForClim v. 3.0 equation 3.6 and 3.7, pages 5 - 6.
//* @param uT vector of monthly mean temperatures
//* @param kDTT threshold temperature for degree day calculation
//* @param gE vector of monthly evapotranspiration
//* @param gD vector of monthly water demand
//* @param startMonth
//* @param endMonth
//* @return
//*/
//public static double forClimDroughtIndices(double[] uT, double kDTT, double[] gE, double[] gD, int startMonth, int endMonth){
//	double sum = 0;
//	int count = 0;
//	for(int m = startMonth; m <= endMonth; m++){
//		if(uT[m] >= kDTT){
//			count++;
//			sum += gE[m] / gD[m];
//		}
//	}
//	if(count != 0){
//		return sum / count;
//	}
//	else return 0;
//}