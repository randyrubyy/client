package sample.object.Building;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntityConstants.Fieryness;
import sample.object.SampleWorldModel;

public class BuildingInfo {

	private final Building building_;

	// 得到当前房屋
	public Building getBuilding() {
		return building_;
	}

	private List<Building> negihbours_;
	public static final int MAX_DIST_NEAR_BUILDINGS = 7000;
	private static final int MIN_HEATING_VALUE = 0;
	int latestTemperature_;
	int temperatureUpdateTime_;

	public BuildingInfo(Building building) {
		building_ = building;
		if (building.isTemperatureDefined()) {
			latestTemperature_ = building.getTemperature();
		} else {
			latestTemperature_ = 0;
		}
	}

	public List<Building> getNeighbours(SampleWorldModel world) {
		if (negihbours_ == null) {
			negihbours_ = new ArrayList<Building>();
			negihbours_ = world.findNearBuildings(building_,
					MAX_DIST_NEAR_BUILDINGS);
			return negihbours_;
		}
		return negihbours_;
	}

	public List<Building> getNeighboursFar(SampleWorldModel world) {
		if (negihbours_ == null) {
			negihbours_ = new ArrayList<Building>();
			negihbours_ = world.findNearBuildings(building_, 15000);
			return negihbours_;
		}
		return negihbours_;
	}

	public boolean isNeighbourBuilding(Building building, SampleWorldModel world) {
		if (negihbours_ == null) {
			negihbours_ = getNeighbours(world);
		}
		if (!negihbours_.isEmpty()) {
			for (Building neighbour : negihbours_) {
				if (building.getID().getValue() == neighbour.getID().getValue()) {
					return true;
				}
			}
		}
		return false;
	}

	public int getLatestTemperature() {
		return latestTemperature_;
	}

	public void setLatestTemperature(int latestTemperature) {
		this.latestTemperature_ = latestTemperature;
	}

	public boolean isHeating() {
		if (building_.isFierynessDefined()
				&& building_.getFierynessEnum() != Fieryness.BURNT_OUT) {
			int d;
			boolean heating;

			d = getTemperatureDifference();
			heating = (d > MIN_HEATING_VALUE);
			return heating;
		} else {
			return false;
		}
	}

	public double getHeatingSpeed(int timeStep) {
		int timeDifference;

		timeDifference = timeStep - temperatureUpdateTime_;
		if (timeDifference > 0) {
			int temperatureDifference;
			double r;

			temperatureDifference = getCurrentTemperature()
					- latestTemperature_;
			r = (double) temperatureDifference / timeDifference;
			return r;
		} else {
			return 0;
		}
	}

	public int getCurrentTemperature() {
		if (building_.isTemperatureDefined()) {
			return building_.getTemperature();
		} else {
			return 0;
		}
	}

	public int getTemperatureDifference() {
		int temperature, difference;

		temperature = getCurrentTemperature();
		difference = temperature - latestTemperature_;
		return difference;
	}

	public void updateTemperature(int timeStep) {
		if (building_.isTemperatureDefined()) {
			latestTemperature_ = building_.getTemperature();
			temperatureUpdateTime_ = timeStep;
		}
	}
}
