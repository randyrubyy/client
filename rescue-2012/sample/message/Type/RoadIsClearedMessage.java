package sample.message.Type;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.worldmodel.EntityID;
import sample.message.IntegerProperty;
import sample.message.MessageProperty;
import sample.message.PropertyName;

public class RoadIsClearedMessage extends Message {

	public RoadIsClearedMessage() {
		super();
	}

	public RoadIsClearedMessage(EntityID road) {
		this();
		setEntityId(PropertyName.LocationId, road);
		// setEnum(PropertyName.ClearPathReason, clearPathReson);
	}

	@Override
	protected List<MessageProperty<?>> createProperties() {
		List<MessageProperty<?>> properties = new ArrayList<MessageProperty<?>>();
		properties.add(new IntegerProperty(PropertyName.LocationId, 2));
		// properties.add(new IntegerProperty(PropertyName.ClearPathReason, 2));
		return properties;
	}

	@Override
	public MessageType getType() {
		return MessageType.RoadIsClearedMessage;
	}

	public EntityID getRoadId() {
		return getEntityId(PropertyName.LocationId);
	}

	@Override
	public String toString() {
		return getType() + getRoadId().toString();
	}

}
