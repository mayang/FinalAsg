// This file should read all the video files and preprocess their data
// and put their atributes into an xml file per video

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.*;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.Attribute;


public class VideoPreProcessor {

	File root; // root of where the videos are
	public static int[] byteIndicies; // keeps indexes where new frames start;
	
	// constructor
	public VideoPreProcessor(String folderpath) {
		System.out.println("Video PreProcessor called");
		root = new File(folderpath);
		
		// get where frames start
    	byteIndicies = new int[720];
    	for (int b = 0; b < 720; ++b) {
    		byteIndicies[b] = b * 304128;
    	}
		
	}
	
	// goes through directories, finds files and processes them
	public void fileTraverse() {
		System.out.println("down all the files");
		try {
			fileRecurse(root);
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void fileRecurse(File curr) throws XMLStreamException {
		if (curr.isDirectory()) { 
			System.out.println(curr.getName());
			File contents[] = curr.listFiles();
			if (contents.length == 2 && contents[0].isFile() && contents[1].isFile()) {
				// TODO This is a "video!" preprocess this
				processVideo(contents[0], contents[1]);
			} else {
				// recurse through other folders
				for (File f : contents)  { 
					fileRecurse(f);
				}
			}
		} 
	}
	
	// process the video!
	private void processVideo(File video, File audio) throws XMLStreamException {
		if (!(video.getName().endsWith(".rgb") || audio.getName().endsWith(".wav"))) {
			System.out.println("these files aren't valid!");
			System.exit(1);
		}
		
		String xml_name = video.getName().substring(0, video.getName().indexOf("."));
		xml_name += ".xml";
		// generate xml file
		XMLOutputFactory of = XMLOutputFactory.newInstance();
		XMLEventWriter ew = null;
		try {
			ew = of.createXMLEventWriter(new FileOutputStream(xml_name));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		XMLEventFactory ef = XMLEventFactory.newInstance();
		XMLEvent end = ef.createDTD("\n");
		// start tag
		StartDocument startDoc = ef.createStartDocument();
		ew.add(startDoc);
		ew.add(end);
		// <video>
		StartElement vidElem = ef.createStartElement("", "", "Video");
		ew.add(vidElem);
		ew.add(end);
		// for each frame <frame no=i> </frame>
		for (int i = 0; i < 720; ++i) {
			Attribute frameNo = ef.createAttribute("no", Integer.toString(i));
			List<Attribute> attrList = Arrays.asList(frameNo);
			List<Object> nsList = Arrays.asList();
			StartElement frameElem = ef.createStartElement("", "", "Frame",  attrList.iterator(), nsList.iterator());
			ew.add(frameElem);
			ew.add(end);
			// TODO: video descriptors
			ew.add(ef.createEndElement("", "", "Frame"));
			ew.add(end);
		}
		
		// </video>
		ew.add(ef.createEndElement("", "", "Video"));
		ew.add(end);
		// end document
		ew.add(ef.createEndDocument());
		ew.close();
	}
	
}
