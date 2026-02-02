import random
from datetime import datetime, timedelta
from .base_generator import BaseGenerator


class MatchGenerator(BaseGenerator):
    # 뷰티 관련 옵션
    SKIN_TYPES = ['건성', '지성', '복합성', '중성', '민감성']
    SKIN_BRIGHTNESS = ['21호', '23호', '25호', '27호']
    MAKEUP_STYLES = ['내추럴', '글로우', '매트', '데일리', '포인트', '풀메이크업']
    BEAUTY_CATEGORIES = ['스킨케어', '메이크업', '헤어케어', '바디케어', '향수', '네일']
    BEAUTY_FUNCTIONS = ['보습', '미백', '주름개선', '진정', '각질제거', '모공케어', '트러블케어']

    # 패션 관련 옵션
    HEIGHTS = [str(i) for i in range(140, 201)]
    BODY_SHAPES = ["웨이브", "스트레이트", "내추럴", "커브"]
    TOP_SIZES = ['33', '44', '55', '66', '77']
    BOTTOM_SIZES = [str(i) for i in range(23, 66)]
    FASHION_FIELDS = ['캐주얼', '스트릿', '빈티지', '미니멀', '클래식', '스포티', '럭셔리', '페미닌', '유니섹스']
    FASHION_STYLES = ['데일리룩', '오피스룩', '데이트룩', '여행룩', '운동복', '홈웨어', '파티룩']
    FASHION_BRANDS = ['자라', '유니클로', 'H&M', '무신사', 'COS', '마시모두띠', '나이키', '아디다스', '뉴발란스', '컨버스']

    # 콘텐츠 관련 옵션
    VIEWER_GENDERS = ['여성', '남성']
    VIEWER_AGES = ['10~20대', '20~30대', '30~40대', '40~50대', '50대~']
    VIDEO_LENGTHS = ['~15초', '15~30초', '30~45초', '45~60초']
    VIEWS = ['1~10만회', '10~50만회', '50~100만회', '100만회~']
    CONTENT_FORMATS = ['브이로그', '리뷰', '겟레디윗미', '비포&에프터', '스토리/썰', '챌린지']
    CONTENT_TONES = ['전문적인', '감성적인', '유쾌/재밌는', '트렌디한', '일상적인', '수다적인']
    CREATOR_TYPES = ['뷰티 크리에이터', '패션 크리에이터', '라이프스타일', '푸드', '여행', '일상 브이로거']
    GOOD_WITH = ['메이크업', '스킨케어', '패션 스타일링', '코디', '언박싱', '리뷰', 'ASMR', '숏폼']
    INVOLVEMENT_OPTIONS = ['관여안함', '가이드만 제공', '대본 일부 제공', '모든 연출 관여']
    USAGE_SCOPE_OPTIONS = ['크리에이터 1차 활용', '브랜드 2차 활용']

    SNS_PLATFORMS = ['instagram.com', 'youtube.com', 'tiktok.com']

    def _get_table_columns(self, table_name):
        """테이블 컬럼 목록 조회"""
        try:
            with self.connection.cursor() as cursor:
                cursor.execute(f"DESCRIBE {table_name}")
                return [row['Field'] for row in cursor.fetchall()]
        except Exception:
            return []

    def _table_exists(self, table_name):
        """테이블 존재 여부 확인"""
        try:
            with self.connection.cursor() as cursor:
                cursor.execute(f"SHOW TABLES LIKE '{table_name}'")
                return cursor.fetchone() is not None
        except Exception:
            return False

    def _get_enum_values(self, table_name, column_name):
        """ENUM 컬럼의 허용 값 조회"""
        try:
            with self.connection.cursor() as cursor:
                cursor.execute(f"SHOW COLUMNS FROM {table_name} WHERE Field = %s", (column_name,))
                result = cursor.fetchone()
                if result and result.get('Type', '').startswith('enum'):
                    enum_str = result['Type']
                    values = enum_str.replace("enum(", "").replace(")", "").replace("'", "").split(",")
                    return [v.strip() for v in values]
        except Exception:
            pass
        return []

    def generate_match_brand_history(self):
        """브랜드 매칭 히스토리 생성"""
        print(f"\n[브랜드 매칭 히스토리] 생성 중...")

        if not self._table_exists('match_brand_history'):
            print("  - match_brand_history 테이블이 없습니다. 스킵합니다.")
            return

        columns = self._get_table_columns('match_brand_history')

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM users WHERE role = 'CREATOR'")
            creator_ids = [row['id'] for row in cursor.fetchall()]

            cursor.execute("SELECT id FROM brand")
            brand_ids = [row['id'] for row in cursor.fetchall()]

        if not creator_ids:
            print("[경고] 크리에이터가 없습니다.")
            return

        if not brand_ids:
            print("[경고] 브랜드가 없습니다.")
            return

        results = []

        for brand_id in brand_ids:
            num_matches = self.fake.random_int(1, min(5, len(creator_ids)))
            selected_creators = self.fake.random_sample(creator_ids, length=num_matches)

            for creator_id in selected_creators:
                data = {}
                if 'user_id' in columns:
                    data['user_id'] = creator_id
                if 'brand_id' in columns:
                    data['brand_id'] = brand_id
                if 'matching_ratio' in columns:
                    data['matching_ratio'] = self.fake.random_int(50, 100)
                if 'is_deprecated' in columns:
                    data['is_deprecated'] = False
                if 'is_deleted' in columns:
                    data['is_deleted'] = False
                if 'created_at' in columns:
                    data['created_at'] = datetime.now() - timedelta(days=self.fake.random_int(0, 60))
                if 'updated_at' in columns:
                    data['updated_at'] = datetime.now() - timedelta(days=self.fake.random_int(0, 10))
                if data:
                    results.append(data)

        if results:
            cols = list(results[0].keys())
            col_names = ', '.join(cols)
            placeholders = ', '.join([f'%({c})s' for c in cols])
            sql = f"INSERT IGNORE INTO match_brand_history ({col_names}) VALUES ({placeholders})"
            self.execute_many(sql, results, "브랜드 매칭 히스토리")

    def generate_match_campaign_history(self):
        """캠페인 매칭 히스토리 생성"""
        print(f"\n[캠페인 매칭 히스토리] 생성 중...")

        if not self._table_exists('match_campaign_history'):
            print("  - match_campaign_history 테이블이 없습니다. 스킵합니다.")
            return

        columns = self._get_table_columns('match_campaign_history')

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM users WHERE role = 'CREATOR'")
            creator_ids = [row['id'] for row in cursor.fetchall()]

            cursor.execute("SELECT id FROM campaign")
            campaign_ids = [row['id'] for row in cursor.fetchall()]

        if not creator_ids:
            print("[경고] 크리에이터가 없습니다.")
            return

        if not campaign_ids:
            print("[경고] 캠페인이 없습니다.")
            return

        results = []

        for campaign_id in campaign_ids:
            num_matches = self.fake.random_int(1, min(3, len(creator_ids)))
            selected_creators = self.fake.random_sample(creator_ids, length=num_matches)

            for creator_id in selected_creators:
                data = {}
                if 'user_id' in columns:
                    data['user_id'] = creator_id
                if 'campaign_id' in columns:
                    data['campaign_id'] = campaign_id
                if 'matching_ratio' in columns:
                    data['matching_ratio'] = self.fake.random_int(50, 100)
                if 'is_deprecated' in columns:
                    data['is_deprecated'] = False
                if 'is_deleted' in columns:
                    data['is_deleted'] = False
                if 'created_at' in columns:
                    data['created_at'] = datetime.now() - timedelta(days=self.fake.random_int(0, 30))
                if 'updated_at' in columns:
                    data['updated_at'] = datetime.now() - timedelta(days=self.fake.random_int(0, 5))
                if data:
                    results.append(data)

        if results:
            cols = list(results[0].keys())
            col_names = ', '.join(cols)
            placeholders = ', '.join([f'%({c})s' for c in cols])
            sql = f"INSERT IGNORE INTO match_campaign_history ({col_names}) VALUES ({placeholders})"
            self.execute_many(sql, results, "캠페인 매칭 히스토리")

    def generate_user_matching_detail(self):
        """유저 매칭 상세 정보 생성"""
        print(f"\n[유저 매칭 상세] 유저 매칭 상세 정보 생성 중...")

        if not self._table_exists('user_matching_detail'):
            print("  - user_matching_detail 테이블이 없습니다. 스킵합니다.")
            return

        columns = self._get_table_columns('user_matching_detail')

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM users WHERE role = 'CREATOR'")
            creator_ids = [row['id'] for row in cursor.fetchall()]

        if not creator_ids:
            print("[경고] 크리에이터가 없습니다.")
            return

        results = []

        for user_id in creator_ids:
            data = {}

            if 'user_id' in columns:
                data['user_id'] = user_id

            # 뷰티 관련 필드
            if 'skin_type' in columns:
                data['skin_type'] = random.choice(self.SKIN_TYPES)
            if 'skin_brightness' in columns:
                data['skin_brightness'] = random.choice(self.SKIN_BRIGHTNESS)
            if 'makeup_style' in columns:
                data['makeup_style'] = ', '.join(random.sample(self.MAKEUP_STYLES, random.randint(1, 3)))
            if 'interest_categories' in columns:
                data['interest_categories'] = ', '.join(random.sample(self.BEAUTY_CATEGORIES, random.randint(1, 3)))
            if 'interest_functions' in columns:
                data['interest_functions'] = ', '.join(random.sample(self.BEAUTY_FUNCTIONS, random.randint(1, 3)))

            # 패션 관련 필드
            if 'height' in columns:
                data['height'] = random.choice(self.HEIGHTS)
            if 'body_shape' in columns:
                data['body_shape'] = random.choice(self.BODY_SHAPES)
            if 'upper_size' in columns:
                data['upper_size'] = random.choice(self.TOP_SIZES)
            if 'lower_size' in columns:
                data['lower_size'] = random.choice(self.BOTTOM_SIZES)
            if 'interest_fields' in columns:
                data['interest_fields'] = ', '.join(random.sample(self.FASHION_FIELDS, random.randint(1, 3)))
            if 'interest_styles' in columns:
                data['interest_styles'] = ', '.join(random.sample(self.FASHION_STYLES, random.randint(1, 3)))
            if 'interest_brands' in columns:
                data['interest_brands'] = ', '.join(random.sample(self.FASHION_BRANDS, random.randint(1, 4)))

            # 콘텐츠 관련 필드
            if 'sns_url' in columns:
                platform = random.choice(self.SNS_PLATFORMS)
                data['sns_url'] = f"https://www.{platform}/{self.fake.user_name()}"
            if 'viewer_gender' in columns:
                data['viewer_gender'] = random.choice(self.VIEWER_GENDERS)
            if 'viewer_age' in columns:
                data['viewer_age'] = ', '.join(random.sample(self.VIEWER_AGES, random.randint(1, 2)))
            if 'video_length' in columns:
                data['video_length'] = random.choice(self.VIDEO_LENGTHS)
            if 'views' in columns:
                data['views'] = random.choice(self.VIEWS)
            if 'content_formats' in columns:
                data['content_formats'] = ', '.join(random.sample(self.CONTENT_FORMATS, random.randint(1, 3)))
            if 'content_tones' in columns:
                data['content_tones'] = ', '.join(random.sample(self.CONTENT_TONES, random.randint(1, 3)))
            if 'creator_type' in columns:
                data['creator_type'] = random.choice(self.CREATOR_TYPES)
            if 'good_with' in columns:
                data['good_with'] = ', '.join(random.sample(self.GOOD_WITH, random.randint(1, 3)))
            if 'desired_involvement' in columns:
                data['desired_involvement'] = random.choice(self.INVOLVEMENT_OPTIONS)
            if 'desired_usage_scope' in columns:
                data['desired_usage_scope'] = random.choice(self.USAGE_SCOPE_OPTIONS)

            # 타임스탬프
            if 'created_at' in columns:
                data['created_at'] = datetime.now() - timedelta(days=self.fake.random_int(0, 60))
            if 'updated_at' in columns:
                data['updated_at'] = datetime.now() - timedelta(days=self.fake.random_int(0, 10))
            if 'is_deleted' in columns:
                data['is_deleted'] = False

            if data:
                results.append(data)

        if results:
            cols = list(results[0].keys())
            col_names = ', '.join(cols)
            placeholders = ', '.join([f'%({c})s' for c in cols])
            sql = f"INSERT IGNORE INTO user_matching_detail ({col_names}) VALUES ({placeholders})"
            self.execute_many(sql, results, "유저 매칭 상세")

    def generate_all(self):
        self.generate_match_brand_history()
        self.generate_match_campaign_history()
        self.generate_user_matching_detail()
