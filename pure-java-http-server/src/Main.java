import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * JDK에 포함된 HttpServer를 사용해 간단한 HTTP 서버를 실행하는 클래스입니다.
 */
public class Main {
    // 서버가 클라이언트의 요청을 기다릴 포트 번호입니다.
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        /*
         * 지정한 포트에 HTTP 서버를 생성합니다.
         * 두 번째 인자인 0은 대기 중인 연결 요청의 최대 개수를
         * 운영체제의 기본 설정에 맡긴다는 의미입니다.
         */
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // /hello 경로로 들어온 요청을 handleHello 메서드가 처리하도록 등록합니다.
        server.createContext("/hello", Main::handleHello);

        /*
         * 프로그램이 Ctrl+C 등으로 종료될 때 실행할 작업을 등록합니다.
         * stop(0)은 진행 중인 요청을 추가로 기다리지 않고 서버를 종료합니다.
         */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nStopping server...");
            server.stop(0);
        }));

        // 등록한 설정을 바탕으로 서버를 시작하고 HTTP 요청을 기다립니다.
        server.start();
        System.out.printf("Server started at http://localhost:%d/hello%n", PORT);
    }

    /**
     * /hello 경로로 들어온 HTTP 요청을 처리합니다.
     *
     * @param exchange 요청 정보와 응답 작성 기능을 제공하는 객체
     */
    private static void handleHello(HttpExchange exchange) throws IOException {
        // GET이 아닌 요청에는 허용된 메서드를 알리고 405 상태 코드를 반환합니다.
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Allow", "GET");
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        // 정상적인 GET 요청에는 200 상태 코드와 인사말을 반환합니다.
        sendResponse(exchange, 200, "Hello, Java HTTP Server!");
    }

    /**
     * 상태 코드와 문자열 본문으로 HTTP 응답을 작성합니다.
     *
     * @param exchange   현재 HTTP 요청과 응답을 나타내는 객체
     * @param statusCode 반환할 HTTP 상태 코드
     * @param body       반환할 문자열 본문
     */
    private static void sendResponse(HttpExchange exchange, int statusCode, String body)
            throws IOException {
        // 문자열을 UTF-8 바이트 배열로 변환해 실제 응답 크기를 계산합니다.
        byte[] response = body.getBytes(StandardCharsets.UTF_8);

        // 클라이언트가 본문을 UTF-8 일반 텍스트로 해석하도록 Content-Type을 설정합니다.
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");

        // HTTP 상태 코드와 본문의 바이트 길이를 클라이언트에 전송합니다.
        exchange.sendResponseHeaders(statusCode, response.length);

        /*
         * 응답 본문 스트림에 데이터를 씁니다.
         * try-with-resources가 작업 후 스트림을 자동으로 닫아 응답을 완료합니다.
         */
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(response);
        }
    }
}
