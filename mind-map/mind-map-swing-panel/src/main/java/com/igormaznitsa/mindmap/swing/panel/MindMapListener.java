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

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.Topic;
import java.awt.Dimension;

public interface MindMapListener {
  void onMindMapModelChanged(MindMapPanel source);
  void onMindMapModelRealigned(MindMapPanel source, Dimension coveredAreaSize);
  void onEnsureVisibilityOfTopic(MindMapPanel source, Topic topic);
  void onClickOnExtra(MindMapPanel source,int clicks, Topic topic, Extra<?> extra);
  void onChangedSelection(MindMapPanel source, Topic [] currentSelectedTopics);
  boolean allowedRemovingOfTopics(MindMapPanel source, Topic [] topics);
}
