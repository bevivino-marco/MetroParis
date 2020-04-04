package it.polito.tdp.metroparis.model;

import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;

public class EdgeTraversedGraphListener implements TraversalListener<Fermata, DefaultEdge> {
	private Map <Fermata, Fermata> back ;
	private Graph <Fermata, DefaultEdge> grafo;
	

	public EdgeTraversedGraphListener(Graph <Fermata, DefaultEdge> grafo , Map<Fermata, Fermata> back) {
		this.grafo=grafo;
		this.back = back;
	}

	@Override
	public void connectedComponentFinished(ConnectedComponentTraversalEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void connectedComponentStarted(ConnectedComponentTraversalEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> ev) {
		/**
		 * back codifica relazioni del tipo child---> parent
		 * 
		 * per un nuovo vertice ' child' scoperto
		 * devo avere che :
		 * -child è ancora sconosciuto (non ancora trovato)
		 * -parent è gia stato visitato
		 */
		Fermata sourceVertex  = grafo.getEdgeSource(ev.getEdge());
		Fermata targetVertex = grafo.getEdgeTarget(ev.getEdge());
		/**
		 * se il grafo è orientato allora il source sara il parente e il target sara il child.
		 * se non è orientato, potrebbe anche essere il contrario.
		 */
		if (!back.containsKey(targetVertex) && back.containsKey(sourceVertex)) {
			back.put(targetVertex, sourceVertex);
		}else if (!back.containsKey(sourceVertex) && back.containsKey(targetVertex)) {
			back.put(sourceVertex, targetVertex);
		}
		
	}

	@Override
	public void vertexFinished(VertexTraversalEvent<Fermata> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void vertexTraversed(VertexTraversalEvent<Fermata> arg0) {
		// TODO Auto-generated method stub
		
	}


}
