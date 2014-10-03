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

import com.badlogic.gdx.utils.Array;

/**
 * Node of a behavior tree, has one control and a list of children
 *
 * @param <E> type of the blackboard nodes use to read or modify game state
 *
 * @author implicit-invocation
 */
public abstract class Node<E> {

  protected Node<E> control;
  protected Node<E> runningNode;
  protected Array<Node<E>> children;
  protected E object;

  /**
   * This method will add a child to the list of this node's children
   *
   * @param node the child node which will be added
   */
  public void addChild(Node<E> node) {
    children.add(node);
  }

  /**
   * This method will set a node as this node's control (parent)
   *
   * @param control the parent node
   */
  public void setControl(Node<E> control) {
    this.control = control;
  }

  /**
   * This method will be called once before this node's first run
   *
   * @param object the blackboard
   */
  public void start(E object) {

  }

  /**
   * This method will be called when this node succeeds or fails
   *
   * @param object blackboard
   */
  public void end(E object) {

  }

  /**
   * This method contains update logic of this node
   *
   * @param object blackboard
   */
  public abstract void run(E object);

  /**
   * This method will be called in {@link #run(E)} to inform control that this
   * node needs to run again
   */
  public final void running() {
    control.childRunning(this, this);
  }

  /**
   * This method will be called in {@link #run(E)} to inform control that this
   * node has finished running with a success result
   */
  public void success() {
    end(object);
    control.childSuccess(this);
  }

  /**
   * This method will be called in {@link #run(E)} to inform control that this
   * node has finished running with a failure result
   */
  public void fail() {
    end(object);
    control.childFail(this);
  }

  /**
   * This method will be called when one of the children of this node succeeds
   *
   * @param node the node that succeeded
   */
  public void childSuccess(Node<E> node) {
    this.runningNode = null;
  }

  /**
   * This method will be called when one of the children of this node fails
   *
   * @param node the node that failed
   */
  public void childFail(Node<E> node) {
    this.runningNode = null;
  }

  /**
   * This method will be called when one of the ancestors of this node needs to
   * run again
   *
   * @param runningNode the node that needs to run again
   * @param reporter the node that reports, usually one of this node's children
   */
  public void childRunning(Node<E> runningNode, Node<E> reporter) {
    this.runningNode = runningNode;
  }
}
