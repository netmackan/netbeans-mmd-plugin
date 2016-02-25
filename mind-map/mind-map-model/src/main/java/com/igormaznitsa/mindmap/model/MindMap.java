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
package com.igormaznitsa.mindmap.model;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.io.IOUtils;

import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.parser.Lexer;

public final class MindMap implements Serializable, Constants, TreeModel {

  private static final long serialVersionUID = 5929181596778047354L;

  private static final Logger LOGGER = LoggerFactory.getLogger(MindMap.class);

  @Nullable
  private final Topic root;

  private final Lock locker = new ReentrantLock();
  private final Map<String, String> attributes = new HashMap<String, String>();
  private static final Pattern PATTERN_ATTRIBUTES = Pattern.compile("^\\s*\\>\\s(.+)$"); //NOI18N
  private static final Pattern PATTERN_ATTRIBUTE = Pattern.compile("[,]?\\s*([\\S]+?)\\s*=\\s*(\\`+)(.*?)\\2"); //NOI18N

  private static final String GENERATOR_VERSION_NAME = "__version__"; //NOI18N
  private static final String GENERATOR_VERSION = "1.0"; //NOI18N

  private final transient List<TreeModelListener> treeListeners = new ArrayList<TreeModelListener>();

  private final MindMapController controller;

  public MindMap(@Nullable final MindMapController nullableController) {
    this.root = new Topic(this, null, ""); //NOI18N
    this.controller = nullableController;
  }

  public MindMap(@Nonnull final MindMap map, @Nullable final MindMapController nullableController) {
    this.attributes.putAll(map.attributes);
    final Topic rootTopic = map.getRoot();
    this.root = rootTopic == null ? null : rootTopic.makeCopy(this, null);
    this.controller = nullableController;
  }

  public MindMap(@Nullable final MindMapController nullableController, @Nonnull final Reader reader) throws IOException {
    this.controller = nullableController;
    final String text = IOUtils.toString(Assertions.assertNotNull(reader));

    final Lexer lexer = new Lexer();
    lexer.start(text, 0, text.length(), Lexer.TokenType.HEAD_LINE);

    Topic rootTopic = null;

    boolean process = true;

    while (process) {
      lexer.advance();
      final Lexer.TokenType token = lexer.getTokenType();
      if (token == null) {
        throw new IllegalArgumentException("Wrong format of mind map, end of header is not found");
      }
      switch (token) {
        case HEAD_LINE:
          continue;
        case ATTRIBUTE: {
          fillMapByAttributes(lexer.getTokenText(), this.attributes);
        }
        break;
        case HEAD_DELIMITER: {
          process = false;
          rootTopic = Topic.parse(this, lexer);
        }
        break;
      }
    }

    this.root = rootTopic;
    this.attributes.put(GENERATOR_VERSION_NAME, GENERATOR_VERSION);
  }

  @Nullable
  public MindMapController getController() {
    return this.controller;
  }

  private void fireModelChanged() {
    final Topic rootTopic = this.root;
    final TreeModelEvent evt = new TreeModelEvent(this, rootTopic == null ? (Topic[]) null : rootTopic.getPath());
    for (final TreeModelListener l : this.treeListeners) {
      l.treeStructureChanged(evt);
    }
  }

  private void fireTopicChanged(@Nullable final Topic topic) {
    final TreeModelEvent evt = new TreeModelEvent(this, topic == null ? null : topic.getPath());
    for (final TreeModelListener l : this.treeListeners) {
      l.treeNodesChanged(evt);
    }
  }

  @Nullable
  public String getAttribute(@Nonnull final String name) {
    return this.attributes.get(name);
  }

  public void setAttribute(@Nonnull final String name, @Nullable final String value) {
    this.locker.lock();
    try {
      if (value == null) {
        this.attributes.remove(name);
      }
      else {
        this.attributes.put(name, value);
      }
    }
    finally {
      this.locker.unlock();
    }
  }

  public void resetPayload() {
    this.locker.lock();
    try {
      if (this.root != null) {
        resetPayload(this.root);
      }
    }
    finally {
      this.locker.unlock();
    }
  }

  private void resetPayload(@Nullable final Topic t) {
    if (t != null) {
      t.setPayload(null);
      for (final Topic m : t.getChildren()) {
        resetPayload(m);
      }
    }
  }

  @Nullable
  public Topic findForPositionPath(@Nullable final int[] positions) {
    if (positions == null || positions.length == 0) {
      return null;
    }
    if (positions.length == 1) {
      return this.root;
    }

    Topic result = this.root;
    int index = 1;
    while (result != null && index < positions.length) {
      final int elementPosition = positions[index++];
      if (elementPosition < 0 || result.getChildren().size() <= elementPosition) {
        result = null;
        break;
      }
      result = result.getChildren().get(elementPosition);
    }
    return result;
  }

  @Nonnull
  public List<Topic> removeNonExistingTopics(@Nonnull final List<Topic> origList) {
    final List<Topic> result = new ArrayList<Topic>();
    final Topic rootTopic = this.root;
    if (rootTopic != null) {
      this.locker.lock();
      try {
        for (final Topic t : origList) {
          if (rootTopic.containTopic(t)) {
            result.add(t);
          }
        }
      }
      finally {
        this.locker.unlock();
      }
    }
    return result;
  }

  @Override
  @Nullable
  public Topic getRoot() {
    this.locker.lock();
    try {
      return this.root;
    }
    finally {
      this.locker.unlock();
    }
  }

  static boolean fillMapByAttributes(@Nonnull final String line, @Nonnull final Map<String, String> map) {
    final Matcher attrmatcher = PATTERN_ATTRIBUTES.matcher(line);
    if (attrmatcher.find()) {
      final Matcher attrParser = PATTERN_ATTRIBUTE.matcher(attrmatcher.group(1));
      while (attrParser.find()) {
        map.put(attrParser.group(1), attrParser.group(3));
      }
      return true;
    }
    return false;
  }

  static String allAttributesAsString(@Nonnull final Map<String, String> map) throws IOException {
    final StringBuilder buffer = new StringBuilder();

    boolean nonfirst = false;
    for (final Map.Entry<String, String> e : map.entrySet()) {
      if (nonfirst) {
        buffer.append(',');
      }
      else {
        nonfirst = true;
      }
      buffer.append(e.getKey()).append('=').append(ModelUtils.makeMDCodeBlock(e.getValue()));
    }

    return buffer.toString();
  }

  @Nonnull
  public String packToString() {
    final StringWriter writer = new StringWriter(16384);
    try {
      write(writer);
    }
    catch (IOException ex) {
      throw new Error("Unexpected exception", ex);
    }
    return writer.toString();
  }

  public void write(@Nonnull final Writer out) throws IOException {
    this.locker.lock();
    try {
      out.append("Mind Map generated by NB MindMap plugin").append(NEXT_PARAGRAPH); //NOI18N
      this.attributes.put(GENERATOR_VERSION_NAME, GENERATOR_VERSION);
      if (!this.attributes.isEmpty()) {
        out.append("> ").append(MindMap.allAttributesAsString(this.attributes)).append(NEXT_LINE); //NOI18N
      }
      out.append("---").append(NEXT_LINE); //NOI18N
      final Topic rootTopic = this.root;
      if (rootTopic != null) {
        rootTopic.write(out);
      }
    }
    finally {
      this.locker.unlock();
    }
  }

  public void lock() {
    this.locker.lock();
  }

  public void unlock() {
    this.locker.unlock();
  }

  @Nullable
  public Topic cloneTopic(@Nullable final Topic topic, final boolean cloneFullTree) {
    this.locker.lock();
    try {
      if (topic == null || topic == this.root) {
        return null;
      }

      final Topic clonedtopic = topic.makeCopy(this, topic.getParent());
      if (!cloneFullTree) {
        clonedtopic.removeAllChildren();
      }

      clonedtopic.removeAttributeFromSubtree(ExtraTopic.TOPIC_UID_ATTR);

      fireModelChanged();

      return clonedtopic;
    }
    finally {
      this.locker.unlock();
    }
  }

  public boolean removeTopic(@Nullable final Topic topic) {
    this.locker.lock();
    try {
      final boolean result;
      final Topic rootTopic = this.root;
      if (rootTopic == null) {
        result = false;
      }
      else if (this.root == topic) {
        rootTopic.setText(""); //NOI18N
        rootTopic.removeExtras();
        rootTopic.setPayload(null);
        rootTopic.removeAllChildren();
        result = true;
      }
      else {
        rootTopic.removeTopic(topic);
        result = rootTopic.removeAllLinksTo(topic);
      }
      if (result) {
        fireModelChanged();
      }

      return result;
    }
    finally {
      this.locker.unlock();
    }
  }

  @Nullable
  public Topic findTopicForLink(@Nullable final ExtraTopic link) {
    Topic result = null;
    if (link != null) {
      final Topic rootTopic = this.root;
      if (rootTopic != null) {
        this.locker.lock();
        try {
          result = rootTopic.findForAttribute(ExtraTopic.TOPIC_UID_ATTR, link.getValue());
        }
        finally {
          this.locker.unlock();
        }
      }
    }
    return result;
  }

  @Nonnull
  public List<Topic> findAllTopicsForExtraType(@Nonnull final Extra.ExtraType type) {
    final List<Topic> result = new ArrayList<Topic>();
    final Topic rootTopic = this.root;
    if (rootTopic != null) {
      this.locker.lock();
      try {
        _findAllTopicsForExtraType(rootTopic, type, result);
      }
      finally {
        this.locker.unlock();
      }
    }
    return result;
  }

  private void _findAllTopicsForExtraType(@Nonnull final Topic topic, @Nonnull final Extra.ExtraType type, @Nonnull final List<Topic> result) {
    if (topic.getExtras().containsKey(type)) {
      result.add(topic);
    }
    for (final Topic c : topic.getChildren()) {
      _findAllTopicsForExtraType(c, type, result);
    }
  }

  @Override
  public Object getChild(@Nonnull final Object parent, final int index) {
    return ((Topic) parent).getChildren().get(index);
  }

  @Override
  public int getChildCount(@Nonnull Object parent) {
    return ((Topic) parent).getChildren().size();
  }

  @Override
  public boolean isLeaf(@Nonnull final Object node) {
    return !((Topic) node).hasChildren();
  }

  @Override
  public void valueForPathChanged(@Nonnull final TreePath path, @Nullable final Object newValue) {
    if (newValue instanceof String) {
      ((Topic) path.getLastPathComponent()).setText((String) newValue);
      fireTopicChanged((Topic) path.getLastPathComponent());
    }
    else {
      LOGGER.warn("Attempt to set non string value to path : " + path); //NOI18N
    }
  }

  @Override
  public int getIndexOfChild(@Nonnull final Object parent, @Nullable final Object child) {
    return ((Topic) parent).getChildren().indexOf(child);
  }

  @Override
  public void addTreeModelListener(@Nonnull final TreeModelListener l) {
    this.treeListeners.add(l);
  }

  @Override
  public void removeTreeModelListener(@Nonnull final TreeModelListener l) {
    this.treeListeners.remove(l);
  }

  public boolean doesContainFileLink(@Nonnull final File baseFolder, @Nonnull final MMapURI file) {
    boolean result = false;
    final Topic rootTopic = this.root;
    if (rootTopic != null) {
      this.locker.lock();
      try {
        return rootTopic.doesContainFileLink(baseFolder, file);
      }
      finally {
        this.locker.unlock();
      }
    }
    return result;
  }

  public boolean deleteAllLinksToFile(@Nonnull final File baseFolder, @Nonnull final MMapURI file) {
    boolean changed = false;
    final Topic rootTopic = this.root;
    if (rootTopic != null) {
      this.locker.lock();
      try {
        changed = rootTopic.deleteLinkToFileIfPresented(baseFolder, file);
      }
      finally {
        this.locker.unlock();
      }
      if (changed) {
        fireModelChanged();
      }
    }
    return changed;
  }

  public boolean replaceAllLinksToFile(@Nonnull final File baseFolder, @Nonnull final MMapURI oldFile, @Nonnull final MMapURI newFile) {
    boolean changed = false;
    final Topic rootTopic = this.root;
    if (rootTopic != null) {
      this.locker.lock();
      try {
        changed = rootTopic.replaceLinkToFileIfPresented(baseFolder, oldFile, newFile);
      }
      finally {
        this.locker.unlock();
      }
      if (changed) {
        fireModelChanged();
      }
    }
    return changed;
  }

}
