package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.swing.dialogs.SimpleInputDialog;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.metadata.IMetadataReader;

class SetMetadataSeriesNameAction extends ASetCommonMetadataAction implements IDoOnlyOnceAction<SimpleInputDialog> {

	private static final long serialVersionUID = 4772310971481868593L;

	private final IResourceHandler resourceHandler;
	
	private SimpleInputDialog inputDialog;

	private int index;

	private int max;
	
	SetMetadataSeriesNameAction(IResourceHandler resourceHandler) {
		this.resourceHandler = resourceHandler;
		putValue(Action.NAME, Bundle.getString("SetMetadataSeriesNameAction.name"));
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		final MainController controller = MainController.getController();
		final SimpleInputDialog inputDialog = this.doOnce();
		
		controller.getProgressMonitor().monitorProgressStart(Bundle.getFormattedString("SetMetadataSeriesNameAction.message", inputDialog.getInput(), resourceHandler.getName()));
		controller.getProgressMonitor().setProgress(index, max);		
		
		setMetaData(resourceHandler, IMetadataReader.METADATA_TYPES.SERIES_NAME);
	}

	/**
	 * Tells the factory if this writer is able to work with the 
	 * {@link IResourceHandler} given with the constructor.
	 * @return <code>true</code> if this action is able to do something or <code>false</code> otherwise.
	 */
	public static boolean canHandle(final IResourceHandler resourceHandler) {
		return true;
	}

	@Override
	public synchronized SimpleInputDialog doOnce() {
		if(inputDialog == null) {
			inputDialog = new SimpleInputDialog(MainController.getController().getMainWindow(), "Authors name", true);
			inputDialog.setDialogMode(SimpleInputDialog.OK_CANCEL_DIALOG);
			inputDialog.setQuestionTest(Bundle.getString("SetMetadataAuthorAction.input.text"));
			inputDialog.setSize(300,160);
			inputDialog.setVisible(true);
		}
		
		return this.inputDialog;
	}

	@Override
	public void setDoOnceResult(SimpleInputDialog result) {
		this.inputDialog = result;
	}

	@Override
	public void prepareFor(int index, int max) {
		this.index = index;
		this.max = max;
	}

}
