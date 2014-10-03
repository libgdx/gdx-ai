/* 
 * Copyright 2014  See AUTHORS file.
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

package com.badlogic.gdx.ai.btree.parser;

import com.badlogic.gdx.utils.Array;

/**
 *
 * @author implicit-invocation
 */
public class TreeParser {

  public static <T> T parse(String data, LineProcessor<T> processor) {
    String[] lines = data.split("\\r?\\n");
    int currentDepth = -1;
    T root = null;
    T prev = null;
    int step = 1;
    Array<T> out = new Array<T>();
    for (String string : lines) {
      String line = string.replaceAll("^[ \\t]+", "");
      int tabs = string.length() - line.length();
      if (prev == null) {
        root = processor.process(line);
        prev = root;
      } else {
        if (prev == root) {
          step = tabs;
        }
        T t = processor.process(line);
        if (tabs > currentDepth) {
          out.add(prev); // push
        } else if (tabs < currentDepth) {
          int i = (currentDepth - tabs) / step;
          for (int j = 0; j < i; j++) {
            out.pop();
          }
        }
        processor.addChild(out.peek(), t);
        prev = t;
      }
      currentDepth = tabs;
    }
    return root;
  }
}
