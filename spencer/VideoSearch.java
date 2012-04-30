// CS 576 - ASG 1
// may ang
// mayang@usc.edu
// spencer frazier
// sjfrazie@usc.edu

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;

import javax.management.modelmbean.XMLParseException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;
import javax.swing.*;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


public class VideoSearch implements MouseListener, MouseMotionListener 
{  

   public static void main(String[] args) 
   {
	   	String fileName = args[0];
   		String soundFileName = args[1];
   		int width = 352; 
   		int height = 288;
   		//String fileName = "../image1.rgb";
   		
    	byteIndicies = new int[720];
    	for (int b = 0; b < 720; ++b) {
    		byteIndicies[b] = b * 304128;
    	}
    	
   		//VideoPreProcessor vpp = new VideoPreProcessor("vdos", byteIndicies);
   		//vpp.fileTraverse();
    	//fileNames = (vpp.getFileNames()).toArray(new String[0]);
    	
    	// temp so i don't have to preprocess every fucking time I run this
    	String[] temp = {"vdo3", "vdo4", "vdo6"}; 
    	fileNames = temp;
   		
   		
   		VideoSearch ir = new VideoSearch(width, height, fileName, soundFileName);
   		// for video!
	    if (vidFlag) {
	    	ir.fps.start();
	    }
   }
      
   public static VideoPreProcessor vpp;
   public static BufferedImage img; // img
   public static JFrame frame; // frame for UI
   public static JPanel panel; // panel to be shown
   public static boolean vidFlag;
   public static int currFrame = 0; // current frame
   public static byte[] bytes; // bytes from file
   public static int[] byteIndicies; // keeps indexes where new frames start;
   public static int state; // 0 = play, 1 = pause, 2 = stop?
   public static int startFrame; // starting frame of this video's search strip
   public static JPanel currStrip;  // the video strip of the playing video
   public static int o_width; // original width
   public static int o_height; // original height
   public static String currVid; // name of current video that is playing
   public static String[] fileNames; // names of all the vid files
   public static String search1; // name of video to be searched
   public static byte[] searchBytes1; // the bytes of the video that is being searched 
   public static JPanel strip1; // video strip of video that is being searched in the first one
   public static int strip1Start; // where the 1st strip (color starts
   public static String color; // the color to search for
   public static int [] colorFrames; // frames that go in the first search strip
  // public static String color; // color to match
   public static JPanel strip2; // video strip for 2nd search
   public static int strip2Start; // start of 2nd video strip
   public static String motion; // the motion to search for
   public static int[] motionFrames; // frames of matched motion
   public static JPanel strip3; // video strip for 3rd search
   public static int strip3Start; // start of 3rd video strip
   public static String audio; // audio to be matched
   public static int[] audioFrames; // frames of matched audio
   
   public static File soundFile;
   public static FileInputStream sound;
   public static boolean dataLineFlushed;
   public static byte[] soundBytes;
   //public final int soundByteCount = 25518752;   
   public static int[] soundByteIndicies;
   public static InputStream waveStream;
   private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb
   //private final int EXTERNAL_BUFFER_SIZE = 500000;
   public static SourceDataLine dataLine;
   public static AudioInputStream audioInputStream;
   public static boolean isAlreadyPlaying = false;
   Thread soundThread;
   Timer fps;
   
   public static BufferedImage blankFrame;

   
   public VideoSearch(int width, int height, String fileName, String soundFileName)
   {
	
	    state = 2;
	    o_width = width;
	    o_height = height;
	    
	    img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    //Reading File
	    try {
	    	File file = new File(fileName);
		    InputStream is = new FileInputStream(file);
		    
	    	soundFile = new File(soundFileName);
		    InputStream sis = new FileInputStream(soundFile);
		    
		    long len = file.length();
		    long slen = soundFile.length();
		    
		    bytes = new byte[(int) len];
		    soundBytes = new byte[(int) slen];
		    
//		    System.out.println("file length:"+ len);
			audioInputStream = null;
			try {
			    audioInputStream = AudioSystem.getAudioInputStream(sis);
			} catch (UnsupportedAudioFileException e1) {
			    new PlayWaveException(e1);
			} catch (IOException e1) {
			    new PlayWaveException(e1);
			}
		    // is this a full path name?
		    int slash = fileName.lastIndexOf("\\");
		    int dot = fileName.indexOf(".");
		    if (slash == -1) { // this was just a file name 
		    	currVid = fileName.substring(0, dot);
		    } else {
		    	currVid = fileName.substring(slash + 1, dot);
		    }
		    System.out.println(currVid);
		   // bytes = vpp.getFileBytes(currVid);
		    
//		   // System.out.println(currVid);
		    
		    
//		    System.out.println("file length:"+ len);
		    
		    int offset = 0;
	        int numRead = 0;
	        while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	        	offset += numRead;
	        }
	        
	    	// if file longer then height*width*3 or whatever the length is then it is a video!	
	    	// len = 304128 for single picture
	        vidFlag = false;
	        // this is a video!
	        //if (len > 304128) {
	        	//vidFlag = true;
	        	fps = new Timer(42, new refreshFrame());
	        	fps.setInitialDelay(42);
	        //}
	        
	        // get indicies in bytes array where each frame starts
	        //if (vidFlag) {
//	        	byteIndicies = new int[720];
//	        	for (int b = 0; b < 720; ++b) {
//	        		byteIndicies[b] = b * 304128;
//	        	}
	       //ze }
	        		        
	        int ind = 0;
	        // get first frame
        	for(int y = 0; y < height; y++){
				for(int x = 0; x < width; x++){
					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 
					
					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					img.setRGB(x,y,pix);
					ind++;
				}
			}
        	
			
			
	    } catch (FileNotFoundException e) {
	      e.printStackTrace();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	    
		// Make a blank tile, for use when there's no search and when there's less matches then are shown though i think that's really unlikely   
		blankFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < blankFrame.getHeight(); ++y) {
			for (int x = 0; x < blankFrame.getWidth(); ++x) {
				blankFrame.setRGB(x, y, 0x00FFFFFF);
			}
		}
		blankFrame = scaleImage(blankFrame, width, height, .2);
	    strip1Start = 0;
	    // Debuggin'
//	    System.out.println("image dimensions");
//	    System.out.println(width);
//	    System.out.println(height);
//	    System.out.println("number of tiles");
//	    System.out.println(wTiles);
//	    System.out.println(hTiles);
	    
	   //splitImage();
	    
	    // Use a label to display the image
	    frame = new JFrame();
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
	    panel = new JPanel(); 
	    
	    // show original image
	    showImage(frame.getContentPane());

	    // Buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.setPreferredSize(new Dimension(200, height));
	    frame.getContentPane().add(buttonPanel, BorderLayout.EAST);
		
	    buttonPanel.add(Box.createRigidArea(new Dimension(0, 25))); // Spacing
	    
	    MyButton playButton = new MyButton("Play");
	    playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
	    buttonPanel.add(playButton);
	    
	    buttonPanel.add(Box.createRigidArea(new Dimension(0, 25))); // Spacing
	    
	    MyButton pauseButton = new MyButton("Pause");
	    pauseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
	    buttonPanel.add(pauseButton);
	    
	    buttonPanel.add(Box.createRigidArea(new Dimension(0, 25))); // Spacing
	    
	    MyButton stopButton = new MyButton("Stop");
	    stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
	    buttonPanel.add(stopButton);
	    
	    buttonPanel.add(Box.createRigidArea(new Dimension(0, 25))); // Spacing
	    
	    MyButton searchButton = new MyButton("Search");
	    searchButton.setAlignmentX(Component.CENTER_ALIGNMENT);
	    buttonPanel.add(searchButton);
	    
	    buttonPanel.add(Box.createRigidArea(new Dimension(0, 25))); // Spacing
	    
		MyButton closeButton = new MyButton("Close");
		closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPanel.add(closeButton);	
		
		ImageIcon prev = new ImageIcon("images/left.gif");
		ImageIcon next = new ImageIcon("images/right.gif");
		// Panel of strips
		JPanel stripsPanel = new JPanel();
		stripsPanel.setLayout(new BoxLayout(stripsPanel, BoxLayout.Y_AXIS));
		stripsPanel.setPreferredSize(new Dimension(width, 425));
			// This videos image strip
			JPanel videoStripPanel = new JPanel();
			videoStripPanel.setLayout(new BoxLayout(videoStripPanel, BoxLayout.X_AXIS));
			videoStripPanel.setPreferredSize(new Dimension(width, 100));
			
			MyButton prevButton = new MyButton("Prev", prev);
			prevButton.setAlignmentX(Component.LEFT_ALIGNMENT);
			videoStripPanel.add(prevButton);
			
				currStrip = new JPanel();
				startFrame = currFrame;
				showVideoStrip(currStrip, width, height);
				currStrip.addMouseListener(new MouseListener() {
					@Override
					public void mouseReleased(MouseEvent e) {
						// TODO Auto-generated method stub
					}
					
					@Override
					public void mousePressed(MouseEvent e) {
						// TODO Auto-generated method stub
						System.out.println(e.getX() + " and " + e.getY());
						System.out.println(e.getX() / 70);
						currFrame = (e.getX() / 70) + startFrame;
						if (currFrame > 719) {
							currFrame -= 720;
						}
						img = refreshFrame(currFrame);
						//if (view == 0) {
						videoOriginal(img);					
					}
					
					@Override
					public void mouseExited(MouseEvent e) {
						// TODO Auto-generated method stub
					}
					
					@Override
					public void mouseEntered(MouseEvent e) {
						// TODO Auto-generated method stub
					}
					
					@Override
					public void mouseClicked(MouseEvent e) {
						// TODO Auto-generated method stub
					}
				});
				currStrip.setAlignmentX(Component.CENTER_ALIGNMENT);
			videoStripPanel.add(currStrip);
			
			MyButton nextButton = new MyButton("Next", next);
			nextButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
			videoStripPanel.add(nextButton);
			videoStripPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		stripsPanel.add(videoStripPanel);
			JComboBox<String> fileList1 = new JComboBox<String>(fileNames);
			fileList1.setPreferredSize(new Dimension(width, 10));
			fileList1.setSelectedIndex(0);
			fileList1.addActionListener(new ActionListener() {
			//	@Override
				public void actionPerformed(ActionEvent e) {
					JComboBox<String> cb = (JComboBox<String>) e.getSource();
					search1 = (String) cb.getSelectedItem();
					System.out.println(search1);
				}
			});
			fileList1.setAlignmentX(Component.CENTER_ALIGNMENT);
			//searchStrip1Panel.add(fileList1);
		stripsPanel.add(fileList1);
			// The search strip for this video
			//stripsPanel.add(searchStrip1Panel);
			// The actual frames for this search
				/////////////////////////////////////////
			// Search Panel 1 (Color)
			JPanel searchStrip1Panel = new JPanel();
			searchStrip1Panel.setLayout(new BoxLayout(searchStrip1Panel, BoxLayout.X_AXIS));
			searchStrip1Panel.setPreferredSize(new Dimension(width, 100));
//			JPanel strip1Panel = new JPanel();
//			strip1Panel.setLayout(new BoxLayout(strip1Panel, BoxLayout.X_AXIS));
//			strip1Panel.setPreferredSize(new Dimension(width, 100));
			
			MyButton prevButtonSearch1 = new MyButton("PrevSearch1", prev);
			prevButtonSearch1.setAlignmentX(Component.LEFT_ALIGNMENT);
			searchStrip1Panel.add(prevButtonSearch1);
					
					// the actual frames
					strip1 = new JPanel();
					showMatchedFrames(strip1, width, height, "", "", 0, null);
					strip1.addMouseListener(new MouseListener() {
						@Override
						public void mouseReleased(MouseEvent e) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void mousePressed(MouseEvent e) {
							// TODO Auto-generated method stub
							System.out.println(e.getX() + " and " + e.getY());
							System.out.println(e.getX() / 70);
							currFrame = colorFrames[(e.getX() / 70) + strip1Start];
							if (currFrame > colorFrames.length - 1) {
								currFrame -= colorFrames.length;
							}
							bytes = searchBytes1;
							currVid = search1;
							img = refreshFrame(currFrame);
							videoOriginal(img);	
							showVideoStrip(currStrip, 352, 288);
							
						}
						
						@Override
						public void mouseExited(MouseEvent e) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void mouseEntered(MouseEvent e) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void mouseClicked(MouseEvent e) {
							// TODO Auto-generated method stub
							
						}
					});
					strip1.setAlignmentX(Component.CENTER_ALIGNMENT);
					
			searchStrip1Panel.add(strip1);
			MyButton nextButtonSearch1 = new MyButton("NextSearch1", next);
			nextButtonSearch1.setAlignmentX(Component.RIGHT_ALIGNMENT);
			searchStrip1Panel.add(nextButtonSearch1);
			//searchStrip1Panel.add(strip1Panel);
			// add to the main strip panel
			searchStrip1Panel.setAlignmentX(Component.CENTER_ALIGNMENT);
		stripsPanel.add(searchStrip1Panel);
		////////////////////////////////////////////
			// 2nd search strip (motion)
			JPanel searchStrip2Panel = new JPanel();
			searchStrip2Panel.setLayout(new BoxLayout(searchStrip2Panel, BoxLayout.X_AXIS));
			searchStrip2Panel.setPreferredSize(new Dimension(width, 100));
			MyButton prevButtonSearch2 = new MyButton("PrevSearch2", prev);
			prevButtonSearch2.setAlignmentX(Component.LEFT_ALIGNMENT);
			searchStrip2Panel.add(prevButtonSearch2);
				// Actual frames
				strip2 = new JPanel();
				showMatchedFrames(strip2, width, height, "", "", 0, null);
				strip2.setAlignmentX(Component.CENTER_ALIGNMENT);
			searchStrip2Panel.add(strip2);
			MyButton nextButtonSearch2 = new MyButton("NextSearch2", next);
			nextButtonSearch2.setAlignmentX(Component.RIGHT_ALIGNMENT);
			searchStrip2Panel.add(nextButtonSearch2);
			searchStrip2Panel.setAlignmentX(Component.CENTER_ALIGNMENT);
		stripsPanel.add(searchStrip2Panel);
		///////////////////////////////////////////////////
			// 3rd search strip (sound)
			JPanel searchStrip3Panel = new JPanel();
			searchStrip3Panel.setLayout(new BoxLayout(searchStrip3Panel, BoxLayout.X_AXIS));
			searchStrip3Panel.setPreferredSize(new Dimension(width, 100));
			MyButton prevButtonSearch3 = new MyButton("PrevSearch3", prev);
			prevButtonSearch3.setAlignmentX(Component.LEFT_ALIGNMENT);
			searchStrip3Panel.add(prevButtonSearch3);
				// Actual Frames
				strip3 = new JPanel();
				showMatchedFrames(strip3, width, height, "", "", 0, null);
				strip3.setAlignmentX(Component.CENTER_ALIGNMENT);
			searchStrip3Panel.add(strip3);
			MyButton nextButtonSearch3 = new MyButton("NextSearch3", next);
			nextButtonSearch3.setAlignmentX(Component.RIGHT_ALIGNMENT);
			searchStrip3Panel.add(nextButtonSearch3);
			searchStrip3Panel.setAlignmentX(Component.CENTER_ALIGNMENT);
		stripsPanel.add(searchStrip3Panel);
		frame.getContentPane().add(stripsPanel, BorderLayout.SOUTH);
	
		frame.pack();
	    frame.setVisible(true); 
	    
   }
   
   
   // Function calls
   /////////////////////////////////////////////////////
   /// VIDEO STUFF
   ///////////////////////////////////////////////////////
   
   // scale image
   public BufferedImage scaleImage(BufferedImage img, int oWidth, int oHeight, double scale ) {
	   double newW = oWidth * scale;
	   double newH = oHeight * scale;
	   BufferedImage scaledImg = new BufferedImage((int) newW, (int) newH, BufferedImage.TYPE_INT_RGB);
	   Graphics2D gImg = scaledImg.createGraphics();
	   
	   gImg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	   gImg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	   gImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
   
	   gImg.drawImage(img, 0, 0, (int) newW, (int) newH, null);
	   gImg.dispose();
	   
	   return scaledImg;
   }
   
   // show original image
   public void showImage(Container pane) {
	   
	   panel.removeAll();
	   panel.setLayout(new BorderLayout());
	   JLabel label = new JLabel(new ImageIcon(img));
	   label.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
	   //original.add(label, BorderLayout.CENTER);
	   panel.add(label, BorderLayout.CENTER);
	   panel.revalidate();
	   panel.repaint();
	   pane.add(panel, BorderLayout.CENTER);

   }
   
   // show images in strip for current video
	public void showVideoStrip(JPanel strip, int width, int height) {
		int loopcount = 0;
		
		// get image
		strip.removeAll();
		strip.setLayout(new BoxLayout(strip, BoxLayout.X_AXIS));
		strip.setPreferredSize(new Dimension(width-20, 100));
		int end = startFrame + 6;
		for (int i = startFrame; i < end; ++i) {
			if (i > 719) { // cycle!
				i = 0;
				//startFrame = 0;
				end = 6 - loopcount; // what should this be?
			}
			BufferedImage vid_frame = refreshFrame(i);
			vid_frame = scaleImage(vid_frame, width, height, .2);
			JLabel label = new JLabel(new ImageIcon(vid_frame));
			strip.add(label);
			++loopcount;
		}
		strip.revalidate();
		strip.repaint();
	}
   
	
	// get next frame for the main video
	public BufferedImage refreshFrame(int currFrame) {
		// get new picture
    	int ind = byteIndicies[currFrame];
    	//System.out.println(currFrame + " is at " + ind);
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
		//img = scaleImage(img, 352, 288, scale);
		return img;
	}
	
	// // this renders a frame in the search strips
	public BufferedImage renderFrame(int f, byte[] vid_bytes) {
		// get new picture
    	int ind = byteIndicies[f];
    	//System.out.println(currFrame + " is at " + ind);
		BufferedImage img = new BufferedImage(352, 288, BufferedImage.TYPE_INT_RGB);
		for(int y = 0; y < 288; y++){
			for(int x = 0; x < 352; x++){
				//System.out.println("i:" + ind);
				byte a = 0;
				byte r = vid_bytes[ind];
				byte g = vid_bytes[ind+288*352];
				byte b = vid_bytes[ind+288*352*2]; 
				
				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
				img.setRGB(x,y,pix);
				ind++;
			}
    	}
		//img = scaleImage(img, 352, 288, scale);
		return img;
	}
	
	// refresh original pane
	public void videoOriginal(BufferedImage img) {
		// update image
		panel.removeAll();
	   panel.setLayout(new BorderLayout());
		JLabel label = new JLabel(new ImageIcon(img));
	   label.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
	   panel.add(label, BorderLayout.CENTER);
	   panel.revalidate();
	   panel.repaint();
	}

	// show the actual frames that match a certain parameter
	public void showMatchedFrames(JPanel strip, int w, int h, String vidName, String search, int start, int[] frames) {
		
		// get image
		strip.removeAll();
		strip.setLayout(new BoxLayout(strip, BoxLayout.X_AXIS));
		strip.setPreferredSize(new Dimension(w-20, 100));
		JLabel label;
		int end = start + 6;
		int loopcount = 0;
		for (int i = start; i < end; ++i) {
			if (vidName.compareTo("") == 0 ) {
				label = new JLabel(new ImageIcon(blankFrame));
				strip.add(label);
			} else {
				if (i > frames.length) { // cycle? why aren't you cycling back!
					i = 0;
					end = 6 - loopcount;
				}
				// Draw strip
				BufferedImage vid_frame = renderFrame(frames[i], searchBytes1);
				vid_frame = scaleImage(vid_frame, 352, 288, .2);
				label = new JLabel(new ImageIcon(vid_frame));
				strip.add(label);
				++loopcount;
			}
		}
		strip.revalidate();
		strip.repaint();
		
	}
	
	//////////////////////////////////////////////////////////
	/// SEARCH STUFF 
	////////////////////////////////////////////////////////////
	
	// Gets search parameters from the current frame
	public void extractSearchParams() throws XPathExpressionException, IOException {
//		Read current frame's xml file
		XPathFactory xpf = XPathFactory.newInstance(); 
		XPath xpath = xpf.newXPath();
		InputSource is = new InputSource(new FileInputStream(currVid + ".xml"));
		
		// Query for the descriptors of this frame
		String fno = Integer.toString(currFrame);
		String queryColor = "/video/frame[@no=" + fno + "]/color";
		color = xpath.evaluate(queryColor , is);
		System.out.println(color);
		is = new InputSource(new FileInputStream(currVid + ".xml"));
		String queryMotion = "/video/frame[@no" + fno + "]/motion";
		motion = xpath.evaluate(queryMotion, is);
		System.out.println(motion);
		is = new InputSource(new FileInputStream(currVid + ".xml"));
		String queryAudio = "/video/frame[@no" + fno + "]/audio";
		audio = xpath.evaluate(queryAudio, is);
		
		// read in search file vdos/vdo*/vdo*.rgb
		// if this file isn't the file that's already in because that would be silly to reread it
		if (currVid.compareTo(search1) != 0) {
			System.out.println("this is a different video");
			File file = new File("vdos/" + search1 + "/" + search1 + ".rgb");
			InputStream fis = new FileInputStream(file);
			long len = file.length();
			searchBytes1 = new byte[(int) len];
		    int offset = 0;
	        int numRead = 0;
	        while (offset < searchBytes1.length && (numRead=fis.read(searchBytes1, offset, searchBytes1.length-offset)) >= 0) {
	        	offset += numRead;
	        }
		} else { // this is searching against itself
			searchBytes1 = bytes;
		}
		//int [] colorFrames = null;
		// Call get Frames
//		if (vidName.compareTo("") != 0) {
			try {
				colorFrames = getMatchedFrames("color", color);
				motionFrames = getMatchedFrames("motion", motion);
				audioFrames = getMatchedFrames("audio", audio);
			} catch (FileNotFoundException | XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		
		// Call showMatchedFrame, 
		// shit how am i to pick which strip, new arg to do later i guess get this one showing first
		showMatchedFrames(strip1, 352, 288, search1, color, strip1Start, colorFrames);
		showMatchedFrames(strip3, 352, 288, search1, motion, strip2Start, motionFrames);
		showMatchedFrames(strip3, 352, 288, search1, audio, strip3Start, audioFrames);
	}

	// get frames with this search parameter and display them in a strip
	public int[] getMatchedFrames(String param, String desc) throws FileNotFoundException, XPathExpressionException {
		//List<Integer> frames = new ArrayList<Integer>();
		XPathFactory xpf = XPathFactory.newInstance();
		XPath xpath = xpf.newXPath();
		InputSource is = new InputSource(new FileInputStream(search1 + ".xml"));
		String query = "/video/frame[" + param + "=\"" + desc + "\"]/@no";
		System.out.println(query);
		NodeList nodes = (NodeList) xpath.evaluate(query, is, XPathConstants.NODESET);
		int[] frames = new int[nodes.getLength()];
		//System.out.println(nodes == null);
		for (int i = 0; i < nodes.getLength(); ++i) {
			//System.out.println(nodes.item(i).getTextContent());
			frames[i] = Integer.parseInt(nodes.item(i).getTextContent());
		}

		return frames;
	}
	

	
	
	
	
	/////////////////////////////////////////////////////////
	/// CONTROLLER STUFF
	/////////////////////////////////////////////////////////////
	  // buttons
	public void buttonPressed(String name)
	{
		if (name.equals("Play")) { // Play
			state = 0;
			soundThread = null;
			soundThread = new Thread(new RefreshSound());
			fps.start();
			soundThread.start();
			isAlreadyPlaying = true;
			//dataLineFlushed = false;
		} else if (name.equals("Pause")) { // Pause
			state = 1;
			fps.stop();
			try {
				audioInputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			soundThread.interrupt();
			dataLine.stop();
			dataLine.flush();
			dataLine.close();
		} else if (name.equals("Stop")) { // Stop
			state = 2;
			//fps.stop();
			currFrame=0;
			BufferedImage f = refreshFrame(currFrame);
			//if (view == 0) {
			videoOriginal(f);
			isAlreadyPlaying = false;
			soundThread.interrupt();
			dataLine.stop();
			dataLine.flush();
			dataLine.close();
		} else if (name.equals("Search")) {
			// search only if not playing
			if (state != 0) {
				// extract what to match
				try {
					extractSearchParams();
				} catch (XPathExpressionException | FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		} else if (name.equals("Close")) { // close
			System.exit(0);
		} else if (name.equals("Prev")) { // Prev
			if (startFrame > 0) {
				--startFrame;
			} else {
				startFrame = 719; // how to cycle?
			}
			showVideoStrip(currStrip, o_width, o_height);
		} else if (name.equals("Next")) { // next
			if (startFrame < 719) { 
				++startFrame;
			} else {
				startFrame = 0;
			}
			showVideoStrip(currStrip, o_width, o_height);
		} else if (name.equals("PrevSearch1")) {
			if (strip1Start > 0) {
				--strip1Start;
			} else {
				strip1Start = colorFrames.length - 1;
			}
			showMatchedFrames(strip1, 352, 288, search1, color, strip1Start, colorFrames);
		} else if (name.equals("NextSearch1")) {
			if (strip1Start < colorFrames.length) {
				++strip1Start;
			} else {
				strip1Start = 0;
			}
			showMatchedFrames(strip1, 352, 288, search1, color, strip1Start, colorFrames);
		} else if (name.equals("PrevSearch2")) {
			if (strip2Start > 0) {
				--strip2Start;
			} else {
				strip2Start = motionFrames.length - 1;
			}
			showMatchedFrames(strip2, 352, 288, search1, motion, strip2Start, motionFrames);
		} else if (name.equals("NextSearch2")) {
			if (strip2Start < motionFrames.length) {
				++strip2Start;
			} else {
				strip2Start = 0;
			}
			showMatchedFrames(strip2, 352, 288, search1, motion, strip2Start, motionFrames);
		} else if (name.equals("PrevSearch3")) {
			if (strip3Start > 0) {
				--strip3Start;
			} else {
				strip3Start = audioFrames.length - 1;
			}
			showMatchedFrames(strip3, 352, 288, search1, audio, strip3Start, audioFrames);
		} else if (name.equals("NextSearch3")) {
			if (strip3Start < audioFrames.length) {
				++strip3Start;
			} else {
				strip3Start = 0;
			}
			showMatchedFrames(strip3, 352, 288, search1, audio, strip3Start, audioFrames);
		}
	}
	
	// frame indexing for current frame, this might have to be moved directly into the function
	@Override
	public void mouseClicked(MouseEvent arg0) {
		System.out.println(arg0.getX() + " and " + arg0.getY());
		System.out.println(arg0.getX() / 70);
		currFrame = (arg0.getX() / 70) + startFrame;
		if (currFrame > 719) {
			// TODO Handle Cycle
			currFrame -= 720;
		}
		img = refreshFrame(currFrame);
		//if (view == 0) {
		videoOriginal(img);
		//moveTiles(arg0.getX(), arg0.getY());
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		//System.out.println("Moving");
	} 
	
	class MyButton extends JButton {
		MyButton(String label){
			setFont(new Font("Helvetica", Font.BOLD, 10));
			setText(label);
			addMouseListener(
				new MouseAdapter() {
	  				public void mousePressed(MouseEvent e) 
	  				{
						buttonPressed(getText());
					}
				}
			);
		}
		
		MyButton(String label, ImageIcon icon){
			Image img = icon.getImage();
			Image scaleimg = img.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
			setIcon(new ImageIcon(scaleimg));
			setName(label);
			//setFont(new Font("Helvetica", Font.PLAIN, 0));
			addMouseListener(
				new MouseAdapter() {
	  				public void mousePressed(MouseEvent e) {
						buttonPressed(getName());
					}
				}
			);
		}
	}
	
	class refreshFrame implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (state == 0) { // play
				++currFrame;
				if (currFrame == 720) {
					currFrame = 0;
					soundThread.interrupt();
					dataLine.stop();
					dataLine.flush();
					dataLine.close();
					soundThread = null;
					soundThread = new Thread(new RefreshSound());
					soundThread.start();
				}
				//BufferedImage f 
				img = refreshFrame(currFrame);
				//if (view == 0) {
				videoOriginal(img);
			} else if (state == 1) { // pause
				BufferedImage f = refreshFrame(currFrame);
				//if (view == 0) {
				videoOriginal(f);
				fps.stop();
			} else if (state == 2) { // stop
//				currFrame = 0;
				BufferedImage f = refreshFrame(currFrame);
				//if (view == 0) {
				videoOriginal(f);
				fps.stop();
			}


		  // System.out.println("Frame:" + currFrame);
			
		}
	}
	
	protected int calculateRMSLevel(byte[] audioData)
    { // audioData might be buffered data read from a data line
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
	
	public class RefreshSound implements Runnable {
		public void run() {
			// Obtain the information about the AudioInputStream
			
				InputStream sis = null;
				try {
					sis = new FileInputStream(soundFile);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				long slen = soundFile.length();
		    
				soundBytes = new byte[(int) slen];
		    
//		   		 System.out.println("file length:"+ len);
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

						// opens the audio channel
						
						dataLine = null;
						try {
						    dataLine = (SourceDataLine) AudioSystem.getLine(info);
						    dataLine.open(audioFormat, EXTERNAL_BUFFER_SIZE);
						} catch (LineUnavailableException e1) {
						    new PlayWaveException(e1);
						}
						
			dataLine.start();

			int readBytes = 0;
			byte[] audioBuffer = new byte[EXTERNAL_BUFFER_SIZE];
			try {
				audioInputStream.skip((long) (currFrame*7357.0));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
			    while (readBytes != -1) {
			    
				readBytes = audioInputStream.read(audioBuffer, 0,
					audioBuffer.length);
				//System.out.println(readBytes);
				
					if (readBytes >= 0){
				    dataLine.write(audioBuffer, 0, readBytes);
				    System.out.println(dataLine.getLevel());
				    //System.out.print("Number of bytes read this round:");System.out.println(readBytes);
					System.out.println("hi");
					int level = 0;
					level = calculateRMSLevel(audioBuffer);
					System.out.println(level);
					System.out.println("bye");
				    AudioFormat af = dataLine.getFormat();
				    float afloaz = af.getFrameRate();
				    float samples = af.getSampleRate();
				    int channels = af.getChannels();
				    System.out.println(afloaz);
				    System.out.println(samples);
				    System.out.println(channels);
					}
					//if(readBytes<)
			    }
			    //dataLine.stop();
			} catch (IOException e1) {
			    new PlayWaveException(e1);
			}
			
	
		}
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