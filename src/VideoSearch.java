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
   		wTiles = Integer.parseInt(args[1]); // number of columns
   		hTiles = Integer.parseInt(args[2]); // number of rows
   		scale = 1.0; // default scale
   		// if there's a scale specified
   		if (args.length > 3) {
   			scale = Double.parseDouble(args[3]);
   		}
   		
   		int width = 352; 
   		int height = 288;
   		//String fileName = "../image1.rgb";
   		
   		VideoSearch ir = new VideoSearch(width, height, scale, fileName);
   		// for video!
	    if (vidFlag) {
	    	ir.fps.start();
	    }
   }

   // stores section of image where tile comes from
   class Tile {
	   int width; // width of tile
	   int height; // height of tile
	   int leftCornerX; // x index of left Corner of main image section
	   int leftCornerY; // y index of left Corner of main image section
	   int rightCornerX; // x index of right corner of main image section
	   int rightCornerY; // y index of right corner of main image section
   };
      
   public static BufferedImage img; // img
   public static BufferedImage blankTile; // blank image
   public static Tile tiles[]; // tiles of images
   public static Tile puzzledTiles[]; // tiles in order of puzzle
   public static JFrame frame; // frame for UI
   public static JPanel panel; // panel to be shown
   public static int wTiles; // number of columns
   public static int hTiles; // number of rows
   public static double scale; // scale
   public static int blankI; // index of blank tile
   public static boolean vidFlag;
   public static int view; // 0 for original, 1 for tiled, 2 for puzzled
   public static int currFrame = 0; // current frame
   public static byte[] bytes; // bytes from file
   public static int[] byteIndicies; // keeps indexes where new frames start;
   
   Timer fps;
   
   public VideoSearch(int width, int height, double scale, String fileName)
   {
	
	    img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    view = 0;
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
	    
	    
	    // scale image
	    if (scale != 1.0) {
	    	BufferedImage scaledImage = scaleImage(img, width, height, scale);
	    	img = scaledImage;
	    }

	    //update values of image
	    double scaledW = width * scale;
	    double scaledH = height * scale;
	    width = (int) scaledW;
	    height = (int) scaledH;
	    
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
		buttonPanel.setPreferredSize(new Dimension(width, 50));
	    frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		MyButton splitButton = new MyButton("Split");
		buttonPanel.add(splitButton, BorderLayout.WEST);

		MyButton initButton = new MyButton("Initialize");
		buttonPanel.add(initButton, BorderLayout.WEST);
		
		MyButton resetButton = new MyButton("Reset");
		buttonPanel.add(resetButton, BorderLayout.WEST);
		
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
   
   // split image into tiles
   public void splitImage(BufferedImage img) {
	   int w = img.getWidth();
	   int h = img.getHeight();
	   
	   tiles = new Tile[wTiles * hTiles];
	   
	   // dimensions of each tile
	   int tileW = w / wTiles;
	   int tileH = h / hTiles;

	   // Debuggin'
//	   System.out.println("tile dimensions");
//	   System.out.println(tileW);
//	   System.out.println(tileH);
	   
	   // Get tiles
	  int i = 0; // index for tiles array
	   for (int y = 0; y < hTiles; ++y) {
		   for (int x = 0; x < wTiles; ++x) {
			   tiles[i] = new Tile();
			   tiles[i].width = tileW;
			   tiles[i].height = tileH;
			   tiles[i].leftCornerX = x * tileW;
			   tiles[i].leftCornerY = y * tileH;
			   tiles[i].rightCornerX = x * tileW + tileW;
			   tiles[i].rightCornerY = y * tileH + tileH;
			   
			   ++i; // increment i
		   }
	   }
   
	   // split image panel
	   panel.removeAll();
	   GridLayout tiledLayout = new GridLayout(hTiles, wTiles, 5, 5);
	   panel.setLayout(tiledLayout);
	   for (i = 0; i < tiles.length; ++i) {
		   BufferedImage tile = new BufferedImage(tiles[i].width, tiles[i].height, BufferedImage.TYPE_INT_RGB);
		   
		   Graphics2D gTile = tile.createGraphics();
		   gTile.drawImage(img, 0, 0, tiles[i].width, tiles[i].height, 
				   tiles[i].leftCornerX, tiles[i].leftCornerY,
				   tiles[i].rightCornerX, tiles[i].rightCornerY, null);
		   gTile.dispose();		   
		   JLabel label = new JLabel(new ImageIcon(tile));
		   label.setPreferredSize(new Dimension(tile.getWidth(), tile.getHeight()));
		   panel.add(label);
	   }
	   panel.revalidate();
	   panel.repaint();
   }
    
   public void jumbleTiles() {
	   if (tiles == null) {
		   return;
	   }
	   
	   puzzledTiles = (Tile[]) tiles.clone();
	   
	   // shuffle!
	   Collections.shuffle(Arrays.asList(puzzledTiles));
	   
	   // Make blank white tile
	   blankTile = new BufferedImage(tiles[0].width, 
			   tiles[0].height, BufferedImage.TYPE_INT_RGB);
	   for (int y = 0; y < blankTile.getHeight(); ++y) {
		   for (int x = 0; x < blankTile.getWidth(); ++x) {
			   blankTile.setRGB(x, y, 0x00FFFFFF);
		   }
	   }
	   // get a random index 
	   Random rand = new Random();
	   blankI = rand.nextInt(tiles.length);
	   //tiles[blankI] = blankTile	  
   }
   
   // jumble image
   public void initPuzzle(BufferedImage img) {
	   	    
	  if (puzzledTiles == null) {
		  view = 0;
		  return;
	  }
	   
	   // make pane!
	   panel.removeAll();
	   GridLayout tiledLayout = new GridLayout(hTiles, wTiles, 5, 5);
	   panel.setLayout(tiledLayout);
	   for (int i = 0; i < tiles.length; ++i) {
		   JLabel label;
		   // draw blank tile
		   if (i == blankI) {
			   label = new JLabel(new ImageIcon(blankTile));
			   label.setPreferredSize(new Dimension(blankTile.getWidth(), blankTile.getHeight()));
		   } else {
			   BufferedImage piece = new BufferedImage(puzzledTiles[i].width, puzzledTiles[i].height, BufferedImage.TYPE_INT_RGB);
			   
			   Graphics2D gTile = piece.createGraphics();
			   gTile.drawImage(img, 0, 0, puzzledTiles[i].width, puzzledTiles[i].height, 
					   puzzledTiles[i].leftCornerX, puzzledTiles[i].leftCornerY,
					   puzzledTiles[i].rightCornerX, puzzledTiles[i].rightCornerY, null);
			   gTile.dispose();
			   
			   label = new JLabel(new ImageIcon(piece));
			   label.setPreferredSize(new Dimension(piece.getWidth(), piece.getHeight()));
		   }
		   panel.add(label);

		   panel.addMouseListener(this);
		   panel.addMouseMotionListener(this); 
	   }
	   panel.revalidate();
	   panel.repaint();
   }
   
   // Move tiles
   public void moveTiles(int mX, int mY) {
	   /// get tile that was clicked
	   int x = mX / puzzledTiles[0].width;
	   int y = mY / puzzledTiles[0].height;
//	   System.out.println("x:" + x);
//	   System.out.println("y:" + y);
	   int mIndex = (y * wTiles) + x;
	   
//	   System.out.println("blank" + blankI);
//	   System.out.println("tile" + mIndex);
	   
	   // check if tile is movable (next to the blank tile)
	   // get possible indexes
	   int left = -1, right = -1, above = -1, below = -1;
	   if (blankI % 4 != 0) { // there can be a tile on the left
		   left = blankI - 1;
	   }
	   if ((blankI + 1) % 4 != 0) { // there can be a tile on the right!
		   right = blankI + 1;
	   }
	   if ((blankI - wTiles) >= 0) { // there can be a tile above!
		   above = blankI - wTiles;
	   }
	   if ((blankI + wTiles) < puzzledTiles.length) { // there can be a tile below!
		   below = blankI + wTiles;
	   }
	   
	   // is this tile that was clicked on a valid tile to move?
	   if (mIndex == left || mIndex == right || mIndex == above || mIndex == below) { 
		   // move the tile
		   swapTiles(mIndex);
		   //System.out.println("new blank:" + blankI);
		   
		   // update puzzle
		   panel.removeAll();
		   // re-add the images 
		   GridLayout tiledLayout = new GridLayout(hTiles, wTiles, 5, 5);
		   panel.setLayout(tiledLayout);
		   for (int i = 0; i < puzzledTiles.length; ++i) {
			   JLabel label;
			   // draw blank tile if index is blank tile index
			   if (i == blankI) {
				   label = new JLabel(new ImageIcon(blankTile));
				   label.setPreferredSize(new Dimension(blankTile.getWidth(), blankTile.getHeight()));
			   } else {
				   BufferedImage piece = new BufferedImage(puzzledTiles[i].width, puzzledTiles[i].height, BufferedImage.TYPE_INT_RGB);
				   
				   Graphics2D gTile = piece.createGraphics();
				   gTile.drawImage(img, 0, 0, puzzledTiles[i].width, puzzledTiles[i].height, 
						   puzzledTiles[i].leftCornerX, puzzledTiles[i].leftCornerY,
						   puzzledTiles[i].rightCornerX, puzzledTiles[i].rightCornerY, null);
				   gTile.dispose();
				   
				   label = new JLabel(new ImageIcon(piece));
				   label.setPreferredSize(new Dimension(piece.getWidth(), piece.getHeight()));
			   }
			   panel.add(label);
		   }
		   panel.revalidate();
		   panel.repaint();
		   panel.addMouseListener(this);
		   panel.addMouseMotionListener(this);
	   } 
	   
   }
   
   // swap tiles
   public void swapTiles(int index) {
	   Tile temp = puzzledTiles[blankI];
	   puzzledTiles[blankI] = puzzledTiles[index];
	   puzzledTiles[index] = temp;
	   blankI = index;
   }
   
	public void buttonPressed(String name)
	{
		if (name.equals("Split"))
		{
			//System.out.println("Split");
			if (!vidFlag)
				splitImage(img);
			view = 1;
		} else if (name.equals("Initialize"))
		{
			//System.out.println("Initialize");
			jumbleTiles();
			if (!vidFlag)
				initPuzzle(img);
			view = 2;
		} else if (name.equals("Reset"))
		{
			//System.out.println("Reset");
			if (!vidFlag)
				showImage(frame.getContentPane());
			view = 0;
		} else if (name.equals("Close"))
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
		img = scaleImage(img, 352, 288, scale);
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
		moveTiles(arg0.getX(), arg0.getY());
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
			if (view == 0) {
				videoOriginal(f);
			} else if (view == 1) {
				splitImage(f);
			} else if (view == 2) {
				initPuzzle(f);
			}
		  // System.out.println("Frame:" + currFrame);
		}
	}
}