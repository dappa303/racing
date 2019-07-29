package org.real.racing;

import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;

public class ResultsDownloader {

	final static Logger logger = Logger.getLogger(ResultsDownloader.class);
	private ChromeDriver driver;
	private Robot robot;
	
	public ResultsDownloader() {
		
	}

	public Element getMeeting(String url, String downloadPath) {
		setUp(downloadPath);
		Element resultsMeeting = new Element("results-meeting");
		Element sectionalsMeeting = new Element("sectionals-meeting");
		driver.get(url);
		// https://www.racing.com/form/2019-06-01/caulfield
		WebElement rail = driver.findElement(By.xpath("//div[@class='rail'][1]"));
		Actions actions = new Actions(driver);
		actions.moveToElement(rail).click().perform();
		resultsMeeting.attr("rail", rail.findElement(By.xpath("./div[1]/p[1]/span[1]")).getText());
		String prevRail = rail.findElement(By.xpath(".//div[@class='popover-content']/p[1]/span[1]")).getText();
		if (prevRail.length() > 8) {
			resultsMeeting.attr("prev-rail", prevRail.substring(7, prevRail.length() - 1));
		}

		List<WebElement> linkAnchors = driver
				.findElements(By.xpath("//ul[@class='paging hlist '][1]/li[@class=' previous']/a"));
		ArrayList<String> links = new ArrayList<String>();
		for (WebElement anchor : linkAnchors)
			if (anchor.isDisplayed())
				links.add(anchor.getAttribute("href"));

		Element stewardsMeeting = new Element("stewards-meeting");
		boolean hasStewards = !driver
				.findElements(By.xpath("//ul[@class='nav-tabs'][1]/li[2]/a[contains(., \"stewards' report\")]"))
				.isEmpty();
		if (hasStewards) {
			stewardsMeeting = getStewards();
		}

		for (int i = 0; i < links.size(); i++) {
			driver.get(links.get(i));

			String status = driver
					.findElement(By.xpath("//div[@class='c tright'][1]/div[@class='exotics']/label/span[1]")).getText()
					.trim();

			if (status.equals("Results")) {
				Element resultsRace = getResultsRace();
				resultsMeeting.appendChild(resultsRace);
				boolean hasSectionals = !driver
						.findElements(By.xpath("//div[@class='nav-tabs-outer'][1]/ul/li/a[contains(., 'sectionals')]"))
						.isEmpty();
				if (hasSectionals) {
					Element sectionalsRace = getSectionalsRace();
					sectionalsMeeting.appendChild(sectionalsRace);
				}
			}
		}

		Element meeting = new Element("meeting");
		meeting.appendChild(resultsMeeting);
		Elements children = sectionalsMeeting.children();
		if (children.size() > 0)
			meeting.appendChild(sectionalsMeeting);
		children = stewardsMeeting.children();
		if (children.size() > 0)
			meeting.appendChild(stewardsMeeting);

		try {
			FileWriter writer = new FileWriter("C:\\Users\\Workventures\\Documents\\sect\\sectionals.xml");
			writer.write(meeting.toString());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return meeting;

	}

	private Element getSectionalsRace() {

		Element race = new Element("race");
		ArrayList<Element> runners = new ArrayList<Element>();
		boolean fullSectionals;
		Integer raceDistance;

		WebElement sect = driver
				.findElement(By.xpath("//div[@class='nav-tabs-outer'][1]/ul/li/a[contains(., 'sectionals')]"));
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		jse.executeScript("arguments[0].scrollIntoView();", sect);
		jse.executeScript("window.scrollBy(0,-250)", "");
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block

		}
		Actions actions = new Actions(driver);
		actions.moveToElement(sect).click().perform();

		WebElement nameContainer = driver
				.findElement(By.xpath("//div[@class='race-name-container'][1]/div[@class='t tc']"));
		String distanceString = nameContainer.findElement(By.xpath("./div[@class='c']/h3[1]/span[2]")).getText().trim();
		distanceString = distanceString.replaceFirst("m", "");
		raceDistance = new Integer(distanceString);

		int options = driver.findElements(By.xpath("//div[@id='sectionals'][1]/div[1]/div[1]/ul[1]/li")).size();
		if (options == 3)
			fullSectionals = true;
		else
			fullSectionals = false;

		String raceNumber = driver
				.findElement(By.xpath("//div[@class='race-name-container'][1]/div[@class='t tc']/div[1]/span"))
				.getText().trim();
		race.attr("number", raceNumber);

		ArrayList<String> names = new ArrayList<String>();
		List<WebElement> runnerElements = null;
		if (fullSectionals) {
			runnerElements = driver.findElements(By.xpath(
					"//div[@id='overview-container'][1]/div[@class='static-left']/table/tbody/tr/td[@class='name']/a/span"));
		} else {
			runnerElements = driver.findElements(By.xpath(
					"//div[@id='sectionals-container'][1]/div[@class='static-left']/table/tbody/tr/td[@class='name']/a/span"));
		}

		for (WebElement runner : runnerElements) {
			System.out.println(runner.getText());
			String name = runner.getText().trim();
			String[] splitName = name.split("\\(");
			name = splitName[0];
			name = name.replaceFirst("\\d+\\.", "").trim();
			names.add(name);
		}

		ArrayList<String> peaks = new ArrayList<String>();
		;
		if (fullSectionals) {
			List<WebElement> peakElements = driver.findElements(
					By.xpath("//div[@id='overview-container'][1]/div[@class='data-roll']/div/table/tbody/tr/td[5]"));
			for (WebElement peakElement : peakElements) {
				jse.executeScript("arguments[0].scrollIntoView();", peakElement);
				jse.executeScript("window.scrollBy(0,-250)", "");
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block

				}

				actions = new Actions(driver);
				actions.moveToElement(peakElement).build().perform();

				String peak = peakElement.findElement(By.xpath("//div[@class='popover-content'][1]/p")).getText()
						.trim();
				peaks.add(peak);
			}
		}

		for (int i = 0; i < names.size(); i++) {
			Element run = new Element("run");
			run.attr("name", names.get(i));
			if (fullSectionals) {
				String peakDetails = peaks.get(i);
				String[] splitPeak = peakDetails.split("km/h\\s+at\\s+");
				String peakSpeed = splitPeak[0];
				run.attr("peak-speed", peakSpeed);
				String peakDistance = splitPeak[1].replaceFirst("m", "");
				run.attr("peak-distance", peakDistance);

			}
			runners.add(run);
		}

		if (fullSectionals) {
			WebElement sectionals = driver
					.findElement(By.xpath("//ul[@class='grid-filters ng-isolate-scope'][1]/li[@id='sectionals']/a"));
			jse.executeScript("arguments[0].scrollIntoView();", sectionals);
			jse.executeScript("window.scrollBy(0,-250)", "");
			actions = new Actions(driver);
			actions.moveToElement(sectionals).click().perform();
		}

		List<WebElement> sectionalElements = driver.findElements(
				By.xpath("//div[@id='sectionals-container'][1]/div[@class='data-roll']/div/table/tbody/tr"));

		WebElement winner = sectionalElements.get(0);
		String raceTime = getTime(winner.findElement(By.xpath("./td[1]/div/div[1]/span")).getText());
		race.attr("time", raceTime);

		System.out.println(race.toString());

		for (int i = 0; i < runners.size(); i++) {
			ArrayList<Element> sectionals = getSectionals(sectionalElements.get(i), raceDistance, fullSectionals);
			Element runner = runners.get(i);
			for (Element sectional : sectionals)
				runner.appendChild(sectional);
			race.appendChild(runner);
		}

		return race;
	}

	private ArrayList<Element> getSectionals(WebElement sects, Integer raceDistance, boolean fullSectionals) {

		ArrayList<Element> sectionals = new ArrayList<Element>();

		driver.manage().timeouts().implicitlyWait(0, TimeUnit.MILLISECONDS);

		List<WebElement> sectCells = sects.findElements(By.xpath("./td/div"));

		if (fullSectionals) {
			for (int i = 0; i < sectCells.size(); i++) {
				Integer distance = (sectCells.size() - i) * 200;
				if (distance <= raceDistance) {
					Element sectional = new Element("sectional");
					sectional.attr("distance", distance.toString());
					WebElement sectCell = sectCells.get(i);
					boolean isSimple = sectCell.findElements(By.xpath("./div[@data-container='body']")).isEmpty();
					String time = "";
					String position = "";
					if (isSimple) {
						time = getTime(sectCell.findElement(By.xpath("./div[1]/span")).getText());
						position = sectCell.findElement(By.xpath("./div[2]/span")).getText();
					} else {
						time = getTime(sectCell.findElement(By.xpath("./div/div[1]/span")).getText());
						position = sectCell.findElement(By.xpath("./div/div[2]/span")).getText();
					}
					sectional.attr("time", time);
					position = position.replaceFirst("(st|nd|rd|th)", "");
					sectional.attr("position", position);
					sectionals.add(sectional);
				}
			}

		} else {
			for (int i = 1; i < sectCells.size(); i++) {
				Integer distance = (sectCells.size() - i) * 200;
				Element sectional = new Element("sectional");
				sectional.attr("distance", distance.toString());
				WebElement sectCell = sectCells.get(i);
				boolean isSimple = sectCell.findElements(By.xpath("./div[@data-container='body']")).isEmpty();
				String time = "";
				String position = "";
				if (isSimple) {
					time = getTime(sectCell.findElement(By.xpath("./div[1]/span")).getText());
					position = sectCell.findElement(By.xpath("./div[2]/span")).getText();
				} else {
					time = getTime(sectCell.findElement(By.xpath("./div/div[1]/span")).getText());
					position = sectCell.findElement(By.xpath("./div/div[2]/span")).getText();
				}
				sectional.attr("time", time);
				position = position.replaceFirst("(st|nd|rd|th)", "");
				sectional.attr("position", position);
				sectionals.add(sectional);
			}
		}

		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

		return sectionals;
	}

	private Element getResultsRace() {
		Element race = new Element("race");

		WebElement nameContainer = driver
				.findElement(By.xpath("//div[@class='race-name-container'][1]/div[@class='t tc']"));
		String number = nameContainer.findElement(By.xpath("./div[1]/span")).getText().trim();
		race.attr("number", number);
		String startTime = nameContainer.findElement(By.xpath("./div[@class='c']/h3[1]/span[1]")).getText().trim();
		race.attr("start-time", startTime);
		String distance = nameContainer.findElement(By.xpath("./div[@class='c']/h3[1]/span[2]")).getText().trim();
		int index = distance.indexOf("m");
		distance = distance.substring(0, index);
		race.attr("distance", distance);
		String name = nameContainer.findElement(By.xpath("./div[@class='c']/h3[1]/span[3]")).getText().trim();
		race.attr("name", name);

		WebElement infoContainer = driver.findElement(By.xpath("//div[@class='track-info-container t'][1]"));
		try {
			String weightCondition = infoContainer
					.findElement(By.xpath("./span[@ng-if='result.WeightConditionDescription']")).getText().trim();
			weightCondition = weightCondition.replaceAll("&nbsp;", "");
			weightCondition = weightCondition.replaceAll("\\.", " ").trim();
			race.attr("weight-condition", weightCondition);
		} catch (NoSuchElementException e) {
			// do nothing
		}
		try {
			String restrictions = infoContainer.findElement(By.xpath("./span[@ng-if='result.Restrictions']")).getText()
					.trim();
			restrictions = restrictions.replaceAll("&nbsp;", "");
			restrictions = restrictions.replaceAll("\\.", " ").trim();
			race.attr("restrictions", restrictions);
		} catch (NoSuchElementException e) {
			// do nothing
		}
		try {
			String raceClass = infoContainer.findElement(By.xpath("./span[@ng-if='result.ClassRestriction']")).getText()
					.trim();
			raceClass = raceClass.replaceAll("&nbsp;", "");
			raceClass = raceClass.replaceAll("\\.", " ").trim();
			race.attr("race-class", raceClass);
		} catch (NoSuchElementException e) {
			// do nothing
		}
		try {
			String groupStatus = infoContainer.findElement(By.xpath("./span[@ng-if='result.GroupDesc']")).getText()
					.trim();
			groupStatus = groupStatus.replaceAll("&nbsp;", "");
			groupStatus = groupStatus.replaceAll("\\.", " ").trim();
			race.attr("group-status", groupStatus);
		} catch (NoSuchElementException e) {
			// do nothing
		}
		try {
			WebElement claim = infoContainer.findElement(By.xpath("./span[@ng-if='result.ApprenticesCanClaim']"));
			race.attr("claim", "true");
		} catch (NoSuchElementException e) {
			race.attr("claim", "false");
		}
		try {
			String fieldLimit = infoContainer.findElement(By.xpath("./span[@ng-if='result.FieldLimit']")).getText()
					.trim();
			String[] splitLimit = fieldLimit.split(":");
			splitLimit = splitLimit[1].split("\\.");
			fieldLimit = splitLimit[0].trim();
			race.attr("field-limit", fieldLimit);
		} catch (NoSuchElementException e) {
			// do nothing
		}

		String trackCondition = driver.findElement(By.xpath("//div[@class='condition'][1]/div[1]/p[1]/span")).getText();
		String[] splitCondition = trackCondition.split("\\s+");
		trackCondition = splitCondition[1].trim();
		race.attr("track-condition", trackCondition);

		String trackRecord = driver.findElement(By.xpath("//div[@class='record'][1]/div[1]/p[1]")).getText().trim();
		race.attr("track-record", trackRecord);

		String prizeMoney = driver.findElement(By.xpath("//div[@class='prizemoney'][1]/div[1]/p[1]")).getText().trim();
		prizeMoney = prizeMoney.replaceFirst("\\$", "");
		prizeMoney = prizeMoney.replaceAll(",", "");
		race.attr("prize-money", prizeMoney);

		WebElement flucs = infoContainer.findElement(By.xpath("//div[@class='odds-switch noselect'][1]/span[2]"));
		Actions actions = new Actions(driver);
		actions.moveToElement(flucs).click().perform();

		List<WebElement> runners = driver.findElements(By.xpath(
				"//table[@class='sort table-form-view race-runners ng-scope' or @class='sort table-form-view ng-scope no-winners' or @class='sort table-form-view ng-scope' ]/tbody/tr"));
		for (WebElement runner : runners) {
			Element run = getRunner(runner);
			race.appendChild(run);
		}

		return race;
	}

	private Element getRunner(WebElement runner) {

		Element run = new Element("run");

		String position = runner.findElement(By.xpath("./td[@class='td-position tcenter']/span")).getText().trim();
		position = position.replaceFirst("(st|nd|rd|th)", "");
		run.attr("position", position);

		WebElement horseDetails = runner.findElement(By.xpath("./td[@class='td-horse tleft']/table/tbody"));
		String number = horseDetails.findElement(By.xpath("./tr[1]/td[@class='horse-name']/h3/a/span[1]")).getText()
				.trim();
		number = number.replaceFirst("\\.", "");
		run.attr("number", number);

		String name = horseDetails.findElement(By.xpath("./tr[1]/td[@class='horse-name']/h3/a/span[2]")).getText()
				.trim();
		if (name.contains("(")) {
			String[] splitName = name.split("\\(");
			name = splitName[0].trim();
			String nationality = splitName[1].replaceAll("\\)", "");
			run.attr("name", name);
			run.attr("nationality", nationality);
		} else {
			run.attr("name", name);
		}

		String barrier = horseDetails.findElement(By.xpath("./tr[1]/td[@class='horse-name']/h3/a/span[3]")).getText()
				.trim().replaceAll("[\\()]", "");
		run.attr("barrier", barrier);
		String trainer = horseDetails.findElement(By.xpath("./tr[2]/td[@class='horse-details']/span[1]/a")).getText()
				.trim();
		run.attr("trainer", trainer);
		String jockey = horseDetails.findElement(By.xpath("./tr[2]/td[@class='horse-details']/span[2]/a")).getText()
				.trim();
		run.attr("jockey", jockey);
		String silks = horseDetails.findElement(By.xpath("./tr[1]/td[@class='horse-silk']/img")).getAttribute("src");
		String[] splitSilks = silks.split("/");
		silks = splitSilks[splitSilks.length - 1];
		run.attr("silks", silks);
		String weight = runner.findElement(By.xpath("./td[@class='td-weight tcenter']/p")).getText().trim()
				.replaceFirst("kg", "");
		run.attr("weight", weight);
		String margin = runner.findElement(By.xpath("./td[@class='td-margin tcenter']/span/span")).getText().trim();
		if (margin.contains("L"))
			margin = margin.replaceFirst("L", "");
		else
			margin = "0.0";
		run.attr("margin", margin);
		String comment = runner.findElement(By.xpath("./td[@class='td-expert-opinion tcenter']/span"))
				.getAttribute("data-content");
		run.attr("comment", comment);
		String startingPrice = runner.findElement(By.xpath("./td[@class='td-odds tcenter'][1]/div/span[1]")).getText()
				.trim().replaceFirst("\\$", "");
		run.attr("starting-price", startingPrice);
		String openingPrice = runner.findElement(By.xpath("./td[@class='td-odds tcenter'][2]/div/span[1]")).getText()
				.trim().replaceAll("[\\s+OP\\$]", "");
		run.attr("opening-price", openingPrice);
		String topFluc = runner.findElement(By.xpath("./td[@class='td-odds tcenter'][2]/div/span[2]")).getText().trim()
				.replaceAll("[\\s+TF\\$]", "");
		run.attr("top-fluc", topFluc);

		ArrayList<Element> gearElements = getGear(runner);
		if (gearElements != null) {
			for (Element gear : gearElements)
				run.appendChild(gear);
		}

		return run;
	}

	private ArrayList<Element> getGear(WebElement runner) {

		driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

		JavascriptExecutor jse = (JavascriptExecutor) driver;
		jse.executeScript("arguments[0].scrollIntoView();", runner);
		jse.executeScript("window.scrollBy(0,-250)", "");

		WebElement gearCell = runner.findElement(By.xpath("./td[@class='td-gears tcenter']"));
		String numGear = gearCell.findElement(By.xpath("./p/span[@class='number']")).getText().trim();

		if (numGear.equals("0")) {
			return null;
		} else {
			Pattern p = Pattern.compile("\\d+\\.\\s+");

			Actions actions = new Actions(driver);
			actions.moveToElement(gearCell).build().perform();

			ArrayList<Element> gearElements = new ArrayList<Element>();

			List<WebElement> gearItems = gearCell.findElements(By.xpath(".//ul[1]/li/span/span"));
			for (WebElement gearItem : gearItems) {
				String gearString = gearItem.getText().trim();
				Matcher m = p.matcher(gearString);
				if (m.find()) {
					gearString = gearString.replaceFirst("\\d+\\.\\s*", "");
					Element gear = new Element("gear");
					gear.attr("item", gearString);
					gear.attr("change", "false");
					gearElements.add(gear);
				}
			}

			try {
				gearItems = gearCell.findElements(By.xpath(".//ul[2]/li/strong"));
				for (WebElement gearItem : gearItems) {
					String gearString = gearItem.getText().trim();
					gearString = gearString.replaceFirst("\\d+\\.\\s*", "");
					Element gear = new Element("gear");
					gear.attr("item", gearString);
					gear.attr("change", "true");
					gearElements.add(gear);
				}
			} catch (NoSuchElementException e) {
				// do nothing
			}
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			return gearElements;
		}

	}

	private Element getStewards() {

		Element stewardsMeeting = new Element("stewards-meeting");
		WebElement stewards = driver.findElement(By.xpath("//ul[@class='nav-tabs'][1]/li[2]/a"));
		Actions actions = new Actions(driver);
		actions.moveToElement(stewards).click().perform();
		List<WebElement> stewardsReports = driver.findElements(By.xpath(
				"//div[@ng-bind-html='content']/div/p[b[span] and span and count(descendant::*)=3 and name(./*[1])='b']"));
		for (WebElement report : stewardsReports) {
			Element horse = new Element("horse");
			String name = report.findElement(By.xpath("./b")).getText();
			name = name.replaceAll("\u2011", "-");
			name = name.replaceAll("\\s*[\\r\\n]+\\s*", " ");
			String[] splitName = name.split("\\(");
			name = splitName[0].trim();
			String comment = report.findElement(By.xpath("./span")).getText();
			comment = comment.replaceAll("\u2011", "-");
			comment = comment.replaceAll("\\s*[\\r\\n]+\\s*", " ").trim();
			horse.attr("name", name);
			horse.attr("comment", comment);
			horse.appendTo(stewardsMeeting);
		}
		return stewardsMeeting;
	}

	private String getTime(String time) {

		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);

		if (time.contains(":")) {
			String[] splitTime = time.split(":");
			Double tm = new Double(splitTime[0]) * 60.0;
			tm += new Double(splitTime[1]);
			time = nf.format(tm);
		}

		return time;
	}

	private void downloadImages() {

		try {
			Robot bot = new Robot();
			bot.delay(1000);
			bot.mouseMove(1340, 47);
			bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			bot.mouseRelease(InputEvent.BUTTON1_MASK);
			bot.delay(5000);
			bot.mouseMove(1080, 140);
			bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			bot.mouseRelease(InputEvent.BUTTON1_MASK);
			bot.delay(200);
			bot.mouseMove(1265, 88);
			bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			bot.mouseRelease(InputEvent.BUTTON1_MASK);
			bot.delay(5000);
			bot.mouseMove(400, 100);
			bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			bot.mouseRelease(InputEvent.BUTTON1_MASK);

		} catch (Exception e) {
		}
		;
	}

	private void setUp(String downloadPath) {
		setUpDriver(downloadPath);
		setImageDownloaderOptions();
		setDownloaderPopup();
		closeExtensionTab();
		checkGoogle();
	}

	private void checkGoogle() {
		driver.get("https://www.google.com.au/");
		List<WebElement> input = driver.findElements(By.xpath("//input[@name='btnK']"));
		if (input.size() == 0) {
			logger.fatal("Could not navigate to Google, exiting");
			System.exit(0);
		}

	}

	private void closeExtensionTab() {

		ArrayList<String> tabs2 = new ArrayList<String>(driver.getWindowHandles());
		driver.switchTo().window(tabs2.get(1));
		driver.close();
		driver.switchTo().window(tabs2.get(0));
	}

	private void setUpDriver(String downloadPath) {

		System.setProperty("webdriver.chrome.driver", "C:/Users/Workventures/chromedriver/chromedriver.exe");
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--start-maximized");
		options.addArguments("disable-infobars");
		options.addExtensions(new File("resources/extensions/Image-Downloader_v2.4.2.crx"));
		options.addArguments("user-data-dir=C:/Users/mark/AppData/Local/Google/Chrome/User Data");
		Map<String, Object> preferences = new Hashtable<String, Object>();
		preferences.put("profile.default_content_settings.popups", 0);
		preferences.put("download.prompt_for_download", "false");
		preferences.put("download.default_directory", downloadPath);
		preferences.put("plugins.plugins_disabled", new String[] { "Adobe Flash Player", "Chrome PDF Viewer" });
		preferences.put("profile.default_content_setting_values.notifications", 2);
		options.setExperimentalOption("prefs", preferences);
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		driver = new ChromeDriver(capabilities);
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

	}

	private void setImageDownloaderOptions() {
		driver.navigate().to("chrome-extension://cnpniohnfphhjihaiiggeabnkjhpaldj/views/options.html");
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		jse.executeScript("document.getElementById('show_download_confirmation_checkbox').checked = false;");
		jse.executeScript("document.getElementById('show_download_notification_checkbox').checked = true;");
		jse.executeScript("document.getElementById('show_image_width_filter_checkbox').checked = false;");
		jse.executeScript("document.getElementById('show_image_height_filter_checkbox').checked = false;");
		jse.executeScript("document.getElementById('show_url_filter_checkbox').checked = true;");
		jse.executeScript("document.getElementById('show_only_images_from_links_checkbox').checked = false;");
		jse.executeScript("document.getElementById('show_open_image_button_checkbox').checked = true;");
		jse.executeScript("document.getElementById('show_download_image_button_checkbox').checked = true;");
		jse.executeScript("document.getElementById('columns_numberbox').value='2';");
		jse.executeScript("document.getElementById('image_min_width_numberbox').value='150';");
		jse.executeScript("document.getElementById('image_max_width_numberbox').value='150';");
		jse.executeScript("document.getElementById('image_border_width_numberbox').value='3';");
		jse.executeScript("document.getElementById('save_button').click();");
		Robot bot = null;
		try {
			bot = new Robot();
			bot.delay(1000);
			bot.mouseMove(1260, 190);
			bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			bot.mouseRelease(InputEvent.BUTTON1_MASK);
		} catch (Exception e) {
		}
		;
	}

	private void setDownloaderPopup() {

		driver.navigate().to("chrome-extension://cnpniohnfphhjihaiiggeabnkjhpaldj/views/popup.html");
		WebElement url = driver.findElement(By.id("filter_textbox"));
		url.clear();
		url.sendKeys("/\\d+.\\.png");
		driver.findElement(By.id("filter_url_mode_input")).click();
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
		}
		;
		driver.findElement(By.xpath(".//option[@value='regex']")).click();

		driver.findElement(By.id("toggle_all_checkbox")).click();
		driver.findElement(By.id("download_button")).click();
	}
}
