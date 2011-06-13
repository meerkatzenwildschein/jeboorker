package org.rr.commons.utils;

import java.io.Serializable;

public final class MathUtils implements Serializable {
    
	private static final long serialVersionUID = -3880152775090057372L;

	private MathUtils() {}
    
	public static final int TYPE_0 = 0 ;
	public static final int TYPE_1 = 1 ;
	public static final int TYPE_2 = 2 ;
	public static final int TYPE_3 = 3 ;
	public static final int TYPE_4 = 4 ;
	
	/**
	 * Round a double value to the next closest number considing the specified number of decimal places.
	 *
	 * @param value The number to be rounded.
	 * @param places Specifies how many places to the right of the decimal are included in the rounding.
	 * @return The rounded value.
	 */
	public static final double round(double value, int places) {
	    long factor = (long)Math.pow(10,places);
	    // Shift the decimal the correct number of places to the right.
	    value = value * factor;
		return (double) Math.round(value) / factor;
	}

	/**
	 * Round a double value to the next greater number considing the specified number of decimal places.
	 *
	 * @param value The number to be rounded.
	 * @param places Specifies how many places to the right of the decimal are included in the rounding.
	 * @return The rounded value.
	 */
	public static final double roundUp(double value, int places) {
	    long factor = (long)Math.pow(10,places);
	    // Shift the decimal the correct number of places to the right.
	    value = value * factor;
		return (double) Math.ceil(value) / factor;
	}

	/**
	 * Round a double value to the next smaller number considing the specified number of decimal places.
	 *
	 * @param value The number to be rounded.
	 * @param places Specifies how many places to the right of the decimal are included in the rounding.
	 * @return The rounded value.
	 */
	public static final double roundDown(double value, int places) {
	    long factor = (long)Math.pow(10,places);
	    // Shift the decimal the correct number of places to the right.
	    value = value * factor;
		return (double) Math.floor(value) / factor;
	}

	/**
	 * Creates a string that represents the hexadecimal value of a specified number.
	 * 
	 * If number is not a whole number, it is rounded to the nearest whole number before being evaluated.
	 * @param value The number to be used for calculation.
	 * @return The hex <code>String</code> from the given number.
	 */
	public static final String hex(double value) {
		value = round(value, 0);
		return Integer.toHexString((int)value).toUpperCase();
	}

	/**
	 * Creates a string that represents the octal value of a specified number.
	 * 
	 * If number is not a whole number, it is rounded to the nearest whole number before being evaluated.
	 * @param value The number to be used for calculation.
	 * @return The hex <code>String</code> from the given number.
	 */
	public static final String oct(double value) {
		value = round(value, 0);
		return Integer.toOctalString((int)value);
	}
    
    /**
     * Just truncates the the rest after the decimal point. 
     * 
     * @param value The value to be truncated.
     * @return The truncated value.
     */
    public static final int fix(double value) {
        String stringValue = String.valueOf(value);
        int index = stringValue.indexOf('.');
        if (index!=-1) {
            stringValue = stringValue.substring(0, index);
            return Integer.parseInt(stringValue);
        } else {
            return (int) value;
        }
    }

	/**
	 * Creates an integer that indicates the sign of a specified number.
	 * 
	 * @param value A value to be determined if it has a positive or negative sign.
	 * @return If the specified number is:
	 * <ul>
	 * <li> &gt;0 - Sgn returns 1</li>
	 * <li> =0 - Sgn returns 0</li>
	 * <li> &lt;0 - Sgn returns -1</li>
	 */
	public static final int sgn(double value) {
		if (value>0) {
			return 1;
		} else if (value<0) {
			return -1;
		} else {
			return 0;
		}
	}

	/**
	 * Calulates a random value with a specified upper and lower <code>int</code> value.
	 * 
	 * @param lower The lower value.
	 * @param upper The lower value.
	 * @return A random value between the specified upper and lower value.
	 */
	public static int random(int lower, int upper) {
		return (int)(Math.random() * (double)((upper - lower) + 1)) + lower;
	}
	
	
	/**
	 * Gets the factorial of a number
	 * 
	 * @param number
	 *            The number, of which the factorial should be calculated
	 * @return the factorial of the number
	 */
	public static double fact(double number) {
		if (number < 0) {
			throw new RuntimeException("Negative value");
		}

		double zaehler = 1;
		double result = 1;
		for (; zaehler <= number; zaehler++) {
			result = result * zaehler;
		}

		return result;
	}

	/**
	 * Gets the polynominal of a group of numbers
	 * 
	 * @param numbers
	 *            The numbers of which the polynomial should be calculated
	 * @return The polynominal of the numbers
	 */
	public static double polynomial(double[] numbers) {
		for (int i = 0; i < numbers.length; i++) {
			if (numbers[i] < 1) {
				throw new RuntimeException("Negative value");
			}
		}

		double first = 0;
		double second = 1;

		for (int i = 0; i < numbers.length; i++) {
			first = first + numbers[i];
		}
		for (int j = 0; j < numbers.length; j++) {
			second = second * fact(numbers[j]);
		}

		return (fact(first)) / (second);
	}

	/**
	 * Gets the factorial of a number with a step lenght of 2
	 * 
	 * @param number
	 *            The number of which the factorial should be calculated
	 * @return The factorial of the number with a step lenght of 2
	 */
	public static double factdouble(double number) {
		if (number < 0) {
			throw new RuntimeException("Negative value");
		}

		double zaehler = 0;

		if (number % 2 == 0) {
			zaehler = 2;
		} else if (number % 2 == 1) {
			zaehler = 1;
		}

		double result = number;
		for (; zaehler <= number - 2; zaehler = zaehler + 2) {
			result = result * (zaehler);
		}

		return result;
	}

	
	/**
	 * Translates a arabic number to a roman number
	 * 
	 * @param number
	 *            Is the arabic number which has to be translated
	 * @param type
	 *            Sets the type of the roman number <br>
	 *            Parameters could be:<br>
	 *            <li>TYPE_0 : classic </li>
	 *            <li>TYPE_1 : shorter </li>
	 *            <li>TYPE_2 : shorter </li>
	 *            <li>TYPE_3 : shorter </li>
	 *            <li>TYPE_4 : simplified</li>
	 * @return The roman number as a text
	 */
	public static String roman(double number, int type) {
		if (number < 0) {
			throw new RuntimeException("Negative value");
		}
		if (number > 3999) {
			throw new RuntimeException("Value too high " + number);

		}

		if (type == TYPE_0) {
			return type0Roemisch(number);
		} else if (type == TYPE_1) {
			return type1Roemisch(number);
		} else if (type == TYPE_2) {
			return type2Roemisch(number);
		} else if (type == TYPE_3) {
			return type3Roemisch(number);
		} else if (type == TYPE_4) {
			return type4Roemisch(number);
		} else {
			return "";
		}

	}

	/**
	 * Translates a arabic number to a roman number in <code>TYPE_0</code>
	 * 
	 * @param number
	 *            Is the arabic number which has to be translated
	 * @return The roman number as a text in the <code>TYPE_0</code>
	 */
	
	//englisch
	private static String type0Roemisch(double number) {
		String result = "";

		while (number > 0) {
			if (number > 1000 || number == 1000) {
				result = result + "M";
				number = number - 1000;
			} else if (number > 900 || number == 900) {
				result = result + "CM";
				number = number - 900;
			} else if (number > 500 || number == 500) {
				result = result + "D";
				number = number - 500;
			} else if (number > 400 || number == 400) {
				result = result + "CD";
				number = number - 400;
			} else if (number > 100 || number == 100) {
				result = result + "C";
				number = number - 100;
			} else if (number > 90 || number == 90) {
				result = result + "XC";
				number = number - 90;
			} else if (number > 50 || number == 50) {
				result = result + "L";
				number = number - 50;
			} else if (number > 40 || number == 40) {
				result = result + "XL";
				number = number - 40;
			} else if (number > 10 || number == 10) {
				result = result + "X";
				number = number - 10;
			} else if (number > 9 || number == 9) {
				result = result + "IX";
				number = number - 9;
			} else if (number > 5 || number == 5) {
				result = result + "V";
				number = number - 5;
			} else if (number > 4 || number == 4) {
				result = result + "IV";
				number = number - 4;
			} else if (number > 1 || number == 1) {
				result = result + "I";
				number = number - 1;
			}
		}
		return result;
	}

	/**
	 * Translates a arabic number to a roman number in <code>TYPE_1</code>
	 * 
	 * @param number
	 *            Is the arabic number which has to be translated
	 * @return The roman number as a text in the <code>TYPE_1</code>
	 */
	
	//type terminus
	private static String type1Roemisch(double number) {
		String result = "";

		while (number > 0) {
			if (number > 1000 || number == 1000) {
				result = result + "M";
				number = number - 1000;
			} else if (number > 950 || number == 950) {
				result = result + "LM";
				number = number - 950;
			} else if (number > 900 || number == 900) {
				result = result + "CM";
				number = number - 900;
			} else if (number > 500 || number == 500) {
				result = result + "D";
				number = number - 500;
			} else if (number > 450 || number == 450) {
				result = result + "LD";
				number = number - 450;
			} else if (number > 400 || number == 400) {
				result = result + "CD";
				number = number - 400;
			} else if (number > 100 || number == 100) {
				result = result + "C";
				number = number - 100;
			} else if (number > 95 || number == 95) {
				result = result + "VC";
				number = number - 95;
			} else if (number > 90 || number == 90) {
				result = result + "XC";
				number = number - 90;
			} else if (number > 50 || number == 50) {
				result = result + "L";
				number = number - 50;
			} else if (number > 45 || number == 45) {
				result = result + "VL";
				number = number - 45;
			} else if (number > 40 || number == 40) {
				result = result + "XL";
				number = number - 40;
			} else if (number > 10 || number == 10) {
				result = result + "X";
				number = number - 10;
			} else if (number > 9 || number == 9) {
				result = result + "IX";
				number = number - 9;
			} else if (number > 5 || number == 5) {
				result = result + "V";
				number = number - 5;
			} else if (number > 4 || number == 4) {
				result = result + "IV";
				number = number - 4;
			} else if (number > 1 || number == 1) {
				result = result + "I";
				number = number - 1;
			}
		}

		return result;
	}

	/**
	 * Translates a arabic number to a roman number in <code>TYPE_2</code>
	 * 
	 * @param number
	 *            Is the arabic number which has to be translated
	 * @return The roman number as a text in the <code>TYPE_2</code>
	 */
	private static String type2Roemisch(double number) {
		String result = "";

		while (number > 0) {
			if (number > 1000 || number == 1000) {
				result = result + "M";
				number = number - 1000;
			} else if (number > 990 || number == 990) {
				result = result + "XM";
				number = number - 990;
			} else if (number > 950 || number == 950) {
				result = result + "LM";
				number = number - 950;
			} else if (number > 900 || number == 900) {
				result = result + "CM";
				number = number - 900;
			} else if (number > 500 || number == 500) {
				result = result + "D";
				number = number - 500;
			} else if (number > 490 || number == 490) {
				result = result + "XD";
				number = number - 490;
			} else if (number > 450 || number == 450) {
				result = result + "LD";
				number = number - 450;
			} else if (number > 400 || number == 400) {
				result = result + "CD";
				number = number - 400;
			} else if (number > 100 || number == 100) {
				result = result + "C";
				number = number - 100;
			} else if (number > 95 || number == 95) {
				result = result + "VC";
				number = number - 95;
			} else if (number > 90 || number == 90) {
				result = result + "XC";
				number = number - 90;
			} else if (number > 50 || number == 50) {
				result = result + "L";
				number = number - 50;
			} else if (number > 45 || number == 45) {
				result = result + "VL";
				number = number - 45;
			} else if (number > 40 || number == 40) {
				result = result + "XL";
				number = number - 40;
			} else if (number > 10 || number == 10) {
				result = result + "X";
				number = number - 10;
			} else if (number > 9 || number == 9) {
				result = result + "IX";
				number = number - 9;
			} else if (number > 5 || number == 5) {
				result = result + "V";
				number = number - 5;
			} else if (number > 4 || number == 4) {
				result = result + "IV";
				number = number - 4;
			} else if (number > 1 || number == 1) {
				result = result + "I";
				number = number - 1;
			}
		}

		return result;
	}

	/**
	 * Translates a arabic number to a roman number in <code>TYPE_3</code>
	 * 
	 * @param number
	 *            Is the arabic number which has to be translated
	 * @return The roman number as a text in the <code>TYPE_3</code>
	 */
	private static String type3Roemisch(double number) {
		String result = "";

		while (number > 0) {
			if (number > 1000 || number == 1000) {
				result = result + "M";
				number = number - 1000;
			} else if (number > 995 || number == 995) {
				result = result + "VM";
				number = number - 995;
			} else if (number > 990 || number == 990) {
				result = result + "XM";
				number = number - 990;
			} else if (number > 950 || number == 950) {
				result = result + "LM";
				number = number - 950;
			} else if (number > 900 || number == 900) {
				result = result + "CM";
				number = number - 900;
			} else if (number > 500 || number == 500) {
				result = result + "D";
				number = number - 500;
			} else if (number > 495 || number == 495) {
				result = result + "VD";
				number = number - 495;
			} else if (number > 490 || number == 490) {
				result = result + "XD";
				number = number - 490;
			} else if (number > 450 || number == 450) {
				result = result + "LD";
				number = number - 450;
			} else if (number > 400 || number == 400) {
				result = result + "CD";
				number = number - 400;
			} else if (number > 100 || number == 100) {
				result = result + "C";
				number = number - 100;
			} else if (number > 95 || number == 95) {
				result = result + "VC";
				number = number - 95;
			} else if (number > 90 || number == 90) {
				result = result + "XC";
				number = number - 90;
			} else if (number > 50 || number == 50) {
				result = result + "L";
				number = number - 50;
			} else if (number > 45 || number == 45) {
				result = result + "VL";
				number = number - 45;
			} else if (number > 40 || number == 40) {
				result = result + "XL";
				number = number - 40;
			} else if (number > 10 || number == 10) {
				result = result + "X";
				number = number - 10;
			} else if (number > 9 || number == 9) {
				result = result + "IX";
				number = number - 9;
			} else if (number > 5 || number == 5) {
				result = result + "V";
				number = number - 5;
			} else if (number > 4 || number == 4) {
				result = result + "IV";
				number = number - 4;
			} else if (number > 1 || number == 1) {
				result = result + "I";
				number = number - 1;
			}
		}

		return result;
	}

	/**
	 * Translates a arabic number to a roman number in <code>TYPE_4</code>
	 * 
	 * @param number
	 *            Is the arabic number which has to be translated
	 * @return The roman number as a text in the <code>TYPE_4</code>
	 */
	private static String type4Roemisch(double number) {
		String result = "";
		
		while (number > 0) {
			if (number > 1000 || number == 1000) {
				result = result + "M";
				number = number - 1000;
			} else if (number > 999 || number == 999) {
				result = result + "IM";
				number = number - 999;
			} else if (number > 995 || number == 995) {
				result = result + "VM";
				number = number - 995;
			} else if (number > 990 || number == 990) {
				result = result + "XM";
				number = number - 990;
			} else if (number > 950 || number == 950) {
				result = result + "LM";
				number = number - 950;
			} else if (number > 900 || number == 900) {
				result = result + "CM";
				number = number - 900;
			} else if (number > 500 || number == 500) {
				result = result + "D";
				number = number - 500;
			} else if (number > 500 || number == 500) {
				result = result + "D";
				number = number - 500;
			} else if (number > 499 || number == 499) {
				result = result + "ID";
				number = number - 499;
			} else if (number > 490 || number == 490) {
				result = result + "XD";
				number = number - 490;
			} else if (number > 450 || number == 450) {
				result = result + "LD";
				number = number - 450;
			} else if (number > 400 || number == 400) {
				result = result + "CD";
				number = number - 400;
			} else if (number > 100 || number == 100) {
				result = result + "C";
				number = number - 100;
			} else if (number > 95 || number == 95) {
				result = result + "VC";
				number = number - 95;
			} else if (number > 90 || number == 90) {
				result = result + "XC";
				number = number - 90;
			} else if (number > 50 || number == 50) {
				result = result + "L";
				number = number - 50;
			} else if (number > 45 || number == 45) {
				result = result + "VL";
				number = number - 45;
			} else if (number > 40 || number == 40) {
				result = result + "XL";
				number = number - 40;
			} else if (number > 10 || number == 10) {
				result = result + "X";
				number = number - 10;
			} else if (number > 9 || number == 9) {
				result = result + "IX";
				number = number - 9;
			} else if (number > 5 || number == 5) {
				result = result + "V";
				number = number - 5;
			} else if (number > 4 || number == 4) {
				result = result + "IV";
				number = number - 4;
			} else if (number > 1 || number == 1) {
				result = result + "I";
				number = number - 1;
			}
		}

		return result;
	}

	/**
	 * Gets the integer part of a division
	 * 
	 * @param numerator
	 *            Is the dividend
	 * @param denominator
	 *            Is the divisor
	 * @return The result of the division as a integer
	 */
	public static int quotient(double numerator, double denominator) {

		if(denominator==0){
			throw new RuntimeException("Negative value");
		}
		return (int) (numerator / denominator);
	}

	/**
	 * Gets the sum of powers
	 * 
	 * @param x
	 *            Is the value of the variables of the power series
	 * @param n
	 *            Is the first power you want x to upraise
	 * @param m
	 *            Is the increment you want n to extend in every link
	 * @param a
	 *            A group of coefficients you want to multipliy x
	 * @return The sum of the power series
	 */
	public static double powerseries(double x, double n, double m, double[] a) {
		double result = 0;

		for (int i = 0; i < a.length; i++) {
			result = result + a[i] * Math.pow(x, n + (m * i));
		}

		return result;
	}

	/**
	 * Rounds a number to the next integer value or to the next multiple of
	 * Schritt
	 * 
	 * @param value
	 *            The value which has to be rounded
	 * @param step
	 *            The value of which multiple has to be rounded
	 * @return The rounded value
	 */
	public static double ceiling(double value, double step) {
		if ((value < 0 && step > 0) || (value > 0 && step < 0)) {
			throw new RuntimeException("Negative value");
		}

		double result = 0;
		double zwresult = value / step;

		if (value % step == 0) {
			result = value;
		} else {
			result = step * Math.ceil(zwresult);

		}

		return result;

	}

	/**
	 * Gets the number of combinations of n with k elements
	 * 
	 * @param n
	 *            Is the total number of elements
	 * @param k
	 *            Sets how much elements in one combination have to be
	 * @return The number of combinations
	 */
	public static double combin(double n, double k) {
		if (k < 0 || n < 0) {
			throw new RuntimeException("Negative value");
		}
		if (n < k) {
			throw new RuntimeException("Wrong value for n " + n);
		}

		return fact(n) / (fact(k) * fact(n - k));
	}

	/**
	 * Converts degrees in radian
	 * 
	 * @param angle
	 *            Degrees to convert
	 * @return Value which represents the radian
	 */
	public static double radians(double angle) {

		return (angle * Math.PI) / 180;
	}

	/**
	 * Gets the square root of the number which is multiplied with pi
	 * 
	 * @param number
	 *            Is the number which has to ber multiplied with pi
	 * @return The square root of the number
	 */
	public static double sqrtpi(double number) {
		if (number < 0) {
			throw new RuntimeException("Negative value");
		}

		return Math.sqrt(number * Math.PI);
	}

	/**
	 * Gets the variance of the array values
	 * @param values The values of which the variance should be calculated
	 * @return The variance of a value
	 */
	public static double variance(double values[]){
		double sum=0;
		for(int i=0;i<values.length;i++){
			sum=sum+Math.pow(values[i],2);
		}
		
		double sum2=0;
		for(int i=0;i<values.length;i++){
			sum2=sum2+values[i];
		}
		
		double result=(values.length*sum-Math.pow(sum2,2)) / (Math.pow(values.length,2));
		
		return result;
	}
	
	/**
	 * Calculates the inverse of the given value.
	 * @param value Value to be calculated
	 * @return The inverse of the given value.
	 */
	public static double inv(double value) {
		 return 1. / value;
	}
	
	/**
	 * Determines if the specified <code>value</code> is between the <code>firstScope</code>
	 * and the <code>secondScope</code> value. 
	 * 
	 * @param value Value to be tested
	 * @param firstScope The first value which specifies the range where the <code>value</code> is to be tested.
	 * @param secondScope The second value which specifies the range where the <code>value</code> is to be tested.
	 * @return <code>true</code> if the <code>value</code> is between the <code>firstScope</code> and  <code>secondScope</code>
	 * value and <code>false</code> otherwise. 
	 */
	public static boolean between(double value, double firstScope, double secondScope) {
		//TODO Should be implemented to the FunctionNode
		if (value > firstScope && value < secondScope) {
			return true;
		}
		
		if (value > secondScope && value < firstScope) {
			return true;
		}		
		
		return false;
	}
	
	/**
	 * Calculates the difference between the two values specified with the
	 * patameters <code>value1</code> and <code>value2</code>. The result
	 * is a positive value in any case.
	 * 
	 * @param value1 First value which differenc should be caluclated
	 * @param value2 Second value which differenc should be caluclated
	 * @return The difference between the two values.
	 */
	public static double difference(double value1, double value2) {
		if (value1 > value2) {
			return value1 - value2;
		} else {
			return value2 - value1;
		}
	}

}
