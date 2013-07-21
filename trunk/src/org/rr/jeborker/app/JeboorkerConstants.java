package org.rr.jeborker.app;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.metal.OceanTheme;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertGreen;
import com.jgoodies.looks.plastic.theme.DesertRed;
import com.jgoodies.looks.plastic.theme.DesertYellow;
import com.jgoodies.looks.plastic.theme.ExperienceGreen;
import com.jgoodies.looks.plastic.theme.ExperienceRoyale;
import com.jgoodies.looks.plastic.theme.SkyBlue;
import com.jgoodies.looks.plastic.theme.SkyGreen;
import com.jgoodies.looks.plastic.theme.SkyKrupp;
import com.jgoodies.looks.plastic.theme.SkyRed;
import com.jgoodies.looks.plastic.theme.SkyYellow;


public class JeboorkerConstants {
	
	public static interface SupportedMime {
		public String getName();
		public String getMime();
	}
	
	public static enum SUPPORTED_MIMES implements SupportedMime {
		MIME_EPUB {

			@Override
			public String getName() {
				return "epub";
			}

			@Override
			public String getMime() {
				return "application/epub+zip".intern();
			}
			
			@Override
			public String toString() {
				return getMime();
			}
		},
		MIME_PDF {

			@Override
			public String getName() {
				return "pdf";
			}

			@Override
			public String getMime() {
				return "application/pdf".intern();
			}
			
			@Override
			public String toString() {
				return getMime();
			}			
		},
		MIME_CBZ {

			@Override
			public String getName() {
				return "cbz";
			}

			@Override
			public String getMime() {
				return "application/x-cbz".intern();
			}
			
			@Override
			public String toString() {
				return getMime();
			}			
			
		},
		MIME_CBR {

			@Override
			public String getName() {
				return "cbr";
			}

			@Override
			public String getMime() {
				return "application/x-cbr".intern();
			}
			
			@Override
			public String toString() {
				return getMime();
			}			
			
		},
		MIME_HTML {

			@Override
			public String getName() {
				return "html";
			}

			@Override
			public String getMime() {
				return "text/html".intern();
			}
			
			@Override
			public String toString() {
				return getMime();
			}			
			
		}				
	};

	public static final Map<String, String> LOOK_AND_FEELS = new LinkedHashMap<String, String>() {
		{
			put("Plastic3D;SkyBlue", Plastic3DLookAndFeel.class.getName() + ";" + SkyBlue.class.getName());
			put("Plastic3D;SkyGreen", Plastic3DLookAndFeel.class.getName() + ";" + SkyGreen.class.getName());
			put("Plastic3D;SkyKrupp", Plastic3DLookAndFeel.class.getName() + ";" + SkyKrupp.class.getName());
			put("Plastic3D;SkyRed", Plastic3DLookAndFeel.class.getName() + ";" + SkyRed.class.getName());
			put("Plastic3D;SkyYello", Plastic3DLookAndFeel.class.getName() + ";" + SkyYellow.class.getName());
			put("Plastic3D;DesertGreen", Plastic3DLookAndFeel.class.getName() + ";" + DesertGreen.class.getName());
			put("Plastic3D;DesertRed", Plastic3DLookAndFeel.class.getName() + ";" + DesertRed.class.getName());
			put("Plastic3D;DesertYellow", Plastic3DLookAndFeel.class.getName() + ";" + DesertYellow.class.getName());
			put("Plastic3D;Ocean", Plastic3DLookAndFeel.class.getName() + ";" + OceanTheme.class.getName());
			put("Plastic3D;ExperienceGreen", Plastic3DLookAndFeel.class.getName() + ";" + ExperienceGreen.class.getName());
			put("Plastic3D;ExperienceRoyale", Plastic3DLookAndFeel.class.getName() + ";" + ExperienceRoyale.class.getName());
			
			put("PlasticXP;SkyBlue", PlasticXPLookAndFeel.class.getName() + ";" + SkyBlue.class.getName());
			put("PlasticXP;SkyGreen", PlasticXPLookAndFeel.class.getName() + ";" + SkyGreen.class.getName());
			put("PlasticXP;SkyKrupp", PlasticXPLookAndFeel.class.getName() + ";" + SkyKrupp.class.getName());
			put("PlasticXP;SkyRed", PlasticXPLookAndFeel.class.getName() + ";" + SkyRed.class.getName());
			put("PlasticXP;SkyYello", PlasticXPLookAndFeel.class.getName() + ";" + SkyYellow.class.getName());
			put("PlasticXP;DesertGreen", PlasticXPLookAndFeel.class.getName() + ";" + DesertGreen.class.getName());
			put("PlasticXP;DesertRed", PlasticXPLookAndFeel.class.getName() + ";" + DesertRed.class.getName());
			put("PlasticXP;DesertYellow", PlasticXPLookAndFeel.class.getName() + ";" + DesertYellow.class.getName());
			put("PlasticXP;Ocean", PlasticXPLookAndFeel.class.getName() + ";" + OceanTheme.class.getName());
			put("PlasticXP;ExperienceGreen", PlasticXPLookAndFeel.class.getName() + ";" + ExperienceGreen.class.getName());
			put("PlasticXP;ExperienceRoyale", PlasticXPLookAndFeel.class.getName() + ";" + ExperienceRoyale.class.getName());
			
			final LookAndFeelInfo[] installedLookAndFeels = UIManager.getInstalledLookAndFeels();
			for(LookAndFeelInfo laf : installedLookAndFeels) {
				String className = laf.getClassName();
				String name = className.substring(className.lastIndexOf('.') + 1);
				if(name.startsWith("Motif")) {
					continue;
				}
				if(name.contains("LookAndFeel")) {
					name = name.substring(0, name.indexOf("LookAndFeel"));
				}
				put(name, className);
			}
		}
	};	
}
