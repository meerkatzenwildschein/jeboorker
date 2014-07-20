package org.rr.commons.swing.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.rr.commons.swing.components.model.DefaultJRCheckBoxComboBoxModel;
import org.rr.commons.swing.components.model.JRCheckBoxComboBoxModel;

import net.miginfocom.swing.MigLayout;

public class JRCheckBoxComboBox<E> extends JRComboBox<E> {

	public static enum CheckState {
		NONE, ALL, MULTIPLE;
	}
	
	private CharSequence textForNone = "";
	private CharSequence textForAll = "***";
	private CharSequence textForMultiple = "...";

	private JRCheckBoxComboBoxModel<E> model;
	
	private int popupHeight = 300;
	
	private int minPopupWidth = -1;

	public JRCheckBoxComboBox() {
		super();
		initialize();
	}

	private void initialize() {
		this.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				JComboBox<E> box = JRCheckBoxComboBox.this;
				Object comp = box.getUI().getAccessibleChild(box, 0);
				if (comp instanceof JPopupMenu) {
					preparePopup((JPopupMenu) comp);
				}
			}

			private void preparePopup(JPopupMenu popup) {
				popup.removeAll();
				popup.setPopupSize(getSize().width, popupHeight);
				popup.setLightWeightPopupEnabled(false);
				popup.setLayout(new BorderLayout());
				
				JPanel panel = new JPanel(new MigLayout());
				panel.setOpaque(false);
				JScrollPane scrollPane = new JRScrollPane(panel);
				
				JRCheckBoxComboBoxModel<E> model = getJRCheckBoxComboBoxModel();
				for (int i = 0; i < model.getSize(); i++) {
					String label = model.getLabel(i);
					boolean checked = model.isChecked(i);
					JCheckBox checkBox = new JCheckBox(label, checked);
					checkBox.setOpaque(false);
					checkBox.setName(String.valueOf(i));
					panel.add(checkBox, "wrap");
					checkBox.addItemListener(new ItemListener() {
						
						@Override
						public void itemStateChanged(ItemEvent e) {
							int index = Integer.valueOf(((JCheckBox)e.getSource()).getName());
							if(e.getStateChange() == ItemEvent.SELECTED) {
								getJRCheckBoxComboBoxModel().setChecked(index, true);
							} else {
								getJRCheckBoxComboBoxModel().setChecked(index, false);
							}
							JRCheckBoxComboBox.this.repaint();
						}
					});
					popup.setPopupSize(Math.max(
							Math.max(popup.getPreferredSize().width, checkBox.getPreferredSize().width + scrollPane.getVerticalScrollBar().getSize().width),
							minPopupWidth), popupHeight);
				}
				
				scrollPane.getVerticalScrollBar().setUnitIncrement(10);
				scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
				popup.add(scrollPane, BorderLayout.CENTER);
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});
		this.setRenderer(new ListCellRenderer<E>() {

			JLabel label = new JLabel();
			
			@Override
			public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
				JRCheckBoxComboBoxModel<E> model = getJRCheckBoxComboBoxModel();
				if(model.getCheckCount() == 0) {
					label.setText(String.valueOf(textForNone));
				} else if(model.getCheckCount() > 1) {
					label.setText(String.valueOf(textForMultiple));
				} else if(model.getCheckCount() == model.getSize()) {
					label.setText(String.valueOf(textForAll));
				} else {
					Set<Integer> checkedIndices = model.getCheckedIndices();
					String labelText = model.getLabel(checkedIndices.iterator().next());
					label.setText(labelText);
				}
				return label;
			}
		});
	}
	
	public JRCheckBoxComboBoxModel<E> getCheckBoxComboBoxModel() {
		if(model ==null) {
			model = new DefaultJRCheckBoxComboBoxModel<E>(new ArrayList<E>(), null);
		}
		return model;
	}

	public void setModel(JRCheckBoxComboBoxModel<E> aModel) {
		throw new UnsupportedOperationException("setModel is not supported. Use setCheckBoxComboBoxModel instead.");
	}
	
	public void setCheckBoxComboBoxModel(JRCheckBoxComboBoxModel<E> aModel) {
		this.model = aModel;
		super.setModel(new DefaultComboBoxModel<E>() {

			@Override
			public int getSize() {
				return model.getSize();
			}

			@Override
			public E getElementAt(int index) {
				return model.getValueAt(index);
			}

			@Override
			public Object getSelectedItem() {
				return null;
			}
		});
	}

	public JRCheckBoxComboBoxModel<E> getJRCheckBoxComboBoxModel() {
		return this.model;
	}

	public void setTextFor(CheckState checkState, CharSequence text) {
		if(checkState == CheckState.ALL) {
			textForAll = text;
		} else if(checkState == CheckState.NONE) {
			textForNone = text;
		} else if(checkState == CheckState.MULTIPLE) {
			textForMultiple = text;
		}
	}

	public int getPopupHeight() {
		return popupHeight;
	}

	public void setPopupHeight(int popupHeight) {
		this.popupHeight = popupHeight;
	}

	public int getMinPopupWidth() {
		return minPopupWidth;
	}

	public void setMinPopupWidth(int minPopupWidth) {
		this.minPopupWidth = minPopupWidth;
	}

}
