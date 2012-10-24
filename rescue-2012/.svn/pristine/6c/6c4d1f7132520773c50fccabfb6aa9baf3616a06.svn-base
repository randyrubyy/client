package sample.message.Type;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.worldmodel.EntityID;
import sample.message.IntegerProperty;
import sample.message.MessageProperty;
import sample.message.PropertyName;

public class CivilianIsSavedOrDeadMessage extends Message {

	public CivilianIsSavedOrDeadMessage() {
		super();
	}

	public CivilianIsSavedOrDeadMessage(EntityID civilianId) {
		this();
		setEntityId(PropertyName.CivilianId, civilianId);
	}

	@Override
	protected List<MessageProperty<?>> createProperties() {
		List<MessageProperty<?>> properties = new ArrayList<MessageProperty<?>>();
		properties.add(new IntegerProperty(PropertyName.CivilianId, 2));
		return properties;
	}

	@Override
	public MessageType getType() {
		return MessageType.CivilianIsSavedOrDeadMessage;
	}

	public EntityID getCivilianId() {
		return getEntityId(PropertyName.CivilianId);
	}
}
