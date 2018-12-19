package gapModels;

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class TestGapModelCalculator {

	Logger logger = LogManager.getLogger();

	/* Values from the parameter table in the original publication. */
	double[] heightMax;
	double[] dbhMax;
	double[] b2; 
	double[] b3; 
	int[] ageMax;
	double[] g;

	@Before
	public void setup()
	{
		/* Values from the parameter table in the original publication. */
		heightMax = new double[] {40.11, 36.6,  30.5,  21.6,  5,    10,   11.26, 5,    18.3,  18.3,  18.3, 5,    36.6 };
		dbhMax = new double[]    {152.5, 122,   122,   50,    13.5, 22.5, 28,    10,   50,    50,    46,   10,   152.5 };
		b2 = new double[]        {50.9,  57.8,  47.8,  80.2,  53.8, 76.6, 70.6,  72.6, 67.9,  67.9,  73.6, 72.6, 46.3 };
		b3 = new double[]        {0.167, 0.237, 0.196, 0.802, 2,    1.7,  1.26,  3.63, 0.679, 0.679, 0.8,  3.63, 0.152 };
		ageMax = new int[]       {200,   300,   300,   100,   25,   30,   30,    20,   80,    80,    80,   30,   150 };
		g = new double[]         {170,   150,   100,   130,   150,  150,  200,   150,  200,   200,   140,  150,  240 };
	}

	double b1 = 1.37;
	double tol = 0.01;

	@Test
	public void testGDD()
	{
		double[] elevation = new double[] {0, 400, 1000, 1500 };
		double[] gdd = new double[] {4000, 3000, 2000, 1000 };
		tol = 200;
		
		double julyMean = 66.7;
		double janMean = 16.8;
		double baseElev = 290;
		double conversionFactor = (9d / 5d);

		/* Lapse rates from Botkin et al 1972; 
		 * Rates given in degrees C per 1000 m:*/
		double julyLapseRate = 6.4;
		double janLapseRate = 3.9;
		
		julyLapseRate *= conversionFactor;
		janLapseRate *= conversionFactor;
		
		for (int i = 0; i < gdd.length; i++)
		{
			double july = GapModelCalculator.jaBoWaLapseTemperature(julyMean, elevation[i] - baseElev, julyLapseRate);
			double jan = GapModelCalculator.jaBoWaLapseTemperature(janMean, elevation[i] - baseElev, janLapseRate);
			assertEquals(GapModelCalculator.jaBoWaDegreeDaysTwoPointEstimate(july, jan, 40), gdd[i], tol);
		}
	}
	
	
	@Test
	public void testAllometricPolynomialHeight() 
	{
		/* Height should be close to the heightMax when dbhMax is used as input. */
		for (int i = 0; i < dbhMax.length; i++)
			assertEquals(GapModelCalculator.allometricPolynomialHeight(b1, b2[i], b3[i], dbhMax[i]), heightMax[i], tol * heightMax[i]);
	}

	@Test
	public void testAllometricParameterB2_B3()
	{
		/* should provide estimates close to those given in the original paper table: */
		for (int i = 0; i < dbhMax.length; i++)
		{
			assertEquals(GapModelCalculator.allometricHeightParameterB2(b1, heightMax[i], dbhMax[i]), b2[i], tol * b2[i]);
			assertEquals(GapModelCalculator.allometricHeightParameterB3(b1, heightMax[i], dbhMax[i]), b3[i], tol * b3[i]);
		}
	}

	@Test
	public void testGrowthParameterG()
	{
		tol = 0.11;

		/* This test fails because they adjusted g for some of the species in order to 
		 * get more reasonable growth rates. */
		g[1] = 106;
		g[3] = 190;
		g[4] = 190;
		g[5] = 305;
		g[6] = 344;
		g[7] = 233;
		g[10] = 204;
		for (int i = 0; i < dbhMax.length; i++)
		{
			//			logger.info(i);
			assertEquals(GapModelCalculator.growthParameterG_Jabowa(ageMax[i], dbhMax[i], heightMax[i]), g[i], tol * g[i]);
		}
	}

	@Test
	public void testOptimalIncrement()
	{
		/* Table 1 footnote h of Botkin states that growth should be about 
		 * two thirds of maximum diameter at one-half maximum age starting from 0.5 cm dbh.*/

		/* This fails if we use the adjusted g coefficients from the table... 
		 * It works if we use the calculated parameter value. */

		tol = 0.2;

		double dbh, height, dbhExpected, dbhTol, g1;
		
		for (int i = 0; i < dbhMax.length; i++)
		{
//			g1 = g[i]; 
			g1 =  GapModelCalculator.growthParameterG_Jabowa(ageMax[i], dbhMax[i], heightMax[i]);
			int age1 = (int)((double)ageMax[i] * 0.5);
			
			dbh = 0.5;
			height = GapModelCalculator.allometricPolynomialHeight(b1, b2[i], b3[i], dbh);
			for (int age = 0; age < age1; age++)
			{
				dbh += GapModelCalculator.allometricMaxDiameterIncrementDiskJabowa(
						g1, dbh, height, dbhMax[i], heightMax[i], b1, b2[i], b3[i]);
				height = GapModelCalculator.allometricPolynomialHeight(b1, b2[i], b3[i], dbh);
			}
			
			dbhExpected = 0.67 * dbhMax[i];
			dbhTol = tol * dbhMax[i];
			assertEquals(dbh, dbhExpected, dbhTol);

			for(int age = age1; age < ageMax[i]; age++)
			{			
				dbh += GapModelCalculator.allometricMaxDiameterIncrementDiskJabowa(
						g1, dbh, height, dbhMax[i], heightMax[i], b1, b2[i], b3[i]);
				height = GapModelCalculator.allometricPolynomialHeight(b1, b2[i], b3[i], dbh);
			}
			dbhExpected = 1.0 * dbhMax[i];
			assertEquals(dbh, dbhExpected, dbhTol);
			
			/* continue for additional years, the dbh should not increase. */
			for(int age = 0; age < 100; age++)
			{			
				dbh += GapModelCalculator.allometricMaxDiameterIncrementDiskJabowa(
						g1, dbh, height, dbhMax[i], heightMax[i], b1, b2[i], b3[i]);
				height = GapModelCalculator.allometricPolynomialHeight(b1, b2[i], b3[i], dbh);
			}
			assertEquals(dbh, dbhExpected, dbhTol);
		}
	}


	@Test
	public void testLightCurve()
	{
		tol = 0.02;
		/* Shade tolerant 
		 * The test values were estimated from the light response curve figure in
		 * Botkin et al. 1972. */
		
		double[] aL = new double[]        {0.9,  0.53, 0.3, 0.05 };
		double[] adjLTol = new double[]   {1,    0.9,  0.7, 0d };
		double[] adjLIntol = new double[] {1.35, 0.9,  0.5, 0d };
		
		for (int i = 0; i < aL.length; i++)
		{
			assertEquals(GapModelCalculator.exponentialLightResponseCurve(aL[i], 1d, 4.64, 0.05), adjLTol[i], adjLTol[i] * tol);
			assertEquals(GapModelCalculator.exponentialLightResponseCurve(aL[i], 2.24, 1.136, 0.08), adjLIntol[i], adjLIntol[i] * tol);
		}
	}
}
