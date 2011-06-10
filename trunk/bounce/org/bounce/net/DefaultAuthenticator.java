/*
 * $Id: DefaultAuthenticator.java,v 1.5 2008/01/28 21:28:37 edankert Exp $
 *
 * Copyright (c) 2002 - 2008, Edwin Dankert
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, 
 *	 this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright 
 * 	 notice, this list of conditions and the following disclaimer in the 
 *	 documentation and/or other materials provided with the distribution. 
 * * Neither the name of 'Edwin Dankert' nor the names of its contributors 
 *	 may  be used to endorse or promote products derived from this software 
 *	 without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.bounce.net;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.border.EmptyBorder;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.Box;

import org.bounce.FormLayout;

/**
 * An authenticator which prompts for username and password.
 *
 * @version	$Revision: 1.5 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class DefaultAuthenticator extends Authenticator {
	// these values can be overriden with other values
	protected static Dimension SIZE = new Dimension( 350, 210);
	
	protected String TITLE 			= "Enter Username and Password";
	protected String DESCRIPTION	= "Please provide your username and password.";
	protected String USERNAME 		= "Username:";
	protected String PASSWORD 		= "Password:";
	protected String HOST 			= "Site:";
	protected String REALM 			= "Realm:";
	protected String OK_BUTTON 		= "OK";
	protected String CANCEL_BUTTON 	= "Cancel";
	
	private JDialog dialog = null;

	private boolean okPressed = false;
	
	private JFrame parent = null;

	private JPasswordField passwordField	= null;
	private JTextField usernameField 		= null;
	private JLabel hostField 				= null;
	private JLabel realmField 				= null;
	
	/**
	 * Constructor for the authenticator, setting the parent frame.
	 * 
	 * @param parent the parent frame.
	 */
	public DefaultAuthenticator( JFrame parent) {
		this.parent = parent;
	}
	
	private JDialog getDialog() {
		if ( dialog == null) {
			// create the dialog
			dialog = new JDialog( parent);
			dialog.setTitle( TITLE);
			dialog.setModal( true);
			dialog.setResizable( false);
			
			JPanel panel = new JPanel( new FormLayout( 5, 5));
			panel.setBorder( new EmptyBorder( 10, 10, 10, 10));
			
			JLabel description = new JLabel( DESCRIPTION);
			description.setFont( description.getFont().deriveFont( Font.PLAIN));
			
			panel.add( description, FormLayout.FULL_FILL);

			panel.add( Box.createVerticalStrut( 10), FormLayout.FULL_FILL);

			panel.add( new JLabel( HOST), FormLayout.LEFT);
			hostField = new JLabel();
			hostField.setFont( hostField.getFont().deriveFont( Font.PLAIN));
			panel.add( hostField, FormLayout.RIGHT_FILL);

			panel.add( new JLabel( REALM), FormLayout.LEFT);
			realmField = new JLabel();
			realmField.setFont( realmField.getFont().deriveFont( Font.PLAIN));
			panel.add( realmField, FormLayout.RIGHT_FILL);
			
			panel.add( new JLabel( USERNAME), FormLayout.LEFT);
			usernameField = new JTextField();
			panel.add( usernameField, FormLayout.RIGHT_FILL);

			panel.add( new JLabel( PASSWORD), FormLayout.LEFT);
			passwordField = new JPasswordField();
			panel.add( passwordField, FormLayout.RIGHT_FILL);
			
			JButton cancelButton = new JButton( CANCEL_BUTTON);
			cancelButton.setMnemonic( 'C');
			cancelButton.setFont( cancelButton.getFont().deriveFont( Font.PLAIN));
			cancelButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e) {
					okPressed = false;
					dialog.setVisible( false);
				}
			});

			JButton okButton = new JButton( OK_BUTTON);
			okButton.setMnemonic( 'O');
			okButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e) {
					okPressed = true;
					dialog.setVisible( false);
				}
			});

			dialog.getRootPane().setDefaultButton( okButton);

			JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.CENTER, 0, 0));
			buttonPanel.setBorder( new EmptyBorder( 10, 0, 3, 0));
			buttonPanel.add( okButton);
			buttonPanel.add( cancelButton);
			
			JPanel main = new JPanel( new BorderLayout());
			
			main.add( panel, BorderLayout.CENTER);
			main.add( buttonPanel, BorderLayout.SOUTH);
	
			dialog.addWindowListener( new WindowAdapter() {
				public void windowClosing( WindowEvent e) {
					okPressed = false;
					dialog.setVisible( false);
				}
			});
	
			dialog.setContentPane( main);
			dialog.setDefaultCloseOperation( JDialog.HIDE_ON_CLOSE);
			dialog.setSize( SIZE);
		}
		
		return dialog;
	}

	/**
	 * Return the PasswordAuthentication object.
	 * 
	 * @return the PasswordAuthentication object.
	 */
	protected PasswordAuthentication getPasswordAuthentication() {
		JDialog dialog = getDialog();
		
		passwordField.setText( "");
		usernameField.setText( "");
		hostField.setText( getRequestingHost());
		realmField.setText( getRequestingPrompt());
		
		dialog.setLocationRelativeTo( parent);
		dialog.setVisible( true);
		
		if ( okPressed) {
			return new PasswordAuthentication( usernameField.getText(), passwordField.getPassword());
		}

        return null;
	}
} 

