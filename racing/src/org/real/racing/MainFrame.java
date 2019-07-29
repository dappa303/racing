package org.real.racing;

import java.awt.EventQueue;
import javax.swing.JFrame;
import org.jdatepicker.JDatePicker;
import com.mongodb.MongoClient;
import org.bson.types.ObjectId;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.query.Query;
import org.real.racing.domain.Track;
import org.real.racing.domain.Speedmap;
import java.util.List;
import java.util.Vector;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JComboBox;
import java.awt.Color;
import javax.swing.border.LineBorder;
import java.awt.SystemColor;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.JButton;
import javax.swing.JTextPane;
import java.text.SimpleDateFormat;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainFrame {

	private JFrame frame;
	private JDatePicker datePicker;
	private JComboBox<String> comboTracks;
	private JButton btnGo;
	private JRadioButton rdbtnSpeedmaps;
	private JRadioButton rdbtnDownload;
	private JRadioButton rdbtnLoad;
	private JSpinner spnNumRaces;
	private JSpinner spnStewards;
	private JTextPane textPaneMsg;
	private Morphia morphia;
	private Datastore datastore;
	private List<Track> tracks;
	private Track track;
	private Date date;
	private boolean isSelection;
	private SimpleDateFormat format;
	private Properties urls;
	private Properties headers;
	private Properties paths;
	private SpeedmapDownloader speedmapper;
	private MeetingDownloadController downloader;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame window = new MainFrame();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainFrame() {
		isSelection = false;
		format = new SimpleDateFormat("EEE, dd MMM yyyy");
		morphia = new Morphia();
		datastore = morphia.createDatastore(new MongoClient(), "racing");
		getTracks();
		getProperties();
		System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 541);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
	
		
		datePicker = new JDatePicker(new Date());
		datePicker.getFormattedTextField().setBorder(new LineBorder(new Color(171, 173, 179)));
		datePicker.getFormattedTextField().setBackground(new Color(240, 240, 240));
		datePicker.getFormattedTextField().setText("PICK A DATE");
		datePicker.getJDateInstantPanel().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				date =  (Date) datePicker.getModel().getValue();
				if(date == null)
					datePicker.getFormattedTextField().setText("PICK A DATE");
				if(date != null && track != null) {
					isSelection = true;
					btnGo.setEnabled(true);
					showSelected();
				}
				else {
					textPaneMsg.setText("");
					isSelection = false;
					btnGo.setEnabled(false);
				}
			}
			
		});
		datePicker.setBounds(20, 250, 404, 22);
		frame.getContentPane().add(datePicker);
		
		JLabel lblTrack = new JLabel("DATE");
		lblTrack.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblTrack.setLabelFor(datePicker);
		lblTrack.setBounds(20, 218, 50, 21);
		frame.getContentPane().add(lblTrack);
		
		JLabel lblDate = new JLabel("TRACK");
		lblDate.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblDate.setBounds(22, 283, 60, 14);
		frame.getContentPane().add(lblDate);
		
		Vector<String> trackNames = new Vector<String>();
		for(Track track : tracks)
			trackNames.add(track.getRcom());
		
		comboTracks = new JComboBox<String>(trackNames);
		comboTracks.setSelectedIndex(-1);
		comboTracks.setBounds(20, 307, 404, 20);
		comboTracks.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String rcom = (String) comboTracks.getSelectedItem();
				for(Track trk : tracks) {
					if(trk.getRcom().equals(rcom)) {
						track = trk;
						break;
					}
				}
				if(date != null && track != null) {
					isSelection = true;
					btnGo.setEnabled(true);
					showSelected();
				}
				else {
					textPaneMsg.setText("");
					isSelection = false;
					btnGo.setEnabled(false);
				}
			}
		});
		frame.getContentPane().add(comboTracks);
		
		rdbtnSpeedmaps = new JRadioButton("Speedmaps");
		rdbtnSpeedmaps.setBounds(20, 19, 109, 23);
		rdbtnSpeedmaps.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(isSelection)
					showSelected();
			}
		});
		frame.getContentPane().add(rdbtnSpeedmaps);
		
		rdbtnDownload = new JRadioButton("Download Results");
		rdbtnDownload.setBounds(20, 73, 147, 23);
		rdbtnDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(isSelection)
					showSelected();
			}
		});
		frame.getContentPane().add(rdbtnDownload);
		
		rdbtnLoad = new JRadioButton("Load Results");
		rdbtnLoad.setBounds(20, 155, 109, 23);
		rdbtnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(isSelection)
					showSelected();
			}
		});
		frame.getContentPane().add(rdbtnLoad);
		
		ButtonGroup btnGrp = new ButtonGroup();
		btnGrp.add(rdbtnSpeedmaps);
		btnGrp.add(rdbtnDownload);
		btnGrp.add(rdbtnLoad);
		rdbtnSpeedmaps.setSelected(true);
		
		String [] versions = {"3","2","1","none"};
		SpinnerModel modelStewards = new SpinnerListModel(versions);
		spnStewards = new JSpinner(modelStewards);
		spnStewards.setValue("none");
		spnStewards.setBounds(41, 115, 50, 20);
		spnStewards.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if(isSelection)
					showSelected();
			}
		});
		frame.getContentPane().add(spnStewards);
		
		Integer [] numRaces = {12,11,10,9,8,7,6,5,4,3,2,1};
		SpinnerModel modelNumber = new SpinnerListModel(Arrays.asList(numRaces));
		spnNumRaces = new JSpinner(modelNumber);
		spnNumRaces.setValue(9);
		spnNumRaces.setBounds(138, 115, 44, 20);
		spnNumRaces.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if(isSelection)
					showSelected();
			}
		});
		frame.getContentPane().add(spnNumRaces);
		
		JButton btnQuit = new JButton("Quit");
		btnQuit.setFont(new Font("Tahoma", Font.BOLD, 18));
		btnQuit.setBounds(22, 443, 121, 37);
		btnQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		frame.getContentPane().add(btnQuit);
		
		btnGo = new JButton("Go");
		btnGo.setFont(new Font("Tahoma", Font.BOLD, 18));
		btnGo.setBounds(315, 443, 109, 37);
		btnGo.setEnabled(false);
		btnGo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnGo.setEnabled(false);
				if(rdbtnSpeedmaps.isSelected()) {
					if(speedmapper == null)
						speedmapper = new SpeedmapDownloader(urls.getProperty("urlrnet"),paths.getProperty("xsltSpeedmaps"));
					Speedmap speedmap = speedmapper.getSpeedmaps(track, date);
					System.out.println(speedmap.toString());
					loadSpeedmap(speedmap);
				}
				else if(rdbtnDownload.isSelected()) {
					if(downloader == null)
						downloader = new MeetingDownloadController();
					downloader.process(track, date, (Integer) spnNumRaces.getValue());
					
				}
				setDefault();
			}
		});
		frame.getContentPane().add(btnGo);
		
		textPaneMsg = new JTextPane();
		textPaneMsg.setBounds(20, 348, 404, 82);
		frame.getContentPane().add(textPaneMsg);
		

	}
	
	private void loadSpeedmap(Speedmap speedmap) {
		datastore.save(speedmap);
	}
	
	private void showSelected() {
		
		String msg = "";
		if(rdbtnSpeedmaps.isSelected()) {
			msg += "Speedmap\n";
		}
		else if(rdbtnDownload.isSelected()) {
			msg += "Download results\n";
		}
		else {
			msg += "Upload results\n";
		}
		msg += track.getName();
		msg += "\n";
		if(rdbtnDownload.isSelected() && track.getState().equals("NSW")) {
			msg += "No. races: ";
			msg += spnNumRaces.getValue();
			msg += "  Stewards version: ";
			msg += spnStewards.getValue();
			msg += "\n";
		}
		msg += format.format(date);
		textPaneMsg.setText(msg);
	}
	
	private void setDefault() {
		rdbtnSpeedmaps.setSelected(true);
		spnNumRaces.setValue(9);
		spnStewards.setValue("none");
		comboTracks.setSelectedIndex(-1);
		btnGo.setEnabled(false);
		datePicker.getFormattedTextField().setText("PICK A DATE");
		textPaneMsg.setText("");
		date = null;
		track =  null;
		isSelection = false;
	}
	
	private void getTracks() {
		
		morphia.mapPackage("org.real.racing.domain");
		Query<Track> query = datastore.find(Track.class);
		tracks = query.asList();
		
	}
	
	private void getProperties() {
		FileInputStream fin = null;
		
		try {
			fin = new FileInputStream("resources/properties/urls.properties");
		}
		catch(FileNotFoundException e) {
			System.out.println("NO FILE");
		}
		
		try {
			if(fin != null) {
				urls = new Properties();
				urls.load(fin);
				fin.close();
			}
		}
		catch(IOException e) {
			System.out.println("IO Exception");
		}
		
		fin = null;
		
		try {
			fin = new FileInputStream("resources/properties/headers.properties");
		}
		catch(FileNotFoundException e) {
			System.out.println("NO FILE");
		}
		
		try {
			if(fin != null) {
				headers = new Properties();
				headers.load(fin);
				fin.close();
			}
		}
		catch(IOException e) {
			System.out.println("IO Exception");
		}
		
		fin = null;
		
		try {
			fin = new FileInputStream("resources/properties/paths.properties");
		}
		catch(FileNotFoundException e) {
			System.out.println("NO FILE");
		}
		
		try {
			if(fin != null) {
				paths = new Properties();
				paths.load(fin);
				fin.close();
			}
		}
		catch(IOException e) {
			System.out.println("IO Exception");
		}
	}
}
