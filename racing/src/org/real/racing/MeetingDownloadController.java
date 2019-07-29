package org.real.racing;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.xml.transform.TransformerFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Enumeration;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;
import org.real.racing.domain.Track;



public class MeetingDownloadController {
	
	final static Logger logger = Logger.getLogger(MeetingDownloadController.class);
	
	private Properties urls;
	private Properties headers;
	private Properties paths;
	private Properties log4jProps;
	private SimpleDateFormat resultsFormat;
	private SimpleDateFormat stewardsFormat;
	private SimpleDateFormat sectFormat;

	public MeetingDownloadController() {
		getProperties();
		resultsFormat = new SimpleDateFormat("yyyy-MM-dd");
		stewardsFormat = new SimpleDateFormat("ddMMyyyy");
		sectFormat = new SimpleDateFormat("yyyy/ddMM");
	}
	
	public void process(Track track, Date date, Integer numRaces) {
		
		String downloadPath = paths.getProperty("download");
		downloadPath += resultsFormat.format(date);
		downloadPath += "_";
		downloadPath += track.getCode();
		System.out.println(downloadPath);
		
		File downloadFolder = new File(downloadPath);
		if((!downloadFolder.exists() && downloadFolder.isDirectory()))
			downloadFolder.mkdir();
		
			
		
		String resultsUrl = urls.getProperty("urlrcom");
		String resultsDate = resultsFormat.format(date);
		resultsUrl += resultsDate;
		resultsUrl += "/";
		resultsUrl += track.getRcom();
		System.out.println(resultsUrl);	
		
		String sectionalsUrl = urls.getProperty("urlsect");
		sectionalsUrl += sectFormat.format(date);
		sectionalsUrl += track.getCode();
		sectionalsUrl += "-";
		sectionalsUrl += "1111";
		sectionalsUrl += ".pdf";
		System.out.println(sectionalsUrl);	
		
		String stewardsUrl = urls.getProperty("urlstewards");
		stewardsUrl += stewardsFormat.format(date);
		stewardsUrl += track.getCode();
		stewardsUrl += ".pdf";
		System.out.println(stewardsUrl);	
		
		Enumeration<String> enums = (Enumeration<String>) headers.propertyNames();
	    while (enums.hasMoreElements()) {
	      String key = enums.nextElement();
	      String value = headers.getProperty(key);
	      System.out.println(key + " : " + value);
	    }
	 
/*		ResultsDownloader results = new ResultsDownloader();
		Element meeting = results.getMeeting(resultsUrl, downloadPath);
		meeting.attr("date", resultsDate);
		meeting.attr("track", track.getName());
		try {
			String meetingFile = downloadPath;
			meetingFile += "\\meeting.xml";
			FileWriter writer = new FileWriter(meetingFile);
			writer.write(meeting.toString());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	    System.out.println(numRaces);
	    System.out.println(sectionalsUrl);
	    SectionalsDownloader sectionalsDownloader = new SectionalsDownloader(headers,log4jProps);
	    Element sectionals = sectionalsDownloader.getMeeting(sectionalsUrl, numRaces);
		
		System.out.println(sectionals.toString());

	}
	
	private void getProperties() {
		FileInputStream fin = null;
		
		try {
			fin = new FileInputStream("resources/properties/urls.properties");
			urls = new Properties();
			urls.load(fin);
			fin.close();
		} catch (FileNotFoundException e1) {
			String msg = "File not found in MeetingDownloadController: resources/properties/urls.properties";
			logger.fatal(msg);
			System.exit(0);
		} catch (IOException e1) {
			String msg = "File IOException in MeetingDownloadController: resources/properties/urls.properties";
			logger.fatal(msg);
			System.exit(0);
		}
		
		try {
			fin = new FileInputStream("resources/properties/headers.properties");
			headers = new Properties();
			headers.load(fin);
			fin.close();
		} catch (FileNotFoundException e) {
			String msg = "File not found in MeetingDownloadController: resources/properties/headers.properties";
			logger.fatal(msg);
			System.exit(0);
		} catch (IOException e) {
			String msg = "File IOException in MeetingDownloadController: resources/properties/headers.properties";
			logger.fatal(msg);
			System.exit(0);
		}
		
		try {
			fin = new FileInputStream("resources/properties/paths.properties");
			paths = new Properties();
			paths.load(fin);
			fin.close();
		} catch (FileNotFoundException e) {
			String msg = "File not found in MeetingDownloadController: resources/properties/paths.properties";
			logger.fatal(msg);
			System.exit(0);
		} catch (IOException e) {
			String msg = "File IOException in MeetingDownloadController: resources/properties/paths.properties";
			logger.fatal(msg);
			System.exit(0);
		}
		
		try {
			fin = new FileInputStream("resources/properties/log4j.properties");
			log4jProps = new Properties();
			log4jProps.load(fin);
			fin.close();
		} catch (FileNotFoundException e) {
			String msg = "File not found in MeetingDownloadController: resources/properties/log4jProps.properties";
			logger.fatal(msg);
			System.exit(0);
		} catch (IOException e) {
			String msg = "File IOException in MeetingDownloadController: resources/properties/log4jProps.properties";
			logger.fatal(msg);
			System.exit(0);
		}
	}
}
