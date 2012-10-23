package sample.message.Type;

import java.util.ArrayList;
import java.util.List;

import sample.message.IntegerProperty;
import sample.message.MessageProperty;
import sample.message.PropertyName;

public class AgentIsBuriedMessage extends Message {

	public AgentIsBuriedMessage() {
		super();
	}

	public AgentIsBuriedMessage(int locationId, int agentId, int timeStep,
			int healtPoint, int buriedness, int damage) {
		this();
		setInteger(PropertyName.LocationId, locationId);
		setInteger(PropertyName.AgentId, agentId);
		setInteger(PropertyName.TimeStep, timeStep);
		setInteger(PropertyName.HealthPoint, healtPoint);
		setInteger(PropertyName.Buriedness, buriedness);
		setInteger(PropertyName.Damage, damage);
	}

	@Override
	protected List<MessageProperty<?>> createProperties() {
		List<MessageProperty<?>> properties = new ArrayList<MessageProperty<?>>();
		properties.add(new IntegerProperty(PropertyName.LocationId, 2));
		properties.add(new IntegerProperty(PropertyName.AgentId, 2));
		properties.add(new IntegerProperty(PropertyName.TimeStep, 2));
		properties.add(new IntegerProperty(PropertyName.HealthPoint, 2));
		properties.add(new IntegerProperty(PropertyName.Buriedness, 2));
		properties.add(new IntegerProperty(PropertyName.Damage, 2));
		return properties;
	}

	@Override
	public MessageType getType() {
		return MessageType.AgentIsBuriedMessage;
	}

	public int getAgentId() {
		return getInteger(PropertyName.AgentId);
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

}
