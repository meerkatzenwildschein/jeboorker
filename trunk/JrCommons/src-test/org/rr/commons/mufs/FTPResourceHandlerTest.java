package org.rr.commons.mufs;

import java.text.DateFormat;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class FTPResourceHandlerTest {

	static String target = "";
	static String target2 = "";
	
	static String server = "";
	
	
	public static void main(String[] args) {
//		String target = "ftp://username:password@ftp.whatever.com/file.zip;type=i";
		
		
		Runnable run = new Runnable() {
		
			@Override
			public void run() {
				System.out.println("start new Thread");
					testFTPResourcehandler();
					testFTPResourcehandler2();
			}
		};

		for (int i = 0; i < 14; i++) {
			new Thread(run).start();
		}
		
		System.out.println("end");
		
//		new ParallelForInt(20).loop(new IterationInt() {
//			public void iteration(int y) {
//				System.out.println("performing: " + y);
//				testFTPResourcehandler();
//			}
//		});
			 
	}
	
	public static void testFTPResourcehandler() {
		try {
			IResourceHandler resourceLoader = ResourceHandlerFactory.getResourceHandler(target);
//			IResourceHandler[] listDirectoryResources = resourceLoader.getParentResource().listDirectoryResources();
//			for (int i = 0; i < listDirectoryResources.length; i++) {
//				System.out.println(listDirectoryResources[i].getName());
//				System.err.println(listDirectoryResources[i].exists());
//			}
			
			IResourceHandler[] listFileResources = resourceLoader.listFileResources();
//			for (int i = 0; i < listFileResources.length; i++) {
//				System.out.println("downloading " +  listFileResources[i].getName());
//				byte[] content = listFileResources[i].getContent();
//				FileUtils.writeByteArrayToFile(new File("/tmp/" + listFileResources[i].getName()), content);
//			}

		
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    
	}
	
	public static void testFTPResourcehandler2() {
		try {
			IResourceHandler resourceLoader2 = ResourceHandlerFactory.getResourceHandler(target2);
			
			IResourceHandler[] listFileResources2 = resourceLoader2.listFileResources();
		
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    
	}	
	
	public void testDirect() {
		try
	    {
	      // Connect and logon to FTP Server
	      FTPClient ftp = new FTPClient();
	      
		ftp.connect(server);
	      ftp.login( "gurumaker", "" );
	      System.out.println("Connected to " + 
	           server + ".");
	      System.out.print(ftp.getReplyString());

	      // List the files in the directory
	      ftp.changeWorkingDirectory( "/" );
	      FTPFile[] files = ftp.listFiles();
	      System.out.println( "Number of files in dir: " + files.length );
	      DateFormat df = DateFormat.getDateInstance( DateFormat.SHORT );
	      for( int i=0; i<files.length; i++ )
	      {
	    	  System.out.println(files[i].getName());
//	        Date fileDate = files[ i ].getTimestamp().getTime();
//	        if( fileDate.compareTo( start.getTime() ) >= 0 &&
//	          fileDate.compareTo( end.getTime() ) <= 0 )
//	        {
//	          // Download a file from the FTP Server
//	          System.out.print( df.format( files[ i ].getTimestamp().getTime() ) );
//	          System.out.println( "\t" + files[ i ].getName() );
//	          File file = new File( destinationFolder + 
//	               File.separator + files[ i ].getName() );
//	          FileOutputStream fos = new FileOutputStream( file ); 
//	          ftp.retrieveFile( files[ i ].getName(), fos );
//	          fos.close();
//	          file.setLastModified( fileDate.getTime() );
//	        }
	      }

	      // Logout from the FTP Server and disconnect
	      ftp.logout();
	      ftp.disconnect();

	    }
	    catch( Exception e )
	    {
	      e.printStackTrace();
	    }
	}
}
