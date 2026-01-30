#!/usr/bin/env python3

import argparse
import sys
from config import DatabaseConfig
from generators import (
    UserGenerator,
    BrandGenerator,
    CampaignGenerator,
    TagGenerator,
    BusinessGenerator,
    ChatGenerator,
    MatchGenerator
)


class DummyDataOrchestrator:
    def __init__(self):
        self.db_config = DatabaseConfig()
        self.connection = self.db_config.get_connection()

    def __del__(self):
        if hasattr(self, 'connection') and self.connection:
            self.connection.close()
            print("\n[성공] 데이터베이스 연결 종료")

    def generate_all(self, user_count=50, brand_count=20, campaign_count=30,
                     room_count=20, messages_per_room=10, applies_per_campaign=3):
        print("[시작] 더미 데이터 생성 시작...\n")
        print(f"생성할 데이터:")
        print(f"  - 사용자: {user_count}명")
        print(f"  - 브랜드: {brand_count}개")
        print(f"  - 캠페인: {campaign_count}개")
        print(f"  - 채팅방: {room_count}개")

        try:
            user_gen = UserGenerator(self.connection)
            user_gen.generate_all(user_count)

            brand_gen = BrandGenerator(self.connection)
            brand_gen.generate_all(brand_count)

            campaign_gen = CampaignGenerator(self.connection)
            campaign_gen.generate_all(campaign_count)

            tag_gen = TagGenerator(self.connection)
            tag_gen.generate_all()

            business_gen = BusinessGenerator(self.connection)
            business_gen.generate_all(applies_per_campaign)

            chat_gen = ChatGenerator(self.connection)
            chat_gen.generate_all(room_count, messages_per_room)

            match_gen = MatchGenerator(self.connection)
            match_gen.generate_all()

            print("\n" + "="*60)
            print("[완료] 모든 더미 데이터 생성 완료!")
            print("="*60)
        except Exception as e:
            print(f"\n[오류] 오류 발생: {e}")
            self.connection.rollback()
            raise


def main():
    parser = argparse.ArgumentParser(
        description='RealMatch 더미 데이터 생성 스크립트',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
예시:
  python main.py --users 50 --brands 20 --campaigns 30
  python main.py --all 100
        """
    )

    parser.add_argument('--users', type=int, default=50,
                        help='생성할 사용자 수 (기본값: 50)')
    parser.add_argument('--brands', type=int, default=20,
                        help='생성할 브랜드 수 (기본값: 20)')
    parser.add_argument('--campaigns', type=int, default=30,
                        help='생성할 캠페인 수 (기본값: 30)')
    parser.add_argument('--rooms', type=int, default=20,
                        help='생성할 채팅방 수 (기본값: 20)')
    parser.add_argument('--messages', type=int, default=10,
                        help='채팅방당 메시지 수 (기본값: 10)')
    parser.add_argument('--applies', type=int, default=3,
                        help='캠페인당 지원 수 (기본값: 3)')
    parser.add_argument('--all', type=int,
                        help='모든 항목에 동일한 개수 적용')

    args = parser.parse_args()

    if args.all:
        args.users = args.all
        args.brands = args.all // 2
        args.campaigns = args.all // 2
        args.rooms = args.all // 2

    orchestrator = DummyDataOrchestrator()
    orchestrator.generate_all(
        user_count=args.users,
        brand_count=args.brands,
        campaign_count=args.campaigns,
        room_count=args.rooms,
        messages_per_room=args.messages,
        applies_per_campaign=args.applies
    )


if __name__ == '__main__':
    main()
