public class Blob
{
	static final Blob[] refs = new Blob[1000000];
	static int idCount = 1;

	static Pixel pixelR;

	final int ID;
	final Pixel startB;

	Pixel remove;
	Pixel current;

	Blob next;
	Blob prev;

	static int groupCount;
	static Pixel[] groups = new Pixel[100000];
	static int c;

	public Blob()
	{
		ID = 0;
		startB = null;
	}

	public Blob(Pixel startA)
	{
		refs[ID = ++idCount] = this;

		startB = startA;
		remove = startB;

		current = startA.next;
		while(current != startA)
		{
			current.owner = ID;
			current = current.next;
		}
	}

	public void run()
	{
		if(startB == startB.next){destroy();return;}

		if(startB == startB.next.next){destroy2();return;}
		if(startB == startB.next.next.next){destroy2();return;}

		current = startB;

		//Iterates through every outer pixel
		do
		{
			current = current.next;

			//Iterates through each neighbor and evaluating each as the new pixel
			for(Pixel pixelA : current.neighbors)
			{
				if(pixelA.owner != 0) continue;

				//Iterates through the new pixel's neighbors to check for colliding blobs
				for(Pixel n : pixelA.neighbors)
					if(n.owner > 0 && n.owner != ID)
					{
						Water.combineBlobs(this,blobAt(n));
						return;
					}

				pixelR = (remove = remove.prev);

				///////////////////////////////////////////////////////////////////////////

				if(pixelR.x - pixelA.x < -100) continue;//Realistic Phyics Modifier
				if(pixelR.x - pixelA.x > 100) continue;//Realistic Phyics Modifier

				if(pixelA.value < pixelR.value) continue;

				if(remove == startB) continue;
				if(remove == current) continue;

				///////////////////////////////////////////////////////////////////////////

				removeFromBorder(pixelR);

				pixelR.owner = 0;
				pixelR.setColor(Interface.WHITE);

				//Iterates through the removee's neighbors and adds water to the border.
				for(Pixel n : pixelR.neighbors)
				{
					n.open--;

					if(n.owner == -1)
						addToBorder(n);
				}

				//Either adds the new pixel to the border or to the middle
				if(pixelA.open != 4)
				{
					addToBorder(pixelA);
				}
				else
				{
					pixelA.owner = -1;
					pixelA.setColor(Interface.BLUE);
				}

				//Iterates through the new pixel's neighbors and
				for(Pixel n : pixelA.neighbors)
				{
					n.open++;

					if(n.open == 4 && n.owner == ID)
						removeFromBorder(n);
				}
			}
		}
		while(current != startB);

		if(Water.frame%5==0)seperationDetect();
	}

	public void acceptPixel(Pixel p)
	{
		p.setColor(Interface.BLUE);

		if(p.open == 4)
			p.owner = -1;
		else
			addToBorder(p);

		for(Pixel n : p.neighbors)
			if(n.open == 4 && n.owner == ID)
				removeFromBorder(n);

		for(Pixel n : p.neighbors)
			if(n.owner > 0 && n.owner != ID)
				Water.combineBlobs(this,blobAt(n));
	}

	private final void addToBorder(Pixel p)
	{
		if(Water.debug) p.setColor(Interface.RED); else p.setColor(Interface.BLUE);

		p.owner = ID;

		//smooth movement - (Odd Movement can be achieved by inverting the "prev" and "next")
		//startB - p -  startB.next
		p.prev = startB;
		p.next = startB.next;
		startB.next.prev = p;
		startB.next = p;
	}

	private final void removeFromBorder(Pixel p)
	{
		if(Water.debug)  p.setColor(Interface.BLUE);

		if(remove == p)
			remove = remove.prev;

		if(p == current)
			current = current.next;

		p.owner = -1;

		//p.prev - p.next
		p.next.prev = p.prev;
		p.prev.next = p.next;
	}

	public void destroy()
	{
		prev.next = next;
		next.prev = prev;

		if(Water.current == this)
			Water.current = prev;
	}

	public void destroy2()
	{
		destroy();

		current = startB;

		do
		{
			current = current.next;

			if(current != startB)
				vaporizePixel(current);
		}
		while(current != startB);
	}

	public void clearPixel(Pixel p)
	{
		if(p.owner == ID)
			removeFromBorder(p);

		p.setColor(Interface.WHITE);
		p.owner = 0;

		for(Pixel n : p.neighbors)
		{
			n.open--;

			if(n.owner == -1)
				addToBorder(n);
		}
	}

	public void vaporizePixel(Pixel p)
	{
		p.setColor(Interface.WHITE);
		p.owner = 0;

		for(Pixel n : p.neighbors)
			n.open--;
	}

	public static Blob blobAt(Pixel p)
	{
		while(p.owner <= 0)
			p = p.left;

		return refs[p.owner];
	}

	///////////////////////////////////////////////////////////////////////////
	//////////////////         Seperation Code          ///////////////////////
	///////////////////////////////////////////////////////////////////////////

	public void seperationDetect()
	{
		groupCount = 0;

		current = startB;
		do
		{
			current.t = current.next;
			current.next = null;
			current = current.t;
		}
		while(current != startB);

		current = startB;
		do
		{
			current = current.t;

			for(Pixel n : current.neighborsAll)
			{
				if(n.owner == ID)
					connect(current,n);
			}
		}
		while(current != startB);

		current = startB.t;
		while(current != startB)
		{
			if(current.next == null)
				vaporizePixel(current);

			current = current.t;
		}

		this.destroy();

		for(int i = 0;i < groupCount;i++)
		{
			Pixel b = groups[i];

			if(b != null)
			{
				Pixel a = new Pixel();
				a.next = b.next;
				a.prev = b;
				a.next.prev = a;
				b.next = a;

				Water.addBlob(new Blob(a));
			}
		}
	}

	private void connect(Pixel a,Pixel b)
	{
		Pixel ag = a.next;
		Pixel bg = b.next;

		if(ag == null && bg == null)
		{
			b.group = a.group = groupCount;

			a.prev = a.next = b;
			b.prev = b.next = a;

			groups[groupCount++] = a;
		}
		else if(ag == null)
		{
			a.group = b.group;

			a.prev = b;
			a.next = b.next;
			b.next = b.next.prev = a;
		}
		else if(bg == null)
		{
			b.group = a.group;

			b.prev = a;
			b.next = a.next;
			a.next = a.next.prev = b;
		}
		else if(a.group != b.group)
		{
			b.prev.next = a.next;
			a.next.prev = b.prev;
			b.prev = a;
			a.next = b;

			groups[b.group] = groups[a.group] = null;

			current = a;
			do
			{
				current.group = groupCount;
				current = current.next;
			}while(current != a);

			groups[groupCount++] = a;
		}
	}
}

