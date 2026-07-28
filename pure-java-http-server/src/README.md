# Pure Java HTTP Server

외부 라이브러리와 빌드 도구 없이 JDK의 `HttpServer` API로 만든 최소 HTTP 서버입니다.

## 요구 사항

- JDK 21 이상
- `curl` (응답 확인용)

## 컴파일

프로젝트 루트에서 다음 명령을 실행합니다.

```bash
cd pure-java-http-server
mkdir -p out
javac --add-modules jdk.httpserver -d out src/Main.java
```

## 실행

```bash
java --add-modules jdk.httpserver -cp out Main
```

서버는 `8080` 포트에서 실행됩니다.

## 요청

```bash
curl -i http://localhost:8080/hello
```

정상 응답:

```text
HTTP/1.1 200 OK
Content-Type: text/plain; charset=UTF-8

Hello, Java HTTP Server!
```

`GET` 이외의 메서드는 `405 Method Not Allowed`를 반환합니다.

```bash
curl -i -X POST http://localhost:8080/hello
```

등록되지 않은 경로는 JDK `HttpServer`가 `404 Not Found`를 반환합니다.

```bash
curl -i http://localhost:8080/unknown
```

서버를 종료하려면 실행 중인 터미널에서 `Ctrl+C`를 누릅니다.
