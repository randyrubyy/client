package sample.message.Type;

import java.util.ArrayList;
import java.util.List;

import sample.message.IntegerProperty;
import sample.message.MessageProperty;
import sample.message.PropertyName;

public class ExtinguishFireTaskMessage extends Message {

	public ExtinguishFireTaskMessage() {
		super();
	}

	public ExtinguishFireTaskMessage(int buildingId, int agentId) {
		this();
		setInteger(PropertyName.BuildingId, buildingId);
		setInteger(PropertyName.AgentId, agentId);
	}

	@Override
	protected List<MessageProperty<?>> createProperties() {
		List<MessageProperty<?>> properties = new ArrayList<MessageProperty<?>>();
		properties.add(new IntegerProperty(PropertyName.BuildingId, 2));
		properties.add(new IntegerProperty(PropertyName.AgentId, 2));
		return properties;
	}

	@Override
	public MessageType getType() {
		return MessageType.ExtinguishFireTaskMessage;
	}

	public int getBuildingId() {
		return getInteger(PropertyName.BuildingId);
	}

	public int getAgentId() {
		return getInteger(PropertyName.AgentId);
	}
}
