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
package com.igormaznitsa.mindmap.exporters;

import static com.igormaznitsa.mindmap.exporters.AbstractMindMapExporter.BUNDLE;
import static com.igormaznitsa.mindmap.exporters.AbstractMindMapExporter.selectFileForFileFilter;
import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.utils.Icons;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import org.apache.commons.io.IOUtils;

public class TextExporter extends AbstractMindMapExporter {

  private static final int SHIFT_STEP = 1;

  private static class State {

    private static final String NEXT_LINE = System.getProperty("line.separator", "\n");//NOI18N
    private final StringBuilder buffer = new StringBuilder(16384);

    public State append(final char ch) {
      this.buffer.append(ch);
      return this;
    }

    public State append(final String str) {
      this.buffer.append(str);
      return this;
    }

    public State nextLine() {
      this.buffer.append(NEXT_LINE);
      return this;
    }

    @Override
    public String toString() {
      return this.buffer.toString();
    }

  }

  private static String[] split(final String text) {
    return text.replace("\r", "").split("\\n");//NOI18N
  }

  private static String replaceAllNextLineSeq(final String text, final String newNextLine) {
    return text.replace("\r", "").replace("\n", newNextLine);//NOI18N
  }

  private static String shiftString(final String text, final char fill, final int shift) {
    final String[] lines = split(text);
    final StringBuilder builder = new StringBuilder();
    final String line = generateString(fill, shift);
    boolean nofirst = false;
    for (final String s : lines) {
      if (nofirst) {
        builder.append(State.NEXT_LINE);
      }
      else {
        nofirst = true;
      }
      builder.append(line).append(s);
    }
    return builder.toString();
  }

  private static String generateString(final char chr, final int length) {
    final StringBuilder buffer = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      buffer.append(chr);
    }
    return buffer.toString();
  }

  private static String makeLineFromString(final String text) {
    final StringBuilder result = new StringBuilder(text.length());

    for (final char c : text.toCharArray()) {
      if (Character.isISOControl(c)) {
        result.append(' ');
      }
      else {
        result.append(c);
      }
    }

    return result.toString();
  }

  private static int getMaxLineWidth(final String text) {
    final String[] lines = replaceAllNextLineSeq(text, "\n").split("\\n");//NOI18N
    int max = 0;
    for (final String s : lines) {
      max = Math.max(s.length(), max);
    }
    return max;
  }

  private static void writeTopic(final Topic topic, final char ch, final int shift, final State state) {
    final int maxLen = getMaxLineWidth(topic.getText());
    state.append(shiftString(topic.getText(), ' ', shift)).nextLine().append(shiftString(generateString(ch, maxLen + 2), ' ', shift)).nextLine();//NOI18N

    final ExtraFile file = (ExtraFile) topic.getExtras().get(Extra.ExtraType.FILE);
    final ExtraLink link = (ExtraLink) topic.getExtras().get(Extra.ExtraType.LINK);
    final ExtraNote note = (ExtraNote) topic.getExtras().get(Extra.ExtraType.NOTE);
    final ExtraTopic transition = (ExtraTopic) topic.getExtras().get(Extra.ExtraType.TOPIC);

    boolean hasExtras = false;
    boolean extrasPrinted = false;

    if (file != null || link != null || note != null || transition != null) {
      hasExtras = true;
    }

    if (file != null) {
      final String uri = file.getValue().asString(false, false);
      state.append(shiftString("FILE: ", ' ', shift)).append(uri).nextLine();//NOI18N
      extrasPrinted = true;
    }

    if (link != null) {
      final String uri = link.getValue().asString(false, false);
      state.append(shiftString("URL: ", ' ', shift)).append(uri).nextLine();//NOI18N
      extrasPrinted = true;
    }

    if (transition != null) {
      final Topic linkedTopic = topic.getMap().findTopicForLink(transition);
      state.append(shiftString("Related to: ", ' ', shift)).append(linkedTopic == null ? "<UNKNOWN>" : '\"' + makeLineFromString(linkedTopic.getText()) + "\"").nextLine();//NOI18N
      extrasPrinted = true;
    }

    if (note != null) {
      if (extrasPrinted) {
        state.nextLine();
      }
      state.append(shiftString(note.getValue(), ' ', shift)).nextLine();
    }
  }

  private void writeInterTopicLine(final State state) {
    state.nextLine();
  }

  private void writeOtherTopicRecursively(final Topic t, int shift, final State state) {
    writeInterTopicLine(state);
    writeTopic(t, '.', shift, state);
    shift += SHIFT_STEP;
    for (final Topic ch : t.getChildren()) {
      writeOtherTopicRecursively(ch, shift, state);
    }
  }

  @Override
  public void doExport(final MindMapPanel panel, final JComponent options, final OutputStream out) throws IOException {
    final State state = new State();

    state.append("# Generated by NB Mind Map Plugin (https://github.com/raydac/netbeans-mmd-plugin)").nextLine();//NOI18N
    state.append("# ").append(new Timestamp(new java.util.Date().getTime()).toString()).nextLine().nextLine();//NOI18N

    int shift = 0;

    final Topic root = panel.getModel().getRoot();
    if (root != null) {
      writeTopic(root, '=', shift, state);//NOI18N

      shift += SHIFT_STEP;

      final Topic[] children = Utils.getLeftToRightOrderedChildrens(root);
      for (final Topic t : children) {
        writeInterTopicLine(state);
        writeTopic(t, '-', shift, state);
        shift += SHIFT_STEP;
        for (final Topic tt : t.getChildren()) {
          writeOtherTopicRecursively(tt, shift, state);
        }
        shift -= SHIFT_STEP;
      }
    }

    final String text = state.toString();

    File fileToSaveMap = null;
    OutputStream theOut = out;
    if (theOut == null) {
      fileToSaveMap = selectFileForFileFilter(panel, BUNDLE.getString("TextExporter.saveDialogTitle"), ".txt", BUNDLE.getString("TextExporter.filterDescription"), BUNDLE.getString("TextExporter.approveButtonText"));
      fileToSaveMap = checkFileAndExtension(panel, fileToSaveMap, ".txt");//NOI18N
      theOut = fileToSaveMap == null ? null : new BufferedOutputStream(new FileOutputStream(fileToSaveMap, false));
    }
    if (theOut != null) {
      try {
        IOUtils.write(text, theOut, "UTF-8");
      }
      finally {
        if (fileToSaveMap != null) {
          IOUtils.closeQuietly(theOut);
        }
      }
    }
  }

  @Override
  public String getName() {
    return BUNDLE.getString("TextExporter.exporterName");
  }

  @Override
  public String getReference() {
    return BUNDLE.getString("TextExporter.exporterReference");
  }

  @Override
  public ImageIcon getIcon() {
    return Icons.ICO_TXT.getIcon();
  }

}
