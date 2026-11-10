
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {

    static StudentService service = new StudentService();

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8080), 0);

        server.createContext("/", Main::handleRequest);
        server.setExecutor(null);
        server.start();

        System.out.println("Student Management System Started!");
        System.out.println("Open http://localhost:8080");
    }

    private static void handleRequest(HttpExchange exchange) {

        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if (path.startsWith("/api/")) {
                handleAPI(exchange, path, method);
            } else {
                serveFrontend(exchange, path);
            }

        } catch (Exception e) {
            try {
                send(exchange, 500,
                        "{\"error\":\"" + escape(e.getMessage()) + "\"}",
                        "application/json");
            } catch (Exception ignored) {
                exchange.close();
            }
        }
    }

    private static void handleAPI(
            HttpExchange exchange, String path, String method)
            throws Exception {

        if (method.equals("OPTIONS")) {
            exchange.getResponseHeaders().set(
                    "Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set(
                    "Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().set(
                    "Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        if (path.equals("/api/students") && method.equals("GET")) {
            send(exchange, 200, studentsJSON(service.getAllStudents()),
                    "application/json");
            return;
        }

        if (path.equals("/api/topper") && method.equals("GET")) {
            Student topper = service.getTopper();

            send(exchange, 200,
                    topper == null ? "null" : studentJSON(topper),
                    "application/json");
            return;
        }

        if (!method.equals("POST")) {
            send(exchange, 405, "{\"error\":\"Method not allowed\"}",
                    "application/json");
            return;
        }

        Map<String, String> data = readForm(exchange);

        if (path.equals("/api/add")) {

            Student student = new Student(
                    integer(data, "id"),
                    required(data, "name"),
                    required(data, "email"),
                    decimal(data, "marks"),
                    integer(data, "attendance"),
                    StudentStatus.valueOf(
                            data.getOrDefault("status", "ACTIVE")),
                    new Course(required(data, "course"), "Not specified")
            );

            service.addStudent(student);

            send(exchange, 200,
                    "{\"message\":\"Student added successfully\"}",
                    "application/json");

        } else if (path.equals("/api/update")) {

            service.updateStudent(
                    integer(data, "id"),
                    decimal(data, "marks"),
                    integer(data, "attendance")
            );

            send(exchange, 200,
                    "{\"message\":\"Student updated successfully\"}",
                    "application/json");

        } else if (path.equals("/api/delete")) {

            service.removeStudent(integer(data, "id"));

            send(exchange, 200,
                    "{\"message\":\"Student deleted successfully\"}",
                    "application/json");

        } else {
            send(exchange, 404, "{\"error\":\"API not found\"}",
                    "application/json");
        }
    }

    private static Map<String, String> readForm(
            HttpExchange exchange) throws Exception {

        String body = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8);

        Map<String, String> data = new HashMap<>();

        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);

            String key = URLDecoder.decode(
                    parts[0], StandardCharsets.UTF_8);

            String value = parts.length > 1
                    ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                    : "";

            data.put(key, value);
        }

        return data;
    }

    private static String required(
            Map<String, String> data, String key) {

        String value = data.get(key);

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(key + " is required");
        }

        return value.trim();
    }

    private static int integer(
            Map<String, String> data, String key) {

        return Integer.parseInt(required(data, key));
    }

    private static double decimal(
            Map<String, String> data, String key) {

        return Double.parseDouble(required(data, key));
    }

    private static String studentsJSON(List<Student> students) {

        StringBuilder json = new StringBuilder("[");
        boolean first = true;

        for (Student student : students) {
            if (!first) json.append(",");
            json.append(studentJSON(student));
            first = false;
        }

        return json.append("]").toString();
    }

    private static String studentJSON(Student s) {

        return "{"
                + "\"id\":" + s.getId() + ","
                + "\"name\":\"" + escape(s.getName()) + "\","
                + "\"email\":\"" + escape(s.getEmail()) + "\","
                + "\"marks\":" + s.getMarks() + ","
                + "\"attendance\":" + s.getAttendance() + ","
                + "\"grade\":\"" + s.calculateGrade() + "\","
                + "\"status\":\"" + s.getStatus() + "\","
                + "\"course\":\"" +
                escape(s.getCourse().getCourseName()) + "\""
                + "}";
    }

    private static String escape(String value) {

        if (value == null) return "";

        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static void serveFrontend(
            HttpExchange exchange, String urlPath) throws Exception {

        String fileName = urlPath.equals("/")
                ? "index.html" : urlPath.substring(1);

        Path root = Path.of("frontend").toRealPath();
        Path file = root.resolve(fileName).normalize();

        if (!file.startsWith(root) || !Files.isRegularFile(file)) {
            send(exchange, 404, "File not found", "text/plain");
            return;
        }

        String type;

        if (fileName.endsWith(".html")) {
            type = "text/html";
        } else if (fileName.endsWith(".css")) {
            type = "text/css";
        } else if (fileName.endsWith(".js")) {
            type = "application/javascript";
        } else {
            type = "application/octet-stream";
        }

        byte[] content = Files.readAllBytes(file);
        exchange.getResponseHeaders().set(
                "Content-Type", type + "; charset=UTF-8");

        exchange.sendResponseHeaders(200, content.length);
        exchange.getResponseBody().write(content);
        exchange.close();
    }

    private static void send(
            HttpExchange exchange, int status,
            String response, String contentType) throws Exception {

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type", contentType + "; charset=UTF-8");

        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
