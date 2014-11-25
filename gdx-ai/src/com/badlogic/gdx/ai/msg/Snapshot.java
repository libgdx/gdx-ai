package com.badlogic.gdx.ai.msg;

/**
 * Telegrams snapshots that can be used to serialize/deserialize and reuse.
 * Created by pbrzostowski on 25.11.14.
 */
public class Snapshot {

    /** The message type. */
    public int message;

    /** Message remaining delay in seconds. */
    public float delay;

    /** Any additional information that may accompany the message */
    public Object extraInfo;

    /** Message receiver class **/
    public Class<? extends Telegraph> receiver;

    /** Message sender class **/
    public Class<? extends Telegraph> sender;

    /** Creates a {@code Snapshot}.
     * @param telegram for taking snapshot from
     * @param currentTime {@code MessageDispatcher} current time to calculate remaining delay for this snapshot
     * */
    Snapshot(Telegram telegram, float currentTime){
        message = telegram.message;
        delay = telegram.getTimestamp() - currentTime;
        extraInfo = telegram.extraInfo;
        receiver = telegram.receiver != null ? telegram.receiver.getClass() : null;
        sender = telegram.sender != null ? telegram.sender.getClass() : null;
    }

    /** prevents from creating snapshots outside package **/
    private Snapshot(){}
}
