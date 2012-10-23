package sample.message;

import java.util.Comparator;

public class QueueMessageComparator implements Comparator<QueueMessage> {
	/**
	 * Compares the given QueueMessages according to importance level.
	 * 
	 * @param x
	 *            The first message to compare
	 * @param y
	 *            The second message to compare
	 * @return Returns an 1 or -1 accroding to importance level of the messages.
	 */
	@Override
	public int compare(QueueMessage x, QueueMessage y) {
		if (x.importance.ordinal() < y.importance.ordinal()) // compares the
																// imortance
																// level of the
																// messages
		{
			return -1;
		}
		if (x.importance.ordinal() > y.importance.ordinal()) {
			return 1;
		}
		return 0;
	}
}
