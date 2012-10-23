package sample.object.Road;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.standard.entities.Area;
import sample.utilities.Search.Node;

public class AreaInfo {

	Area area;
	List<Node> connectedNodes;
	List<Node> connectedTerminalNodes;

	public AreaInfo(Area area) {
		this.area = area;
		connectedNodes = new ArrayList<Node>();
		connectedTerminalNodes = new ArrayList<Node>();
	}

	public void addNode(Node node) {
		connectedNodes.add(node);
	}

	public void addTerminalNode(Node node) {
		connectedTerminalNodes.add(node);
	}

	public List<Node> getTerminalNodes() {
		return connectedTerminalNodes;
	}

	public List<Node> getNodes() {
		return connectedNodes;
	}

}
