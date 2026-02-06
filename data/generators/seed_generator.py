from datetime import datetime
from .base_generator import BaseGenerator


class SeedGenerator(BaseGenerator):

    def _table_exists(self, table_name):
        try:
            with self.connection.cursor() as cursor:
                cursor.execute(f"SHOW TABLES LIKE '{table_name}'")
                return cursor.fetchone() is not None
        except Exception:
            return False

    def _generate_tag_content_for_business(self):
        """
        비지니스 컨텐츠에서만 사용됨.
        """
        print(f"\n[비지니스용 콘텐츠 태그] 비지니스용 콘텐츠 태그 생성 중...")

        table_exists = self._table_exists('tag_content')
        print(f"  - tag_content 테이블 존재: {table_exists}")
        
        if not table_exists:
            print("  - tag_content 테이블이 없습니다. 스킵합니다.")
            return

        tags = [
            # FORMAT
            (1, 'FORMAT', 'INSTAGRAM_STORY', '인스타 스토리'),
            (2, 'FORMAT', 'INSTAGRAM_POST', '인스타 포스트'),
            (3, 'FORMAT', 'INSTAGRAM_REELS', '인스타 릴스'),
            (4, 'FORMAT', 'ETC', '기타'),
            # CATEGORY
            (5, 'CATEGORY', 'VLOG', '브이로그'),
            (6, 'CATEGORY', 'REVIEW', '리뷰'),
            (7, 'CATEGORY', 'GET_READY_WITH_ME', '겟레디윗미'),
            (8, 'CATEGORY', 'BEFORE_AFTER', '비포&애프터'),
            (9, 'CATEGORY', 'STORY_TALK', '스토리/썰'),
            (10, 'CATEGORY', 'CHALLENGE', '챌린지'),
            (11, 'CATEGORY', 'ETC', '기타'),
            # TONE
            (12, 'TONE', 'PROFESSIONAL', '전문적인'),
            (13, 'TONE', 'EMOTIONAL', '감성적인'),
            (14, 'TONE', 'FUN', '유쾌/재밌는'),
            (15, 'TONE', 'TRENDY', '트렌디한'),
            (16, 'TONE', 'CASUAL', '일상적인'),
            (17, 'TONE', 'TALKATIVE', '수다적인'),
            (18, 'TONE', 'ETC', '기타'),
            # INVOLVEMENT
            (19, 'INVOLVEMENT', 'NONE', '관여안함'),
            (20, 'INVOLVEMENT', 'GUIDE_ONLY', '가이드만 제공'),
            (21, 'INVOLVEMENT', 'PARTIAL_SCRIPT', '대본 일부 제공'),
            (22, 'INVOLVEMENT', 'FULL_CONTROL', '모든 연출 관여'),
            (23, 'INVOLVEMENT', 'ETC', '기타'),
            # USAGE_RANGE
            (24, 'USAGE_RANGE', 'CREATOR_PRIMARY', '크리에이터 1차활용'),
            (25, 'USAGE_RANGE', 'BRAND_SECONDARY', '브랜드 2차활용'),
            (26, 'USAGE_RANGE', 'ETC', '기타'),
        ]

        data = []
        for tag in tags:
            data.append({
                'id': tag[0],
                'tag_type': tag[1],
                'eng_name': tag[2],
                'kor_name': tag[3],
                'created_at': datetime.now(),
                'updated_at': datetime.now(),
                'is_deleted': False
            })

        sql = """
            INSERT IGNORE INTO tag_content (id, tag_type, eng_name, kor_name, created_at, updated_at, is_deleted)
            VALUES (%(id)s, %(tag_type)s, %(eng_name)s, %(kor_name)s, %(created_at)s, %(updated_at)s, %(is_deleted)s)
        """
        self.execute_many(sql, data, "콘텐츠 태그")

    def _generate_terms(self):
        print(f"\n[약관] 약관 데이터 생성 중...")

        if not self._table_exists('term'):
            print("  - term 테이블이 없습니다. 스킵합니다.")
            return

        terms = [
            (1, 'AGE', True, '1.0'),
            (2, 'SERVICE_TERMS', True, '1.0'),
            (3, 'PRIVACY_COLLECTION', True, '1.0'),
            (4, 'PRIVACY_THIRD_PARTY', True, '1.0'),
            (5, 'MARKETING_PRIVACY_COLLECTION', False, '1.0'),
            (6, 'MARKETING_NOTIFICATION', False, '1.0'),
        ]

        data = []
        for term in terms:
            data.append({
                'id': term[0],
                'name': term[1],
                'is_required': term[2],
                'version': term[3],
                'created_at': datetime.now(),
                'updated_at': datetime.now()
            })

        sql = """
            INSERT IGNORE INTO term (id, name, is_required, version, created_at, updated_at)
            VALUES (%(id)s, %(name)s, %(is_required)s, %(version)s, %(created_at)s, %(updated_at)s)
        """
        self.execute_many(sql, data, "약관")

    def _generate_signup_purposes(self):
        print(f"\n[가입 목적] 가입 목적 데이터 생성 중...")

        if not self._table_exists('signup_purposes'):
            print("  - signup_purposes 테이블이 없습니다. 스킵합니다.")
            return

        purposes = [
            (1, '제품 협찬'),
            (2, '수익 창출'),
            (3, '팔로워 증대'),
            (4, '브랜드 강화'),
            (5, '신규 브랜드'),
            (6, '트렌드 탐색'),
        ]

        data = []
        for purpose in purposes:
            data.append({
                'id': purpose[0],
                'purpose_name': purpose[1],
                'created_at': datetime.now(),
                'updated_at': datetime.now()
            })

        sql = """
            INSERT IGNORE INTO signup_purposes (id, purpose_name, created_at, updated_at)
            VALUES (%(id)s, %(purpose_name)s, %(created_at)s, %(updated_at)s)
        """
        self.execute_many(sql, data, "가입 목적")

    def _generate_content_categories(self):
        """콘텐츠 카테고리 데이터 생성"""
        print(f"\n[콘텐츠 카테고리] 콘텐츠 카테고리 데이터 생성 중...")

        if not self._table_exists('content_categories'):
            print("  - content_categories 테이블이 없습니다. 스킵합니다.")
            return

        categories = [
            (1, '패션'),
            (2, '뷰티'),
        ]

        data = []
        for cat in categories:
            data.append({
                'id': cat[0],
                'category_name': cat[1],
                'created_at': datetime.now(),
                'updated_at': datetime.now()
            })

        sql = """
            INSERT IGNORE INTO content_categories (id, category_name, created_at, updated_at)
            VALUES (%(id)s, %(category_name)s, %(created_at)s, %(updated_at)s)
        """
        self.execute_many(sql, data, "콘텐츠 카테고리")

    def generate_sample_brand(self):
        """샘플 브랜드 생성"""
        print(f"\n[샘플 브랜드] 샘플 브랜드 생성 중...")

        if not self._table_exists('brand'):
            print("  - brand 테이블이 없습니다. 스킵합니다.")
            return

        # user_id 1이 있는지 확인
        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM users WHERE id = 1")
            user = cursor.fetchone()
            if not user:
                print("  - user_id 1이 없습니다. 스킵합니다.")
                return

        data = [{
            'brand_name': '비플레인',
            'user_id': 1,
            'industry_type': 'BEAUTY',
            'logo_url': 'https://cdn.example.com/logo/beplain.png',
            'simple_intro': '클린 뷰티를 지향하는 스킨케어 브랜드',
            'detail_intro': '비플레인은 자연 유래 성분을 기반으로 민감 피부도 안심하고 사용할 수 있는 클린 뷰티 제품을 만듭니다.',
            'homepage_url': 'https://www.beplain.com',
            'matching_rate': 87,
            'created_by': 1,
            'created_at': datetime.now(),
            'updated_at': datetime.now(),
            'is_deleted': False
        }]

        sql = """
            INSERT IGNORE INTO brand (brand_name, user_id, industry_type, logo_url, simple_intro,
                detail_intro, homepage_url, matching_rate, created_by, created_at, updated_at, is_deleted)
            VALUES (%(brand_name)s, %(user_id)s, %(industry_type)s, %(logo_url)s, %(simple_intro)s,
                %(detail_intro)s, %(homepage_url)s, %(matching_rate)s, %(created_by)s, %(created_at)s, %(updated_at)s, %(is_deleted)s)
        """
        self.execute_many(sql, data, "샘플 브랜드")

    def generate_sample_campaigns(self):

        print(f"\n[샘플 캠페인] 샘플 캠페인 생성 중...")

        if not self._table_exists('campaign'):
            print("  - campaign 테이블이 없습니다. 스킵합니다.")
            return

        # brand_id 1이 있는지 확인
        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM brand LIMIT 1")
            brand = cursor.fetchone()
            if not brand:
                print("  - 브랜드가 없습니다. 스킵합니다.")
                return
            brand_id = brand['id']

        campaigns = [
            # 기본 캠페인 3개
            {
                'title': '비플레인 선크림 리뷰 콘텐츠',
                'description': '선크림 리뷰 영상 제작 캠페인입니다.',
                'image_url': 'https://cdn.example.com/logo/beplain.png',
                'preferred_skills': '인스타 뷰티 크리에이터 우대',
                'schedule': '모집 : 2025-01-05 ~ 2025-01-15  /  업로드 : 2025-02-01',
                'video_spec': '추후 논의 예정',
                'product': '글로우 크림 1개',
                'reward_amount': 200000,
                'start_date': '2025-01-20',
                'end_date': '2025-01-30',
                'recruit_start_date': '2025-01-05 00:00:00',
                'recruit_end_date': '2025-01-15 23:59:59',
                'quota': 5,
            },
            {
                'title': '비플레인 민감성 피부 케어 리뷰',
                'description': '민감성 피부를 위한 비플레인 제품 리뷰 캠페인입니다.',
                'image_url': 'https://cdn.example.com/logo/beplain.png',
                'preferred_skills': '인스타 뷰티 크리에이터 우대',
                'schedule': '모집 : 2025-01-18 ~ 2025-01-28  /  업로드 : 2025-02-25',
                'video_spec': '영상 1개, 길이 20초 이내',
                'product': '글로우 크림 1개',
                'reward_amount': 200000,
                'start_date': '2025-02-01',
                'end_date': '2025-02-10',
                'recruit_start_date': '2025-01-18 00:00:00',
                'recruit_end_date': '2025-01-28 23:59:59',
                'quota': 5,
            },
            {
                'title': '비플레인 데일리 스킨케어 콘텐츠',
                'description': '비플레인 제품을 활용한 일상적인 스킨케어 콘텐츠 제작 캠페인입니다.',
                'image_url': 'https://cdn.example.com/logo/beplain.png',
                'preferred_skills': '',
                'schedule': '추후 공지 예정',
                'video_spec': '추후 논의 예정',
                'product': '글로우 크림 1개',
                'reward_amount': 200000,
                'start_date': '2025-02-15',
                'end_date': '2025-02-25',
                'recruit_start_date': '2025-02-01 00:00:00',
                'recruit_end_date': '2025-02-10 23:59:59',
                'quota': 5,
            },
            # 진행중 캠페인 5개
            {'title': '비플레인 진정 토너 진행중', 'description': '진정 토너 리뷰 캠페인', 'product': '진정 토너', 'reward_amount': 150000, 'start_date': '2026-02-10', 'end_date': '2026-02-20', 'recruit_start_date': '2026-02-01 00:00:00', 'recruit_end_date': '2026-02-10 23:59:59'},
            {'title': '비플레인 약산성 클렌저 진행중', 'description': '약산성 클렌저 리뷰 캠페인', 'product': '클렌저', 'reward_amount': 150000, 'start_date': '2026-02-12', 'end_date': '2026-02-22', 'recruit_start_date': '2026-02-02 00:00:00', 'recruit_end_date': '2026-02-11 23:59:59'},
            {'title': '비플레인 수분 크림 진행중', 'description': '수분 크림 데일리 루틴 캠페인', 'product': '수분 크림', 'reward_amount': 180000, 'start_date': '2026-02-15', 'end_date': '2026-02-25', 'recruit_start_date': '2026-02-03 00:00:00', 'recruit_end_date': '2026-02-12 23:59:59'},
            {'title': '비플레인 저자극 루틴 진행중', 'description': '저자극 스킨케어 루틴 캠페인', 'product': '스킨케어 세트', 'reward_amount': 200000, 'start_date': '2026-02-14', 'end_date': '2026-02-24', 'recruit_start_date': '2026-02-01 00:00:00', 'recruit_end_date': '2026-02-09 23:59:59'},
            {'title': '비플레인 약산성 라인 진행중', 'description': '약산성 라인 집중 리뷰 캠페인', 'product': '약산성 라인', 'reward_amount': 200000, 'start_date': '2026-02-18', 'end_date': '2026-02-28', 'recruit_start_date': '2026-02-04 00:00:00', 'recruit_end_date': '2026-02-13 23:59:59'},
            # 진행예정 캠페인 5개
            {'title': '비플레인 봄 수분 케어 예정', 'description': '봄 시즌 수분 케어 캠페인', 'product': '수분 크림', 'reward_amount': 180000, 'start_date': '2026-03-05', 'end_date': '2026-03-15', 'recruit_start_date': '2026-02-15 00:00:00', 'recruit_end_date': '2026-02-25 23:59:59'},
            {'title': '비플레인 진정 앰플 예정', 'description': '진정 앰플 리뷰 캠페인', 'product': '진정 앰플', 'reward_amount': 200000, 'start_date': '2026-03-10', 'end_date': '2026-03-20', 'recruit_start_date': '2026-02-18 00:00:00', 'recruit_end_date': '2026-02-28 23:59:59'},
            {'title': '비플레인 약산성 브랜디드 예정', 'description': '브랜디드 콘텐츠 캠페인', 'product': '약산성 세트', 'reward_amount': 250000, 'start_date': '2026-03-15', 'end_date': '2026-03-25', 'recruit_start_date': '2026-02-20 00:00:00', 'recruit_end_date': '2026-03-02 23:59:59'},
            {'title': '비플레인 데일리 루틴 챌린지 예정', 'description': '데일리 루틴 챌린지 캠페인', 'product': '스킨케어 키트', 'reward_amount': 300000, 'start_date': '2026-03-20', 'end_date': '2026-03-30', 'recruit_start_date': '2026-02-22 00:00:00', 'recruit_end_date': '2026-03-05 23:59:59'},
            {'title': '비플레인 민감 피부 SOS 예정', 'description': '민감 피부 SOS 케어 캠페인', 'product': 'SOS 세트', 'reward_amount': 220000, 'start_date': '2026-03-25', 'end_date': '2026-04-05', 'recruit_start_date': '2026-02-25 00:00:00', 'recruit_end_date': '2026-03-07 23:59:59'},
        ]

        data = []
        for camp in campaigns:
            data.append({
                'title': camp['title'],
                'description': camp['description'],
                'brand_id': brand_id,
                'image_url': camp.get('image_url', 'https://cdn.example.com/logo/beplain.png'),
                'preferred_skills': camp.get('preferred_skills', ''),
                'schedule': camp.get('schedule', ''),
                'video_spec': camp.get('video_spec', ''),
                'product': camp['product'],
                'reward_amount': camp['reward_amount'],
                'start_date': camp['start_date'],
                'end_date': camp['end_date'],
                'recruit_start_date': camp['recruit_start_date'],
                'recruit_end_date': camp['recruit_end_date'],
                'quota': camp.get('quota', 5),
                'created_by': 1,
                'created_at': datetime.now(),
                'updated_at': datetime.now(),
                'is_deleted': False
            })

        sql = """
            INSERT IGNORE INTO campaign (title, description, brand_id, image_url, preferred_skills, schedule,
                video_spec, product, reward_amount, start_date, end_date, recruit_start_date, recruit_end_date,
                quota, created_by, created_at, updated_at, is_deleted)
            VALUES (%(title)s, %(description)s, %(brand_id)s, %(image_url)s, %(preferred_skills)s, %(schedule)s,
                %(video_spec)s, %(product)s, %(reward_amount)s, %(start_date)s, %(end_date)s, %(recruit_start_date)s,
                %(recruit_end_date)s, %(quota)s, %(created_by)s, %(created_at)s, %(updated_at)s, %(is_deleted)s)
        """
        self.execute_many(sql, data, "샘플 캠페인")

    def generate_campaign_content_tags(self):
        """캠페인 콘텐츠 태그 연결"""
        print(f"\n[캠페인 콘텐츠 태그] 캠페인 콘텐츠 태그 연결 중...")

        if not self._table_exists('campaign_content_tag'):
            print("  - campaign_content_tag 테이블이 없습니다. 스킵합니다.")
            return

        if not self._table_exists('tag_content'):
            print("  - tag_content 테이블이 없습니다. 스킵합니다.")
            return

        with self.connection.cursor() as cursor:
            # 캠페인 ID 조회
            cursor.execute("SELECT id FROM campaign ORDER BY id LIMIT 3")
            campaigns = cursor.fetchall()
            if len(campaigns) < 3:
                print("  - 캠페인이 3개 미만입니다. 스킵합니다.")
                return

            campaign_ids = [c['id'] for c in campaigns]

            # 태그 ID 매핑 조회
            cursor.execute("SELECT id, tag_type, eng_name FROM tag_content")
            tags = cursor.fetchall()

        tag_map = {(t['tag_type'], t['eng_name']): t['id'] for t in tags}

        # 캠페인 1 태그
        campaign1_tags = [
            ('FORMAT', 'INSTAGRAM_REELS'),
            ('CATEGORY', 'GET_READY_WITH_ME'),
            ('CATEGORY', 'BEFORE_AFTER'),
            ('TONE', 'CASUAL'),
            ('INVOLVEMENT', 'GUIDE_ONLY'),
            ('USAGE_RANGE', 'CREATOR_PRIMARY'),
        ]

        # 캠페인 2 태그
        campaign2_tags = [
            ('FORMAT', 'INSTAGRAM_POST'),
            ('CATEGORY', 'REVIEW'),
            ('TONE', 'PROFESSIONAL'),
            ('INVOLVEMENT', 'PARTIAL_SCRIPT'),
            ('USAGE_RANGE', 'BRAND_SECONDARY'),
        ]

        # 캠페인 3 태그
        campaign3_tags = [
            ('FORMAT', 'INSTAGRAM_REELS'),
            ('CATEGORY', 'VLOG'),
            ('TONE', 'FUN'),
            ('INVOLVEMENT', 'NONE'),
            ('USAGE_RANGE', 'CREATOR_PRIMARY'),
        ]

        data = []

        # 캠페인 1 태그 추가
        for tag_key in campaign1_tags:
            if tag_key in tag_map:
                data.append({
                    'campaign_id': campaign_ids[0],
                    'content_tag_id': tag_map[tag_key],
                    'custom_tag_value': None,
                    'created_at': datetime.now(),
                    'updated_at': datetime.now()
                })

        # 캠페인 2 태그 추가
        for tag_key in campaign2_tags:
            if tag_key in tag_map:
                data.append({
                    'campaign_id': campaign_ids[1],
                    'content_tag_id': tag_map[tag_key],
                    'custom_tag_value': None,
                    'created_at': datetime.now(),
                    'updated_at': datetime.now()
                })

        # 캠페인 2 ETC 태그 (커스텀 값)
        if ('CATEGORY', 'ETC') in tag_map:
            data.append({
                'campaign_id': campaign_ids[1],
                'content_tag_id': tag_map[('CATEGORY', 'ETC')],
                'custom_tag_value': '성분 분석 리뷰',
                'created_at': datetime.now(),
                'updated_at': datetime.now()
            })

        # 캠페인 3 태그 추가
        for tag_key in campaign3_tags:
            if tag_key in tag_map:
                data.append({
                    'campaign_id': campaign_ids[2],
                    'content_tag_id': tag_map[tag_key],
                    'custom_tag_value': None,
                    'created_at': datetime.now(),
                    'updated_at': datetime.now()
                })

        # 캠페인 3 ETC 태그 (커스텀 값)
        if ('FORMAT', 'ETC') in tag_map:
            data.append({
                'campaign_id': campaign_ids[2],
                'content_tag_id': tag_map[('FORMAT', 'ETC')],
                'custom_tag_value': '인스타 스레드',
                'created_at': datetime.now(),
                'updated_at': datetime.now()
            })

        if data:
            sql = """
                INSERT IGNORE INTO campaign_content_tag (campaign_id, content_tag_id, custom_tag_value, created_at, updated_at)
                VALUES (%(campaign_id)s, %(content_tag_id)s, %(custom_tag_value)s, %(created_at)s, %(updated_at)s)
            """
            self.execute_many(sql, data, "캠페인 콘텐츠 태그")

    def generate_all(self):
        """모든 시드 데이터 생성"""
        self._generate_tag_content_for_business()
        self._generate_terms()
        self._generate_signup_purposes()
        self._generate_content_categories()
        self.generate_sample_brand()
        self.generate_sample_campaigns()
        self.generate_campaign_content_tags()
