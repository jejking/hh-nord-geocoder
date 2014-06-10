package info.jejking.hamburg.nord.drucksachen.importer;

import info.jejking.hamburg.nord.drucksachen.allris.RawDrucksache;
import info.jejking.hamburg.nord.drucksachen.matcher.RawDrucksacheWithLabelledMatches;
import info.jejking.hamburg.nord.geocoder.AbstractNeoImporter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;


public class DrucksachenImporter extends AbstractNeoImporter<Iterable<File>>{

    @Override
    public void writeToNeo(Iterable<File> files, final GraphDatabaseService graph) {
        Observable.from(files)
        .map(new Func1<File, RawDrucksache>() {

            @Override
            public RawDrucksache call(File t1) {
                try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(t1)))) {
                    return (RawDrucksache) ois.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            
        })
        .map(new Func1<RawDrucksache, RawDrucksache>() {
            // attempt to extract a date from the contents if we have optional absent.
            @Override
            public RawDrucksache call(RawDrucksache t1) {
                // TODO Auto-generated method stub
                return null;
            }
            
        })
        .map(new Func1<RawDrucksache, RawDrucksacheWithLabelledMatches>() {

            @Override
            public RawDrucksacheWithLabelledMatches call(RawDrucksache t1) {
                // TODO run the matchers and attach their results
                return null;
            }
            
        })
        .subscribe(new Action1<RawDrucksacheWithLabelledMatches>() {

            @Override
            public void call(RawDrucksacheWithLabelledMatches t1) {
                try (Transaction tx = graph.beginTx()) {
                    
                    Node drucksachenNode = writeDruckSache(graph);
                    
                    createRelationshipsToGazetteer(drucksachenNode, graph);
                    
                    tx.success();
                }
                
                
            }

            private void createRelationshipsToGazetteer(Node drucksachenNode, GraphDatabaseService graph) {
                // TODO Auto-generated method stub
                
            }

            private Node writeDruckSache(GraphDatabaseService graph) {
                // TODO Auto-generated method stub
                return null;
            }
        });
        
        
    }

}
