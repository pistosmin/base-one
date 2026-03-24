#!/bin/sh
# ====================================================
# MinIO 초기 버킷 생성 스크립트
#
# Docker Compose의 minio-init 서비스에서 실행됩니다.
# MinIO가 정상 기동된 후, community-media 버킷을 생성하고
# 익명 읽기 권한을 설정합니다.
#
# 버킷이 이미 존재하면 무시하고 정상 종료합니다.
# ====================================================

echo "⏳ MinIO 초기화 시작..."

# MinIO 서버에 별칭(alias) 등록
# Docker 네트워크 내에서 'minio' 호스트명으로 접근
mc alias set myminio http://minio:9000 "${MINIO_ACCESS_KEY}" "${MINIO_SECRET_KEY}"

# community-media 버킷 생성 (이미 있으면 무시)
mc mb myminio/community-media --ignore-existing

# 버킷에 익명 다운로드 권한 설정
# 업로드된 이미지를 인증 없이 URL로 접근 가능하게 함
mc anonymous set download myminio/community-media

echo "✅ MinIO 초기화 완료! 버킷: community-media"
