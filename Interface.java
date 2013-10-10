import javax.swing.JFrame;
import java.awt.Image;
import java.awt.*;
import java.awt.image.MemoryImageSource;
import java.awt.event.*;
import java.awt.MouseInfo;

public class Interface extends JFrame implements KeyListener,MouseListener,MouseWheelListener
{
	Thread thread;

	public static final int BLUE = -16777100,
							ORANGE = -14336,
							WHITE = -1,
							RED = -54123478,
							CYAN = -16711681;

	int mouseButton = 0;
	boolean pause = false;
	int size = 1;
	double pauseTime = 0;

	MemoryImageSource imageSource;
	Image image;
	Graphics graphics;

	public Interface()
	{
		setLocation((1440-Water.WIDTH)/2,(900-Water.HEIGHT)/2);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(Water.WIDTH,Water.HEIGHT);

		addMouseWheelListener(this);
		addMouseListener(this);
		addKeyListener(this);

		setVisible(true);

		imageSource = new MemoryImageSource(Water.WIDTH,Water.HEIGHT,Pixel.color,0,Water.WIDTH);
		imageSource.setFullBufferUpdates(true);
		imageSource.setAnimated(true);
		image = createImage(imageSource);
		graphics = getGraphics();

		(thread = new Thread(new Water(this),"Water Movement")).start();
	}

	public void draw()
	{
		imageSource.newPixels();
		graphics.drawImage(image,0,0,null);
	}

	public void keyPressed(KeyEvent e)
	{
		String name = KeyEvent.getKeyText(e.getKeyCode());
		char key = name.charAt(0);

		switch(key)
		{
			case 'P':	if(pause = !pause)break;
			case 'N': 	thread.interrupt();break;
		}
	}

	public int mouseX()
	{
		return MouseInfo.getPointerInfo().getLocation().x - getX();
	}

	public int mouseY()
	{
		return MouseInfo.getPointerInfo().getLocation().y - getY();
	}

	public void mousePressed(MouseEvent e)
	{
		mouseButton = e.getModifiersEx();
	}

 	public void mouseReleased(MouseEvent e)
 	{
		mouseButton = e.getModifiersEx();
	}

	public void mouseWheelMoved(MouseWheelEvent e)
	{
		if((size += e.getWheelRotation()) < 1) size = 1;
	}

	public void mouseClicked(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void keyTyped(KeyEvent evt){}
	public void keyReleased(KeyEvent e){}

	public static void main(String[] args)
	{
		Interface i = new Interface();
	}
}