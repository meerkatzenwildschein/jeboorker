package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.MainMonitor;
import org.rr.jeborker.gui.model.EbookPropertyDBTableModel;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

class SearchAction extends AbstractAction {

	private static SearchAction singletonInstance;
	
	private static final String QUERY_IDENTIFER = SearchAction.class.getName();

	private SearchAction() {
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("play_16.png"));
	}
	
	public static SearchAction getInstance() {
		if(singletonInstance == null) {
			singletonInstance = new SearchAction();
		}
		return singletonInstance;
	}
	
	public void actionPerformed(ActionEvent e) {
		final MainController controller = MainController.getController();
		final String filterText = controller.getFilterText();

		monitorStart(filterText);
		try {
			final List<String> filterValues = ListUtils.split(filterText, ",", -1);

			controller.changeToDatabaseModel().addWhereCondition(new EbookPropertyDBTableModel.EbookPropertyDBTableModelQuery() {

				@Override
				public String getIdentifier() {
					return QUERY_IDENTIFER;
				}
				
				@Override
				public void appendKeyword(List<String> keywords) {
					for (String filterValue : filterValues) {
						if(!StringUtil.toString(filterValue).trim().isEmpty()) {
							controller.setFilterColorEnabled(true);
							List<Field> selectedFilterFields = controller.getSelectedFilterFields();
							if(!selectedFilterFields.isEmpty()) {
								for (int i = 0; i < selectedFilterFields.size(); i++) {
									Field field = selectedFilterFields.get(i);
									keywords.add(field.getName() + ":" + filterValue);
								}
							} else {
								//default filter/search fields
								keywords.add(filterValue);
							}
						} else {
							controller.setFilterColorEnabled(false);
						}
					}
				}
			});

			controller.getEbookTableHandler().refreshTable();
			controller.addFilterFieldSearch(filterText);
		} finally {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					int rowCount = controller.getModel().getRowCount();
					monitorStop(filterText, rowCount);
				}
			});

		}
	}
	
	private void monitorStart(String filterText) {
		MainMonitor progressMonitor = MainController.getController().getProgressMonitor();
		if(progressMonitor!=null) {
			progressMonitor.monitorProgressStart(Bundle.getFormattedString("SearchAction.message", filterText));
		}
	}
	
	private void monitorStop(final String filterText, final int entries) {
		MainMonitor progressMonitor = MainController.getController().getProgressMonitor();
		if(progressMonitor!=null) {
			if(filterText!=null && filterText.length() > 0) {
				progressMonitor.monitorProgressStop(
						Bundle.getFormattedString("SearchAction.message.finish", filterText) +
						" / " +
						Bundle.getFormattedString("SearchAction.message.count", String.valueOf(entries))
				);
			} else {
				progressMonitor.monitorProgressStop(Bundle.getFormattedString("SearchAction.message.count", String.valueOf(entries)));
			}
		}
	}
}
