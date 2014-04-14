SeamCarving
===========
Seam-carving is a content-aware image resizing technique where the image is reduced in size by one pixel of height (or width) at a time. A vertical seam in an image is a path of pixels connected from the top to the bottom with one pixel in each row. (A horizontal seam is a path of pixels connected from the left to the right with one pixel in each column.) Below left is the original 505-by-287 pixel image; below right is the result after removing 150 vertical seams, resulting in a 30% narrower image. Unlike standard content-agnostic resizing techniques (e.g. cropping and scaling), the most interesting features (aspect ratio, set of objects present, etc.) of the image are preserved.

As you'll soon see, the underlying algorithm is quite simple and elegant. Despite this fact, this technique was not discovered until 2007 by Shai Avidan and Ariel Shamir.

Finding and removing a seam involves three parts and a tiny bit of notation:

Energy calculation. The first step is to calculate the energy of each pixel, which is a measure of the importance of each pixel—the higher the energy, the less likely that the pixel will be included as part of a seam (as we'll see in the next step).

Seam identification. The next step is to find a vertical seam of minimum total energy. This is similar to the classic shortest path problem in an edge-weighted digraph except for the following:
The weights are on the vertices instead of the edges.
We want to find the shortest path from any of the W pixels in the top row to any of the W pixels in the bottom row.
The digraph is acyclic, where there is a downward edge from pixel (x, y) to pixels (x − 1, y + 1), (x, y + 1), and (x + 1, y + 1), assuming that the coordinates are in the prescribed range.

Seam removal. The final step is to remove from the image all of the pixels along the seam.

The SeamCarver API:

public class SeamCarver {
   public SeamCarver(Picture picture)
   public Picture picture()                       // current picture
   public     int width()                         // width  of current picture
   public     int height()                        // height of current picture
   public  double energy(int x, int y)            // energy of pixel at column x and row y in current picture
   public   int[] findHorizontalSeam()            // sequence of indices for horizontal seam in current picture
   public   int[] findVerticalSeam()              // sequence of indices for vertical   seam in current picture
   public    void removeHorizontalSeam(int[] a)   // remove horizontal seam from current picture
   public    void removeVerticalSeam(int[] a)     // remove vertical   seam from current picture
}


We will use the dual gradient energy function: The energy of pixel (x, y) is Δx2(x, y) + Δy2(x, y), where the square of the x-gradient Δx2(x, y) = Rx(x, y)2 + Gx(x, y)2 + Bx(x, y)2, and where the central differences Rx(x, y), Gx(x, y), and Bx(x, y) are the absolute value in differences of red, green, and blue components between pixel (x + 1, y) and pixel (x − 1, y). The square of the y-gradient Δy2(x, y) is defined in an analogous manner. We define the energy of pixels at the border of the image to be 2552 + 2552 + 2552 = 195075.
As an example, consider the 3-by-4 image with RGB values (each component is an integer between 0 and 255) as shown in the table below.


  (255, 101, 51)  	(255, 101, 153)  	(255, 101, 255)  
  (255,153,51)  	  (255,153,153)  	  (255,153,255)  
  (255,203,51)  	  (255,204,153)  	  (255,205,255)  
  (255,255,51)  	  (255,255,153)  	  (255,255,255)  


The ten border pixels have energy 195075. Only the pixels (1, 1) and (1, 2) are nontrivial. We calculate the energy of pixel (1, 2):


Rx(1, 2) = 255 − 255 = 0, 
Gx(1, 2) = 205 − 203 = 2, 
Bx(1, 2) = 255 − 51 = 204, 
yielding Δx2(1, 2) = 22 + 2042 = 41620.


Ry(1, 2) = 255 − 255 = 0, 
Gy(1, 2) = 255 − 153 = 102, 
By(1, 2) = 153 − 153 = 0, 
yielding Δy2(1, 2) = 1022 = 10404.


Thus, the energy of pixel (1, 2) is 41620 + 10404 = 52024. Similarly, the energy of pixel (1, 1) is 2042 + 1032 = 52225.


Finding a vertical seam. The findVerticalSeam() method returns an array of length H such that entry x is the column number of the pixel to be removed from row x of the image. 

Finding a horizontal seam. The behavior of findHorizontalSeam() is analogous to that of findVerticalSeam() except that it should return an array of length W such that entry y is the row number of the pixel to be removed from column y of the image.

Exceptions.

By convention, the indices x and y are integers between 0 and W − 1 and between 0 and H − 1 respectively. Throws a java.lang.IndexOutOfBoundsException if either x or y is outside its prescribed range.
Throws a java.lang.IllegalArgumentException if removeVerticalSeam() or removeHorizontalSeam() is called with an array of the wrong length or if the array is not a valid seam (i.e., either an entry is outside its prescribed range or two adjacent entries differ by more than 1).
Throws a java.lang.IllegalArgumentException if either removeVerticalSeam() or removeHorizontalSeam() is called when either the width or height is less than or equal to
