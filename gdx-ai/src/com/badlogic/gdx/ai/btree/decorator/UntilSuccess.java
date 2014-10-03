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

package com.badlogic.gdx.ai.btree.decorator;

import com.badlogic.gdx.ai.btree.Decorator;
import com.badlogic.gdx.ai.btree.Node;

/**
 * A UntilSuccess Decorator will repeat the wrapped node until that node
 * succeeds. Using UntilSuccess with AlwaysFail without any ancestor call
 * running() will cause StackOverflow.
 *
 * @param <E> type of the blackboard nodes use to read or modify game state
 *
 * @author implicit-invocation
 */
public class UntilSuccess<E> extends Decorator<E> {

  public UntilSuccess() {
  }

  public UntilSuccess(Node<E> node) {
    super(node);
  }

  @Override
  public void childSuccess(Node<E> runningNode) {
    control.childSuccess(this);
  }

  @Override
  public void childFail(Node<E> runningNode) {
    start(object);
    run(object);
  }
}
