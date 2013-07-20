package org.rr.commons.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public final class FinancialMathUtils implements Serializable {
	
	private static final long serialVersionUID = -1841958159779826300L;

	private FinancialMathUtils() {};

	private static final int BOOLEAN_TRUE = 1;

	private static final int BOOLEAN_FALSE = 0;

	/**
	 * Calculates the actual cash value of an annuity
	 * 
	 * @param rate
	 *            The interest rate for a period
	 * @param nper
	 *            The total time of the periods
	 * @param pmt
	 *            The payment of one period
	 * @param fv
	 *            The future value that has to be reached after the last payment
	 * @param type
	 *            The value which specifies when a payment has to be done
	 * @return A double value which stands for the actual cash value of an
	 *         annuity <br>
	 *         <br>
	 *         rate and nper must be assigned in the same time unit <br>
	 *         if nper is given in month the rate has to be given in month too
	 */
	public static double pv(double rate, int nper, double pmt, double fv, int type) {
		if (rate != 0) {
			double a = Math.pow(1 + rate, nper);
			double b = pmt * (1 + rate * type) * ((Math.pow(1 + rate, nper) - 1) / rate) + fv;

			return -b / a;

		} else {
			return -((pmt * nper) + fv);
		}

	}

	/**
	 * Calculates the capital share of a payment
	 * 
	 * @param rate
	 *            The interest rate for a period
	 * @param per
	 *            The period for which the ppmt has to be calculate
	 * @param nper
	 *            The total time of the periods
	 * @param pv
	 *            The actual cash value of the payment in the future
	 * @param fv
	 *            The future value that has to bee reached after the last
	 *            payment
	 * @param type
	 *            The value which specifies when a payment has to be done
	 * @return A double value which stands for the capital share of a payment
	 *         <br>
	 *         <br>
	 *         rate and nper must be assigned in the same time unit <br>
	 *         if nper is given in month the rate has to be given in month too
	 */
	public static double ppmt(double rate, int per, int nper, double pv, double fv, int type) {
		if (per < 1 || per > nper) {
			throw new RuntimeException("Wrong per argument " + per);
		}

		return pmt(rate, nper, pv, fv, type) - ipmt(rate, per, nper, pv, fv, type);
	}

	/**
	 * Calculates the payoff of an annuity
	 * 
	 * @param rate
	 *            The interest rate for a period
	 * @param nper
	 *            nper The total time of the periods
	 * @param pv
	 *            The actual cash value of the payment in the future
	 * @param fv
	 *            The future value that has to bee reached after the last
	 *            payment
	 * @param type
	 *            The value which specifies when a payment has to be done
	 * @return A double value which stands for the payoff of an annuity <br>
	 *         <br>
	 *         rate and nper must be assigned in the same time unit <br>
	 *         if nper is given in month the rate has to be given in month too
	 */
	public static double pmt(double rate, int nper, double pv, double fv, int type) {

		if (rate != 0) {
			double a = rate * (pv * Math.pow(1 + rate, nper) + fv);
			double b = (1 + rate * type) * ((Math.pow(1 + rate, nper) - 1));
			double result = -a / b;

			return result;
		} else {
			return -((pv + fv) / nper);
		}
	}

	/**
	 * Calculates the actual net cash value of a payment
	 * 
	 * @param rate
	 *            The interest rate for a period
	 * @param values
	 *            Cash flow values <br>
	 *            at least one positive and one negative value must be in the
	 *            array
	 * @return A double value which stands for the actual net cash value of a
	 *         payment
	 * 
	 */
	public static double npv(double rate, double[] values) {

		List<Double> positiveValues = new ArrayList<Double>(values.length);
		List<Double> negativeValues = new ArrayList<Double>(values.length);

		for (int i = 0; i < values.length; i++) {
			if (values[i] < 0) {
				negativeValues.add(Double.valueOf(values[i]));
			} else {
				positiveValues.add(Double.valueOf(values[i]));
			}
		}

		// if (positiveValues.size() == 0) {
		// throw (new
		// ExpressionInterpreter.InterpreterException(Bundle.getString("VBFinancialMath.err.NoPositiveValuesInArray")));
		// } else if (negativeValues.size() == 0) {
		// throw (new
		// ExpressionInterpreter.InterpreterException(Bundle.getString("VBFinancialMath.err.NoNegativeValuesInArray")));
		// }

		return calculateNpvValue(rate, values);
	}

	private static double calculateNpvValue(double rate, double[] values) {
		double result = 0;

		for (int i = 0; i < values.length; i++) {

			result = result + (values[i] / Math.pow(1 + rate, i + 1));
		}
		return result;
	}

	/**
	 * Calculates the total time of the periods of one payment
	 * 
	 * @param rate
	 *            The interest rate for a period
	 * @param pmt
	 *            The payment of one period
	 * @param pv
	 *            The actual cash value of the payment in the future
	 * @param fv
	 *            The future value that has to bee reached after the last
	 *            payment
	 * @param type
	 *            The value which specifies when a payment has to be done
	 * @return A double value which stands for the total time of the periods
	 */
	public static double nper(double rate, double pmt, double pv, double fv, int type) {

		if (rate != 0) {
			double a = pmt * (1 + rate * type) + (-1 / rate) * fv;
			double b = pv * rate + pmt * (1 + rate * type);
			double c = 1 + rate;

			return Math.log(a / b) / Math.log(c)*100;
		} else {
			return -((pv + fv) / pmt);
		}

	}

	/**
	 * Calculates the modified interior earning rate
	 * 
	 * @param values
	 *            Cash flow values at least on positive and one negative value
	 *            must be in the array
	 * @param finance_rate
	 *            The rate has to be paid at the financing of the arrangement
	 * @param reinvest_rate
	 *            The rate that can be reached at a new arrangement of capital
	 * @return A double value which stands for the modified interior earning
	 *         rate
	 */
	public static double mirr(double[] values, double finance_rate, double reinvest_rate) {

		List<Double> positiveValues = new ArrayList<Double>(values.length);
		List<Double> negativeValues = new ArrayList<Double>(values.length);

		for (int i = 0; i < values.length; i++) {
			if (values[i] < 0) {
				negativeValues.add(Double.valueOf(values[i]));
			} else {
				positiveValues.add(Double.valueOf(values[i]));
			}
		}

		if (positiveValues.size() == 0) {
                    throw new RuntimeException("No positive value found in array.");
		} else if (negativeValues.size() == 0) {
			throw new RuntimeException("No negative value found in array.");
		}

		double[] pArr = new double[positiveValues.size()];
		double[] nArr = new double[negativeValues.size()];

		for (ListIterator<Double> i = positiveValues.listIterator(); i.hasNext();) {
			pArr[i.nextIndex()] = i.next().doubleValue();
		}
		for (ListIterator<Double> i = negativeValues.listIterator(); i.hasNext();) {
			nArr[i.nextIndex()] = i.next().doubleValue();
		}

		return Math.pow((-calculateNpvValue(reinvest_rate, pArr) * Math.pow(1.0 + reinvest_rate, pArr.length))
				/ (calculateNpvValue(finance_rate, nArr) * (1.0 + finance_rate)), 1.0 / (values.length - 1.0)) - 1.0;
	}

	/**
	 * Calculates the future value of an annuity
	 * 
	 * @param rate
	 *            The interest rate for a period
	 * @param nper
	 *            The total time of the periods
	 * @param pmt
	 *            The payment of one period
	 * @param pv
	 *            The actual cash value of the payment in the future
	 * @param type
	 *            The value which specifies when a payment has to be done
	 * @return A double value which stands for the future value of an annuity
	 *         <br>
	 *         <br>
	 *         rate and nper must be assigned in the same time unit <br>
	 *         if nper is given in month the rate has to be given in month too
	 */
	public static double fv(double rate, double nper, double pmt, double pv, int type) {
		if (rate != 0) {
			return -(pv * Math.pow(1 + rate, nper) + pmt * (1 + rate * type) * ((Math.pow(1 + rate, nper) - 1) / rate));
		} else {
			return -((pmt * nper) + pv);
		}
	}

	/**
	 * Calculates the accrual asset for a period
	 * 
	 * @param cost
	 *            The original value of the effect
	 * @param salvage
	 *            The value of the effect at the end of the period
	 * @param life
	 *            The expected useful life of the effect
	 * @param period
	 *            The period for which the effect will be calculate
	 * @param factor
	 *            The value at which the effect is reduced in a period
	 * @return A double value which stands for the the accrual asset for a
	 *         period
	 */
	public static double ddb(double cost, double salvage, double life, double period, double factor) {

		double writedownForPeriod = 0;
		boolean reachedSalvage = false;

		if (life == 1) {
			life++;
		}

		for (; period >= 1; period--) {
			double oldcost = cost;

			writedownForPeriod = (((cost) * factor) / life);

			cost = cost - writedownForPeriod;

			if (reachedSalvage) {
				writedownForPeriod = 0;
			} else if (cost < salvage) {
				writedownForPeriod = oldcost - salvage;
				reachedSalvage = true;
			}

		}

		return writedownForPeriod;
	}

	/**
	 * Calculates the payment of interest for a period
	 * 
	 * @param rate
	 *            The interest rate for a period
	 * @param per
	 *            The period for which the ppmt has to be calculate
	 * @param nper
	 *            The total time of the periods
	 * @param pv
	 *            The actual cash value of the payment in the future
	 * @param fv
	 *            The future value that has to bee reached after the last
	 *            payment
	 * @param type
	 *            The value which specifies when a payment has to be done
	 * @return A double value which stands for the payment of interest for a
	 *         period <br>
	 *         <br>
	 *         rate and nper must be assigned in the same time unit <br>
	 *         if nper is given in month the rate has to be given in month too
	 */
	public static double ipmt(double rate, int per, int nper, double pv, double fv, int type) {
		if (per < 1 || per > nper || nper <= 0.0) {
                    throw new RuntimeException("Wrong per argument " + per);
		}

		double pmt = pmt(rate, nper, pv, fv, type);

		double zins = 0.0;

		if (type == BOOLEAN_FALSE) {
			for (int i = 0; i < per; i++) {
				zins = pv * rate;

				pv = pv + pmt + zins;
			}

		} else if (type == BOOLEAN_TRUE) {
			for (int i = 0; i < per; i++) {
				pv = pv + pmt + zins;

				zins = pv * rate;
			}

		}
		return -zins;
	}

	/**
	 * Gets the linear depreciation of a economic good for a period
	 * 
	 * @param cost
	 *            Prime cost of the economic good
	 * @param salvage
	 *            The value at the end of the useful life of the good
	 * @param life
	 *            The useful life of the good
	 * @return The linear depreciation for a period
	 */
	public static double sln(double cost, double salvage, double life) {

		return (cost - salvage) / life;
	}

	/**
	 * Gets the reducing depreciation of one period
	 * 
	 * @param cost
	 *            Prime cost of the economic good
	 * @param salvage
	 *            The value at the end of the useful life of the good
	 * @param life
	 *            The useful life of the good
	 * @param period
	 *            The period of which the depreciation should be calculated
	 * @return The depreciation of the period
	 */
	public static double syd(double cost, double salvage, double life, int period) {
		return ((cost - salvage) * (life - period + 1) * 2) / ((life) * (life + 1));
	}

}
