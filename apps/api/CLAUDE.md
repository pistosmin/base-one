# Backend Context — Spring Boot 4.0.3 + Java 21

## 빌드 & 테스트 명령어
```bash
cd apps/api
./gradlew bootRun --args='--spring.profiles.active=dev'  # 개발 서버 시작
./gradlew compileJava          # 컴파일 체크 (QueryDSL Q클래스 생성 포함)
./gradlew test                 # 전체 테스트 실행
./gradlew build -x test        # 프로덕션 빌드 (테스트 제외)
```

## 패키지 구조
```
com.community/
├── CommunityApplication.java     # 메인 진입점
├── global/                        # 전역 설정 & 공통 모듈
│   ├── config/                    # SecurityConfig, JpaConfig, RedisConfig, CorsConfig, SwaggerConfig
│   ├── security/jwt/              # JwtTokenProvider, JwtAuthFilter, JwtTokenDto
│   ├── common/response/           # ApiResponse<T>, PageResponse<T>
│   ├── common/entity/             # BaseTimeEntity (createdAt, updatedAt)
│   ├── common/exception/          # ErrorCode, BusinessException, GlobalExceptionHandler
│   └── util/                      # SecurityUtil, SlugUtil
└── domain/                        # 도메인별 패키지
    ├── auth/                      # 인증 (controller, service, dto, repository)
    ├── user/                      # 사용자 (entity, repository, dto, mapper, service, controller)
    ├── post/                      # 게시글 (entity, repository, dto, mapper, service, controller)
    ├── comment/                   # 댓글
    ├── vote/                      # 추천
    ├── bookmark/                  # 북마크
    ├── notification/              # 알림 ★MSA 분리 1순위
    ├── media/                     # 미디어 ★MSA 분리 2순위
    ├── report/                    # 신고
    └── admin/                     # 관리자
```

## Spring Boot 4.0 핵심 주의사항
1. **패키지**: `jakarta.*` 사용. `javax.*`는 **존재하지 않음**.
   ```java
   // ✅ 올바름
   import jakarta.persistence.Entity;
   import jakarta.validation.constraints.NotBlank;
   // ❌ 컴파일 에러
   import javax.persistence.Entity;
   ```
2. **Security**: `SecurityFilterChain`을 `@Bean`으로 등록. `WebSecurityConfigurerAdapter`는 **삭제된 클래스**.
3. **Virtual Threads**: `spring.threads.virtual.enabled=true` 활성화됨. `synchronized` 대신 `ReentrantLock` 권장.
4. **JSpecify null-safety**: `@Nullable`, `@NonNull` 어노테이션이 JSpecify로 통합.

## Entity 작성 규칙
```java
@Entity
@Table(name = "테이블명")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class EntityName extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Setter 금지! 비즈니스 메서드로 상태 변경
    public void update(String field) {
        this.field = field;
    }
}
```

## DTO 작성 규칙
```java
// Request DTO — record + 검증 어노테이션
public record CreatePostRequest(
    @NotBlank @Size(max = 200) String title,
    @NotBlank String content,
    Long categoryId  // nullable은 어노테이션 없이
) {}

// Response DTO — record
public record PostDetailResponse(
    Long id,
    UserSummaryResponse author,
    String title,
    // ... 한국어 JavaDoc 필수
) {}
```

## Service 작성 패턴
- 읽기 메서드: `@Transactional(readOnly = true)`
- 쓰기 메서드: `@Transactional`
- 에러 처리: `BusinessException(ErrorCode.XXX)` throw
- 의존성: `@RequiredArgsConstructor` + `private final` 필드

## 테스트 작성 가이드
- 서비스 테스트: `@ExtendWith(MockitoExtension.class)` + `@Mock` + `@InjectMocks`
- 통합 테스트: `@SpringBootTest` + `@Testcontainers` (PostgreSQL, Redis)
- 컨트롤러 테스트: `@WebMvcTest` + `MockMvc`

## QueryDSL 사용 시
- Q클래스가 없으면 `./gradlew compileJava` 실행
- Repository: `PostRepositoryCustom` (인터페이스) + `PostRepositoryImpl` (구현)
- N+1 방지: `.join(post.author).fetchJoin()`

## Flyway 마이그레이션
- 경로: `src/main/resources/db/migration/`
- 파일명: `V{번호}__{설명}.sql` (언더바 2개)
- TIMESTAMP: 항상 `WITH TIME ZONE`
- 새 마이그레이션 추가 시 기존 파일 수정 금지
