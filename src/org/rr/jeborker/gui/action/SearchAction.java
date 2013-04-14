package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.db.QueryCondition;
import org.rr.jeborker.gui.FilterPanelController;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.MainMonitor;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

class SearchAction extends AbstractAction {

	private static final long serialVersionUID = -2514716618739877972L;
	
	private static final String QUERY_IDENTIFER = SearchAction.class.getName();

	SearchAction() {
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("play_16.png"));
	}
	
	public void actionPerformed(ActionEvent e) {
		final MainController controller = MainController.getController();
		FilterPanelController filterPanelController = controller.getFilterPanelController();
		if(filterPanelController==null) {
			if(e.getSource() instanceof FilterPanelController) {
				filterPanelController = (FilterPanelController) e.getSource();
			}
		}
		final String filterText = filterPanelController.getFilterText();

		monitorStart(filterText);
		try {
			final List<String> filterValues = ListUtils.split(filterText, ",", -1);

			final QueryCondition rootCondition = controller.getTableModel().getQueryCondition();
			rootCondition.removeConditionByIdentifier(QUERY_IDENTIFER); //remove possibly existing search conditions
			
			final QueryCondition rootFilterCondition = new QueryCondition(null,null,null, QUERY_IDENTIFER);
			rootCondition.addAndChild(rootFilterCondition);
			for (String filterValue : filterValues) {
				if(!StringUtils.toString(filterValue).trim().isEmpty()) {
					List<Field> selectedFilterFields = filterPanelController.getSelectedFilterFields();
					if(!selectedFilterFields.isEmpty()) {
						for (Field field : selectedFilterFields) {
							//rootFilterCondition.addOrChild(new QueryCondition(field.getName(), "%" + filterValue + "%", "like", QUERY_IDENTIFER));
							rootFilterCondition.addOrChild(new QueryCondition(field.getName(), filterValue, "CONTAINSTEXT", QUERY_IDENTIFER));
						}
					} else {
						//default filter/search fields
						rootFilterCondition.addOrChild(new QueryCondition("author", filterValue, "CONTAINSTEXT", QUERY_IDENTIFER));
						rootFilterCondition.addOrChild(new QueryCondition("title", filterValue, "CONTAINSTEXT", QUERY_IDENTIFER));
						rootFilterCondition.addOrChild(new QueryCondition("file", filterValue, "CONTAINSTEXT", QUERY_IDENTIFER));
					}
				}
			}
			controller.refreshTable();
			filterPanelController.addFilterFieldSearch(filterText);
		} finally {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					int rowCount = controller.getTableModel().getRowCount();
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
