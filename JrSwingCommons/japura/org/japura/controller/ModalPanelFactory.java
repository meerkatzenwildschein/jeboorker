package org.japura.controller;

import javax.swing.JComponent;

/**
 * Build standards panels for modal.
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
 */
public interface ModalPanelFactory{

  /**
   * Build a error message modal.
   * 
   * @param controller
   *          the controller
   * @param title
   *          title for the modal
   * @param error
   *          the error
   */
  public JComponent buildErrorPanel(Controller<?> controller, String title,
									String error);

  /**
   * Build a warning message modal.
   * 
   * @param controller
   *          the controller
   * @param title
   *          title for the modal
   * @param warning
   *          the warning
   */
  public JComponent buildWarningPanel(Controller<?> controller, String title,
									  String warning);

  /**
   * Build a information message modal.
   * 
   * @param controller
   *          the controller
   * @param title
   *          title for the modal
   * @param info
   *          the information
   */
  public JComponent buildInformationPanel(Controller<?> controller,
										  String title, String info);

  /**
   * Build a question message modal.
   * 
   * @param controller
   *          the controller
   * @param title
   *          title for the modal
   * @param question
   *          the question
   * @param yesAction
   *          action for the confirmation button
   * @param noAction
   *          action for the cancel button
   */
  public JComponent buildQuestionPanel(Controller<?> controller, String title,
									   String question, ModalAction yesAction,
									   ModalAction noAction);

  /**
   * Build a confirmation message modal.
   * 
   * @param controller
   *          the controller
   * @param title
   *          title for the modal
   * @param confirmation
   *          the confirmation
   * @param confirmAction
   *          action for the confirmation button
   * @param cancelAction
   *          action for the cancel button
   */
  public JComponent buildConfirmationPanel(Controller<?> controller,
										   String title, String confirmation,
										   ModalAction confirmAction,
										   ModalAction cancelAction);

}
