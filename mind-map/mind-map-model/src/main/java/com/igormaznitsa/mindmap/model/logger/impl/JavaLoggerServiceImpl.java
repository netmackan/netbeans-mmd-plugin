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
package com.igormaznitsa.mindmap.model.logger.impl;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerService;
import java.util.HashMap;
import java.util.Map;

public class JavaLoggerServiceImpl implements LoggerService {

  private final Map<Class<?>,Logger> cacheForClasses = new HashMap<Class<?>, Logger>();
  private final Map<String,Logger> cacheForNames = new HashMap<String, Logger>();
  
  @Override
  public Logger getLogger (final Class<?> klazz) {
    synchronized(this.cacheForClasses){
      Logger result = this.cacheForClasses.get(klazz);
      if(result == null){
        result = new JavaLogger(klazz);
        this.cacheForClasses.put(klazz, result);
      }
      return result;
    }
  }

  @Override
  public Logger getLogger (final String name) {
    synchronized (this.cacheForNames) {
      Logger result = this.cacheForNames.get(name);
      if (result == null) {
        result = new JavaLogger(name);
        this.cacheForNames.put(name, result);
      }
      return result;
    }
  }

}