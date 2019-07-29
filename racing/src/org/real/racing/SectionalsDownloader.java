package org.real.racing;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Properties;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Logger;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.nodes.Element;

public class SectionalsDownloader {

	final static Logger logger = Logger.getLogger(SectionalsDownloader.class);
	private Properties headers;

	public SectionalsDownloader(Properties headers, Properties log4j) {
		this.headers = headers;
		PropertyConfigurator.configure(log4j);
		
	}

	public Element getMeeting(String url, Integer numRaces) {

		logger.info("Entering Sydney sectionals getMeeting");
		Element meeting = new Element("sectional-meeting");

		for (int i = 1; i <= numRaces; i++) {
			String num = new Integer(i).toString();
			num += ".";
			url = url.replaceFirst("\\d+\\.", num);
			
			Element race = getRace(url);
			race.appendTo(meeting);
		}

		return meeting;
	}

	private Element getRace(String urlString) {
		Element race = new Element("race");
		byte[] content = getContent(urlString);
		ArrayList<String> pages = processPDF(content);
		String raceNumber = getRaceNumber(pages.get(0));
		Integer raceDistance = getRaceDistance(pages.get(0));
		race.attr("number", raceNumber);
		ArrayList<String> raceLines = mergePages(pages);

		String winner = raceLines.get(0);
		Pattern p = Pattern.compile("^\\d+m\\d+:\\d+\\.\\d+:\\d+\\.\\d{2}(\\d:\\d+\\.\\d{2})\\s+");
		Matcher m = p.matcher(winner);
		if (m.find()) {
			Double raceTime = getTime(m.group(1));
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(2);
			nf.setMinimumFractionDigits(2);
			race.attr("time", nf.format(raceTime));
		} else {
			String msg = "Could not find race time in page\n";
			msg += winner;
			logger.fatal(msg);
			System.exit(0);
			return null;
		}

		for (String line : raceLines) {
			Element horse = getHorse(line, raceDistance);
			horse.appendTo(race);
		}

		return race;
	}

	private Element getHorse(String line, Integer dist) {

		Element horse = new Element("horse");

		Pattern p = Pattern.compile(
				"^(\\d+)m\\d+:\\d+\\.\\d+:\\d+\\.\\d{2}(.*])\\d+:\\d+\\.\\d{2}(\\d{2}\\.\\d)\\d+([A-Z' ]+)\\d+$");
		Matcher m = p.matcher(line);
		m.find();
		String fastestDistance = m.group(1);
		String sectionals = m.group(2);
		String fastestSpeed = m.group(3);
		String name = m.group(4);

		horse.attr("name", name);
		horse.attr("peak-distance", fastestDistance);
		horse.attr("peak-speed", fastestSpeed);

		ArrayList<Element> sects = getSectionals(sectionals, dist);
		for (Element s : sects)
			s.appendTo(horse);

		return horse;
	}

	private ArrayList<Element> getSectionals(String line, Integer dist) {

		ArrayList<Element> sectionals = new ArrayList<Element>();
		ArrayList<String> timeStrings = new ArrayList<String>();
		ArrayList<Double> baseTimes = new ArrayList<Double>();
		ArrayList<Double> sectTimes = new ArrayList<Double>();
		ArrayList<String> positions = new ArrayList<String>();

		String[] timePos = line.split("]");
		for (String tp : timePos) {
			String[] sp = tp.split("\\s*\\[");
			timeStrings.add(sp[0]);
			positions.add(sp[1]);
		}

		for (String t : timeStrings) {
			Double tm = getTime(t);
			baseTimes.add(tm);
		}

		Double raceTime = baseTimes.get(0);
		for (int i = 1; i < baseTimes.size(); i++) {
			Double sect = raceTime - baseTimes.get(i);
			sectTimes.add(sect);
		}
		sectTimes.add(raceTime);

		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		for (int i = 0; i < baseTimes.size(); i++) {
			Element sectional = new Element("sectional");
			sectional.attr("time", nf.format(sectTimes.get(i)));
			sectional.attr("position", positions.get(i));
			Integer d = new Integer((i + 1) * 200);
			if (d <= dist) {
				sectional.attr("distance", d.toString());
				sectionals.add(sectional);
			}
		}

		for (int i = 0; i < sectionals.size() / 2; i++) {
			Element temp = sectionals.get(i);
			sectionals.set(i, sectionals.get(sectionals.size() - 1 - i));
			sectionals.set(sectionals.size() - 1 - i, temp);
		}
		return sectionals;
	}

	Double getTime(String tm) {

		String[] splitTime = tm.split(":");
		Double time = new Double(splitTime[0]) * 60.0;
		time += new Double(splitTime[1]);

		return time;

	}

	private ArrayList<String> mergePages(ArrayList<String> pages) {

		boolean isLargeField;

		if (pages.size() == 1) {
			isLargeField = false;
		} else if (pages.size() == 2) {
			isLargeField = checkLargeField(pages.get(0), pages.get(1));
		} else if (pages.size() == 3) {
			isLargeField = false;
		} else {
			isLargeField = true;
		}

		ArrayList<ArrayList<String>> splitPages = new ArrayList<ArrayList<String>>();
		for (String page : pages) {
			ArrayList<String> splitPage = trimReverse(page);
			splitPages.add(splitPage);

		}

		if (isLargeField) {
			ArrayList<ArrayList<String>> tempSplits = new ArrayList<ArrayList<String>>();
			for (int i = 0; i < splitPages.size(); i += 2) {
				ArrayList<String> temp = new ArrayList<String>();
				temp.addAll(splitPages.get(i));
				temp.addAll(splitPages.get(i + 1));
				tempSplits.add(temp);
			}
			splitPages = tempSplits;
		}

		ArrayList<String> mergedPage;
		if (splitPages.size() == 1)
			mergedPage = splitPages.get(0);
		else
			mergedPage = mergeDistances(splitPages);

		return mergedPage;
	}

	private ArrayList<String> mergeDistances(ArrayList<ArrayList<String>> splits) {

		int numRunners = splits.get(0).size();
		int numDistances = splits.size();
		ArrayList<String> mergedPage = new ArrayList<String>();
		for (int i = 0; i < numRunners; i++) {
			String mergedLine = "";
			for (int j = numDistances - 1; j >= 0; j--) {
				String s = splits.get(j).get(i);
				if (j < (numDistances - 1))
					s = removeLeft(s);
				if (j > 0)
					s = removeRight(s);

				mergedLine += s;
			}
			mergedPage.add(mergedLine);
		}
		return mergedPage;
	}

	private String removeRight(String s) {
		String[] splits = s.split("]");
		String str = "";
		for (int i = 0; i < splits.length - 1; i++) {
			str += splits[i];
			str += "]";
		}
		return str;
	}

	private String removeLeft(String s) {
		Pattern p = Pattern.compile("\\s*(\\d+m\\d+:\\d+\\.\\d{2,2}).*");
		Matcher m = p.matcher(s);
		m.find();
		String leftStr = m.group(1);
		int leftSz = leftStr.length();
		s = s.substring(leftSz, s.length());
		return s;
	}

	private ArrayList<String> trimReverse(String page) {

		String[] linesArray = page.split("[\\r\\n]+");
		ArrayList<String> doubleLines = new ArrayList<String>();
		boolean inSectionals = false;
		for (String line : linesArray) {
			if (line.startsWith("Last"))
				inSectionals = false;
			if (inSectionals)
				doubleLines.add(line.trim());
			if (line.startsWith("Track Rating:"))
				inSectionals = true;
		}
		ArrayList<String> lines = new ArrayList<String>();
		String fastestDistance = "";
		for (int i = 0; i < doubleLines.size(); i++) {
			if (i % 2 == 0) {
				String[] splits = doubleLines.get(i).split("\\)");
				String split = splits[splits.length - 1];
				splits = split.split("m");
				fastestDistance = splits[0];
				fastestDistance += "m";
			} else {
				String line = fastestDistance + doubleLines.get(i);
				lines.add(line);
			}
		}

		for (int i = 0; i < (lines.size() / 2); i++) {
			String temp = lines.get(i);
			lines.set(i, lines.get(lines.size() - 1 - i));
			lines.set(lines.size() - 1 - i, temp);
		}
		return lines;
	}

	private boolean checkLargeField(String page1, String page2) {
		System.out.println(page1);
		System.out.println(page2);
		Pattern p = Pattern.compile("([0-9mFnish]+)Distance\\s+To\\s+Go");
		Matcher m = p.matcher(page1);
		String distances1;
		m.find();
		distances1 = m.group(1);
		m = p.matcher(page2);
		String distances2;
		m.find();
		distances2 = m.group(1);
		System.out.println(distances1);
		System.out.println(distances2);
		return distances1.equals(distances2);
	}

	private String getRaceNumber(String s) {

		Pattern p = Pattern.compile("Race\\s+(\\d+):");
		Matcher m = p.matcher(s);
		if (m.find()) {
			return m.group(1);
		} else {
			String msg = "Could not find race number in page\n";
			msg += s;
			logger.fatal(msg);
			System.exit(0);
			return null;
		}
	}

	private Integer getRaceDistance(String s) {

		Pattern p = Pattern.compile("Race\\s+[0-9.]+:.*\\s(\\d+)m");
		Matcher m = p.matcher(s);
		if (m.find()) {
			return new Integer(m.group(1));

		} else {
			String msg = "Could not find race distance in page\n";
			msg += s;
			logger.fatal(msg);
			System.exit(0);
			return null;
		}
	}

	private ArrayList<String> processPDF(byte[] b) {

		try {

			RandomAccessBuffer buf = new RandomAccessBuffer(b);
			PDFParser parser = new PDFParser(buf);
			parser.parse();
			COSDocument cosDoc = parser.getDocument();
			PDFTextStripper reader = new PDFTextStripper();
			PDDocument doc = new PDDocument(cosDoc);
			int numPages = doc.getNumberOfPages();
			ArrayList<String> pages = new ArrayList<String>();
			for (int i = 1; i <= numPages; i++) {
				reader.setStartPage(i);
				reader.setEndPage(i);
				pages.add(reader.getText(doc));
			}
			doc.close();
			return pages;
		} catch (InvalidPasswordException e) {
			String msg = "InvalidPasswordException processing sectionals pdf\n";
			msg += e.getMessage();
			msg += "\n";
			msg += getStackTrace(e);
			logger.fatal(msg);
			System.exit(0);
			return null;

		} catch (IOException e) {
			String msg = "IOException processing stewards pdf\\n";
			msg += e.getMessage();
			msg += "\n";
			msg += getStackTrace(e);
			logger.fatal(msg);
			System.exit(0);
			return null;
		}
	}

	private byte[] getContent(String urlString) {

		try {
			URL url = new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod("GET");
			connection.setRequestProperty("User-Agent", headers.getProperty("User-Agent"));
			connection.setRequestProperty("DNT", headers.getProperty("DNT"));
			connection.setRequestProperty("Upgrade-Insecure-Requests",
					headers.getProperty("Upgrade-Insecure-Requests"));
			connection.setRequestProperty("Accept", headers.getProperty("Accept"));
			connection.setReadTimeout(15 * 1000);
			connection.connect();
			int responseCode = connection.getResponseCode();
			if (responseCode >= 400) {
				String msg = "Could not reach site. Response code " + responseCode + ". With url string\n";
				msg += urlString;
				logger.fatal(msg);
				System.exit(0);
			}

			int byteRead = 0;
			byte[] buf = new byte[1024];
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			InputStream is = url.openStream();
			while ((byteRead = is.read(buf)) != -1) {
				bos.write(buf, 0, byteRead);
			}
			byte[] content = bos.toByteArray();

			return content;

		} catch (MalformedURLException e) {
			String msg = "MalformedURLException with url string\n";
			msg += urlString;
			msg += "\n";
			msg += e.getMessage();
			msg += "\n";
			msg += getStackTrace(e);
			logger.fatal(msg);
			System.exit(0);
			return null;
		} catch (ProtocolException e) {
			String msg = "ProtocolException with url string\n";
			msg += urlString;
			msg += "\n";
			msg += e.getMessage();
			msg += "\n";
			msg += getStackTrace(e);
			logger.fatal(msg);
			System.exit(0);
			return null;
		} catch (IOException e) {
			String msg = "IOException with url string\n";
			msg += urlString;
			msg += "\n";
			msg += e.getMessage();
			msg += "\n";
			msg += getStackTrace(e);
			logger.fatal(msg);
			System.exit(0);
			return null;
		}
	}

	private String getStackTrace(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String st = sw.toString();
		return st;
	}

}
