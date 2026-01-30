#!/bin/bash
# RealMatch 더미 데이터 생성 환경 설정 스크립트

echo "[시작] RealMatch 더미 데이터 생성 환경 설정"
echo ""

# Python 버전 확인
if ! command -v python3 &> /dev/null; then
    echo "[오류] Python3가 설치되어 있지 않습니다."
    echo "       Python3를 먼저 설치해주세요."
    exit 1
fi

echo "[확인] Python 버전: $(python3 --version)"
echo ""

# 가상환경 생성
if [ ! -d "venv" ]; then
    echo "[진행] 가상환경 생성 중..."
    python3 -m venv venv
    echo "[완료] 가상환경 생성 완료"
else
    echo "[확인] 가상환경이 이미 존재합니다."
fi
echo ""

# 가상환경 활성화
echo "[진행] 가상환경 활성화 중..."
source venv/bin/activate
echo "[완료] 가상환경 활성화 완료"
echo ""

# 패키지 설치
echo "[진행] 필요한 패키지 설치 중..."
pip install --upgrade pip
pip install -r requirements.txt
echo "[완료] 패키지 설치 완료"
echo ""

echo "============================================================"
echo "[완료] 설정 완료!"
echo "============================================================"
echo ""
echo "더미 데이터를 생성하려면 다음 명령어를 실행하세요:"
echo ""
echo "  python generate_dummy_data.py"
echo ""
echo "또는 개수를 지정하여 실행:"
echo ""
echo "  python generate_dummy_data.py --users 100 --brands 30 --campaigns 50"
echo ""
echo "도움말:"
echo ""
echo "  python generate_dummy_data.py --help"
echo ""
