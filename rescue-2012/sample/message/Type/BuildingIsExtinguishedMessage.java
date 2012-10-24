package sample.message.Type;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.worldmodel.EntityID;
import sample.message.IntegerProperty;
import sample.message.MessageProperty;
import sample.message.PropertyName;

public class BuildingIsExtinguishedMessage extends Message {

	public BuildingIsExtinguishedMessage() {
		super();
	}

	public BuildingIsExtinguishedMessage(EntityID buildingId, int fieryness) {
		this();
		setEntityId(PropertyName.BuildingId, buildingId);
		setInteger(PropertyName.Fieryness, fieryness);
	}

	@Override
	protected List<MessageProperty<?>> createProperties() {
		List<MessageProperty<?>> properties = new ArrayList<MessageProperty<?>>();
		properties.add(new IntegerProperty(PropertyName.BuildingId, 2));
		properties.add(new IntegerProperty(PropertyName.Fieryness, 2));
		return properties;
	}

	@Override
	public MessageType getType() {
		return MessageType.BuildingIsExtinguishedMessage;
	}

	public EntityID getBuildingId() {
		return getEntityId(PropertyName.BuildingId);
	}

	public int getFieryness() {
		return getInteger(PropertyName.Fieryness);
	}
}
