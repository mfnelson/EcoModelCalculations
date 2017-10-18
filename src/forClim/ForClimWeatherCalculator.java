/**
 * 
 */
package forClim;

/**
 * @author michaelfrancenelson
 *
 */
public class ForClimWeatherCalculator {

	

	
	/*************************************************************************
	 * 
	 * The following methods are adapted from
	 * Update() in Weather.cs
	 * 
	 *************************************************************************/
	
	//TODO implement the climate change code
	
	//anomalies in case of climate change
    double CC_mTsp;    // change in temperature of spring months (mar-may) to have occured at CC_time
    double CC_mTs;    // change in temperature of summer months (jun-aug) to have occured at CC_time
    double CC_mTf;    // change in temperature of fall months (sep-nov) to have occured at CC_time
    double CC_mTw;    // change in temperature of winter months (dec-feb) to have occured at CC_time

    double CC_mPsp;    //mean % change in precipitation of spring months (mar-may) to have occured at CC_time
    double CC_mPs;    //mean % change in precipitation of summer months (jun-aug) to have occured at CC_time
    double CC_mPf;    //mean % change in precipitation of fall months (sep-nov) to have occured at CC_time
    double CC_mPw;    //mean % change in precipitation of winter months (dec-feb) to have occured at CC_time    

    double CC_sdTsp;    //change in stDev of temperature of spring months (mar-may) to have occured at CC_time
    double CC_sdTs;    //change in stDev of temperature of summer months (jun-aug) to have occured at CC_time
    double CC_sdTf;    //change in stDev of temperature of fall months (sep-nov) to have occured at CC_time
    double CC_sdTw;    //change in stDev of temperature of winter months (dec-feb) to have occured at CC_time

    double CC_sdPsp;    //% change in stDev of precipitation of spring months (mar-may) to have occured at CC_time
    double CC_sdPs;    //% change in stDev of precipitation of summer months (jun-aug) to have occured at CC_time
    double CC_sdPf;    //% change in stDev of precipitation of fall months (sep-nov) to have occured at CC_time
    double CC_sdPw;    //% change in stDev of precipitation of winter months (dec-feb) to have occured at CC_time  

    double CC_rTPsp;    //cross-correlation of spring months (mar-may) to have occured at CC_time, if different from rTP
    double CC_rTPs;  //cross-correlation of summer months (jun-aug) to have occured at CC_time
    double CC_rTPf; //cross-correlation of fall months (sep-nov) to have occured at CC_time
    double CC_rTPw;    //cross-correlation of winter months (dec-feb) to have occured at CC_time, if different from rTP  

	
	
	/*************************************************************************
	 * 
	 * The following methods are adapted from
	 * Output() in Weather.cs
	 * 
	 *************************************************************************/


	/** ForClim v 3.0.0 equations 2.2 and 2.3
	 *  From Weather.cs Output(), lines 538 - 540
	 *  
	 * @param t monthly mean temperature
	 * @param kDays number of days in the month
	 * @param kTDD correction factor for estimating degree days from monthly data
	 * @return
	 */
	public static double degreeDaysMonth(double t, double kTDD, double kDays){
		if(t > kTDD) return Math.max(0d, t - kTDD) * kDays + degreeDaysCorrected(t);
		else return 0d;
	}

	/** ForClim v. 3.0.0 equation 2.2
	 *  Adapted from rom Weather.cs Output() Lines 542 - 550
	 *  
	 * @param tM vector of monthly mean temperatures
	 * @param k threshold temperature
	 * @param kDays mean number of days in month
	 * @param tCorrections correction factors for estimation of degree days from monthly mean data
	 * @return uDDAn approximate annual degree days above a given threshold temperature
	 */
	public static double degreeDaysAnnual(double[] tM, double k, double kDays){
		return degreeDaysSeason(tM, k, kDays, 0, 11);
	}

	/** ForClim v. 3.0.0 equation 2.3
	 * 
	 *  Adapted from rom Weather.cs Output() Lines 542 - 550
	 *  
	 * @param tM vector of monthly mean temperatures
	 * @param k threshold temperature
	 * @param kDays mean number of days in month
	 * @param tCorrections correction factors for estimation of degree days from monthly mean data
	 * @param startMonth first month of the season (January = 0)
	 * @param endMonth last month of the season
	 * @return uDDSe approximate annual degree days above a given threshold temperature
	 */
	public static double degreeDaysSeason(double[] tM, double k, double kDays, int startMonth, int endMonth){
		double sum = 0;
		for(int i = startMonth; i <= endMonth; i++){
			if(tM[i] > k){
				sum += degreeDaysMonth(tM[i], k, kDays);
			}
		}
		return sum;
	}

	
	
	/*************************************************************************
	 * 
	 * Auxilliary methods in Weather.cs
	 * 
	 *************************************************************************/
	
	
	/** ForClim v.3.0 helper function for equations 2.2 and 2.3
	 * 
	 *  From Weather.cs, lines 571 - 584
	 *	//TODO Look up info on this in Bugman 1994 
	 *
	 * @param t temperature
	 * @return gCorr degree day correction value
	 */
	public static double degreeDaysCorrected(double t){
        double gCorr;  //Degree day correction value

        if (t <= 5.5)
            gCorr = 8.52 * Math.pow(10, 0.165 * t);
        else if (5.5 <= t & t <= 15.5)
            gCorr = 187.2 * Math.pow(10, -0.0908 * t);
        else
            gCorr = -31.8 + 2.377 * t;

        return gCorr;
	}


	
	
	
}
