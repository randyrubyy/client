package sample.message.Type;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.worldmodel.EntityID;
import sample.message.IntegerProperty;
import sample.message.MessageProperty;
import sample.message.PropertyName;

public class BuildingIsBurningMessage extends Message {

	public BuildingIsBurningMessage() {
		super();
	}

	public BuildingIsBurningMessage(EntityID buildingId, int fieryness,
			int temperature) {
		this();
		setEntityId(PropertyName.BuildingId, buildingId);
		setInteger(PropertyName.Fieryness, fieryness);
		setInteger(PropertyName.Temperature, temperature);
	}

	@Override
	protected List<MessageProperty<?>> createProperties() {
		List<MessageProperty<?>> properties = new ArrayList<MessageProperty<?>>();
		properties.add(new IntegerProperty(PropertyName.BuildingId, 2));
		properties.add(new IntegerProperty(PropertyName.Fieryness, 2));
		properties.add(new IntegerProperty(PropertyName.Temperature, 2));
		return properties;
	}

	@Override
	public MessageType getType() {
		return MessageType.BuildingIsBurningMessage;
	}

	public EntityID getBuildingId() {
		return getEntityId(PropertyName.BuildingId);
	}

	public int getFieryness() {
		return getInteger(PropertyName.Fieryness);
	}

	public int getTemperature() {
		return getInteger(PropertyName.Temperature);
	}
}
