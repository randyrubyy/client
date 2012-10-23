package sample.object.Partition;

import java.util.Comparator;

import sample.object.SampleWorldModel;
import sample.object.StandardPoint;

public class RegionDistanceComparator implements Comparator<EntityInPartition> {

	StandardPoint<Integer> reference_;
	SampleWorldModel model_;

	public RegionDistanceComparator(EntityInPartition reference, SampleWorldModel model) {
		model_ = model;
		reference_ = reference.getCenter(model);
	}

	@Override
	public int compare(EntityInPartition a, EntityInPartition b) {
		StandardPoint<Integer> p1 = a.getCenter(model_);
		StandardPoint<Integer> p2 = b.getCenter(model_);
		double d1 = p1.distanceTo(reference_);
		double d2 = p2.distanceTo(reference_);
		return (int) (d1 - d2);
	}
}
