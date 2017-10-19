/**
 * 
 */
package forClim;

import cern.jet.random.Uniform;

/**
 * @author michaelfrancenelson
 *
 */
public class PlantCalculator {

	
	
	//================================================================================
	//
    // The following methods are from the ForClim Cohort submodel
	// C# source: Cohort.cs 
    //
	// ================================================================================

	
	
	/** Calculate the initial height of the cohort
	 * 
	 * Cohort.cs Cohort() lines 139, 184, 247
	 * 
	 * @param kB1 Allometric parameter for relating dbh to height
	 * @param kHMax Adjusted maximum tree height	
	 * @param kSIn Initial skinniness, formerly the static s-value
	 * @param dbh current diameter at bh
	 * @return starting height for the cohort
	 */
	public static double initialHeight(double kB1, double kHMax, double kSIn, double dbh){
		return  kB1 + (kHMax - kB1) * (1 - Math.exp(-kSIn * dbh / (kHMax - kB1)));
	}
	
	/** Calculate the value of the A1 allometric crown geometry parameter. <br>
	 * 
	 * Cohort.cs Cohort() line 193, 282
	 * 
	 * @param kA1min maximum possible value for A1
	 * @param kA1max minimum possible value for A1
	 * @param initLAI Initial leaf area index for stand data initialization
	 * @param lcp_La1 Light compensation point for shade-tolerant species 
	 * @return A1 Current value of parameter for the allometric relationship between dbh and foliage weight
	 */
	public static double allometricParameterA1(
			double kA1min, double kA1max, double initLAI, double lcp_La1){
		return Math.max(kA1min, kA1max - (kA1max - kA1min) * (initLAI / lcp_La1));
	} 
	
	/** Update the A1 allometric crown geometry parameter. <br>
	 * 
	 * Cohort.cs method CohortCrownGeometry() lines 279 - 286
	 * 
	 * @param kA1min minimum for allometric parameter for foliage weight Species.cs
	 * @param kA1max maximum for allometric parameter for foliage weight Species.cs
	 * @param lai the cohort's current leaf area index Cohort.cs
	 * @param lcp_La1 Light compensation point for shade-tolerant species [MD] Plant.cs
	 * @param a1 Current value of parameter for the allometric relationship between dbh and foliage weight Cohort.cs
	 * @return The updated value of the allometric parameter
	 */
	public static double cohortCrownGeometry(
			double kA1min, double kA1max, double lai, double lcp_La1, double a1){
        double gA1;
				
		// first check that current value for kA1 does not fall below kA1min :
        gA1 = allometricParameterA1(kA1min, kA1max, lai, lcp_La1);
        
		// second check that current value for A1 does not increase :
		return Math.min(gA1, a1);
	}
	
	/** Crown length growth factor <br>
	 * 
	 * Cohort.cs method Grow() lines 302 - 304
	 * 
	 * @param lcp_La1 Light compensation point for shade-tolerant species [MD] Plant.cs
	 * @param lcp_La9 Light compensation point for shade-intolerant species [MD] Plant.cs
	 * @param kLa Shade tolerance of adult trees Species.cs
	 * @param kCLGF_a Parameters for growth reduction based on crown geometry [MD] Plant.cs
	 * @param a1 Current value of parameter for the allometric relationship between dbh and foliage weight Cohort.cs
	 * @param kA1max 
	 * @param kA1min
	 * @return
	 */
	public static double crownLengthGrowthFactor(
			double lcp_La1, double lcp_La9, double kLa, 
			double kCLGF_a, double a1, double kA1max, double kA1min)
	{
		double gLCP_La = lcp_La1 - (lcp_La1 - lcp_La9) * (kLa - 1d) * 0.125;
		double gCLGF = kCLGF_a * a1 / (kA1max - kA1min) * gLCP_La / ((lcp_La1 + lcp_La9) * 0.5);
		
		/* ensure the growth factor is between 0 and 1. */
		return Math.min(1d, Math.max(0, gCLGF));
	}
	
	/** Growth reduction factor <br>
	 *  C# code says the crown length growth factor is excluded,
	 *  but includes it in the calculation. <br>
	 *  
	 *  Cohort.cs method Grow() line 307
	 * 
	 * @param gSGRF slow growth reduction factor
	 * @param aglf available light growth factor Cohort.cs
	 * @param gCLGF crown length growth factor
	 * @return growth reduction factor
	 */
	public static double growthReductionFactor(double gSGRF, double aglf, double gCLGF){
		return gSGRF * Math.pow(aglf, 0.333) * Math.pow(gCLGF, 0.333);
	}
	
	/** "Skinniness" of the tree. <br>
	 * 
	 *  Cohort.cs method Grow() line 310
	 * 
	 * @param kSMin Smallest value the s-parameter may take Species.cs
	 * @param kE1 Slope of s-change Species.cs
	 * @param al current available light Cohort.cs
	 * @return 
	 */
	public static double skinnyTree(double kSMin, double kE1, double al){
		return kSMin + kE1 * (1d - al);
	}
	
	/** Competition parameter
	 * 
	 *  Cohort.cs method Grow() line 311
	 * 
	 * @param gS skinniness of the trees Cohort.cs
	 * @param h current height of the trees Cohort.cs
	 * @param kB1 Allometric parameter for relating DBH to height Species.cs
	 * @param kHMax Adjusted maximum tree height Species.cs
	 * @return Parameter representing competition function deciding D/H growth allocation [LR]
	 */
	public static double competitionFunctionParameter(double gS, double h, double kB1, double kHMax){
		return Math.max(0d,  gS * (1d - (h - kB1) / (kHMax - kB1)));
	}
	
	/** Diameter growth rate <br>
	 * 
	 * //TODO this is the same as one of the classic gap model equations.  Find the reference 
	 * 
	 * Cohort.cs method Grow() line 314
	 * 
	 * @param gGRF growth reduction factor
	 * @param kG growth rate parameter Species.cs
	 * @param dbh current tree DBH
	 * @param height current tree height
	 * @param kHMax Adjusted maximum tree height Species.cs
	 * @param gFun competition parameter dbh/height growth allocation
	 * @return the unadjusted diameter growth rate //TODO in cm?
	 */
	public static double diameterGrowthRate(double gGRF, double kG, double dbh, double height, double kHMax, double gFun){
		return Math.max(0, gGRF * kG * dbh * (1d - height / kHMax) / (2d *height + gFun * dbh));
	}
	
	/** The increase in dbh for the current time step <br>
	 * 
	 * Cohort.cs method Grow() line 317
	 * 
	 * @param timeStep length of the time step Plant.cs, SubModel.cs
	 * @param gRateD unadjusted diameter growthRate
	 * @return the increment for dbh
	 */
	public static double diameterIncrement(double timeStep, double gRateD){
		return timeStep * gRateD;
	}
	
	/**  The height increment for the current time step. <br>
	 * 
	 * Cohort.cs method Grow() line 318
	 * 
	 * @param dbhIncrement the increment in dbh, adjusted for time step
	 * @param gFun competition parameter dbh/height growth allocation
	 * @return the increment for height
	 */
	public static double heightIncrement(double dbhIncrement, double gFun){
		return dbhIncrement * gFun;
	}
	
	/** The tree's increase in volume for the time step. <br>
	 * 
	 * Cohort.cs method Grow() line 321
	 * 
	 * @param dbh current tree DBH Cohort.cs
	 * @param height current tree height Cohort.cs
	 * @param dbhIncrement current cohort's diameter increment Cohort.cs
	 * @param heightIncrement current cohort's height increment Cohort.cs
	 * @return volume increment
	 * //TODO Note: the second term has dbh * dbh, should one of those be height?
	 */
	public static double volumeIncrement(double dbh, double height, double dbhIncrement, double heightIncrement){
		return 2d * dbh * height * dbhIncrement + dbh * dbh * heightIncrement;
	}
	
	/** Update the number of years the the cohort has been in a slow growth state. <br>
	 *  
	 *  Cohort.cs method Grow() line 328
	 * 
	 * @param gGRF growth reduction factor
	 * @param kMinRelInc Minimum relative diameter growth increment, below which the cohort growth statis is slow.
	 * @param gRateD unadjusted diameter growth rate //TODO should this be the dbh increment instead?
	 * @param kMinAbsInc Minimum absolute diameter growth increment, below which the cohort growth statis is slow.
	 * @param sGr The number of years the cohort has been in a slow growth status.
	 * @return
	 */
	public static int slowGrowthRateUpdate(double gGRF, double kMinRelInc, double gRateD, double kMinAbsInc, int sGr){
		if(gGRF < kMinRelInc | gRateD < kMinAbsInc) return sGr + 1;
		else return 0;
	}
	
	/**  The mortality rate for stressed trees <br>
	 * 	ForClim v.3.0 equation 4.30
	 * 
	 *  Cohort.cs method Die() line 343
	 *  
	 * @param sGr number of consecutive years the cohort has been growing slowly
	 * @param kSlowGrowThreshold number of years a tree can grow slowly without being subject to stress-induced mortality
	 * @param kSlowGrP mortality probability for slow growing tree
	 * @return
	 */
	public static double stressInducedMortalityRate(int sGr, int kSlowGrowThreshold, double kSlowGrowMortProb){
		if(sGr > kSlowGrowThreshold) return kSlowGrowMortProb;
		else return 0d;
	} 
	
	/**  overall annual probability that a tree dies<br>
	 * ForClim v.3.0 equation 4.33:<br>
	 * 
	 *  Cohort.cs method Die() line 344 
	 *  
	 * @param gPDist disturbance related mortality probablilty
	 * @param gPAge background mortality probability
	 * @param gPStr stress-induced (i.e. slow growth) mortality probability
	 * @return probability that a tree dies in the current year.*/
	public static double annualTreeMortalityRate(double gPDist, double gPAge, double gPStr){
		return gPDist + (1d - gPDist) * (gPAge + (1d - gPAge) * gPStr);
	}
	
	/** ForClim v.3.0 equation 4.12
	 * 
	 * Cohort.cs Light() Lines 378 - 381
	 * 
	 * @param aL available light
	 * @param kLa Shade tolerance of adult trees Species.cs
	 * @return
	 */
	public static double availableLightGrowthFactor(double aL, double kLa){
		double gL1 = 1d - Math.exp(-4.64 * (aL - 0.05));
	    double gL9 = 2.24 * (1d - Math.exp(-1.136 * (aL - 0.08)));
	    return Math.max(0d, gL1 + (kLa - 1) * (gL9 - gL1) / 8d);
	} 
	
	/** ForClim v.3.0 equation 4.1 <br>
	 * 
	 * Cohort.cs in method FolWeight() line 491 
	 *           and line 623 in method FoliageBiomass() 
	 * 
	 * @param dbh current diameter at breast height
	 * @param kC1 dry to wet ratio of foliage mass
	 * @param gA1 'indirect linear measure for crown growth'
	 * @param kA2 allometric foliage weight parameter
	 * @return gFolW foliage weight in kg of a single tree in the cohort
	 */
	public static double foliageWeight(double dbh, double kC1, double gA1, double kA2){
		return kC1 * gA1 * Math.pow(dbh, kA2);} 

	/** Sum of the volumes of an individual tree <br>
	 *  Calculated with the Denzin formula. <br>
	 * 
	 * Cohort.cs method Volume line 502
	 * 
	 * @param dbh current tree diameter
	 * @return wood volume in meters cubed of an individual tree
	 */
	public static double treeStemVolume(double dbh){return Math.pow(dbh, 2d) * 0.001;}
	
	/** Wood energy volume for branches and debris for evergreen and deciduous trees <br>
	 * 
	 * Cohort.cs method Vol_WE() lines 513 - 546
	 * 
	 * @param kType tree type (evergreen or deciduous)
	 * @param dbh current dbh of the trees
	 * @param volume stem volume of an individual tree
	 * @return the volume of branches and woody debris for an individual tree
	 * 
	 * TODO:  It is problematic that these values are hard-coded.  
	 * They should be in a parameters of the species.
	 * Move to species class.
	 */
	public static double branchesWoodyDebrisEnergyVolume(String kType, double dbh, double volume){
		
        double k1 = 0.0;       //coefficient large branches
        double k2 = 0.0;       //coefficient large branches
        double k3 = 0.0;       //coefficient woody debris
        double k4 = 0.0;       //coefficient woody debris
        double w_br = 0.0;     //factor large branches
        double w_wd = 0.0;     //factor woody debris
		
        /* Evergreen trees */
		if(kType.startsWith("E")){
            k1 = -8.7330758;
            k2 = 0.059208154;
            k3 = -1.933950168;
            k4 = -0.016986685;
            w_br = Math.exp(k1 + k2 * dbh);
            w_wd = Math.exp(k3 + k4 * dbh);
		} 
		/* Deciduous trees */
		else if(kType.startsWith("D")){
            k1 = -4.9398872;
            k2 = 0.061619224;
            k3 = -1.206413264;
            k4 = -0.019186451;
            w_br = Math.exp(k1 + k2 * dbh);
            w_wd = Math.exp(k3 + k4 * dbh);
		}
		return woodEnergyVolume(w_br, volume) + woodEnergyVolume(w_wd, volume);
	}
	
	/** Wood energy volume< br>
	 * 
	 * Cohort.cs method Vol_WE() line 543 - 544
	 * 
	 * @param wood energy factor
	 * @param wood volume
	 * @return
	 */
	public static double woodEnergyVolume(double woodFactor, double volume){
		return woodFactor / (1d + woodFactor) * volume;
	}
	
	/** Parameters for calculating biomass of wood
	 * 
	 * Cohort.cs methods StandBiomass() and BranchesBiomass() lines 568 - 581, 596 - 609
	 * 
	 * TODO it is highly problematic that these values are hard coded.  They should be parameters.
	 * Move to the plant species class.
	 * 
	 * @param kType whether the species is evergreen or deciduous
	 * @return product of the BEF and CDF parameters
	 */
	public static double woodyBiomassParam(String kType){
		double bef = 0.0;
		double cdf = 0.0;

		if(kType.startsWith("E")){
                    bef = 1.3;
                    cdf = 0.51;
		} else if(kType.startsWith("D")){
                    bef = 1.4;
                    cdf = 0.48;
		}
		return bef * cdf;
	}
	
	/** Wood Biomass (aboveground Carbon) [MM] <br>
	 * 
	 * Cohort.cs method StemBiomass() lines 583, 611
	 * 
	 * @param volume the stem volume of an individual tree
	 * @param kWD //TODO this is confusing in the C# source, it says TODO. Declaration in Species.cs line 99
	 * @param bef Not labeled in original C# code TODO: find source for this equation
	 * @param cdf Not labeled in original C# code
	 * @return the stem biomass of an individual tree
	 */
	public static double woodyBiomass(double volume, double kWD, double bef, double cdf){
		return volume * kWD * bef * cdf * 1000d;
	}
	
	/** Wood Biomass (aboveground Carbon) [MM] <br>
	 * 
	 * Cohort.cs method StemBiomass() and BranchesBiomass() lines 583, 611
	 * 
	 * @param volume the stem volume of an individual tree
	 * @param kWD //TODO this is confusing in the C# source, it says TODO. Declaration in Species.cs line 99
	 * @param bef Not labeled in original C# code TODO: find source for this equation
	 * @param cdf Not labeled in original C# code
	 * @return the stem biomass of an individual tree
	 */
	public static double woodyBiomass(double volume, double kWD, double woodyBiomassParam){
		return volume * kWD * woodyBiomassParam * 1000d;
	}
	
	/** Basal area of a tree
	 * 
	 * Cohort.cs method BasalArea() line 640
	 * 
	 * @param dbh the current DBH of the tree
	 * @return The basal area of an individual tree
	 * NOTE:  The C# code calculated for the cohort.
	 */
	public static double basalArea(double dbh){return Math.PI * dbh * dbh * 0.25;}
	
	/** Calculate the foliage area of an individual tree<br>
	 * 
	 * NOTE: This is just a wrapper for foliageWeight() above.<br>
	 * 
	 *  Cohort.cs FoliageArea lines 669 - 671
	 * 
	 * @param kC2 Foliage area per unit foliage weight Species.cs
	 * @param gA1 Current value of parameter for the allometric relationship between dbh and foliage weight Cohort.cs
	 * @param dbh diameter at bh of the cohort's trees
	 * @param kA2 Allometric parameter for foliage weight Species.cs
	 * @return foliage area for a single tree in the cohort
	 */
	public static double foliageArea(double kC2, double gA1, double dbh, double kA2){
		return foliageWeight(dbh, kC2, gA1, kA2);
	} 
	
	/** The foliage litter (TODO is this the weight?) for an entire cohort <br>
	 * 
	 *  Cohort.cs method FoliageLitter() lines 694 - 696
	 * 
	 * @param kC1 Dry to wet weight ratio of foliage Species.cs
	 * @param gA1 Current value of parameter for the allometric relationship between dbh and foliage weight Cohort.cs
	 * @param dbh diameter at bh of the cohort's trees Cohort.cs
	 * @param kA2 Allometric parameter for foliage weight Species.cs
	 * @param timeStep length of the time step Plant.cs
	 * @param kFRT Average time of foliage retention Species.cs
	 * @param dTrs Number of dead trees Cohort.cs
	 * @param kAshFree Proportion of organic matter content of dry weight Plant.cs
	 * @return The total weight of foliage in the cohort
	 */
	public static double foliageLitterCohort(
			int liveTreeCount, double kC1, double gA1, double dbh, 
			double kA2, double timeStep, double kFRT, int deadTreeCount, 
			double kAshFree)
	{
		double foliageWeightAll = foliageWeight(dbh, kC1, gA1, kA2);
		double foliageWeightOrganic = foliageWeightAll * kAshFree;
		double proportionLiveFoliageRetained = timeStep / kFRT;
		
		return foliageWeightOrganic * (
				(double)liveTreeCount * proportionLiveFoliageRetained + 
				(double)deadTreeCount);
	}
	
	/** Calculate the twig litter of a single tree. <br>
	 * 
	 * Cohort.cs method TwigLitter line 710.<br>
	 * 
	 * Note: the C# code calculated for a whole cohort, this is for a single tree.
	 * @param dbh current dbh of trees in the cohort.
	 * @param kConv Conversion factor basal area-twig litter
	 * @param kAshFree Proportion of organic matter content of dry weight Plant.cs
	 * @return amount of twig litter for a tree TODO: What are the units?
	 */
	public static double twigLitter(double dbh, double kConv, double kAshFree){
		/* (Math.PI / 4d) is 0.7853982 */
		return 0.7853982 * dbh * dbh * kConv * kAshFree;
	}
	
	/** Calculate the root litter output of a single tree <br>
	 * 
	 * Cohort.cs method RootLitter Lines 726 - 728<br>
	 * 
	 * Note: The C# code calculates the root litter for a cohort.
	 * 
	 * @param kC1 Dry to wet weight ratio of foliage. Species.cs
	 * @param gA1 Current value of parameter for the allometric relationship between dbh and foliage weight. Cohort.cs
	 * @param dbh Current dbh of the tree Cohort.cs
	 * @param kA2 Allometric parameter for foliage weight Species.cs
	 * @param kRSR Root:shoot ratio of litter production Plant.cs
	 * @param kAshFree Organic matter proportion of dry foliage weight Plant.cs
	 * @return mass of root litter per tree
	 */
	public static double RootLitterPerTree(
			double kC1, double gA1, double dbh, 
			double kA2,	double kRSR, double kAshFree)
	{
		double foliageTotalDryWeightPerTree = foliageWeight(dbh, kC1, gA1, kA2);
		double foliageOrganicWeightPerTree = foliageTotalDryWeightPerTree * kAshFree;
		return kRSR * foliageTotalDryWeightPerTree;
	}
	
	/** Calculate the root litter output of an entire cohort <br>
	 * 
	 * Cohort.cs method RootLitter Lines 726 - 728<br>
	 * 
	 * Note: The C# code calculates the root litter for a cohort.
	 * 
	 * @param kC1 Dry to wet weight ratio of foliage. Species.cs
	 * @param gA1 Current value of parameter for the allometric relationship between dbh and foliage weight. Cohort.cs
	 * @param dbh Current dbh of the tree Cohort.cs
	 * @param kA2 Allometric parameter for foliage weight Species.cs
	 * @param kRSR Root:shoot ratio of litter production Plant.cs
	 * @param kAshFree Organic matter proportion of dry foliage weight Plant.cs
	 * @param countLiveTrees number of living trees Cohort.cs
	 * @param countDeadTrees number of dead trees Cohort.cs  TODO:  Are these trees that died this time step or cumulative?
	 * @param kFRT Average time of foliage retention Species.cs
	 * @return mass of root litter per cohort TODO: units?
	 */
	public static double RootLitterPerCohort(
			double kC1, double gA1, double dbh, 
			double kA2,	double kRSR, double kAshFree,
			int countLiveTrees, int countDeadTrees, double kFRT)
	{
		double rootLitterPerTree = RootLitterPerTree(kC1, gA1, dbh, kA2, kRSR, kAshFree);
		return rootLitterPerTree * ((double)countDeadTrees + (double)countLiveTrees / kFRT);
	}
	
	/** Calculate the stemwood biomass of a single tree. <br>
	 * 
	 * Cohort.cs method WoodyLitter line 743<br>
	 * 
	 * TODO The C# code does not provide the source of this equation...
	 * 
	 * @param dbh The tree's current DBH
	 * @return
	 */
	public static double stemwoodTree(double dbh){
		return 0.12 * Math.pow(dbh, 2.4);
	}
	
	/** Calculate the woody litter produced by a dead tree.<br>
	 * 
	 * Cohort.cs method WoodyLitter line 744 <br>
	 * 
	 * @param dbh The tree's current DBH
	 * @param kAshFree Organic matter proportion of dry foliage weight Plant.cs
	 * @return
	 */
	public static double woodyLitter(double dbh, double kAshFree){
		return stemwoodTree(dbh) * kAshFree;
	}
	
	
	//================================================================================
	//
    // The following methods are from the ForClim Plant class
	// C# source: Plant.cs 
    //
	// ================================================================================
	
	
	
	/** Light availability given the leaf area above
	 * 
	 * Plant.cs LightAvailability() lines 579 - 582
	 * 
	 * @param gLAI Cumulative leaf area above
	 * @param kLAtt Light attenuation coefficient
	 * @param kPatchSize The area of the patch in square meters //TODO verify the units
	 * @return //TODO what are the units?
	 */
	public static double lightAvailablilty(double gLAI, double kLAtt){
		return Math.exp(-kLAtt * gLAI);
	}
	
	
	/** Leaf area index
	 * 
	 * Plant.cs method LeafAreaIndex() line 672
	 * 
	 * @param gCumulativeFoliageArea cumulative foliage area above 
	 * @param kPatchSize the size of the patch //TODO what are the units?
	 * @return the leaf area index corresponding to the input foliage area
	 */
	public static double leafAreaIndex(double gCumulativeFoliageArea, double kPatchSize){
		return gCumulativeFoliageArea / kPatchSize;
	}
	
	
	
	
	//================================================================================
	//
    // The following methods are from the ForClim Species class
	// C# source: Species.cs 
    //
	// ================================================================================
	
	
	
	/** Calculate the likelihood of browsing. <br>
	 * 
	 * Species.cs in method Initialize() lines 182 - 189<br>
	 * 
	 * TODO this is bad practice to have these values hard-coded
	 * 
	 * @param kBrow browsing susceptibility level
	 * @param kBrPr Browsing pressure intensity
	 * @return browsing probability
	 */
	public static double browsingProbability(int kBrow, double kBrPr){
		double kBrPrAdj = 0.01 * kBrPr;
		switch(kBrow) //new relationship between kBrP and kBrPr for 5 levels of kBrow [MD]
		{
			case 1: return Math.pow(kBrPrAdj, 4);  
			case 2: return Math.pow(kBrPrAdj, 2);  
			case 3: return Math.pow(kBrPrAdj, 1);  
			case 4: return Math.pow(kBrPrAdj, 0.5); 
			case 5: return Math.pow(kBrPrAdj, 0.25); 
			default: return 0d;
		}
	}
	
	/** Calculate the slope of the s-change TODO find better description.
	 * 
	 * Species.cs Method Initialize() line 219
	 * 
	 * TODO find source for this equation
	 * 
	 * @param kLa shade tolerance of adult trees
	 * @return value of the slope parameter
	 */
	public static double sChangeSlope(double kLa){
		return 14d * kLa + 13d;
	}
	
	/** Calculate the minimum value for the s-parameter.  TODO need better description
	 * 
	 * Species.cs method Initialize line 220
	 * 
	 * @param kLa shade tolerance of adult trees
	 * @return minimum allowed value for the parameter
	 * 
	 * TODO find sources for these constants
	 */
	public static double sMinValue(double kLa){return 1.3 * kLa + 39.5;}
	
	
	/** Calculate the initial value for the skininess parameter. <br>
	 * 
	 * Species.cs method Initialize() line 221
	 * @param kSMin minimum value of the skininess parameter
	 * @param kE1 slope of skininess parameter change
	 * @return initial skininess parameter value
	 */
	public static double initialSkininess(double kSMin, double kE1){return kSMin + 0.75 * kE1;}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * 
	 * @param isDeciduous
	 * @param mDDSe
	 * @param mDDAn
	 * @param kDDMin
	 * @param kDrTol
	 * @param kRedMax
	 * @param kHMaxIn
	 * @return
	 */
	public static double hMaxReduction(
			boolean isDeciduous, double mDDSe, double mDDAn, 
			double kDDMin, double kDrTol, double kRedMax, double kHMaxIn)
	{
		double mDD;
        double mDr;
        
		double gRedFacDI = 0.0;		//% reduction of Hmax caused by drought
        double gRedFacDD = 0.0;		//% reduction of Hmax caused by degree days	
    	double gRedFac = 0.0;;	//overall reduction of Hmax
        double gHMax = 0.0;		//maximal possible tree height
        if(isDeciduous){
        	mDD = mDDSe;
        	mDr = mDDSe;
        } else
        {
        	mDD = mDDAn;
        	mDr = mDDAn;
        }
		double toAdd = 470.947;
		if(isDeciduous) toAdd = 352.9838;
        double gDDOpt = kDDMin + toAdd;
		
        //Reduction caused by drought
        gRedFacDI = 100 - (mDr / (kDrTol / (100 - kRedMax)));
		gRedFacDI = Math.max(gRedFacDI, kRedMax);
		
		//Reduction caused by available degree days
		if (kDDMin < gDDOpt) 
		{
			gRedFacDD = 100-((gDDOpt-mDD)*((100-kRedMax)/(gDDOpt-kDDMin)));
			gRedFacDD = Math.min(gRedFacDD,100);
			gRedFacDD = Math.max(gRedFacDD,kRedMax);
		}
		else {gRedFacDD = 100.0;}
		
		//Calculation of final reduction of Hmax
		gRedFac = Math.min(gRedFacDI,gRedFacDD);
//		gRedFac = Math.Min(Math.Min(gRedFacAvN,gRedFacDI),gRedFacDD);
		
		gHMax = kHMaxIn / 100d * gRedFac;
		gHMax *= 100d;	//m to cm
		
		return gHMax;
		
	}
	
	
	
	
	

	/** ForClim v.3.0 equation 4.3
	 * @param gLAI0 leaf area index above the cohort //TODO? this is unclear in the maual
	 * @param kLAImax 
	 * @return
	 */
	public static double forClimLeafAreaIndexShadingFactor(double gLAI0, double kLAImax){
		return Math.min(1d, gLAI0 / kLAImax); 
	} //TODO verify in C# source



	/** ForClim v.3.0 equation 4.5
	 * @param kPatchSize patch size in square meters
	 * @param gFolAreaAbove cumulative foliage area above the cohort.
	 * @return cumulative leaf area index at a cohort height
	 */
	public static double forClimLeafAreaIndexAtCohortHeight(double kPatchSize, double gFolAreaAbove){
		return gFolAreaAbove / kPatchSize;
	} //TODO verify in C# source May be duplicate of method above

//	/** ForClim v.3.0 equation 4.6
//	 * @param kLatt light attenuation coefficient
//	 * @param gLAIh cumulative leaf area index above cohort
//	 * @return
//	 */
//	public static double forClimAvailableLight(double kLatt, double gLAIh){
//		return Math.exp(-kLatt * gLAIh);
//	} //TODO verify in C# source

	/** ForClim v.3.0 equation 4.7
	 * 
	 * @param kG species-specific growth parameter
	 * @param dbh current dbh
	 * @param height current height
	 * @param hMax max height for the species
	 * @param fH function that distributes volume growth between height and diameter
	 * @return
	 */
	public static double forClimIdealDiameterIncrement(double kG, double dbh, double height, double hMax, double fH){
		double numerator = 1d - (height / hMax);
		double denominator = 2d * height + fH * dbh;
		return(kG * dbh * (numerator / denominator));
	} //TODO verify in C# source

	/** ForClim v.3.0 equation 4.8
	 * @param dDGF degree days growth factor
	 * @param sMGF soil moisture growth factor
	 * @param sNGF soil nitrogen growth factor
	 * @param aLGF available light growth factor
	 * @return
	 */
	public static double forClimGrowthReductionFactor(double dDGF, double sMGF, double sNGF, double aLGF){
		return Math.pow(dDGF * sMGF * sNGF * aLGF, 0.3333333);
	} //TODO verify in C# source

	/** ForClim v 3.0 equation 4.9 
	 * @param speciesDegDayMin
	 * @param degDaySlopeParam
	 * @param speciesDegreeDaySum
	 * @return
	 */
	public static double forClimDegreeDayGrowthFactor(
			double speciesDegDayMin, double degDaySlopeParam, double speciesDegreeDaySum)
	{return Math.max(0, 1d - Math.exp((speciesDegDayMin - speciesDegreeDaySum) * degDaySlopeParam));} //TODO verify in C# source

	/** ForClim v3.0 equation 4.10
	 * @param gDr annual drought index
	 * @param kGrTol species-specific drought tolerance parameter
	 * @return
	 */
	public static double forClimSoilMoistureGrowthFactor(double gDr, double kGrTol){
		return Math.sqrt(Math.max(0, 1d - gDr / kGrTol));
	} //TODO verify in C# source

	/** ForClim v.3.0 equation 4.11 
	 * @param kN1 species-specific nitrogen response parameter
	 * @param kN2 species-specific nitrogen repsonse parameter
	 * @param uAvN available nitrogen in kg/ha
	 * @return
	 */
	public static double forClimSoilNitrogenGrowthFactor(double kN1, double kN2, double uAvN){
		return Math.max(0, 1d - Math.exp(kN1 * (uAvN - kN2)));
	} //TODO verify in C# source



	
	
	
	/** ForClim v.3.0 equation 4.15a ForClim manual version
	 * @param kCLGF_a crown length growth factor parameter, hard-coded in the original as 4/3
	 * @param gA1 allometric parameter for dbh and foliage weight relationship
	 * @param kA1max maximum value for parameter gA1
	 * @param kLCPs species-specific light compensation point
	 * @param kLCPmean mean light compensation point considering all species
	 * @return
	 */
	public static double forClimCrownLengthGrowthFactor(double kCLGF_a, double gA1, double kA1max,double kLCPs, double kLCPmean){
		return Math.min(kCLGF_a * (gA1 * kLCPs) / (kA1max * kLCPmean), 1d); 
	} //TODO verify in C# source
	/** ForClim v.3.0 equation 4.15b ForClim C# source version
	 * @param kCLGF_a crown length growth factor parameter, hard-coded in the original as 4/3
	 * @param gA1 allometric parameter for dbh and foliage weight relationship
	 * @param kA1max maximum value for parameter gA1
	 * @param kA1min minimum value for parameter gA1
	 * @param kLCPs species-specific light compensation point
	 * @param kLCPmean mean light compensation point considering all species
	 * @return
	 */
	public static double forClimCrownLengthGrowthFactor(double kCLGF_a, double gA1, double kA1max, double kA1min, double kLCPs, double kLCPmean){
		return Math.min(kCLGF_a * (gA1 * kLCPs) / ((kA1max - kA1min) * kLCPmean), 1d); // TODO the C# code is different here
	} //TODO verify in C# source

	public static double forClimInitialHeight(double kB1, double hMax, double kSIn, double dbh){
		double adjHMax = hMax - kB1;
		double proportion = Math.exp(-kSIn * dbh / adjHMax);
		double unadjustedOut = adjHMax * proportion;
		return kB1 + unadjustedOut;
	} //TODO verify in C# source

	/** ForCoim v.3.0 equation 4.16
	 * 
	 * @param dbh current dbh
	 * @param fH function distributing volume growth among height and dbh
	 * @return
	 */
	public static double forClimHeightIncrement(double dbh, double fH){
		return fH * dbh;
	} //TODO verify in C# source

	/** From the ForClim v 3.0 equation 4.17. 
	 * @param height
	 * @param maxHeight
	 * @param b1
	 * @param competition
	 * @return
	 */
	public static double forClimFH(double height, double maxHeight, double b1, double competition){
		return competition * (1d - (height - b1) / (maxHeight - b1));
	} //TODO verify in C# source

	/** ForClim v 3.0 equation 4.18
	 * @param sMin species specific parameter: the smallest value the s-parameter can take
	 * @param kE1 species specific slope of the s-change
	 * @param aLH light intensity at the top of the tree's (cohort's) crown.
	 * @return
	 */
	public static double forClimCompetition(double sMin, double kE1, double aLH){
		return sMin + kE1 * (1d - aLH);
	} //TODO verify in C# source

	/** ForClim v3.0 height reduction equation from C# code
	 * 
	 * @param mDD mean (annual or seasonal) degree days based on long-term data;
	 * @param mDr mean (annual or seasonal) drought index based on long-term mean data;
	 * @param kDDMin minimal annual degree day sum for the species to grow.
	 * @param kDDRedRange the magnitude of the range in degree days between kDDMin and the value after which degree days are no longer limiting
	 * @param kDrTol drought tolerance parameter
	 * @param kRedMax greatest percent by which the max height may be reduced.
	 * @param kHMaxIn the species-specific unadjusted maximum height under nonlimiting conditiosn
	 * @param uAvN (not currently used) nitrogen availability in kg * ha-1
	 * @param kN2 (not currently used) nitrogen response parameter in kg * ha-1 * yr -1
	 * @param gAvNOpt (not currently used) value after which nitrogen is no longer limiting
	 * @return
	 */
	public static double forClimAdjustedMaxHeight(
			double mDD, double mDr, 
			double kDDMin, double kDDRedRange, //TODO make this a parameter 
			double kDrTol,
			double kRedMax, double kHMaxIn)
	//			double uAvN, double kN2, double gAvNOpt) /* these are commented out in the C# code. */
	{
		/* lowest possible value for overall reduction percent*/
		double minHmaxPct = 100d - kRedMax;

		/* Reduction caused by drought */
		double gRedFacDI = 100d - mDr * minHmaxPct / kDrTol;
		gRedFacDI = Math.max(gRedFacDI,  kRedMax);

		//		/* Reduction caused by nitrogen content. 
		//		 * This is commented out in the C# code!*/
		//		double gRedFacAvN = 100d - (gAvNOpt - uAvN) * minHmaxPct / (gAvNOpt - kN2);
		//		gRedFacAvN = Math.min(Math.max(gRedFacAvN, 100d), kRedMax);

		/* Reduction caused by degree days. */
		double gRedFacDD = 100d;
		/* Value after which degree days are no longer limiting. */
		double gDDOpt = kDDMin + kDDRedRange;
		if(kDDMin < gDDOpt){
			gRedFacDD = 100d - (gDDOpt - mDD) * minHmaxPct / (gDDOpt - kDDMin);
		} //TODO verify in C# source

		/* Final reduction is the lowest of the component reductions. */
		//		double finalRedFac = Math.min(gRedFacDI, Math.min(gRedFacAvN, gRedFacDD));
		double finalRedFac = Math.min(gRedFacDD, gRedFacDI) / 100d;
		return kHMaxIn * finalRedFac;
	} //TODO verify in C# source

	/** ForClim v.3.0 equation 4.19
	 *  Equations 4.20 to 4.26 are implemented within this method.
	 * @param unif a uniform random number generator
	 * @param kEstP rate of seedling establishment (0 - 1) to account for other factors (pathogens, etc.)
	 * @param kDrTol species-specific drought tolerance
	 * @param uDr this season's drought index
	 * @param kWiTX winter low temp maximum threshold
	 * @param uWiT observed winter minimum temperature
	 * @param kWiTN winter low temp minimum threshold 
	 * @param kDDMin species-specific minimum degree day requirement
	 * @param uDD observed sum of degree days during growing season
	 * @param kLy sapling shade tolerance
	 * @param uAL0 light availability on the forest floor
	 * @param kBrPr browsing pressure
	 * @param aKbrow exponent for levels of browsing sensitivity
	 * @return the probability that any seedlings of the species establish this year
	 */
	public static double forClimProbabilityEstablishment(
			Uniform unif, double kEstP, double kDrTol, 
			double uDr, double kWiTx, double uWiT, double kWiTN, 
			double kDDMin, double uDD, double kLy, double uAL0, 
			double kBrPr)
	{

		double out = 0d;

		/* Soil moisture establishment flag */
		if(kDrTol > uDr){
			/* Winter temperature establishment flag */
			if(kWiTx > uWiT & uWiT > kWiTN){
				/* Degree days flag */
				if(kDDMin < uDD){
					/* Light availablilty flag. */
					if(kLy < uAL0){
						/* Browsing probability flag */
						if(unif.nextDouble() > kBrPr){
							out = kEstP;
						} 
			}}}}

		//TODO implement immigration flag?
		return out;
	} //TODO verify in C# source

	/** ForClim v 3.0 equation 4.27
	 * 
	 * @param kEstDens maximum sapling establishment rate
	 * @param kPatchSize size of the forest patch in hectares
	 * @param kLa species-specific shade tolerance
	 * @return
	 */
	public static int forClimMaximumSaplingEstablishmentRate(double kEstDens, double kPatchSize, double kLa){
		return (int)(0.5 + kEstDens * (kPatchSize * 1E4) * kLa); // Remember that patch size is in hectares!
	} //TODO verify in C# source

	/** ForClim v.3.0 equation 4.29
	 * @param kDeathP mortality probability coefficient
	 * @param kAm species-specific maximum tree age
	 * @return
	 */
	public static double forClimBackgroundMortalityPct(double kDeathP, double kAm){
		return kDeathP / kAm;
	} //TODO verify in C# source


	
	/** ForClim v.3.0 equation 4.31: Increment the counter of slow growth years.
	 * @param sGrC number of years a cohort has grown slowly
	 * @param growthReductionFactor the calculated growth reduction factor from the grow() method
	 * @param kMinRelInc minimum threshold percentage of the ideal diameter growth increment below which growth is considered slow
	 * @param dbhIncrement the cohort's trees' most recent dbh increment in cm
	 * @param kMinAbsInc the minimum threshold of annual radial growth below which growth is considered slow
	 * @return incremented counter if growth is slow, 0 otherwise	 */
	public static int forClimSlowGrowthIncrementer(int sGrC, double growthReductionFactor, double kMinRelInc, double dbhIncrement, double kMinAbsInc){
		if(growthReductionFactor < kMinRelInc | dbhIncrement < kMinAbsInc) return sGrC +1;
		else return 0;
		
	} //TODO verify in C# source
	


	
	
	
	
} //TODO verify in C# source


