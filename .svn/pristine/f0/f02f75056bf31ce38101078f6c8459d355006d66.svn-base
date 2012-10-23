package sample.message.Type;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.worldmodel.EntityID;
import sample.message.IntegerProperty;
import sample.message.MessageProperty;
import sample.message.PropertyName;

public class PartitionAssignmentMessage extends Message {

	public PartitionAssignmentMessage() {
		super();
	}

	/**
	 * Construct a Reagion Asssignment Message
	 * 
	 * @param id
	 *            Id of the agent which assigned
	 * @param region
	 *            Id of the region which is assigned
	 */

	public PartitionAssignmentMessage(EntityID id, int region) {
		this();
		setEntityId(PropertyName.AgentId, id);
		setInteger(PropertyName.PartitionId, region);
	}

	public EntityID getAgentId() {
		return getEntityId(PropertyName.AgentId);
	}

	public int getRegion() {
		return getInteger(PropertyName.PartitionId);
	}

	/**
	 * Explains the messase as string.
	 * 
	 * @return Returns the information about the message as string.
	 */
	@Override
	public String toString() {
		return "Agent#" + getAgentId() + " is assigned to region#"
				+ getRegion();
	}

	@Override
	protected List<MessageProperty<?>> createProperties() {
		List<MessageProperty<?>> list = new ArrayList<MessageProperty<?>>();
		list.add(new IntegerProperty(PropertyName.AgentId, 2));
		list.add(new IntegerProperty(PropertyName.PartitionId, 1));
		return list;
	}

	@Override
	public MessageType getType() {
		return MessageType.RegionAssignmentMessage;
	}
}
