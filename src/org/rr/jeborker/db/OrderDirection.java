package org.rr.jeborker.db;

public class OrderDirection {

	public static final int DIRECTION_ASC = 0;
	
	public static final int DIRECTION_DESC = 1;
	
	private int direction;
	
	public OrderDirection(int direction) {
		this.direction = direction;
	}
	
	public String toString() {
		if(direction==DIRECTION_ASC) {
			return "asc";
		} else {
			return "desc";
		}
	}
}
