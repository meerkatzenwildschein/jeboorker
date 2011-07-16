package com.parallel4;

/**
 * (C) Copyright 2008 Markus Junginger.
 * 
 * @author Markus Junginger
 */
/*
 * This file is part of Parallel4.
 * 
 * Parallel4 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * Parallel4 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Parallel4. If not, see <http://www.gnu.org/licenses/>.
 */
public class ParallelException extends RuntimeException {
	private static final long serialVersionUID = 7428436608525063032L;
	private Number failedIteration;

	public ParallelException() {
		super();
	}

	public ParallelException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ParallelException(String arg0) {
		super(arg0);
	}

	public ParallelException(Throwable arg0) {
		super(arg0);
	}

	public Number getFailedIteration() {
		return failedIteration;
	}

	public void setFailedIteration(Number failedIteration) {
		this.failedIteration = failedIteration;
	}
	
	
	

}
