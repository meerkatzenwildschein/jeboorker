package org.rr.jeborker.metadata.comicbook;

import org.rr.commons.utils.BooleanUtils;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.StringUtil;

public class ComicBookUtils {
	
	static Integer getAsInteger(Object value) {
		if(value instanceof Integer) {
			return (Integer) value;
		} else {
			Number number = CommonUtils.toNumber(value);
			if(number != null) {
				return Integer.valueOf(number.intValue());
			}
		}
		return null;
	}
	
	static Long getAsLong(Object value) {
		if(value instanceof Long) {
			return (Long) value;
		} else {
			Number number = CommonUtils.toNumber(value);
			if(number != null) {
				return Long.valueOf(number.longValue());
			}
		}
		return null;
	}	

	static YeyNoType getAsYesNoType(Object value) {
		if(value instanceof YeyNoType) {
			return (YeyNoType) value;
		} else if(value != null) {
			return YeyNoType.getInstance(StringUtil.toString(value));
		}
		return null;
	}
	
	static ComicPageType getAsComicPageType(Object value) {
		if(value instanceof YeyNoType) {
			return (ComicPageType) value;
		} else if(value != null) {
			return ComicPageType.getInstance(StringUtil.toString(value));
		}
		return null;
	}

	public static Boolean getAsBoolean(Object value) {
		if(value instanceof Boolean) { 
			return (Boolean) value;
		} else {
			return BooleanUtils.toBoolean(value);
		}
	}	
}
