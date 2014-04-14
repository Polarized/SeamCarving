public class SeamCarver {
	private final float BORDER_ENERGY = 195075;
	private Picture currentPic;
	private float pixelEnergy[][];

	
	public SeamCarver(Picture picture) {
	   if (picture == null) throw new NullPointerException();
	   
	   this.pixelEnergy = new float[picture.height()][picture.width()];
	   
	   this.currentPic = picture;
    }
   
	
	public Picture picture() {
		return this.currentPic; 
	}
   
	
	public int width()  {
	   // width  of current picture
	   return this.currentPic.width();
   
	}
	
	public int height() {
	   // height of current picture
	   return this.currentPic.height();
   
	}
   
	public double energy(int x, int y) {
	   // energy of pixel at column x and row y in current picture
		if (x < 0 || x >= this.width()) throw new IndexOutOfBoundsException();
		if (y < 0 || y >= this.height()) throw new IndexOutOfBoundsException();
		if (y == 0 || y == this.height() - 1 || x == 0 || x == this.width() - 1 ) {
			return BORDER_ENERGY;
		}
		return xGradient(y, x) + yGradient(y, x);
	}
   
	
	public int[] findHorizontalSeam() {
	   // sequence of indices for horizontal seam in current picture
		int width = this.width();
		int height = this.height();
		int[] edgeTo = new int [width * height];
		
		this.computeEnergy();
		
        for (int col = 1; col < width; col++) {
        	for (int row = 0; row < height; row++) {
        		relax(row, col, edgeTo, false);
        	}
        }
        
        int minEnergyIndex = getMin(false);
        
        return getSeam(minEnergyIndex, edgeTo, false);
	}
   
	
	public int[] findVerticalSeam() {
	   // sequence of indices for vertical seam in current picture
		int width = this.width();
		int height = this.height();
		int[] edgeTo = new int [width * height];
		this.computeEnergy();
        for (int row = 1; row < height; row++) {
        	for (int col = 0; col < width; col++) {
        		relax(row, col, edgeTo, true);
        	}
        }
        
        int minEnergyIndex = getMin(true);
        
        return getSeam(minEnergyIndex, edgeTo, true);
	}


	public void removeHorizontalSeam(int[] a) {
	   // remove horizontal seam from current picture
		if (a.length != this.width()) throw new IllegalArgumentException();
		if (this.width() <= 1 || this.height() <= 1) throw new IllegalArgumentException();
		
		int width = this.width();
		int height = this.height();
		float[][] copy = new float[height - 1][width];
		Picture newPic = new Picture(width, height - 1);
		
		for(int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				
				if (a[col] == row) continue;
				
				if (row > a[col])
				{
				    copy[row - 1][col] = this.pixelEnergy[row][col];
				    newPic.set(col, row-1, currentPic.get(col,row));
				}
				else {
				    copy[row][col] = this.pixelEnergy[row][col];
				    newPic.set(col, row, currentPic.get(col,row));
				}
			}
		}
		pixelEnergy = copy;
		this.currentPic = newPic;
	}
   
	
	public void removeVerticalSeam(int[] a) {
	   // remove vertical seam from current picture
		if (a.length != this.height()) throw new IllegalArgumentException();
		if (this.width() <= 1 || this.height() <= 1) throw new IllegalArgumentException();
		
		Picture newPic;

		float[][] copy = new float[this.height()][this.width() - 1];
		newPic = new Picture(this.width() - 1, this.height());
		for (int i = 0; i < a.length; i++)
		{
			System.arraycopy(pixelEnergy[i], 0, copy[i], 0, a[i]);
			for(int j = 0; j < a[i]; j++) {
				newPic.set(j, i, currentPic.get(j, i));
			}
			System.arraycopy(pixelEnergy[i], a[i] + 1, copy[i], a[i], pixelEnergy[i].length - a[i] - 1);
			for(int j = a[i] + 1; j < pixelEnergy[i].length ; j++) {
				newPic.set(j - 1, i, currentPic.get(j, i));
			}
		}
		pixelEnergy = copy;
		this.currentPic = newPic;
	}
	
	private void computeEnergy() {
		for (int row = 0; row < this.pixelEnergy.length; row++) {
			for (int col = 0; col < this.pixelEnergy[row].length; col++) {
				this.pixelEnergy[row][col] = (float) this.energy(col, row);
			}
		}
	}
	
	private int[] getSeam(int minEnergyIndex, int[] edgeTo, boolean isVertical) {
		int[] seam;
		int vertex;
		if (isVertical) {
			seam = new int[this.height()];
			seam[seam.length - 1] = minEnergyIndex;
			vertex = this.getVertexID(this.height() - 1, minEnergyIndex);
		}
		else {
			seam = new int[this.width()];
			seam[seam.length - 1] = minEnergyIndex;
			vertex = this.getVertexID(minEnergyIndex, this.width() - 1);
		}
		for (int i = seam.length - 2; i >= 0; i--) { 
			vertex = edgeTo[vertex];
			if (isVertical) seam[i] = this.getCol(vertex);
			else seam[i] = this.getRow(vertex);
		}
		return seam;
	}

	private int getMin(boolean isVertical) {
		int width = this.width();
		int height = this.height();
		float min = Float.MAX_VALUE;
		int minIndex = 0;
		
		if (isVertical) {
			for (int col = 0; col < width; col++) {
				if (this.pixelEnergy[height - 1][col] < min) {
					min = this.pixelEnergy[height - 1][col];
					minIndex = col;
				}
			}
		}
		else {
			for (int row = 0; row < height; row++) {
				if (this.pixelEnergy[row][width - 1] < min) {
					min = this.pixelEnergy[row][width - 1];
					minIndex = row;
				}
			}
		}
		return minIndex;
	}
	
	private void relax(int row, int col, int[] edgeTo, boolean isVertical) {
		// get neighbors of current vertex
		// for each neighbor calculate the energy + current Energy
		double minEnergy = Double.MAX_VALUE;
		int minNeighbor = 0;
		int[] neighbors;
		
		if (isVertical) neighbors = getNeighborsV(row, col);
		else neighbors = getNeighborsH(row, col);
		
		for (int id : neighbors) {
			if (this.pixelEnergy[this.getRow(id)][this.getCol(id)] < minEnergy) {
				minNeighbor = id;
				minEnergy = this.pixelEnergy[this.getRow(id)][this.getCol(id)];
			}
		}
		this.pixelEnergy[row][col] += minEnergy;
		edgeTo[this.getVertexID(row, col)] = minNeighbor;
	}
	
	private int[] getNeighborsV(int row, int col) {
		// get each of the neighbors that point to the current vertex
		// for vertices on the border you will have two neighbors
		// for vertices not on the border you will have three neighbors
		int [] neighbors;
		    if (col == 0) {
			    neighbors = new int[2];
			    neighbors[0] = this.getVertexID(row - 1, col);
			    neighbors[1] = this.getVertexID(row - 1, col + 1);
		    }
		    else if (col == this.width() - 1) {
			    neighbors = new int[2];
			    neighbors[0] = this.getVertexID(row - 1, col - 1);
			    neighbors[1] = this.getVertexID(row - 1, col);
		    }
		    else {
			    neighbors = new int[3];
			    neighbors[0] = this.getVertexID(row - 1, col - 1);
			    neighbors[1] = this.getVertexID(row - 1, col);
			    neighbors[2] = this.getVertexID(row - 1, col + 1);
		    }
		return neighbors;
	}
	
	private int[] getNeighborsH(int row, int col) {
		// get each of the neighbors that point to the current vertex
		// for vertices on the border you will have two neighbors
		// for vertices not on the border you will have three neighbors
		int [] neighbors;
		    if (row == 0) {
			    neighbors = new int[2];
			    neighbors[0] = this.getVertexID(row, col - 1);
			    neighbors[1] = this.getVertexID(row + 1, col - 1);
		    }
		    else if (row == this.height() - 1) {
			    neighbors = new int[2];
			    neighbors[0] = this.getVertexID(row - 1, col - 1);
			    neighbors[1] = this.getVertexID(row, col - 1);
		    }
		    else {
			    neighbors = new int[3];
			    neighbors[0] = this.getVertexID(row - 1, col - 1);
			    neighbors[1] = this.getVertexID(row, col - 1);
			    neighbors[2] = this.getVertexID(row + 1, col - 1);
		    }
		return neighbors;
	}
	
	private float yGradient(int y, int x) {
		float r = Math.abs(this.currentPic.get(x, y - 1).getRed() - this.currentPic.get(x, y + 1).getRed());
		
		float g = Math.abs(this.currentPic.get(x, y - 1).getGreen() - this.currentPic.get(x, y + 1).getGreen());
		
		float b = Math.abs(this.currentPic.get(x, y - 1).getBlue() - this.currentPic.get(x, y + 1).getBlue());
		
		return r*r + g*g + b*b;
	}
	
	private float xGradient(int y, int x) {
		float r = Math.abs(this.currentPic.get(x - 1, y).getRed() - this.currentPic.get(x + 1, y).getRed());
		
		float g = Math.abs(this.currentPic.get(x - 1, y).getGreen() - this.currentPic.get(x + 1, y).getGreen());
		
		float b = Math.abs(this.currentPic.get(x - 1, y).getBlue() - this.currentPic.get(x + 1, y).getBlue());
		
		return r*r + g*g + b*b;
	}
	
	private int getVertexID(int r, int c) {
		int W = this.width();
		return W * r + c;
	}
	
	private int getRow(int id) {
		int W = this.width();
		return id / W;
	}
	
	private int getCol(int id) {
		int W = this.width();
		return id % W;
	}
}