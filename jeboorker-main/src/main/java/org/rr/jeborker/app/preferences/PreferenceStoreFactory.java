package org.rr.jeborker.app.preferences;


/**
 * {@link PreferenceStoreFactory} allows to access the different preference store implementations.
 */
public class PreferenceStoreFactory {
	
	public interface PreferenceKey {
		
		public String getKey();

		public String getDefaultValue();
		
		public int getDefaultType();
		
	}
	
	public static enum PREFERENCE_KEYS implements PreferenceKey {
		DELETE_EBOOK_AFTER_IMPORT {
			@Override
			public String getKey() {
				return "deleteEbookAfterImport";
			}
			
			@Override
			public String getDefaultValue() {
				return "false";
			}

			@Override
			public int getDefaultType() {
				return DB_STORE;
			}
		},
		TREE_AUTO_SCROLLING_ENABLED {
			@Override
			public String getKey() {
				return "TreeAutoScrollingEnabled";
			}
			
			@Override
			public String getDefaultValue() {
				return "false";
			}
			
			@Override
			public int getDefaultType() {
				return DB_STORE;
			}
		},
		MAIN_TABLE_AUTO_SAVE_METADATA_ENABLED {
			@Override
			public String getKey() {
				return "MainTableAutoSaveMetadataEnabled";
			}
			
			@Override
			public String getDefaultValue() {
				return "false";
			}

			@Override
			public int getDefaultType() {
				return DB_STORE;
			}
		},
		BASIC_FILE_TYPES {
			@Override
			public String getKey() {
				return "BasicFileTypes";
			}
			
			@Override
			public String getDefaultValue() {
				return "doc,docx,rtf,fb2,tr2,tr3,djvu,pdb,xeb,ceb,azw,kf8,lit,mobi,prc,opf,txt,pdb,ps,pdg,tebr,rb,xml";
			}

			@Override
			public int getDefaultType() {
				return DB_STORE;
			}
		},
		JEBOORKER_DB_VERSION_KEY {
			@Override
			public String getKey() {
				return "jb.dbversion";
			}
			
			@Override
			public String getDefaultValue() {
				return null;
			}

			@Override
			public int getDefaultType() {
				return DB_STORE;
			}
		},
		LOOK_AND_FEEL {
			
			private final String INIT_DEFAULT_VALUE = javax.swing.plaf.metal.MetalLookAndFeel.class.getName();
			
			@Override
			public String getKey() {
				return "LookAndFeel";
			}
			
			@Override
			public String getDefaultValue() {
				return INIT_DEFAULT_VALUE;
			}
			
			@Override
			public int getDefaultType() {
				return SYSTEM_STORE;
			}
		}
	}

	public static final int DB_STORE = 0;
	
	public static final int SYSTEM_STORE = 1;
	
	public static final int PROPERTIES_STORE = 2;
	
	private static final DBPreferenceStore DB_STORE_INSTANCE = new DBPreferenceStore();
	
	private static final SystemPreferenceStore SYSTEM_STORE_INSTANCE = new SystemPreferenceStore();
	
	private static final PropertiesPreferenceStore PROPERTIES_STORE_INSTANCE = new PropertiesPreferenceStore();
	
	/**
	 * Get the store of the given <code>type</code>
	 * @param type The key where the store should be returned for. For example
	 *   {@link #DB_STORE} or {@link #SYSTEM_STORE}
	 * @return The desired {@link APreferenceStore} instance.
	 * @throws IllegalArgumentException
	 */
	public static APreferenceStore getPreferenceStore(final int type) {
		switch(type) {
			case DB_STORE:
				return DB_STORE_INSTANCE;
			case SYSTEM_STORE:
				return SYSTEM_STORE_INSTANCE;
			case PROPERTIES_STORE:
				return PROPERTIES_STORE_INSTANCE;
			default:
				throw new IllegalArgumentException("Unkown type " + type);
		}
	}
	
	/**
	 * Get the default store for the given {@link PREFERENCE_KEYS}.
	 * @param key The key where the store should be returned for.
	 * @return The desired {@link APreferenceStore} instance.
	 */
	public static APreferenceStore getPreferenceStore(final PREFERENCE_KEYS key) {
		final int defaultType = key.getDefaultType();
		return getPreferenceStore(defaultType);
	}

}
