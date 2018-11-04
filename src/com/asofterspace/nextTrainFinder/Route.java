package com.asofterspace.nextTrainFinder;


public class Route {

	private String planned;
	
	private String currentInfo;
	
	private String duration;
	
	private String products;
	

	public Route(String planned, String prognosis, String duration, String products) {
	
		this.planned = planned;
		
		this.currentInfo = "";
		String infoSep = "";
		while (true) {
			int infoPos = prognosis.indexOf("title=\"");
			if (infoPos < 0) {
				break;
			}
			prognosis = prognosis.substring(infoPos);
			prognosis = prognosis.substring(prognosis.indexOf("\"")+1);
			this.currentInfo = this.currentInfo + infoSep + prognosis.substring(0, prognosis.indexOf("\""));
			infoSep = " and ";
		}
		this.currentInfo = this.currentInfo.replace("Dieses Symbol zeigt an, dass mit Versp&#228;tungen zu rechnen ist. Die voraussichtlichen An-/Abfahrtszeiten sind in der Detailansicht eingef&#252;gt.", "versp&#228;tet");

		this.duration = duration;
		
		this.products = products;
	}
	
	public String getPlanned() {
		return planned;
	}
	
	public String getProducts() {
		return products;
	}
	
	public String getCurrentInfo() {
		return currentInfo;
	}
	
}