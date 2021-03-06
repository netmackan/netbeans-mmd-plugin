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

import java.io.StringReader;
import java.io.StringWriter;
import org.junit.Test;
import static org.junit.Assert.*;

public class MindMapTest {
  
  @Test
  public void testMindMapParse_NoAttributes() throws Exception {
    final MindMap map = new MindMap(null,new StringReader("lkf\n---\n# Hello"));
    assertNull(map.getAttribute("test"));
    assertEquals("Hello",map.getRoot().getText());
  }

  @Test
  public void testMindMapParse_oneAttributeDoubleNewLine() throws Exception {
    final MindMap map = new MindMap(null,new StringReader("lkf\n> test=`Hi`\n\n---\n# Hello"));
    assertEquals("Hi",map.getAttribute("test"));
    assertEquals("Hello",map.getRoot().getText());
  }

  @Test
  public void testMindMapParse_oneAttributeSingleNewLine() throws Exception {
    final MindMap map = new MindMap(null,new StringReader("lkf\n> test=`Hi`\n---\n# Hello"));
    assertEquals("Hi",map.getAttribute("test"));
    assertEquals("Hello",map.getRoot().getText());
  }

  @Test
  public void testMindMapParse_overridenAttributes_SingleNewLine() throws Exception {
    final MindMap map = new MindMap(null,new StringReader("lkf\n> test=`Hi`\n> test=`Lo`\n---\n# Hello"));
    assertEquals("Lo",map.getAttribute("test"));
    assertEquals("Hello",map.getRoot().getText());
  } 
  
  @Test
  public void testMindMapParse_overridenAttributes_SeveralNewLine() throws Exception {
    final MindMap map = new MindMap(null,new StringReader("lkf\n> test=`Hi`\n> test=`Lo`\n---\n\n\n\n# Hello"));
    assertEquals("Lo",map.getAttribute("test"));
    assertEquals("Hello",map.getRoot().getText());
  }
  
  @Test
  public void testMindMapWrite_WithoutAttributes() throws Exception {
    final MindMap map = new MindMap(null);
    final StringWriter writer = new StringWriter();
    map.write(writer);
    assertEquals("Mind Map generated by NB MindMap plugin   \n> __version__=`1.0`\n---\n\n# \n",writer.toString());
  }

  @Test
  public void testMindMapWrite_WithAttribute() throws Exception {
    final MindMap map = new MindMap(null);
    map.setAttribute("hello", "World");
    final StringWriter writer = new StringWriter();
    map.write(writer);
    assertEquals("Mind Map generated by NB MindMap plugin   \n> __version__=`1.0`,hello=`World`\n---\n\n# \n",writer.toString());
  }
}
