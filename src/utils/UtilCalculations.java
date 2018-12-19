package utils;

public class UtilCalculations {

	
	public static double[] seq(double start, double end, double interval){
		
//		if((end > start) & interval >= 0d){throw new IllegalArgumentException();}
//		if((start > end) & interval <= 0d){throw new IllegalArgumentException();}
		interval = Math.abs(interval);
		if(start > end) interval = -interval;
		int nEntries = Math.abs((int)Math.ceil((end - start) / interval));
		double[] sequence = new double[nEntries];
		sequence[0] = start;
		for(int i = 1; i < nEntries - 1; i++){
			sequence[i] = sequence[i - 1] + interval;
		}
		sequence[nEntries - 1] = end;
		return sequence;
	}
	
	/** Create a sequence of integers with an interval of 1
	 * 
	 * @param start starting integer
	 * @param end ending integer
	 * @return
	 */
	public static int[] seq(int start, int end){
		int[] sequence = new int[Math.abs(end - start) + 1];
		
		int interval = 1;
		if(start > end) interval = -1;
		sequence[0] = start;
		for(int i = 1; i < sequence.length; i++){
			sequence[i] = sequence[i - 1] + interval;
		}
		return sequence;
	}
	
	public static double[] normalize(double[] input){
		double[] output = new double[input.length];
		
		double sum = 0;
		for(double i : output) sum += i;
		
		if(sum > 0){
			for(int i = 0; i < input.length; i++){
				output[i] = input[i] / sum;
			}
		}
		return output;
	}
	
	public static void main(String[] args){
		printArray(seq(5d, -3d, 0.5));
		printArray(seq(-5d, 3d, 0.5));
		printArray(seq(1, 6));
		printArray(seq(-1, -6));
	}
	
	
	/** Print the string representation of each element of an array1a */
	public static <T> void printArray(T[] arr){
		System.out.print("{");
		System.out.print(arr[0].toString());
		for(int i = 1; i < arr.length; i++){
			System.out.print(", " + arr[i].toString());
		}
		System.out.print("}\n");
	}
	
	/** Print the string representation of each element of an array1a */
	public static void printArray(double[] arr){
		System.out.print("{");
		System.out.print(arr[0]);
		for(int i = 1; i < arr.length; i++){
			System.out.print(", " + arr[i]);
		}
		System.out.print("}\n");
	}
	
	/** Print the string representation of each element of an array1a */
	public static void printArray(int[] arr){
		System.out.print("{");
		System.out.print(arr[0]);
		for(int i = 1; i < arr.length; i++){
			System.out.print(", " + arr[i]);
		}
		System.out.print("}\n");
	}
}
