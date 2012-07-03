package org.rr.jeborker.db.item;

import java.io.Serializable;

import org.rr.jeborker.db.IDBObject;

public class EbookKeywordItem implements IDBObject, Serializable {
	
	public EbookKeywordItem() {}
	
	public EbookKeywordItem(String keyword) {
		this.setKeyword(keyword);
	}

	@Index(type= "FULLTEXT")
	private String lowercaseKeyword;
	
	private String keyword;

	public String getLowercaseKeyword() {
		return lowercaseKeyword;
	}

	public void setLowercaseKeyword(String lowercaseKeyword) {
		this.lowercaseKeyword = lowercaseKeyword != null ? lowercaseKeyword.trim().toLowerCase() : null;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword != null ? keyword.trim() : null;
		this.setLowercaseKeyword(keyword);
	}
	
}
