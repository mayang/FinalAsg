// This file should read all the video files and preprocess their data
// and put their atributes into an xml file per video

import java.awt.*;
import java.awt.image.*;
import java.io.*;
public class VideoPreProcessor {

	File root;
	
	public VideoPreProcessor(String folderpath) {
		System.out.println("Video PreProcessor called");
		root = new File(folderpath);
	}
	
	// goes through directories, finds files and processes them
	public void fileTraverse() {
		System.out.println("down all the files");
		fileRecurse(root);
	}
	
	public void fileRecurse(File curr) {
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
	public void processVideo(File video, File audio) {
		System.out.println("\t" + video.getName());
	}
	
}
