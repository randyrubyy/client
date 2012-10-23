package sample.message.Type;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.worldmodel.EntityID;
import sample.message.IntegerProperty;
import sample.message.MessageProperty;
import sample.message.PropertyName;

public class ClearPathIsNeededMessage extends Message {

	public ClearPathIsNeededMessage() {
		super();
	}

	public ClearPathIsNeededMessage(EntityID startId, EntityID finishId,
			ClearPathReason clearPathReson) {
		this();
		setEntityId(PropertyName.ClearPathStartId, startId);
		setEntityId(PropertyName.ClearPathFinishId, finishId);
		setEnum(PropertyName.ClearPathReason, clearPathReson);
	}

	@Override
	protected List<MessageProperty<?>> createProperties() {
		List<MessageProperty<?>> properties = new ArrayList<MessageProperty<?>>();
		properties.add(new IntegerProperty(PropertyName.ClearPathStartId, 2));
		properties.add(new IntegerProperty(PropertyName.ClearPathFinishId, 2));
		properties.add(new IntegerProperty(PropertyName.ClearPathReason, 2));
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

	@Override
	public String toString() {
		return getType() + "(" + getStartId().getValue() + ", "
				+ getFinishId().getValue() + ")";
	}

	public ClearPathReason getClearPathReason() {
		return getEnum(PropertyName.ClearPathReason, ClearPathReason.values());
	}

}
