package org.rr.jeborker.db;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.rr.commons.collection.ICloseableList;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.db.item.PreferenceItem;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

/**
 * The {@link DefaultDBManager} provides methods for handle database connections and it's content.
 *
 * @param <T>
 */
public abstract class DefaultDBManager {

	private static DefaultDBManager manager;

	protected static final Class<?>[] KNOWN_CLASSES = new Class<?>[] { EbookPropertyItem.class, PreferenceItem.class };

	private JdbcPooledConnectionSource connection;

	/**
	 * Gets a shared {@link ConfigManager} instance.
	 *
	 * @return The desired {@link ConfigManager} instance.
	 */
	public synchronized static DefaultDBManager getInstance() {
		if (manager == null) {
			manager = new H2DBManager();
			JdbcPooledConnectionSource initDatabase = manager.initDatabase();
			manager.connection = initDatabase;
		}
		return manager;
	}

	protected DefaultDBManager() {
	}

	/**
	 * Initializes the database system to be used by this {@link DefaultDBManager} instance.
	 */
	protected abstract JdbcPooledConnectionSource initDatabase();

	/**
	 * @return the connection pool used by this {@link DefaultDBManager} instance.
	 */
	protected JdbcPooledConnectionSource getConnectionPool() {
		return connection;
	}

	protected void setConnectionPool(JdbcPooledConnectionSource connection) {
		this.connection = connection;
	}

	/**
	 * Closes and shutdown all database connections previously opened.
	 */
	public synchronized void shutdown() {
		try {
			connection.close();
		} catch (Exception e) {
			LoggerFactory.log(Level.SEVERE, this, "shutdown database has failed", e);
		}
	}

	/**
	 * Get a {@link QueryBuilder} instance for the given entity class.
	 * @return a new {@link QueryBuilder} instance for the given class. Never returns <code>null</code>
	 * @throws RuntimeException if an SQL error occurs.
	 */
	public synchronized <T> QueryBuilder<T, T> getQueryBuilder(Class<T> cls) {
		Dao<T, T> createDao;
		try {
			createDao = DaoManager.createDao(connection, cls);
			QueryBuilder<T, T> queryBuilder = createDao.queryBuilder();
			return queryBuilder;
		} catch (SQLException e) {
			throw new RuntimeException("Failed to create dao for " + cls, e);
		}
	}

	public synchronized IDBObject storeObject(final IDBObject item) {
		try {
			Dao<IDBObject, ?> createDao = (Dao<IDBObject, ?>) DaoManager.createDao(connection, item.getClass());
			createDao.createOrUpdate(item);
		} catch (Exception e) {
			LoggerFactory.log(Level.SEVERE, this, "Failed to store object " + item, e);
		}
		return item;
	}

	/**
	 * Gets simply all items from the database which matches to the given class type.
	 *
	 * @param cls The class type of the pojos to be fetched
	 * @return A Iterable which provides the data. Never returns <code>null</code>.
	 */
	public synchronized <T> Collection<T> getItems(Class<T> cls) {
		try {
			Dao<IDBObject, T> createDao = (Dao<IDBObject, T>) DaoManager.createDao(connection, cls);
			List<T> queryForAll = (List<T>) createDao.queryForAll();
			return queryForAll;
		} catch (Exception e) {
			LoggerFactory.log(Level.SEVERE, this, "Failed to query objects " + cls, e);
			return Collections.emptyList();
		}
	}

	/**
	 * Gets simply all items from the database which matches to the given class type.
	 *
	 * @param cls The class type of the pojos to be fetched
	 * @return A Iterable which provides the data. Never returns <code>null</code>.
	 */
	public synchronized <T> List<T> getItems(final Class<T> cls, Where<T, T> where, final List<Field> orderFields,
			final OrderDirection orderDirection) {
		try {
			Dao<T, T> createDao = DaoManager.createDao(connection, cls);
			QueryBuilder<T, T> queryBuilder = createDao.queryBuilder();
			if(where != null && !DBUtils.isEmpty(where)) {
				queryBuilder.setWhere(where);
			}

			for(Field orderField : orderFields) {
				queryBuilder.orderBy(orderField.getName(), orderDirection.isAscending());
			}

//System.out.println(DBUtils.isEmpty(where) ? "" : where.getStatement());
			List<T> queryRaw = createDao.query(queryBuilder.prepare());
//System.out.println(queryRaw.size() + " items fetched");

			return queryRaw;
		} catch (Exception e) {
			LoggerFactory.logWarning(this, "Reading database entries has failed", e);
			return Collections.emptyList();
		}
	}

	public abstract <T> ICloseableList<T> queryFullTextSearch(Class<T> cls, Where<T, T> where, List<String> keywords, List<Field> orderFields,
			OrderDirection orderDirection);

	/**
	 * Updates the given item.
	 *
	 * @param item
	 *            {@link IDBObject} instance to be updated.
	 */
	public synchronized IDBObject updateObject(final IDBObject item) {
		// store the bytes before deleting
		final HashMap<Field, byte[]> data = new HashMap<Field, byte[]>();

		// restore binary data to IDBObject
		for (Map.Entry<Field, byte[]> entry : data.entrySet()) {
			Field field = entry.getKey();
			byte[] value = entry.getValue();
			try {
				field.set(item, value);
			} catch (Exception e) {
				LoggerFactory.getLogger(this).log(Level.WARNING, "could not restore binary data for " + item, e);
			}
		}

		// store the item and
		return this.storeObject(item);
	}

	/**
	 * Simple getObject method which searches for an entry with the given class type allowing to specify one field with a value as condition.
	 *
	 * @param <T>
	 *            The class type to be searched
	 * @param class1
	 *            The entry class type.
	 * @param field
	 *            The name of the field for the condition
	 * @param value
	 *            The condition value.
	 * @return A list with all results.
	 */
	public synchronized <T> List<T> getObject(Class<T> class1, final String field, final String value) {
		try {
			Dao<T, T> createDao = DaoManager.createDao(connection, class1);
			QueryBuilder<T, T> queryBuilder = createDao.queryBuilder();
			queryBuilder.where().eq(field, StringUtil.escapeSql(value));
			List<T> query = createDao.query(queryBuilder.prepare());
			return query;
		} catch(Exception e) {
			LoggerFactory.getLogger(this).log(Level.WARNING, "getObject for " + class1 + " and field=" + field + " and value=" + value + " failed.", e);
			return Collections.emptyList();
		}
	}

	public synchronized boolean deleteObject(IDBObject item) {
		return deleteObject(item, true);
	}

	/**
	 * Deletes the given item and it's binary entries from the database.
	 *
	 * @param item
	 *            The item to be deleted.
	 * @return
	 */
	public synchronized boolean deleteObject(IDBObject item, boolean deleteCover) {
		try {
			Dao<IDBObject, ?> createDao = (Dao<IDBObject, ?>) DaoManager.createDao(connection, item.getClass());
			createDao.delete(item);

			if (item instanceof EbookPropertyItem) {
				EbookPropertyItemUtils.deleteCoverThumbnail(((EbookPropertyItem) item).getResourceHandler());
			}
		} catch (Exception e) {
			LoggerFactory.getLogger(this).log(Level.WARNING, "failed to delete " + item, e);
			return false;
		}
		return true;
	}

	/**
	 * Gets an instance for the desired {@link IDBObject} class.
	 *
	 * @param item
	 *            The class for the new {@link IDBObject}.
	 * @return The desired object instance.
	 */
	public <T> T newInstance(Class<T> item) {
		return (T) ReflectionUtils.getObjectInstance(item, null);
	}

	/**
	 * Rereads the given item from the database.
	 *
	 * @return The new item or <code>null</code> if the item is no longer present in the database.
	 */
	public IDBObject reload(IDBObject item) {
		if (item != null) {
			try {
				Dao<IDBObject, ?> createDao = (Dao<IDBObject, ?>) DaoManager.createDao(connection, item.getClass());
				createDao.refresh(item);
			} catch(Exception e) {
				LoggerFactory.getLogger(this).log(Level.WARNING, "failed to load " + item, e);
			}
		}
		return item;
	}
}
