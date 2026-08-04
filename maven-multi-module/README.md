# Maven Multi Module

Java 21, Maven, Spring Boot를 사용하는 중첩 멀티 모듈 예제입니다. 총 8개의 `pom.xml`을 통해 부모 설정 상속, 모듈 집계, 모듈 간 의존성을 보여줍니다.

## 구조

```text
maven-multi-module/
├── pom.xml
├── libraries/
│   ├── pom.xml
│   ├── greeting-domain/
│   │   └── pom.xml
│   └── greeting-service/
│       └── pom.xml
└── applications/
    ├── pom.xml
    └── services/
        ├── pom.xml
        ├── greeting-api/
        │   └── pom.xml
        └── greeting-batch/
            └── pom.xml
```

`greeting-service`는 `greeting-domain`에 의존하며, `greeting-api`와 `greeting-batch`는 `greeting-service`를 재사용합니다. 루트와 중간 디렉터리의 POM은 하위 모듈을 묶는 집계자 역할을 합니다.

## 전체 빌드

Windows PowerShell:

```powershell
./mvnw.cmd clean verify
```

macOS 또는 Linux:

```bash
sh ./mvnw clean verify
```

## 선택 모듈 빌드

`-pl`은 빌드할 모듈을 선택하고 `-am`은 해당 모듈에 필요한 의존 모듈도 함께 빌드합니다.

```powershell
./mvnw.cmd -pl applications/services/greeting-api -am package
```

## REST API 실행

```powershell
./mvnw.cmd -pl applications/services/greeting-api -am spring-boot:run
```

다른 터미널에서 호출합니다.

```powershell
Invoke-RestMethod "http://localhost:8080/api/greetings?name=Codex"
```

응답:

```json
{"message":"Hello, Codex!"}
```

`name`을 생략하거나 공백으로 전달하면 `Hello, World!`을 반환합니다.

## 배치 실행

먼저 전체 프로젝트를 빌드한 다음 실행 가능한 JAR을 시작합니다.

```powershell
./mvnw.cmd package
java -jar applications/services/greeting-batch/target/greeting-batch-1.0-SNAPSHOT.jar --name=Codex
```

출력:

```text
Hello, Codex!
```
