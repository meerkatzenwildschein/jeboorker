package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.commons.utils.UtilConstants;
import org.rr.jeborker.db.QueryCondition;
import org.rr.jeborker.gui.FilterPanelController;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.MainMonitor;

public class SearchAction extends AbstractAction {

	private static final long serialVersionUID = -2514716618739877972L;
	
	private static final String QUERY_IDENTIFER = SearchAction.class.getName();

	SearchAction() {
		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("play_16.gif")));
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
			final List<String> filterValues = ListUtils.split(filterText, " ", -1, UtilConstants.COMPARE_BINARY);
			final QueryCondition rootCondition = controller.getTableModel().getQueryCondition();
			rootCondition.removeConditionByIdentifier(QUERY_IDENTIFER); //remove possibly existing search conditions
			
			final QueryCondition rootFilterCondition = new QueryCondition(null,null,null, QUERY_IDENTIFER);
			rootCondition.addAndChild(rootFilterCondition);
			for (String filterValue : filterValues) {
				if(StringUtils.toString(filterValue).length() > 0) {
					rootFilterCondition.addOrChild(new QueryCondition("author", "%" + filterValue + "%", "like", QUERY_IDENTIFER));
					rootFilterCondition.addOrChild(new QueryCondition("title", "%" + filterValue + "%", "like", QUERY_IDENTIFER));
					rootFilterCondition.addOrChild(new QueryCondition("file", "%" + filterValue + "%", "like", QUERY_IDENTIFER));
				}
			}
			controller.refreshTable(true);
			filterPanelController.addFilterFieldSearch(filterText);
		} finally {
			monitorStop(filterText);
		}
	}

	private void monitorStart(String filterText) {
		MainMonitor progressMonitor = MainController.getController().getProgressMonitor();
		if(progressMonitor!=null) {
			progressMonitor.monitorProgressStart(Bundle.getFormattedString("SearchAction.message", filterText));
		}
	}
	
	private void monitorStop(String filterText) {
		MainMonitor progressMonitor = MainController.getController().getProgressMonitor();
		if(progressMonitor!=null) {
			if(filterText!=null && filterText.length() > 0) {
				progressMonitor.monitorProgressStop(Bundle.getFormattedString("SearchAction.message.finish", filterText));
			} else {
				progressMonitor.monitorProgressStop("");
			}
		}
	}
}
