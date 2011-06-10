package org.japura.exception;

import java.util.HashMap;

/**
 * Pool of handler exception.
 * <P>
 * Registers handler for a specific or super class exception.
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
 */
public abstract class HandlerExceptionManager{

  private static HashMap<String, HandlerException> handlers =
	  new HashMap<String, HandlerException>();

  public static void register(Class<? extends Exception> exceptionClass,
							  HandlerException handler) {
	if (exceptionClass != null && handler != null) {
	  handlers.put(exceptionClass.getName(), handler);
	}
  }

  /**
   * Handle an exception.
   * 
   * @param exception
   *          {@link Exception}
   */
  public static void handle(Exception exception) {
	handle(exception, null);
  }

  /**
   * Handle an exception.
   * 
   * @param exception
   *          {@link Exception}
   * @param parameters
   *          HashMap<String,Object>
   */
  public static void handle(Exception exception,
							HashMap<String, Object> parameters) {
	if (handlers.size() == 0) {
	  register(Exception.class, new HandlerException() {
		@Override
		public void handle(Exception exception,
						   HashMap<String, Object> parameters) {
		  exception.printStackTrace();
		}
	  });
	}

	if (exception != null) {
	  Class<?> exceptionClass = exception.getClass();
	  while (exceptionClass != null) {
		if (handlers.containsKey(exceptionClass.getName())) {
		  HandlerException handler = handlers.get(exceptionClass.getName());
		  if (parameters == null) {
			parameters = new HashMap<String, Object>();
		  }
		  handler.handle(exception, parameters);
		  return;
		} else {
		  exceptionClass = exceptionClass.getSuperclass();
		}
	  }
	}
  }

}
