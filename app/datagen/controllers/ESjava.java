package datagen.controllers;

import static org.elasticsearch.node.NodeBuilder.*;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;

import static org.elasticsearch.client.Requests.indexRequest;

public class ESjava {
	Node node;
	Client client;
	BulkRequestBuilder bulk;
	private String indexName;
	
	public ESjava(){		
	}
	
	public void init(String clusterName, String indexName){
		Node node;
		String esConfig = "/etc/elasticsearch/elasticsearch.yml";
		if(!"".equals(clusterName)){
			System.out.println("use clustername and not settings from file"+esConfig);
			node = nodeBuilder().clusterName(clusterName).client(true).node();
		}else{
			System.out.println("load settings from "+esConfig);
			node = nodeBuilder().settings(nodeBuilder().settings().					
					loadFromSource(esConfig).build()).client(true).node();
		}
		client = node.client();
		bulk = client.prepareBulk();
		this.indexName = indexName;
	}
	
	public void addToBatch(StringBuilder sb){
		bulk.add(indexRequest(indexName).type("measurement").source(sb.toString().getBytes()));
		//System.out.println("adding to bulk: "+sb.toString());	
	}
	
	public void sendBatch(){
		ListenableActionFuture<BulkResponse> execute = bulk.execute();
		BulkResponse response = execute.actionGet();
		if(response.hasFailures()){
			System.out.println(response.buildFailureMessage());
		}else{
			//System.out.println("bulk ok");
		}
		
		bulk = client.prepareBulk();
	}

}
