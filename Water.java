public class Water implements Runnable
{
	static final int WIDTH = 640, HEIGHT = 480;

	static Blob start = new Blob();
	static Blob current;

	static Blob queueStart = new Blob();

	static final Pixel[] pixels = new Pixel[WIDTH*HEIGHT];

	static int[] 	lineX = new int[5000],
					lineY = new int[5000];

	static int 	frameRate,
				frame;

	static Interface window;

	long  previousFrameTime, previousRepaint, now;

	static final int waterSize = 30;

	static int previousButton = 0;
	static int mouseX,mouseY,oldX,oldY;

	static final boolean debug = true;

	static int c,o;

	public Water(Interface j)
	{
		window = j;

		start.next = start;
		start.prev = start;

		for(int x = 0; x < WIDTH; x++)
			for(int y = 0; y < HEIGHT; y++)
			{
				pixels[x+y*WIDTH] = new Pixel((x==0?null:pixels[x-1+y*WIDTH]),x+y*WIDTH);

				if(x == 50 || x == WIDTH-50 || y == 50  /*|| Math.abs(12*Math.sin(0.06*x) - y + 430) < 4*/  || x == 1 || x == WIDTH-1 || y == HEIGHT-50 || y == HEIGHT-1)
					changePixel(x,y,-2,Interface.ORANGE);
				else
					changePixel(x,y,0,Interface.WHITE);
			}

		for(int x = 1; x < WIDTH-1; x++)
			for(int y = 1; y < HEIGHT-1; y++)
				pixels[x+y*WIDTH].setNeighbors(	pixels[x+((y-1)*WIDTH)],
												pixels[(x+1)+(y*WIDTH)],
												pixels[x+((y+1)*WIDTH)],
												pixels[(x-1)+(y*WIDTH)],
												pixels[(x+1)+((y-1)*WIDTH)],
												pixels[(x+1)+((y+1)*WIDTH)],
												pixels[(x-1)+((y+1)*WIDTH)],
												pixels[(x-1)+((y-1)*WIDTH)]);

		queueStart.prev = queueStart.next = queueStart;
	}

	public void run()
	{
		while(true)
		{
			frame++;
			frameRate++;

			now = System.currentTimeMillis();

			if(window.pause)
				try
				{
					Thread.sleep(10000000);
				}
				catch(Exception e){}

			mouseX = window.mouseX();
			mouseY = window.mouseY();

			if(now - previousFrameTime > 1000)
			{
				System.out.println("FPS: " + frameRate);
				frameRate = 0;
				previousFrameTime = now;
			}

			if(mouseX != oldX)
			switch(window.mouseButton)
			{
				case 1024: pixelRegion(mouseX-(waterSize/2),mouseY-(waterSize/2),waterSize,waterSize);break;
				case 2048: drawLine();break;
				case 4096: drawLine();break;
			}

			previousButton = window.mouseButton;

			current = start.next;
			while(current != start)
			{
				current.run();
				current = current.next;
			}

			//start - queueStart.next - queueStart.prev - start.next
			if(queueStart != queueStart.next)
			{
				queueStart.prev.next = start.next;
				queueStart.next.prev = start;
				start.next.prev = queueStart.prev;
				start.next = queueStart.next;

				queueStart.prev = queueStart.next = queueStart;
			}

			if(now-previousRepaint> 15)
			{
				window.draw();
				previousRepaint = now;
			}
		}
	}

	public void drawLine()
	{
		if(previousButton == window.mouseButton)
		{
			switch(window.mouseButton)
			{
				case 2048:	o = 0;
							c = Interface.WHITE;break;
				default:	o = -2;
							c = Interface.ORANGE;
			}

			double incr = .9/Math.sqrt((oldX-mouseX)*(oldX-mouseX)+(oldY-mouseY)*(oldY-mouseY));

			for(double i = 0;i < 1;i += incr)
			{
				changePixel((int)(mouseX*i-oldX*i+oldX),(int)(mouseY*i-oldY*i+oldY),o,c);
				changePixel((int)(mouseX*i-oldX*i+oldX)+1,(int)(mouseY*i-oldY*i+oldY),o,c);
				changePixel((int)(mouseX*i-oldX*i+oldX),(int)(mouseY*i-oldY*i+oldY)+1,o,c);
				changePixel((int)(mouseX*i-oldX*i+oldX)+1,(int)(mouseY*i-oldY*i+oldY)+1,o,c);
			}

		}

		oldX = mouseX;
		oldY = mouseY;
	}

	public static void pixelRegion(int x,int y,int length,int width)
	{
		Blob c;

		for(int x2 = x; x2 < x + length; x2++)
			for(int y2 = y; y2 < y + width; y2++)
				if(pixels[x2+(y2*WIDTH)].owner == 0)
				{
					Pixel w = pixels[x2+(y2*WIDTH)];

					c = null;

					for(Pixel n:w.neighbors)
					{
						n.open++;
						if(n.owner > 0)
							c = (c != null?combineBlobs(c,Blob.refs[n.owner]):Blob.refs[n.owner]);
					}

					if(c != null)
						c.acceptPixel(w);
					else
					{
						Pixel p = new Pixel();
						p.prev = p.next = w;
						w.next = w.prev = p;
						addBlob(new Blob(p));
					}
				}
	}

	public static void changePixels(int x,int y,int r,int m,int c)
	{
		for(int x2 = -r; x2 < r; x2++)
			for(int y2 = -r; y2 < r; y2++)
				changePixel(x-2 + x2,y-2 + y2,m,c);
	}

	public static void changePixel(int x,int y,int m,int c)
	{
		if(x < 0 || y < 0 || x >= WIDTH || y >= HEIGHT)
			return;

		Pixel p = pixels[x+y*WIDTH];

		if(p.owner > 0 || p.owner == -1)
			Blob.blobAt(p).clearPixel(p);

		p.owner = m;
		p.setColor(c);
	}

	public static Blob combineBlobs(Blob a,Blob b)
	{
		if(a == b) return a;

		a.destroy();
		b.destroy();

		Pixel startA = a.startB;
		Pixel startB = b.startB;

		//startA - startB.next - startB.prev - startA.next
		startB.next.prev = startA;
		startB.prev.next = startA.next;
		startA.next.prev = startB.prev;
		startA.next = startB.next;

		Blob newB = new Blob(startA);
		addBlob(newB);

		return newB;
	}

	///start - newb - start.next
	public static void addBlob(Blob newb)
	{
		///queueStart - newb - queueStart.next
		newb.prev = queueStart;
		newb.next = queueStart.next;
		queueStart.next.prev = newb;
		queueStart.next = newb;
	}
}