# Maven Hello World

Java 21과 Maven 표준 디렉터리 구조를 사용하는 간단한 콘솔 애플리케이션입니다.
Maven Wrapper가 포함되어 있어 Maven을 별도로 설치하지 않아도 됩니다.

## 테스트

Windows PowerShell 또는 명령 프롬프트:

```powershell
./mvnw.cmd test
```

macOS 또는 Linux:

```bash
sh ./mvnw test
```

## 빌드 및 실행

Windows:

```powershell
./mvnw.cmd package
java -jar target/maven-hello-world-1.0-SNAPSHOT.jar
```

macOS 또는 Linux:

```bash
sh ./mvnw package
java -jar target/maven-hello-world-1.0-SNAPSHOT.jar
```

정상 출력:

```text
Hello, Maven!
```

Wrapper를 처음 실행할 때 Apache Maven과 프로젝트 의존성을 다운로드하므로 네트워크 연결이 필요합니다.
