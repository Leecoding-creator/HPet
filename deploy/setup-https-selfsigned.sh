#!/usr/bin/env bash
# HPet - 가비아 클라우드 서버에 자체서명 HTTPS 적용 스크립트 (해커톤 데모용)
#
# 이 스크립트는 서버(1.201.117.185)에 root로 SSH 접속한 뒤 직접 실행하세요.
# 실행 전에 이 저장소가 /root/HPet 에 클론되어 있고, 백엔드가 8080 포트에서
# 이미 (또는 곧) 떠 있다는 것을 전제로 합니다.
#
# 실행: sudo bash deploy/setup-https-selfsigned.sh
set -euo pipefail

REPO_DIR="/root/HPet"
NGINX_CONF_SRC="$REPO_DIR/deploy/nginx-hpet.conf"
NGINX_CONF_DST="/etc/nginx/conf.d/hpet.conf"

echo "== 1. nginx 설치 =="
dnf install -y nginx

echo "== 2. 자체서명 SSL 인증서 생성 (유효기간 30일) =="
mkdir -p /etc/nginx/ssl
openssl req -x509 -nodes -days 30 -newkey rsa:2048 \
  -keyout /etc/nginx/ssl/selfsigned.key \
  -out /etc/nginx/ssl/selfsigned.crt \
  -subj "/CN=1.201.117.185"

echo "== 3. nginx 설정 적용 (저장소의 deploy/nginx-hpet.conf 를 심볼릭 링크로 연결) =="
ln -sf "$NGINX_CONF_SRC" "$NGINX_CONF_DST"

echo "== 4. 기존 80포트 python http.server 종료 =="
pkill -f "http.server" || true

echo "== 5. frontend/js/api.js 의 HPET_API_BASE 확인 =="
grep -n "HPET_API_BASE" "$REPO_DIR/frontend/js/api.js" || true
echo "  (feature/https-selfsigned 브랜치에서 이미 https://1.201.117.185 로 수정되어 있어야 합니다)"

echo "== 6. nginx 문법 검사 및 재시작 =="
nginx -t
systemctl enable nginx
systemctl restart nginx

echo "== 7. 검증 =="
curl -sk https://localhost
echo
curl -sk https://localhost/api/auth/login -X POST -H "Content-Type: application/json" -d '{}'
echo
echo "위 두 응답이 각각 index.html 내용, 401/400 계열의 API 에러 JSON 이면 라우팅 성공입니다."
