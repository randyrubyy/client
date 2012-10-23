package sample.message.Type;

import java.util.ArrayList;
import java.util.List;

import sample.message.IntegerProperty;
import sample.message.MessageProperty;
import sample.message.PropertyName;

public class MessageCount extends Message {

	@Override
	protected List<MessageProperty<?>> createProperties() {
		List<MessageProperty<?>> list = new ArrayList<MessageProperty<?>>();
		list.add(new IntegerProperty(PropertyName.Counter, 1));
		return list;
	}

	@Override
	public MessageType getType() {
		return MessageType.CountMessage;
	}

	public MessageCount() {
		super();
	}

	public MessageCount(int counter) {
		this();
		setInteger(PropertyName.Counter, counter);
	}

	public int getCounter() {
		return getInteger(PropertyName.Counter);
	}

	@Override
	public String toString() {
		return "Count(" + getCounter() + ")";
	}
}
