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

package org.bounce.wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import org.bounce.CardPanel;
import org.bounce.QDialog;
import org.bounce.QPanel;

public abstract class Wizard extends QDialog implements WizardPageListener {
    private static final long serialVersionUID = 7119005231853816073L;
    
    private JLabel titleLabel = null;
    private JLabel descriptionLabel = null;
    private JLabel iconLabel = null;

    private CardPanel<WizardPage> cards = null;

    private JButton nextButton = null;
    private JButton backButton = null;
    private JButton finishButton = null;
    private JButton cancelButton = null;

    public Wizard(Frame parent) {
        super(parent, true);

        createCenterPanel();
        
        QPanel header = new QPanel(new BorderLayout());

        header.setGradientBackground(true);
        header.setGradientColor(getGradientColor());
        header.setBackground(Color.white);
        header.setOpaque(true);
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1));
        titleLabel = new JLabel();
        titleLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14));
        
        descriptionLabel = new JLabel();
        descriptionLabel.setBorder(new EmptyBorder(5, 20, 5, 10));
        descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(Font.PLAIN));
        
        panel.add(titleLabel);
        panel.add(descriptionLabel);
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        JPanel info = new JPanel();
        info.setLayout(new BorderLayout());
        info.add(panel, BorderLayout.CENTER);
                
        iconLabel = new JLabel();
        iconLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        panel.setOpaque(false);
        info.setOpaque(false);
        
        header.add(iconLabel, BorderLayout.EAST);
        header.add(info, BorderLayout.CENTER);
        
        header.setBorder(new MatteBorder(0, 0, 1, 0, UIManager.getColor("controlDkShadow")));

        JPanel centerPanel = createCenterPanel();
        centerPanel.add(header, BorderLayout.NORTH);
        centerPanel.add(getCards(), BorderLayout.CENTER);
        centerPanel.add(createSouthPanel(), BorderLayout.SOUTH);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(centerPanel, BorderLayout.CENTER);
        setContentPane(contentPane);
    }
    
    protected JPanel createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        return panel;
    }
    
    protected JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(getBackButton());
        buttonPanel.add(getNextButton());
        buttonPanel.add(getFinishButton());
        buttonPanel.add(getCancelButton());
        
        return buttonPanel;
    }
    
    protected JPanel createSouthPanel() {
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, UIManager.getColor("controlDkShadow")), new EmptyBorder(5, 5, 5, 5)));
        southPanel.add(createButtonPanel(), BorderLayout.EAST);
        
        return southPanel;
    }

    protected CardPanel<WizardPage> getCards() {
        if (cards == null) {
            cards = new CardPanel<WizardPage>();
        }
        
        return cards;
    }

    protected JButton getFinishButton() {
        if (finishButton == null) {
            finishButton = new JButton("Finish");
            finishButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    close();
                }
            });
            finishButton.setMnemonic('F');
        }
        
        return finishButton;
    }

    protected JButton getNextButton() {
        if (nextButton == null) {
            nextButton = new JButton("Next");
            nextButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    nextPage();
                }
            });

            nextButton.setMnemonic('N');
        }
        
        return nextButton;
    }

    protected JButton getBackButton() {
        if (backButton == null) {
            backButton = new JButton("Back");
            backButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    backPage();
                }
            });
            backButton.setMnemonic('B');
            backButton.setEnabled(false);
        }
        
        return backButton;
    }

    protected JButton getCancelButton() {
        if (cancelButton == null) {
            cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    cancel();
                }
            });
        }
        
        return cancelButton;
    }

    private Color getGradientColor() {
        Color color = UIManager.getColor("Button.focus");

        if (color == null) {
            return (new JPanel()).getBackground();
        }
        
        while (((color.getBlue() + color.getRed() + color.getGreen()) / 3) < 128) {
            color = color.brighter();
        }
        
        return color;
    }
    
    /**
     * Add a page to the wizard.
     * 
     * @param page the page to add.
     */
    public void addPage(WizardPage page) {
        cards.add(page);
    }
    
    protected abstract String getWizardTitle();

    public void setTitle(String title) {
        super.setTitle(getWizardTitle() + " - " + title);
        
        titleLabel.setText(title);
    }
    
    public void setDescription(String description) {
        descriptionLabel.setText(description);
    }
    
    public void backPage() {
        WizardPage page = cards.selected();
        setPage(page.getBack());
    }

    public void nextPage() {
        WizardPage page = cards.selected();
        WizardPage next = page.getNext();

        next.setBack(page);
        setPage(next);
    }

    protected void setPage(WizardPage page) {
        if (cards.selected() != null) {
            cards.selected().removeWizardPageListener(this);
        }

        cards.show(page);

        setTitle(page.getTitle());
        setDescription(page.getDescription());

        backButton.setEnabled(page.getBack() != null);
        if (page.getNext() != null) {
            nextButton.setEnabled(true);
            getRootPane().setDefaultButton(nextButton);
        } else {
            nextButton.setEnabled(false);
            getRootPane().setDefaultButton(finishButton);
        }

        page.addWizardPageListener(this);
    }
    
    public WizardPage getPage() {
        return cards.selected();
    }
    
    public void pageChanged(WizardPageEvent event) {
        setPage((WizardPage)event.getSource());
    }
}
