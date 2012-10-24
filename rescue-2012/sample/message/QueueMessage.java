package sample.message;

import sample.message.Type.Message;

public class QueueMessage {
	public Message message;
	public int channel;
	public MessagePriority importance;

	/**
	 * Construct a QueueMessage object
	 * 
	 * @param Time
	 *            Simulation time
	 * @param Channel
	 *            Message channel
	 * @param Msg
	 *            Message
	 * @param LocationId
	 *            Send message from which location
	 * @param Importance
	 *            Importance level of the message
	 */
	protected QueueMessage(int Channel, Message msg, MessagePriority Importance) {
		message = msg;
		channel = Channel;
		importance = Importance;
	}
}
