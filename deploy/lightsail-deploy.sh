#!/usr/bin/env bash
# HPet - AWS Lightsail(Linux) 원클릭 배포 스크립트 (MVP 데모용)
#
# 전제: Java 17+ 설치됨, 이 저장소가 서버에 클론되어 있음, backend/.env 작성됨
#   backend/.env 예시:
#     GEMINI_API_KEY=발급받은_키
#     JWT_SECRET=32자_이상_아무_문자열     (선택, 없으면 데모용 기본값)
#
# 실행 (저장소 루트에서):  sudo bash deploy/lightsail-deploy.sh [도메인 또는 공인IP]
#   예) sudo bash deploy/lightsail-deploy.sh hpet.duckdns.org
#   - 도메인을 주면 Let's Encrypt 정식 인증서를 발급합니다 (브라우저 경고 없음, 자동 갱신).
#     DNS가 이 서버 IP를 가리키고 80 포트가 열려 있어야 합니다.
#   - 인자를 생략하면 서버 공인 IP + 자체서명 인증서로 배포합니다.
#   - 다시 실행하면 최신 코드로 재빌드·재배포합니다. (H2 메모리 DB라 데이터는 초기화됨)
#
# Lightsail 콘솔에서 할 일: Static IP 연결, 네트워킹 탭에서 80·443 포트 열기
set -euo pipefail

REPO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$REPO_DIR/backend"
WEB_ROOT="/var/www/hpet"
RUN_USER="${SUDO_USER:-$(whoami)}"

if [[ $EUID -ne 0 ]]; then echo "sudo 로 실행하세요: sudo bash deploy/lightsail-deploy.sh"; exit 1; fi

SERVER_NAME="${1:-}"
if [[ -z "$SERVER_NAME" ]]; then
  TOKEN=$(curl -s -m 3 -X PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 60" || true)
  SERVER_NAME=$(curl -s -m 3 -H "X-aws-ec2-metadata-token: $TOKEN" http://169.254.169.254/latest/meta-data/public-ipv4 || true)
  [[ -z "$SERVER_NAME" ]] && SERVER_NAME=$(curl -s -m 5 https://checkip.amazonaws.com | tr -d '[:space:]')
fi
[[ -z "$SERVER_NAME" ]] && { echo "공인 IP를 찾지 못했습니다. 인자로 넘겨주세요."; exit 1; }
echo "== 대상 주소: $SERVER_NAME (저장소: $REPO_DIR) =="

echo "== 1. 사전 점검 =="
java -version 2>&1 | head -1 || { echo "Java가 없습니다. Java 17 이상을 설치하세요."; exit 1; }
if [[ ! -f "$BACKEND_DIR/.env" ]]; then
  echo "backend/.env 가 없습니다. 아래처럼 만든 뒤 다시 실행하세요:"
  echo "  echo 'GEMINI_API_KEY=발급받은_키' > $BACKEND_DIR/.env"
  exit 1
fi
grep -q '^GEMINI_API_KEY=.\+' "$BACKEND_DIR/.env" || echo "  [경고] backend/.env 에 GEMINI_API_KEY 값이 없습니다. 사진 인증이 실패합니다."
chmod 600 "$BACKEND_DIR/.env"; chown "$RUN_USER" "$BACKEND_DIR/.env"

echo "== 2. nginx 설치 =="
if command -v apt-get >/dev/null; then
  PKG="apt"; WEB_USER="www-data"
  apt-get update -y -q && apt-get install -y -q nginx openssl
  rm -f /etc/nginx/sites-enabled/default
else
  PKG="dnf"; WEB_USER="nginx"
  (command -v dnf >/dev/null && dnf install -y nginx openssl) || yum install -y nginx openssl
fi

echo "== 3. 백엔드 빌드 =="
cd "$BACKEND_DIR"
chmod +x gradlew
sudo -u "$RUN_USER" ./gradlew bootJar -x test --no-daemon -q
JAR=$(ls -t build/libs/*.jar | grep -v plain | head -1)
echo "  빌드 결과: $JAR"

echo "== 4. systemd 서비스 등록 (hpet) =="
cat > /etc/systemd/system/hpet.service <<UNIT
[Unit]
Description=HPet Spring Boot backend
After=network.target

[Service]
User=$RUN_USER
WorkingDirectory=$BACKEND_DIR
EnvironmentFile=$BACKEND_DIR/.env
ExecStart=$(command -v java) -Xms256m -Xmx768m -jar $BACKEND_DIR/$JAR
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
UNIT
systemctl daemon-reload
systemctl enable hpet >/dev/null
systemctl restart hpet

echo "== 5. 프론트 정적 파일 배포 =="
mkdir -p "$WEB_ROOT"
rm -rf "${WEB_ROOT:?}"/*
cp -r "$REPO_DIR/frontend/." "$WEB_ROOT/"
chown -R "$WEB_USER":"$WEB_USER" "$WEB_ROOT"
echo "  정적 파일 수: 원본 $(find "$REPO_DIR/frontend" -type f | wc -l) / 배포 $(find "$WEB_ROOT" -type f | wc -l)"

echo "== 6. HTTPS 인증서 =="
SSL_CERT=/etc/nginx/ssl/selfsigned.crt
SSL_KEY=/etc/nginx/ssl/selfsigned.key
mkdir -p /etc/nginx/ssl /var/www/certbot
# nginx가 먼저 뜰 수 있도록 자체서명 인증서는 항상 준비해 둔다
if [[ ! -f $SSL_CERT ]] || ! openssl x509 -in $SSL_CERT -noout -subject | grep -q "$SERVER_NAME"; then
  openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout $SSL_KEY -out $SSL_CERT -subj "/CN=$SERVER_NAME" 2>/dev/null
fi

apply_nginx() {
  sed -e "s#__SERVER_NAME__#$SERVER_NAME#g" -e "s#__SSL_CERT__#$1#g" -e "s#__SSL_KEY__#$2#g" \
    "$REPO_DIR/deploy/nginx-hpet.conf" > /etc/nginx/conf.d/hpet.conf
  nginx -t
  systemctl enable nginx >/dev/null
  systemctl restart nginx
}

echo "== 7. nginx 설정 적용 =="
apply_nginx "$SSL_CERT" "$SSL_KEY"

# 도메인이면(IP가 아니면) Let's Encrypt 발급 시도, 실패하면 자체서명 유지
if [[ ! "$SERVER_NAME" =~ ^[0-9.]+$ ]]; then
  echo "== 7-1. Let's Encrypt 인증서 발급 ($SERVER_NAME) =="
  if [[ $PKG == apt ]]; then apt-get install -y -q certbot; else dnf install -y certbot || true; fi
  LE_DIR="/etc/letsencrypt/live/$SERVER_NAME"
  if command -v certbot >/dev/null && certbot certonly --webroot -w /var/www/certbot -d "$SERVER_NAME" \
       --non-interactive --agree-tos --register-unsafely-without-email --keep-until-expiring \
       --deploy-hook "systemctl reload nginx"; then
    apply_nginx "$LE_DIR/fullchain.pem" "$LE_DIR/privkey.pem"
    echo "  정식 인증서 적용 완료 (자동 갱신: certbot 타이머)"
  else
    echo "  [경고] 발급 실패 → 자체서명 인증서로 계속합니다. DNS·80 포트를 확인하세요."
  fi
fi

echo "== 8. 검증 (백엔드 기동 대기 최대 90초) =="
for i in $(seq 1 30); do
  curl -s -o /dev/null http://localhost:8080/actuator/health && break
  sleep 3
done
echo "  백엔드 health: $(curl -s http://localhost:8080/actuator/health || echo '응답 없음 → journalctl -u hpet -n 50')"
echo "  프론트 index : HTTP $(curl -sk -o /dev/null -w '%{http_code}' https://localhost/ -H "Host: $SERVER_NAME")"
echo "  로그인 API   : HTTP $(curl -sk -o /dev/null -w '%{http_code}' -X POST https://localhost/api/auth/login -H "Host: $SERVER_NAME" -H 'Content-Type: application/json' -d '{}') (400/401이면 정상)"
echo
echo "완료: https://$SERVER_NAME"
echo "  (IP로 배포했거나 정식 인증서 발급에 실패했다면 첫 접속 시 '고급 → 계속' 필요)"
echo "로그: sudo journalctl -u hpet -f"
