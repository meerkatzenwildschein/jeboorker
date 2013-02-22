package org.japura.controller.modals;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.japura.controller.ModalAction;
import org.japura.gui.Images;

/**
 * Question panel for modal.
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
public class QuestionPanel extends AbstractPanel{

  private JPanel buttonsPanel;
  private JButton confirmButton;
  private JButton cancelButton;

  private ModalAction confirmAction;
  private ModalAction cancelAction;

  public QuestionPanel(String title, String text, ModalAction confirmAction) {
	this(title, text, confirmAction, null);
  }

  public QuestionPanel(String title, String text, ModalAction confirmAction,
	  ModalAction cancelAction) {
	super(title, text);
	this.confirmAction = confirmAction;
	this.cancelAction = cancelAction;
  }

  @Override
  protected JPanel getButtonsPanel() {
	if (buttonsPanel == null) {
	  buttonsPanel = new JPanel();
	  buttonsPanel.setName("buttonsPanel");
	  buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 0));
	  buttonsPanel.setOpaque(false);
	  buttonsPanel.add(getConfirmButton());
	  buttonsPanel.add(getCancelButton());
	}
	return buttonsPanel;
  }

  /**
   * Obtém o botão "Confirmar"/"Sim"
   * 
   * @return {@link JButton}
   */
  private JButton getConfirmButton() {
	if (confirmButton == null) {
	  confirmButton = new JButton();
	  confirmButton.setName("confirmButton");
	  confirmButton.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
		  closeModal();
		  if (confirmAction != null) {
			SwingUtilities.invokeLater(new Runnable() {
			  @Override
			  public void run() {
				confirmAction.executeAction(QuestionPanel.this);
			  }
			});
		  }
		}
	  });
	}
	return confirmButton;
  }

  /**
   * Obtém o botão "Cancelar"/"Não"
   * 
   * @return {@link JButton}
   */
  private JButton getCancelButton() {
	if (cancelButton == null) {
	  cancelButton = new JButton();
	  cancelButton.setName("cancelButton");
	  cancelButton.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
		  closeModal();
		  if (cancelAction != null) {
			SwingUtilities.invokeLater(new Runnable() {
			  @Override
			  public void run() {
				cancelAction.executeAction(QuestionPanel.this);
			  }
			});
		  }
		}
	  });
	}
	return cancelButton;
  }

  @Override
  protected Icon getIcon() {
	return new ImageIcon(Images.QUESTION);
  }

  public void setCancelButtonText(String text) {
	getCancelButton().setText(text);
  }

  public void setConfirmButtonText(String text) {
	getConfirmButton().setText(text);
  }

  @Override
  protected JComponent getContent() {
	return null;
  }

}
