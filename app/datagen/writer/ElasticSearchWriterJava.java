package datagen.writer;

import static org.elasticsearch.client.Requests.indexRequest;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

public class ElasticSearchWriterJava {
	private Node node;
	private Client client;
	private BulkRequestBuilder bulk;
	private String indexName;
	
	public ElasticSearchWriterJava(String clusterName, Boolean multicast, String hosts, String indexName){
		if(!"".equals(clusterName)){
			System.out.println("use clustername "+clusterName+" and not settings from file");
			NodeBuilder nb = nodeBuilder().clusterName(clusterName).client(true);
			if(!multicast){
				nb.settings().put("discovery.zen.ping.multicast.enabled", multicast);
				nb.settings().put("discovery.zen.ping.unicast.hosts", hosts);
			}
			node = nb.node();
		}else{
			String esConfig = "/etc/elasticsearch/elasticsearch.yml";
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
	
	public void close(){
		client.close();
		node.close();
	}
}
