package org.japura.controller.modals;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.japura.gui.I18nStringKeys;
import org.japura.gui.Images;
import org.japura.i18n.I18nManager;

/**
 * Information panel for modal.
 * <P>
 * Copyright (C) 2009-2010 Carlos Eduardo Leite de Andrade
 * <P>
 * This library is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <P>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * <P>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <A
 * HREF="www.gnu.org/licenses/">www.gnu.org/licenses/</A>
 * <P>
 * For more information, contact: <A HREF="www.japura.org">www.japura.org</A>
 * <P>
 * 
 * @author Carlos Eduardo Leite de Andrade
 */
public class InformationPanel extends AbstractPanel{

  private JPanel buttonsPanel;
  private JButton closeButton;

  public InformationPanel(String title, String text) {
	super(title, text);
  }

  @Override
  protected JPanel getButtonsPanel() {
	if (buttonsPanel == null) {
	  buttonsPanel = new JPanel();
	  buttonsPanel.setName("buttonsPanel");
	  buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 0));
	  buttonsPanel.setOpaque(false);
	  buttonsPanel.add(getCloseButton());
	}
	return buttonsPanel;
  }

  /**
   * Obtém o botão de fechar
   * 
   * @return JButton
   */
  private JButton getCloseButton() {
	if (closeButton == null) {
	  String text = I18nManager.getString(I18nStringKeys.CLOSE.getKey());
	  closeButton = new JButton(text);
	  closeButton.setName("closeButton");
	  closeButton.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
		  closeModal();
		}
	  });
	}
	return closeButton;
  }

  @Override
  protected Icon getIcon() {
	return new ImageIcon(Images.INFORMATION);
  }

  @Override
  protected JComponent getContent() {
	return null;
  }

}
