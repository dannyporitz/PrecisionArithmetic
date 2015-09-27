package com.poritz.precisionarithmetic;
import java.util.Arrays;

/* A class that stores and operates on arbitrarily large numbers */
public class PrecisionNumber implements Comparable<PrecisionNumber> {
	private int size;
	private int[] digits;
	private boolean negative;
	
	private static final PrecisionNumber ZERO = new PrecisionNumber("0");
	private static final PrecisionNumber ONE = new PrecisionNumber("1");
	
	public PrecisionNumber(String number) {
		if (!isValidNumber(number)) {
			throw new IllegalArgumentException("Error: Invalid Number");
		}
		int i = 0;
		if (number.charAt(0) == '-') {
			negative = true;
			i = 1;
		}
		// Strip off leading zeroes (until the number is a single 0)
		while (i < number.length() - 1 && number.charAt(i) == '0') {
			i++;
		}
		number = number.substring(i);
		size = number.length();
		digits = new int[size];
		String[] digitStrings = number.split("");
		for (int j = 0; j < digitStrings.length; j++) {
			digits[j] = Integer.parseInt(digitStrings[j]);
		}
		
		// Special case: treat -0 as 0
		if (negative && number.equals("0")) negative = false;
	}
	
	private PrecisionNumber(int[] digits, int byPowerOf10, boolean negative) {
		// Strip off leading zeros
		int i = 0;
		while (i < digits.length - 1 && digits[i] == 0) {
			i++;
		}
		this.digits = Arrays.copyOfRange(digits, i, digits.length + byPowerOf10);
		size = this.digits.length;
		this.negative = negative;
		
		// Special case: treat -0 as 0
		if (this.negative && size == 1 && this.digits[0] == 0) this.negative = false;
	}
	
	private boolean isValidNumber(String number) {
		return number.matches("-?\\d+");
	}
	
	public static PrecisionNumber add(PrecisionNumber num1, PrecisionNumber num2) {
		// Addition pre-processing
		if (num1.negative == num2.negative) { // same sign --> final sign remains the same
			return add(num1, num2, num1.negative);
		}
		else { // different sign --> final sign is the same the one with the bigger magnitude
			int magComp = num1.compareTo(num2, true);
			return (magComp >= 0) ? subtract(num1, num2, num1.negative) : subtract(num2, num1, num2.negative);
		}
	}
	
	private static PrecisionNumber add(PrecisionNumber num1, PrecisionNumber num2, boolean negative) {
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
		return new PrecisionNumber(result, 0, negative);
	}
	
	public static PrecisionNumber subtract(PrecisionNumber num1, PrecisionNumber num2) {
		// Subtraction pre-processing
		if (num1.negative != num2.negative) { // different sign --> final sign is the same at the first number
			return add(num1, num2, num1.negative);
		}
		else { // same sign --> final sign is either the same if the first number has bigger magnitude, or the opposite if the second does
			int magComp = num1.compareTo(num2, true);
			return (magComp >= 0) ? subtract(num1, num2, num1.negative) : subtract(num2, num1, !num1.negative);
		}
	}
	
	private static PrecisionNumber subtract(PrecisionNumber num1, PrecisionNumber num2, boolean negative) {	
		int[] result = new int[num1.size];
		int borrow = 0;
		int j = num2.size - 1;
		for (int i = num1.size - 1; i >= 0; i--) {
			result[i] = num1.digits[i] - (j >= 0 ? num2.digits[j] : 0) - borrow;
			j--;
			if (result[i] < 0) {
				result[i] += 10;
				borrow = 1;
			}
			else {
				borrow = 0;
			}
		}
		return new PrecisionNumber(result, 0, negative);
	}
	
	public static PrecisionNumber multiply(PrecisionNumber num1, PrecisionNumber num2) {
		boolean negative = num1.negative ^ num2.negative;
		PrecisionNumber bigger = (num1.size >= num2.size) ? num1 : num2;
		PrecisionNumber smaller = (bigger == num2) ? num1 : num2;
		
		PrecisionNumber result = ZERO;
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
			
			result = add(result, new PrecisionNumber(currentSum, offset, negative));
			offset++;
		}
		return result;
	}
	
	// Note: Does integer division (as Java normally does)
	public static PrecisionNumber divide(PrecisionNumber num1, PrecisionNumber num2) {
		boolean negative = num1.negative ^ num2.negative;
		
		if (num2.compareTo(ZERO) == 0) {
			throw new ArithmeticException("Error: Divide by zero");
		}
		
		if (num2.compareTo(num1, true) > 0) {
			return ZERO;
		}
		
		int sizeDiff = Math.abs(num1.size - num2.size);
		int[] result = new int[sizeDiff + 1];
		
		for (int i = 0; i <= sizeDiff; i++) {
			int offset = sizeDiff - i;
			for (int digit = 9; digit >= 0; digit--) {
				PrecisionNumber currentMultiple = multiply(new PrecisionNumber(num2.digits, offset, false), new PrecisionNumber(String.valueOf(digit)));
				if (currentMultiple.compareTo(num1, true) <= 0) {
					result[i] = digit;
					num1 = subtract(num1, currentMultiple, false);
					break;
				}
			}	
		}
		return new PrecisionNumber(result, 0, negative);
	}
	
	// Note: Does Java's version of mod
	public static PrecisionNumber mod(PrecisionNumber num1, PrecisionNumber num2) {	
		// Simple mod formula utilizing the quotient from divide
		PrecisionNumber quotient = divide(num1, num2);
		return subtract(num1, multiply(quotient, num2));
		
		/*
		// The full mod algorithm. Very similar to divide. (Slightly faster than the short version)
		int sizeDiff = Math.abs(num1.size - num2.size);
		PrecisionNumber result = num1;
		
		for (int i = 0; i <= sizeDiff; i++) {
			int offset = sizeDiff - i;
			for (int digit = 9; digit >= 0; digit--) {
				PrecisionNumber currentMultiple = multiply(new PrecisionNumber(num2.digits, offset, false), new PrecisionNumber(String.valueOf(digit)));
				if (currentMultiple.compareTo(result, true) <= 0) {
					result = subtract(result, currentMultiple, false);
					break;
				}
			}	
		}
		return new PrecisionNumber(result.digits, 0, num1.negative);
		*/
	}

	public static PrecisionNumber power(PrecisionNumber num1, PrecisionNumber num2) {
		PrecisionNumber result = ONE;
		while (num2.compareTo(ZERO) > 0) {
			result = multiply(result, num1);
			num2 = subtract(num2, ONE);
		}
		return result;
	}
	
	public boolean equals(Object other) {
		if (this == other) return true;
		if (other == null || !(other instanceof PrecisionNumber)) return false;
		
		// equals() is best consistent with hashCode() but should be the same as: return this.compareTo((PrecisionNumber) other) == 0;
		PrecisionNumber num = (PrecisionNumber) other;
		return this.toString().equals(num.toString());
	}
	
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	public int compareTo(PrecisionNumber other) {
		return compareTo(other, false);
	}
	
	// Compares two numbers. If the magnitude flag is true, sign will be ignored
	private int compareTo(PrecisionNumber other, boolean magnitude) {
		int modifier = 1; // becomes -1 if both numbers are negative and we're not comparing magnitude
		
		if (!magnitude) {
			if (this.negative && other.negative) modifier = -1;
			else if (this.negative) return -1;
			else if (other.negative) return 1;
		}
		
		if (this.size != other.size) {
			return (this.size - other.size) * modifier;
		}
		else {
			for (int i = 0; i < this.size; i++) {
				if (this.digits[i] != other.digits[i]) {
					return (this.digits[i] - other.digits[i]) * modifier;
				}
			}
		}
		// If we get here, all of the digits are the same
		return 0;
	}
	
	public String toString() {
		return (negative ? "-" : "") +
			   Arrays.toString(digits).replaceAll("[ ,\\[\\]]", "");
	}
	
	/** Fun methods to make use of these large numbers **/
	public static PrecisionNumber fibonacci(int n) {
		if (n < 0) throw new IllegalArgumentException("Error: Fibonacci argument must be non-negative");
		PrecisionNumber[] f = new PrecisionNumber[Math.max(n + 1, 2)];
		f[0] = ZERO;
		f[1] = ONE;
		for (int i = 2; i <= n; i++) {
			f[i] = add(f[i-1], f[i-2]);
		}
		return f[n];
	}
	
	public static PrecisionNumber factorial(int n) {
		if (n < 0) throw new IllegalArgumentException("Error: Factorial argument must be non-negative");
		PrecisionNumber result = ONE;
		for (int i = 2; i <= n; i++) {
			result = multiply(result, new PrecisionNumber(String.valueOf(i)));
		}
		return result;
	}
}