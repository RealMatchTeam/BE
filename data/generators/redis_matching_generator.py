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
            redis_host = os.getenv('REDIS_HOST', 'localhost')

            self.redis_client = redis.Redis(
                host=redis_host,
                port=int(os.getenv('REDIS_PORT', 6379)),
                password=os.getenv('REDIS_PASSWORD', None),
                decode_responses=True
            )
            self.redis_client.ping()
            self.redis_connected = True
            print(f"[성공] Redis 연결 성공: {redis_host}:{os.getenv('REDIS_PORT', 6379)}")
        except Exception as e:
            print(f"[경고] Redis 연결 실패: {e}")
            print("  - Redis 데이터 생성을 스킵합니다.")
            self.redis_connected = False

        self.tags_by_category = {}
        self.tags_by_type = {}
        self._load_tags_from_db()

    def __del__(self):
        if hasattr(self, 'mysql_conn') and self.mysql_conn:
            self.mysql_conn.close()
            print("[성공] MySQL 연결 종료")

    def _load_tags_from_db(self):
        print("\n[태그] MySQL에서 태그 정보 로드 중...")

        with self.mysql_conn.cursor() as cursor:
            cursor.execute("SELECT id, tag_type, tag_name, tag_category FROM tag")
            tags = cursor.fetchall()

        for tag in tags:
            tag_id = tag['id']
            tag_type = tag['tag_type']
            tag_category = tag['tag_category']

            if tag_category not in self.tags_by_category:
                self.tags_by_category[tag_category] = []
            self.tags_by_category[tag_category].append(tag_id)

            if tag_type not in self.tags_by_type:
                self.tags_by_type[tag_type] = []
            self.tags_by_type[tag_type].append(tag_id)

        print(f"  - 총 {len(tags)}개의 태그 로드 완료")
        print(f"  - 태그 타입: {list(self.tags_by_type.keys())}")
        print(f"  - 태그 카테고리: {list(self.tags_by_category.keys())}")

    def _get_random_tag_ids(self, categories, min_count=1, max_count=3):

        available_ids = []
        for cat in categories:
            if cat in self.tags_by_category:
                available_ids.extend(self.tags_by_category[cat])

        if not available_ids:
            return []

        count = random.randint(min_count, min(max_count, len(available_ids)))
        return random.sample(available_ids, count)

    def _get_random_tag_ids_by_type(self, tag_type, min_count=1, max_count=3):

        if tag_type not in self.tags_by_type:
            return []

        available_ids = self.tags_by_type[tag_type]
        count = random.randint(min_count, min(max_count, len(available_ids)))
        return random.sample(available_ids, count)

    def generate_brand_documents(self):
        print("\n[브랜드] Redis 브랜드 태그 문서 생성 중...")

        with self.mysql_conn.cursor() as cursor:
            # 브랜드 정보 조회
            cursor.execute("SELECT id, brand_name, industry_type FROM brand WHERE is_deleted = FALSE")
            brands = cursor.fetchall()

            # brand_tag 매핑 정보 조회
            cursor.execute("""
                SELECT bt.brand_id, bt.tag_id, t.tag_type, t.tag_category
                FROM brand_tag bt
                JOIN tag t ON bt.tag_id = t.id
            """)
            brand_tag_mappings = cursor.fetchall()

        if not brands:
            print("[경고] 브랜드 데이터가 없습니다.")
            return 0

        # brand_id별 태그 그룹화
        brand_tags = {}
        for mapping in brand_tag_mappings:
            brand_id = mapping['brand_id']
            if brand_id not in brand_tags:
                brand_tags[brand_id] = {'패션': [], '뷰티': [], '콘텐츠': []}
            tag_type = mapping['tag_type']
            if tag_type in brand_tags[brand_id]:
                brand_tags[brand_id][tag_type].append(mapping['tag_id'])

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

            # 해당 브랜드의 실제 태그 매핑 사용
            brand_tag_data = brand_tags.get(brand_id, {'패션': [], '뷰티': [], '콘텐츠': []})

            # 매핑된 태그가 없으면 랜덤 생성
            fashion_tags = brand_tag_data['패션'] if brand_tag_data['패션'] else self._get_random_tag_ids_by_type('패션', 2, 5)
            beauty_tags = brand_tag_data['뷰티'] if brand_tag_data['뷰티'] else self._get_random_tag_ids_by_type('뷰티', 2, 5)
            content_tags = brand_tag_data['콘텐츠'] if brand_tag_data['콘텐츠'] else self._get_random_tag_ids_by_type('콘텐츠', 2, 4)

            # 체형 태그 ID 조회
            body_type_tags = self._get_random_tag_ids(['체형'], 1, 2)
            audience_age_tags = self._get_random_tag_ids(['시청자 나이대'], 1, 3)
            audience_gender_tags = self._get_random_tag_ids(['시청자 성별'], 1, 2)
            video_length_tags = self._get_random_tag_ids(['평균 영상 길이'], 1, 3)

            doc = {
                'brandId': brand_id,
                'brandName': brand['brand_name'],
                'categories': categories,
                'preferredFashionTags': fashion_tags,
                'preferredBeautyTags': beauty_tags,
                'preferredContentTags': content_tags,
                'minCreatorHeight': random.choice([None, 150, 155, 160]),
                'maxCreatorHeight': random.choice([None, 175, 180, 185]),
                'preferredBodyTypeTags': body_type_tags,
                'preferredTopSizeTags': [s for s in random.sample(range(44, 110, 2), 3)],
                'preferredBottomSizeTags': [s for s in random.sample(range(24, 36), 3)],
                'minContentsAverageViews': random.choice([None, 10000, 50000, 100000]),
                'maxContentsAverageViews': random.choice([None, 500000, 1000000]),
                'preferredContentsAgeTags': audience_age_tags,
                'preferredContentsGenderTags': audience_gender_tags,
                'preferredContentsLengthTags': video_length_tags,
            }

            key = f"com.example.RealMatch.match.infrastructure.redis.document.BrandTagDocument:brand:{brand_id}"
            self.redis_client.json().set(key, '$', doc)
            count += 1

        print(f"[완료] {count}개의 브랜드 태그 문서 생성 완료")
        return count

    def generate_campaign_documents(self):
        print("\n[캠페인] Redis 캠페인 태그 문서 생성 중...")

        with self.mysql_conn.cursor() as cursor:
            cursor.execute("""
                SELECT id, title, description, reward_amount, recruit_end_date, start_date, end_date, quota
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

            # 태그 ID 랜덤 생성
            fashion_tags = self._get_random_tag_ids_by_type('패션', 2, 5)
            beauty_tags = self._get_random_tag_ids_by_type('뷰티', 2, 5)
            content_tags = self._get_random_tag_ids_by_type('콘텐츠', 2, 4)
            body_type_tags = self._get_random_tag_ids(['체형'], 1, 2)
            audience_age_tags = self._get_random_tag_ids(['시청자 나이대'], 1, 3)
            audience_gender_tags = self._get_random_tag_ids(['시청자 성별'], 1, 2)
            video_length_tags = self._get_random_tag_ids(['평균 영상 길이'], 1, 3)

            doc = {
                'campaignId': campaign_id,
                'campaignName': campaign['title'],
                'description': campaign['description'],
                'rewardAmount': float(campaign['reward_amount']) if campaign['reward_amount'] else None,
                'recruitEndDate': campaign['recruit_end_date'].isoformat() if campaign['recruit_end_date'] else None,
                'categories': categories,
                'preferredFashionTags': fashion_tags,
                'preferredBeautyTags': beauty_tags,
                'preferredContentTags': content_tags,
                'minCreatorHeight': random.choice([None, 150, 155, 160]),
                'maxCreatorHeight': random.choice([None, 175, 180, 185]),
                'preferredBodyTypeTags': body_type_tags,
                'minCreatorTopSizes': random.choice([None, 44, 50, 55]),
                'maxCreatorTopSizes': random.choice([None, 100, 105, 110]),
                'minCreatorBottomSizes': random.choice([None, 24, 26, 28]),
                'maxCreatorBottomSizes': random.choice([None, 32, 34, 36]),
                'minContentsAverageViews': random.choice([None, 10000, 50000, 100000]),
                'maxContentsAverageViews': random.choice([None, 500000, 1000000]),
                'preferredContentsAgeTags': audience_age_tags,
                'preferredContentsGenderTags': audience_gender_tags,
                'preferredContentsLengthTags': video_length_tags,
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
            # 사용자 정보 조회
            cursor.execute("SELECT id, gender FROM users WHERE role = 'CREATOR' AND is_deleted = FALSE")
            users = cursor.fetchall()

            # user_tag 매핑 정보 조회
            cursor.execute("""
                SELECT ut.user_id, ut.tag_id, t.tag_type, t.tag_category
                FROM user_tag ut
                JOIN tag t ON ut.tag_id = t.id
            """)
            user_tag_mappings = cursor.fetchall()

        if not users:
            print("[경고] CREATOR 역할의 사용자가 없습니다.")
            return 0

        # user_id별 태그 그룹화
        user_tags = {}
        for mapping in user_tag_mappings:
            user_id = mapping['user_id']
            if user_id not in user_tags:
                user_tags[user_id] = {'패션': [], '뷰티': [], '콘텐츠': []}
            tag_type = mapping['tag_type']
            if tag_type in user_tags[user_id]:
                user_tags[user_id][tag_type].append(mapping['tag_id'])

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

            # 해당 사용자의 실제 태그 매핑 사용
            user_tag_data = user_tags.get(user_id, {'패션': [], '뷰티': [], '콘텐츠': []})

            # 매핑된 태그가 없으면 랜덤 생성
            fashion_tags = user_tag_data['패션'] if user_tag_data['패션'] else self._get_random_tag_ids_by_type('패션', 2, 5)
            beauty_tags = user_tag_data['뷰티'] if user_tag_data['뷰티'] else self._get_random_tag_ids_by_type('뷰티', 2, 5)
            content_tags = user_tag_data['콘텐츠'] if user_tag_data['콘텐츠'] else self._get_random_tag_ids_by_type('콘텐츠', 2, 4)

            # 체형 및 기타 태그 ID
            body_type_tag = self._get_random_tag_ids(['체형'], 1, 1)
            audience_age_tags = self._get_random_tag_ids(['시청자 나이대'], 1, 2)
            audience_gender_tags = self._get_random_tag_ids(['시청자 성별'], 1, 2)
            video_length_tag = self._get_random_tag_ids(['평균 영상 길이'], 1, 1)

            doc = {
                'userId': user_id,
                'fashionTags': fashion_tags,
                'beautyTags': beauty_tags,
                'contentTags': content_tags,
                'heightTag': height,
                'bodyTypeTag': body_type_tag[0] if body_type_tag else None,
                'topSizeTag': int(top_size),
                'bottomSizeTag': int(bottom_size),
                'averageContentsViewsTags': self._get_random_tag_ids(['영상 조회수'], 1, 2),
                'contentsAgeTags': audience_age_tags,
                'contentsGenderTags': audience_gender_tags,
                'contentsLengthTags': video_length_tag,
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
                    NumericField('$.heightTag', as_name='heightTag'),
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

        if not self.redis_connected:
            print("[스킵] Redis 연결이 없어 데이터 생성을 스킵합니다.")
            print("=" * 60)
            return

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
  python redis_matching_generator.py              # 모든 문서 + 인덱스 생성
  python redis_matching_generator.py --brands     # 브랜드 문서만 생성
  python redis_matching_generator.py --campaigns  # 캠페인 문서만 생성
  python redis_matching_generator.py --users      # 사용자 문서만 생성
  python redis_matching_generator.py --index-only # 인덱스만 생성
  python redis_matching_generator.py --no-clear   # 기존 데이터 유지하고 추가
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
