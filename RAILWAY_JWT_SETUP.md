# Railway JWT 환경 변수 설정 가이드

## 🚨 문제 상황
모든 API 요청이 401 Unauthorized 에러를 반환하고 있습니다.

```
GET /categories → 401
GET /campaigns/status → 401
GET /users/profile → 401
```

## 🔍 원인
Railway에 `JWT_SECRET` 환경 변수가 설정되지 않아서, 백엔드가 토큰을 검증하지 못하고 있습니다.

## ✅ 해결 방법

### 1. Railway 대시보드 접속
1. [Railway Dashboard](https://railway.app) 접속
2. `review_admin` 프로젝트 선택
3. 백엔드 서비스 클릭

### 2. 환경 변수 설정
1. **Variables** 탭 클릭
2. **New Variable** 버튼 클릭
3. 다음 값 입력:
   ```
   Key: JWT_SECRET
   Value: subeen-admin-service-secret-key-2024-01-30-secure
   ```
4. **Add** 버튼 클릭

### 3. 서비스 재시작
환경 변수를 추가하면 Railway가 자동으로 재배포합니다.
- 재배포 완료까지 약 1~2분 소요
- Deployments 탭에서 상태 확인 가능

### 4. 테스트
1. 앱에서 **로그아웃**
2. **재로그인**
3. 정상 작동 확인

## 📝 참고사항
- `prod.env` 파일은 로컬 개발용 참고 파일입니다
- Railway는 대시보드에서 설정한 환경 변수만 사용합니다
- 환경 변수 변경 시 항상 재배포가 필요합니다

## 🔐 보안 권장사항
프로덕션 환경에서는 더 강력한 시크릿 키 사용을 권장합니다:
```bash
# 예시: 64자 이상의 랜덤 문자열
openssl rand -base64 64
```
