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
package com.badlogic.gdx.ai.btree.branch;

import com.badlogic.gdx.ai.btree.BranchNode;
import com.badlogic.gdx.ai.btree.Node;
import java.util.ArrayList;
import java.util.List;

/**
 * A parallel is a special branch node that starts or resumes all children nodes
 * every single time, parallel node will succeed if all the children succeed,
 * fail if one of the children fail. The typical usecase: make the game entity
 * react on event while sleeping or wandering.
 *
 * @param <E> type of the blackboard nodes use to read or modify game state
 *
 * @author implicit-invocation
 */
public class Parallel<E> extends BranchNode<E> {

  private final List<Node<E>> runningNodes = new ArrayList<Node<E>>();
  private boolean success = true;
  private int notDone;

  public Parallel(List<Node<E>> nodes) {
    super(nodes);
  }

  @Override
  public void start(E object) {
    this.object = object;
    runningNodes.clear();
    success = true;
  }

  @Override
  public void childRunning(Node<E> node, Node<E> reporter) {
    if (!runningNodes.contains(reporter)) {
      runningNodes.add(reporter);
    }
    notDone--;
    control.childRunning(this, this);
  }

  @Override
  public void run(E object) {
    notDone = children.size();
    this.object = object;
    for (Node<E> node : children) {
      if (runningNodes.contains(node)) {
        node.run(object);
      } else {
        node.setControl(this);
        node.start(object);
        node.run(object);
      }
    }
  }

  @Override
  public void childSuccess(Node<E> runningNode) {
    runningNodes.remove(runningNode);
    success = success && true;
    notDone--;
    if (runningNodes.isEmpty() && notDone == 0) {
      if (success) {
        success();
      } else {
        fail();
      }
    }
  }

  @Override
  public void childFail(Node<E> runningNode) {
    runningNodes.remove(runningNode);
    success = success && false;
    notDone--;
    if (runningNodes.isEmpty() && notDone == 0) {
      if (success) {
        success();
      } else {
        fail();
      }
    }
  }

}
