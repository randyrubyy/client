package sample.message;

import java.util.ArrayList;
import java.util.List;

public class IntegerProperty extends MessageProperty<Integer> {

	public IntegerProperty(PropertyName propertyName, int byteCount) {
		super(propertyName, byteCount);
	}

	@Override
	public List<Byte> getBytes(Object o) {
		if (o instanceof Integer) {
			Integer value = (Integer) o;
			int nBytes = getByteCount();
			List<Byte> bytes = new ArrayList<Byte>();
			int v = value;
			for (int i = 0; i < nBytes; i++) {
				byte b = (byte) (v % BYTE_MAX);
				v = v / BYTE_MAX;
				bytes.add(b);
			}
			return bytes;
		} else {
			return new ArrayList<Byte>();
		}
	}

	@Override
	protected Integer getValue(byte[] bytes) {
		int value = 0;
		for (int i = bytes.length - 1; i >= 0; i--) {
			byte b = bytes[i];
			value *= BYTE_MAX;
			int v = (b + BYTE_MAX) % BYTE_MAX;
			value += v;
		}
		return value;
	}

	@Override
	public String toString() {
		return propertyName + "(" + byteCount + ")";
	}
}
