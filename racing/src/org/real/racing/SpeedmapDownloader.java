package org.real.racing;

import org.jsoup.nodes.Element;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Map;
import java.util.Hashtable;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.real.racing.domain.Track;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.real.racing.domain.Speedmap;




public class SpeedmapDownloader {
	
	ChromeDriver driver;
	String baseUrl;
	String xsltPath;
	private SimpleDateFormat urlFormat;
	private SimpleDateFormat dbFormat;
	private Pattern stylePat;
	private StringTransformer stringTransformer;


	public SpeedmapDownloader(String baseUrl, String xsltPath) {
		
		
		urlFormat = new SimpleDateFormat("yyyyMMdd");
		dbFormat = new SimpleDateFormat("yyyy-MM-dd");
		stylePat = Pattern.compile("\\s*width:\\s+([0-9.]+)%;\\s+height:\\s+([0-9.]+)%;\\s+left:\\s+([0-9.]+)%;\\s+top:\\s+([0-9.]+)%;\\s*");
		this.baseUrl = baseUrl;
		this.xsltPath =xsltPath;
		stringTransformer = new StringTransformer();
	}
	
	public Speedmap getSpeedmaps(Track track, Date date) {
		
		String url = baseUrl + track.getRnet() + "-" + urlFormat.format(date) + "/all-races";
		//String url = baseUrl + "ballarat" + "-" + urlFormat.format(date) + "/all-races";

		setUpDriver();
		driver.get(url);
		Element meeting = new Element("speedmaps");
		meeting.attr("track", track.getName());
		meeting.attr("date", dbFormat.format(date));
		
		meeting.appendChild(getFields());
		meeting.appendChild(getPositions(track));
		
		String meetingStr = stringTransformer.transform(meeting.toString(), xsltPath);
		
		Speedmap speedmap = unmarshallMeeting(meetingStr);
		
		
		
		
		try {
			FileWriter writer = new FileWriter("C:\\Users\\Workventures\\Documents\\speedmaps1.xml");
			writer.write(meetingStr);
			writer.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		

		
		return speedmap;
		
	}
	
	private Speedmap unmarshallMeeting(String meeting) {
		
		
		
		try {
			JAXBContext context = JAXBContext.newInstance(Speedmap.class);
			Unmarshaller um = context.createUnmarshaller();
			Speedmap speedmap = (Speedmap) um.unmarshal(new StringReader(meeting));
			return speedmap;
		} catch (JAXBException e) {
			
			e.printStackTrace();
			return null;
		}

	}
	
	private Element getFields() {
		
		Element fields = new Element("fields");
		
		List<WebElement> raceFields = driver.findElements(By.xpath("//div[@class='rn-tabs rn-js-tabs-sync']"));
		for(WebElement raceField : raceFields){
			Element race = new Element("race");
			String raceNum = raceField.getAttribute("data-race");
			race.attr("number",raceNum);
			String distance = raceField.getAttribute("data-race-distance");
			race.attr("distance",distance);
			List<WebElement> horseRows = raceField.findElements(By.xpath(".//tr[starts-with(@class,'fields-horse desktop')]"));
			for(WebElement horseRow : horseRows){
				Element horse = new Element("horse");
				String number = horseRow.getAttribute("data-horse-number");
				horse.attr("number", number);
				String name = horseRow.findElement(By.xpath(".//h3/a[1]")).getText();
				int indexNat = name.indexOf("(");
				if(indexNat > 0)
					name = name.substring(0, indexNat).trim();
				horse.attr("name", name);
				String barrier = horseRow.findElement(By.xpath(".//h3/span[@class='horseNumber']")).getText();
				horse.attr("barrier", barrier);
				String classAttribute = horseRow.getAttribute("class");
				if(classAttribute.contains("scratched")) {
					horse.attr("scratched", "true");
				}
				else {
					horse.attr("scratched", "false");
				}
				race.appendChild(horse);
			}
			
			fields.appendChild(race);
		}
		
		return fields;
	}
	
	private Element getPositions(Track track) {
			
			Element positions = new Element("positions");

			Actions actions = new Actions(driver);
			List<WebElement> linkMaps = driver.findElements(By.xpath("//a[@aria-controls='speed-maps']"));
			for(WebElement linkMap : linkMaps){
				actions.moveToElement(linkMap).click().perform();
			}
			
			List<WebElement> linkSettles = driver.findElements(By.xpath("//a[@data-type='settling-position']"));
			for(WebElement linkSettle : linkSettles){
				actions.moveToElement(linkSettle).click().perform();
			}
			
			List<WebElement> mapTables = driver.findElements(By.xpath("//div[@class='speed-maps-table']"));
			for(WebElement mapTable : mapTables){
				Element race = new Element("race");
				String raceNum = mapTable.getAttribute("data-race-number");
				race.attr("number", raceNum);
				WebElement paceDiv  = mapTable.findElement(By.xpath(".//div[starts-with(@class,'rn-speedmap__main-pace-moderate-second')]"));
				String pace = paceDiv.getText();
				race.attr("pace", pace);
				List<WebElement> horses = mapTable.findElements(By.xpath(".//div[@class='rn-speedmap__settling-horse-bg']"));
				for(WebElement horseDiv : horses) {
					Element horse = new Element("horse");
					String number = horseDiv.findElement(By.xpath(".//div[starts-with(@class,'rn-speedmap__settling-horse-circle')]/span")).getText();
					horse.attr("number", number);
					String name = horseDiv.findElement(By.xpath(".//div[@class='rn-speedmap__settling-horse-name']")).getText();
					int indexNat = name.indexOf("(");
					if(indexNat > 0)
						name = name.substring(0, indexNat).trim();
					horse.attr("name", name);
					String style = horseDiv.getAttribute("style");
					Matcher m = stylePat.matcher(style);
					if (m.find()) {
						Double width = new Double(m.group(1));
						Double height = new Double(m.group(2));
						Double left = new Double(m.group(3));
						Double top = new Double(m.group(4)); 
						Long forward = Math.round(left/width);
						Long wide = 3 - Math.round(top/height);
						if(track.getState().equals("NSW"))
							forward = 11 - forward;
						horse.attr("forward", forward.toString());
						horse.attr("wide", wide.toString());
				    }
					
					race.appendChild(horse);
					
				}
				positions.appendChild(race);
			}
			
			driver.quit();
			
			return positions;
	}
	
	private void setUpDriver(){
		
		System.setProperty("webdriver.chrome.driver", "C:/Users/Workventures/chromedriver/chromedriver.exe");
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--start-maximized");
		options.addArguments("disable-infobars");
		//options.addArguments("user-data-dir=C:/Users/Workventures/AppData/Local/Google/Chrome/User Data");
		Map<String, Object> preferences = new Hashtable<String, Object>();
		preferences.put("profile.default_content_settings.popups", 0);
		preferences.put("profile.default_content_setting_values.notifications", 2);
		preferences.put("download.prompt_for_download", "false");
		options.setExperimentalOption("prefs", preferences);
		driver = new ChromeDriver(options);
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
	}

}
