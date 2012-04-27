// CS 576 - ASG 1
// may ang
// mayang@usc.edu

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.*;
import java.io.*;

import javax.xml.stream.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import javax.management.modelmbean.XMLParseException;
import javax.swing.*;

import org.xml.sax.InputSource;


public class VideoSearch implements MouseListener, MouseMotionListener 
{  

   public static void main(String[] args) 
   {
	   	String fileName = args[0];
   		
   		int width = 352; 
   		int height = 288;
   		//String fileName = "../image1.rgb";
   		
    	byteIndicies = new int[720];
    	for (int b = 0; b < 720; ++b) {
    		byteIndicies[b] = b * 304128;
    	}
    	
//   		VideoPreProcessor vpp = new VideoPreProcessor("vdos", byteIndicies);
//   		vpp.fileTraverse();
   		
   		
   		VideoSearch ir = new VideoSearch(width, height, fileName);
   		// for video!
	    if (vidFlag) {
	    	ir.fps.start();
	    }
   }
      
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
   public static String currVid; // name of current video that is playaing
      
   Timer fps;
   
   public VideoSearch(int width, int height, String fileName)
   {
	
	    state = 2;
	    o_width = width;
	    o_height = height;
	    
	    img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    //Reading File
	    try {
		    File file = new File(fileName);
		    // is this a full path name?
		    int slash = fileName.lastIndexOf("\\");
		    int dot = fileName.indexOf(".");
		    if (slash == -1) { // this was just a file name 
		    	currVid = fileName.substring(0, dot);
		    } else {
		    	currVid = fileName.substring(slash + 1, dot);
		    }
		   // System.out.println(currVid);
		    InputStream is = new FileInputStream(file);
	
		    long len = file.length();
		    bytes = new byte[(int) len];
		    
		    
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
		
	    buttonPanel.add(Box.createRigidArea(new Dimension(0, 25)));
	    
	    MyButton playButton = new MyButton("Play");
	    playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
	    buttonPanel.add(playButton);
	    
	    buttonPanel.add(Box.createRigidArea(new Dimension(0, 25)));
	    
	    MyButton pauseButton = new MyButton("Pause");
	    pauseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
	    buttonPanel.add(pauseButton);
	    
	    buttonPanel.add(Box.createRigidArea(new Dimension(0, 25)));
	    
	    MyButton stopButton = new MyButton("Stop");
	    stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
	    buttonPanel.add(stopButton);
	    
	    buttonPanel.add(Box.createRigidArea(new Dimension(0, 25)));
	    
	    MyButton searchButton = new MyButton("Search");
	    searchButton.setAlignmentX(Component.CENTER_ALIGNMENT);
	    buttonPanel.add(searchButton);
	    
	    buttonPanel.add(Box.createRigidArea(new Dimension(0, 25)));
	    
		MyButton closeButton = new MyButton("Close");
		closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPanel.add(closeButton);	
		
				
		// This videos image strip
		JPanel videoStripPanel = new JPanel();
		videoStripPanel.setLayout(new BoxLayout(videoStripPanel, BoxLayout.X_AXIS));
		videoStripPanel.setPreferredSize(new Dimension(width, 100));
		
		MyButton prevButton = new MyButton("Prev", new ImageIcon("images/left.gif"));
		prevButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		videoStripPanel.add(prevButton);
		
		currStrip = new JPanel();
		startFrame = currFrame;
		showVideoStrip(currStrip, width, height);
		currStrip.addMouseListener(this);
		currStrip.addMouseMotionListener(this);
		currStrip.setAlignmentX(Component.CENTER_ALIGNMENT);
		videoStripPanel.add(currStrip);
		
		MyButton nextButton = new MyButton("Next", new ImageIcon("images/right.gif"));
		nextButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
		videoStripPanel.add(nextButton);
		
		frame.getContentPane().add(videoStripPanel, BorderLayout.SOUTH);

		
		
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
   
   // show images in strip
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
   
 
	
	// get next frame
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

	//////////////////////////////////////////////////////////
	/// SEARCH STUFF 
	////////////////////////////////////////////////////////////
	public void extractSearchParams() throws XPathExpressionException, FileNotFoundException {
//		Read current frame's xml file
		XPathFactory xpf = XPathFactory.newInstance(); 
		XPath xpath = xpf.newXPath();
		InputSource is = new InputSource(new FileInputStream(currVid + ".xml"));
		String fno = Integer.toString(currFrame);
		String query = "/video/frame[@no=" + fno + "]/color";
		String color = xpath.evaluate(query , is);
		System.out.println(color);
		
	}
	
	
	
	/////////////////////////////////////////////////////////
	/// CONTROLLER STUFF
	/////////////////////////////////////////////////////////////
	  // buttons
	public void buttonPressed(String name)
	{
		if (name.equals("Play")) { // Play
			state = 0;
			fps.start();
		} else if (name.equals("Pause")) { // Pause
			state = 1;
			//BufferedImage f 
			img = refreshFrame(currFrame);
			//if (view == 0) {
			videoOriginal(img);
			fps.stop();
		} else if (name.equals("Stop")) { // Stop
			state = 2;
			currFrame = 0;
			//BufferedImage f
			img = refreshFrame(currFrame);
			//if (view == 0) {
			videoOriginal(img);
			fps.stop();
		} else if (name.equals("Search")) {
			// search only if not playing
			if (state != 0) {
				try {
					extractSearchParams();
				} catch (XPathExpressionException | FileNotFoundException e) {
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
		}
	}
	
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println(arg0.getX() + " and " + arg0.getY());
		System.out.println(arg0.getX() / 70);
		currFrame = (arg0.getX() / 70) + startFrame;
		if (currFrame > 719) {
			// TODO Handle Cycle
			currFrame -= 720;
		}
		BufferedImage f = refreshFrame(currFrame);
		//if (view == 0) {
		videoOriginal(f);
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
				}
				//BufferedImage f 
				img = refreshFrame(currFrame);
				//if (view == 0) {
				videoOriginal(img);
			} else if (state == 1) { // pause
//				//BufferedImage f 
//				img = refreshFrame(currFrame);
//				//if (view == 0) {
//				videoOriginal(img);
				//fps.stop();
			} else if (state == 2) { // stop
//				currFrame = 0;
//				//BufferedImage f
//				img = refreshFrame(currFrame);
//				//if (view == 0) {
//				videoOriginal(img);
//				fps.stop();
			}


		  // System.out.println("Frame:" + currFrame);
		}
	}
}