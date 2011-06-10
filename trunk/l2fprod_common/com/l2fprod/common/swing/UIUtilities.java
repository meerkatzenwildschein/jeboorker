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
package com.l2fprod.common.swing;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;

/**
 * UIUtilities. <br>
 *  
 */
public class UIUtilities {

  public static void centerOnScreen(Window window) {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension size = window.getSize();
    window.setLocation(
      (screenSize.width - size.width) / 2,
      (screenSize.height - size.height) / 2);
  }

}
