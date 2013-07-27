package org.rr.jeborker.gui.cell;

import java.awt.Component;

import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import org.rr.commons.swing.SwingUtils;

public class FilterFieldComboboxEditor extends BasicComboBoxEditor {
	
    public Component getEditorComponent() {
    	RendererUtils.setColor(editor, false);
    	editor.setSelectedTextColor(SwingUtils.getSelectionForegroundColor());
    	editor.setSelectionColor(SwingUtils.getSelectionBackgroundColor());
        return editor;
    }
    
    /**
     * Creates the internal editor component. Override this to provide
     * a custom implementation.
     *
     * @return a new editor component
     * @since 1.6
     */
    protected JTextField createEditorComponent() {
        JTextField editor = new BorderlessTextField("", 9);
        editor.setBorder(null);
        return editor;
    }    
    
    static class BorderlessTextField extends JTextField {
        public BorderlessTextField(String value, int n) {
            super(value, n);
        }

        // workaround for 4530952
        public void setText(String s) {
            if (getText().equals(s)) {
                return;
            }
            super.setText(s);
        }

        public void setBorder(Border b) {
            if (!(b instanceof UIResource)) {
                super.setBorder(b);
            }
        }
    }    
}
