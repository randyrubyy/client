package sample.message.Type;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.worldmodel.EntityID;
import sample.message.IntegerProperty;
import sample.message.MessageProperty;
import sample.message.PropertyName;

public class InformTeamMessage extends Message {

	public InformTeamMessage() {
		super();
	}

	public InformTeamMessage(EntityID agentId, EntityID targetRefugeId,
			int buriedness) {
		this();
		setEntityId(PropertyName.AgentId, agentId);
		setEntityId(PropertyName.LocationId, targetRefugeId);
		setInteger(PropertyName.Buriedness, buriedness);
		// setEnum(PropertyName.ClearPathReason, clearPathReson);
	}

	@Override
	protected List<MessageProperty<?>> createProperties() {
		List<MessageProperty<?>> properties = new ArrayList<MessageProperty<?>>();
		properties.add(new IntegerProperty(PropertyName.AgentId, 2));
		properties.add(new IntegerProperty(PropertyName.LocationId, 2));
		properties.add(new IntegerProperty(PropertyName.Buriedness, 2));
		// properties.add(new IntegerProperty(PropertyName.ClearPathReason, 2));
		return properties;
	}

	@Override
	public MessageType getType() {
		return MessageType.InformTeamMessage;
	}

	public EntityID getAgentId() {
		return getEntityId(PropertyName.AgentId);
	}

	public EntityID getLocationId() {
		return getEntityId(PropertyName.LocationId);
	}

	public int getBuriedness() {
		return getInteger(PropertyName.Buriedness);
	}

	@Override
	public String toString() {
		return getType() + "(" + getAgentId().getValue() + ", "
				+ getLocationId().getValue() + "," + getBuriedness() + ")";
	}

}
