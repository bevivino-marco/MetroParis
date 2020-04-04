package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.metroparis.db.MetroDAO;
import org.jgrapht.*;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
public class Model {

	private class EdgeTraversedGraphListener implements TraversalListener<Fermata, DefaultWeightedEdge> {

	

		@Override
		public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> ev) {
			
			/**
			 * questo metodo serve pre trovare tutti i posti raggiungibili 
			 * da un nodo partenza, nel caso sia una visita in ampiezza 
			 * e il grafo sia non pesato, garantisce anche il cammino minimo
			 * ma è un caso particolare, negli altri (visita in profondità
			 * o grafo pesato) non garantisce il cammino minimo
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
			if (!backVisit.containsKey(targetVertex) && backVisit.containsKey(sourceVertex)) {
				backVisit.put(targetVertex, sourceVertex);
			}else if (!backVisit.containsKey(sourceVertex) && backVisit.containsKey(targetVertex)) {
				backVisit.put(sourceVertex, targetVertex);
			}
			
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
		public void vertexFinished(VertexTraversalEvent<Fermata> arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void vertexTraversed(VertexTraversalEvent<Fermata> arg0) {
			// TODO Auto-generated method stub
			
		}
	
	}
	private Graph<Fermata, DefaultWeightedEdge> grafo;
	private List<Fermata> fermate;
	private Map <Integer, Fermata> fermateIdMap;
	private Map <Fermata, Fermata> backVisit;

public void creaGrafo(){
	// 1. crea l oggetto grafo
	this.grafo =new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    // 2. aggiungere i vertici
	MetroDAO dao = new MetroDAO();
	this.fermate= dao.getAllFermate();
	Graphs.addAllVertices(this.grafo, this.fermate);
    // 3. aggiungere gli archi
    // ci sono 3 approcci generale ( in base a dove metto la complessita)
	// opzione 2
	/*for (Fermata partenza : this.grafo.vertexSet()) {
		for (Fermata arrivo : this.grafo.vertexSet()) {
			if (dao.esisteConnessione(partenza, arrivo)) {
				this.grafo.addEdge(partenza, arrivo);
			}
		}
	}*/
	this.fermateIdMap= new HashMap<>();
	for (Fermata f : fermate) {
		fermateIdMap.put(f.getIdFermata(), f);
	}
	
	// aggiungi gli archi (opzione 2)
	for (Fermata partenza : this.grafo.vertexSet()) {
		List<Fermata> arrivi = dao.stazioniArrivo(partenza, fermateIdMap);
		for (Fermata arrivo : arrivi) {
			this.grafo.addEdge(partenza, arrivo); 
		}
	}
	// aggiungi peso agli archi
	// posso anche rifare il grafo da capo o graphs. addEdgeWithVertices
	List <ConnessioneVelocita> archiPesati = dao.getConnessioniVelocita();
	for (ConnessioneVelocita cp : archiPesati) {
		Fermata partenza = fermateIdMap.get(cp.getStazP());
		Fermata arrivo = fermateIdMap.get(cp.getStazA());
		double distanza = LatLngTool.distance(partenza.getCoords(), arrivo.getCoords()	, LengthUnit.KILOMETER);
		double peso = (distanza/cp.getVelocita()) * 3600;
		grafo.setEdgeWeight(partenza, arrivo, peso);
		// Graphs.addEdgeWithVertices(grafo , partenza, arrivo, peso)
	}

}
public Graph<Fermata, DefaultWeightedEdge> getGrafo(){
	return grafo;
	}

public List<Fermata> fermateRaggiungibili(Fermata source){
	List <Fermata> result = new ArrayList<Fermata>();
	backVisit = new HashMap<>();
	GraphIterator <Fermata, DefaultWeightedEdge> it = new DepthFirstIterator<>(this.grafo, source);
	//GraphIterator <Fermata, DefaultEdge> it = new BreadthFirstIterator<>(this.grafo, source);
    backVisit.put(source, null); // la radice non ha un padre
	it.addTraversalListener(new Model.EdgeTraversedGraphListener());
	/**
	 * faccio una classe interna anonima, usa e getta
	 
	it.addTraversalListener(new TraversalListener<Fermata, DefaultEdge>() {

		@Override
		public void connectedComponentFinished(ConnectedComponentTraversalEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void connectedComponentStarted(ConnectedComponentTraversalEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void vertexFinished(VertexTraversalEvent<Fermata> arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void vertexTraversed(VertexTraversalEvent<Fermata> arg0) {
			// TODO Auto-generated method stub
			
		}});
	
	*/
	while (it.hasNext()) {
		result.add(it.next());
	}
	System.out.println(backVisit);
	return result;
}
 public List<Fermata> getFermate()
 {
	 return this.fermate;
 }

public List<Fermata> percorsoFinoA(Fermata target){
	if (!backVisit.containsKey(target)) {
		// il target non è raggiungibile dalla source
		return null;
	}
	List<Fermata> percorso = new LinkedList<>();
	Fermata f = target;
	while (f!= null) {
	percorso.add(0, f);
	f = backVisit.get(f);
	}
	return percorso;
}

public List <Fermata> trovaCamminoMinimo(Fermata partenza, Fermata arrivo){
	DijkstraShortestPath <Fermata, DefaultWeightedEdge> dijkstra = new DijkstraShortestPath<>(this.grafo);
	GraphPath <Fermata, DefaultWeightedEdge>path = dijkstra.getPath(partenza, arrivo);
	return path.getVertexList();
}

}

