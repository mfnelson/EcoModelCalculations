package mountainPineBeetle;



public class SafranyikMPBCalculator {

	
	/** Safranyik et al 1999: equations 1a, 1b page 4. <br>
	 *  The size of the beetle population in the next time step. <br>
	 *  For equation 1a, dbhClassesReproduction should come from 
	 * 
	 * @param adultBeetlesT  number of adult beetles at time step t
	 * @param lostBeetlesT number of beetles lost due to inability to fly or dispersal out of the area
	 * @param propFemaleT proportion of all beetles that are female
	 * @param dbhClassesReproduction 
	 * @return
	 */
	public static int nextAttackingPop(int adultBeetlesT, int lostBeetlesT, int propFemaleT, double dbhClassesReproduction){
		int out = 0;
		out = (int)(((adultBeetlesT - lostBeetlesT)) * propFemaleT * dbhClassesReproduction);
		return out;
	}
	
	/** Safranyik et al. 1999: equation 1a-1.  <br> Helper function for calculating the sum of the proportional
	 *  reproduction rates for the various DBH classes.
	 * 
	 * @param proportionPerDBHClass
	 * @param survival
	 * @param meanEggs
	 * @param proportionFemale
	 * @return
	 */
	public static double dbhClassesReproductionAll(double[] proportionPerDBHClass, double[] survival, double[] meanEggs){
		double[] unity = new double[proportionPerDBHClass.length]; for(int i = 0; i < unity.length; i++) unity[i] = 1d;
		return dbhClassesReproductionFemale(proportionPerDBHClass, survival, meanEggs, unity);
	}
	
	/** Safranyik et al. 1999: equation 1b-1.  <br> Helper function for calculating the sum of the proportional
	 *  reproduction rates for the various DBH classes.
	 * 
	 * @param proportionPerDBHClass
	 * @param survival
	 * @param meanEggs
	 * @param proportionFemale
	 * @return
	 */
	public static double dbhClassesReproductionFemale(double[] proportionPerDBHClass, double[] survival, double[] meanEggs, double[] proportionFemale){
		double out = 0d;
		for(int i = 0; i < proportionPerDBHClass.length; i++){out += proportionPerDBHClass[i] * survival[i] * meanEggs[i] * proportionFemale[i];}
		return out;
	}

	
	
	/** Safranyik et al. 1999 Equation 2, page 5.  Beetle across-generation survival within DBH classes
	 * NOTE:  We can replace this with survival from the Regnière and Bentz model.
	 * @param eggs
	 * @param larvae
	 * @param pupae
	 * @param youngAdults
	 * @param matureAdults
	 * @return survival proportion within a dbh class.*/
	public static double beetleSurvival(double eggs, double larvae, double pupae, double youngAdults, double matureAdults){
		return eggs * larvae * pupae * youngAdults * matureAdults;
	}
	
	
	/** Safranyik et al. 1999 Equation 3, page 5
	 * 
	 * @param nextFemalePop
	 * @param proportionPerDBHClass
	 * @param attackedTreesPerDBHClass
	 * @param infestedMeanBarkAreaPerDBHClass
	 * @return
	 */
	public static double treesAttacked(double nextFemalePop, double[] proportionPerDBHClass, int[] attackedTreesPerDBHClass, double[] infestedMeanBarkAreaPerDBHClass){
		int out = 0;
		
		for(int i = 0; i < proportionPerDBHClass.length; i++){
			out += (int)((proportionPerDBHClass[i]) / ((double) attackedTreesPerDBHClass[i] * infestedMeanBarkAreaPerDBHClass[i]));
		}
		return nextFemalePop * out;
	}


	/** Safranyik et al. 1999. Equation 4, page 5 (Attack submodel) 
	 * 
	 * @param dbh
	 * @param height
	 * @param infestedHeight
	 * @return
	 */
	public static double infestedBoleSurfaceArea(double dbh, double height, double infestedHeight){
		return totalBoleSurfaceArea(dbh, height) * proportionBoleAreaBelow(infestedHeight, height);
	}
	
	/**  f1 in equation 5 of Safranyik et al. 1999 (Attack submodel) <br>
	 *   total bole surface area as function of dbh and tree height.
	 *   //TODO stub
	 * @param dbh
	 * @param height
	 * @return	 */
	public static double totalBoleSurfaceArea(double dbh, double height){
		return 0d;
	}

	/** f2 in equation 5 of Safranyik et al. 1999 (Attack submodel) <br>
	 *  proporiton of bole surface below a given height.
	 *  // TODO stub
	 * @param infestedHeight
	 * @param height
	 * @return	 */
	public static double proportionBoleAreaBelow(double infestedHeight, double height){
		return 0d;
	}
	
	/** Safranyik et al. 1999 Equation 5, page 5 (Attack submodel)<br>
	 * 	Infested height of the bole.  
	 *  TODO:  See Amman and Cole 1980 for details
	 * @param a
	 * @param b
	 * @param c
	 * @param dbh
	 * @param siteIndex
	 * @return
	 */
	public static double infestedHeight(double a, double b, double c, 
			double dbh, double siteIndex)
	{
		return a * Math.pow(dbh - b, c * siteIndex);
	}
	
	/** Safranyik et al. equation from caption to Figure 1. <br>
	 *  Constants were taken from the figure caption. <br>
	 *  Caption references: <br>
	 *  Safranyik 1988, Estimating attack and brood totals and densities of the mountain pine beetle in 
	 *  	individual lodgepole pine trees. The Canadian Entomologist 120: 323-331.
	 * 
	 * @param dbh current cohort (tree) diameter in meters
	 * @param height current cohort (tree) height in meters
	 * @param proportionHeight the height up to which to calculate the cumulative area
	 * @return
	 */
	public static double cumulativeBoleArea(double dbh, double height, 
			double proportionHeight)
	{
		 return (0.3455 + 1.9708 * dbh * height) * 
				 (0.0059 + 1.6761 * proportionHeight -
						 0.6657 * Math.pow(proportionHeight, 2d));
	}
	
	
	/** Safranyik et al. 1999 equation 6, page 6. (Attack submodel)<br>
	 *  Proportional allocation of attacking beetles to DBH classes.
	 * @param liveTrees
	 * @param dbh
	 * @return
	 */
	public static double[] attackProportionDBHClasses(int[] liveTrees, double[] dbh){
		double denomSum = 0d;
		double num[] = new double[liveTrees.length];
		
		for(int i = 0; i < liveTrees.length; i++){
			denomSum += liveTrees[i] * dbh[i];
			num[i] = liveTrees[i] * dbh[i];
		}

		/* Division calculations are more expensive than multiplication, so I've heard.
		 * Just do it one time here and use multiplication below. */
		denomSum = 1d / denomSum;
		
		for(int i = 0; i < liveTrees.length; i++){
			num[i] *= denomSum;
		}
		return num;
	}
	
	
	/** Safranyik et al. 1999 equation 7, page 7  (Attack submodel)<br>
	 *  Losses during dispersal due to suboptimal stand conditions
	 * 
	 * @param standAge
	 * @param distance
	 * @param meanDBH
	 * @return
	 */
	public static double suboptimalStandConditionsDispersalLoss(double standAge, double distance, double meanDBH){
		return Math.pow(standAge * distance * meanDBH, 0.33);
	}
	
	
	/** Safranyik et al. 1999 equation 8, page 7.  (Attack submodel) <br>
	 *  The minimum dbh of trees that beetles will attack, given the mean nearest neighbor distance in the stand.
	 * 
	 * @param distance mean nearest neighbor distance (in meters) among pines with dbh ? > 10cm.
	 * @return */
	public static double minimumAttckedDBHInStand(double distance){
		return 175d / (5d + 27d * Math.exp(-0.35 * distance));
	}
	
	/** Safranyik et al. 1999 equation 9, page 7 (Attack submodel) <br>
	 *  Probability of successful attack.
	 * 
	 * @param b constant parameter //TODO find values: caption for figure 2 uses 12.0
	 * @param relativeHostResistance (0 = not resistant, 1 = fully resistant)
	 * @param x0 linear function of average attack density
	 * @return
	 */
	public static double successfulAttackProbability(double b, double relativeHostResistance, double x0){
		return 1d / (1d + Math.exp(-b * (x0 - relativeHostResistance)));
	}
	
	/** Safranyik et al. 1999 equation from caption of figure 2, page 10 (Attack submodel)
	 * 
	 * @param attackDensity
	 * @return
	 */
	public static double adjustedAverageAttackDensity(double attackDensity){
		return 0.4 + 0.4 * ((attackDensity - 10d) / 10);
	}
	
	
	/** Safranyik et al. 1000 equation 10, page 8
	 * 
	 * @param a a constant
	 * @param galleryLength average egg gallery length per attack (equation 11)
	 * @param eggsPerUnitLength number of eggs laid per unit of egg gallery
	 * @return
	 */
	public static double eggsPerFemale(double a, double galleryLength, double eggsPerUnitLength){
		return a * galleryLength * eggsPerUnitLength;
	}
	
	/** Safranyik et al. 1999 equation 11, page 8
	 * @param a a constant
	 * @param femaleProtonumWidth female size relative to the average
	 * @param b a constant
	 * @param attackDensity attack density from equation 12
	 * @param adjustedHostResistance TODO which equation?
	 * @return average length of the egg gallery per attack
	 */
	public static double averageEggGalleryLength(double a, double femaleProtonumWidth,  
			double b, double attackDensity, double adjustedHostResistance){
		/* Average width was assumed to be 2.1 mm in Safranyik et al. 1999, page 8 */
		double averageProtonumSizeRecip = 0.48;
		if(attackDensity > 0){
			double femaleSizeRatio = a * femaleProtonumWidth * averageProtonumSizeRecip;
			double adjustedAttackDensity = Math.pow(b * attackDensity, -0.33);
			double hostSusceptibility = 1d - adjustedHostResistance;
			
			return(femaleSizeRatio * adjustedAttackDensity * hostSusceptibility);
		} else return 0d;
	}
	
	/** Safranyik et al. 1999 equation 11a from figure 3 caption, page 9
	 * 
	 * @param m value from equation 11b
	 * @param relativeHostResistance
	 * @return
	 */
	public static double adjustedRelativeHostResistance(double m, double relativeHostResistance){
		return m / (1d + Math.exp(-0.2667 * Math.sqrt(relativeHostResistance)));
	}
	
	

	
	/** Safranyik et al. equation 11b from figure 3 caption, page 9
	 * @param relativeHostResistance
	 * @param attackDensity
	 * @return variable M in equation 11a
	 */
	public static double helperFunctionM(double relativeHostResistance, double attackDensity){
		if(attackDensity < 10d) {
			return 0.9 * Math.sqrt(relativeHostResistance);
		} else if(attackDensity < 100d){
			return (0.9 - (attackDensity - 10d) / attackDensity) * Math.sqrt(relativeHostResistance);
		} else{
			return 0d;
		}
	}
	
	
	
	/** Safranyik et al 1999 Equation 12, page 9
	 * 
	 * @param a a constant
	 * @param meanDailyTemp
	 * @param b a constant
	 * @param dbh mean dbh of tree class
	 * @param e a constant
	 * @param galleryLengthDensity
	 * @return
	 */
	public static double averageFemaleSize(double a, double meanDailyTemp, double b, double dbh, double e, double galleryLengthDensity){
		return (1d - a * (meanDailyTemp - 15d) * (b + dbh - e * Math.pow(galleryLengthDensity, 3d)));
	}
	
	
	
	public static double[] doubleSeq(double start, double end, double interval){
		int n = (int)((end - start) / interval);
		double[] out = new double[n + 1];
		double sum = start;
		for(int i = 0; i < out.length; i++){
			out[i] = sum;
			sum += interval;
		}
		out[n] = end; 
		
		return out;
	}
	
}
