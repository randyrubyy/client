package sample.object.Partition;

import java.util.Comparator;

public class RegionSizeComparator implements Comparator<EntityInPartition> {

	public RegionSizeComparator() {
	}

	@Override
	public int compare(EntityInPartition a, EntityInPartition b) {
		int s1 = a.getAssignCost();
		int s2 = b.getAssignCost();
		return s1 - s2;
	}
}
