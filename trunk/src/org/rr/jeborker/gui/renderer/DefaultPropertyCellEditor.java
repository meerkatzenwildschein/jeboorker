/**
 * L2FProd.com Common Components 7.3 License.
 *
 * Copyright 2005-2007 L2FProd.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rr.jeborker.gui.renderer;

import java.util.List;

import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.rr.commons.utils.ListUtils;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;
import com.l2fprod.common.swing.LookAndFeelTweaks;

/**
 * StringPropertyEditor.<br>
 *
 */
public class DefaultPropertyCellEditor extends AbstractPropertyEditor {

  public DefaultPropertyCellEditor() {
    editor = new JTextField();
    ((JTextField) editor).setBorder(LookAndFeelTweaks.EMPTY_BORDER);
  }
  
  public Object getValue() {
    return ((JTextComponent)editor).getText();
  }
  
  public void setValue(Object value) {
    if (value == null) {
      ((JTextComponent)editor).setText("");
    } else {
    	if(value instanceof List<?>) {
    		Object first = ListUtils.first((List<?>) value);
    		((JTextComponent)editor).setText(String.valueOf(first));
    	} else {
    		((JTextComponent)editor).setText(String.valueOf(value));
    	}
    }
  }
  
}
