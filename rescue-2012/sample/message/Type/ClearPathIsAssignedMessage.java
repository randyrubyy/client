package sample.message.Type;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.worldmodel.EntityID;
import sample.message.IntegerProperty;
import sample.message.MessageProperty;
import sample.message.PropertyName;

public class ClearPathIsAssignedMessage extends Message {

	public ClearPathIsAssignedMessage() {
		super();
	}

	public ClearPathIsAssignedMessage(EntityID startId, EntityID finishId,
			EntityID agentId) {
		this();
		setEntityId(PropertyName.ClearPathStartId, startId);
		setEntityId(PropertyName.ClearPathFinishId, finishId);
		setEntityId(PropertyName.AgentId, agentId);
	}

	@Override
	protected List<MessageProperty<?>> createProperties() {
		List<MessageProperty<?>> properties = new ArrayList<MessageProperty<?>>();
		properties.add(new IntegerProperty(PropertyName.ClearPathStartId, 2));
		properties.add(new IntegerProperty(PropertyName.ClearPathFinishId, 2));
		properties.add(new IntegerProperty(PropertyName.AgentId, 2));
		return properties;
	}

	@Override
	public MessageType getType() {
		return MessageType.ClearPathIsNeededMessage;
	}

	public EntityID getStartId() {
		return getEntityId(PropertyName.ClearPathStartId);
	}

	public EntityID getFinishId() {
		return getEntityId(PropertyName.ClearPathFinishId);
	}

	public EntityID getAgentId() {
		return getEntityId(PropertyName.ClearPathFinishId);
	}

	@Override
	public String toString() {
		return getType() + "(" + getStartId().getValue() + ", "
				+ getFinishId().getValue() + ")";
	}

}
