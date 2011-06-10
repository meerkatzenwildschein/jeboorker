package org.japura.task;

import java.util.HashMap;

import org.japura.exception.HandlerExceptionManager;

/**
 * Task to be executed in a background thread.
 * <P>
 * All exception are handled in the <CODE>handleException</CODE> method.
 * <P>
 * The computed result in the <CODE>doInBackground</CODE> method, must be setted
 * with the <CODE>setResult</CODE> method. This result, can be obtained in the
 * <CODE>done</CODE> method, with the <CODE>getResult</CODE> method.
 * <P>
 * The <CODE>done</CODE> method executed in the EDT, and the
 * <CODE>doInBackground</CODE> method in a background thread.
 * <P>
 * After the task is completed, the following methods will be executed:
 * <UL>
 * <LI><CODE>done</CODE>:if the <CODE>doInBackground</CODE> method finish
 * successfully.</LI>
 * <LI><CODE>handleException</CODE>: if the <CODE>doInBackground</CODE>,
 * <CODE>done</CODE> or <CODE>canceled</CODE> method finish with an exception</LI>
 * <LI><CODE>canceled</CODE>: if the task is canceled</LI>
 * </UL>
 * <P>
 * Copyright (C) 2009-2010 Carlos Eduardo Leite de Andrade
 * <P>
 * This library is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <P>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * <P>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <A
 * HREF="www.gnu.org/licenses/">www.gnu.org/licenses/</A>
 * <P>
 * For more information, contact: <A HREF="www.japura.org">www.japura.org</A>
 * <P>
 * 
 * @author Carlos Eduardo Leite de Andrade
 * @param <R>
 */
public abstract class Task<R> {

  private R result;
  private Exception exception;

  /**
   * Computes a result.
   * <P>
   * Need throws an exception if unable to do so, to be handled in a
   * <CODE>handleException</CODE> method.
   * <P>
   * This method is executed in a background thread.
   * 
   * @throws Exception
   * @see #handleException(Exception, Integer)
   */
  public abstract void doInBackground() throws Exception;

  /**
   * Executed on the Event Dispatch Thread if the <CODE>doInBackground</CODE>
   * method finish successfully.
   * <P>
   * The default implementation does nothing.
   */
  public void done() {}

  /**
   * Action performed if the <CODE>doInBackground</CODE>, <CODE>done</CODE> or
   * <CODE>canceled</CODE> method finish with an exception.
   * 
   * @param exception
   *          {@link Exception}
   * @param taskGroupId
   *          {@link Integer}
   */
  protected void handleException(Exception exception, Integer taskGroupId) {
	HashMap<String, Object> parameters = new HashMap<String, Object>();
	parameters.put(HandlerExceptionParameters.TASK_GROUP_ID.getParameterName(),
		taskGroupId);

	HandlerExceptionManager.handle(exception, parameters);
  }

  /**
   * Action performed if the task is canceled.
   */
  protected void canceled() {

  }

  /**
   * Set the result
   * 
   * @param result
   */
  protected final void setResult(R result) {
	this.result = result;
  }

  /**
   * Get the result.
   * 
   * @return R
   */
  protected final R getResult() {
	return result;
  }

  /**
   * Get the exception.
   * 
   * @return {@link Exception}
   */
  Exception getException() {
	return exception;
  }

  /**
   * Set the exception
   * 
   * @param exception
   *          {@link Exception}
   */
  void setException(Exception exception) {
	this.exception = exception;
  }

  public static enum HandlerExceptionParameters {
	TASK_GROUP_ID("TASK_GROUP_ID");

	private String parameterName;

	HandlerExceptionParameters(String parameterName) {
	  this.parameterName = parameterName;
	}

	public String getParameterName() {
	  return parameterName;
	}
  }

}
