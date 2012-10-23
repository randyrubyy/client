package sample.message.Type;

import java.util.List;

import sample.message.IntegerProperty;
import sample.message.PropertyName;

public enum MessageType {
	BlankMessage(BlankMessage.class), CivilianInformationMessage(
			CivilianInformationMessage.class), RegionAssignmentMessage(
			PartitionAssignmentMessage.class), CountMessage(MessageCount.class), BuildingIsExploredMessage(
			BuildingIsExploredMessage.class), RegionResignMessage(
			RegionResignMessage.class), InformTeamMessage(
			InformTeamMessage.class), ClearPathIsNeededMessage(
			ClearPathIsNeededMessage.class), BuildingIsBurningMessage(
			BuildingIsBurningMessage.class), ExtinguishFireTaskMessage(
			ExtinguishFireTaskMessage.class), InformTeamSavingCivilianMessage(
			InformTeamSavingCivilianMessage.class), InformTeamExtinguishingFireMessage(
			InformTeamExtinguishingFireMessage.class), AgentIsStuckMessage(
			AgentIsStuckMessage.class), BuildingIsExtinguishedMessage(
			BuildingIsExtinguishedMessage.class), GetTaskAssignmentMessage(
			GetTaskAssignmentMessage.class), ExtinguishHelpMessage(
			ExtinguishHelpMessage.class), CivilianIsSavedOrDeadMessage(
			CivilianIsSavedOrDeadMessage.class), AgentIsBuriedMessage(
			AgentIsBuriedMessage.class), AssignRefugeMessage(
			AssignRefugeMessage.class), RoadIsClearedMessage(
			RoadIsClearedMessage.class), AssignFiredBuildingMessage(
			AssignFiredBuildingMessage.class), ;

	private Class<? extends Message> messageClass;

	private static int getRequiredByteCount() {
		return 1;
	}

	private MessageType(Class<? extends Message> messageClass) {
		this.messageClass = messageClass;
	}

	public Class<? extends Message> getMessageClass() {
		return messageClass;
	}

	/**
	 * Returns a new message instance of associated type
	 * 
	 * @return message instance
	 */
	public Message newInstance() {
		Message message = null;
		try {
			message = messageClass.newInstance();
		} catch (Exception ex) {
			// LOG.fatal(message, ex);
		}
		return message;
	}

	/**
	 * Returns the messageType instance associated with the given class
	 * 
	 * @param messageClass
	 *            the class of message
	 * @return the message type
	 */
	public static MessageType getMessageType(Class<Message> messageClass) {
		for (MessageType type : MessageType.values()) {
			if (type.getMessageClass() == messageClass) {
				return type;
			}
		}
		return null;
	}

	public List<Byte> getBytes() {
		int n = ordinal();
		IntegerProperty property = getProperty();
		List<Byte> bytes = property.getBytes(n);
		return bytes;
	}

	public static MessageType getMessageType(byte[] bytes) {
		IntegerProperty property = getProperty();
		int index = property.getValue(bytes, 0);
		MessageType type;

		if (index < values().length) {
			type = values()[index];
		} else {
			type = null;
		}
		return type;
	}

	public static IntegerProperty getProperty() {
		int byteCount = getRequiredByteCount();
		IntegerProperty property = new IntegerProperty(
				PropertyName.MessageType, byteCount);
		return property;
	}
}
