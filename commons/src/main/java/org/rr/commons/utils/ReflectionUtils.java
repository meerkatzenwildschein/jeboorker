package org.rr.commons.utils;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.rr.commons.collection.LRUCacheMap;
import org.rr.commons.log.LoggerFactory;

public class ReflectionUtils implements Serializable {
	
	private static final long serialVersionUID = 5079109720850871791L;

	public static int VISIBILITY_VISIBLE_ACCESSIBLE_ONLY = 0;
	
	public static int VISIBILITY_VISIBLE_ALL = 1;

	public static int OS_WINDOWS = 0;
	
	public static int OS_LINUX = 1;
	
	public static int OS_MAC = 2;
	
	public static int OS_UNKNOWN = 99;
	
	/**
	 * stores the java version
	 */
	private static int javaVersion = -1;
	
	/**
	 * stores the os type
	 */
	private static int os = -1; 
	
	private static final int DEFAULT_MAX_CACHE_CAPACITY = 30; 
	
	private static final LRUCacheMap<String, Class<?>> classNameToClassCache = new LRUCacheMap<String, Class<?>>(DEFAULT_MAX_CACHE_CAPACITY);
	
	private static final LRUCacheMap<String, Method> methodNameToMethodCache = new LRUCacheMap<String, Method>(DEFAULT_MAX_CACHE_CAPACITY);
	
	private static final LRUCacheMap<String, Field> fieldNameToFieldCache = new LRUCacheMap<String, Field>(DEFAULT_MAX_CACHE_CAPACITY);
	
	private static final LRUCacheMap<Class<?>, List<Field>> fieldClassCache = new LRUCacheMap<Class<?>, List<Field>>(DEFAULT_MAX_CACHE_CAPACITY);
	
	/**
	 * Gets the <code>Field</code> value from the field with the name specified with the
	 * <code>fieldName</code> parameter from the Object instance specified with the <code>object</code> parameter.
	 * <br><br>
	 * The Field which value should be fetched can also be declared as private, packaged or protected
	 * and also from superclasses.
	 * 
	 * @param object The object containing the desired Field.
	 * @param clazz The <code>Class</code> to be fetched using <code>object.getClass()</code> from the <code>object</code> parameter.
	 * @param fieldName The name of the field which value should be fetched from the Object specified with the <code>object</code> parameter.
	 * @return The value of the <code>Field</code>.
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchFieldException
	 */
	private static Field getField(final Object object, final Class<?> clazz, final String fieldName) throws ReflectionFailureException {
		final Class<?> superclass = clazz.getSuperclass();
		Field field = null;
		
		try {
			field = clazz.getDeclaredField(fieldName);
		} catch (Exception e) {/*not found, go on*/}

		if (field != null) {
			//field found, return the value
			field.setAccessible(true);
			return field;
		} else if (superclass!=null && !Object.class.equals(  superclass )) {
			//there is no filed found but a super class to be searched. RECURSION
			return getField(object, superclass, fieldName);
		} else {
			//throw an Exception if the field could not be allocated.
			throw new ReflectionFailureException("No such field " + fieldName);
		}
	}
	
    /**
     * Get all fields which are marked with the given annotation class.
     * @return The desired fields.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<Field> getFieldsByAnnotation(final Class annotationClass, final Class<?> itemClass) {
		//get fields to be displayed in the combobox
		final List<Field> fields = ReflectionUtils.getFields(itemClass, ReflectionUtils.VISIBILITY_VISIBLE_ALL);
		final ArrayList<Field> listEntries = new ArrayList<>(fields.size());
		for (Field field : fields) {
			Object dbViewFieldAnnotation = field.getAnnotation(annotationClass);
			if(dbViewFieldAnnotation!=null) {
				listEntries.add(field);
			}
		} 
		
		return listEntries;  	
    } 		
	
	/**
	 * Gets the <code>Field</code> value from the field with the name specified with the
	 * <code>fieldName</code> parameter from the Object instance specified with the <code>object</code> parameter.
	 * <br><br>
	 * The Field which value should be fetched can also be declared as private, packaged or protected
	 * and also from superclasses.
	 * 
	 * @param object The object containing the desired Field.
	 * @param fieldName The name of the field which value should be fetched from the Object specified with the <code>object</code> parameter.
	 * @return The value of the <code>Field</code>.
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchFieldException
	 */
	public static Object getFieldValue(final Object object, final String fieldName, boolean useGetter) throws ReflectionFailureException {
		try {
			final Class<?> c;
			if(object instanceof Class<?>) {
				c = (Class<?>) object;
			} else {
				c = object.getClass();
			}
			
			if(useGetter) {
				String getter = "get" + StringUtil.capitalize(fieldName);
				Method method = getMethod(c, getter, null, VISIBILITY_VISIBLE_ACCESSIBLE_ONLY);
				if(method != null) {
					method.setAccessible(true);
					Object methodResult = method.invoke(object, null);
					return methodResult;
				}
			}
			
			//first try to get the desired field from the cache and return it's value.
			Field field = fieldNameToFieldCache.get(createFieldCacheKey(c, fieldName));
			if(field==null) {
				field = getField(object, c, fieldName);
				fieldNameToFieldCache.put(createFieldCacheKey(c, fieldName), field);
			} 
			
			//second try to get the field from the given class, put the found Field to the cache. 
			Object o = field.get(object);
			return o;	
		} catch(Exception e) {
			throw new ReflectionFailureException(e);
		}
	}
	
	/**
	 * Gets the <code>Field</code> value from the field with the name specified with the
	 * <code>qualifiedFieldName</code> parameter. For example: <code>"System.out"</code> returns
	 * the <code>{@link OutputStream}</code> from the <code>{@link System}</code> class. If only 
	 * a Class file is requested with the <code>qualifiedFieldName</code> string, it will also be returned.
	 * <br><br>
	 * The Field which value should be fetched can also be declared as private, packaged or protected
	 * and also from superclasses.
	 * 
	 * @param qualifiedFieldName the field name to be fetched. The qualified field name must be described with the full package 
	 *   path the class name and the name of the field as the last segment, all separated by a '.' character.
	 * @return The desired object instance or <code>{@link java.lang.Class}</code>.
	 * @throws ReflectionFailureException
	 */
	public static Object getStaticFieldValue(final String qualifiedFieldName) throws ReflectionFailureException {
		String fieldName = StringUtil.substringAfter(qualifiedFieldName, ".", false, UtilConstants.COMPARE_BINARY);
		
		//test if there is a class already identified and cached. 
		//the cache only contains the class if the class is also the result. If a
		//field value is requested, it is not putten into the cache.
		if(classNameToClassCache.containsKey(qualifiedFieldName)) {
			Object cachedClass = classNameToClassCache.get(qualifiedFieldName);
			if(cachedClass==null) {
				//a cached exception ;-)
				throw new ReflectionFailureException(qualifiedFieldName);
			}
			return cachedClass;
		}
		
		String className = StringUtil.substringBefore(qualifiedFieldName, ".", false, UtilConstants.COMPARE_BINARY);
		if(className.length()==0) {
			className = qualifiedFieldName;
		}
		Class<?> clazz = null;
		try {
			clazz = Class.forName(className);
		} catch (Exception e) {
			try { 
				clazz = Class.forName("java.lang." + className);
			} catch (Exception e1) {
				try {
					clazz = Class.forName(qualifiedFieldName);
				} catch (ClassNotFoundException e2) {
					//the non existing class should also not searched again!
					classNameToClassCache.put(qualifiedFieldName, null);
					throw new ReflectionFailureException(e2);
				}
				classNameToClassCache.put(qualifiedFieldName, clazz);
				return clazz;
			}
		}
		
		if(fieldName.length()==0) {
			classNameToClassCache.put(qualifiedFieldName, clazz);
			return clazz;
		}
		Object fieldValue = getFieldValue(clazz, fieldName, false);
		return fieldValue;
	}

	/**
	 * Puts the <code>value</code> to the field <code>fieldName</code> at the given <code>object</code> instance.  
	 * <br><br>
	 * This method is not applicable for setting static fields on a class. Only object instanced can be feed into the
	 * <code>object</code> parameter.
	 * 
	 * @param object The object instance which contains the field to be set.
	 * @param fieldName The name of the field within the object instance to be set.
	 * @param value The value to be set to the field specified with the <code>fieldName</code> parameter.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 */
	public static void setFieldValue(final Object object, final String fieldName, final Object value) throws ReflectionFailureException  {
		setFieldValue(object, object.getClass(), fieldName, value);
	}
	
	public static void setFieldValue(final Object object, Class<?> clazz, final String fieldName, final Object value) throws ReflectionFailureException  {
		Field field = null;
		Class<?> superclass = clazz.getSuperclass();
		
		if(object instanceof Class<?> && clazz.equals(java.lang.Class.class) && !object.equals(java.lang.Class.class)) {
			clazz = (Class<?>) object;
		}
		
		try {
			field = clazz.getDeclaredField(fieldName);
			
		} catch (Exception e) {/*not found, go on*/}

		
		if (field != null) {
			//field found, set the value
			field.setAccessible(true);
			try {
				field.set(object, value);
			} catch (IllegalAccessException e) {
				throw new ReflectionFailureException(e);
			}
			return;
		} else if (superclass!=null && !Object.class.equals(  superclass )) {
			//there is no filed found but a super class to be searched.
			setFieldValue(object, superclass, fieldName, value);
		} else {
			//throw an Exception if the field could not be allocated.
			throw new ReflectionFailureException();
		}
	}
	
	/**
	 * Executed the specified method in the specified class. Arguments will be converted to numeric, boolean or string
	 * types.
	 * @param object The <code>Object</code> where the method specified with the <code>methodName</code> parameter should be invoked.
	 * @param methodName The method to be executed.
	 * @param args The arguments for the method to be executed.
	 * @return A <code>ExpressionNode</code> matching to the result of the called method.
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchMethodException 
	 */
	public static Object invokeMethod(final Object object, final String methodName, final Object ... args) throws ReflectionFailureException {
		if (object instanceof Class<?>) {
			return invokeMethod(null, (Class<?>)object, methodName, args);
		} else {
			return invokeMethod(object, object.getClass(), methodName, args);
		}
	}
	
	/**
	 * Tries to create an instance for the given class.
	 * 
	 * @param className The class name where a new Object instance should be created for.
	 * @param initargs The constructor arguments 
	 */
	public static Object getObjectInstance(final String className, Object[] initargs) {
		Class<?> classForName = getClassForName(className);
		return getObjectInstance(classForName, initargs);
	}
	
	/**
	 * Tries to create an instance for the given class.
	 * 
	 * @param clazz The class where a new Object instance should be created for.
	 * @param initargs The constructor arguments 
	 * @return
	 */
	public static Object getObjectInstance(final Class<?> clazz, Object[] initargs) {
		if (clazz == null) {
			return null;
		}
		
		Object result = null;
		if (initargs==null || initargs.length==0) {
			try {
				result = clazz.newInstance();
				return result;
			} catch (Exception e) {}
			
			initargs = new Object[0];
		}
		
		List<Constructor<?>> constructors = ListUtils.union(Arrays.asList(clazz.getConstructors()), Arrays.asList(clazz.getDeclaredConstructors()));
		constructors = ListUtils.distinct(constructors, UtilConstants.COMPARE_BINARY);
		
		for (Constructor<?> constructor : constructors) {
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			if((parameterTypes.length==0 && initargs==null) || parameterTypes.length == initargs.length) {
				Object[] args = createArguments(parameterTypes, initargs, false);
	
				if(args!=null) {
					try {
						constructor.setAccessible(true);
						return constructor.newInstance(args);
					} catch (Exception e) {
						continue;
					}
				} else if(initargs==null && parameterTypes.length==0) {
					try {
						constructor.setAccessible(true);
						return constructor.newInstance(initargs);
					} catch (Exception e) {
						continue;
					}
				}
			}
		}
		
		//last try .. create a new instance with the possible hidden default constructor
		try {
			Constructor<?> constuctor = clazz.getDeclaredConstructor(new Class[0]);
			constuctor.setAccessible(true);
			return constuctor.newInstance(new Object[]{});
		} catch (Exception e) {}
		
		return null;
	}
	
	public static boolean containsMethod(final String className, final String name, final Class<?>[] initargs, final int visibility) {
		Class<?> classForName = getClassForName(className);
		return containsMethod(classForName, name, initargs, visibility);
	}
	
	public static boolean containsMethod(final Class<?> clazz, final String name, final Class<?>[] initargs, final int visibility) {
		return getMethod(clazz, name, initargs, visibility) != null;
	}
	
	/**
	 * Gets a Method from the given class and  
	 */
	public static Method getMethod(final Class<?> clazz, final String name, final Class<?>[] initargs, final int visibility) {
		Method result = null;
		try {
			result = clazz.getMethod(name, initargs);
			result.setAccessible(true);
		} catch (Exception e) {
			if (visibility==VISIBILITY_VISIBLE_ACCESSIBLE_ONLY) {
				return null;
			}
			try {
				result = clazz.getDeclaredMethod(name, initargs);
				result.setAccessible(true);
			} catch (Exception e1) {
			}
		}
		
		return result;
	}
	
	/**
	 * Get all these fields which have public getter.
	 * @param clazz The class where the fields should be fetched for.
	 * @param allowedReturnTypes All allowed field types. <code>null</code> for all types.
	 * @return The fields which have public getter.
	 */
	public static Field[] getFieldsWithGetter(final Class<?> clazz, final Class<?>[] allowedTypes) {
		final List<Field> fields = getFields(clazz, VISIBILITY_VISIBLE_ALL);
		final ArrayList<Field> result = new ArrayList<>(fields.size());
		for (Field field : result) {
			boolean isAllowedType = true;
			if(allowedTypes!=null) {
				isAllowedType = false;
				for (int j = 0; j < allowedTypes.length; j++) {
					if(field.getType().getName().equals(allowedTypes[j].getName())) {
						isAllowedType = true;
						break;
					}
				}
			}
			if(isAllowedType) {
				Method method = getMethod(clazz, "get" + StringUtil.capitalize(field.getName()), null, VISIBILITY_VISIBLE_ACCESSIBLE_ONLY);
				if(method!=null) {
					result.add(field);
				} else {
					method = getMethod(clazz, "is" + StringUtil.capitalize(field.getName()), null, VISIBILITY_VISIBLE_ACCESSIBLE_ONLY);
					if(method!=null) {
						result.add(field);
					}
				}
			}
		}
		return result.toArray(new Field[result.size()]);
	}
	
	/**
	 * Get the field from the given class with the given name. 
	 * @param clazz The class wehere the Field instance shoudl be fetched from.
	 * @param name The name of the desired field. 
	 * @return The desired Field instance or <code>null</code> if no such field could be found.
	 */
	public static Field getField(final Class<?> clazz, String name) {
		List<Field> fields = getFields(clazz, VISIBILITY_VISIBLE_ALL);
		for(Field field : fields) {
			if(field.getName().equals(name)) {
				return field;
			}
		}
		return null;
	}
	
	/**
	 * Fetches all Fields from the specified class.
	 * 
	 * @param clazz The class where the fields should be fetched from.
	 * @param visibility Specifies the visibility of the fields to be fetched.
	 * For specifying the visibility, use the constants <B>VISIBILITY_VISIBLE_ACCESSIBLE_ONLY</B>, <B>VISIBILITY_VISIBLE_ALL</B>.
	 * @return The fields to be fetched from the given class.
	 */
	public static List<Field> getFields(final Class<?> clazz, int visibility) {
		if (clazz==null) {
			return new ArrayList<Field>(0);
		}
		
		List<Field> cachedFields = fieldClassCache.get(clazz);
		if(cachedFields != null) {
			return cachedFields;
		}
		
		List<Field> fields = new ArrayList<>(0);
		Class<?> superclass = clazz.getSuperclass();
		
		try {
			if (visibility==VISIBILITY_VISIBLE_ALL) {
				fields = ListUtils.union(fields, Arrays.asList(clazz.getDeclaredFields()));
			} else {
				fields = ListUtils.union(fields, Arrays.asList(clazz.getFields()));
			}
		} catch (Exception e) {/*not found, go on*/}
		
		if (superclass!=null && !Object.class.equals(superclass)) {
			fields = ListUtils.union(fields, getFields(superclass, visibility));
		}
		
		//set all fields accessible
		if (visibility==VISIBILITY_VISIBLE_ALL) {
			for (Field field : fields) {
				field.setAccessible(true);
			}
		}
		
		List<Field> result =  distinct(fields, UtilConstants.COMPARE_BINARY);
		fieldClassCache.put(clazz, result);
		return result;
	}
	
	/**
	 * Fetches all Methods from the specified class.
	 * 
	 * @param clazz The class where the methods should be fetched from.
	 * @param visibility Specifies the visibility of the methods to be fetched.
	 * For specifying the visibility, use the constants <B>VISIBILITY_VISIBLE_ACCESSIBLE_ONLY</B>, <B>VISIBILITY_VISIBLE_ALL</B>.
	 * @return The methods to be fetched from the given class.
	 */
	public static List<Method> getMethods(final Class<?> clazz, int visibility) {
		if (clazz==null) {
			return new ArrayList<Method>(0);
		}
		
		List<Method> methods = new ArrayList<>();
		Class<?> superclass = clazz.getSuperclass();
		
		try {
			if (visibility==VISIBILITY_VISIBLE_ALL) {
				methods = ListUtils.union(methods, Arrays.asList(clazz.getDeclaredMethods()));
			} else {
				methods = ListUtils.union(methods, Arrays.asList(clazz.getMethods()));
			}
		} catch (Exception e) {/*not found, go on*/}
		
		if (superclass!=null && !Object.class.equals(superclass)) {
			methods = ListUtils.union(methods, getMethods(superclass, visibility));
		}
		
		//set all methods accessible
		if (visibility==VISIBILITY_VISIBLE_ALL) {
			for (Method method : methods) {
				method.setAccessible(true);
			}
		}
		
		return distinct(methods, UtilConstants.COMPARE_BINARY);
	}
	
	/**
	 * filters duplicates from a <code>Method</code> or <code>Field</code> array.
	 * 
	 * @param values An Object that is a Method, a Field or provides a <code>getName()</code> method.
	 * @param compare
	 * @return A new array instance without any duplicates.
	 * 
	 * @throws RuntimeInvocationTargetException should not be happens but will be possible if an array of Objects is specified 
	 * which did not provides a <code>getName()</code> method.
	 */
	private static <T>List<T> distinct(final List<T> values, final int compare) {
		List<T> result = new ArrayList<>(values.size());
		
		try {
			ArrayList<String> names = new ArrayList<>(values.size());
			for (Object value : values) {
				//getting the name using a cast is much faster than using the reflection api
				if (value instanceof Method) {
					names.add(((Method)value).getName());
				} else if (value instanceof Field) {
					names.add(((Field)value).getName());
				} else {
					names.add((String) invokeMethod(value, "getName", null));
				}
			}
			
			Iterator<?> valuesIterator = values.iterator();
			for (int i=0; valuesIterator.hasNext(); i++) {
				Object value = valuesIterator.next();
				if (value instanceof Method) {
					if ( ListUtils.indexOf(names, ((Method)value).getName(), compare, UtilConstants.SEARCH_DEFAULT)==i ) {
						result.add((T) value);
					}
				} else if (value instanceof Field) {
					if ( ListUtils.indexOf(names, ((Field)value).getName(), compare, UtilConstants.SEARCH_DEFAULT)==i ) {
						result.add((T) value);
					}
				}else {
					if ( ListUtils.indexOf(names, (String) invokeMethod(value, "getName", null), compare, UtilConstants.SEARCH_DEFAULT)==i ) {
						result.add((T) value);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return result;
	}
	
	/**
	 * Takes the <code>targetParameterTypes</code> and tries to convert the <code>sourceValues</code>
	 * to a matching type. If a method requires an <code>int</code> and a <code>{@link BigDecimal}</code> instance
	 * is given, the <code>{@link BigDecimal}</code> instance will be converted to match to the int.
	 * 
	 * @param targetParameterTypes The parameter class types required.
	 * @param sourceValues The values to be converted into the required <code>targetParameterTypes</code>
	 * @param force <code>true</code> means that the values array will also be returned if it has not been
	 * 	completely converted. <code>false</code> will return <code>null</code> if one value could not be identified and converted. 
	 * @return All required object instances assignable to this ones given with the <code>targetParameterTypes<7code> parameter. 
	 */
	private static Object[] createArguments(Class<?>[] targetParameterTypes, Object[] sourceValues, boolean force) {
		Object[] result = new Object[targetParameterTypes.length];
		int found=0;
		for (int j=0; j < targetParameterTypes.length && j < sourceValues.length; j++) {
			//test for numeric values			
			if ((targetParameterTypes[j].equals(int.class) || targetParameterTypes[j].equals(Integer.class)) && CommonUtils.toNumber(sourceValues[j])!=null) {
				result[j] = Integer.valueOf( CommonUtils.toNumber(sourceValues[j]).intValue() );
				found++;
				continue;
			} else if ((targetParameterTypes[j].equals(double.class) || targetParameterTypes[j].equals(Double.class)) && CommonUtils.toNumber(sourceValues[j])!=null) {
				result[j] = Double.valueOf(CommonUtils.toNumber(sourceValues[j]).doubleValue());
				found++;
				continue;
			} else if ((targetParameterTypes[j].equals(float.class) || targetParameterTypes[j].equals(Float.class)) && CommonUtils.toNumber(sourceValues[j])!=null) {
				result[j] = Float.valueOf(CommonUtils.toNumber(sourceValues[j]).floatValue());
				found++;
				continue;
			} else if ((targetParameterTypes[j].equals(long.class) || targetParameterTypes[j].equals(Long.class)) && CommonUtils.toNumber(sourceValues[j])!=null) {
				result[j] = new Long(CommonUtils.toNumber(sourceValues[j]).longValue());
				found++;
				continue;
			} else if ((targetParameterTypes[j].equals(short.class) || targetParameterTypes[j].equals(Short.class)) && CommonUtils.toNumber(sourceValues[j])!=null) {
				result[j] = new Short(CommonUtils.toNumber(sourceValues[j]).shortValue());
				found++;
				continue;
			} 
			//test for boolean
			else if ((targetParameterTypes[j].equals(boolean.class) || targetParameterTypes[j].equals(Boolean.class)) && sourceValues[j] instanceof Boolean) {
				result[j] = sourceValues[j]; 
				found++;
				continue;
			} 
			//test for Character
			else if (targetParameterTypes[j].equals(char.class) && sourceValues[j]!=null) {
				result[j] = new Character(String.valueOf(sourceValues[j]).charAt(0)); 
				found++;
				continue;
			} 
			
			//test for String
			else if (targetParameterTypes[j].equals(String.class) && sourceValues[j]!=null) {
				result[j] = String.valueOf(sourceValues[j]); 
				found++;
				continue;
			} 
			
			//test for Object[]
			else if (targetParameterTypes[j].equals(Object[].class) && sourceValues[j]!=null) {
				result[j] = sourceValues[j]; 
				found++;
				continue;
			} 
			
			//test for String[]
			else if (targetParameterTypes[j].equals(String[].class) && sourceValues[j]!=null) {
				String[] converted = new String[Array.getLength(sourceValues[j])];
				System.arraycopy(sourceValues[j], 0, converted, 0, converted.length);
				result[j] = converted;
				found++;
				continue;
			} 
			
			
			//test for Object[][]
			else if (targetParameterTypes[j].equals(Object[][].class) && sourceValues[j]!=null) {
				Object[][] cast = new Object[((Object[])sourceValues[j]).length][0];
				for (int k = 0; k < cast.length; k++) {
					cast[k] = (Object[]) ((Object[])sourceValues[j])[k];
				}
				result[j] = cast; 
				found++;
				continue;
			} 
			//null matches to aything else (excepting the primitives)
			else if (sourceValues[j]==null && 
					(targetParameterTypes[j].equals(char.class) || targetParameterTypes[j].equals(int.class) || targetParameterTypes[j].equals(double.class) || targetParameterTypes[j].equals(float.class) || targetParameterTypes[j].equals(long.class) || targetParameterTypes[j].equals(short.class) || targetParameterTypes[j].equals(boolean.class)) == false
					) {
				result[j] = sourceValues[j]; 
				found++;
				continue;
			} 
			//Object (must be the last!)
			else if (targetParameterTypes[j].equals(Object.class)) {
				result[j] = sourceValues[j]; 
				found++;
				continue;
			} else if (targetParameterTypes[j].isInstance(sourceValues[j])) {
				result[j] = sourceValues[j]; 
				found++;
				continue;
			}
		}
		
		//test if all arguments could be identified
		if(found==targetParameterTypes.length) {
			return result;
		} else {
			if(force) {
				return result;
			}
			return null;
		}
	}
	
	/**
	 * Executed the specified method in the specified class. Arguments will be converted to numeric, boolean or string
	 * types.
	 * @param object The <code>Object</code> where the method specified with the <code>methodName</code> parameter should be invoked.
	 * @param methodName The method to be executed.
	 * @param args The arguments for the method to be executed.
	 * @return A <code>ExpressionNode</code> matching to the result of the called method.
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchMethodException 
	 */
	public static Object invokeMethod(final Object object, Class<?> clazz, final String methodName, Object ... args) throws ReflectionFailureException {
		final String methodCacheKey = createMethodCacheKey(object, clazz, methodName, args);
		Method method = methodNameToMethodCache.get(methodCacheKey);
		Object[] argClasses = null;
		boolean methodFromCache = false;
		if(method!=null) {
			methodFromCache = true;
			
			//use the method from the cache
			Class<?>[] parameters = method.getParameterTypes();
			argClasses = createArguments(parameters, args, false);
		} else {
			//search for the right method
			if (args!=null) {
				argClasses = new Object[args.length];	
			} else { 
				argClasses = new Object[0];
				args = new Object[0];
			}
			
			//method name like the class name means thats not a method, it's a constructor
			if(clazz!=null && (clazz.getName().equals(methodName) || clazz.getSimpleName().equals(methodName))) {
				return ReflectionUtils.getObjectInstance(clazz, args);
			}
			
			//Get all matching methods
			List<Method> methods = clazz != null ? ListUtils.union(Arrays.asList(clazz.getDeclaredMethods()), Arrays.asList(clazz.getMethods())) : Collections.<Method>emptyList();
			for (Method m : methods) {
				String targetMethodName = m.getName();
				if (targetMethodName.equalsIgnoreCase(methodName) && m.getParameterTypes().length == args.length ) { //Method name match
					//get the parameter class types
					Class<?>[] parameters = m.getParameterTypes();
					argClasses = createArguments(parameters, args, false);
					
					//all values matching to the method
					if (argClasses!=null) {
						method = m;
						break;
					}
					
					//but store the method with a matching amount of parameters
					method = m;
				}
			}
		}
		
		methodNameToMethodCache.put(methodCacheKey, method);
		
		//if a method could be identified
		if (method!=null) {
			method.setAccessible(true);
			if(argClasses==null) {
				argClasses = createArguments(method.getParameterTypes(), args, true);
			}
			try {
				Object retval = null;
				try {
					try {
						retval = method.invoke(object, argClasses);
					} catch (NullPointerException e) {
						if(object==null) {
							retval = method.invoke(getObjectInstance(clazz, null), argClasses);
						}
					}
				} catch(IllegalArgumentException nse) {
					if(methodFromCache) {
						//(10.11.09) Try it again but without the method from the cache.
						methodNameToMethodCache.remove(methodCacheKey);
						invokeMethod(object, methodName, args);
					}
				}
				return retval;
			} catch (Exception e) {
				throw new ReflectionFailureException(e);
			}
		} 
		
		Class<?> superclass = clazz.getSuperclass();
		if (superclass!=null && !Object.class.equals(superclass)) {
			//search with the super class
			return invokeMethod(object, superclass, methodName, args);
		}
			
		throw new ReflectionFailureException("No such method " + methodName);
	}	
	
	/**
	 * Creates the key for the HashMap where the Methods gets cached.
	 * @return A key for the Method which can be used for the cache HashMap.
	 */
	private static String createMethodCacheKey(final Object object, Class<?> clazz, final String methodName, Object[] args) {
		String methodCacheKey = String.valueOf(methodName) + (clazz!=null ? clazz.getName() : object.getClass().getName());
		if(args!=null) {
			methodCacheKey += args.length;
		}
		return methodCacheKey;
	}
	
	/**
	 * Creates the key for the HashMap where the Fields gets cached.
	 * @return A key for the Field which can be used for the cache HashMap.
	 */
	private static String createFieldCacheKey(final Class<?> clazz, final String fieldName) {
		String key = String.valueOf(fieldName) + (clazz!=null ? clazz.getName() : EMPTY);
		return key;
	}
	
	/**
	 * gets the {@link StackTraceElement}s for the current Thread. This method
	 * is a replacement for <code>Thread.currentThread().getStackTrace()</code> existing
	 * with java 1.5
	 * 
	 * @return The {@link StackTraceElement} for the current Thread.
	 */
	public static StackTraceElement[] getStackTrace() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement[] resultStackTrace = new StackTraceElement[stackTrace.length-1];
		System.arraycopy(stackTrace, 1, resultStackTrace, 0, resultStackTrace.length);
		
		return resultStackTrace;
	}
	
	/**
	 * Determines if the <code>invokingClass</code> and/or <code>invokingMethod</code>
	 * is in the stack of the current thread. Anonym classes are also assumed to the 
	 * given <code>invokingClass</code>.
	 * 
	 * @param invokingClass Class to be searched in the stack (required).
	 * @param invokingMethod Method to be searched (can be null).
	 * @return The location in the stack hierarchy or -1 if the class/method could not be found in the thread stack.
	 */
	public static int isInvokedFrom(final Class<?> invokingClass, final String invokingMethod) {
		StackTraceElement[] elements = getStackTrace();
		
		//if only the method name is specified
		if (invokingClass==null) {
			for (int i = 0; i < elements.length; i++) {
				if (elements[i].getMethodName().equals(invokingMethod)) {
					return i;
				}
			}
			return -1;
		}
		
    	String invokingPath = invokingClass.getName();
    	if (invokingMethod!=null && invokingMethod.length()>0) {
    		invokingPath += "." + invokingMethod;
    	}

    	
    	for (int i = 2; i < elements.length; i++) {
    		try {
    			boolean isAssignable = false;
    			try {
    				isAssignable = isAssignable(invokingClass, Class.forName(elements[i].getClassName(), false, Thread.currentThread().getContextClassLoader()));
    			} catch (ClassNotFoundException e) {
    				//always happens if the class is not accessible from the current ClassLoader. 
    				isAssignable = false;
    			}
    			String className = elements[i].getClassName();
    			if(className.indexOf('$')!=-1) {
    				String numeric = className.substring(className.lastIndexOf('$')+1);
    				Number num = CommonUtils.toNumber(numeric);
    				if(num!=null) {
    					//for example com.odc.eva3.rt.se.form.FormPrefetchingManager$PrefetchingThread$1 
    					//the 1 can be cutted.. it's the anonymous class which can not requested.
    					className = className.substring(0, className.lastIndexOf('$'));
    				}
    			}
    			
				if (className.equals(invokingClass.getName()) || isAssignable) {
					if (invokingMethod==null || (invokingMethod!=null && elements[i].getMethodName().equals(invokingMethod)) ) {
						int result = i-2;
						if(result<0) {
							return 0;
						}
						return result;
					}
				}
			} catch (Exception e) {
				//LoggerUtils.getLogger(ReflectionUtils.class).log(Level.INFO, "isInvokedFrom has failed.", e);
				return -1;
			} 
		}
    	return -1;
	}
	
	/**
	 * Tests if the second class is assignable to the first class.
	 * @param first The first Class to be tested if the second class is assignable to it.
	 * @param second The second Class to be tested if it's assignable to the first Class.
	 * @return <code>true</code> if the second class is assignable to the first class
	 */
	public static boolean isAssignable(Class<?> first, Class<?> second) {
		//collect all super classes from the given second class to the arraylist
		ArrayList<Class<?>> superclasses = new ArrayList<Class<?>>();
		Class<?> superclass = second;
		superclasses.add(superclass);
		while((superclass=superclass.getSuperclass())!=null) {
			superclasses.add(superclass);
		}
		
		//test all classes from the superclasses if it's assignable
		for (int i = 0; i < superclasses.size(); i++) {
			Class<?> c = (Class<?>) superclasses.get(i);
			if(first.getName().equals(c.getName())) {
				return true;
			}
		}
		
		return false;
	}
    
	/**
	 * Copies all public fields and public getter/setter methods from the <code>source</code> Object to the
	 * <code>target</code> Object.  
	 * 
	 * @param <T> The target object type.
	 * @param source The source object where the properties are copied from
	 * @param target The target object where the properties are copied to
	 * @return The <code>target</code> object instance.
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static <T> T  copyProperties(Object source, T target) throws ReflectionFailureException {
		//start cloning public fields
		Field[] fields = source.getClass().getFields();
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].isAccessible()) {
				Object fieldValue = getFieldValue(source, fields[i].getName(), false);
				try {
					fields[i].set(target, fieldValue);
				} catch (Exception e) {}
			}
		}
		
		//all available methods from the target class
		Method[] methods = target.getClass().getMethods();
		
		//the map stores the getter methods for the target class. The names are stored without any "is" or "get" 
		HashMap<String, Method> getterMethodsMap = new HashMap<String, Method>();
		
		//all the setter methods are stored here available in the target class
		ArrayList<Method> setterMethods = new ArrayList<>(methods.length);
		
		
		//assign getter and setter to the array lists
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().startsWith("get") && methods[i].getParameterTypes().length==0) {
				getterMethodsMap.put(methods[i].getName().substring(3), methods[i]);
			} else if (methods[i].getName().startsWith("is") && methods[i].getParameterTypes().length==0) {
				getterMethodsMap.put(methods[i].getName().substring(2), methods[i]);
			} else if (methods[i].getName().startsWith("set") && methods[i].getParameterTypes().length==1) {
				setterMethods.add(methods[i]);
			}
		}
		
		//loop the setter and search for a matching getter for taking over the property.
		
		Object[] parameter = new Object[1];
		for (int i = 0; i < setterMethods.size(); i++) {
			Method setterMethod = (Method) setterMethods.get(i);
			String getterName = setterMethod.getName().substring(3);
			Method getterMethod = getterMethodsMap.get(getterName);
			
			//the getter method is possibly not available at the source object
			//all methods are fetched from the target class!
			try {
				Method m = source.getClass().getMethod(getterMethod.getName(), getterMethod.getParameterTypes());
				
				parameter[0] = m.invoke(source, (Object[]) null);
			} catch (Exception e) {
				continue;
			}
			
			try {
				setterMethod.invoke(target, parameter);
			} catch (Exception e) {
				continue;
			}
		}		

		return null;
	}
	
    /**
     * Attempts to list all the classes in the specified package as determined
     * by the context class loader
     * 
     * @param packagename
     *            the package name to search
     * @return a list of classes that exist within that package
     * @throws ClassNotFoundException 
     * @throws ClassNotFoundException
     *             if something went wrong
     */
    public static List<Class<?>> getClassesForPackage(final String packagename) throws ClassNotFoundException {
        // This will hold a list of directories matching the packagename. There may be more than one if a package is split over multiple jars/paths
        ArrayList<File> directories = new ArrayList<>();
        try {
            ClassLoader cld = Thread.currentThread().getContextClassLoader();
            if (cld == null) {
                throw new ClassNotFoundException("Can't get class loader.");
            }
            String path = packagename.replace('.', '/');
            // Ask for all resources for the path
            Enumeration<URL> resources = cld.getResources(path);
            while (resources.hasMoreElements()) {
                directories.add(new File(URLDecoder.decode( resources.nextElement().getPath() , "UTF-8")));
            }
        } catch (NullPointerException x) {
            throw new ClassNotFoundException(packagename + " does not appear to be a valid package (Null pointer exception)");
        } catch (UnsupportedEncodingException encex) {
            throw new ClassNotFoundException(packagename + " does not appear to be a valid package (Unsupported encoding)");
        } catch (IOException ioex) {
            throw new ClassNotFoundException("IOException was thrown when trying to get all resources for " + packagename);
        }
 
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        // For every directory identified capture all the .class files
        for (int i = 0; i < directories.size(); i++) {
            if (((File)directories.get(i)).exists()) {
                // Get the list of the files contained in the package
                String[] files = ((File)directories.get(i)).list();
                for (int j = 0; j < files.length; j++) {
                    // we are only interested in .class files
                    if (files[j].endsWith(".class")) {
                        // removes the .class extension
                        classes.add(Class.forName(packagename + '.' + files[j].substring(0, files[j].length() - 6)));
                    }
                }
            } else {
                throw new ClassNotFoundException(packagename + " (" + ((File)directories.get(i)).getPath() + ") does not appear to be a valid package");
            }
        }
        return classes;
    }	
    
    /**
	 * gets the package without the classname, empty string if there is no package.
	 * 
	 * @param c
	 *            The class where the package name should be extracted from.
	 * @return The The package part of the given class.
	 */
	public static String getPackageName(Class<?> c) {
		if(c==null) {
			//return an empty string if no class is given.
			return EMPTY;
		}
		
		String fullyQualifiedName = c.getName();

		//cut the [L from the class/package name. This happens always with array classes. 
		if(fullyQualifiedName.startsWith("[L")) {
			fullyQualifiedName = fullyQualifiedName.substring(2);
		}
		
		int lastDot = fullyQualifiedName.lastIndexOf('.');
		if (lastDot == -1) {
			return EMPTY;
		}
		return fullyQualifiedName.substring(0, lastDot);
	}
    

    /**
     * Tries to load the <code>{@link Class}</code> specified with the <code>className</code>
     * parameter. The parameter should contain a qualified class name.
     * 
     * @param className The name of the <code>{@link Class}</code> to be loaded.
     * @return The desired <code>{@link Class}</code> or <code>null</code> if the class could not be loaded.
     * 	Never throws any kind of <code>{@link Exception}</code> 
     */
    public static Class<?> getClassForName(String className) {
    	//hint: there is no reason to add some caching because the
    	//java framework already uses a Class cache. 
    	Class<?> result = null;
    	
    	//try to load the class using Class#forName
    	try {
    		result = Class.forName(className);
    	} catch (Exception e) {}

    	//just try out if the desired class is located in the default package
    	if(result==null && className.indexOf('.')==-1) {
        	try {
        		result = Class.forName("java.lang." + className);
        	} catch (Exception e) {}
    	}
    	return result;
    }


    /**
     * Determines if the given class is a member class (sub class).
     * 
     * @param cls The {@link Class} to be tested.
     * @return <code>true</code> if we have a member class or <code>false</code> otherwise.
     * 	If the given {@link Class} is <code>null</code>, <code>false</code> is returned.
     */
    public static boolean isMemberClass(Class<?> cls) {
    	if(cls == null) {
    		return false;
    	}
    	
    	String clsStr = cls.toString();
    	if(clsStr.indexOf('$')!=-1) {
    		return true;
    	}
    	return false;
    }
    
    /**
     * Tests if the both given classes are the same. It will not
     * be detected if both classes extends a common class or have
     * the same interface.
     * 
     * @param class1 The first classed to be compared to the second class.
     * @param class2 The second class to be compared to the first one.
     * @return <code>true</code> if the classes equals and <code>false</code> otherwise.
     */
    public static boolean equals(Class<?> class1, Class<?> class2) {
    	if(class1==null || class2==null) {
    		return false;
    	}
    	return class1.getName().equals(class2.getName());
    }
    
    /**
     * Determines the type of operating system.
     * @return One of the constants:
     * 	<ul>
     * 		<li>OS_WINDOWS</li>
     * 		<li>OS_LINUX</li>
     * 		<li>OS_MAC</li>
     * 	</ul>
     */
    public static int getOS() {
    	if(os!=-1) {
    		return os;
    	}
    	
    	String osName = System.getProperty("os.name").toLowerCase();
    	if(osName.indexOf("window")!=-1) {
    		return os = OS_WINDOWS;
    	} else if(osName.indexOf("linux")!=-1) {
    		return os = OS_LINUX;
    	} else if(osName.indexOf("mac")!=-1) {
    		return os = OS_LINUX;
    	}
    	
    	try {
    		Class.forName("sun.print.Win32PrintJob"); 
    		return os = OS_WINDOWS;
    	} catch (Exception e) {
    	}
    	
    	return os = OS_UNKNOWN;
    }
    
    /**
     * Tells if the current operating system is linux.
     */
    public static boolean isLinux() {
    	return getOS() == OS_LINUX;
    }
    
	/**
	 * is64Bit()
	 *
	 * Determine if this is a 64 bit environment
	 */
	public static boolean is64bit() {
		String val = System.getProperty("sun.arch.data.model");
		boolean is64bit = false;
		if (val.equals("64")) {
			is64bit = true;
		}
		return is64bit;
	}	

    /**
     * Writes the stack trace of the current Thread to a String.
     * @return The desired stack trace.
     */
    public static String dumpStackToString() {
    	StringOutputStream str = new StringOutputStream();
    	new Exception("Stack trace").printStackTrace(str);
    	return str.toString();
    }
    
	private static class StringOutputStream extends PrintStream {
		public StringOutputStream() {
			super(new OutputStream() {
				public void write(int b) throws IOException {
					//it's  a null stream! 
				}
			});
		}

		StringBuilder buffer = new StringBuilder();
		
		public void write(int b)  {
			buffer.append(b);
		}
		
		public void println(String x) {
			buffer.append(x + "\n");
		}

		public String toString() {
			return buffer.toString();
		}
	}    
    
    /**
     * Determines a two digit version number. The java version 1.5.0_11 simply returns 15, the version 1.4.2 returns 14
     * and so on.  
     * 
     * @return The two digit version number or -1 if no version could be determined. Never throws any kind of <code>{@link Exception}</code>.
     */
    public static int javaVersion() {
		if (javaVersion != -1) {
    		return javaVersion;
    	}
    	try {
	    	String versionProperty = System.getProperty( "java.version" );
	    	String numString = StringUtil.replace(versionProperty, ".", EMPTY).substring(0,2);
	    	javaVersion = CommonUtils.toNumber(numString).intValue();
	    	return javaVersion;
    	} catch (Exception e) {
    		return -1;
    	}
    }
    
    /**
     * Does a <code>Thread.sleep</code> but without having these nasty
     * exception handling with the {@link InterruptedException}. The {@link InterruptedException}
     * gets simple be logged.
     */
    public static void sleepSilent(int time) {
    	try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			LoggerFactory.getLogger().log(Level.WARNING, "Sleep has been interrupted", e);
		}
    }
  

}
