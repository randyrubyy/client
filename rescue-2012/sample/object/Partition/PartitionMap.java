package sample.object.Partition;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.standard.entities.StandardEntity;

public class PartitionMap {
	List<EntityInPartition> partition;
	// 分区总数
	int size;
	// 第几个分区
	int number;

	public PartitionMap(int no) {
		partition = new ArrayList<EntityInPartition>();
		number = no;
	}

	public List<EntityInPartition> getPartitions() {
		return partition;
	}

	public void setPartitions(List<EntityInPartition> regions) {
		this.partition = regions;
	}

	public int getTotalSize() {
		return size;
	}

	public void setTotalSize(int totalSize) {
		this.size = totalSize;
	}

	public int getNo() {
		return number;
	}

	void addRegion(EntityInPartition region) {
		partition.add(region);
		region.setRegionGroup(this);
	}

	public boolean contains(StandardEntity entity) {
		for (EntityInPartition region : partition) {
			if (region.contains(entity)) {
				return true;
			}
		}
		return false;
	}

}
