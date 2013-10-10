final class Pixel
{
	static final int[] color = new int[Water.WIDTH*Water.HEIGHT];

	final Pixel left;
	final Pixel[] neighbors;
	final Pixel[] neighborsAll;

	Pixel prev;
	Pixel next;

	final int value;
	final int x;
	final int y;

	int owner;
	//Blob owner;
	int open;

	public int group;

	Pixel t;

	public Pixel()
	{
		left = null;
		value = -1;
		x = 0;
		y = 0;
		this.neighbors = new Pixel[0];
		this.neighborsAll = new Pixel[0];
	}

	public Pixel(Pixel left,int v)
	{
		this.left = left;
		this.value = v;
		this.x = value%Water.WIDTH;
		this.y = value/Water.WIDTH;
		this.neighbors = new Pixel[]{null,null,null,null};
		this.neighborsAll = new Pixel[]{null,null,null,null,null,null,null,null};
	}

	public void setNeighbors(Pixel north,Pixel east,Pixel south,Pixel west,Pixel ne,Pixel se, Pixel sw,Pixel nw)
	{
		neighbors[0] = north;
		neighbors[1] = east;
		neighbors[2] = south;
		neighbors[3] = west;

		neighborsAll[0] = north;
		neighborsAll[1] = east;
		neighborsAll[2] = ne;
		neighborsAll[3] = nw;
		neighborsAll[4] = ne;
		neighborsAll[5] = se;
		neighborsAll[6] = sw;
		neighborsAll[7] = nw;
	}

	public void setColor(int c)
	{
		color[value] = c;
	}
}