package sample.utilities.Search;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.Road;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import sample.object.SampleWorldModel;
import sample.object.Road.AreaInfo;

public class NodeCreator {

	SampleWorldModel model;
	List<Node> nodes;

	public NodeCreator(SampleWorldModel model) {
		initialize(model);
	}

	public void initialize(SampleWorldModel model) {
		this.model = model;
		nodes = new ArrayList<Node>();
	}

	public void createNodes() {
		for (Entity entity : model.getAllEntities()) {
			if (entity instanceof Road || entity instanceof Building) {
				Area area;

				area = (Area) entity;
				for (EntityID id : area.getNeighbours()) {
					// so that there will be only one node for two neighbors
					if (id.getValue() > area.getID().getValue()) {
						Entity neighbor;
						Edge edge;
						Node node;

						neighbor = model.getEntity(id);
						if (neighbor instanceof Area) {
							Area otherArea;

							otherArea = (Area) neighbor;
							edge = area.getEdgeTo(id);
							if (edge.isPassable()) {
								AreaInfo info1, info2;
								node = new Node(edge);
								node.setConnectedAreas(area, otherArea);
								info1 = model.getAreaInfo(area.getID());
								info2 = model.getAreaInfo(otherArea.getID());

								info1.addNode(node);
								info2.addNode(node);
								nodes.add(node);

								node.setId(nodes.size());
							}
						}
					}
				}
			}
			if (entity instanceof Building || entity instanceof Road) {
				Node node;
				Area area;

				area = (Area) entity;
				node = new Node(area.getX(), area.getY());
				node.setConnectedAreas(area);
				if (!nodes.contains(node)) {
					AreaInfo info;
					nodes.add(node);
					node.setId(nodes.size());
					info = model.getAreaInfo(area.getID());
					info.addNode(node);
					info.addTerminalNode(node);
				}
			}
		}
	}

	public static Node getNode(Point point, Area area) {
		Node node;

		node = new Node(point.x, point.y);
		node.setConnectedAreas(area);
		return node;
	}

	public List<Node> getNodes() {
		return nodes;
	}
}
