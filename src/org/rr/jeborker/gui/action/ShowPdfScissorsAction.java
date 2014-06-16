package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.swing.SwingUtils;
import org.rr.commons.swing.components.JRComboBox;
import org.rr.commons.swing.dialogs.JPreferenceDialog;
import org.rr.commons.swing.dialogs.JPreferenceDialog.PreferenceEntry;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.MainController;

import bd.amazed.pdfscissors.model.PageGroup;
import bd.amazed.pdfscissors.view.PdfScissorsMainFrame;


class ShowPdfScissorsAction extends AbstractAction {

	private static final long serialVersionUID = -6464113132395695332L;

	ShowPdfScissorsAction(String text) {
		String name = Bundle.getString("ShowPdfScissorsAction.name");
		if(text == null) {
			putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
		} else {
			putValue(Action.NAME, text);
		}
//		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("download_16.png"));
//		putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("download_22.png"));
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE); //No threading
		putValue(MNEMONIC_KEY, SwingUtils.getMnemonicKeyCode(name));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final MainController controller = MainController.getController();
		final List<EbookPropertyItem> selectedEbookPropertyItems = controller.getSelectedEbookPropertyItems();

		if(!selectedEbookPropertyItems.isEmpty()) {
			PdfScissorsMainFrame scissors = new PdfScissorsMainFrame(false);
			scissors.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			scissors.setExtendedState(JFrame.NORMAL);

			EbookPropertyItem ebookPropertyItem = selectedEbookPropertyItems.get(0);
			IResourceHandler resourceHandler = ebookPropertyItem.getResourceHandler();
			IResourceHandler uniqueResourceHandler = ResourceHandlerFactory.getUniqueResourceHandler(resourceHandler, null);
			scissors.setSaveFile(uniqueResourceHandler.toFile());

			openOptionDialog(controller, scissors, resourceHandler);
		}
	}

	private void openOptionDialog(final MainController controller, final PdfScissorsMainFrame scissors, final IResourceHandler resourceHandler) {
		JPreferenceDialog preference = new JPreferenceDialog(controller.getMainWindow());
		JRComboBox<String> pageGroupTypeCombobox = new JRComboBox<String>();
		pageGroupTypeCombobox.setModel(new DefaultComboBoxModel<String>(new String[] {
				Bundle.getString("ShowPdfScissorsAction.pageGroupType.all"),
				Bundle.getString("ShowPdfScissorsAction.pageGroupType.oddAndEven"),
				Bundle.getString("ShowPdfScissorsAction.pageGroupType.separate"),
		}));
		pageGroupTypeCombobox.setWide(true);
		preference.addPreferenceEntry(new PreferenceEntry("0", Bundle.getString("ShowPdfScissorsAction.cropType.label"), pageGroupTypeCombobox));

		final JCheckBox shouldCreateStackViewCheckBox = new JCheckBox();
		shouldCreateStackViewCheckBox.setSelected(true);
		preference.addPreferenceEntry(new PreferenceEntry("1", Bundle.getString("ShowPdfScissorsAction.stackedView"), shouldCreateStackViewCheckBox));


		preference.setVisible(true);
		if(preference.getActionResult() == JPreferenceDialog.ACTION_RESULT_OK) {
			final int pageGroupType;
			String selectedItem = (String) pageGroupTypeCombobox.getSelectedItem();
			if(selectedItem.equals(Bundle.getString("ShowPdfScissorsAction.pageGroupType.all"))) {
				pageGroupType = PageGroup.GROUP_TYPE_ALL;
			} else if(selectedItem.equals(Bundle.getString("ShowPdfScissorsAction.pageGroupType.oddAndEven"))) {
				pageGroupType = PageGroup.GROUP_TYPE_ODD_EVEN;
			} else if(selectedItem.equals(Bundle.getString("ShowPdfScissorsAction.pageGroupType.separate"))) {
				pageGroupType = PageGroup.GROUP_TYPE_INDIVIDUAL;
			} else {
				pageGroupType = PageGroup.GROUP_TYPE_ALL;
			}
			scissors.setVisible(true);
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					scissors.openFile(resourceHandler, pageGroupType, shouldCreateStackViewCheckBox.isSelected());
				}
			});

		}
	}

}
