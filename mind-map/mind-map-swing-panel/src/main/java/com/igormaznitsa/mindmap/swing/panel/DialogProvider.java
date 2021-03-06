/*
 * Copyright 2015 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.igormaznitsa.mindmap.swing.panel;

import java.io.File;
import javax.swing.JComponent;
import javax.swing.filechooser.FileFilter;

public interface DialogProvider {
  void msgError(String text);
  void msgInfo(String text);
  void msgWarn(String text);
  boolean msgConfirmOkCancel(String title, String question);
  boolean msgOkCancel(String title, JComponent component);
  boolean msgConfirmYesNo(String title, String question);
  Boolean msgConfirmYesNoCancel(String title, final String question);
  File msgSaveFileDialog(String id, String title, File defaultFolder, boolean filesnly, FileFilter fileFilter, String approveButtonText);
}
