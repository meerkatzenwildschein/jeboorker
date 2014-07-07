package org.rr.commons.swing.dialogs.chooser;

import java.awt.Component;
import java.io.File;
import java.util.concurrent.Future;
import java.util.logging.Level;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteWatchdog;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.swing.DesktopUtils;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.ProcessExecutor;
import org.rr.commons.utils.ProcessExecutorHandler;

/**
 * {@link IFileChooser} implementation for Linux with zenity
 */
class ZenityFileChooser implements IFileChooser {

	private File selectedFile;

	private File selectedDir;

	private DIALOG_TPYE type = DIALOG_TPYE.OPEN;

	private RETURN_OPTION returnValue;

	private String title;

	@Override
	public void setSelectedFile(File file) {
		this.selectedFile = file;
	}

	@Override
	public File getSelectedFile() {
		return this.selectedFile;
	}

	@Override
	public void setCurrentDirectory(File dir) {
		this.selectedDir = dir;
	}

	@Override
	public File getCurrentDirectory() {
		return this.selectedDir;
	}

	@Override
	public void setDialogType(DIALOG_TPYE type) {
		this.type = type;
	}

	@Override
	public RETURN_OPTION showDialog(Component parent) {
		String commandLineString = DesktopUtils.getZenityBinary().getAbsolutePath() + " --file-selection --confirm-overwrite ";
		CommandLine cl = CommandLine.parse(commandLineString);
		if(type.equals(DIALOG_TPYE.SAVE)) {
			cl.addArgument("--save");
		}

		if(getSelectedFile() != null && getSelectedFile().getName() != null) {
			cl.addArgument("--filename=" + getSelectedFile().getName());
		}

		if(this.title != null) {

			cl.addArgument("--title=" + ProcessExecutor.saveWhitespaces(this.title));
		}

		final StringBuilder result = new StringBuilder();
		try {
			Future<Long> p = ProcessExecutor.runProcess(cl, new ProcessExecutorHandler() {

				@Override
				public void onStandardOutput(String msg) {
					if(result.length() > 0) {
						result.append(File.pathSeparator);
					}
					result.append(msg);
				}

				@Override
				public void onStandardError(String msg) {
				}
			}, ExecuteWatchdog.INFINITE_TIMEOUT);
			p.get(); //wait

			if(result.length() > 0) {
				File file = new File(result.toString());
				this.setSelectedFile(new File(file.getName()));
				this.setCurrentDirectory(file.getParentFile());
				return returnValue = RETURN_OPTION.APPROVE;
			}
		} catch (Exception e) {
			LoggerFactory.getLogger().log(Level.SEVERE, "Failed to open zenity dialog", e);
			return returnValue = RETURN_OPTION.ERROR;
		}
		return returnValue = RETURN_OPTION.CANCEL;
	}

	static boolean isSupported() {
		if(CommonUtils.isLinux()) {
			return new File("/usr/bin/zenity").exists();
		}
		return false;
	}

	@Override
	public RETURN_OPTION getReturnValue() {
		return this.returnValue;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

}
