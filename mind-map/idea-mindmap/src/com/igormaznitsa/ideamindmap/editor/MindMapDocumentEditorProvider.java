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
package com.igormaznitsa.ideamindmap.editor;

import com.igormaznitsa.ideamindmap.filetype.MindMapFileType;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import javax.annotation.Nonnull;

public class MindMapDocumentEditorProvider implements FileEditorProvider, DumbAware {
  @Override
  public boolean accept(@Nonnull Project project, @Nonnull VirtualFile virtualFile) {
    return virtualFile.getFileType() instanceof MindMapFileType;
  }

  @Nonnull
  @Override
  public FileEditor createEditor(@Nonnull Project project, @Nonnull VirtualFile virtualFile) {
    return new MindMapDocumentEditor(project, virtualFile);
  }

  @Override
  public void disposeEditor(@Nonnull FileEditor fileEditor) {

  }

  @Nonnull
  @Override
  public FileEditorState readState(@Nonnull Element element, @Nonnull Project project, @Nonnull VirtualFile virtualFile) {
    return MindMapFileEditorState.DUMMY;
  }

  @Override
  public void writeState(@Nonnull FileEditorState fileEditorState, @Nonnull Project project, @Nonnull Element element) {

  }

  @Nonnull
  @Override
  public String getEditorTypeId() {
    return "com.igormaznitsa.ideamindmap.editor.MindMapDocumentEditor";
  }

  @Nonnull
  @Override
  public FileEditorPolicy getPolicy() {
    return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
  }
}
