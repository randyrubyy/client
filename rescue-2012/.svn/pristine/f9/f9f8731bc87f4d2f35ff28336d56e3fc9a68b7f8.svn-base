package sample.object;

import java.util.Comparator;

import rescuecore2.standard.entities.Human;

public class BuriedHumanComparator implements Comparator<Human> {

	public int compare(Human h1, Human h2) {
		if (h1.isBuriednessDefined() && h2.isBuriednessDefined()) {
			return h1.getBuriedness() - h2.getBuriedness();
		} else {
			return 0;
		}
	}

}
