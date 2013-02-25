package org.rr.common.swing.tree;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.tree.TreeSelectionModel;

public class JRTree extends JTree {

	public JRTree() {
		super();
		
		
		this.addMouseListener(new MouseAdapter() {
			//fixes that the renderer did not fille the tree horizonally and
			//on clicks behind the renderer the selection did not change.
			
			@Override
			public void mouseClicked(MouseEvent e) {
				Point point = e.getPoint();
				int row = JRTree.this.getRowForLocation(0, point.y);
				for(int i = 0; row < 0 && i < JRTree.this.getWidth(); i += 10) {
					row = JRTree.this.getRowForLocation(i, point.y);
				}
				if(getSelectionModel().getSelectionMode() == TreeSelectionModel.SINGLE_TREE_SELECTION
						|| !(e.isAltDown() || e.isControlDown() || e.isShiftDown())) {
					int[] selectedRows = JRTree.this.getSelectionRows();
					if(selectedRows != null && selectedRows.length > 0 && row != selectedRows[0]) {
						JRTree.this.setSelectionRow(row);
					}
					
				}
			}
		});
	}

}
