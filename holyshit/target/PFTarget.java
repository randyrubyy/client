package holyshit.target;
import rescuecore2.worldmodel.EntityID;

public class PFTarget implements Comparable<PFTarget> {
	/**
	 * @author iorange
	 * PF任务
	 */
	float weight; //权重
	EntityID locationID; //目标(location)
	EntityID humanID;    //人的ID(区别是否重复消息)
	
	public PFTarget(float wt, EntityID locID, EntityID humID)
	{
		this.weight = wt;
		this.locationID = locID;
		this.humanID = humID;
	}
	
	@Override
	
	public int compareTo(PFTarget o) {
		/**
		 * 排序的时候用的东西。。
		 * 权重大的优先！
		 */
		if( weight < o.weight)
			return 1;
		else if(weight > o.weight)
			return -1;
		else
			return 0;
	}
	
	public EntityID getHumanId()
	{
		return this.humanID;
	}
	
	public EntityID getLocId()
	{
		return this.locationID;
	}
	
	public float getWeight()
	{
		return this.weight;
	}
	
}