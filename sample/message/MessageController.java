package sample.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import sample.message.Type.Message;
import sample.object.SampleWorldModel;

public class MessageController {

	Comparator<QueueMessage> comparator;
	PriorityQueue<QueueMessage> queue;

	public MessageController() {
		comparator = new QueueMessageComparator();
		queue = new PriorityQueue<QueueMessage>(10, comparator);
	}

	public void setCommunicationCondition(SampleWorldModel world) {
		Collection<StandardEntity> ambulanceCenter = world
				.getEntitiesOfType(StandardEntityURN.AMBULANCE_CENTRE);
		Collection<StandardEntity> fireStation = world
				.getEntitiesOfType(StandardEntityURN.FIRE_STATION);
		Collection<StandardEntity> policeOffice = world
				.getEntitiesOfType(StandardEntityURN.POLICE_OFFICE);

		if (ambulanceCenter.isEmpty() && fireStation.isEmpty()
				&& policeOffice.isEmpty()) {
		} else if (ambulanceCenter.isEmpty() && fireStation.isEmpty()) {
		} else if (ambulanceCenter.isEmpty() && policeOffice.isEmpty()) {
		} else if (fireStation.isEmpty() && policeOffice.isEmpty()) {
		} else if (ambulanceCenter.isEmpty()) {
		} else if (policeOffice.isEmpty()) {
		} else if (fireStation.isEmpty()) {
		} else {
		}
	}

	public void addMessage(int channel, Message message,
			MessagePriority importance) {
		QueueMessage queueMessage = new QueueMessage(channel, message,
				importance);
		queue.add(queueMessage);
	}

	public Collection<QueueMessage> getMostImportantMessages() {
		if (!queue.isEmpty()) {
			Collection<QueueMessage> result = new ArrayList<QueueMessage>();
			// for (int i = 0; i < NUMBER_OF_MESSAGES_TO_SEND &&
			// !queue.isEmpty(); i++) {
			while (!queue.isEmpty()) {
				result.add(queue.poll());
			}
			return result;
		} else {
			return null;
		}
	}

	public void clearQueue() {
		queue.clear();
	}

	public int queueSize() {
		return queue.size();
	}
}
