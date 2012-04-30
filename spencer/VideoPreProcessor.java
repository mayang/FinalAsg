// This file should read all the video files and preprocess their data
// and put their atributes into an xml file per video

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.stream.*;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.Attribute;

import javax.swing.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;

public class VideoPreProcessor {

	File root; // root of where the videos are
	public static int[] byteIndicies; // keeps indexes where new frames start;
	public static byte[] bytes; // bytes from file
	public static byte[] soundBytes; // bytes from file
	public final int SOUND_INDEX = 7357;
	private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb
	AudioInputStream audioInputStream;
	public static List<String> fileNames; // names of files
	public static Map<String, byte[]> fileBytes; // bytes of the files
	
	// constructor
	public VideoPreProcessor(String folderpath, int[] bi) {
		System.out.println("Video PreProcessor called");
		root = new File(folderpath);
		fileNames = new ArrayList<String>();
		fileBytes = new HashMap<String, byte[]>();
		// get where frames start
    	byteIndicies = bi;
	}
	
	// goes through directories, finds files and processes them
	public void fileTraverse() {
		System.out.println("down all the files");
		try {
			try {
				fileRecurse(root);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void fileRecurse(File curr) throws XMLStreamException, FileNotFoundException {
		if (curr.isDirectory()) { 
			System.out.println(curr.getName());
			File contents[] = curr.listFiles();
			if (contents.length == 2 && contents[0].isFile() && contents[1].isFile()) {
				// TODO This is a "video!" preprocess this
				fileNames.add(curr.getName());
				processVideo(contents[0], contents[1]);
			} else {
				// recurse through other folders
				for (File f : contents)  { 
					fileRecurse(f);
				}
			}
		} 
	}
	
	public List<String> getFileNames() {
		return fileNames;
	}

	public byte[] getFileBytes(String key) {
		return fileBytes.get(key);
	}
	
	// reads in this video file
	private void readinFile(File video) throws IOException {
	    InputStream is;
		is = new FileInputStream(video);
		
	    long len = video.length();
	    bytes = new byte[(int) len];
	    
//	    System.out.println("file length:"+ len);
	    
	    int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
        	offset += numRead;
        }
        
        //String key = (video.getName()).substring(0, video.getName().indexOf("."));
//        System.out.println(key);
//        fileBytes.put(key, bytes);
	}
	
	//reads in sound file
		private void readinSoundFile(File sound) throws IOException {
			
			InputStream sis = null;
			try {
				sis = new FileInputStream(sound);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			long slen = sound.length();
	    
			soundBytes = new byte[(int) slen];
	    
//	   		 System.out.println("file length:"+ len);
			audioInputStream = null;
			try {
		    audioInputStream = AudioSystem.getAudioInputStream(sis);
			} catch (UnsupportedAudioFileException e1) {
		    new PlayWaveException(e1);
			} catch (IOException e1) {
		    new PlayWaveException(e1);
			}

					AudioFormat audioFormat = audioInputStream.getFormat();
					Info info = new Info(SourceDataLine.class, audioFormat);
					System.out.println("hi");
					int available = 0;
					System.out.println(available);
					System.out.println("bye");
					// opens the audio channel
					
					SourceDataLine dataLine = null;
					dataLine = null;
					try {
					    dataLine = (SourceDataLine) AudioSystem.getLine(info);
					    dataLine.open(audioFormat, EXTERNAL_BUFFER_SIZE);
					} catch (LineUnavailableException e1) {
					    new PlayWaveException(e1);
					}
					
		dataLine.start();
		}
	// process the video!
	private void processVideo(File video, File audio) throws XMLStreamException, FileNotFoundException {
		if (!(video.getName().endsWith(".rgb") || audio.getName().endsWith(".wav"))) {
			System.out.println("these files aren't valid!");
			System.exit(1);
		}
		// read in the video file
		try {
			readinFile(video);
			readinSoundFile(audio);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		String xml_name = video.getName().substring(0, video.getName().indexOf("."));
		xml_name += ".xml";
		// generate xml file
		XMLOutputFactory of = XMLOutputFactory.newInstance();
		XMLEventWriter ew = null;
		ew = of.createXMLEventWriter(new FileOutputStream(xml_name));

		XMLEventFactory ef = XMLEventFactory.newInstance();
		XMLEvent end = ef.createDTD("\n");
		XMLEvent tab = ef.createDTD("\t");
		
		// start tag
		StartDocument startDoc = ef.createStartDocument();
		ew.add(startDoc);
		ew.add(end);
		
		// <video>
		StartElement vidElem = ef.createStartElement("", "", "video");
		ew.add(vidElem);
		ew.add(end);

		// for each frame <frame no=i> </frame>
		for (int i = 0; i < 720; ++i) {
			ew.add(tab);
			Attribute frameNo = ef.createAttribute("no", Integer.toString(i));
			List<Attribute> attrList = Arrays.asList(frameNo);
			List<Object> nsList = Arrays.asList();
			StartElement frameElem = ef.createStartElement("", "", "frame",  attrList.iterator(), nsList.iterator());
			ew.add(frameElem);
			ew.add(end);
			// TODO: video descriptors
			extractVidDesc(ew, i);
			ew.add(tab);
			ew.add(ef.createEndElement("", "", "frame"));
			ew.add(end);
		}
		
		// </video>
		ew.add(ef.createEndElement("", "", "video"));
		ew.add(end);
		
		// end document
		ew.add(ef.createEndDocument());
		ew.close();
	}
	
	// extracting descriptors from the video and putting it into the xml file
	private void extractVidDesc(XMLEventWriter ew, int frameNo) throws XMLStreamException {
		// make frame
		int ind = byteIndicies[frameNo];
		BufferedImage img = new BufferedImage(352, 288, BufferedImage.TYPE_INT_RGB);
		for(int y = 0; y < 288; y++){
			for(int x = 0; x < 352; x++){
				//System.out.println("i:" + ind);
				byte a = 0;
				byte r = bytes[ind];
				byte g = bytes[ind+288*352];
				byte b = bytes[ind+288*352*2]; 
				
				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
				img.setRGB(x,y,pix);
				ind++;
			}
    	}
		//Future Frame for Motion Search
		
				int ind2 = byteIndicies[frameNo+5]; //look 10 frames into the future
				BufferedImage nextImg = new BufferedImage(352, 288, BufferedImage.TYPE_INT_RGB);
				for(int y = 0; y < 288; y++){
					for(int x = 0; x < 352; x++){
						//System.out.println("i:" + ind);
						byte a = 0;
						byte r = bytes[ind2];
						byte g = bytes[ind2+288*352];
						byte b = bytes[ind2+288*352*2]; 
						
						int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
						//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
						nextImg.setRGB(x,y,pix);
						ind2++;
					}
		    	}
				
				//Template image for motion search (take the 6th subimage, row-wise from the main image, 3 rows 4 cols)
				
				BufferedImage templateImg = img.getSubimage(96,88,96,88);
				/*
				int readBytes = 0;
				byte[] audioBuffer = new byte[EXTERNAL_BUFFER_SIZE];
				
				try {
					audioInputStream.skip((long) (frameNo*SOUND_INDEX));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try {
				    if (readBytes != -1) {
					readBytes = audioInputStream.read(audioBuffer, (frameNo*SOUND_INDEX),
						audioBuffer.length);
				    }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				
				XMLEventFactory ef = XMLEventFactory.newInstance();
				XMLEvent end = ef.createDTD("\n");
				XMLEvent tab = ef.createDTD("\t");
				// Get dominant color of image
				String color = extractColor(img);
				ew.add(tab);
				ew.add(tab);
				ew.add(ef.createStartElement("", "", "color"));
				Characters content = ef.createCharacters(color);
				ew.add(content);
				ew.add(ef.createEndElement("", "", "color"));
				ew.add(end);
				// Get relative motion
				String motion = extractMotion(templateImg,nextImg);
				ew.add(tab);
				ew.add(tab);
				ew.add(ef.createStartElement("", "", "motion"));
				Characters content2 = ef.createCharacters(motion);
				ew.add(content2);
				ew.add(ef.createEndElement("", "", "motion"));
				ew.add(end);
				// Get relative audio
				String audio = extractAudio(frameNo);
				ew.add(tab);
				ew.add(tab);
				ew.add(ef.createStartElement("", "", "audio"));
				Characters content3 = ef.createCharacters(audio);
				ew.add(content3);
				ew.add(ef.createEndElement("", "", "audio"));
				ew.add(end);
			
	
	}
	
	// extract dominant color of this frame ( or really just the hue)
	// shouldn't have to examine every pixel, could subsample?
	// use hues since that seems to be simple since it's one number, have to check saturation though
	// for black or white or whatever it is that tells if its black or white
	
	public String extractColor(BufferedImage frame) {
		String color = "";
		int redCount = 0, greenCount = 0, blueCount = 0;
		Map<String, Integer> colorHistogram = new HashMap<String, Integer>(); 
		for (int j = 0; j < frame.getHeight(); ++j) {
			for (int i = 0; i < frame.getWidth(); i += 4) { // subsample
				// get rgb value of this pixel
				int rgb = frame.getRGB(i, j);
				int r = (rgb & 0x00FF0000) >>> 16;
				int g = (rgb & 0x0000FF00) >>> 8;
				int b = (rgb & 0x000000FF);
				// convert to hue
				float hsb[] = null;
				hsb = Color.RGBtoHSB(r, g, b, null);
				//System.out.println(hsb[0]);
				// stick it in hue histogram
				// buckets [300-360, 0-60], [60-180], [180-300]
				// this is red
				if ((hsb[0] < 0.17) || (hsb[0] >= 0.83)) {
					//System.out.println("red pixel");
					colorHistogram.put("red", new Integer(++redCount));
				// this is green
				} else if (hsb[0] >= 0.17 && hsb[0] < 0.5) {
					//System.out.println("green pixel");
					colorHistogram.put("green", new Integer(++greenCount));
				// this is blue
				} else if (hsb[0] >= 0.5 && hsb[0] < 0.83) {
					//System.out.println("blue Pixel");
					colorHistogram.put("blue", new Integer(++blueCount));
				}
			}
		}
		// which color is dominant
		//int max = colorHistogram.get("red");
		//color = "red";
		Map.Entry<String, Integer> dominantColor = null;
		for (Map.Entry<String, Integer> e : colorHistogram.entrySet()) {
			if (dominantColor == null || e.getValue().compareTo(dominantColor.getValue()) > 0) {
				dominantColor = e;
			}
		}
		color = dominantColor.getKey();
		
		return color;
	}
	

	//returns a string with a direction and a velocity (upfast, downslow, leftmed, rightmed, etc.) using Template Matching
	//Sum of Absolute Differences
	private String extractMotion(BufferedImage templateFrame, BufferedImage futureFrame) {
		String motion = "";
		double minRedSAD = 100;
		double minGreenSAD = 100;
		double minBlueSAD = 100;
		double SAD = 0.0;
		double redSAD = 0.0;
		double greenSAD = 0.0;
		double blueSAD = 0.0;
		int bestRedRow = 0;
		int bestRedCol = 0;
		int bestBlueRow = 0;
		int bestBlueCol = 0;
		int bestGreenRow = 0;
		int bestGreenCol = 0;
		double bestRedSAD = 0.0;
		double bestBlueSAD = 0.0;
		double bestGreenSAD = 0.0;
		
		// loop through the search image
		for ( int x = 0; x <= 288 - 96; x++ ) {
		    for ( int y = 0; y <= 352 - 88; y++ ) {
		        redSAD = 0.0;
		        greenSAD = 0.0;
		        blueSAD = 0.0;
		 
		        // loop through the template image
		        for ( int i = 0; i < 96; i++ )
		            for ( int j = 0; j < 88; j++ ) {
		            	
		            	int color = futureFrame.getRGB(x+i, y+j);
		            	int  red = (color & 0x00ff0000) >> 16;
		            	//int  green = (color & 0x0000ff00) >> 8;
		            	//int  blue = color & 0x000000ff;
		            	
		            	int templateColor = templateFrame.getRGB(x+i, y+j);
		            	int  templateRed = (color & 0x00ff0000) >> 16;
		            	//int  templateGreen = (color & 0x0000ff00) >> 8;
		            	//int  templateBlue = color & 0x000000ff;
		            	
		                //int pixel1 p_SearchIMG = futureFrame[x+i][y+j];
		                //int pixel2 p_TemplateIMG = frame[i][j];
		 
		                redSAD += abs( (double)red - (double)templateRed );
		                //greenSAD += abs( (double)green - (double)templateGreen );
		                //blueSAD += abs( (double)blue - (double)templateBlue );
		            }
		 
		        // save the best found position 
		        if ( minRedSAD > redSAD ) { 
		            minRedSAD = redSAD;
		            // give me VALUE_MAX
		            bestRedRow = x;
		            bestRedCol = y;
		            bestRedSAD = redSAD;
		        }
		        /*
		        if ( minGreenSAD > greenSAD ) { 
		            minGreenSAD = greenSAD;
		            // give me VALUE_MAX
		            bestGreenRow = x;
		            bestGreenCol = y;
		            bestGreenSAD = greenSAD;
		        }
		        if ( minBlueSAD > blueSAD ) { 
		            minBlueSAD = blueSAD;
		            // give me VALUE_MAX
		            bestBlueRow = x;
		            bestBlueCol = y;
		            bestBlueSAD = blueSAD;
		        }
		        */
		        
		    }
		}
		
		System.out.println(bestRedRow);
		System.out.println(bestRedCol);
		
		//compute only red (gray) intensity for now
		
		if (bestRedRow<144-96 && bestRedCol <176-88){
			motion = "UpLeft";
		}
		else if (bestRedRow>144-96 && bestRedCol <176-88){
			motion = "DownLeft";
		}
		else if (bestRedRow<144-96 && bestRedCol >176-88){
			motion = "UpRight";
		}
		else if (bestRedRow>144-96 && bestRedCol >176-88){
			motion = "DownRight";
		}
		
		return motion;
	}
	
	//Returns a number 0-100
	private String extractAudio(int frameNo) {
		Integer audio = 0;
		int readBytes = 0;
		byte[] audioBuffer = new byte[EXTERNAL_BUFFER_SIZE];
		
		/*
		try {
			audioInputStream.skip((long) (frameNo*SOUND_INDEX));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		try {
		    if (readBytes != -1) {
			readBytes = audioInputStream.read(audioBuffer, (frameNo*SOUND_INDEX),
				audioBuffer.length);
			audio = calculateRMSLevel(audioBuffer);
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return audio.toString();
	}
	
	//Returns the Root Mean Square of the audio data at a given frame.
	public int calculateRMSLevel(byte[] audioData)
    { 
        long lSum = 0;
        for(int i=0; i<audioData.length; i++)
            lSum = lSum + audioData[i];
 
        double dAvg = lSum / audioData.length;
 
        double sumMeanSquare = 0d;
        for(int j=0; j<audioData.length; j++)
            sumMeanSquare = sumMeanSquare + Math.pow(audioData[j] - dAvg, 2d);
 
        double averageMeanSquare = sumMeanSquare / audioData.length;
        return (int)(Math.pow(averageMeanSquare,0.5d) + 0.5);
    }
	
	public class PlayWaveException extends Exception {

	    public PlayWaveException(String message) {
		super(message);
	    }

	    public PlayWaveException(Throwable cause) {
		super(cause);
	    }

	    public PlayWaveException(String message, Throwable cause) {
		super(message, cause);
	    }

	}
}
