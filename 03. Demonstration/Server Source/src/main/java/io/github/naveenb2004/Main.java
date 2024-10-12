package io.github.naveenb2004;

import com.google.gson.Gson;
import io.github.lycoriscafe.nexus.http.HTTPServer;
import io.github.lycoriscafe.nexus.http.configuration.HTTPServerConfiguration;
import io.github.lycoriscafe.nexus.http.core.HTTPEndpoint;
import io.github.lycoriscafe.nexus.http.core.requestMethods.annotations.GET;
import io.github.lycoriscafe.nexus.http.core.requestMethods.annotations.POST;
import io.github.lycoriscafe.nexus.http.core.statusCodes.HTTPStatusCode;
import io.github.lycoriscafe.nexus.http.engine.ReqResManager.HTTPRequest;
import io.github.lycoriscafe.nexus.http.engine.ReqResManager.HTTPResponse;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@HTTPEndpoint("/")
public class Main {
    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {
        HTTPServerConfiguration httpServerConfiguration = new HTTPServerConfiguration(Main.class).setPort(2004);
        HTTPServer httpServer = new HTTPServer(httpServerConfiguration);
        httpServer.start();
    }

    private static final Gson gson = new Gson();
    private static SensorData sensorData;

    @GET("/")
    public static HTTPResponse<?> webIndex(HTTPRequest<?> request) {
        HTTPResponse<File> index = new HTTPResponse<>(request.getREQUEST_ID());
        index.setStatusCode(HTTPStatusCode.OK);
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Type", List.of("text/html"));
        index.setHeaders(headers);
        index.setContent(new File("website\\index.html"));
        return index;
    }

    @GET("/styles.css")
    public static HTTPResponse<?> webStyles(HTTPRequest<?> request) {
        HTTPResponse<File> styles = new HTTPResponse<>(request.getREQUEST_ID());
        styles.setStatusCode(HTTPStatusCode.OK);
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Type", List.of("text/css"));
        styles.setHeaders(headers);
        styles.setContent(new File("website\\styles.css"));
        return styles;
    }

    @GET("/script.js")
    public static HTTPResponse<?> webScript(HTTPRequest<?> request) {
        HTTPResponse<File> script = new HTTPResponse<>(request.getREQUEST_ID());
        script.setStatusCode(HTTPStatusCode.OK);
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Type", List.of("text/javascript"));
        script.setHeaders(headers);
        script.setContent(new File("website\\script.js"));
        return script;
    }

    @POST("/set-sensor-data")
    public static HTTPResponse<?> setSensorData(HTTPRequest<?> request) {
        byte[] b = (byte[]) request.getContent();
        sensorData = gson.fromJson(new String(b), SensorData.class);
        System.out.println("Water level : " + sensorData.getLevel()
                + " | Water quality : " + sensorData.getQuality()
                + " | Relay : " + (sensorData.getRelay() == 0 ? "OFF" : "ON"));

        HTTPResponse<String> httpResponse = new HTTPResponse<>(request.getREQUEST_ID());
        httpResponse.setStatusCode(HTTPStatusCode.OK);
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Length", List.of("0"));
        httpResponse.setHeaders(headers);
        return httpResponse;
    }

    @GET("/get-sensor-data")
    public static HTTPResponse<?> getSensorData(HTTPRequest<?> request) {
        HTTPResponse<String> httpResponse = new HTTPResponse<>(request.getREQUEST_ID());
        httpResponse.setStatusCode(HTTPStatusCode.OK);
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Type", List.of("text/json"));
        httpResponse.setHeaders(headers);
        httpResponse.setContent(SensorData.getWebData());
        return httpResponse;
    }

    public static class SensorData {
        private int level;
        private int quality;
        private int relay;

        public void setLevel(int level) {
            this.level = level;
        }

        public void setQuality(int quality) {
            this.quality = quality;
        }

        public void setRelay(int relay) {
            this.relay = relay;
        }

        public int getLevel() {
            return level;
        }

        public int getQuality() {
            return quality;
        }

        public int getRelay() {
            return relay;
        }

        public static String getWebData() {
            SensorData webData = new SensorData();
            webData.setLevel(Math.abs(((sensorData.getLevel() - 1) - 15) * 100 / 15));
            webData.setQuality(Math.abs(sensorData.getQuality() * 100 / 1024));
            webData.setRelay(sensorData.getRelay());
            return gson.toJson(webData);
        }
    }
}