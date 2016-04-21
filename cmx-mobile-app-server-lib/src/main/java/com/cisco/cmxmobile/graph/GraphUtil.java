package com.cisco.cmxmobile.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Transformer;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

public final class GraphUtil {

    private GraphUtil() {
        //Empty Constructor
    }

    public static Graph<Node, Edge> getGraphFromJSON(String json) throws JSONException {
        if (json == null) {
            return null;
        }

        JSONObject rootObj = new JSONObject(json);
        Graph<Node, Edge> graph = new DirectedSparseGraph<Node, Edge>();
        JSONArray nodeArray = rootObj.getJSONArray("nodeList");
        Map<String, Node> nodeMap = new HashMap<String, Node>();
        if (nodeArray != null) {
            for (int i = 0; i < nodeArray.length(); i++) {
                JSONObject nodeObj = nodeArray.getJSONObject(i);
                Node node = new Node(nodeObj.getString("id"));
                node.setX(nodeObj.getDouble("x"));
                node.setY(nodeObj.getDouble("y"));

                // Read attributes and store in the node
                Map<String, Object> attributesMap = new HashMap<String, Object>();
                if (nodeObj.has("attributes")) {
                    JSONObject attributeObj = nodeObj.getJSONObject("attributes");
                    if (attributeObj != null) {
                        Iterator keys = attributeObj.keys();
                        while (keys.hasNext()) {
                            String key = (String) keys.next();
                            Object value = attributeObj.get(key);
                            attributesMap.put(key, value);
                        }
                    }
                }
                node.setAttributes(attributesMap);

                graph.addVertex(node);
                nodeMap.put(node.toString(), node);
            }
        }

        JSONArray edgeArray = rootObj.getJSONArray("edgeList");

        if (edgeArray != null) {
            for (int i = 0; i < edgeArray.length(); i++) {
                JSONObject edgeObj = edgeArray.getJSONObject(i);

                Node sourceNode = nodeMap.get(edgeObj.get("source"));
                Node targetNode = nodeMap.get(edgeObj.get("target"));

                if (sourceNode != null && targetNode != null) {
                    Edge edge = new Edge(sourceNode.toString() + "-" + targetNode.toString());
                    edge.setSource(sourceNode.toString());
                    edge.setTarget(targetNode.toString());
                    graph.addEdge(edge, sourceNode, targetNode);

                    /*
                     * if (edgeObj.get("type").equals("biDirectional")) { //add
                     * an edge from target to source as well Edge edge1 = new
                     * Edge(targetNode.toString() + "-" +
                     * sourceNode.toString());
                     * edge1.setSource(targetNode.toString());
                     * edge1.setTarget(sourceNode.toString());
                     * graph.addEdge(edge1, targetNode, sourceNode); }
                     */
                }
            }
        }
        return graph;
    }

    public static List<Node> getShortestPath(final Graph<Node, Edge> graph, Node source, Node target) throws JSONException {
        Transformer<Edge, Double> wtTransformer = new Transformer<Edge, Double>() {
            public Double transform(Edge link) {
                Pair<Node> node = graph.getEndpoints(link);
                return Math.sqrt(Math.pow((node.getFirst().getX() - node.getSecond().getX()), 2.0d) + Math.pow((node.getFirst().getY() - node.getSecond().getY()), 2.0d));
            }
        };

        // first find the closest node - this is done until i implement the
        // hyperedges
        Node realSource = findClosestNode(source, graph);
        Node realTarget = findClosestNode(target, graph);

        DijkstraShortestPath<Node, Edge> djik = new DijkstraShortestPath<Node, Edge>(graph, wtTransformer);

        List<Edge> path = djik.getPath(realSource, realTarget);
        Iterator<Edge> it = path.iterator();

        List<Node> pathNode = new ArrayList<Node>();
        Node tempSource = null;
        while (it.hasNext()) {
            Edge edge = it.next();
            Pair<Node> pair = graph.getEndpoints(edge);

            if (tempSource == null) {
                Node startNode = new Node(pair.getFirst().getId());
                startNode.setX(pair.getFirst().getX());
                startNode.setY(pair.getFirst().getY());
                startNode.setAttributes(pair.getFirst().getAttributes());
                /*
                 * JSONObject startObject = new JSONObject();
                 * startObject.put("x", pair.getFirst().getX());
                 * startObject.put("y", pair.getFirst().getY());
                 * startObject.put("id", pair.getFirst().getId());
                 * startObject.put("attributes",
                 * GraphUtil.getAttributesInfo(pair.getFirst()));
                 */
                pathNode.add(startNode);
            }

            Node endNode = new Node(pair.getSecond().getId());
            endNode.setX(pair.getSecond().getX());
            endNode.setY(pair.getSecond().getY());
            endNode.setAttributes(pair.getSecond().getAttributes());

            /*
             * JSONObject endObject = new JSONObject(); endObject.put("x",
             * pair.getSecond().getX()); endObject.put("y",
             * pair.getSecond().getY()); endObject.put("id",
             * pair.getSecond().getId()); endObject.put("attributes",
             * GraphUtil.getAttributesInfo(pair.getSecond()));
             */
            pathNode.add(endNode);

            tempSource = source;
        }

        // JSONObject wrapper = new JSONObject();
        // wrapper.put("path", nodeArray);

        return pathNode;
    }

    private static Node findClosestNode(Node source, Graph<Node, Edge> graph) {
        Collection<Node> vertices = graph.getVertices();
        double shortestDistance = Integer.MAX_VALUE;
        Node shortestNode = source;
        for (Node vertex : vertices) {
            Map<String, Object> attrMap = vertex.getAttributes();
            if (attrMap != null && !attrMap.isEmpty() && attrMap.get("type").equals("navNode")) {
                double distance = Math.sqrt(Math.pow((source.getX() - vertex.getX()), 2.0d) + Math.pow((source.getY() - vertex.getY()), 2.0d));
                if (distance > 0 && distance < shortestDistance) {
                    shortestDistance = distance;
                    shortestNode = vertex;
                }
            }

        }
        return shortestNode;
    }
}
