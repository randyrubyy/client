package sample.message.Type;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.worldmodel.EntityID;
import sample.message.IntegerProperty;
import sample.message.MessageProperty;
import sample.message.PropertyName;

public class AssignFiredBuildingMessage extends Message {

	public AssignFiredBuildingMessage() {
		super();
	}

	public AssignFiredBuildingMessage(EntityID agentId,
			EntityID targetBuildingId) {
		this();
		setEntityId(PropertyName.AgentId, agentId);
		setEntityId(PropertyName.PartitionId, targetBuildingId);
		// setEnum(PropertyName.ClearPathReason, clearPathReson);
	}

	@Override
	protected List<MessageProperty<?>> createProperties() {
		List<MessageProperty<?>> properties = new ArrayList<MessageProperty<?>>();
		properties.add(new IntegerProperty(PropertyName.AgentId, 2));
		properties.add(new IntegerProperty(PropertyName.PartitionId, 2));
		// properties.add(new IntegerProperty(PropertyName.ClearPathReason, 2));
		return properties;
	}

	@Override
	public MessageType getType() {
		return MessageType.AssignFiredBuildingMessage;
	}

	public EntityID getAgentId() {
		return getEntityId(PropertyName.AgentId);
	}

	public EntityID getBuildingId() {
		return getEntityId(PropertyName.PartitionId);
	}

	@Override
	public String toString() {
		return getType() + "(" + getAgentId().getValue() + ", "
				+ getBuildingId().getValue() + ")";
	}

}
