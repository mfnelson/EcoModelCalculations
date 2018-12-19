package mountainPineBeetle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;


/**The set of equations needed to run the Mountain Pine Beetle winter survival model from:<br><br>
 * 
 *    Régnière, Jacques, and Barbara Bentz.<br>
 *    “Modeling Cold Tolerance in the Mountain Pine Beetle, Dendroctonus Ponderosae.”<br> 
 *    Journal of Insect Physiology 53, no. 6 (June 2007): 559–72. <br>
 *    https://doi.org/10.1016/j.jinsphys.2007.02.007. 
 * 
 * @author michaelfrancenelson */

public class RegniereBentzMPBCalculator {
	
	public static double logistic(double x, double alpha, double beta){
		double term1 = Math.exp(-(x - alpha) / beta);
		return term1 / (beta * Math.pow(1d + term1, 2d));
	}

	public static double sCPDistributionState1(double supercoolingTemperature)
	{ return logistic(supercoolingTemperature, ModelParameters.meanSCPAlpha1, ModelParameters.spreadSCPBeta1); }

	public static double sCPDistributionState2(double supercoolingTemperature)
	{ return logistic(supercoolingTemperature, ModelParameters.meanSCPAlpha2, ModelParameters.spreadSCPBeta2); }

	public static double sCPDistributionState3(double supercoolingTemperature)
	{ return logistic(supercoolingTemperature, ModelParameters.meanSCPAlpha3, ModelParameters.spreadSCPBeta3); }


	/** Equation 2 */
	public static double deltaColdHardeningState(double coldHardeningState, double gain, double loss)
	{ return (1d - coldHardeningState) * gain - coldHardeningState * loss; }


	/** Equation 3 */
	public static double gain(
			double dailyPhloemTempRangeR,
			double dailyPhloemTempMeanTau, 
			double tempOptimumGainTG)
	{ 
		return dailyPhloemTempRangeR * ModelParameters.maxGainRateRhoG * 
			logistic(dailyPhloemTempMeanTau, tempOptimumGainTG, ModelParameters.spreadGainSigmaG);
	}


	/** Equation 4 */
	public static double loss(
			double dailyPhloemTempRangeR,
			double dailyPhloemTempMeanTau, 
			double tempOptimumLossTL)
	{
		return dailyPhloemTempRangeR * ModelParameters.maxLossRateRhoL * 
				logistic(dailyPhloemTempMeanTau, tempOptimumLossTL, ModelParameters.spreadLossSigmaL);	
	}


	/** Equation 5 */
	public static double supercoolingTempGain(double coldHardeningState)
	{
		return ModelParameters.optimalGainTempMuG + ModelParameters.optimalGainTempSlopeKappaG * coldHardeningState;
	}

	/** Equation 6 */
	public static double supercoolingTempLoss(double coldHardeningState)
	{ 
		return ModelParameters.optimalLossTempMuL + ModelParameters.optimalLossSlopeKappaL * coldHardeningState;
		}


	/** Equation 7 */
	public static double currentColdHardening(
			double coldHardening,
			double gain,
			double loss,
			boolean gainOnly
			){
		double gainTerm = coldHardening + (1d - coldHardening) * gain;
		if(gainOnly == true & coldHardening < 0.5){
			return gainTerm;
		} else 
		{
			return gainTerm - coldHardening * loss;
		}
	}


	/** Equation 8 */
	public static double medianLethalTemperature(double coldHardeningState)
	{
		double p1 = proportion1(coldHardeningState);
		double p3 = proportion3(coldHardeningState);
		double p2 = proportion2(p1, p3);

		return ModelParameters.meanSCPAlpha1 * p1 + ModelParameters.meanSCPAlpha2 * p2 + 
				ModelParameters.meanSCPAlpha3 * p3;
	}

	/** Equation 9a: Proportion in summer state. 
	 *  This equation is wrong in the paper:
	 *  	The min function has no second term, it should be a 1
	 *  	The denominator for p1 should be 0.5 - lambda0
	 *  	The denominator for p3 should be lambda1 - 0.5  */
	public static double proportion1(double coldHardeningState){
		return 
				Math.max(
						0, 
						Math.min(
								1,
								(0.5 - coldHardeningState) / (0.5 - ModelParameters.thresholdLambda0)
								)
						);
	}

	/** Equation 9b: proportion in winter state. 
	 * 	This equation is wrong in the paper:
	 * 		The min function has no second term, it should be a 1
	 *  	The denominator for p1 should be 0.5 - lambda0
	 *  	The denominator for p3 should be lambda1 - 0.5 */
	public static double proportion3(double coldHardeningState){
		return 
				Math.max(
						0,
						Math.min(
								1, 
								(coldHardeningState - 0.5) / (ModelParameters.thresholdLambda1 - 0.5)
								)
						);
	}

	/** Equation 9c: proportion in fall/spring state. */
	public static double proportion2(double p1, double p3){return 1d - (p1 + p3);}

	/** Equation 9c: proportion in fall/spring state. */
	public static double proportion2(double coldHardeningState)
	{
		return 1d - 
				proportion1(coldHardeningState) -
				proportion3(coldHardeningState);
	}

	/** Equation 9 combined */
	public static double[] allProportions(double coldHardeningState)
	{
		//			, double thresholdLambda0, double thresholdLambda1){
		double p1 = proportion1(coldHardeningState);
		double p3 = proportion3(coldHardeningState);
		double p2 = proportion2(p1, p3);
		return new double[] {p1, p2, p3};
	}

	/** Equation 10 */
	public static double probSurvival(
			double previousProb,
			double dailyMinimumTemperature,
			double[] proportions)
	{

		double[] alpha = new double[] {ModelParameters.meanSCPAlpha1, ModelParameters.meanSCPAlpha2, ModelParameters.meanSCPAlpha3};
		double[] beta = new double[] {ModelParameters.spreadSCPBeta1, ModelParameters.spreadSCPBeta2, ModelParameters.spreadSCPBeta3};
		/* If there is missing data, the minimum temperature will be a super low number.
		 * In that case, don't calculate survival for today, just return yesterday's value */
		if(dailyMinimumTemperature < -300){
			return previousProb;
		} else
		{
			if(alpha.length != 3 | beta.length != 3 | proportions.length != 3){
				throw new IllegalArgumentException("");
			}
			double newProb = 0d;

			for(int i = 0; i < 3; i++){
				newProb += proportions[i] / (1d + Math.exp(-(dailyMinimumTemperature - alpha[i]) / beta[i]));
			}
			return Math.min(previousProb, newProb);
		}
	}

	/** Equation 11: Average phloem maximum temperature (north/south sides) from
	 * Bolstad, P. V., B. J. Bentz, and J. A. Logan. 1997. 
	 * Modelling micro-habitat temperature for Dendroctonus ponderosae 
	 * coleoptera: scolytidae). Ecological Modelling 94:287â€“297. */
	public static double phloemTempTauMax(double tempMin, double tempMax)
	{
		double deltaMax = 3.25;
		return tempMax + deltaMax * (tempMax - tempMin) / 24.4;
	}

	/** Equation 11: Average phloem maximum temperature (north/south sides) from
	 * Bolstad, P. V., B. J. Bentz, and J. A. Logan. 1997. 
	 * Modelling micro-habitat temperature for Dendroctonus ponderosae 
	 * coleoptera: scolytidae). Ecological Modelling 94:287â€“297. */
	public static double phloemTempTauMin(double tempMin)
	{
		return tempMin + 1.8;
	}
	
	/**  Update the beetle survival score given today's min and max temperature. <br> 
	 *  If temperature data for the day is missing, today's update is skipped and 
	 *  the survival remains at yesterday's value. <br><br>
	 * 
	 *  state[0] = cold hardening <br>
	 *  state[1] = gain <br>
	 *  state[2] = loss <br>
	 *  state[3] = survival <br>
	 *  
	 * @param temps today's temperatures [0] = low, [1] = high
	 * @param today today's date1
	 * @param state
	 * @return
	 */
	public static double[] updateState(double[] temps, LocalDate today, double[] state)
	{
		
		/* state[0] = cold hardening
		 * state[1] = gain
		 * state[2] = loss
		 * state[3] = survival
		 */
		/* If there is missing data for today, skip the update. */
		if(!(temps[0] <= -999 || temps[1] <= -999)){
	

			boolean gainOnly = false;
			if(today.getMonthValue() >= 8) {gainOnly = true;}
			
			/* Calculate today's phloem temperatures: */
			double phloemMaxTemp = RegniereBentzMPBCalculator.phloemTempTauMax(temps[0], temps[1]);
			double phloemMinTemp = RegniereBentzMPBCalculator.phloemTempTauMin(temps[0]);
			
			/* Update the gain and loss: */
			double supercoolingTempGain = RegniereBentzMPBCalculator.supercoolingTempGain(state[0]);
			double supercoolingTempLoss = RegniereBentzMPBCalculator.supercoolingTempLoss(state[0]);
			
			state[1] = RegniereBentzMPBCalculator.gain(
					phloemMaxTemp - phloemMinTemp, 
					0.5 * (phloemMaxTemp + phloemMinTemp), 
					supercoolingTempGain);
			state[2] = RegniereBentzMPBCalculator.loss(
					phloemMaxTemp - phloemMinTemp, 
					0.5 * (phloemMaxTemp + phloemMinTemp), 
					supercoolingTempLoss);
			
			state[0] = RegniereBentzMPBCalculator.currentColdHardening( state[0], state[1], state[2], gainOnly);
			
			state[3] = 	RegniereBentzMPBCalculator.probSurvival(
				state[3], temps[0], RegniereBentzMPBCalculator.allProportions(state[0]));
			return state;
		}
		return state;
	}
	

	public static class ModelParameters {

		/** Mean SCP in State 1  */
		public static double meanSCPAlpha1 = -9.8;
		/** Spread of SCP in State 1  */
		public static double spreadSCPBeta1 = 2.26;

		/** Mean SCP in State 2  */
		public static double meanSCPAlpha2 = -21.2;
		/** Spread of SCP in State 2  */
		public static double spreadSCPBeta2 = 1.47;

		/** Mean SCP in State 3  */
		public static double meanSCPAlpha3 = -32.3;
		/** Spread of SCP in State 3  */
		public static double spreadSCPBeta3 = 2.42;

		/** Maximum gain rate  */
		public static double maxGainRateRhoG = 0.311;
		/** Spread of the gain temperature response  */
		public static double spreadGainSigmaG = 8.716;

		/** Optimum gain temperature at C = 0  */
		public static double optimalGainTempMuG = -5d;
		/** Optimal gain temperature vs. C  */
		public static double optimalGainTempSlopeKappaG = -39.3;

		/** Maximum loss rate  */
		public static double maxLossRateRhoL = 0.791;
		/** Spread of the loss temperature response  */
		public static double spreadLossSigmaL = 3.251;

		/** Optimum loss temperature at C = 0 kL  */
		public static double optimalLossTempMuL = 33.9;
		/** Optimal loss temperature vs C  */
		public static double optimalLossSlopeKappaL = -32.7;

		/** Threshold C for State 1-2 transition  */
		public static double thresholdLambda0 = 0.254;
		/** Threshold C for State 2-3 transition  */
		public static double thresholdLambda1 = 0.764;


		//		public static ModelParameters(String fileName) throws IOException{
		//			readFromCSV(fileName);
		//		}

		public static void readFromCSV(String fileName) throws IOException{

			BufferedReader reader;
			reader = new BufferedReader(new FileReader(fileName));

			ArrayList<String> paramNames = new ArrayList<String>();
			ArrayList<Double> paramVals = new ArrayList<Double>();

			/* The first line of the file is the column headings, we can ignor it. */
			String line = reader.readLine();
			String[] entries = line.split(",");

			while((line = reader.readLine()) != null){
				entries = line.split(",");
				paramNames.add(entries[0]);
				paramVals.add(Double.parseDouble(entries[1]));
			}

			reader.close();

			/* Assign the values. */
			meanSCPAlpha1 = paramVals.get(paramNames.indexOf("alpha1"));
			spreadSCPBeta1  = paramVals.get(paramNames.indexOf("beta1"));
			meanSCPAlpha2 = paramVals.get(paramNames.indexOf("alpha2"));
			spreadSCPBeta2  = paramVals.get(paramNames.indexOf("beta2"));
			meanSCPAlpha3 = paramVals.get(paramNames.indexOf("alpha3"));
			spreadSCPBeta3 = paramVals.get(paramNames.indexOf("beta3"));
			maxGainRateRhoG = paramVals.get(paramNames.indexOf("rhoG"));
			spreadGainSigmaG = paramVals.get(paramNames.indexOf("sigmaG"));
			optimalGainTempMuG = paramVals.get(paramNames.indexOf("muG"));
			optimalGainTempSlopeKappaG = paramVals.get(paramNames.indexOf("kappaG"));
			maxLossRateRhoL = paramVals.get(paramNames.indexOf("rhoL"));
			spreadLossSigmaL = paramVals.get(paramNames.indexOf("sigmaL"));
			optimalLossTempMuL = paramVals.get(paramNames.indexOf("muL"));
			optimalLossSlopeKappaL = paramVals.get(paramNames.indexOf("kappaL"));
			thresholdLambda0 = paramVals.get(paramNames.indexOf("lambda0"));
			thresholdLambda1 = paramVals.get(paramNames.indexOf("lambda1"));

		}
	}
}
