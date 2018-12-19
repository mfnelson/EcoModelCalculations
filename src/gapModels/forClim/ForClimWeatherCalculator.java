/**
 * 
 */
package gapModels.forClim;

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

	/** Create a simulated monthly mean temperature
	 * 
	 * Weather.cs method Update() line 336
	 * 
	 * @param referenceTemp long-term monthly mean temperature
	 * @param randVector a 2-vector consisting of two independently-sampled standard Normal variates
	 * @param eigenvectorCovarTemp the temperature column of the column matrix of eigenvectors of the covariance matrix for temperature and precip
	 * @return A simulated monthly mean temperature
	 */
	public static double monthlyMeanTemp(double referenceTemp, double[] randVector, double[] eigenvectorCovarTemp){
		return referenceTemp + randVector[0] * eigenvectorCovarTemp[0] + randVector[1] * eigenvectorCovarTemp[1];
	}

	/** Create a simulated monthly precipitation sum
	 * 
	 * Weather.cs method Update() lines 337-338
	 * 
	 * @param referencePrecip long-term monthly mean precipitation sum
	 * @param randVector a 2-vector consisting of two independently-sampled standard Normal variates
	 * @param eigenvectorCovarPrecip the precipitation column of the column matrix of eigenvectors of the covariance matrix for temperature and precip
	 * @return A simulated value for the total monthly precipitation
	 */
	public static double monthlyMeanPrecip(double referencePrecip, double[] randVector, double[] eigenvectorCovarPrecip){
		// ForClim V.2.9.3: log-normal distribution for precipitation
		return Math.max(0d, Math.exp(referencePrecip + randVector[0] * eigenvectorCovarPrecip[0] + randVector[1] * eigenvectorCovarPrecip[1]) - 1.0);
	}


	/** ForClim v 3.0.0 equations 2.2 and 2.3
	 *  From Weather.cs Output(), lines 538 - 540
	 *  
	 * @param t monthly mean temperature
	 * @param kDays number of days in the month
	 * @param kDTT Development threshold temperature
	 * @return
	 */
	public static double degreeDaysMonth(double t, double kDTT, double kDays){
		return Math.max(0d, t - kDTT) * kDays + degreeDaysCorrected(t);
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
	 * @param endMonth last1a month of the season
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


	public static class Matrix {
		//------------------------------------------------------------
		//Create and allocate a symmetric 2x2 matrix A with 
		//components a=A[0,0], b=A[1,1], c=A[0,1]=A[1,0]
		public static double[][] create(double a, double b, double c){
			double[][] A = new double[2][2];
			A[0][0] = a;
			A[1][1] = b;
			A[0][1] = c;
			A[1][0] = c;

			return A;
		}

		//------------------------------------------------------------
		//Vector of Eigenvalues are obtained 
		//with one elementary Givens-Rotation
		public static double[] eigenVal(double[][] A){
			double a = A[0][0];
			double b = A[1][1];
			double c = A[0][1];

			//Rotation angle of principal axes
			double theta = Math.atan2(2 * c, a - b) / 2;
			double cos = Math.cos(theta);
			double sin = Math.sin(theta);

			//Eigenvalues derived from rotation angle
			double[] k = new double[2];
			k[0] = sin * sin * b + cos * cos * a + 2 * sin * cos * c;
			k[1] = sin * sin * a + cos * cos * b - 2 * sin * cos * c;

			return k;
		}
		//------------------------------------------------------------
		//Column matrix of normalized Eigenvectors is obtained 
		//with one elementary Givens-Rotation
		public static double[][] EigenVect(double[][] A)
		{
			double a = A[0][0];
			double b = A[1][1];
			double c = A[0][1];

			//Rotation angle of principal axes
			double theta = Math.atan2(2 * c, a - b) / 2;
			double cos = Math.cos(theta);
			double sin = Math.sin(theta);

			//Normalised eigenvectors
			double[][] V = new double[2][2];
			V[0][0] = cos;
			V[1][0] = sin;
			V[0][1] = -sin;
			V[1][1] = cos;

			return V;
		}
		//------------------------------------------------------------
		//The column matrix of de-normalized Eigenvectors
		//i.e. the orthonormal Eigenvectors multiplied with
		//the square root of the corresonding Eigenvalues
		public static double[][] denormEigenVect(double[][] A)
		{
			double a = A[0][0];
			double b = A[1][1];
			double c = A[0][1];

			//Rotation angle of principal axes
			double theta = Math.atan2(2 * c, a - b) / 2;
			double cos = Math.cos(theta);
			double sin = Math.sin(theta);

			//Eigenvalues derived from rotation angle
			double s0 = Math.sqrt(sin * sin * b + cos * cos * a + 2 * sin * cos * c);
			double s1 = Math.sqrt(sin * sin * a + cos * cos * b - 2 * sin * cos * c);

			//De-normalised eigenvectors as column matrix
			double[][] R = new double[2][2];
			R[0][0] = s0 * cos;
			R[1][0] = s0 * sin;
			R[0][1] = -s1 * sin;
			R[1][1] = s1 * cos;

			return R;
		}
	}



}
