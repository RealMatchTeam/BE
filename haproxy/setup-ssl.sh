#!/bin/bash

# Let's Encrypt SSL 인증서 발급 스크립트
# 사용법: ./setup-ssl.sh your-domain.com

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 도메인 확인
if [ -z "$1" ]; then
    echo -e "${RED}오류: 도메인을 입력해주세요${NC}"
    echo "사용법: $0 your-domain.com"
    exit 1
fi

DOMAIN=$1
CERT_DIR="/home/ubuntu/realmatch/haproxy/certs"

echo -e "${GREEN}=== Let's Encrypt SSL 인증서 발급 시작 ===${NC}"
echo -e "도메인: ${YELLOW}$DOMAIN${NC}"
echo ""

# Certbot 설치 확인
if ! command -v certbot &> /dev/null; then
    echo -e "${YELLOW}Certbot이 설치되어 있지 않습니다. 설치를 시작합니다...${NC}"
    sudo apt-get update
    sudo apt-get install -y certbot
fi

# HAProxy 임시 중지 (포트 80이 필요)
echo -e "${YELLOW}HAProxy를 임시로 중지합니다...${NC}"
cd /home/ubuntu/realmatch
docker-compose down haproxy 2>/dev/null || true

# Let's Encrypt 인증서 발급
echo -e "${GREEN}Let's Encrypt 인증서를 발급받습니다...${NC}"
echo -e "${YELLOW}이메일 주소와 약관 동의가 필요합니다.${NC}"
sudo certbot certonly --standalone \
    --preferred-challenges http \
    -d $DOMAIN \
    --non-interactive --agree-tos --register-unsafely-without-email || \
    sudo certbot certonly --standalone \
    --preferred-challenges http \
    -d $DOMAIN

# 인증서 경로
CERT_PATH="/etc/letsencrypt/live/$DOMAIN"

# 인증서 확인
if [ ! -f "$CERT_PATH/fullchain.pem" ] || [ ! -f "$CERT_PATH/privkey.pem" ]; then
    echo -e "${RED}인증서 발급에 실패했습니다.${NC}"
    echo -e "${YELLOW}다음을 확인해주세요:${NC}"
    echo "1. 도메인이 이 서버의 IP를 가리키고 있는지"
    echo "2. 포트 80이 열려있는지 (방화벽 확인)"
    exit 1
fi

# HAProxy용 PEM 파일 생성 (cert + key 결합)
echo -e "${GREEN}HAProxy용 인증서 파일을 생성합니다...${NC}"
sudo cat $CERT_PATH/fullchain.pem $CERT_PATH/privkey.pem | sudo tee $CERT_DIR/realmatch.pem > /dev/null
sudo chmod 644 $CERT_DIR/realmatch.pem

# 인증서 정보 출력
echo ""
echo -e "${GREEN}=== 인증서 발급 완료! ===${NC}"
sudo certbot certificates -d $DOMAIN

# HAProxy 설정 파일 업데이트 안내
echo ""
echo -e "${GREEN}=== 다음 단계 ===${NC}"
echo -e "1. HAProxy 설정에서 HTTPS를 활성화해야 합니다:"
echo -e "   ${YELLOW}haproxy/haproxy.cfg${NC} 파일에서 주석을 해제하세요"
echo ""
echo -e "2. Docker Compose를 재시작합니다:"
echo -e "   ${YELLOW}cd /home/ubuntu/realmatch && docker-compose up -d${NC}"
echo ""
echo -e "3. 인증서는 90일마다 갱신이 필요합니다. 자동 갱신 설정:"
echo -e "   ${YELLOW}sudo certbot renew --dry-run${NC} (테스트)"
echo ""

# 자동 갱신 스크립트 생성
RENEW_SCRIPT="$CERT_DIR/renew-ssl.sh"
cat > $RENEW_SCRIPT << 'EOF'
#!/bin/bash
# SSL 인증서 자동 갱신 스크립트

cd /home/ubuntu/realmatch
docker-compose down haproxy
sudo certbot renew
DOMAIN=$(ls /etc/letsencrypt/live/ | head -n 1)
sudo cat /etc/letsencrypt/live/$DOMAIN/fullchain.pem /etc/letsencrypt/live/$DOMAIN/privkey.pem | sudo tee /home/ubuntu/realmatch/haproxy/certs/realmatch.pem > /dev/null
docker-compose up -d haproxy
EOF

chmod +x $RENEW_SCRIPT

echo -e "${GREEN}자동 갱신 스크립트 생성 완료: $RENEW_SCRIPT${NC}"
echo -e "Cron에 추가하려면: ${YELLOW}sudo crontab -e${NC}"
echo -e "다음 라인 추가: ${YELLOW}0 0 1 * * $RENEW_SCRIPT${NC}"
echo ""
