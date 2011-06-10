/*
 * $Id$
 *
 * Copyright (c) 2002 - 2008, Edwin Dankert
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright 
 *   notice, this list of conditions and the following disclaimer in the 
 *   documentation and/or other materials provided with the distribution. 
 * * Neither the name of 'Edwin Dankert' nor the names of its contributors 
 *   may  be used to endorse or promote products derived from this software 
 *   without specific prior written permission. 
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

package org.bounce.preferences;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.bounce.CardPanel;
import org.bounce.QDialog;

/**
 * Preferences Dialog framework ...
 * 
 * @version $Revision$, $Date$
 * @author Edwin Dankert <edankert@gmail.com>
 */

public abstract class PreferencesDialog extends QDialog implements TreeSelectionListener {
	private static final long serialVersionUID = -7403432397234000652L;

	private JTree tree = null;
	private CardPanel<PreferencesPage> cards = null;
    private JButton cancelButton = null;
    private JButton okButton = null;
	private JScrollPane scrollPane = null;
	private DefaultMutableTreeNode root = null;
	private DefaultTreeModel model = null;
	
	/**
	 * @param parent
	 * @throws java.awt.HeadlessException
	 */
	public PreferencesDialog(Frame parent, String title) {
		super(parent, title, true );
		
		root = new DefaultMutableTreeNode( title);
		
		model = new DefaultTreeModel( root);
		
		tree = new JTree( model);
		tree.setBorder( new EmptyBorder( 2, 0, 2, 0));
		tree.setRootVisible( false);
		tree.setShowsRootHandles( true);
        tree.setCellRenderer(new PreferencesPageCellRenderer());
        tree.setFont( tree.getFont().deriveFont( (float)11));
        tree.addTreeSelectionListener( this);
		
		scrollPane = new JScrollPane(	tree,
										JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
										JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		/**
		 * Work around to make sure the scroll pane shows the vertical 
		 * scrollbar for the first time when resized to a size small enough.
		 * JDK 1.3.0-C 
		 *
		 * Got work around from Bug ID: 4243631 (It should be fixed...)
		 *
		 * ED: Check with JDK1.4
		 */
		ComponentAdapter resizeListener = new ComponentAdapter() {
			public void componentResized( ComponentEvent e) {
				scrollPane.doLayout();
			}
		};

		scrollPane.getViewport().addComponentListener( resizeListener);
		scrollPane.setPreferredSize( new Dimension( 160, 100));

        cards = getCards();

		JPanel main = createCenterPanel();
		main.add(scrollPane, BorderLayout.WEST);
		main.add(cards, BorderLayout.CENTER);
		
        main.add(createSouthPanel(), BorderLayout.SOUTH);
		
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(main, BorderLayout.CENTER);
        setContentPane(contentPane);
	}
    
    protected JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        return panel;
    }
    
    protected JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(getOkButton());
        buttonPanel.add(getCancelButton());
        
        return buttonPanel;
    }
    
    protected JPanel createSouthPanel() {
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(new EmptyBorder( 5, 0, 0, 0));
        southPanel.add(createButtonPanel(), BorderLayout.EAST);
        
        return southPanel;
    }

    protected CardPanel<PreferencesPage> getCards() {
        if (cards == null) {
            cards = new CardPanel<PreferencesPage>();
        }
        
        return cards;
    }

    protected JButton getOkButton() {
        if (okButton == null) {
            okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener() { 
                public void actionPerformed(ActionEvent e) {
                    close();
                }
            });
            getRootPane().setDefaultButton(okButton);
        }
        
        return okButton;
    }

    protected JButton getCancelButton() {
        if (cancelButton == null) {
            cancelButton = new JButton( "Cancel");
            cancelButton.addActionListener( new ActionListener() { 
                public void actionPerformed( ActionEvent e) {
                    cancel();
                }
            });
        }
        
        return cancelButton;
    }

	private DefaultMutableTreeNode getParent(DefaultMutableTreeNode node, PreferencesPage parent) {
		for ( int i = 0; i < node.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode)node.getChildAt( i);
			
			if (child.getUserObject() == parent) {
				return child;
			}

            getParent(child, parent);
		}
		
		return null;
	}
	
	public void add(PreferencesPage parent, PreferencesPage page) {		
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(page);
		
		if (parent == null) {
			model.insertNodeInto(node, root, root.getChildCount());
		} else {
			DefaultMutableTreeNode parentNode = getParent(root, parent); 
			model.insertNodeInto(node, parentNode, parentNode.getChildCount());
		}

		cards.add(page);

		if (root.getChildCount() == 1 && node.getLevel() == 1) {
			tree.expandPath(new TreePath(((DefaultMutableTreeNode)model.getRoot()).getPath()));
			tree.setSelectionPath(new TreePath(node.getPath()));
            cards.show(page);
		}
	}
	
	public void valueChanged(TreeSelectionEvent event) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
		
		String title = ((PreferencesPage)node.getUserObject()).getTitle();
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
		
		while (parent != null) {
            if (parent.getUserObject() instanceof PreferencesPage) {
                title = ((PreferencesPage)parent.getUserObject()).getTitle()+" - "+title;
            } else {
                title = parent.getUserObject().toString()+" - "+title;
            }
            
			parent = (DefaultMutableTreeNode)parent.getParent();
		}
		
		setTitle( title);

		cards.show((PreferencesPage)node.getUserObject());
	}
}
