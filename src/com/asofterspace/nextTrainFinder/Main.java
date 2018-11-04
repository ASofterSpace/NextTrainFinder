package com.asofterspace.nextTrainFinder;

import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.Utils;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.web.WebAccessor;
import com.asofterspace.toolbox.web.WebPreviewer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Main {
	
	public final static String PROGRAM_TITLE = "NextTrainFinder";
	public final static String VERSION_NUMBER = "0.0.0.1(" + Utils.TOOLBOX_VERSION_NUMBER + ")";
	public final static String VERSION_DATE = "4. November 2018";
	
	public static void main(String[] args) {
		
		// let the Utils know in what program it is being used
		Utils.setProgramTitle(PROGRAM_TITLE);
		Utils.setVersionNumber(VERSION_NUMBER);
		Utils.setVersionDate(VERSION_DATE);

		ConfigFile config = new ConfigFile("settings");

		// create a default config file, if necessary
		if (config.getAllContents().isEmpty()) {

			config.setAllContents(new JSON("{}"));
		}
		
		System.out.println("Gathering train info...");
		
		ConnectionAndRouteData data = new ConnectionAndRouteData();
		
		queryFromTo("Frankfurt+(Main)+Südbahnhof", "Darmstadt+Hauptbahnhof", data);
		
		queryFromTo("Frankfurt+(Main)+Südbahnhof", "Frankfurt+(Main)+Uni+Campus+Riedberg", data);
		
		String localOutFileName = "connections.htm";
		
		writeOutput(localOutFileName, data);
		
		WebPreviewer.openLocalFileInBrowser(localOutFileName);
	}
	
	private static String expandLocationStr(String locationName) {
		
		switch (locationName) {
			
			case "Frankfurt+(Main)+Südbahnhof":
				return "A=1@O=Frankfurt+(Main)+Südbahnhof@X=8686447@Y=50099329@U=80@L=003000912@B=1@V=6.9,@p=1541169362@";
			
			case "Frankfurt+(Main)+Uni+Campus+Riedberg":
				return "A=1@O=Frankfurt+(Main)+Uni+Campus+Riedberg@X=8628341@Y=50175494@U=80@L=003060765@B=1@V=6.9,@p=1541169362@";
			
			case "Darmstadt+Hauptbahnhof":
				return "A=1@O=Darmstadt+Hauptbahnhof@X=8631146@Y=49871938@U=80@L=003004734@B=1@V=6.9,@p=1541169362@";
		}
		
		return "";
	}
	
	private static void queryFromTo(String fromStr, String toStr, ConnectionAndRouteData data) {
	
		Map<String, String> parameters = new HashMap<>();
		
		parameters.put("HWAI=JS!ajax", "yes");
		parameters.put("HWAI=JS!js", "yes");
		parameters.put("isUserTime", "yes");
		parameters.put("REQ0JourneyStopsS0A", "1");
		parameters.put("REQ0JourneyStopsSID", expandLocationStr(fromStr));
		parameters.put("REQ0JourneyStopsZ0A", "1");
		parameters.put("REQ0JourneyStopsZID", expandLocationStr(toStr));
		parameters.put("S", fromStr);
		parameters.put("start", "1");
		DateFormat hhmm = new SimpleDateFormat("HH:mm");
		Date now = new Date();
		parameters.put("time", hhmm.format(now));
		parameters.put("timesel", "depart");
		parameters.put("Z", toStr);
		
		String reply = WebAccessor.post("https://www.rmv.de/auskunft/bin/jp/query.exe/dn", parameters);
		
		File replyFile = new File("lastreply.log");
		replyFile.setContent(reply);
		replyFile.save();
		
		System.out.println("We got a reply!");
		
		int i = 0;
		while (true) {
			int position = reply.indexOf("<tr id=\"trOverviewC0-" + i + "\"");
			if (position < 0) {
				break;
			}
			reply = reply.substring(position);
			
			reply = reply.substring(reply.indexOf(" class=\"pearlStart\">"));
			String from = reply.substring(reply.indexOf(">") + 1);
			from = from.substring(0, from.indexOf("<"));
			
			reply = reply.substring(reply.indexOf(" class=\"pearlStop\">"));
			String to = reply.substring(reply.indexOf(">") + 1);
			to = to.substring(0, to.indexOf("<"));
			
			reply = reply.substring(reply.indexOf(" class=\"planed\">"));
			String planned = reply.substring(reply.indexOf(">") + 1);
			planned = planned.substring(0, planned.indexOf("</div>"));
			planned = planned.replace("<br />", ", ");
			
			reply = reply.substring(reply.indexOf(" class=\"prognosis\">"));
			String prognosis = reply.substring(reply.indexOf(">") + 1);
			prognosis = prognosis.substring(0, prognosis.indexOf("</div>"));
			if (prognosis.equals("&nbsp;")) {
				prognosis = "";
			}
			
			reply = reply.substring(reply.indexOf(" class=\"duration\">"));
			String duration = reply.substring(reply.indexOf(">") + 1);
			duration = duration.substring(0, duration.indexOf("</td>"));
			
			reply = reply.substring(reply.indexOf(" class=\"products \">"));
			String productBaseStr = reply.substring(reply.indexOf(">") + 1);
			productBaseStr = productBaseStr.substring(0, productBaseStr.indexOf("</td>"));
			String products = "";
			String productSep = "";
			while (true) {
				int productPos = productBaseStr.indexOf("title=\"");
				if (productPos < 0) {
					break;
				}
				productBaseStr = productBaseStr.substring(productPos);
				productBaseStr = productBaseStr.substring(productBaseStr.indexOf("\"")+1);
				products = products + productSep + productBaseStr.substring(0, productBaseStr.indexOf("\""));
				productSep = " + ";
			}
			
			Connection currentConnection = new Connection(from, to);
			
			Route currentRoute = new Route(planned, prognosis, duration, products);
			
			data.put(currentConnection, currentRoute);
			
			i++;
		}
	}
	
	private static void writeOutput(String localOutFileName, ConnectionAndRouteData data) {
	
		StringBuilder content = new StringBuilder();
		
		content.append("<html>");
		content.append("<head>");
		content.append("<style type=\"text/css\">");
		content.append("div.connectionTitle {");
		content.append("    font-size: 150%;");
		content.append("}");
		content.append("td, th {");
		content.append("    text-align: center;");
		content.append("    min-width: 80px;");
		content.append("    border: 1px solid black;");
		content.append("}");
		content.append("</style>");
		content.append("</head>");
		content.append("<body>");
		
		boolean first = true;
		
		for (Connection connection : data.getConnections()) {
			
			if (first) {
				first = false;
			} else {
				System.out.println("\n");
			}
			
			System.out.println(connection.getFrom() + " nach " + connection.getTo() + ":");
			content.append("<div class=\"connectionTitle\">");
			content.append(connection.getFrom() + " &nbsp;&nbsp;&nbsp;&rarr;&nbsp;&nbsp;&nbsp; " + connection.getTo() + ":");
			content.append("</div>");
			
			List<Route> routes = connection.getRoutes();
			
			content.append("<table>");
			content.append("<tr>");
			content.append("<th>");
			content.append("Zeiten");
			content.append("</th>");
			content.append("<th>");
			content.append("Züge");
			content.append("</th>");
			content.append("<th>");
			content.append("Zusätzliche Informationen");
			content.append("</th>");
			content.append("</tr>");
			
			for (Route route : routes) {
				System.out.println(route.getPlanned() + " mit " + route.getProducts() + " " + route.getCurrentInfo());
				content.append("<tr>");
				content.append("<td>");
				content.append(route.getPlanned().replace(", ", "<br>"));
				content.append("</td>");
				content.append("<td>");
				content.append(route.getProducts().replace(" + ", "<br>"));
				content.append("</td>");
				content.append("<td>");
				content.append(route.getCurrentInfo().replace(" and ", "<br>"));
				content.append("</td>");
				content.append("</tr>");
			}
			content.append("</table>");
			content.append("<br>");
		}
		
		content.append("</body>");
		content.append("</html>");
		
		File localOutFile = new File(localOutFileName);
		
		localOutFile.setContent(content);
		
		localOutFile.save();
	}
}
