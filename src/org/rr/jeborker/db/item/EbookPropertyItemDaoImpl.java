package org.rr.jeborker.db.item;

import java.sql.SQLException;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class EbookPropertyItemDaoImpl extends BaseDaoImpl<EbookPropertyItem, Integer> implements EbookPropertyItemDao {

	public EbookPropertyItemDaoImpl(ConnectionSource connectionSource) throws SQLException {
		super(connectionSource, EbookPropertyItem.class);
	}
}
