from datetime import datetime, timedelta
from .base_generator import BaseGenerator


class TagGenerator(BaseGenerator):
    def generate_tags(self):
        print(f"\n[태그] 태그 생성 중...")

        tag_data = {
            '뷰티': {
                '관심 스타일': ['스킨케어', '메이크업', '바디'],
                '관심 기능': ['트러블', '수분/보습', '진정', '미백', '안티에이징', '각질/모공'],
                '피부 타입': ['건성', '지성', '복합성', '민감성'],
                '피부 밝기': ['17호 이하', '17-21호', '21-23호', '23호 이상'],
                '메이크업 스타일': ['내추럴', '화려한', '글로우', '매트']
            },
            '패션': {
                '관심 스타일': ['미니멀', '페미닌', '러블리', '비지니스 캐주얼', '캐주얼', '스트리트'],
                '관심 아이템/분야': ['의류', '가방', '신발', '주얼리', '패션 소품'],
                '선호 브랜드 종류': ['SPA', '빈티지', '증가 브랜드', '디자이너 브랜드', '명품 브랜드'],
                '키': [f"{i}cm" for i in range(140, 201)],  # 이거 태그에서 분리해야 함.
                "체형": ["웨이브", "스트레이트", "내추럴", "커브"],
                "상의 사이즈": ['33', '44', '55', '66', '77'],
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
                        'created_at': datetime.now() - timedelta(days=self.fake.random_int(60, 365)),
                        'updated_at': datetime.now() - timedelta(days=self.fake.random_int(0, 30))
                    })

        sql = """
            INSERT IGNORE INTO tag (tag_type, tag_name, tag_category, created_at, updated_at)
            VALUES (%(tag_type)s, %(tag_name)s, %(tag_category)s, %(created_at)s, %(updated_at)s)
        """
        self.execute_many(sql, tags, "태그")

    def generate_user_tags(self):
        print(f"\n[사용자 태그] 사용자 태그 매핑 생성 중...")

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM users WHERE role = 'CREATOR'")
            creator_ids = [row['id'] for row in cursor.fetchall()]

            cursor.execute("SELECT id, tag_type FROM tag")
            tags = cursor.fetchall()

        if not creator_ids or not tags:
            print("[경고] 크리에이터 또는 태그가 없습니다.")
            return

        user_tags = []
        for creator_id in creator_ids:
            num_tags = self.fake.random_int(5, 15)
            selected_tags = self.fake.random_sample(tags, length=min(num_tags, len(tags)))

            for tag in selected_tags:
                user_tags.append({
                    'user_id': creator_id,
                    'tag_id': tag['id'],
                    'created_at': datetime.now(),
                    'updated_at': datetime.now()
                })

        sql = """
            INSERT IGNORE INTO user_tag (user_id, tag_id, created_at, updated_at)
            VALUES (%(user_id)s, %(tag_id)s, %(created_at)s, %(updated_at)s)
        """
        self.execute_many(sql, user_tags, "사용자 태그")

    def generate_brand_tags(self):
        print(f"\n[브랜드 태그] 브랜드 태그 매핑 생성 중...")

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM brand")
            brand_ids = [row['id'] for row in cursor.fetchall()]

            cursor.execute("SELECT id FROM tag WHERE tag_type IN ('뷰티', '패션')")
            tags = cursor.fetchall()

        if not brand_ids or not tags:
            print("[경고] 브랜드 또는 태그가 없습니다.")
            return

        brand_tags = []
        for brand_id in brand_ids:
            num_tags = self.fake.random_int(3, 10)
            selected_tags = self.fake.random_sample(tags, length=min(num_tags, len(tags)))

            for tag in selected_tags:
                brand_tags.append({
                    'brand_id': brand_id,
                    'tag_id': tag['id'],
                    'created_at': datetime.now(),
                    'updated_at': datetime.now()
                })

        sql = """
            INSERT IGNORE INTO brand_tag (brand_id, tag_id, created_at, updated_at)
            VALUES (%(brand_id)s, %(tag_id)s, %(created_at)s, %(updated_at)s)
        """
        self.execute_many(sql, brand_tags, "브랜드 태그")

    def generate_all(self):
        self.generate_tags()
        self.generate_user_tags()
        self.generate_brand_tags()
