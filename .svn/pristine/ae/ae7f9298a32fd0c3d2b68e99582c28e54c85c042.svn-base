package sample.message.Type;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.worldmodel.EntityID;
import sample.message.IntegerProperty;
import sample.message.MessageProperty;
import sample.message.PropertyName;

public class InformTeamExtinguishingFireMessage extends Message {

	public InformTeamExtinguishingFireMessage() {
		super();
	}

	public InformTeamExtinguishingFireMessage(EntityID buildingId,
			int fieryness, int timeStep) {
		this();
		setEntityId(PropertyName.BuildingId, buildingId);
		setInteger(PropertyName.Fieryness, fieryness);
		setInteger(PropertyName.TimeStep, timeStep);
	}

	@Override
	protected List<MessageProperty<?>> createProperties() {
		List<MessageProperty<?>> properties = new ArrayList<MessageProperty<?>>();
		properties.add(new IntegerProperty(PropertyName.BuildingId, 2));
		properties.add(new IntegerProperty(PropertyName.Fieryness, 2));
		properties.add(new IntegerProperty(PropertyName.TimeStep, 2));
		return properties;
	}

	@Override
	public MessageType getType() {
		return MessageType.InformTeamExtinguishingFireMessage;
	}

	public EntityID getBuildingId() {
		return getEntityId(PropertyName.BuildingId);
	}

	public int getFieryness() {
		return getInteger(PropertyName.Fieryness);
	}

	public int getTimeStep() {
		return getInteger(PropertyName.TimeStep);
	}
}
