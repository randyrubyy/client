package sample.utilities;

import java.util.Comparator;

import rescuecore2.worldmodel.EntityID;

public class EntityIdComparator implements Comparator<EntityID> {

	public EntityIdComparator() {
	}

	@Override
	public int compare(EntityID a, EntityID b) {
		int s1 = a.getValue();
		int s2 = b.getValue();
		return s1 - s2;
	}
}
