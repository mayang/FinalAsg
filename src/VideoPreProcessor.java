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


public class VideoPreProcessor {

	File root; // root of where the videos are
	public static int[] byteIndicies; // keeps indexes where new frames start;
	public static byte[] bytes; // bytes from file
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
	
	
	// process the video!
	private void processVideo(File video, File audio) throws XMLStreamException, FileNotFoundException {
		if (!(video.getName().endsWith(".rgb") || audio.getName().endsWith(".wav"))) {
			System.out.println("these files aren't valid!");
			System.exit(1);
		}
		// read in the video file
		try {
			readinFile(video);
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

	
}
