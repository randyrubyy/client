package sample.message;

import java.util.List;

public abstract class MessageProperty<T extends Object> {
	public static final int BYTE_MAX = 256;

	protected PropertyName propertyName;
	protected int byteCount;

	public MessageProperty(PropertyName propertyName, int byteCount) {
		this.propertyName = propertyName;
		this.byteCount = byteCount;
	}

	public int getByteCount() {
		return byteCount;
	}

	public PropertyName getPropertyName() {
		return propertyName;
	}

	public abstract List<Byte> getBytes(Object value);

	protected abstract T getValue(byte[] bytes);

	public T getValue(byte[] bytes, int firstIndex) {
		byte[] tempBytes = new byte[byteCount];
		for (int i = 0; i < byteCount; i++) {
			tempBytes[i] = bytes[i + firstIndex];
		}
		return getValue(tempBytes);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final MessageProperty<T> other = (MessageProperty<T>) obj;
		if (this.propertyName != other.propertyName) {
			return false;
		}
		if (this.byteCount != other.byteCount) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		return hash;
	}

}
