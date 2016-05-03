package org.aksw.simba.trash;

import java.util.ArrayList;
import java.util.HashSet;
import com.fluidops.fedx.algebra.StatementSource;
/**
 * A hypergraph to represent each DNF group of triple patterns.
 * @author Saleem
 *
 */
public class HyperGraph {
	/**
	 * Vertex of a hypergraph.
	 * Each vertex has Label, set of incoming hyperedges and outgoing hyperedges
	 * @author Saleem
	 *
	 */
	public static class Vertex{
		public final String label;
		public final HashSet<HyperEdge> inEdges;
		public final HashSet<HyperEdge> outEdges;
		/**
		 *  Vertex constructor 
		 * @param label Label of a vertex
		 */
		public Vertex(String label) {
			this.label = label;
			inEdges = new HashSet<HyperEdge>();
			outEdges = new HashSet<HyperEdge>();
		}

		@Override
		public String toString() {
			return label;
		}
	}
	/**
	 * A hyperedge of a hypergraph. Each hyper edge contains subject node as head and predicate, objects nodes as tail of that hyper edge.
	 * Label of a hyperedge contains the relevant source set
	 * @author Saleem
	 *
	 */
	public static class HyperEdge{
		public final Vertex subj;
		public final Vertex  pred;
		public final Vertex obj;
		public final ArrayList<StatementSource> label;
		/**
		 * A hyperedge constructor. 
		 * @param subject Subject vertex and head of the hyperedge
		 * @param predicate Predicate Vertex
		 * @param object Object Vertesx
		 */
		public HyperEdge(Vertex subject, Vertex predicate, Vertex object) {
			this.subj= subject;
			this.pred = predicate;
			this.obj = object;
			this.label = new ArrayList<StatementSource> ();

		}

	}
}