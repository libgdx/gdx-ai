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

package com.badlogic.gdx.ai.btree;

/**
 * Decorators are wrappers that provide custom behaviors for nodes of any kind
 * (branch node and task)
 *
 * @param <E> type of the blackboard nodes use to read or modify game state
 *
 * @author implicit-invocation
 */
public abstract class Decorator<E> extends Node<E> {

  private Node<E> node;

  /**
   * Creates a decorator with no child node.
   */
  public Decorator() {
  }

  /**
   * Creates a decorator that wraps a tree node
   *
   * @param node the node that will be wrapped
   */
  public Decorator(Node<E> node) {
    this.node = node;
  }

  @Override
  public void addChild(Node<E> node) {
    this.node = node;
  }

  @Override
  public void run(E object) {
    this.object = object;
    node.run(object);
  }

  @Override
  public void end(E object) {
    node.end(object);
  }

  @Override
  public void start(E object) {
    node.setControl(this);
    node.start(object);
  }

  @Override
  public void childRunning(Node<E> runningNode, Node<E> reporter) {
    control.childRunning(runningNode, this);
  }

  @Override
  public void childFail(Node<E> runningNode) {
    control.childFail(this);
  }

  @Override
  public void childSuccess(Node<E> runningNode) {
    control.childSuccess(this);
  }

}
