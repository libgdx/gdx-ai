package com.badlogic.gdx.ai.btree.utils;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.Pool;

/** A {@code BehaviorTreeLibrary} using reference pool.
 * 
 * {@link BehaviorTree} created by {@link PooledBehaviorTreeLibrary} should be disposed by
 * calling {@link BehaviorTreeLibrary#disposeBehaviorTree(String, BehaviorTree)}.
 * 
 * @author mgsx
 *
 */
@SuppressWarnings("rawtypes")
public class PooledBehaviorTreeLibrary extends BehaviorTreeLibrary
{
	protected ObjectMap<String, Pool<BehaviorTree>> pools = new ObjectMap<String, Pool<BehaviorTree>>();
	
	/**
	 * retrieve pool by tree reference, create it if not already exists.
	 * @param treeReference
	 * @return existing or newly created pool.
	 */
	protected Pool<BehaviorTree> getPool(final String treeReference){
		Pool<BehaviorTree> treePool = pools.get(treeReference);
		if(treePool == null){
			treePool = new Pool<BehaviorTree>(){
				@Override
				protected BehaviorTree newObject() {
					return newBehaviorTree(treeReference);
				}
			};
			pools.put(treeReference, treePool);
		}
		return treePool;
	}
	
	/**
	 * creates concrete tree instance.
	 * @param treeReference
	 * @return a new tree instance.
	 */
	protected <T> BehaviorTree<T> newBehaviorTree (String treeReference) {
		return super.createBehaviorTree(treeReference, null);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> BehaviorTree<T> createBehaviorTree(String treeReference, T blackboard) {
		Pool<BehaviorTree> pool = getPool(treeReference);
		BehaviorTree<T> tree = (BehaviorTree<T>)pool.obtain();
		tree.setObject(blackboard);
		return tree;
	}
	
	@Override
	public void disposeBehaviorTree(final String treeReference, BehaviorTree<?> behaviorTree) {
		Pool<BehaviorTree> pool = getPool(treeReference);
		pool.free(behaviorTree);
	}
	
	/**
	 * Clear pool for a tree reference.
	 * @param treeReference
	 */
	public void clear(String treeReference){
		Pool<BehaviorTree> treePool = pools.get(treeReference);
		if(treePool != null){
			treePool.clear();
		}
	}
	
	/**
	 * clear all pools.
	 */
	public void clear(){
		for(Entry<String, Pool<BehaviorTree>> entry : pools.entries()){
			entry.value.clear();
		}
		pools.clear();
	}
}
