package sample.message;

import java.util.HashSet;

import sample.message.Type.Message;

public class MessageMemory {
	private int stepCount;
	private HashSet<Message> messages;

	public MessageMemory(int stepCount) {
		this.stepCount = stepCount;
		messages = new HashSet<Message>();
	}

	public void clearOldMessages(int step) {
		HashSet<Message> copy = new HashSet<Message>(messages);
		for (Message message : copy) {
			if (message.getReceivedStep() + stepCount <= step) {
				messages.remove(message);
			}
		}
	}

	public void setMessageAsProcessed(Message message) {
		messages.add(message);
	}

	public boolean shouldProcessMessage(Message message) {
		for (Message m : messages) {
			if (message.getSender().getValue() == m.getSender().getValue()) {
				if (message.getId() == m.getId()) {
					return false;
				}
			}
		}
		return true;
	}
}
