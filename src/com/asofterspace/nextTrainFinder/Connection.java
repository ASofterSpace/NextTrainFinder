package com.asofterspace.nextTrainFinder;

import java.util.ArrayList;
import java.util.List;


public class Connection {

	private String from;
	
	private String to;
	
	private List<Route> routes;
	
	
	public Connection(String from, String to) {
	
		this.from = from;
		
		this.to = to;
		
		routes = new ArrayList<>();
	}
	
	public void addRoute(Route route) {

		routes.add(route);
	}
	
	public List<Route> getRoutes() {
	
		return routes;
	}
	
	public String getFrom() {
		return from;
	}
	
	public String getTo() {
		return to;
	}
	
	public boolean equals(Object other) {
	
		if (other == null) {
			return false;
		}
		
		if (other instanceof Connection) {
			Connection otherConnection = (Connection) other;
			
			return from.equals(otherConnection.from) && to.equals(otherConnection.to);
		}
		
		return false;
	}
	
	public int hashCode() {
		
		int result = 0;
		
		if (from != null) {
			result += from.hashCode();
		}
		
		if (to != null) {
			result += to.hashCode();
		}
		
		return result;
	}

}