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

import com.badlogic.gdx.ai.btree.Node;
import com.badlogic.gdx.ai.btree.branch.Parallel;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.decorator.AlwaysFail;
import com.badlogic.gdx.ai.btree.decorator.AlwaysSucceed;
import com.badlogic.gdx.ai.btree.decorator.Invert;
import com.badlogic.gdx.ai.btree.decorator.UntilFail;
import com.badlogic.gdx.ai.btree.decorator.UntilSuccess;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author implicit-invocation
 */
public class TreeLineProcessor implements LineProcessor<Node> {

  public Node process(String line) {
    String[] frags = line.split(":");
    String type = frags[0];
    if ("sequence".equals(type)) {
      return new Sequence(new ArrayList<Node>());
    } else if ("selector".equals(type)) {
      return new Selector(new ArrayList<Node>());
    } else if ("parallel".equals(type)) {
      return new Parallel(new ArrayList<Node>());
    } else if ("alwaysFail".equals(type)) {
      return new AlwaysFail(null);
    } else if ("alwaysSucceed".equals(type)) {
      return new AlwaysSucceed(null);
    } else if ("invert".equals(type)) {
      return new Invert(null);
    } else if ("untilFail".equals(type)) {
      return new UntilFail(null);
    } else if ("untilSuccess".equals(type)) {
      return new UntilSuccess(null);
    } else if ("task".equals(type)) {
      String className = frags[1];
      try {
        return (Node) Class.forName(className).newInstance();
      } catch (ClassNotFoundException ex) {
        Logger.getLogger(TreeLineProcessor.class.getName()).log(Level.SEVERE, null, ex);
      } catch (InstantiationException ex) {
        Logger.getLogger(TreeLineProcessor.class.getName()).log(Level.SEVERE, null, ex);
      } catch (IllegalAccessException ex) {
        Logger.getLogger(TreeLineProcessor.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return null;
  }

  public void addChild(Node parent, Node child) {
    parent.addChild(child);
  }

}
