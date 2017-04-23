package org.rr.commons.mufs;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileSystemView;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.StringUtils;
import org.rr.commons.collection.TransformValueList;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.ProcessExecutor;
import org.rr.commons.utils.ProcessExecutorHandler;
import org.rr.commons.utils.ReflectionUtils;

/**
 * A class providing some util methods to be used with
 * {@link IResourceHandler} instances.
 */
public class ResourceHandlerUtils {

	public static final int SORT_BY_NAME = 0;

	public static final int SORT_BY_SIZE = 1;

	public static final int SORT_BY_MODIFY_AT = 2;

	public static final int SORT_BY_TYPE = 3;
	
	/**
	 * Created a string which represents the relative path of the given <code>target</code> resource based on the given <code>base</code>
	 * resource.
	 * 
	 * @param base The base which represents the absolute path.
	 * @param target The target which must be a member of the given <code>base</code>.
	 * @return A relative path statement if <code>target</code> is a member of <code>base</code>. Otherwise the string representation of the
	 *         given <code>target</code> will be returned.
	 */
	public static String makeRelative(final IResourceHandler base, final IResourceHandler target) {
		String relative = target.toString().substring(base.toString().length());
		relative = StringUtils.stripStart(relative, "\\/");
		return relative;
	}

	/**
	 * Gets the resource hierarchy starting with the given base {@link IResourceHandler}.
	 *
	 * @param base The base {@link IResourceHandler} where the resource hierarchy should starts from.
	 * @param target The target - containing more entries than this one given with the base.
	 * @return The resource hierarchy.
	 * @see #getResourceHierarchy(IResourceHandler)
	 */
	public static IResourceHandler[] getRealtivePathResourceHierarchy(final IResourceHandler base, final IResourceHandler target) {
		final IResourceHandler[] baseResourceHierarchy = getResourceHierarchy(base);
		final IResourceHandler[] targetResourceHierarchy = getResourceHierarchy(target);

		//test if the base is longer than the target. no relative path could be created than.
		if(baseResourceHierarchy.length >= targetResourceHierarchy.length) {
			return new IResourceHandler[0];
		}

		IResourceHandler[] realtivePathResourceHierarchy = new IResourceHandler[targetResourceHierarchy.length-baseResourceHierarchy.length];

		//loop the target by starting where the base ends.
		for (int i = baseResourceHierarchy.length; i < targetResourceHierarchy.length; i++) {
			realtivePathResourceHierarchy[i-baseResourceHierarchy.length] = targetResourceHierarchy[i];
		}

		return realtivePathResourceHierarchy;
	}

	/**
	 * Gets the hierarchy for the given {@link IResourceHandler} starting with the root. The last element is always the given {@link IResourceHandler}.
	 * @param resourceHandler The {@link IResourceHandler} where the hirarchy should be fetched for.
	 * @return The resource hierarchy.
	 */
	public static IResourceHandler[] getResourceHierarchy(final IResourceHandler resourceHandler) {
		ArrayList<IResourceHandler> hierarchy = new ArrayList<>();
		IResourceHandler parent = resourceHandler;
		while(parent!=null) {
			hierarchy.add(parent);
			parent = parent.getParentResource();
		}

		Collections.reverse(hierarchy);
		return hierarchy.toArray(new IResourceHandler[hierarchy.size()]);
	}

	/**
	 * Sorts the given {@link IResourceHandler} instances. The result of the sort is always a new array instance.
	 * Directories and files gets sorted separately, so the sorted directories are always at the top of the sort result.
	 *
	 * @param resourceHandlers The {@link IResourceHandler} instances to be sorted.
	 * @param sortType The sort type: {@link #SORT_BY_NAME}, {@link #SORT_BY_MODIFY_AT}, {@link #SORT_BY_TYPE}, {@link #SORT_BY_SIZE}
	 * @param ascending Tells if the given {@link IResourceHandler} instances should be sorted ascending or descending.
	 * @return A new, sorted {@link IResourceHandler} array instance but with the same {@link IResourceHandler} instances specified with the <code>resourceHandlers<7code> parameter.
	 */
	public static IResourceHandler[] sortResourceHandlers(final IResourceHandler[] resourceHandlers, final int sortType, final boolean ascending) {
		if(resourceHandlers==null) {
			return null;
		}

		//split into folders and files to be sorted separately.
		final ArrayList<IResourceHandler> directoryResourceHandler = new ArrayList<>();
		final ArrayList<IResourceHandler> fileResourceHandler = new ArrayList<>();

		for (int i = 0; i < resourceHandlers.length; i++) {
			if(resourceHandlers[i].isDirectoryResource()) {
				directoryResourceHandler.add(resourceHandlers[i]);
			} else {
				fileResourceHandler.add(resourceHandlers[i]);
			}
		}

		IResourceHandler[] sortedDirectoryResources = sort(directoryResourceHandler.toArray(new IResourceHandler[directoryResourceHandler.size()]), sortType, ascending);
		IResourceHandler[] sortedFileResources = sort(fileResourceHandler.toArray(new IResourceHandler[fileResourceHandler.size()]), sortType, ascending);

		//the result array
		final IResourceHandler[] result = new IResourceHandler[resourceHandlers.length];
		System.arraycopy(sortedDirectoryResources, 0, result, 0, sortedDirectoryResources.length);
		System.arraycopy(sortedFileResources, 0, result, sortedDirectoryResources.length, sortedFileResources.length);

		return result;
	}

	/**
	 * Creates a {@link Comparator} which can be used for comparing {@link IResourceHandler} instances.
	 * @param compare The compare mode.
	 * @return A new {@link Comparator} instance.
	 */
	public static Comparator<IResourceHandler> getResourceHandlerComparator(final int compare) {
		Comparator<IResourceHandler> comparator = new Comparator<IResourceHandler>() {

			@Override
			public int compare(IResourceHandler o1, IResourceHandler o2) {
				return compareTo(o1, o2, compare);
			}
		};
		return comparator;
	}

	/**
	 * Simple bubble sort which sorts the given {@link IResourceHandler} array.
	 *
	 * @param arr The entries to be sorted.
	 * @return the same as the given {@link IResourceHandler} array instance.
	 */
	static IResourceHandler[] sort(IResourceHandler[] arr, int sortType, final boolean ascending) {
		for (int j = 0; j < arr.length; j++) {
			for (int i = j + 1; i < arr.length; i++) {
				if(ascending && compareTo(arr[i], arr[j], sortType) < 0) {
					IResourceHandler t = arr[j];
					arr[j] = arr[i];
					arr[i] = t;
				} else if(!ascending && compareTo(arr[i], arr[j], sortType) > 0) {
					IResourceHandler t = arr[j];
					arr[j] = arr[i];
					arr[i] = t;
				}
			}
		}

		return arr;
	}

	/**
	 * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive integer as this object is less than, equal to,
	 * or greater than the specified object.
	 *
	 * This is an alphanumeric comperator.
	 *
	 * @param target
	 *            the object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified objec
	 *
	 * @see Comparable#compareTo(Object)
	 */
	static int compareTo(final IResourceHandler source, final IResourceHandler target, final int compare) {
		String s1 = source.getName(); // this.getName();
		String s2 = target.getName();

		switch(compare) {
			case SORT_BY_SIZE:
				s1 = String.valueOf(source.size());
				s2 = String.valueOf(target.size());
				break;
			case SORT_BY_MODIFY_AT:
				if(source.getModifiedAt()!=null) {
					s1 = SimpleDateFormat.getDateTimeInstance().format(source.getModifiedAt());
				} else {
					s1 = EMPTY;
				}
				if(target.getModifiedAt()!=null) {
					s2 = SimpleDateFormat.getDateTimeInstance().format(target.getModifiedAt());
				} else {
					s2 = EMPTY;
				}
				break;
			case SORT_BY_TYPE:
				s1 = String.valueOf(source.getMimeType(true));
				s2 = String.valueOf(target.getMimeType(true));
				break;
		}

		int thisMarker = 0;
		int thatMarker = 0;
		int s1Length = s1.length();
		int s2Length = s2.length();

		while (thisMarker < s1Length && thatMarker < s2Length) {
			String thisChunk = getChunk(s1, s1Length, thisMarker);
			thisMarker += thisChunk.length();

			String thatChunk = getChunk(s2, s2Length, thatMarker);
			thatMarker += thatChunk.length();

			// If both chunks contain numeric characters, sort them numerically
			int result = 0;
			if (isDigit(thisChunk.charAt(0)) && isDigit(thatChunk.charAt(0))) {
				// Simple chunk comparison by length.
				int thisChunkLength = thisChunk.length();
				result = thisChunkLength - thatChunk.length();
				// If equal, the first different number counts
				if (result == 0) {
					for (int i = 0; i < thisChunkLength; i++) {
						result = thisChunk.charAt(i) - thatChunk.charAt(i);
						if (result != 0) {
							return result;
						}
					}
				}
			} else {
				result = thisChunk.compareTo(thatChunk);
			}

			if (result != 0)
				return result;
		}

		return s1Length - s2Length;
	}

	private static final boolean isDigit(char ch) {
		return ch >= 48 && ch <= 57;
	}

	/**
	 * Length of string is passed in for improved efficiency (only need to calculate it once)
	 **/
	private static final String getChunk(String s, int slength, int marker) {
		StringBuilder chunk = new StringBuilder();
		char c = s.charAt(marker);
		chunk.append(c);
		marker++;
		if (isDigit(c)) {
			while (marker < slength) {
				c = s.charAt(marker);
				if (!isDigit(c))
					break;
				chunk.append(c);
				marker++;
			}
		} else {
			while (marker < slength) {
				c = s.charAt(marker);
				if (isDigit(c))
					break;
				chunk.append(c);
				marker++;
			}
		}
		return chunk.toString();
	}

	/**
	 * removes the upper folder path segement from the given String.
	 * @param relativePath The relative path statement. For example:
	 * 'Folder/Pictures/..' to 'Folder/'.
	 * @return The new path statement.
	 * @deprecated no longer needed.
	 */
	static String removeUpperFolderStatement(String relativePath) {
		List<String> parts = Arrays.asList(relativePath.split("\\/"));
		StringBuilder result = new StringBuilder(relativePath.length());
		for (int i = 0; i < parts.size(); i++) {
			if(parts.size()> i+1 && parts.get(i+1).equals("..")) {
				i++;
				continue;
			}

			if(result.length()==0) {
				result.append('/');
			} if(result.charAt(result.length()-1) != '/') {
				result.append('/');
			}
			result.append(parts.get(i));
		}

		return result.toString();
	}

    /**
     * Returns all root partitions on this system. For example, on
     * Windows, this would be the A: through Z: drives.
     */
	public static IResourceHandler[] getFileSystemRoots() {
		//attach all drives
		File[] roots = File.listRoots();
		final ArrayList<IResourceHandler> resultRoots = new ArrayList<>(roots.length+1);
		for (int i = 0; i < roots.length; i++) {
			resultRoots.add(ResourceHandlerFactory.getResourceHandler(roots[i]));
		}

		//attach the virtual drives like the Desktop folder. At Windows, The Desktop folder
		//also allows to access the Network environment.
		FileSystemView fileSystemView = FileSystemView.getFileSystemView();
		roots = fileSystemView.getRoots();
		for (int i = 0; i < roots.length; i++) {
			if(fileSystemView.getSystemDisplayName(roots[i]).startsWith("Desktop")) {
				resultRoots.add(0, ResourceHandlerFactory.getResourceHandler(roots[i]));
			} else if(roots[i].getName().startsWith("Desktop")) {
				resultRoots.add(0, ResourceHandlerFactory.getResourceHandler(roots[i]));
			}
		}

		return resultRoots.toArray(new IResourceHandler[resultRoots.size()]);
	}

	/**
	 * Reads all files starting from the baseFolder. You can do what you want with each file
	 * using the {@link ResourceNameFilter}. For performance reasons, there is no list result
	 * because it's more performant to do whatever else directly during the iteration process
	 * instead of creating a list and after that doing something with the list.
	 *
	 * @param baseFolder Folder to start reading.
	 * @param filter The filter where we have the possibility to do something with the files.
	 */
	public static int readAllDirectoriesFromBasePath(final IResourceHandler baseFolder, final ResourceNameFilter filter) {
		return readAllDirectoriesFromBasePathRecursive(baseFolder, baseFolder, filter);
	}

	private static int readAllDirectoriesFromBasePathRecursive(final IResourceHandler baseFolder, final IResourceHandler topLevelBaseFolder, final ResourceNameFilter filter) {
		int count = 0;
		if(baseFolder == null || baseFolder.isFileResource()) {
			return count;
		}

		//collect all ebook files from the baseFolder
		try {
			count = baseFolder.listDirectoryResources(filter).length;
		} catch (IOException e) {
			LoggerFactory.log(Level.INFO, ResourceHandlerUtils.class, "Failed reading folder.", e);
		}

		//go into depth
		IResourceHandler[] listDirectoryResources;
		try {
			listDirectoryResources = baseFolder.listDirectoryResources();
			for (int i = 0; i < listDirectoryResources.length; i++) {
				count += readAllDirectoriesFromBasePathRecursive(listDirectoryResources[i], topLevelBaseFolder, filter);
			}
		} catch (IOException e) {
			LoggerFactory.log(Level.INFO, ResourceHandlerUtils.class, "Failed reading subfolders.", e);
		}
		return count;
	}

	/**
	 * Reads all files starting from the baseFolder. You can do what you want with each file
	 * using the {@link ResourceNameFilter}. For performance reasons, there is no list result
	 * because it's more performant to do whatever else directly during the iteration process
	 * instead of creating a list and after that doing something with the list.
	 *
	 * @param baseFolder Folder to start reading.
	 * @param filter The filter where we have the possibility to do something with the files.
	 */
	public static int readAllFilesFromBasePath(final IResourceHandler baseFolder, final ResourceNameFilter filter) {
		return readAllFilesFromBasePathRecursive(baseFolder, baseFolder, filter);
	}

	private static int readAllFilesFromBasePathRecursive(final IResourceHandler baseFolder, final IResourceHandler topLevelBaseFolder, final ResourceNameFilter filter) {
		int count = 0;
		if(baseFolder == null || baseFolder.isFileResource()) {
			return count;
		}

		//collect all ebook files from the baseFolder
		try {
			count = baseFolder.listResources(filter).length;
		} catch (IOException e) {
			LoggerFactory.log(Level.INFO, ResourceHandlerUtils.class, "Failed reading folder.", e);
		}

		//go into depth
		IResourceHandler[] listDirectoryResources;
		try {
			listDirectoryResources = baseFolder.listDirectoryResources();
			for (int i = 0; i < listDirectoryResources.length; i++) {
				count += readAllFilesFromBasePathRecursive(listDirectoryResources[i], topLevelBaseFolder, filter);
			}
		} catch (IOException e) {
			LoggerFactory.log(Level.INFO, ResourceHandlerUtils.class, "Failed reading subfolders.", e);
		}
		return count;
	}

    /**
     * Tries to guess what the image type (if any) of a file based on the file's "magic numbers," the first bytes of the file.
     * <p>
     * <b>Note that this operation can only be performed with {@link InputStream}s supporting marks.</b>
     *
     * @param resourceLoader {@link IResourceHandler} containing image data.
     * @return The mime type of the image or <code>null</code> if no format could be identified.
     * @throw {@link IOException}
     */
    public static String guessFormat(final IResourceHandler resourceLoader) throws IOException {
    	final InputStream is = resourceLoader.getContentInputStream();
        if (!is.markSupported()) {
            return null;
        }

        is.mark(8);
        try {
            int i1 = is.read();
            int i2 = is.read();
            int i3 = is.read();
            int i4 = is.read();

            if ((i1 < 0) || (i2 < 0) || (i3 < 0) || (i4 < 0)) {
                throw new IOException("Couldn't read magic numbers to guess format.");
            }

            int b1 = i1 & 0xff;
            int b2 = i2 & 0xff;
            int b3 = i3 & 0xff;
            int b4 = i4 & 0xff;

            if (b1 == 0x47 && b2 == 0x49) {
                return "image/gif";
            } else if (b1 == 0x89 && b2 == 0x50) {
                return "image/png";
            } else if (b1 == 0xff && b2 == 0xd8) {
                return "image/jpeg";
            } else if (b1 == 0xff && b2 == 0xd9) {
                return "image/jpeg";
            } else if (b1 == 0x42 && b2 == 0x4d) {
                return "image/bmp";
            } else if (b1 == 0x4D && b2 == 0x4D) { // Motorola byte order TIFF
                return "image/tiff";
            } else if (b1 == 0x49 && b2 == 0x49) { // Intel byte order TIFF
                return "image/tiff";
            } else if (b1 == 0x38 && b2 == 0x42) {
                return "image/psd";
            } else if (b1 == 0x50 && b2 == 0x31) {
                return "image/pbm";
            } else if (b1 == 0x50 && b2 == 0x34) {
                return "image/pbm";
            } else if (b1 == 0x50 && b2 == 0x32) {
                return "image/pgm";
            } else if (b1 == 0x50 && b2 == 0x35) {
                return "image/pgm";
            } else if (b1 == 0x50 && b2 == 0x33) {
                return "image/pgm";
            } else if (b1 == 0x50 && b2 == 0x36) {
                return "image/pgm";
            } else if (b1 == 0x97 && b2 == 0x4A && b3 == 0x42 && b4 == 0x32) {
                return "image/x-jbig2";
            } else if (b1 == 0x25 && b2 == 0x50 && b3 == 0x44 && b4 == 0x46) {
                return "application/pdf";
            } else if (b1 == 0xD0 && b2 == 0xCF && b3 == 0x11 && b4 == 0xE0) {
                int bytecount = 512 + 4; // the bytes to be read starting with 0.
                is.reset();
                is.mark(bytecount + 1);
                byte[] bytes = new byte[bytecount]; // buffer to read
                int read = is.read(bytes);
                if (read == -1) {
                    return null;
                }

                if ((bytes[512] & 0xff) == 0xec && (bytes[513] & 0xff) == 0xa5 && (bytes[514] & 0xff) == 0xc1 && (bytes[515] & 0xff) == 0x00) {
                    // Word document subheader (MS Office)
                    return "application/msword";
                } else if ((bytes[512] & 0xff) == 0x09 && (bytes[513] & 0xff) == 0x08 && /* (bytes[514]& 0xff) == 0x10 && */(bytes[515] & 0xff) == 0x00) {
                    // Excel document subheader (MS Office)
                    return "application/excel"; // mime can be application/excel, application/x-excel, application/x-msexcel, application/vnd.ms-excel
                }

                // Word, Powerpoint, Excel have the same container signature
                // The sub type can be determined with the bytes starting at 512 (0x200)
                return null;
            }
        } finally {
            try {
                is.reset();
            } catch (Exception e) {}
        }

        // no format detected..
        return null;
    }

	/**
	 * Copy bytes from an <code>InputStream</code> to an
	 * <code>OutputStream</code>.
	 *
	 * @param input the <code>InputStream</code> to read from
	 * @param output the <code>OutputStream</code> to write to
	 * @return the number of bytes copied
	 * @throws IOException In case of an I/O problem
	 */
	static int copy(InputStream input, OutputStream output, int length) throws IOException {
		byte[] buffer = new byte[1024];
		int count = 0;
		int n = 0;
		while(-1 != (n = input.read(buffer)) && n<length) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	/**
	 * Move the given {@link IResourceHandler} into the user trash.
	 * @param resourceHandler {@link IResourceHandler} to be moved to trash.
	 * @return <code>true</code> if moving to trash was successful and <code>false</code> otherwise.
	 * @throws IOException
	 */
	static boolean moveToTrash(final IResourceHandler resourceHandler) throws IOException {
	    //Die zu loeschende Datei in den Ordner ~/.local/share/Trash/files verschieben.
	    //Im Ordner ~/.local/share/Trash/info eine Datei mit dem gleichen Basename, aber Endung .trashinfo anlegen. In dieser Datei steht der Pfad, der angibt, wo die Datei stand, als sie gelöscht wurde. Außerdem ein Zeitstempel der Löschung.
		if(ReflectionUtils.getOS() == ReflectionUtils.OS_LINUX) {
			final File trashFilesFolder = new File(System.getProperty("user.home") + "/.local/share/Trash/files/");
			if(trashFilesFolder.isDirectory()) {
				final File trashInfoFolder = new File(trashFilesFolder.getParent() + "/info");
				if(trashInfoFolder.isDirectory()) {
					String extension = ".trashinfo";
					int extensionNum = 0;
					File trashInfo;

					while( (trashInfo =  new File(trashInfoFolder.getPath() + "/" + resourceHandler.getName() + (extensionNum != 0 ? extensionNum : EMPTY)  + extension)).exists() ) {
						extensionNum ++;
					}

					{ //create trashinfo
						StringBuilder trashInfoContent = new StringBuilder();
						trashInfoContent.append("[Trash]").append(System.getProperty("line.separator"));

						// Path=/home/admin/Videos/video.avi
						trashInfoContent.append("Path=").append(resourceHandler.toString()).append(System.getProperty("line.separator"));

						// DeletionDate=2012-09-10T13:58:13
						trashInfoContent.append("DeletionDate=").append(new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").format(new Date())).append(System.getProperty("line.separator"));

						IResourceHandler resourceLoader = ResourceHandlerFactory.getResourceHandler(trashInfo);
						resourceLoader.writeStringContent(trashInfoContent.toString(), System.getProperty("file.encoding"));
					}

					resourceHandler.moveTo(ResourceHandlerFactory.getResourceHandler(trashFilesFolder.getPath() + "/" + resourceHandler.getName() + (extensionNum != 0 ? extensionNum : EMPTY) ), false);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Evaluates the file extension for the given {@link IResourceHandler}. If the file
	 * did not have a valid extension, the extension is created from the mime type
	 * of the file.
	 */
	public static String getFileExtension(IResourceHandler resourceHandler) {
		if(resourceHandler == null) {
			return null;
		}

		String fileExtension = resourceHandler.getFileExtension();
		if(fileExtension != null && !fileExtension.isEmpty()) {
			return fileExtension;
		}

		String mimeType = resourceHandler.getMimeType(true);
		if(mimeType != null && !mimeType.isEmpty() &&! mimeType.startsWith("application")) {
			String extension = mimeType.substring(mimeType.indexOf('/') + 1);
			if(extension.equals("jpeg")) {
				return ".jpg";
			}
			return "." + extension;
		}

		return null;
	}

	/**
	 * Get the file name of the given {@link IResourceHandler} instance without path and file extension.
	 * @return The desired file name. Never returns <code>null</code> but an empty {@link String} is possible.
	 */
	public static String getFileNameWithoutFileExtension(IResourceHandler resourceHandler) {
		String name = resourceHandler.getName();
		String fileExtension = resourceHandler.getFileExtension();
		if(!fileExtension.isEmpty() && name.endsWith("." + fileExtension)) {
			name = name.substring(0, name.length() - fileExtension.length() - 1);
		}
		return name;
	}

	/**
	 * Get all child folder of the given {@link IResourceHandler} that matches to the given
	 * regular expression.
	 *
	 * @throws IOException
	 */
	public static List<IResourceHandler> getChildFolderByRegExp(IResourceHandler base, String regExp) {
		final Pattern pattern = Pattern.compile(regExp);
		IResourceHandler[] listDirectoryResources;
		try {
			listDirectoryResources = base.listDirectoryResources(new ResourceNameFilter() {

				@Override
				public boolean accept(IResourceHandler loader) {
					String name = loader.getName();
					if(pattern.matcher(name).find()) {
						return true;
					}
					return false;
				}
			});
			return Arrays.asList(listDirectoryResources);
		} catch (IOException e) {
			return Collections.emptyList();
		}
	}

	/**
	 * Get a list over all external drives.
	 * @return All external drives. never returns <code>null</code>.
	 */
	public static List<IResourceHandler> getExternalDriveResources() {
		final ArrayList<IResourceHandler> result = new ArrayList<>();
		if(CommonUtils.isLinux()) {
			try {
				CommandLine cl = CommandLine.parse("/bin/df");
				Future<Long> runProcess = ProcessExecutor.runProcess(cl, new ProcessExecutorHandler() {

					@Override
					public void onStandardOutput(String msg) {
						int mediaIdx = msg.lastIndexOf("/media/");
						if(msg.indexOf("/media/") != -1) {
							String path = msg.substring(mediaIdx);
							File f = new File(path);
							if(f.isDirectory() && f.canWrite()) {
								result.add(ResourceHandlerFactory.getResourceHandler(f));
							}
						}
					}

					@Override
					public void onStandardError(String msg) {
					}
				}, 2000);
				runProcess.get(); //wait
			} catch (Exception e) {
				LoggerFactory.getLogger(ResourceHandlerUtils.class).log(Level.WARNING, "/bin/df has failed.", e);
			}
		} else {
			File[] roots = File.listRoots();
			for(int i = roots.length - 1; i >= 0; i--) {
				File root = roots[i];
				if(root.canWrite()) {
					result.add(ResourceHandlerFactory.getResourceHandler(root));
				}
				if(root.getPath().equalsIgnoreCase("d:\\")) {
					//C is mostly no external drive and A is a floppy which causes timeouts while
					//getting some information from it. No need for this.
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Test all given {@link IResourceHandler} instances if they're files.
	 * @return <code>true</code> if all given {@link IResourceHandler} are files and <code>false</code> otherwise.
	 */
	public static boolean containFilesOnly(List<IResourceHandler> resourceHandlers) {
		for(int i = 0; i < resourceHandlers.size(); i++) {
			IResourceHandler resourceHandler = resourceHandlers.get(i);
			if(resourceHandler != null && !resourceHandler.isFileResource()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Remove those characters which can't be used in file names.
	 * @param fileName The file name to be filtered for invalid characters.
	 * @return The filtered fileName which no longer contains invalid characters.
	 */
	public static String removeInvalidCharacters(String fileName) {
		if(fileName == null) {
			return null;
		}
		fileName = fileName.replaceAll("%", EMPTY);
		fileName = fileName.replaceAll("/", EMPTY);
		fileName = fileName.replaceAll("\\\\", EMPTY);
		fileName = fileName.replaceAll(File.pathSeparator, EMPTY);
		return fileName;
	}
	
	public static List<String> toFileNames(final List<IResourceHandler> selectedTreeItems) {
		return new TransformValueList<IResourceHandler, String>(selectedTreeItems) {

			@Override
			public String transform(IResourceHandler source) {
				return source.getName();
			}
		};
	}
}
