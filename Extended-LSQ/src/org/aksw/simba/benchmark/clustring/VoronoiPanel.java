/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.benchmark.clustring;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.aksw.simba.benchmark.Config;
/**
 * Draw Voronoi Diagram of the benchmark queries selection process
 * @author ngonga
 */
@SuppressWarnings("unused")
public class VoronoiPanel extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private BufferedImage canvas;
	public static int SIZE = 512;
	public static List<Double[]> centers = new ArrayList<>();
	public static List<Double[]> examplars = new ArrayList<>();

	public VoronoiPanel(int width, int height) {
		canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		fillCanvas(Color.BLUE);

	}
	/**
	 * Get preferred Size of the panel
	 */
	public Dimension getPreferredSize() {
		return new Dimension(canvas.getWidth(), canvas.getHeight());
	}
	/**
	 * Paint componenet
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(canvas, null, null);
	}
	/**
	 * Plot image 
	 * @param queries query to vectors map of all log queries which passes the filtering test
	 * @param centers picked points
	 * @param dim1 dimension 1
	 * @param dim2 dimension 2
	 */
	public void plot(Map<String, Double[]> queries, List<Double[]> centers, int dim1, int dim2) {


		//   jp.add(canvas);
		//frame.getContentPane().(canvas)   ;

		Map<Integer, Color> colorMap = new HashMap<>();
		Random rand = new Random();
		for (int i = 0; i < centers.size(); i++) {
			float r = rand.nextFloat();
			float g = rand.nextFloat();
			float b = rand.nextFloat();
			Color randomColor = new Color(r, g, b);
			colorMap.put(i, randomColor);
		}
		double d;
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				double min = Double.MAX_VALUE;
				int index = -1;
				//get closest exemplar
				for (int k = 0; k < centers.size(); k++) {
					d = (centers.get(k)[dim1] - (double) i) * (centers.get(k)[dim1] - (double) i)
							+ (centers.get(k)[dim2] - (double) j) * (centers.get(k)[dim2] - (double) j);
					if (d < min) {
						min = d;
						index = k;
					}
				}
				//if (index >= 0) {
				canvas.setRGB(i, j, colorMap.get(index).getRGB());
				//}
			}
		}

		for (String key : queries.keySet()) {
			drawRect(Color.BLACK, queries.get(key)[dim1].intValue(), queries.get(key)[dim2].intValue(), 5, 5);
		}

		for (int i = 0; i < centers.size(); i++) {
			drawCircle(Color.RED, centers.get(i)[dim1].intValue(), centers.get(i)[dim2].intValue(), 5);
		}

	}
	/**
	 * Plot image 
	 * @param queries query to vectors map of all log queries which passes the filtering test
	 * @param centers picked points
	 * @param dim1 dimension 1
	 * @param dim2 dimension 2
	 * @param exemplars exemplars

	 */
	public void plot(Map<String, Double[]> queries, List<Double[]> centers, List<Double[]> exemplars, int dim1, int dim2) {
		//   jp.add(canvas);
		//frame.getContentPane().(canvas)   ;
		Map<Integer, Color> colorMap = new HashMap<>();
		Random rand = new Random();
		for (int i = 0; i < centers.size(); i++) {
			float r = rand.nextFloat();
			float g = rand.nextFloat();
			float b = rand.nextFloat();
			Color randomColor = new Color(r, g, b);
			colorMap.put(i, randomColor);
		}
		double d;
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				double min = Double.MAX_VALUE;
				int index = -1;
				//get closest exemplar
				for (int k = 0; k < centers.size(); k++) {
					d = (centers.get(k)[dim1]*SIZE - (double) i) * (centers.get(k)[dim1]*SIZE - (double) i)
							+ (centers.get(k)[dim2]*SIZE - (double) j) * (centers.get(k)[dim2]*SIZE - (double) j);
					if (d < min) {
						min = d;
						index = k;
					}
				}
				//if (index >= 0) {
				canvas.setRGB(i, j, colorMap.get(index).getRGB());
				//}
			}
		}

		for (String key : queries.keySet()) {
			drawRect(Color.BLACK, (int)(queries.get(key)[dim1]*SIZE), (int)(queries.get(key)[dim2]*SIZE), 5, 5);
		}

		for (int i = 0; i < centers.size(); i++) {
			drawCircle(Color.RED, (int)(centers.get(i)[dim1]*SIZE), (int)(centers.get(i)[dim2]*SIZE), 5);
		}

		//        for (int i = 0; i < exemplars.size(); i++) {
		//            drawCircle(Color.YELLOW, (int)(exemplars.get(i)[dim1]*SIZE), (int)(exemplars.get(i)[dim2]*SIZE), 5);
		//        }
	}
	/**
	 * Fill canvas
	 * @param c color
	 */
	public void fillCanvas(Color c) {
		int color = c.getRGB();
		for (int x = 0; x < canvas.getWidth(); x++) {
			for (int y = 0; y < canvas.getHeight(); y++) {
				canvas.setRGB(x, y, color);
			}
		}
		repaint();
	}
	/**
	 * Draw line
	 * @param c center
	 * @param x1 x1 cord.
	 * @param y1  y1 cord.
	 * @param x2 x2 cord.
	 * @param y2 y2 crod.
	 */
	public void drawLine(Color c, int x1, int y1, int x2, int y2) {
		// Implement line drawing
		repaint();
	}
	/**
	 * Draw Rectangle
	 * @param c center
	 * @param x1 x1 cord.
	 * @param y1 y1 cord.
	 * @param width width
	 * @param height height
	 */
	public void drawRect(Color c, int x1, int y1, int width, int height) {
		int color = c.getRGB();
		// Implement rectangle drawing
		for (int x = x1 - width / 2; x < x1 + width / 2; x++) {
			for (int y = y1 - width / 2; y < y1 + width / 2; y++) {
				if (x < 0 || y < 0 || x >= SIZE || y >= SIZE) {
				} else {
					canvas.setRGB(x, y, color);
				}
			}
		}
		repaint();
	}
	/**
	 * Draw Circle
	 * @param c center
	 * @param x1 x1 cord
	 * @param y1 y1 cord
	 * @param radius radius
	 */
	public void drawCircle(Color c, int x1, int y1, int radius) {
		int color = c.getRGB();
		// Implement rectangle drawing
		for (int x = x1 - radius; x < x1 + radius; x++) {
			for (int y = y1 - radius; y < y1 + radius; y++) {
				if (x < 0 || y < 0 || x >= SIZE || y >= SIZE) {
				} else {
					if ((x1 - x) * (x1 - x) + (y1 - y) * (y1 - y) <= radius * radius) {
						canvas.setRGB(x, y, color);
					}
				}
			}
		}
		repaint();
		repaint();
	}
	/**
	 * Write voronoi diagram to a file
	 * @param path path of file
	 */
	public void writeToFile(String path) {
		try {
			File outputFile = new File(path);
			//System.out.println("Voronoi Diagram saved at: " + outputFile.getAbsolutePath());
			ImageIO.write(canvas, "png", outputFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		testRunningExample();
		Map<String, Double[]> queries = new HashMap<>();
		List<Double[]> centers = new ArrayList<>();
		List<Double[]> exemplars = new ArrayList<>();
		for (int i = 0; i < 500; i++) {
			Double[] doubles = new Double[2];
			doubles[0] = Math.random();
			doubles[1] = Math.random();
			queries.put(i + "", doubles);
			if (i % 25 == 0) {
				centers.add(doubles);
			}
			//            if (i % 25 == 0) 
			//                exemplars.add(doubles);

		}
		//    int width = 640;
		//    int height = 480;
		JFrame frame = new JFrame("Voronoi Diagram of the Benchmark");
		VoronoiPanel panel = new VoronoiPanel(SIZE, SIZE);
		panel.plot(queries, centers, exemplars, 0, 1);
		panel.writeToFile("test.png");
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * Draw the voronoi diagram for the running example of the paper
	 */
	private static void testRunningExample() {
		Map<String, Double[]> queries = new HashMap<>();
		List<Double[]> centers = new ArrayList<>();

		Double[] q1 = {0.2 * SIZE, 0.2 * SIZE};
		Double[] q2 = {0.5 * SIZE, 0.3 * SIZE};
		Double[] q3 = {0.8 * SIZE, 0.5 * SIZE};
		Double[] q4 = {0.9 * SIZE, 0.1 * SIZE};
		Double[] q5 = {0.5 * SIZE, 0.5 * SIZE};
		//         Double[] q6 = {0.3*SIZE, 0.9*SIZE};
		//         Double[] q7 = {0.7*SIZE, 0.7*SIZE};
		//         Double[] q8 = {0.8*SIZE, 0.2*SIZE};
		//         Double[] q9 = {0.2*SIZE, 0.8*SIZE};
		//         Double[] q10 = {0.5*SIZE, 0.6*SIZE};
		queries.put("q1", q1);
		queries.put("q1", q1);
		queries.put("q2", q2);
		queries.put("q3", q3);
		queries.put("q4", q4);
		queries.put("q5", q5);
		//   queries.put("q6", q6);queries.put("q7", q7);queries.put("q8", q8);queries.put("q9", q9);queries.put("q10", q10);
		centers.add(q2);
		centers.add(q4);
		// centers.add(q5);
		JFrame frame = new JFrame("Voronoi Diagram of the Running Example");
		VoronoiPanel panel = new VoronoiPanel(SIZE, SIZE);
		panel.plot(queries, centers, examplars, 0, 1);
		panel.writeToFile("test.png");
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	/**
	 * Calculate standard deviation of given vector
	 *
	 * @param vector Vector
	 * @return Standard Deviation
	 */
	public static double getStandardDeviation(Double[] vector) {

		double mean = getMean(vector);
		// sd is sqrt of sum of (values-mean) squared divided by n - 1
		// Calculate the mean
		//  double mean = 0;
		final int n = vector.length;
		if (n < 2) {
			return Double.NaN;
		}
		// calculate the sum of squares
		double sum = 0;
		for (int i = 0; i < n; i++) {
			final double v = vector[i] - mean;
			sum += v * v;
		}
		// Change to ( n - 1 ) to n if you have complete data instead of a sample.
		return Math.sqrt(sum / (n - 1));
	}

	/**
	 * Calculate standard deviation of given vector
	 *
	 * @param vector Vector
	 * @return Standard Deviation
	 */
	public static double getEntropy(Double[] vector) {

		//double mean = getMean(vector);
		// sd is sqrt of sum of (values-mean) squared divided by n - 1
		// Calculate the mean
		//  double mean = 0;
		final int n = vector.length;
		if (n < 2) {
			return 0d;
		}
		// calculate the sum of squares
		double sum = 0;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				sum = sum + (vector[i] - vector[j]);
			}
		}
		// Change to ( n - 1 ) to n if you have complete data instead of a sample.
		return sum;
	}

	/**
	 * Get the mean of the vector values
	 *
	 * @param vector Vector
	 * @return Mean
	 */
	public static double getMean(Double[] vector) {
		double sum = 0;
		for (int i = 0; i < vector.length; i++) {
			sum = sum + vector[i];
		}
		return (sum / vector.length);
	}

	public static void drawVoronoiDiagram(Map<String, Double[]> normalizedVectors, String img) {
		if (Config.drawVoronoiDiagram = true) {
			JFrame frame = new JFrame("Voronoi Diagram of the Benchmark");
			VoronoiPanel panel = new VoronoiPanel(SIZE, SIZE);
			Map<Double, Integer> dimSTDs = getDimensionsStandardDeviations();
			try {
				Integer[] topDim = getTopDimensionsIndex(dimSTDs);
				// printTopDimensions(topDim);
				//for(int i=0; i<6; i++)
				// for(int j=i+1; j<6; j++)
				// {
				panel.plot(normalizedVectors, examplars, centers,topDim[0], topDim[1]);
				//      panel.plot(normalizedVectors, examplars, centers, i, j);
				panel.writeToFile(img);
				//    }
				frame.add(panel);
				frame.pack();
				//frame.setVisible(true);
				frame.setResizable(false);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

	/**
	 * Print the top 2 dimensions of exemplars
	 *
	 * @param topDim topDim index
	 */
	private static void printTopDimensions(Integer[] topDim) {
		try {
			System.out.println("Top Dimension 1: " + topDim[0] + "| Top Dimension 2: " + topDim[1]);
			for (int ex = 0; ex < centers.size(); ex++) {
				System.out.println(centers.get(ex)[topDim[0]] + "| " + centers.get(ex)[topDim[1]]);
			}
		} catch (Exception ex) {
		}
	}

	/**
	 * Ranked the dimesnions based on their standard devitions
	 *
	 * @param dimSTDs standard deviation to dimension map
	 * @return ranked list of dimesnions
	 */
	private static Integer[] getTopDimensionsIndex(Map<Double, Integer> dimSTDs) {
		Integer[] rankedDims = new Integer[dimSTDs.keySet().size()];
		Object[] stds = dimSTDs.keySet().toArray();
		Arrays.sort(stds);//, Collections.reverseOrder());
		for (int i = 0; i < stds.length; i++) {
			rankedDims[i] = dimSTDs.get(stds[i]);
		}
		return rankedDims;
	}

	/**
	 * Get standard deviation of each dimension of the exemplar. We use this to
	 * pick the top 2 larger S.D dimensions
	 *
	 * @return Map of standarad deviation to dimension
	 */
	public static Map<Double, Integer> getDimensionsStandardDeviations() {
		Map<Double, Integer> dimSTDs = new HashMap<Double, Integer>();
		for (int dim = 0; dim < 6; dim++) {
			Double[] dimVector = new Double[centers.size()];
			for (int ex = 0; ex < centers.size(); ex++) {
				dimVector[ex] = centers.get(ex)[dim];
			}
			//dimSTDs.put(getStandardDeviation(dimVector), dim);
			dimSTDs.put(getEntropy(dimVector), dim);
			//  System.out.println("Dimension: " + dim+ " S.D. "+getStandardDeviation(dimVector));
		}
		return dimSTDs;
	}
}
