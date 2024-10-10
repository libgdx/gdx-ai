package com.badlogic.gdx.ai.utility;

/** This is the abstract base class of all UtiltiyAI Actions tasks. The {@code Action} a status
 *
 * @param <E> type of the blackboard object that tasks use to read or modify game state
 *
 * @author fvolz*/
public abstract class Action<E> {

    /** The enumeration of the values that a task's status can have.
     *
     * @author davebaol */
    public enum Status {
        /** Means that the task has never run or has been reset. */
        FRESH,
        /** Means that the task needs to run again. */
        RUNNING,
        /** Means that the task returned a failure result. */
        FAILED,
        /** Means that the task returned a success result. */
        SUCCEEDED,
        /** Means that the task has been terminated by an ancestor. */
        CANCELLED;
    }

    /** The status of this task. */
    protected Status status = Status.FRESH;

    float cooldown;
    float startedTime;
    String name;

    public Action(String name, float cooldown) {
        this.status = Status.FRESH;
        setCooldown(cooldown);
        this.startedTime = 0; //start in idle mode
        this.name = name;
    }

    public void execute(E context) {
        if(canExecute() == false)
            return;

        if(doTryUpdate(context) == false) {
            startedTime = System.currentTimeMillis();
            status = Status.RUNNING;
            onStart(context);
        }
    }

    public boolean doTryUpdate(E context) {
        if(status.equals(Status.RUNNING)) {
            onUpdate(context);
            return true;
        }
        return false;
    }

    protected void endInSuccess(E context) {
        if(!status.equals(Status.RUNNING))
            return;

        status = Status.SUCCEEDED;
        finalizeAction(context);
    }

    protected void onStart(E context) {
        endInSuccess(context);
    }

    protected abstract void onUpdate(E context) ;

    protected abstract void onStop(E context);

    public boolean canExecute() {
        if(isInCooldown()) {
            status = Status.FAILED;
            return false;
        }

        return true;
    }

    void finalizeAction(E context) {
        onStop(context);
    }

    /// <summary>
    ///   Ends the action and sets its status to <see cref="F:Crystal.ActionStatus.Failure"/>.
    /// </summary>
    /// <param name="context">The context.</param>
    protected void endInFailure(E context) {
        if(status != Status.RUNNING)
            return;

        status = Status.FAILED;
        finalizeAction(context);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isInCooldown() {

            if(status.equals(Status.RUNNING) ||
                    status.equals(Status.FRESH))
                return false;

        return System.currentTimeMillis() - startedTime < cooldown;
    }

    public float getCooldown() {
        return cooldown;
    }

    public void setCooldown(float cooldown) {
        this.cooldown = Math.max(0, cooldown);
    }

    public float getStartedTime() {
        return startedTime;
    }

    public void setStartedTime(float startedTime) {
        this.startedTime = startedTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
