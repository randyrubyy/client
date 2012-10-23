package sample.message;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import rescuecore2.config.Config;
import sample.message.Type.BlankMessage;
import sample.message.Type.Message;
import sample.message.Type.MessageType;

//该类对信息进行转码，将String转为Byte
//同时对消息进行信道分配
public class MessageTranslator {

	// 消息重复次数，默认只发送一次
	public static final int DEFAULT_REPEAT_COUNT = 1;
	public static final int MAX_MESSAGE_ID = 255;

	private final int byteRepeatCount;
	private int nextMessageId;
	private final Config config;
	private Map<MessageType, Integer> channelMap;
	private HashMap<Integer, Integer> repeatCounts;

	/**
	 * MessageTranslator构造类
	 */
	public MessageTranslator(Config config) {
		this(config, DEFAULT_REPEAT_COUNT);
	}

	/**
	 * Construct a Message Translator
	 * 
	 * @param repeatCount
	 *            How many times the message will be repeated
	 */
	public MessageTranslator(Config config, int repeatCount) {

		this.byteRepeatCount = repeatCount;
		this.config = config;

		setChannelMap();
		setRepeatCounts();
	}

	public void sense() {
	}

	/**
	 * 将Message转为Byte数组
	 * 
	 * @param message
	 *            进行转码的消息
	 * @return 返回转码成功后的Byte Array
	 */
	public byte[] transmit(Message message) {

		byte[] bytes = message.convertToByteArray();

		if (byteRepeatCount == 1) {
			return bytes;
		}
		byte[] repeatedBytes = getRepeatedBytes(bytes, byteRepeatCount);
		return repeatedBytes;
	}

	public byte[] transmit(byte[] message) {
		return message;
	}

	/**
	 * Translates the byte array to Message object
	 * 
	 * @param bytes
	 *            Byte array message.
	 * @return Message objects of the given byte array.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Message receive(byte[] bytes) throws IOException,
			ClassNotFoundException {
		if (bytes == null || bytes.length == 0) {
			// LOG.warn("Received empty message");
			return new BlankMessage();
		}
		bytes = getNonRepeatedBytes(bytes, byteRepeatCount);
		// logger.info("received bytes:" + bytes.length);
		if (!Message.isValid(bytes)) {
			// LOG.warn("Received corrupted message");
			return new BlankMessage();
		}
		Message message = Message.getMessage(bytes);
		return message;
	}

	/**
	 * Returns the single message from repeated message array.
	 * 
	 * @param msg
	 *            Byte array which contains repeated messages.
	 * @return Returns the extracted byte array.
	 */
	public byte[] getNonRepeatedBytes(byte[] msg) {
		return getNonRepeatedBytes(msg, byteRepeatCount);
	}

	public static byte[] getNonRepeatedBytes(byte[] bytes, int repeatCount) {
		if (repeatCount == 1) {
			return bytes;
		}
		byte[] realMessage = new byte[bytes.length / repeatCount];
		for (int i = 0; i < realMessage.length; i++) {
			byte[] temp = new byte[repeatCount];
			for (int j = 0; j < temp.length; j++) {
				int k = i + j * realMessage.length;
				temp[j] = bytes[k];
			}
			realMessage[i] = getCorrectByte(temp);
		}
		return realMessage;
	}

	public static byte getCorrectByte(byte[] bytes) {
		int BYTE_SIZE = 8;
		byte b = 0;
		int base = 1;
		for (int i = 0; i < BYTE_SIZE; i++) {
			int value = 0;
			for (int j = 0; j < bytes.length; j++) {
				if (bytes[j] % 2 != 0) {
					value++;
				}
				bytes[j] = (byte) (bytes[j] >>> 1);
			}
			if (value > bytes.length / 2) {
				b += base;
			}
			base *= 2;
		}
		return b;
	}

	protected static byte[] getRepeatedBytes(byte[] bytes, int repeatCount) {
		byte[] repeatedBytes = new byte[bytes.length * repeatCount];
		for (int i = 0; i < repeatCount; i++) {
			for (int j = 0; j < bytes.length; j++) {
				int k = i * bytes.length + j;
				repeatedBytes[k] = bytes[j];
			}
		}
		return repeatedBytes;
	}

	protected byte[] getRepeatedBytes(byte[] bytes) {
		return getRepeatedBytes(bytes, byteRepeatCount);
	}

	public static final String COMMUNICATIONS_KEY = "comms";
	public static final String CHANNELS_KEY = "channels";
	public static final String NOISE_KEY = "noise";
	public static final String USE_KEY = "use";
	public static final String PROBABILITY_KEY = "p";
	public static final String COUNT_KEY = "count";

	private int getChannelCount() {
		String key = COMMUNICATIONS_KEY + "." + CHANNELS_KEY + "." + COUNT_KEY;
		int n = config.getIntValue(key);
		return n;
	}

	protected double getNoise(int channel, String ioType, String noiseType) {
		String key = COMMUNICATIONS_KEY + "." + CHANNELS_KEY + "." + channel
				+ "." + NOISE_KEY + "." + ioType + "." + noiseType;
		String useKey = key + "." + USE_KEY;
		try {
			boolean hasNoise = config.getBooleanValue(useKey);
			if (hasNoise) {
				String probabilityKey = key + "." + PROBABILITY_KEY;
				double p = config.getFloatValue(probabilityKey);
				return p;
			}
		} catch (Exception e) {

		}
		return 0.0;
	}

	public static final String INPUT_KEY = "input";
	public static final String OUTPUT_KEY = "output";
	public static final String DROPOUT_KEY = "dropout";
	public static final String FAILURE_KEY = "failure";

	protected double getTotalNoise(int channel) {
		String[] ioTypes = { INPUT_KEY, OUTPUT_KEY };
		String[] noiseTypes = { DROPOUT_KEY, FAILURE_KEY };
		double sum = 0;
		for (String ioType : ioTypes) {
			for (String noiseType : noiseTypes) {
				double p = getNoise(channel, ioType, noiseType);
				sum += p;
			}
		}
		return sum;
	}

	public int getRepeatCount(int channel) {
		return repeatCounts.get(channel);
	}

	public static final double CONFIDENCE = 0.999;

	private int calculateRepeatCount(double probability) {
		if (probability == 0) {
			return 1;
		}
		double top = Math.log(1 - CONFIDENCE);
		double bottom = Math.log(probability);
		return (int) Math.ceil(top / bottom);
	}

	private void setRepeatCounts() {
		repeatCounts = new HashMap<Integer, Integer>();
		int n = getChannelCount();
		for (int i = 0; i < n; i++) {
			double p = getTotalNoise(i);
			int repeat = calculateRepeatCount(p);
			repeatCounts.put(i, repeat);
		}
	}

	public void incrementMessageId() {
		nextMessageId = (nextMessageId + 1) % (MAX_MESSAGE_ID + 1);
	}

	public int getNextMessageId() {
		return nextMessageId;
	}

	private void setChannelMap() {
		int n;
		Map<MessageType, Integer> map;

		// Channel数量
		n = getChannelCount();

		map = new HashMap<MessageType, Integer>();

		if (n == 2) {
			map.put(MessageType.BuildingIsExploredMessage, 1);
			map.put(MessageType.BuildingIsBurningMessage, 1);
			map.put(MessageType.BuildingIsExtinguishedMessage, 1);
			map.put(MessageType.CivilianInformationMessage, 1);
			map.put(MessageType.AgentIsBuriedMessage, 1);
			map.put(MessageType.CivilianIsSavedOrDeadMessage, 1);
			map.put(MessageType.ClearPathIsNeededMessage, 1);
			map.put(MessageType.AgentIsStuckMessage, 1);
			map.put(MessageType.AssignRefugeMessage, 1);
			map.put(MessageType.RoadIsClearedMessage, 1);
			map.put(MessageType.AssignFiredBuildingMessage, 1);
			map.put(MessageType.InformTeamMessage, 1);
		} else if (n == 3) {
			map.put(MessageType.BuildingIsExploredMessage, 1);
			map.put(MessageType.BuildingIsBurningMessage, 2);
			map.put(MessageType.BuildingIsExtinguishedMessage, 2);
			map.put(MessageType.CivilianInformationMessage, 1);
			map.put(MessageType.AgentIsBuriedMessage, 1);
			map.put(MessageType.CivilianIsSavedOrDeadMessage, 1);
			map.put(MessageType.ClearPathIsNeededMessage, 2);
			map.put(MessageType.AgentIsStuckMessage, 2);
			map.put(MessageType.AssignRefugeMessage, 2);
			map.put(MessageType.RoadIsClearedMessage, 2);
			map.put(MessageType.AssignFiredBuildingMessage, 2);
			map.put(MessageType.InformTeamMessage, 2);
		} else if (n == 4) {
			map.put(MessageType.BuildingIsExploredMessage, 1);
			map.put(MessageType.BuildingIsBurningMessage, 2);
			map.put(MessageType.BuildingIsExtinguishedMessage, 2);
			map.put(MessageType.CivilianInformationMessage, 1);
			map.put(MessageType.AgentIsBuriedMessage, 1);
			map.put(MessageType.CivilianIsSavedOrDeadMessage, 1);
			map.put(MessageType.ClearPathIsNeededMessage, 3);
			map.put(MessageType.AgentIsStuckMessage, 3);
			map.put(MessageType.AssignRefugeMessage, 3);
			map.put(MessageType.RoadIsClearedMessage, 3);
			map.put(MessageType.AssignFiredBuildingMessage, 3);
			map.put(MessageType.InformTeamMessage, 2);

		} else if (n >= 5) {
			map.put(MessageType.BuildingIsExploredMessage, 1);
			map.put(MessageType.BuildingIsBurningMessage, 2);
			map.put(MessageType.BuildingIsExtinguishedMessage, 2);
			map.put(MessageType.CivilianInformationMessage, 3);
			map.put(MessageType.AgentIsBuriedMessage, 3);
			map.put(MessageType.CivilianIsSavedOrDeadMessage, 3);
			map.put(MessageType.ClearPathIsNeededMessage, 4);
			map.put(MessageType.AgentIsStuckMessage, 4);
			map.put(MessageType.AssignRefugeMessage, 4);
			map.put(MessageType.RoadIsClearedMessage, 4);
			map.put(MessageType.AssignFiredBuildingMessage, 4);
			map.put(MessageType.InformTeamMessage, 2);

		} else {
			map.put(MessageType.BuildingIsExploredMessage, 0);
			map.put(MessageType.BuildingIsBurningMessage, 0);
			map.put(MessageType.BuildingIsExtinguishedMessage, 0);
			map.put(MessageType.CivilianInformationMessage, 0);
			map.put(MessageType.AgentIsBuriedMessage, 0);
			map.put(MessageType.CivilianIsSavedOrDeadMessage, 0);
			map.put(MessageType.ClearPathIsNeededMessage, 0);
			map.put(MessageType.AgentIsStuckMessage, 0);
			map.put(MessageType.AssignRefugeMessage, 0);
			map.put(MessageType.RoadIsClearedMessage, 0);
			map.put(MessageType.AssignFiredBuildingMessage, 0);
			map.put(MessageType.InformTeamMessage, 0);

		}
		channelMap = map;
	}

	public int getChannel(MessageType messageType) {
		Integer no;

		no = channelMap.get(messageType);
		if (no == null) {
			return 1;
		} else {
			return no;
		}
	}

}
