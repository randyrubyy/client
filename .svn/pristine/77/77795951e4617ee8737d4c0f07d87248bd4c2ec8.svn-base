package sample.message.Type;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.worldmodel.EntityID;
import sample.message.IntegerProperty;
import sample.message.MessageProperty;
import sample.message.PropertyName;

public class AgentIsStuckMessage extends Message {

	public AgentIsStuckMessage() {
		super();
	}

	public AgentIsStuckMessage(EntityID locationId) {
		this();
		setEntityId(PropertyName.LocationId, locationId);
	}

	@Override
	protected List<MessageProperty<?>> createProperties() {
		List<MessageProperty<?>> properties = new ArrayList<MessageProperty<?>>();
		properties.add(new IntegerProperty(PropertyName.LocationId, 2));
		return properties;
	}

	@Override
	public MessageType getType() {
		return MessageType.AgentIsStuckMessage;
	}

	public EntityID getLocationId() {
		return getEntityId(PropertyName.LocationId);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString() + getLocationId().getValue();
	}

}
