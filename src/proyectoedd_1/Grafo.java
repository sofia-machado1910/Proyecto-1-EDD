/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectoedd_1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author pc
 */
public class Grafo {

    private static final int MAX_STOPS = 10000;
    private String[] stops = new String[MAX_STOPS];
    private boolean[][] connections = new boolean[MAX_STOPS][MAX_STOPS];
    private int stopCount = 0;

    public void loadTransportNetwork() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos JSON", "json");
        fileChooser.setFileFilter(filter);

        int resultado = fileChooser.showOpenDialog(null);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                String jsonString = sb.toString();
                graphCreation(jsonString);
            } catch (IOException e) {
                System.err.println("Error al cargar el archivo: " + e.getMessage());
            }
        } else {
            System.out.println("No se seleccionó ningún archivo.");
        }
    }

    private void graphCreation(String jsonData) {
        JSONObject jsonObject = new JSONObject(jsonData);
        Graph graph = new SingleGraph("Red de Transporte");

        graph.setAttribute("ui.stylesheet", "node { fill-color: lightgreen; size: 25px; text-alignment: center; }");

        for (String redNombre : jsonObject.keySet()) {
            JSONArray lines = jsonObject.getJSONArray(redNombre);
            for (int i = 0; i < lines.length(); i++) {
                JSONObject line = lines.getJSONObject(i);
                String lineName = line.keys().next();
                JSONArray stopsArray = line.getJSONArray(lineName);
                String previousStop = null;

                for (int j = 0; j < stopsArray.length(); j++) {
                    Object stopObj = stopsArray.get(j);
                    String currentStop;

                    if (stopObj instanceof JSONObject) {
                        JSONObject connection = (JSONObject) stopObj;
                        for (String key : connection.keySet()) {
                            String connectedStop = connection.getString(key);
                            currentStop = pedestrianCrossing(key, connectedStop);
                            addStop(graph, currentStop);
                            if (previousStop != null) {
                                addConnection(graph, previousStop, currentStop);
                            }
                            previousStop = currentStop;
                        }
                    } else {
                        currentStop = stopObj.toString();
                        addStop(graph, currentStop);
                        if (previousStop != null) {
                            addConnection(graph, previousStop, currentStop);
                        }
                        previousStop = currentStop;
                    }
                }
            }
        }

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                graph.display();
            }
        });
    }

    private void addStop(Graph graph, String stop) {
        if (findStopIndex(stop) == -1) {
            stops[stopCount] = stop;
            graph.addNode(stop).setAttribute("ui.label", stop);
            stopCount++;
        }
    }

    private void addConnection(Graph graph, String stop1, String stop2) {
        int index1 = findStopIndex(stop1);
        int index2 = findStopIndex(stop2);
        if (index1 != -1 && index2 != -1) {
            connections[index1][index2] = true;
            connections[index2][index1] = true;

            graph.addEdge(stop1 + "-" + stop2, stop1, stop2, true);
        }
    }

    private String pedestrianCrossing(String stop1, String stop2) {
        if (stop1.compareTo(stop2) < 0) {
            return stop1 + "-" + stop2;
        } else {
            return stop2 + "-" + stop1;
        }
    }

    private int findStopIndex(String stop) {
        for (int i = 0; i < stopCount; i++) {
            if (stops[i].equals(stop)) {
                return i;
            }
        }
        return -1;
    }
}

