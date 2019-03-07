package mountainPineBeetle;

import sequeuces.Sequences;

public class MPBCalculations {

	/** Calculate a weighted score for a number from 0 to 1.  
	 *  if weight = 1, the output is just the input score.
	 *  if weight = 0, the output is always 1.
	 * @param score
	 * @param weight
	 * @return a weighted score in the range zero to one, inclusive
	 */
	public static  double weightedScore(double score, double weight){
		return 1d - weight + weight * score;
		}
	
	public static double[] weightedScore(double[] score, double weight)
	{
		/* Normalize the scores: */
		double[] scores = Sequences.recenterAndNormalize(score, 0d, 1d);
		for(int i = 0; i < scores.length; i++)
			scores[i] = weightedScore(scores[i], weight);
		
		return scores;
	}
	
	

	/** Utility function to convert a boolean 'true' to a 1
	 * and a boolean 'false' to a 0. */
	public static int booleanToInt(Boolean bool)
	{
		int out = 0;
		if(bool == true) {out = 1;}
		return out;
	}
	
	/** Utility function to convert an int to a boolean:<br>
	 *  nonzero ints return true, zero is false.	 */
	public static boolean intToBoolean(int x){
		if(x == 0) return false;
		else return true;
	}
	
	/** Calculate the attractiveness of a cell to emerging beetles, based on distance from 
	 *   the source cell using the following formula: <br>
	 *   score = paramA * e<sup>(-paramB * distance)</sup>
	 * 
	 * @param distance distance in meters from the source cell
	 * @param dispersalDistParamA
	 * @param dispersalDistParamB
	 * @return distance score
	 */
	public static double exponentialDistanceScore(double distance, double dispersalDistParamA, double dispersalDistParamB){
		return(dispersalDistParamA * Math.exp(-dispersalDistParamB * distance));
	}
	
	
	
}
