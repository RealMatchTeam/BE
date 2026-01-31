#!/usr/bin/env python3
"""
Redis Tag Document Generator for RealMatch

MySQL의 데이터를 기반으로 Redis에 매칭용 태그 문서를 생성합니다.
- BrandTagDocument
- CampaignTagDocument
- UserTagDocument
"""

import argparse
import json
import os
import random
import sys
from datetime import datetime, timedelta

import pymysql
import redis
from dotenv import load_dotenv

load_dotenv()


# 태그 데이터 정의
TAG_DATA = {
    'fashion_styles': ['미니멀', '페미닌', '러블리', '비지니스 캐주얼', '캐주얼', '스트리트', '스포티'],
    'fashion_items': ['의류', '가방', '신발', '주얼리', '패션 소품', '원피스', '블라우스', '후드티', '조거팬츠', '스니커즈'],
    'beauty_interests': ['스킨케어', '메이크업', '향수', '바디', '헤어'],
    'beauty_functions': ['트러블', '수분/보습', '진정', '미백', '안티에이징', '각질/모공'],
    'skin_types': ['건성', '지성', '복합성', '민감성'],
    'content_formats': ['인스타 스토리', '인스타 포스터', '인스타 릴스', '유튜브', '틱톡', '블로그'],
    'content_types': ['브이로그', '리뷰', '겟레디윗미', '비포&에프터', '스토리/썰', '챌린지', '룩북'],
    'content_tones': ['전문적인', '감성적인', '유쾌/재밌는', '트렌디한', '일상적인'],
    'body_types': ['마른 체형', '보통 체형', '통통한 체형', '근육질 체형'],
    'audience_genders': ['여성', '남성'],
    'audience_ages': ['10~20대', '20~30대', '30~40대', '40~50대'],
    'video_lengths': ['~15초', '15~30초', '30~45초', '45~60초', '1분~3분', '3분~10분'],
}


class RedisDataGenerator:

    def __init__(self):
        # MySQL 연결
        try:
            self.mysql_conn = pymysql.connect(
                host=os.getenv('MYSQL_HOST', 'localhost'),
                port=int(os.getenv('MYSQL_PORT', 3306)),
                user=os.getenv('MYSQL_USER'),
                password=os.getenv('MYSQL_PASSWORD'),
                database=os.getenv('MYSQL_DATABASE'),
                charset='utf8mb4',
                cursorclass=pymysql.cursors.DictCursor
            )
            print(f"[성공] MySQL 연결 성공: {os.getenv('MYSQL_DATABASE')}")
        except Exception as e:
            print(f"[오류] MySQL 연결 실패: {e}")
            sys.exit(1)

        # Redis 연결
        try:
            self.redis_client = redis.Redis(
                host=os.getenv('REDIS_HOST', 'localhost'),
                port=int(os.getenv('REDIS_PORT', 6379)),
                password=os.getenv('REDIS_PASSWORD', None),
                decode_responses=True
            )
            self.redis_client.ping()
            print(f"[성공] Redis 연결 성공: {os.getenv('REDIS_HOST', 'localhost')}:{os.getenv('REDIS_PORT', 6379)}")
        except Exception as e:
            print(f"[오류] Redis 연결 실패: {e}")
            sys.exit(1)

    def __del__(self):
        if hasattr(self, 'mysql_conn') and self.mysql_conn:
            self.mysql_conn.close()
            print("[성공] MySQL 연결 종료")

    def _random_subset(self, items, min_count=1, max_count=3):
        count = random.randint(min_count, min(max_count, len(items)))
        return random.sample(items, count)

    def generate_brand_documents(self):
        print("\n[브랜드] Redis 브랜드 태그 문서 생성 중...")

        with self.mysql_conn.cursor() as cursor:
            cursor.execute("SELECT id, brand_name, industry_type FROM brand WHERE is_deleted = FALSE")
            brands = cursor.fetchall()

        if not brands:
            print("[경고] 브랜드 데이터가 없습니다.")
            return 0

        count = 0
        for brand in brands:
            brand_id = brand['id']
            industry = brand['industry_type']

            # 카테고리 설정
            if industry == 'FASHION':
                categories = ['FASHION']
            elif industry == 'BEAUTY':
                categories = ['BEAUTY']
            else:
                categories = random.choice([['FASHION'], ['BEAUTY'], ['FASHION', 'BEAUTY']])

            # 태그 생성
            doc = {
                'brandId': brand_id,
                'brandName': brand['brand_name'],
                'categories': categories,
                'preferredFashionTags': self._random_subset(TAG_DATA['fashion_styles'] + TAG_DATA['fashion_items'], 2, 5),
                'preferredBeautyTags': self._random_subset(TAG_DATA['beauty_interests'] + TAG_DATA['beauty_functions'], 2, 5),
                'preferredContentTags': self._random_subset(TAG_DATA['content_formats'] + TAG_DATA['content_types'], 2, 4),
                'minCreatorHeight': random.choice([None, 150, 155, 160]),
                'maxCreatorHeight': random.choice([None, 175, 180, 185]),
                'preferredBodyTypes': self._random_subset(TAG_DATA['body_types'], 1, 2),
                'preferredTopSizes': [str(s) for s in random.sample(range(44, 110, 2), 3)],
                'preferredBottomSizes': [str(s) for s in random.sample(range(24, 36), 3)],
                'minContentsAverageViews': random.choice([None, 10000, 50000, 100000]),
                'maxContentsAverageViews': random.choice([None, 500000, 1000000]),
                'preferredContentsAges': self._random_subset(TAG_DATA['audience_ages'], 1, 3),
                'preferredContentsGenders': self._random_subset(TAG_DATA['audience_genders'], 1, 2),
                'preferredContentsLengths': self._random_subset(TAG_DATA['video_lengths'], 1, 3),
            }

            # Redis에 저장 (Redis OM Spring 형식)
            key = f"com.example.RealMatch.match.infrastructure.redis.document.BrandTagDocument:brand:{brand_id}"
            self.redis_client.json().set(key, '$', doc)
            count += 1

        print(f"[완료] {count}개의 브랜드 태그 문서 생성 완료")
        return count

    def generate_campaign_documents(self):
        print("\n[캠페인] Redis 캠페인 태그 문서 생성 중...")

        with self.mysql_conn.cursor() as cursor:
            cursor.execute("""
                SELECT id, title, start_date, end_date, quota
                FROM campaign
                WHERE is_deleted = FALSE
            """)
            campaigns = cursor.fetchall()

        if not campaigns:
            print("[경고] 캠페인 데이터가 없습니다.")
            return 0

        count = 0
        for campaign in campaigns:
            campaign_id = campaign['id']

            # 카테고리 랜덤 설정
            categories = random.choice([['FASHION'], ['BEAUTY'], ['FASHION', 'BEAUTY']])

            doc = {
                'campaignId': campaign_id,
                'categories': categories,
                'preferredFashionTags': self._random_subset(TAG_DATA['fashion_styles'] + TAG_DATA['fashion_items'], 2, 5),
                'preferredBeautyTags': self._random_subset(TAG_DATA['beauty_interests'] + TAG_DATA['beauty_functions'], 2, 5),
                'preferredContentTags': self._random_subset(TAG_DATA['content_formats'] + TAG_DATA['content_types'], 2, 4),
                'minCreatorHeight': random.choice([None, 150, 155, 160]),
                'maxCreatorHeight': random.choice([None, 175, 180, 185]),
                'preferredBodyTypes': self._random_subset(TAG_DATA['body_types'], 1, 2),
                'minCreatorTopSizes': random.choice([None, 44, 50, 55]),
                'maxCreatorTopSizes': random.choice([None, 100, 105, 110]),
                'minCreatorBottomSizes': random.choice([None, 24, 26, 28]),
                'maxCreatorBottomSizes': random.choice([None, 32, 34, 36]),
                'minContentsAverageViews': random.choice([None, 10000, 50000, 100000]),
                'maxContentsAverageViews': random.choice([None, 500000, 1000000]),
                'preferredContentsAges': self._random_subset(TAG_DATA['audience_ages'], 1, 3),
                'preferredContentsGenders': self._random_subset(TAG_DATA['audience_genders'], 1, 2),
                'preferredContentsLengths': self._random_subset(TAG_DATA['video_lengths'], 1, 3),
                'startDate': campaign['start_date'].isoformat() if campaign['start_date'] else None,
                'endDate': campaign['end_date'].isoformat() if campaign['end_date'] else None,
                'quota': campaign['quota'],
            }

            key = f"com.example.RealMatch.match.infrastructure.redis.document.CampaignTagDocument:campaign:{campaign_id}"
            self.redis_client.json().set(key, '$', doc)
            count += 1

        print(f"[완료] {count}개의 캠페인 태그 문서 생성 완료")
        return count

    def generate_user_documents(self):
        print("\n[사용자] Redis 사용자 태그 문서 생성 중...")

        with self.mysql_conn.cursor() as cursor:
            cursor.execute("SELECT id, gender FROM users WHERE role = 'CREATOR' AND is_deleted = FALSE")
            users = cursor.fetchall()

        if not users:
            print("[경고] CREATOR 역할의 사용자가 없습니다.")
            return 0

        count = 0
        for user in users:
            user_id = user['id']
            gender = user['gender']

            # 성별에 따른 키 범위 조정
            if gender == 'MALE':
                height = random.randint(165, 185)
                top_size = str(random.randint(95, 110))
                bottom_size = str(random.randint(30, 36))
            elif gender == 'FEMALE':
                height = random.randint(155, 175)
                top_size = str(random.randint(44, 66))
                bottom_size = str(random.randint(24, 30))
            else:
                height = random.randint(160, 180)
                top_size = str(random.randint(50, 100))
                bottom_size = str(random.randint(26, 34))

            doc = {
                'userId': user_id,
                'fashionTags': self._random_subset(TAG_DATA['fashion_styles'] + TAG_DATA['fashion_items'], 2, 5),
                'beautyTags': self._random_subset(TAG_DATA['beauty_interests'] + TAG_DATA['beauty_functions'], 2, 5),
                'contentTags': self._random_subset(TAG_DATA['content_formats'] + TAG_DATA['content_types'], 2, 4),
                'height': height,
                'bodyType': random.choice(TAG_DATA['body_types']),
                'topSize': top_size,
                'bottomSize': bottom_size,
                'averageContentsViews': random.choice([10000, 50000, 100000, 300000, 500000, 1000000]),
                'contentsAge': self._random_subset(TAG_DATA['audience_ages'], 1, 2),
                'contentsGender': self._random_subset(TAG_DATA['audience_genders'], 1, 2),
                'contentsLength': random.choice(TAG_DATA['video_lengths']),
            }

            key = f"com.example.RealMatch.match.infrastructure.redis.document.UserTagDocument:user:{user_id}"
            self.redis_client.json().set(key, '$', doc)
            count += 1

        print(f"[완료] {count}개의 사용자 태그 문서 생성 완료")
        return count

    def clear_redis_documents(self):
        print("\n[정리] 기존 Redis 태그 문서 삭제 중...")

        patterns = [
            "com.example.RealMatch.match.infrastructure.redis.document.BrandTagDocument:*",
            "com.example.RealMatch.match.infrastructure.redis.document.CampaignTagDocument:*",
            "com.example.RealMatch.match.infrastructure.redis.document.UserTagDocument:*",
        ]

        total_deleted = 0
        for pattern in patterns:
            keys = self.redis_client.keys(pattern)
            if keys:
                self.redis_client.delete(*keys)
                total_deleted += len(keys)

        print(f"[완료] {total_deleted}개의 기존 문서 삭제 완료")
        return total_deleted

    def create_search_indexes(self):
        """RediSearch 인덱스 생성 (Redis OM Spring 호환)"""
        print("\n[인덱스] RediSearch 인덱스 생성 중...")

        from redis.commands.search.field import NumericField, TagField
        from redis.commands.search.index_definition import IndexDefinition, IndexType

        indexes = [
            {
                'name': 'com.example.RealMatch.match.infrastructure.redis.document.BrandTagDocumentIdx',
                'prefix': 'com.example.RealMatch.match.infrastructure.redis.document.BrandTagDocument:',
                'schema': (
                    NumericField('$.brandId', as_name='brandId'),
                    TagField('$.brandName', as_name='brandName'),
                    TagField('$.categories[*]', as_name='categories'),
                    TagField('$.preferredFashionTags[*]', as_name='preferredFashionTags'),
                    TagField('$.preferredBeautyTags[*]', as_name='preferredBeautyTags'),
                    TagField('$.preferredContentTags[*]', as_name='preferredContentTags'),
                )
            },
            {
                'name': 'com.example.RealMatch.match.infrastructure.redis.document.CampaignTagDocumentIdx',
                'prefix': 'com.example.RealMatch.match.infrastructure.redis.document.CampaignTagDocument:',
                'schema': (
                    NumericField('$.campaignId', as_name='campaignId'),
                    TagField('$.categories[*]', as_name='categories'),
                    TagField('$.preferredFashionTags[*]', as_name='preferredFashionTags'),
                    TagField('$.preferredBeautyTags[*]', as_name='preferredBeautyTags'),
                    TagField('$.preferredContentTags[*]', as_name='preferredContentTags'),
                )
            },
            {
                'name': 'com.example.RealMatch.match.infrastructure.redis.document.UserTagDocumentIdx',
                'prefix': 'com.example.RealMatch.match.infrastructure.redis.document.UserTagDocument:',
                'schema': (
                    NumericField('$.userId', as_name='userId'),
                    TagField('$.fashionTags[*]', as_name='fashionTags'),
                    TagField('$.beautyTags[*]', as_name='beautyTags'),
                    TagField('$.contentTags[*]', as_name='contentTags'),
                    NumericField('$.height', as_name='height'),
                )
            },
        ]

        for idx in indexes:
            try:
                # 기존 인덱스 삭제
                try:
                    self.redis_client.ft(idx['name']).dropindex(delete_documents=False)
                except Exception:
                    pass

                # 새 인덱스 생성
                definition = IndexDefinition(
                    prefix=[idx['prefix']],
                    index_type=IndexType.JSON
                )
                self.redis_client.ft(idx['name']).create_index(
                    idx['schema'],
                    definition=definition
                )
                print(f"  - {idx['name'].split('.')[-1]} 인덱스 생성 완료")
            except Exception as e:
                print(f"  - {idx['name'].split('.')[-1]} 인덱스 생성 실패: {e}")

        print("[완료] RediSearch 인덱스 생성 완료")

    def generate_all(self, clear_existing=True):
        print("\n" + "=" * 60)
        print("[시작] Redis 태그 문서 생성 시작...")
        print("=" * 60)

        if clear_existing:
            self.clear_redis_documents()

        try:
            # 인덱스 먼저 생성
            self.create_search_indexes()

            brand_count = self.generate_brand_documents()
            campaign_count = self.generate_campaign_documents()
            user_count = self.generate_user_documents()

            print("\n" + "=" * 60)
            print("[완료] Redis 태그 문서 생성 완료!")
            print(f"  - 브랜드 문서: {brand_count}개")
            print(f"  - 캠페인 문서: {campaign_count}개")
            print(f"  - 사용자 문서: {user_count}개")
            print("=" * 60)

        except Exception as e:
            print(f"\n[오류] 오류 발생: {e}")
            raise


def main():
    parser = argparse.ArgumentParser(
        description='RealMatch Redis 태그 문서 생성 스크립트',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
예시:
  python generate_redis_data.py              # 모든 문서 + 인덱스 생성
  python generate_redis_data.py --brands     # 브랜드 문서만 생성
  python generate_redis_data.py --campaigns  # 캠페인 문서만 생성
  python generate_redis_data.py --users      # 사용자 문서만 생성
  python generate_redis_data.py --index-only # 인덱스만 생성
  python generate_redis_data.py --no-clear   # 기존 데이터 유지하고 추가
        """
    )

    parser.add_argument('--brands', action='store_true', help='브랜드 문서만 생성')
    parser.add_argument('--campaigns', action='store_true', help='캠페인 문서만 생성')
    parser.add_argument('--users', action='store_true', help='사용자 문서만 생성')
    parser.add_argument('--index-only', action='store_true', help='인덱스만 생성')
    parser.add_argument('--no-clear', action='store_true', help='기존 데이터 유지')

    args = parser.parse_args()

    generator = RedisDataGenerator()

    # 인덱스만 생성
    if args.index_only:
        generator.create_search_indexes()
        return

    # 특정 타입만 생성할 경우
    if args.brands or args.campaigns or args.users:
        if not args.no_clear:
            generator.clear_redis_documents()

        generator.create_search_indexes()

        if args.brands:
            generator.generate_brand_documents()
        if args.campaigns:
            generator.generate_campaign_documents()
        if args.users:
            generator.generate_user_documents()
    else:
        # 전체 생성
        generator.generate_all(clear_existing=not args.no_clear)


if __name__ == '__main__':
    main()
