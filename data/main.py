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
    MatchGenerator,
    RedisDataGenerator,
    SeedGenerator
)


class DummyDataOrchestrator:
    def __init__(self):
        self.db_config = DatabaseConfig()
        self.connection = self.db_config.get_connection()

    def __del__(self):
        if hasattr(self, 'connection') and self.connection:
            self.connection.close()
            print("\n[성공] 데이터베이스 연결 종료")

    def clear_existing_data(self):
        """기존 더미 데이터 삭제"""
        print("\n[정리] 기존 데이터 삭제 중...")

        tables = [
            'chat_message',
            'chat_room_member',
            'chat_room',
            'user_matching_detail',
            'match_brand_history',
            'match_campaign_history',
            'campaign_proposal_content_tag',
            'campaign_proposal',
            'campaign_like_read',
            'campaign_fashion_tag',
            'campaign_content_tag',
            'campaign_apply',
            'campaign_like',
            'brand_like_read',
            'brand_category_view',
            'brand_sponsor_image',
            'brand_available_sponsor',
            'brand_category',
            'brand_like',
            'user_tag',
            'brand_tag',
            'tag',
            'campaign',
            'brand_image',
            'brand',
            'users',
            'tag_content',
            'term',
            'signup_purposes',
            'content_categories',
        ]

        with self.connection.cursor() as cursor:
            cursor.execute("SET FOREIGN_KEY_CHECKS = 0")
            for table in tables:
                try:
                    cursor.execute(f"TRUNCATE TABLE {table}")
                    print(f"  - {table} 테이블 초기화 완료")
                except Exception as e:
                    print(f"  - {table} 테이블 초기화 실패: {e}")
            try:
                cursor.execute("TRUNCATE TABLE authentication_methods")
                print("  - authentication_methods 테이블 초기화 완료")
            except Exception as e:
                print(f"  - authentication_methods 테이블 초기화 실패: {e}")
            cursor.execute("SET FOREIGN_KEY_CHECKS = 1")
        self.connection.commit()
        print("[완료] 기존 데이터 삭제 완료\n")

    def create_master_account(self):
        print("\n[마스터] 마스터 계정 생성 중...")

        with self.connection.cursor() as cursor:
            cursor.execute("""
                INSERT INTO users (name, nickname, email, gender, role, created_at, updated_at, is_deleted)
                VALUES ('홍길동', 'gildong', 'gildong@test.com', 'MALE', 'ADMIN', NOW(), NOW(), false)
            """)

            cursor.execute("SELECT LAST_INSERT_ID() as id")
            user_id = cursor.fetchone()['id']

            cursor.execute("SET FOREIGN_KEY_CHECKS = 0")
            cursor.execute(f"UPDATE users SET id = 0 WHERE id = {user_id}")
            cursor.execute("SET FOREIGN_KEY_CHECKS = 1")

            cursor.execute("""
                INSERT INTO authentication_methods (id, user_id, email, provider, provider_id, is_deleted, created_at, updated_at, deleted_at)
                VALUES (0, 0, 'gildong@test.com', 'KAKAO', 'MASTER', false, NOW(), NOW(), NULL)
            """)

        self.connection.commit()
        print("  - 마스터 계정 생성 완료 (ID: 0, email: gildong@test.com)")
        print("[완료] 마스터 계정 생성 완료\n")

    def generate_all(self, user_count=50, brand_count=20, campaign_count=30,
                     room_count=20, messages_per_room=10, applies_per_campaign=3,
                     reset=True):
        if reset:
            self.clear_existing_data()
            self.create_master_account()

        print("[시작] 더미 데이터 생성 시작...\n")
        print(f"생성할 데이터:")
        print(f"  - 사용자: {user_count}명")
        print(f"  - 브랜드: {brand_count}개")
        print(f"  - 캠페인: {campaign_count}개")
        print(f"  - 채팅방: {room_count}개")

        try:
            # 시드 데이터 먼저 생성 (태그, 약관 등)
            seed_gen = SeedGenerator(self.connection)
            seed_gen.generate_all()

            user_gen = UserGenerator(self.connection)
            user_gen.generate_all(user_count)

            brand_gen = BrandGenerator(self.connection)
            brand_gen.generate_all(brand_count)

            campaign_gen = CampaignGenerator(self.connection)
            campaign_gen.generate_all(campaign_count)

            # 캠페인 생성 후 협찬 데이터 생성
            brand_gen.generate_sponsors()

            tag_gen = TagGenerator(self.connection)
            tag_gen.generate_all()

            business_gen = BusinessGenerator(self.connection)
            business_gen.generate_all(applies_per_campaign)

            chat_gen = ChatGenerator(self.connection)
            chat_gen.generate_all(room_count, messages_per_room)

            match_gen = MatchGenerator(self.connection)
            match_gen.generate_all()

            redis_gen = RedisDataGenerator()
            redis_gen.generate_all(clear_existing=True)

            print("\n" + "="*60)
            print("[완료] 모든 더미 데이터 생성 완료!")
            print("  - MySQL: 사용자, 브랜드, 캠페인, 태그, 채팅 등")
            print("  - Redis: 브랜드/캠페인/사용자 태그 문서 (매칭용)")
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
  python main.py --users 50 --brands 20 --campaigns 30 --rooms 20 --messages 10 --applies 10
  python main.py --all 100
        """
    )

    parser.add_argument('--users', type=int, default=500,
                        help='생성할 사용자 수 (기본값: 50)')
    parser.add_argument('--brands', type=int, default=20000,
                        help='생성할 브랜드 수 (기본값: 20)')
    parser.add_argument('--campaigns', type=int, default=30000,
                        help='생성할 캠페인 수 (기본값: 30)')
    parser.add_argument('--rooms', type=int, default=1,
                        help='생성할 채팅방 수 (기본값: 20)')
    parser.add_argument('--messages', type=int, default=1,
                        help='채팅방당 메시지 수 (기본값: 10)')
    parser.add_argument('--applies', type=int, default=1,
                        help='캠페인당 지원 수 (기본값: 3)')
    parser.add_argument('--all', type=int,
                        help='모든 항목에 동일한 개수 적용')
    parser.add_argument('--no-reset', action='store_true',
                        help='기존 데이터를 삭제하지 않고 추가')

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
        applies_per_campaign=args.applies,
        reset=not args.no_reset
    )


if __name__ == '__main__':
    main()
