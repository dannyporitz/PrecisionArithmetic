package com.poritz.precisionarithmetic;
import java.util.Arrays;

/* A class that stores and operates on arbitrarily large numbers
 * Note: This version only works with non-negative integers
 */
public class PrecisionNumber implements Comparable<PrecisionNumber> {
	private int size;
	private int[] digits;
	
	/** Some tests with precision numbers **/
//	public static void main(String[] args) {
//		
//		PrecisionNumber num1 = new PrecisionNumber("3171348097513498045370894531314352");
//		PrecisionNumber num2 = new PrecisionNumber("7412342568935865342789346289523475");
//		
//		PrecisionNumber result = add(num1, num2);
//		System.out.println(result);
//		
//		PrecisionNumber num3 = new PrecisionNumber("1351324079834212362438962349872346198234983247425");
//		PrecisionNumber num4 = new PrecisionNumber("51223451313247097123409574093450189154390834109784180");
//		
//		PrecisionNumber result2 = multiply(num3, num4);
//		System.out.println(result2);
//		
//		PrecisionNumber num5 = new PrecisionNumber("894");
//		PrecisionNumber num6 = new PrecisionNumber("901");
//		
//		PrecisionNumber result3 = subtract(num5, num6);
//		System.out.println(result3);
//		
//		PrecisionNumber num7 = new PrecisionNumber("3241892");
//		PrecisionNumber num8 = new PrecisionNumber("3221892");
//		System.out.println(num7.compareTo(num8));
//
//		PrecisionNumber num9 = new PrecisionNumber("98123476213135134");
//		PrecisionNumber num10 = new PrecisionNumber("1534876153");
//		
//	    PrecisionNumber result4 = divide(num9, num10);
//		System.out.println(result4);
//		
//		PrecisionNumber result5 = mod(num9, num10);
//		System.out.println(result5);
//		
//		PrecisionNumber num11 = new PrecisionNumber("7");
//		PrecisionNumber num12 = new PrecisionNumber("4");
//		
//		PrecisionNumber result6 = power(num11, num12);
//	    System.out.println(result6);
//		
//		System.out.println(fib(10000));
//		
//	}
	
	public PrecisionNumber(String number) {
		// Strip off leading zeroes (until the number is a single 0)
		int i = 0;
		while (i < number.length() - 1 && number.charAt(i) == '0') {
			i++;
		}
		number = number.substring(i, number.length());
		size = number.length();
		digits = new int[size];
		String[] digitStrings = number.split("");
		for (int j = 0; j < digitStrings.length - 1; j++) {
			digits[j] = Integer.parseInt(digitStrings[j + 1]); // Since split() into single chars leaves a empty string in front
		}	
	}
	
	private PrecisionNumber(int[] digits) {
		this(digits, 0);
	}
	
	private PrecisionNumber(int[] digits, int byPowerOf10) {
	    // Strip off leading zeros
		int i = 0;
		while (i < digits.length - 1 && digits[i] == 0) {
			i++;
		}
	    this.digits = Arrays.copyOfRange(digits, i, digits.length + byPowerOf10);
		size = this.digits.length;		
	}
	
	public static PrecisionNumber add(PrecisionNumber num1, PrecisionNumber num2) {
		PrecisionNumber bigger = (num1.size >= num2.size) ? num1 : num2;
		PrecisionNumber smaller = (bigger == num2) ? num1 : num2;
		
		int[] result = new int[bigger.size + 1];
		int carry = 0;
		int j = smaller.size - 1;
		for (int i = bigger.size - 1; i >= 0; i--) {
			if (j >= 0) {
				result[i + 1] = (bigger.digits[i] + smaller.digits[j] + carry) % 10;
				carry = (bigger.digits[i] + smaller.digits[j] + carry) / 10;
				j--;
			}
			else {
			    result[i + 1] = (bigger.digits[i] + carry) % 10;
			    carry = (bigger.digits[i] + carry) / 10;
			}
		}
		result[0] = carry;
		return new PrecisionNumber(result);
	}
	
	public static PrecisionNumber subtract(PrecisionNumber num1, PrecisionNumber num2) {
		PrecisionNumber bigger = (num1.compareTo(num2) >= 0) ? num1 : num2;
		PrecisionNumber smaller = (bigger == num2) ? num1 : num2;
		
		int[] result = new int[bigger.size];
		int borrow = 0;
		int j = smaller.size - 1;
		for (int i = bigger.size - 1; i >= 0; i--) {
			result[i] = bigger.digits[i] - (j >= 0 ? smaller.digits[j] : 0) - borrow;
			j--;
			if (result[i] < 0) {
				result[i] += 10;
				borrow = 1;
			}
			else {
				borrow = 0;
			}
		}
		return new PrecisionNumber(result);
	}
	
	public static PrecisionNumber multiply(PrecisionNumber num1, PrecisionNumber num2) {
		PrecisionNumber bigger = (num1.size >= num2.size) ? num1 : num2;
		PrecisionNumber smaller = (bigger == num2) ? num1 : num2;
		
	    PrecisionNumber result = new PrecisionNumber("0");
		int offset = 0;
		int carry;
		int k; // Index for the digits resulting from the current multiplier
		
		for (int i = smaller.size - 1; i >= 0; i--) {
			
			int[] currentSum = new int[bigger.size + 1];
			k = currentSum.length - 1;
			carry = 0;
				
			for (int j = bigger.size - 1; j >= 0; j--) {
				currentSum[k] = (smaller.digits[i] * bigger.digits[j] + carry) % 10;
				carry = (smaller.digits[i] * bigger.digits[j] + carry) / 10;
				k--;
			}
			currentSum[0] = carry;
			
			result = add(result, new PrecisionNumber(currentSum, offset));
			offset++;
		}
		return result;		
	}
	
	// Note: Does integer division (as Java normally does)
	public static PrecisionNumber divide(PrecisionNumber num1, PrecisionNumber num2) {	
		PrecisionNumber bigger = (num1.compareTo(num2) >= 0) ? num1 : num2;
		PrecisionNumber smaller = (bigger == num2) ? num1 : num2;
		
		if (smaller.compareTo(new PrecisionNumber("0")) == 0) {
			throw new ArithmeticException("Error: Divide by zero");
		}
		
		int sizeDiff = bigger.size - smaller.size;
		int[] result = new int[sizeDiff + 1];
		
		for (int i = 0; i <= sizeDiff; i++) {
			int offset = sizeDiff - i;
			for (int digit = 9; digit >= 0; digit--) {
				PrecisionNumber currentMultiple = multiply(new PrecisionNumber(smaller.digits, offset), new PrecisionNumber(String.valueOf(digit)));
				if (currentMultiple.compareTo(bigger) <= 0) {
					result[i] = digit;
					bigger = subtract(bigger, currentMultiple);
					break;
				}
			}	
		}
		return new PrecisionNumber(result);
	}
	
	public static PrecisionNumber mod(PrecisionNumber num1, PrecisionNumber num2) {	
		// Simple mod formula utilizing the quotient from divide
		PrecisionNumber bigger = (num1.compareTo(num2) >= 0) ? num1 : num2;
		PrecisionNumber smaller = (bigger == num2) ? num1 : num2;
		PrecisionNumber quotient = divide(num1, num2);
		return subtract(bigger, multiply(quotient, smaller));
	    
		/*
		// The full mod algorithm. Very similar to divide. (Slightly faster than the short version)
		PrecisionNumber bigger = (num1.compareTo(num2) >= 0) ? num1 : num2;
		PrecisionNumber smaller = (bigger == num2) ? num1 : num2;
		
		int sizeDiff = bigger.size - smaller.size;
		PrecisionNumber result = bigger;
		
		for (int i = 0; i <= sizeDiff; i++) {
			int offset = sizeDiff - i;
			for (int digit = 9; digit >= 0; digit--) {
				PrecisionNumber currentMultiple = multiply(new PrecisionNumber(smaller.digits, offset), new PrecisionNumber(String.valueOf(digit)));
				if (currentMultiple.compareTo(result) <= 0) {
					result = subtract(result, currentMultiple);
					break;
				}
			}	
		}
		return result;
		*/
	}
	
	public static PrecisionNumber power(PrecisionNumber num1, PrecisionNumber num2) {
		PrecisionNumber result = new PrecisionNumber("1");
		while (num2.compareTo(new PrecisionNumber("0")) > 0) {
			result = multiply(result, num1);
			num2 = subtract(num2, new PrecisionNumber("1"));
		}
		return result;
	}
	
	public int compareTo(PrecisionNumber other) {
		if (this.size != other.size) {
			return this.size - other.size;
		}
		else {
			for (int i = 0; i < this.size; i++) {
				if (this.digits[i] != other.digits[i]) {
					return this.digits[i] - other.digits[i];
				}
			}
		}
		// If we get here, all of the digits are the same
		return 0;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < digits.length; i++) {
			builder.append(digits[i]);
		}
		return builder.toString();
	}
	
	// Fun methods to make use of these large numbers	
	public static PrecisionNumber fib(int n) {
		PrecisionNumber[] f = new PrecisionNumber[Math.max(n + 1, 2)];
	    f[0] = new PrecisionNumber("0");
	    f[1] = new PrecisionNumber("1");
	    for (int i = 2; i <= n; i++) {
	    	f[i] = add(f[i-1], f[i-2]);
	    }
	    return f[n];	
	}
}
