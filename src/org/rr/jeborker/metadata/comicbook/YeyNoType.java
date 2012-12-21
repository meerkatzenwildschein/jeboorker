package org.rr.jeborker.metadata.comicbook;

public class YeyNoType {

	private static final String YES = "Yes";
	
	private static final String NO = "No";
	
	private static final String UNKNOWN = "Unknown";
	
	private String type;
	
	public static YeyNoType TYPE_YES = new YeyNoType(YES);
	
	public static YeyNoType TYPE_NO = new YeyNoType(NO);
	
	public static YeyNoType TYPE_UNKNOWN = new YeyNoType(UNKNOWN);
	
	private YeyNoType(String type) {
		this.type = type;
	}
	
	public String toString() {
		return type;
	}
	
	public static YeyNoType getInstance(String type) {
		if(type != null) {
			type = type.trim();
			if(type.equalsIgnoreCase(TYPE_YES.toString())) {
				return TYPE_YES;
			} else if(type.equalsIgnoreCase(TYPE_NO.toString())) {
				return TYPE_NO;
			}
		}
		return TYPE_UNKNOWN;
	}
}
