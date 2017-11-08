package com.badlogic.gdx.ai.utility;

import com.badlogic.gdx.ai.GdxAI;

/**
 * Created by felix on 8/7/2017.
 */
public class DecisionMaker<E> {

    enum State{
        STOPPED,
        RUNNING,
        PAUSED;
    }

    E currentContext;
    Action currentAction;
    State currentState;
    UtilityAI<E> ai;
    int recursionCounter;
    public static final int MaxRecursions = 100;


    //TODO review passing Dog into dcision maker
    public DecisionMaker(UtilityAI<E> ai, E currentContext){
        this.ai = ai;
        this.currentContext = currentContext;
    }


    public void think() {
        if(isActionStillRunning())
            return;

        if(couldNotUpdateContext())
            return;

        if(AiDidSelectAction()) {
            while(isTransition())
                connectorSelectAction();

            executeCurrentAction();
        }
    }

    public void update() {
        if(couldNotUpdateContext())
            return;

        executeCurrentAction();
    }

    public boolean isActionStillRunning() {
        if(null == currentAction) return false;

        return Action.Status.RUNNING.equals(currentAction.getStatus());
    }

    public boolean couldNotUpdateContext() {
        recursionCounter = 0;
        //currentContext = _contextProvider.Context();
        return currentContext == null;
    }

    public boolean AiDidSelectAction() {
        currentAction = ai.select(currentContext);
        return currentAction != null;
    }

    public boolean isTransition() {
        checkForRecursions();
        return false;
//        _transitionAction = _currentAction as ITransition;
//        return _transitionAction != null;
    }

    void checkForRecursions() {
        recursionCounter++;
        if(recursionCounter >= MaxRecursions)
             GdxAI.getLogger().error("DecisionMaker","Circular Dependency on DecisionMaker " + recursionCounter);
            ;
            //throw new Exception("Circular Dependency on DecisionMaker?? " + recursionCounter);
    }

    public void connectorSelectAction() {
       // currentAction = _transitionAction.Select(currentContext);
    }

    void executeCurrentAction() {
        if(currentAction == null)
            return;

        currentAction.execute(currentContext);
        if(!Action.Status.RUNNING.equals(currentAction.getStatus()))
            currentAction = null;
    }

//    public void start() {
//        if(!State.STOPPED.equals(currentState))
//            return;
//
//        currentState = State.RUNNING;
//        //OnStart();
//    }
//
//    public void stop() {
//        if(State.STOPPED.equals(currentState))
//            return;
//
//        currentState = State.STOPPED;
//       // OnStop();
//    }
//
//    public void pause() {
//        if(!State.RUNNING.equals(currentState))
//            return;
//
//        currentState = State.PAUSED;
//        //OnPause();
//    }
//
//    public void resume() {
//        if(!State.PAUSED.equals(currentState))
//            return;
//
//        currentState = State.RUNNING;
//       // OnResume();
//    }
}
