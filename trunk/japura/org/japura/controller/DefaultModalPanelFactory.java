package org.japura.controller;

import javax.swing.JComponent;

import org.japura.controller.modals.ErrorPanel;
import org.japura.controller.modals.InformationPanel;
import org.japura.controller.modals.QuestionPanel;
import org.japura.controller.modals.WarningPanel;
import org.japura.gui.I18nStringKeys;
import org.japura.i18n.I18nManager;

/**
 * Build default standards panels for modal.
 * <P>
 * Copyright (C) 2009, 2010 Carlos Eduardo Leite de Andrade
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
 * @see ModalPanelFactory
 */
public class DefaultModalPanelFactory implements ModalPanelFactory{

  @Override
  public JComponent buildConfirmationPanel(Controller<?> controller,
										   String title, String text,
										   ModalAction confirmAction,
										   ModalAction cancelAction) {
	QuestionPanel modal =
		new QuestionPanel(title, text, confirmAction, cancelAction);
	String confirm = I18nManager.getString(I18nStringKeys.CONFIRM.getKey());
	modal.setConfirmButtonText(confirm);
	String cancel = I18nManager.getString(I18nStringKeys.CANCEL.getKey());
	modal.setCancelButtonText(cancel);
	return modal;

  }

  @Override
  public JComponent buildErrorPanel(Controller<?> controller, String title,
									String text) {
	return new ErrorPanel(title, text);
  }

  @Override
  public JComponent buildInformationPanel(Controller<?> controller,
										  String title, String text) {
	return new InformationPanel(title, text);
  }

  @Override
  public JComponent buildQuestionPanel(Controller<?> controller, String title,
									   String text, ModalAction yesAction,
									   ModalAction noAction) {
	QuestionPanel modal = new QuestionPanel(title, text, yesAction, noAction);
	String yes = I18nManager.getString(I18nStringKeys.YES.getKey());
	modal.setConfirmButtonText(yes);
	String no = I18nManager.getString(I18nStringKeys.NO.getKey());
	modal.setCancelButtonText(no);
	return modal;
  }

  @Override
  public JComponent buildWarningPanel(Controller<?> controller, String title,
									  String text) {
	return new WarningPanel(title, text);
  }

}
