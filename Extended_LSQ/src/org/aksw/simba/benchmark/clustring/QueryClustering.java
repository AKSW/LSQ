package org.aksw.simba.benchmark.clustring;

import java.util.*;

import org.aksw.simba.benchmark.log.operations.CleanQueryReader;

/**
 * Call getPrototypicalQueries will return a set of size nrExemplars that
 * contains the ids of the most representative points queries for the given set
 * of points
 *
 * @author ngonga
 */
public class QueryClustering {


	Map<String, Double> distanceCache;
	/**
	 * Return a set of size nrExemplars that
	 * contains the ids of the most representative points queries for the given set
	 * of points 
	 * @param vectors
	 * @param nrExemplars
	 * @return
	 */
	public Set<String> getPrototypicalQueries(Map<String, Double[]> vectors, int nrExemplars) {
		System.out.println("Benchmark query selection in progress...");
		Set<String> exemplars = getExemplars(vectors, nrExemplars);
		loadExemplarsVectors(vectors,exemplars);
		// System.out.println("Got " + exemplars.size() + " exemplars ...");
		//System.out.println("Exemplars = " + exemplars);
		Map<String, Set<String>> clusters = new HashMap<>();
		for (String ex : exemplars) {
			clusters.put(ex, new TreeSet<String>());
			clusters.get(ex).add(ex);
		}

		// get matching exemplar for each vector
		for (String v : vectors.keySet()) {
			if (!clusters.keySet().contains(v)) {
				double d, min = Double.MAX_VALUE;
				String match = null;
				for (String ex : exemplars) {
					d = getDistanceFromCache(vectors, ex, v);
					if (d < min) {
						min = d;
						match = ex;
					}
				}
				clusters.get(match).add(v);
			}
		}
		//  System.out.println("Got " + clusters.size() + " clusters ...");

		// System.out.println(clusters.size());
		//get most representative query in each cluster
		Set<String> result = new TreeSet<>();

		for (String ex : clusters.keySet()) {
			String r = getMostRepresentative(vectors, clusters.get(ex));
			if (!result.contains(r)) {
				result.add(r);
			} else {
				System.err.println("Query "+r+"already included in "+result);
				result.add(ex);
			}
			//  System.out.println(ex + " -> " + r);
		}

		return result;
	}
	/**
	 * Load centers to be used for plotting voronoi diagram
	 * @param vectors Map of all vectors
	 * @param exemplars set of exemplars
	 */
	public  void loadExemplarsVectors(Map<String, Double[]> vectors, Set<String> exemplars) {
		for(String ex:exemplars)
			VoronoiPanel.centers.add(vectors.get(ex));

	}
	/**
	 * Get examplars for the set of queries
	 * @param vectors Vectors of queries
	 * @param nrExemplars Number of required Examplers
	 * @return examplars Examplers
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public Set<String> getExemplars(Map<String, Double[]> vectors, int nrExemplars) {
		if (vectors.size() <= nrExemplars) {
			System.err.println("Not enough vectors");
			return vectors.keySet();
		}
		distanceCache = new HashMap();
		Set<String> exemplars = new TreeSet<>();
		//first get middle point
		Double[] middle = getAverage(vectors);

		//add it to exemplars
		//exemplars.add(getClosest(vectors, middle));
		exemplars.add(getFurthest(vectors, middle));
		while (exemplars.size() < nrExemplars) {
			exemplars.add(getFurthest(vectors, exemplars));
		}

		return exemplars;
	}

	/**
	 * Computes average vector for a given set of vectors
	 *
	 * @param vectors Set of vectors
	 * @return Average
	 */
	public Double[] getAverage(Map<String, Double[]> vectors) {
		// int dimensions = 	vectors.get(vectors.keySet().iterator().next()).length;

		int dimensions = CleanQueryReader.length;  //length is fixed for all
		// System.out.println(dimensions); 
		//first get middle point
		Double[] middle = new Double[dimensions];
		for (int i = 0; i < dimensions; i++) {
			middle[i] = 0d;
		}

		Double[] v;
		for (String key : vectors.keySet()) {
			v = vectors.get(key);
			// System.out.println(dimensions);
			for (int i = 0; i < dimensions; i++) {
				//System.out.println(middle[i]);
				middle[i] = middle[i] + v[i];
			}
		}

		for (int i = 0; i < dimensions; i++) {
			middle[i] = middle[i] / vectors.keySet().size();
		}

		return middle;
	}
	/**
	 * Get dostamce from cache
	 * @param vectors Vectors
	 * @param ex exampler
	 * @param v vector
	 * @return distinace 
	 */
	public double getDistanceFromCache(Map<String, Double[]> vectors, String ex, String v) {
		double c;
		if (distanceCache.containsKey(ex + "#" + v)) {
			c = distanceCache.get(ex + "#" + v);
		} else {
			c = getDistance(vectors.get(ex), vectors.get(v));
			distanceCache.put(ex + "#" + v, c);
			distanceCache.put(v + "#" + ex, c);
		}
		return c;
	}

	/**
	 * Compute the distance between two queries
	 *
	 * @param a First query
	 * @param b Second query
	 * @return Distance
	 */
	public double getDistance(Double[] a, Double[] b) {
		if (a.length != b.length) {
			return Double.MAX_VALUE;
		}
		double d = 0d;
		for (int i = 0; i < a.length; i++) {
			d = d + Math.pow(a[i] - b[i], 2);
		}
		return Math.sqrt(d);

	}

	/**
	 * Given a point, compute the point that is closest to it
	 *
	 * @param map Set of points
	 * @param middle Point to which the closest is to be found
	 * @return Closest point to middle in vectors
	 */
	private String getClosest(Map<String, Double[]> map, Double[] middle) {
		if (map.isEmpty()) {
			System.err.println("Empty vectors");
			return null;
		}
		double min = Double.MAX_VALUE, d;
		String result = null;
		for (String key : map.keySet()) {
			d = getDistance(map.get(key), middle);
			if (d < min) {
				result = key;
				min = d;
			}
		}
		return result;
	}

	/**
	 * Given a point, compute the point that is closest to it
	 *
	 * @param vectors Set of points
	 * @param middle Point to which the closest is to be found
	 * @return Closest point to middle in vectors
	 */
	private String getFurthest(Map<String, Double[]> vectors, Double[] middle) {
		if (vectors.isEmpty()) {
			System.err.println("Empty vectors");
			return null;
		}
		double max = 0, d;
		String result = null;
		for (String key : vectors.keySet()) {
			d = getDistance(vectors.get(key), middle);
			if (d > max) {
				result = key;
				max = d;
			}
		}
		return result;
	}

	/**
	 * Gets point that is furthest from the exemplars
	 *
	 * @param vectors Vector
	 * @param exemplars Set of exemplars
	 * @return
	 */
	@SuppressWarnings("unused")
	private String getFurthest(Map<String, Double[]> vectors, Set<String> exemplars) {
		double max = 0d;
		double d, c;
		String result = null;
		for (String v : vectors.keySet()) {
			d = 0;
			if (!exemplars.contains(v)) {
				for (String ex : exemplars) {
					//compute total distance from exemplar to non-exemplars                    
					d = d + getDistanceFromCache(vectors, ex, v);
				}
			}
			if (max < d) {
				max = d;
				result = v;
			}
		}
		return result;
	}

	public static void test() {
		Double[] v1 = {0d, 0d};
		Double[] v2 = {1d, 0d};
		Double[] v3 = {0d, 1d};
		Double[] v4 = {1d, 1d};
		Double[] v5 = {0.7d, 0.7d};
		Double[] v6 = {0.3d, 0.3d};

		Map<String, Double[]> vectors = new HashMap<>();
		vectors.put("p1", v1);
		vectors.put("p2", v2);
		vectors.put("p3", v3);
		vectors.put("p4", v4);
		vectors.put("p5", v5);
		vectors.put("p6", v6);

		QueryClustering qc = new QueryClustering();
		Double[] v = qc.getAverage(vectors);
		System.out.println(v[0] + " " + v[1]);
		System.out.println(qc.getClosest(vectors, v));
		System.out.println(qc.getExemplars(vectors, 2));
		System.out.println(qc.getPrototypicalQueries(vectors, 2));
	}

	public static void main(String args[]) {
		test();
	}

	/**
	 * Given a subset of the vectors (a cluster), finds the most representative
	 * point for the given cluster
	 *
	 * @param vectors All queries
	 * @param cluster A subset of the queries
	 * @return Return most representative element of cluster
	 */
	private String getMostRepresentative(Map<String, Double[]> vectors, Set<String> cluster) {
		if (cluster.size() == 1) {
			return cluster.iterator().next();
		}
		//get relevant subset of vectors
		Map<String, Double[]> clusterMap = new HashMap<>();
		for (String c : cluster) {
			clusterMap.put(c, vectors.get(c));
		}

		Double[] middle = getAverage(clusterMap);
		VoronoiPanel.examplars.add(middle);
		return getClosest(clusterMap, middle);
	}
}
