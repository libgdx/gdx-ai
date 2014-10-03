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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.Node;
import com.badlogic.gdx.ai.btree.branch.Parallel;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.decorator.AlwaysFail;
import com.badlogic.gdx.ai.btree.decorator.AlwaysSucceed;
import com.badlogic.gdx.ai.btree.decorator.Invert;
import com.badlogic.gdx.ai.btree.decorator.UntilFail;
import com.badlogic.gdx.ai.btree.decorator.UntilSuccess;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

/**
 *
 * @author implicit-invocation
 */
public class TreeLineProcessor<E> implements LineProcessor<Node<E>> {

  public Node<E> process(String line) {
    String[] frags = line.split(":");
    String type = frags[0];
    if ("sequence".equals(type)) {
      return new Sequence<E>(new Array<Node<E>>());
    } else if ("selector".equals(type)) {
      return new Selector<E>(new Array<Node<E>>());
    } else if ("parallel".equals(type)) {
      return new Parallel<E>(new Array<Node<E>>());
    } else if ("alwaysFail".equals(type)) {
      return new AlwaysFail<E>();
    } else if ("alwaysSucceed".equals(type)) {
      return new AlwaysSucceed<E>();
    } else if ("invert".equals(type)) {
      return new Invert<E>();
    } else if ("untilFail".equals(type)) {
      return new UntilFail<E>();
    } else if ("untilSuccess".equals(type)) {
      return new UntilSuccess<E>();
    } else if ("task".equals(type)) {
      String className = frags[1];
      try {
        @SuppressWarnings("unchecked")
		  Node<E> newInstance = (Node<E>) ClassReflection.forName(className).newInstance();
		  return newInstance;
      } catch (ReflectionException e) {
         Gdx.app.log(TreeLineProcessor.class.getName(), null, e);
      } catch (InstantiationException ex) {
        Gdx.app.log(TreeLineProcessor.class.getName(), null, ex);
      } catch (IllegalAccessException ex) {
        Gdx.app.log(TreeLineProcessor.class.getName(), null, ex);
		}
    }
    return null;
  }

  public void addChild(Node<E> parent, Node<E> child) {
    parent.addChild(child);
  }

}
