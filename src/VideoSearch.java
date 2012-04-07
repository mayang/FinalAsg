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
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import javax.swing.*;


public class VideoSearch implements MouseListener, MouseMotionListener 
{  

   public static void main(String[] args) 
   {
	   	String fileName = args[0];
   		
   		int width = 352; 
   		int height = 288;
   		//String fileName = "../image1.rgb";
   		
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
   
   Timer fps;
   
   public VideoSearch(int width, int height, String fileName)
   {
	
	    img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    //Reading File
	    try {
		    File file = new File(fileName);
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
	        if (len > 304128) {
	        	vidFlag = true;
	        	fps = new Timer(42, new refreshFrame());
	        	fps.setInitialDelay(42);
	        }
	        
	        // get indicies in bytes array where each frame starts
	        if (vidFlag) {
	        	byteIndicies = new int[720];
	        	for (int b = 0; b < 720; ++b) {
	        		byteIndicies[b] = b * 304128;
	        	}
	        }
	        
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
		buttonPanel.setPreferredSize(new Dimension(200, height));
	    frame.getContentPane().add(buttonPanel, BorderLayout.EAST);
				
		MyButton closeButton = new MyButton("Close");
		buttonPanel.add(closeButton, BorderLayout.WEST);	
		
	    frame.pack();
	    frame.setVisible(true); 
	    
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
   
   
   // Function calls

   

   
   // Move tiles
   
	public void buttonPressed(String name)
	{
		if (name.equals("Close"))
		{
			System.exit(0);
		}
	}
	
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

	/////////////////////////////////////////////////////////
	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		//System.out.println(arg0.getX() + " and " + arg0.getY());
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
			setText(label);
			setFont(new Font("Helvetica", Font.PLAIN, 0));
			addMouseListener(
				new MouseAdapter() {
	  				public void mousePressed(MouseEvent e) {
						buttonPressed(getText());
					}
				}
			);
		}
	}
	
	class refreshFrame implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			++currFrame;
			if (currFrame == 720) {
				currFrame = 0;
			}
			BufferedImage f = refreshFrame(currFrame);
			//if (view == 0) {
			videoOriginal(f);
		  // System.out.println("Frame:" + currFrame);
		}
	}
}