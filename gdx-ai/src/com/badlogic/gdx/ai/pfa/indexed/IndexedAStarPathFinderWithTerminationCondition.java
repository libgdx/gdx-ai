/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.ai.pfa.indexed;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.PathFinder;
import com.badlogic.gdx.ai.pfa.PathFinderQueue;
import com.badlogic.gdx.ai.pfa.PathFinderRequest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BinaryHeap;
import com.badlogic.gdx.utils.TimeUtils;

/**
 * Extends IndexedAStarPathFinder
 *
 * Allows search for a given Node or given termination condition
 *
 * Example Termination Condition: Node contains 'health' item type
 *
 * @param <N> Type of node extending {@link IndexedNode}
 *
 * @author fvolz */
public abstract class IndexedAStarPathFinderWithTerminationCondition<N extends IndexedNode<N>> extends IndexedAStarPathFinder<N> {

    public IndexedAStarPathFinderWithTerminationCondition(IndexedGraph<N> graph) {
        super(graph, false);
    }

    public abstract boolean isTerminationConditionSatisfied(N current, N target);

    @Override
    public boolean searchNodePath (N startNode, N endNode, Heuristic<N> heuristic, GraphPath<N> outPath) {

        // Perform AStar
        search(startNode, endNode, heuristic);

        // We're here if we've either found the goal, or if we've no more nodes to search, find which

        //fv: overrode to apply 'equals' operator
        if (!current.node.equals(endNode)) {
            // We've run out of nodes without finding the goal, so there's no solution
            return false;
        }

        generateNodePath(startNode, outPath);

        return true;
    }

    @Override
    protected void search(N startNode, N endNode, Heuristic<N> heuristic) {

        initSearch(startNode, endNode, heuristic);

        // Iterate through processing each node
        do {
            // Retrieve the node with smallest estimated total cost from the open list
            current = openList.pop();
            current.category = IndexedAStarPathFinder.CLOSED;

            //Terminate if we reached the goal node/condition
            if (isTerminationConditionSatisfied(current.node, endNode)) {
                endNode = current.node;

                return;
            }
            ;

            visitChildren(endNode, heuristic);

        } while (openList.size > 0);
    }
}

