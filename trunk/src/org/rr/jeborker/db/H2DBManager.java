package org.rr.jeborker.db;

import static org.rr.jeborker.app.preferences.PreferenceStoreFactory.PREFERENCE_KEYS.DATABASE_VERSION_KEY;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang.StringEscapeUtils;
import org.h2.jdbc.JdbcResultSet;
import org.h2.result.LocalResult;
import org.rr.commons.collection.ICloseableList;
import org.rr.commons.collection.IteratorList;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.ReflectionFailureException;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.jeborker.Jeboorker;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.jdbc.JdbcDatabaseResults;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.stmt.RawRowMapperImpl;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableInfo;
import com.j256.ormlite.table.TableUtils;

class H2DBManager extends DefaultDBManager {

	protected JdbcPooledConnectionSource initDatabase() {
		PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.SYSTEM_STORE);
		String configPath = APreferenceStore.getConfigDirectory();
		try {
			Class.forName("org.h2.Driver");

			JdbcPooledConnectionSource connection = new JdbcPooledConnectionSource("jdbc:h2:" + configPath + "h2db;MULTI_THREADED=TRUE;TRACE_LEVEL_FILE=0");
			connection.setUsername("sa");
			connection.setPassword("");
			setConnectionPool(connection);

			APreferenceStore dbPreferenceStore = PreferenceStoreFactory.getPreferenceStore(DATABASE_VERSION_KEY);
			String jbDbVersion = dbPreferenceStore.getEntryAsString(DATABASE_VERSION_KEY);
			if (jbDbVersion == null) {
				prepareFullTextIndices();
				for (Class<?> entity : KNOWN_CLASSES) {
					TableUtils.createTableIfNotExists(connection, entity);
					initFullTextIndices(entity);
				}
				reCreateFullTextIndices();
				dbPreferenceStore.addEntryAsString(DATABASE_VERSION_KEY, Jeboorker.VERSION);
			}
			return connection;
		} catch (Exception e) {
			LoggerFactory.log(Level.SEVERE, this, "could not init database in " + configPath, e);
		}
		return null;
	}

	private void reCreateFullTextIndices() throws SQLException {
		JdbcPooledConnectionSource connectionPool = getConnectionPool();
		DatabaseConnection connection = null;
		try {
			connection = connectionPool.getReadWriteConnection();
			connection.executeStatement("CALL FT_REINDEX()", DatabaseConnection.DEFAULT_RESULT_FLAGS);
		} finally {
			if (connection != null) {
				connectionPool.releaseConnection(connection);
			}
		}
	}

	private void prepareFullTextIndices() throws SQLException {
		JdbcPooledConnectionSource connectionPool = getConnectionPool();
		DatabaseConnection connection = null;
		try {
			connection = connectionPool.getReadWriteConnection();
			connection.executeStatement("CREATE ALIAS IF NOT EXISTS FT_INIT FOR \"org.h2.fulltext.FullText.init\"", DatabaseConnection.DEFAULT_RESULT_FLAGS);
			connection.executeStatement("CALL FT_INIT()", DatabaseConnection.DEFAULT_RESULT_FLAGS);
		} finally {
			if (connection != null) {
				connectionPool.releaseConnection(connection);
			}
		}
	}

	private void initFullTextIndices(Class<?> entity) throws SQLException {
		JdbcPooledConnectionSource connectionPool = getConnectionPool();
		DatabaseConnection connection = null;
		try {
			connection = connectionPool.getReadWriteConnection();
			connection.executeStatement("CALL FT_CREATE_INDEX('PUBLIC', '" + entity.getSimpleName().toUpperCase() + "', NULL)",
					DatabaseConnection.DEFAULT_RESULT_FLAGS);
		} finally {
			if (connection != null) {
				connectionPool.releaseConnection(connection);
			}
		}
	}

	public <T> ICloseableList<T> queryFullTextSearch(Class<T> cls, Where<T, T> where, List<String> keywords, List<Field> orderFields,
			OrderDirection orderDirection) {
		try {
			// select * from ebookpropertyitem A, FT_SEARCH_DATA('liga', 0, 0) B where A.FILE = B.KEYS
			// select * from ebookpropertyitem A, (select * from FT_SEARCH_DATA('liga', 0, 0) union select * from FT_SEARCH_DATA('jones', 0, 0)) B where A.FILE
			// = B.KEYS

			StringBuilder sql = new StringBuilder();
			String tableName = cls.getSimpleName().toUpperCase();
			sql.append("SELECT A.* FROM ").append(tableName).append(" A ");

			boolean searchTableAppended = appendFulltextQueryTable(keywords, tableName, sql);
			if (searchTableAppended) {
				sql.append(" WHERE A.FILE = B.KEYS AND ");
				boolean queryConditionAppended = appendFulltextQueryCondition(keywords, tableName, sql);
				if(queryConditionAppended) {
					sql.append(" AND ");
				}
			} else {
				sql.append(" WHERE ");
			}
			sql.append(where.getStatement()).append(' ');
			appendOrderFields(orderFields, sql);
			sql.append(orderDirection.toString());
System.out.println(sql);
			Dao<T, T> createDao = DaoManager.createDao(getConnectionPool(), cls);
			GenericRawResults<T> queryRaw = createDao.queryRaw(sql.toString(), new RawRowMapperImpl<T, T>(new TableInfo<T, T>(getConnectionPool(),
					(BaseDaoImpl<T, T>) createDao, cls)), new String[0]);

			Iterator<T> iterator = queryRaw.closeableIterator();
			int rowCount = getRowCount(iterator);

			return new IteratorList<T>(iterator, rowCount);
		} catch (Exception e) {
			LoggerFactory.log(Level.SEVERE, this, "Failed to execute query", e);
			return new IteratorList<T>(new ArrayList<T>(0).iterator(), 0);
		}
	}

	private <T> int getRowCount(Iterator<T> iterator) throws ReflectionFailureException {
		JdbcDatabaseResults results = (JdbcDatabaseResults) ReflectionUtils.getFieldValue(iterator, "results", false);
		JdbcResultSet resultSet = (JdbcResultSet) ReflectionUtils.getFieldValue(results, "resultSet", false);
		LocalResult localResult = (LocalResult) ReflectionUtils.getFieldValue(resultSet, "result", false);
		return localResult.getRowCount();
	}

	private void appendOrderFields(List<Field> orderFields, StringBuilder sql) {
		if (!orderFields.isEmpty()) {
			sql.append("ORDER BY ");
			for (Field orderField : orderFields) {
				sql.append("A.").append(orderField.getName()).append(",");
			}
			sql.setLength(sql.length() - 1);
		}
		sql.append(' ');
	}

	private boolean appendFulltextQueryTable(List<String> keywords, String tableName, StringBuilder sql) {
		if (!keywords.isEmpty()) {
			sql.append(", (");
			for (int i = 0; i < keywords.size(); i++) {
				if (i > 0) {
					sql.append(" union ");
				}
				String keyword = keywords.get(i);
				if (keyword.contains(":")) {
					keyword = keyword.substring(keyword.indexOf(':') + 1);
				}
				keyword = StringEscapeUtils.escapeSql(keyword);
				sql.append("select * from FT_SEARCH_DATA('").append(keyword).append("', 0, 0) B where B.TABLE='").append(tableName).append("'");
			}
			sql.append(") B");
			return true;
		}
		return false;
	}

	private boolean appendFulltextQueryCondition(List<String> keywords, String tableName, StringBuilder sql) {
		if (!keywords.isEmpty()) {
			StringBuilder localSQL = new StringBuilder();
			boolean append = false;
			String orString = " OR ";
			localSQL.append(" (");
			for (String keyword : keywords) {
				if (keyword.contains(":")) {
					String searchColumn = keyword.substring(0, keyword.indexOf(':')).toUpperCase();
					keyword = keyword.substring(keyword.indexOf(':') + 1).toUpperCase();
					keyword = StringEscapeUtils.escapeSql(keyword);
					
					localSQL.append("upper(").append(searchColumn).append(") like '%").append(keyword).append("%'").append(orString);
					append = true;
				}
			}
			if(localSQL.length() > orString.length() && localSQL.substring(localSQL.length() - orString.length(), localSQL.length()).equals(orString)) {
				localSQL.setLength(localSQL.length() - orString.length());				
			}

			localSQL.append(") ");
			
			if(append) {
				sql.append(localSQL);
			}
			return append;
		}
		return false;
	}

}
