## 프로젝트 구조
```
├── view/                      # Swing 기반 화면 클래스
│   ├── HomeView.java
│   └── AuthView.java
├── controller/
│   ├── event/                 # UI 이벤트 컨트롤러 (버튼/액션 리스너)
│   │   ├── HomeEventController.java
│   │   └── AuthEventController.java
│   └── business/              # 서버 통신 컨트롤러
│       ├── UserClientController.java
│       ├── LectureClientController.java
│       └── ReservationClientController.java
├── component/                 # 커스텀 Swing 컴포넌트
├── model/                     # DTO 클래스 정의
│   ├── dto/
│   └── entity/
└── ClientMain.java            # 클라이언트 진입점
```
## 실행 방법

### 주의사항
1. Maven pom.xml에 코드가 작성되어 있어 자동으로 프로젝트 실행하면 라이브러리를 전부 가져옵니다.
2. 원드라이브나 구글드라이브에서 넣고 프로젝트 실행하면 못 불러옵니다.
3. 일반적인 로컬 디스크에 프로젝트를 진행해서 열어주세요 (모든 IDE 동일).
4. 넷빈즈를 사용해 UI 수정시 Main 클래스를 실행시켜 컴파일 한번 진행해 주세요.(커스텀 UI 사용하려면 필요한 작업.)
5. TODO를 검색해서 찾아보면 아직작업되지 않은 목록이 있습니다
    - 동적으로 건물의 정보 가져오기 (가이드라인만 적어둠)
    - 동적으로 건물의 층 정보 가져오기 (가이드라인만 적어둠)
    - 동적으로 강의실 정보 가져오기 (가이드라인만 적어둠)

### 요구사항
- Java 21 이상
- Maven 3.x 이상

### 실행 명령어
```
mvn clean package
java -jar target/DeuLectureRoomClient-1.0.0.jar
```
- 서버 주소: localhost:9999
- 리소스 경로: ./data/
