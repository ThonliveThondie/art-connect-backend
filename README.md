# 🎨 ArtConnect
> AI를 통해 똑부러지게 소상공인과 디자이너를 매칭하는 플랫폼 ArtConnect입니다.

# 📜 아키텍처 설계 & API 명세서
주요 아키텍처 설계와 API 명세서는 노션에 별도로 작성하였습니다.

- <a href="https://accidental-radon-18f.notion.site/2736fd8c80d28150ab99d9337d4aab51?v=2736fd8c80d281f8b144000c62cdc71a">
      <img src="https://img.shields.io/badge/ArtConnect Architecture Docs-000000?style=flat-square&logo=Notion&logoColor=white"/>
</a>

- <a href="https://accidental-radon-18f.notion.site/2736fd8c80d2812ab198dc74f611e10b?v=2736fd8c80d2812bb858000ca48edfb3&pvs=74">
      <img src="https://img.shields.io/badge/ArtConnect API Docs-000000?style=flat-square&logo=Notion&logoColor=white"/>
</a>

# ✒️ 프로젝트 구조
<img width="5605" height="2910" alt="artconnect-architecture drawio_최종" src="https://github.com/user-attachments/assets/cb5e5b19-9a3f-4ad1-86c8-e0a1e289fe03" />

# 📊 ERD 다이어그램
<img width="1804" height="1392" alt="ERD 다이어그램" src="https://github.com/user-attachments/assets/29b605b8-c565-4526-9ca9-b7cc5b1bf386" />

# ⚙️ 기술 스택
<img src="https://img.shields.io/badge/JAVA-007396?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/Spring Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"> <img src="https://img.shields.io/badge/Spring Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white"> <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"/> <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=MySQL&logoColor=white"/> <img src="https://img.shields.io/badge/AWS EC2-F58534?style=for-the-badge&logo=amazonwebservices&logoColor=white"/> <img src="https://img.shields.io/badge/AWS RDS-2E73B8?style=for-the-badge&logo=amazonwebservices&logoColor=white"/>

# ✋ 시작 가이드
> 아래에 내용에 해당하는 시작 가이드는 단순히 로컬 환경에서 서버를 실행해보기 위한 내용입니다.
> 추가로 배포가 필요하다면 아래 링크를 참고하여 AWS EC2에 서버를 실행한 다음, HTTPS로 적용하는 과정이 필요합니다.

- EC2 & RDS를 통한 간단하게 서버 배포하기: <https://docs.aws.amazon.com/ko_kr/AWSEC2/latest/UserGuide/tutorial-connect-ec2-instance-to-rds-database.html>

- HTTP 서버를 HTTPS로 변경하기: <https://hoonsb.tistory.com/83#2.%20%ED%95%B4%EA%B2%B0-1>

## 요구사항
- Java 21 이상
- Spring Boot 3.5.x 이상
- MySQL 8.0 이상
- Docker Desktop 27.x 이상

## SpringBoot & Docker 환경 설정
### 1. 아래 주소에 있는 art-connect-backend repository clone

```
git clone https://github.com/ThonliveThondie/art-connect-backend
```

### 2. application-ai.yml, application-awss3.yml, application-db.yml, application-jwt.yml, application-oauth.yml, .env 파일 별도 생성 후 추가

위에 언급한 파일은 보안 상 깃허브에 업로드하지 않아 포함되지 않은 파일들입니다.

아래에 표시된 `{}` 영역을 각자 설정에 맞게 작성해주세요.

#### application-ai.yml

Spring AI를 통해 Anthropic AI를 사용하기 위해서는 API 키 등록이 필요합니다. 아래 링크에서 API 키 생성 후 파일에 작성해주세요.

- Anthropic Claude Console: <https://console.anthropic.com/login>

```
local-ai:
  anthropic:
    claude:
      api-key: {ANTHROPIC_API_KEY}
      chat:
        options:
          model: claude-3-5-sonnet-20241022
          temperature: 0.7
          max-tokens: 800
```

#### application-awss3.yml

현재 프로젝트에서는 사용자가 이미지를 업로드하고 다운받는 기능을 제공하기 위해 AWS S3 버킷을 사용하고 있습니다. 아래 링크를 참고해서 버킷 생성 후 설정값을 입력해주세요.

- Spring Boot로 S3 이미지 업로드 기능 구현하기: <https://yel-m.tistory.com/19>

```
# AWS S3
cloud:
  aws:
    credentials:
      access-key: {ACCESS_KEY}
      secret-key: {SECRET_KEY}
    region:
      static: {BUCKET_REGION}  # 버킷의 리전
    s3:
      bucket: {BUCKET_NAME}   # 버킷 이름
    stack:
      auto: false

spring:
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB
```

#### application-db.yml

DB인 MySQL 연결에 필요한 설정값을 입력합니다.

```
local-db:
  mysql:
    host: localhost
    port: 3308
    name: artconnect
    username: {MySQL 사용자 이름}
    password: {MySQL 사용자 비밀번호}
```

#### application-jwt.yml
JWT 시크릿 키의 안전한 생성을 위해 OpenSSL 터미널 명령어로 난수를 생성합니다. OpenSSL 설치는 아래 링크를 참고해주세요.

- OpenSSL 설치 공식 사이트: <https://slproweb.com/products/Win32OpenSSL.html>

다음 명령을 통해 난수를 생성한 다음, Base64 형식으로 인코딩하여 출력합니다.
```
openssl rand -base64 64
```

출력된 키를 복사하여 아래의 application-jwt.yml 파일에 작성해줍니다.
```
# JWT 설정
jwt:
  secret-key: {JWT SECRET KEY}
  
  # Access Token 설정 (15분)
  access:
    expiration: 900000
    header: Authorization
  
  # Refresh Token 설정 (7일)
  refresh:
    expiration: 604800000
    header: Authorization-refresh
```

#### application-oauth.yml
해당 파일은 OAuth 2.0 소셜 로그인 적용을 위해 작성한 파일입니다.

OAuth 2.0에 대한 자세한 내용과 설정법은 아래 링크를 참고해주세요.

- Spring Security OAuth 2.0을 사용하여 소셜 로그인 구현: <https://ksh-coding.tistory.com/63>

Google, Kakao, Naver 개발자 센터에서 API 이용 등록 후 application-oauth.yml에 작성할 파일은 다음과 같습니다.

```
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: {클라이언트 아이디}
            client-secret: {클라이언트 비밀키}
            scope: profile, email
            redirect-uri: {등록한 리다이렉션 URI}

          naver:
            client-id: {클라이언트 아이디}
            client-secret: {클라이언트 비밀키}
            redirect-uri: {등록한 리다이렉션 URI}
            authorization-grant-type: authorization_code
            scope: name, email, profile_image
            client-name: Naver

          kakao:
            client-id: {클라이언트 아이디}
            client-secret: {클라이언트 비밀키}
            redirect-uri: {등록한 리다이렉션 URI}
            client-authentication-method: client_secret_post
            authorization-grant-type: authorization_code
            scope: profile_nickname, profile_image
            client-name: Kakao

        provider:
          naver:
            authorization-uri: https://nid.naver.com/oauth2.0/authorize
            token-uri: https://nid.naver.com/oauth2.0/token
            user-info-uri: https://openapi.naver.com/v1/nid/me
            user-name-attribute: response

          kakao:
            authorization-uri: https://kauth.kakao.com/oauth/authorize
            token-uri: https://kauth.kakao.com/oauth/token
            user-info-uri: https://kapi.kakao.com/v2/user/me
            user-name-attribute: id
```

#### .env
```
MYSQL_DATABASE: artconnect
MYSQL_USERNAME: {MySQL 사용자 이름}
MYSQL_ROOT_PASSWORD: {MySQL 사용자 비밀번호}
```

위에서 작성한 5개 파일을 아래 사진의 프로젝트 디렉터리 구조(`src/main/resources/security/`)와 정확히 일치하도록 넣어주세요.

<img width="296" height="245" alt="image" src="https://github.com/user-attachments/assets/bcc40978-7436-41ec-8cc7-f6bb47b8600c" />

### 3. 도커 실행
Intellij 터미널 창 또는 다른 터미널 창을 통해 `docker-compose.yml` 파일 위치로 이동 후 docker compose 하여 MySQL 데이터베이스 컨테이너를 새롭게 추가하여 실행합니다.

```
docker compose up -d
```
