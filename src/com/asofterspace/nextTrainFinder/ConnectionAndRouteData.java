package com.asofterspace.nextTrainFinder;

import java.util.ArrayList;
import java.util.List;


public class ConnectionAndRouteData {
	
	private List<Connection> connections;
	
	
	public ConnectionAndRouteData() {
	
		connections = new ArrayList<>();
	}
	
	public void put(Connection connection, Route route) {
		
		for (Connection curConn : connections) {
			if (curConn.equals(connection)) {
				curConn.addRoute(route);
				return;
			}
		}
		
		connection.addRoute(route);
		connections.add(connection);
	}
	
	public List<Connection> getConnections() {
		return new ArrayList<>(connections);
	}

}