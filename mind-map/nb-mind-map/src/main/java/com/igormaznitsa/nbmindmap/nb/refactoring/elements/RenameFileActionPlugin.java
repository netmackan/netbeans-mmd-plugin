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
package com.igormaznitsa.nbmindmap.nb.refactoring.elements;

import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.nbmindmap.nb.refactoring.MutableFileLink;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Locale;
import org.apache.commons.io.FilenameUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class RenameFileActionPlugin extends AbstractPlugin<RenameRefactoring> {

  public RenameFileActionPlugin(final RenameRefactoring refactoring) {
    super(refactoring);
  }

  private static int numberOfFolders(final String text) {
    int result = 0;
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == '/' || text.charAt(i) == '\\') {
        result++;
      }
    }
    return result;
  }

  @Override
  public Problem fastCheckParameters() {
    final String name = this.refactoring.getNewName();
    if (name == null) return new Problem(true,BUNDLE.getString("RenameFileActionPlugin.errorNameIsNull"));
    if (name.trim().isEmpty()) return new Problem(true, BUNDLE.getString("RenameFileActionPlugin.errorNameIsEmpty"));
    if (name.indexOf('.')>=0) return new Problem(true, BUNDLE.getString("RenameFileActionPlugin.errorNameContainsDots"));
    return null;
  }

  protected static String replaceNameInPath(int pathItemIndexFromEnd, final String path, final String newName) {
    int foldersInNewName = numberOfFolders(newName);

    final String normalizedSeparators = FilenameUtils.separatorsToUnix(path);
    int start = normalizedSeparators.length();

    while (start >= 0 && pathItemIndexFromEnd >= 0) {
      start = normalizedSeparators.lastIndexOf('/', start - 1);
      pathItemIndexFromEnd--;
    }

    String result = path;

    if (start >= 0) {
      final int indexEnd = normalizedSeparators.indexOf('/', start + 1);

      while (start >= 0 && foldersInNewName > 0) {
        start = normalizedSeparators.lastIndexOf('/', start - 1);
        foldersInNewName--;
      }

      if (start >= 0) {
        if (indexEnd <= 0) {
          result = path.substring(0, start + 1) + newName;
        }
        else {
          result = path.substring(0, start + 1) + newName + path.substring(indexEnd);
        }
      }
    }
    return result;
  }

  @Override
  protected Problem processFile(final Project project, final int level, final File projectFolder, final FileObject fileObject) {
    final MMapURI fileAsURI = MMapURI.makeFromFilePath(projectFolder, fileObject.getPath(), null);

    String newFileName = this.refactoring.getNewName();

    if (level == 0 && !fileObject.isFolder()) {
      final String ext = FilenameUtils.getExtension(fileObject.getNameExt());
      if (!ext.isEmpty()) {
        if (!newFileName.toLowerCase(Locale.ENGLISH).endsWith('.'+ext))
        newFileName += '.' + ext;
      }
    }
    else {
      newFileName = newFileName.replace('.', '/');
    }

    final MMapURI newFileAsURI;
    try {
      if (level == 0) {
        newFileAsURI = MMapURI.makeFromFilePath(projectFolder, fileObject.getPath(), null).replaceName(newFileName);
      }
      else {
        newFileAsURI = MMapURI.makeFromFilePath(projectFolder, replaceNameInPath(level, fileObject.getPath(), newFileName), null);
      }
    }
    catch (URISyntaxException ex) {
      logger.error("Can't make new file uri for " + fileObject.getPath(), ex); //NOI18N
      return new Problem(true, BUNDLE.getString("Refactoring.CantMakeURI"));
    }

    for (final FileObject mmap : allMapsInProject(project)) {
      if (isCanceled()) {
        break;
      }
      try {
        if (doesMindMapContainFileLink(project, mmap, fileAsURI)) {
          final RenameElement element = new RenameElement(new MutableFileLink(FileUtil.toFile(mmap)), projectFolder, MMapURI.makeFromFilePath(projectFolder, fileObject.getPath(), null));
          element.setNewFile(newFileAsURI);
          addElement(element);
        }
      }
      catch (Exception ex) {
        ErrorManager.getDefault().notify(ex);
        return new Problem(true, BUNDLE.getString("Refactoring.CantProcessMindMap"));
      }
    }

    return null;
  }
}
