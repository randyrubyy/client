package sample.message.Type;

import java.util.ArrayList;
import java.util.List;

import sample.message.IntegerProperty;
import sample.message.MessageProperty;
import sample.message.PropertyName;

public class CivilianInformationMessage extends Message {

	public CivilianInformationMessage() {
		super();
	}

	public CivilianInformationMessage(int locationId, int civilianId,
			int timeStep, int healtPoint, int buriedness, int damage) {
		this();
		setInteger(PropertyName.LocationId, locationId);
		setInteger(PropertyName.CivilianId, civilianId);
		setInteger(PropertyName.TimeStep, timeStep);
		setInteger(PropertyName.HealthPoint, healtPoint);
		setInteger(PropertyName.Buriedness, buriedness);
		setInteger(PropertyName.Damage, damage);
	}

	@Override
	protected List<MessageProperty<?>> createProperties() {
		List<MessageProperty<?>> properties = new ArrayList<MessageProperty<?>>();
		properties.add(new IntegerProperty(PropertyName.LocationId, 2));
		properties.add(new IntegerProperty(PropertyName.CivilianId, 2));
		properties.add(new IntegerProperty(PropertyName.TimeStep, 2));
		properties.add(new IntegerProperty(PropertyName.HealthPoint, 2));
		properties.add(new IntegerProperty(PropertyName.Buriedness, 2));
		properties.add(new IntegerProperty(PropertyName.Damage, 2));
		return properties;
	}

	@Override
	public MessageType getType() {
		return MessageType.CivilianInformationMessage;
	}

	public int getCivilianId() {
		return getInteger(PropertyName.CivilianId);
	}

	public int getLocationId() {
		return getInteger(PropertyName.LocationId);
	}

	public int getDamage() {
		return getInteger(PropertyName.Damage);
	}

	public int getBuriedness() {
		return getInteger(PropertyName.Buriedness);
	}

	public int getHp() {
		return getInteger(PropertyName.HealthPoint);
	}

	public int getTimeStep() {
		return getInteger(PropertyName.TimeStep);
	}

	@Override
	public String toString() {
		return "Civilian information message: " + getCivilianId() + ", "
				+ getLocationId();
	}

}
