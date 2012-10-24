package sample.message.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rescuecore2.worldmodel.EntityID;
import sample.message.IntegerProperty;
import sample.message.MessageProperty;
import sample.message.PropertyName;

/**
 * This class holds the messages
 */
public abstract class Message {

	protected Map<PropertyName, Object> values;
	private List<MessageProperty<?>> properties;
	private EntityID sender;
	private int receivedStep;
	public static final boolean CHECK_SUM_ENABLED = false;

	protected abstract List<MessageProperty<?>> createProperties();

	protected Message() {
		initialize();
	}

	protected void initialize() {
		properties = new ArrayList<MessageProperty<?>>();
		values = new HashMap<PropertyName, Object>();
		addInitialValues();
	}

	protected void addInitialValues() {
		properties.add(MessageType.getProperty());
		properties.add(new IntegerProperty(PropertyName.MessageId, 1));
		List<MessageProperty<?>> properties = createProperties();
		this.properties.addAll(properties);
		MessageType type = getType();
		setInteger(PropertyName.MessageType, type.ordinal());
		setId(0);
	}

	protected List<MessageProperty<?>> getProperties() {
		return properties;
	}

	public abstract MessageType getType();

	protected int getInteger(PropertyName name) {
		if (values.containsKey(name)) {
			Integer i = (Integer) values.get(name);
			return i;
		} else {
			return 0;
		}
	}

	protected EntityID getEntityId(PropertyName name) {
		int value = getInteger(name);
		EntityID id = new EntityID(value);
		return id;
	}

	protected void setInteger(PropertyName name, int value) {
		values.put(name, value);
	}

	protected void setEntityId(PropertyName name, EntityID id) {
		setInteger(name, id.getValue());
	}

	@SuppressWarnings("rawtypes")
	protected void setEnum(PropertyName name, Enum value) {
		values.put(name, value.ordinal());
	}

	public void setId(int value) {
		setInteger(PropertyName.MessageId, value);
	}

	public int getId() {
		return getInteger(PropertyName.MessageId);
	}

	public <T> T getEnum(PropertyName name, T[] values) {
		int n;

		n = getInteger(name);
		if (n < values.length) {
			return values[n];
		} else {
			return null;
		}
	}

	public byte[] convertToByteArray() {
		List<Byte> bytes = new ArrayList<Byte>();
		for (MessageProperty<?> property : properties) {
			PropertyName key = property.getPropertyName();
			Object value = values.get(key);
			List<Byte> tempBytes = property.getBytes(value);
			bytes.addAll(tempBytes);
		}
		byte[] byteArray;
		if (CHECK_SUM_ENABLED) {
			byteArray = new byte[bytes.size() + 1];
		} else {
			byteArray = new byte[bytes.size()];
		}
		byte sum = 0;
		for (int i = 0; i < bytes.size(); i++) {
			byteArray[i] = bytes.get(i);
			sum += byteArray[i];
		}
		if (CHECK_SUM_ENABLED) {
			byteArray[byteArray.length - 1] = sum;
		}
		return byteArray;
	}

	public static Message getMessage(byte[] bytes) {
		MessageType type = MessageType.getMessageType(bytes);
		Message message;

		if (type == null) {
			message = new BlankMessage();
		} else {
			message = type.newInstance();

			int currentIndex = 0;
			for (MessageProperty<?> property : message.properties) {
				Object value = property.getValue(bytes, currentIndex);
				message.values.put(property.getPropertyName(), value);
				currentIndex += property.getByteCount();
			}
		}
		return message;
	}

	public static boolean isValid(byte[] bytes) {
		if (CHECK_SUM_ENABLED) {
			byte sum = 0;
			for (int i = 0; i < bytes.length - 1; i++) {
				sum += bytes[i];
			}
			return bytes[bytes.length - 1] == sum;
		} else {
			return true;
		}
	}

	/**
	 * Explains the messase as string.
	 * 
	 * @return Returns the information about the message.
	 */
	@Override
	public String toString() {
		return getType().toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Message other = (Message) obj;
		if (this.values != other.values
				&& (this.values == null || !this.values.equals(other.values))) {
			return false;
		}
		if (this.properties != other.properties
				&& (this.properties == null || !this.properties
						.equals(other.properties))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 59 * hash + (this.values != null ? this.values.hashCode() : 0);
		hash = 59 * hash
				+ (this.properties != null ? this.properties.hashCode() : 0);
		return hash;
	}

	public EntityID getSender() {
		return sender;
	}

	public void setSender(EntityID sender) {
		this.sender = sender;
	}

	public int getReceivedStep() {
		return receivedStep;
	}

	public void setReceivedStep(int receivedStep) {
		this.receivedStep = receivedStep;
	}

}
