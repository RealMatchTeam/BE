#!/usr/bin/env python3

import argparse
import os
import sys
from datetime import datetime, timedelta

import pymysql
from dotenv import load_dotenv
from faker import Faker

load_dotenv()

fake = Faker('ko_KR')


class DummyDataGenerator:

    def __init__(self):
        try:
            self.connection = pymysql.connect(
                host=os.getenv('MYSQL_HOST', 'localhost'),
                port=int(os.getenv('MYSQL_PORT', 3306)),
                user=os.getenv('MYSQL_USER'),
                password=os.getenv('MYSQL_PASSWORD'),
                database=os.getenv('MYSQL_DATABASE'),
                charset='utf8mb4',
                cursorclass=pymysql.cursors.DictCursor
            )
            print(f"[성공] 데이터베이스 연결 성공: {os.getenv('MYSQL_DATABASE')}")
        except Exception as e:
            print(f"[오류] 데이터베이스 연결 실패: {e}")
            sys.exit(1)

    def __del__(self):
        if hasattr(self, 'connection') and self.connection:
            self.connection.close()
            print("[성공] 데이터베이스 연결 종료")

    def generate_users(self, count=50):
        print(f"\n[사용자] {count}명의 사용자 생성 중...")

        roles = ['CREATOR', 'BRAND']
        genders = ['MALE', 'FEMALE', 'NONE']

        users = []
        for i in range(count):
            role = fake.random_element(roles)
            gender = fake.random_element(genders)
            birth_date = fake.date_of_birth(minimum_age=18, maximum_age=45)

            user = {
                'name': fake.name(),
                'gender': gender,
                'birth': birth_date,
                'nickname': f"{fake.word()}_{fake.random_number(digits=4)}",
                'email': fake.unique.email(),
                'address': fake.address().replace('\n', ' '),
                'detail_address': f"{fake.random_int(100, 999)}호",
                'role': role,
                'profile_image_url': f"https://api.dicebear.com/7.x/avataaars/svg?seed={fake.uuid4()}",
                'last_login': datetime.now() - timedelta(days=fake.random_int(0, 30)),
                'created_at': datetime.now() - timedelta(days=fake.random_int(1, 365)),
                'updated_at': datetime.now() - timedelta(days=fake.random_int(0, 30))
            }
            users.append(user)

        with self.connection.cursor() as cursor:
            sql = """
                INSERT INTO users (name, gender, birth, nickname, email, address,
                                   detail_address, role, profile_image_url, last_login,
                                   is_deleted, created_at, updated_at)
                VALUES (%(name)s, %(gender)s, %(birth)s, %(nickname)s, %(email)s,
                        %(address)s, %(detail_address)s, %(role)s, %(profile_image_url)s,
                        %(last_login)s, false, %(created_at)s, %(updated_at)s)
            """
            cursor.executemany(sql, users)
            self.connection.commit()
            print(f"[완료] {count}명의 사용자 생성 완료")

    def generate_brands(self, count=20):
        print(f"\n[브랜드] {count}개의 브랜드 생성 중...")

        industry_types = ['BEAUTY', 'FASHION']

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM users WHERE role = 'BRAND' LIMIT %s", (count,))
            user_ids = [row['id'] for row in cursor.fetchall()]

        if not user_ids:
            print("[경고] BRAND 역할의 사용자가 없습니다. 사용자를 먼저 생성하세요.")
            return

        brands = []
        for i in range(min(count, len(user_ids))):
            industry = fake.random_element(industry_types)
            brand_name = f"{fake.company()}" if industry == 'FASHION' else f"{fake.word().title()} Cosmetics"

            brand = {
                'brand_name': brand_name,
                'industry_type': industry,
                'logo_url': f"https://api.dicebear.com/7.x/shapes/svg?seed={fake.uuid4()}",
                'simple_intro': fake.catch_phrase()[:200],
                'detail_intro': fake.text(max_nb_chars=500),
                'homepage_url': fake.url(),
                'matching_rate': fake.random_int(50, 100),
                'created_by': user_ids[i],
                'is_deleted': False,
                'created_at': datetime.now() - timedelta(days=fake.random_int(30, 365)),
                'updated_at': datetime.now() - timedelta(days=fake.random_int(0, 30))
            }
            brands.append(brand)

        with self.connection.cursor() as cursor:
            sql = """
                INSERT INTO brand (brand_name, industry_type, logo_url, simple_intro,
                                   detail_intro, homepage_url, matching_rate, created_by,
                                   is_deleted, created_at, updated_at)
                VALUES (%(brand_name)s, %(industry_type)s, %(logo_url)s, %(simple_intro)s,
                        %(detail_intro)s, %(homepage_url)s, %(matching_rate)s, %(created_by)s,
                        %(is_deleted)s, %(created_at)s, %(updated_at)s)
            """
            cursor.executemany(sql, brands)
            self.connection.commit()
            print(f"[완료] {len(brands)}개의 브랜드 생성 완료")

    def generate_campaigns(self, count=30):
        print(f"\n[캠페인] {count}개의 캠페인 생성 중...")

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM users WHERE role = 'BRAND'")
            user_ids = [row['id'] for row in cursor.fetchall()]

        if not user_ids:
            print("[경고] BRAND 역할의 사용자가 없습니다.")
            return

        campaigns = []
        for i in range(count):
            start_date = fake.date_between(start_date='today', end_date='+60d')
            end_date = start_date + timedelta(days=fake.random_int(30, 90))
            recruit_start = datetime.now() - timedelta(days=fake.random_int(0, 10))
            recruit_end = recruit_start + timedelta(days=fake.random_int(14, 30))

            campaign = {
                'title': fake.sentence(nb_words=6).rstrip('.'),
                'description': fake.text(max_nb_chars=500),
                'preferred_skills': ', '.join([fake.word() for _ in range(3)]),
                'schedule': f"주 {fake.random_int(1, 3)}회 콘텐츠 제작 및 업로드",
                'video_spec': f"{fake.random_element(['세로형', '가로형'])} {fake.random_element(['30초', '1분', '3분'])} 영상",
                'product': fake.sentence(nb_words=4).rstrip('.'),
                'reward_amount': fake.random_element([500000, 1000000, 1500000, 2000000, 3000000]),
                'start_date': start_date,
                'end_date': end_date,
                'recruit_start_date': recruit_start,
                'recruit_end_date': recruit_end,
                'quota': fake.random_int(5, 30),
                'created_by': fake.random_element(user_ids),
                'is_deleted': False,
                'created_at': datetime.now() - timedelta(days=fake.random_int(10, 60)),
                'updated_at': datetime.now() - timedelta(days=fake.random_int(0, 10))
            }
            campaigns.append(campaign)

        with self.connection.cursor() as cursor:
            sql = """
                INSERT INTO campaign (title, description, preferred_skills, schedule,
                                      video_spec, product, reward_amount, start_date,
                                      end_date, recruit_start_date, recruit_end_date,
                                      quota, created_by, is_deleted, created_at, updated_at)
                VALUES (%(title)s, %(description)s, %(preferred_skills)s, %(schedule)s,
                        %(video_spec)s, %(product)s, %(reward_amount)s, %(start_date)s,
                        %(end_date)s, %(recruit_start_date)s, %(recruit_end_date)s,
                        %(quota)s, %(created_by)s, %(is_deleted)s, %(created_at)s, %(updated_at)s)
            """
            cursor.executemany(sql, campaigns)
            self.connection.commit()
            print(f"[완료] {count}개의 캠페인 생성 완료")

    def generate_tags(self, count=50):
        print(f"\n[태그] {count}개의 태그 생성 중...")

        tag_data = {
            '뷰티': {
                '관심 스타일': ['스킨케어', '메이크업', '향수', '바디', '헤어'],
                '관심 기능': ['트러블', '수분/보습', '진정', '미백', '안티에이징', '각질/모공'],
                '피부 타입': ['건성', '지성', '복합성', '민감성']
            },
            '패션': {
                '관심 스타일': ['미니멀', '페미닌', '러블리', '비지니스 캐주얼', '캐주얼', '스트리트'],
                '관심 아이템/분야': ['의류', '가방', '신발', '주얼리', '패션 소품'],
                '선호 브랜드 종류': ['SPA', '빈티지', '증가 브랜드', '디자이너 브랜드', '명품 브랜드'],
                '키': [f"{i}" for i in range(140, 201)],
                "체형": ["마른 체형", "보통 체형", "통통한 체형", "근육질 체형"],
                "상의 사이즈": [str(i) for i in range(44, 120)],
                "하의 사이즈": [str(i) for i in range(23, 66)]
            },
            '콘텐츠': {
                'SNS 정보': ["인스타그램", "유튜브", "틱톡", "블로그", "페이스북"],
                '시청자 성별': ['여성', '남성'],
                '시청자 나이대': ['10~20대', '20~30대', '30~40대', '40~50대', '50대~'],
                '평균 영상 길이': ['~15초', '15~30초', '30~45초', '45~60초'],
                '영상 조회수': ['1~10만회', '10~50만회', '50~100만회', '100만회~'],
                '콘텐츠 유형': ['인스타 스토리', '인스타 포스터', '인스타 릴스'],
                '콘텐츠 종류': ['브이로그', '리뷰', '겟레디윗미', '비포&에프터', '스토리/썰', '챌린지'],
                '콘텐츠 톤': ['전문적인', '감성적인', '유쾌/재밌는', '트렌디한', '일상적인', '수다적인'],
                '콘텐츠 희망 관여도': ['관여 안함', '가이드 라인만 제공', '대본 일부 제공', '모든 연출 관여'],
                '콘텐츠 희망 활용 범위': ['크리에이터 1차 활용', '브랜드 2차 활용']
            }
        }

        tags = []
        for tag_type, categories in tag_data.items():
            for category, names in categories.items():
                for name in names:
                    tags.append({
                        'tag_type': tag_type,
                        'tag_name': name,
                        'tag_category': category,
                        'created_at': datetime.now() - timedelta(days=fake.random_int(60, 365)),
                        'updated_at': datetime.now() - timedelta(days=fake.random_int(0, 30))
                    })

        with self.connection.cursor() as cursor:
            sql = """
                INSERT IGNORE INTO tag (tag_type, tag_name, tag_category, created_at, updated_at)
                VALUES (%(tag_type)s, %(tag_name)s, %(tag_category)s, %(created_at)s, %(updated_at)s)
            """
            cursor.executemany(sql, tags)
            self.connection.commit()
            print(f"[완료] {len(tags)}개의 태그 생성 완료")

    def generate_all(self, users=50, brands=20, campaigns=30, tags=50):
        """모든 더미 데이터 생성"""
        print("[시작] 더미 데이터 생성 시작...\n")
        print(f"생성할 데이터:")
        print(f"  - 사용자: {users}명")
        print(f"  - 브랜드: {brands}개")
        print(f"  - 캠페인: {campaigns}개")
        print(f"  - 태그: {tags}개")

        try:
            self.generate_users(users)
            self.generate_brands(brands)
            self.generate_campaigns(campaigns)
            self.generate_tags(tags)

            print("\n" + "="*60)
            print("[완료] 모든 더미 데이터 생성 완료!")
            print("="*60)
        except Exception as e:
            print(f"\n[오류] 오류 발생: {e}")
            self.connection.rollback()
            raise


def main():
    """메인 함수"""
    parser = argparse.ArgumentParser(
        description='RealMatch 더미 데이터 생성 스크립트',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
예시:
  python generate_dummy_data.py --users 50 --brands 20 --campaigns 30
  python generate_dummy_data.py --all 100
        """
    )

    parser.add_argument('--users', type=int, default=50,
                        help='생성할 사용자 수 (기본값: 50)')
    parser.add_argument('--brands', type=int, default=20,
                        help='생성할 브랜드 수 (기본값: 20)')
    parser.add_argument('--campaigns', type=int, default=30,
                        help='생성할 캠페인 수 (기본값: 30)')
    parser.add_argument('--tags', type=int, default=50,
                        help='생성할 태그 수 (기본값: 50)')
    parser.add_argument('--all', type=int,
                        help='모든 항목에 동일한 개수 적용')

    args = parser.parse_args()

    if args.all:
        args.users = args.all
        args.brands = args.all // 2
        args.campaigns = args.all // 2
        args.tags = args.all

    generator = DummyDataGenerator()
    generator.generate_all(
        users=args.users,
        brands=args.brands,
        campaigns=args.campaigns,
        tags=args.tags
    )


if __name__ == '__main__':
    main()
