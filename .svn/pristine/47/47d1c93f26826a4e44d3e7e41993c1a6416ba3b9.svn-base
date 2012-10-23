package sample.message.Type;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.worldmodel.EntityID;
import sample.message.IntegerProperty;
import sample.message.MessageProperty;
import sample.message.PropertyName;

public class InformTeamSavingCivilianMessage extends Message {

	public InformTeamSavingCivilianMessage() {
		super();
	}

	public InformTeamSavingCivilianMessage(int buildingId, int civilianId,
			int buriedness, int timeStep) {
		this();
		setInteger(PropertyName.BuildingId, buildingId);
		setInteger(PropertyName.CivilianId, civilianId);
		setInteger(PropertyName.Buriedness, buriedness);
		setInteger(PropertyName.TimeStep, timeStep);
	}

	@Override
	protected List<MessageProperty<?>> createProperties() {
		List<MessageProperty<?>> properties = new ArrayList<MessageProperty<?>>();
		properties.add(new IntegerProperty(PropertyName.BuildingId, 2));
		properties.add(new IntegerProperty(PropertyName.CivilianId, 2));
		properties.add(new IntegerProperty(PropertyName.Buriedness, 2));
		properties.add(new IntegerProperty(PropertyName.TimeStep, 2));
		return properties;
	}

	@Override
	public MessageType getType() {
		return MessageType.InformTeamSavingCivilianMessage;
	}

	public EntityID getBuildingId() {
		return getEntityId(PropertyName.BuildingId);
	}

	public int getCivilianId() {
		return getInteger(PropertyName.CivilianId);
	}

	public int getBuriedness() {
		return getInteger(PropertyName.Buriedness);
	}

	public int getTimeStep() {
		return getInteger(PropertyName.TimeStep);
	}

}
