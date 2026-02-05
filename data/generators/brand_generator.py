import random
from datetime import datetime, timedelta
from .base_generator import BaseGenerator


class BrandGenerator(BaseGenerator):
    # 한국어 브랜드명 템플릿
    FASHION_BRAND_NAMES = [
        "스타일리시", "어반룩", "모던웨어", "트렌디샵", "패션포인트",
        "엘레강스", "시크스타일", "뉴웨이브", "클래식룩", "프리미엄웨어",
        "스트릿패션", "캐주얼하우스", "럭셔리룩", "빈티지스타일", "유니크웨어",
        "심플리즘", "오가닉패션", "네오클래식", "모던시크", "퓨어스타일"
    ]

    BEAUTY_BRAND_NAMES = [
        "글로우뷰티", "스킨랩", "퓨어코스메틱", "뷰티플러스", "네이처글로우",
        "클린뷰티", "에센셜케어", "블루밍스킨", "데일리뷰티", "프레시룩",
        "아로마틱", "오가닉스킨", "비건뷰티", "글래머러스", "샤이닝스킨",
        "뷰티랩", "코스메틱존", "스킨포커스", "내추럴글로우", "프리미엄스킨"
    ]

    SIMPLE_INTROS = [
        "트렌디하고 감각적인 스타일을 제안합니다",
        "자연에서 영감을 받은 순수한 아름다움",
        "당신만의 특별한 스타일을 완성하세요",
        "품질과 스타일의 완벽한 조화",
        "일상을 특별하게 만드는 브랜드",
        "감각적인 디자인과 편안한 착용감",
        "세련된 감성과 실용성의 만남",
        "당신의 아름다움을 빛나게 합니다",
        "프리미엄 품질로 완성된 라이프스타일",
        "자신감을 높여주는 특별한 경험"
    ]

    DETAIL_INTROS = [
        "안녕하세요, 저희 브랜드는 고객님의 일상에 특별함을 더하기 위해 탄생했습니다. 최고 품질의 원료와 정성을 담아 제품을 만들고 있으며, 트렌드를 선도하는 디자인과 실용성을 동시에 추구합니다. 고객님의 만족이 저희의 최우선 가치입니다.",
        "저희는 지속 가능한 패션과 뷰티를 추구합니다. 환경을 생각하는 친환경 소재와 윤리적인 생산 방식을 통해 제품을 만들며, 고객님께 건강하고 아름다운 라이프스타일을 제안합니다. 자연과 함께하는 아름다움을 경험해 보세요.",
        "20년 이상의 노하우를 바탕으로 고품질 제품을 선보이고 있습니다. 전문 연구진의 끊임없는 연구와 개발을 통해 고객님께 최상의 결과를 드리기 위해 노력합니다. 신뢰할 수 있는 브랜드로서 함께 성장해 나가겠습니다.",
        "젊고 역동적인 감성을 담은 브랜드입니다. MZ세대의 취향을 반영한 트렌디한 디자인과 합리적인 가격대로 많은 사랑을 받고 있습니다. SNS에서 화제가 되는 핫한 아이템들을 만나보세요.",
        "클래식한 우아함과 현대적인 감각을 결합한 프리미엄 브랜드입니다. 섬세한 디테일과 고급스러운 소재로 특별한 순간을 더욱 빛나게 만들어 드립니다. 품격 있는 라이프스타일의 시작입니다.",
        "자연에서 얻은 순수한 원료로 제품을 만듭니다. 피부에 부담 없이 사용할 수 있는 저자극 포뮬러와 비건 인증을 받은 제품들로 건강한 아름다움을 추구합니다. 자연 그대로의 아름다움을 담았습니다.",
        "혁신적인 기술과 창의적인 디자인으로 새로운 트렌드를 만들어갑니다. 고객님의 다양한 니즈를 반영하여 맞춤형 솔루션을 제공하며, 언제나 한발 앞선 스타일을 제안합니다.",
        "편안함과 스타일을 동시에 잡은 데일리 브랜드입니다. 일상에서 쉽게 활용할 수 있는 실용적인 아이템들과 합리적인 가격으로 많은 고객님들께 사랑받고 있습니다. 매일이 특별해지는 경험을 선사합니다.",
        "글로벌 트렌드를 빠르게 반영하여 세련된 제품을 선보입니다. 해외 유명 디자이너들과의 콜라보레이션을 통해 독특하고 차별화된 스타일을 제안합니다. 세계적인 감각을 경험해 보세요.",
        "고객님의 개성을 존중하는 브랜드입니다. 다양한 스타일과 옵션을 제공하여 자신만의 유니크한 룩을 완성할 수 있도록 도와드립니다. 나만의 스타일을 찾아보세요."
    ]

    def _table_exists(self, table_name):
        print(f"[{table_name}] is creating...")
        try:
            with self.connection.cursor() as cursor:
                cursor.execute(f"SHOW TABLES LIKE '{table_name}'")
                return cursor.fetchone() is not None
        except Exception:
            print(f"[에러] {table_name} 이 없습니다")
            return False

    def _get_table_columns(self, table_name):
        try:
            with self.connection.cursor() as cursor:
                cursor.execute(f"DESCRIBE {table_name}")
                return [row['Field'] for row in cursor.fetchall()]
        except Exception:
            return []

    def _get_enum_values(self, table_name, column_name):
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

    # 브랜드 설명 태그 단어 목록
    BRAND_DESCRIBE_TAGS = [
        "트렌디", "감성적", "고급스러운", "캐주얼", "모던",
        "빈티지", "심플", "유니크", "자연친화", "프리미엄",
        "세련된", "편안한", "실용적", "클래식", "스타일리시",
        "젊은", "우아한", "힙한", "미니멀", "럭셔리",
        "친환경", "비건", "오가닉", "데일리", "베이직"
    ]

    def generate_brand_describe_tag(self, count=3):

        if not self._table_exists('brand_describe_tag'):
            return

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM brand")
            brand_ids = [row['id'] for row in cursor.fetchall()]

        if not brand_ids:
            print("[경고] 브랜드가 없습니다.")
            return

        tags = []
        for brand_id in brand_ids:
            selected_tags = self.fake.random_sample(self.BRAND_DESCRIBE_TAGS, length=min(count, len(self.BRAND_DESCRIBE_TAGS)))
            for tag in selected_tags:
                tags.append({
                    'brand_id': brand_id,
                    'brand_describe_tag': tag,
                    'created_at': datetime.now(),
                })

        if tags:
            sql = """
                INSERT IGNORE INTO brand_describe_tag (brand_id, brand_describe_tag, created_at)
                VALUES (%(brand_id)s, %(brand_describe_tag)s, %(created_at)s)
            """
            self.execute_many(sql, tags, "브랜드 설명 태그")
        else:
            print("[에러] 생성할 데이터가 없습니다.")

    def generate_brands(self, count=20):
        print(f"\n[브랜드] {count}개의 브랜드 생성 중...")

        industry_types = ['BEAUTY', 'FASHION']

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM users WHERE role = 'BRAND' LIMIT %s", (count,))
            user_ids = [row['id'] for row in cursor.fetchall()]

        if not user_ids:
            print("[경고] BRAND 역할의 사용자가 없습니다.")
            return

        brands = []
        for i in range(min(count, len(user_ids))):
            industry = self.fake.random_element(industry_types)

            # 한국어 브랜드명 생성
            if industry == 'FASHION':
                brand_name = random.choice(self.FASHION_BRAND_NAMES)
            else:
                brand_name = random.choice(self.BEAUTY_BRAND_NAMES)

            # 한국어 소개글 생성
            simple_intro = random.choice(self.SIMPLE_INTROS)
            detail_intro = random.choice(self.DETAIL_INTROS)

            brand = {
                'brand_name': brand_name,
                'industry_type': industry,
                'logo_url': f"https://api.dicebear.com/7.x/shapes/svg?seed={self.fake.uuid4()}",
                'simple_intro': simple_intro,
                'detail_intro': detail_intro,
                'homepage_url': self.fake.url(),
                'user_id': user_ids[i],
                'created_by': user_ids[i],
                'is_deleted': False,
                'created_at': datetime.now() - timedelta(days=self.fake.random_int(30, 365)),
                'updated_at': datetime.now() - timedelta(days=self.fake.random_int(0, 30))
            }
            brands.append(brand)

        sql = """
            INSERT INTO brand (brand_name, industry_type, logo_url, simple_intro,
                               detail_intro, homepage_url, user_id, created_by,
                               is_deleted, created_at, updated_at)
            VALUES (%(brand_name)s, %(industry_type)s, %(logo_url)s, %(simple_intro)s,
                    %(detail_intro)s, %(homepage_url)s, %(user_id)s, %(created_by)s,
                    %(is_deleted)s, %(created_at)s, %(updated_at)s)
        """
        self.execute_many(sql, brands, "브랜드")

    def generate_brand_images(self):
        print(f"\n[브랜드 이미지] 브랜드 이미지 생성 중...")

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM brand")
            brand_ids = [row['id'] for row in cursor.fetchall()]

        if not brand_ids:
            print("[경고] 브랜드가 없습니다.")
            return

        images = []
        for brand_id in brand_ids:
            num_images = self.fake.random_int(1, 5)
            for i in range(num_images):
                images.append({
                    'brand_id': brand_id,
                    'image_url': f"https://picsum.photos/800/600?random={self.fake.uuid4()}",
                    'is_deleted': False,
                    'created_at': datetime.now(),
                    'updated_at': datetime.now()
                })

        sql = """
            INSERT INTO brand_image (brand_id, image_url, is_deleted, created_at, updated_at)
            VALUES (%(brand_id)s, %(image_url)s, %(is_deleted)s, %(created_at)s, %(updated_at)s)
        """
        self.execute_many(sql, images, "브랜드 이미지")

    def generate_brand_likes(self):
        print(f"\n[브랜드 좋아요] 브랜드 좋아요 생성 중...")

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM brand")
            brand_ids = [row['id'] for row in cursor.fetchall()]

            cursor.execute("SELECT id FROM users WHERE role = 'CREATOR'")
            creator_ids = [row['id'] for row in cursor.fetchall()]

        if not brand_ids or not creator_ids:
            print("[경고] 브랜드 또는 크리에이터가 없습니다.")
            return

        likes = []
        for creator_id in creator_ids:
            liked_brands = self.fake.random_sample(brand_ids, length=self.fake.random_int(0, min(5, len(brand_ids))))
            for brand_id in liked_brands:
                likes.append({
                    'user_id': creator_id,
                    'brand_id': brand_id,
                    'created_at': datetime.now() - timedelta(days=self.fake.random_int(0, 30))
                })

        if likes:
            sql = """
                INSERT INTO brand_like (user_id, brand_id, created_at)
                VALUES (%(user_id)s, %(brand_id)s, %(created_at)s)
            """
            self.execute_many(sql, likes, "브랜드 좋아요")

    # 협찬 상품명 템플릿
    SPONSOR_NAMES = [
        "프리미엄 스킨케어 세트", "시그니처 메이크업 키트", "데일리 에센스", "럭셔리 크림",
        "모이스처 토너", "비타민 세럼", "클렌징 폼", "선크림 SPF50+", "립밤 세트",
        "헤어 에센스", "바디로션", "핸드크림 세트", "마스크팩 10매", "아이크림",
        "여름 한정 세트", "겨울 보습 세트", "신제품 체험 키트", "베스트셀러 세트"
    ]

    SPONSOR_CONTENTS = [
        "브랜드 대표 제품으로 구성된 스페셜 세트입니다. 다양한 제품을 체험해 보세요.",
        "피부 타입에 맞는 맞춤형 제품입니다. 효과적인 스킨케어 루틴을 경험하세요.",
        "자연 유래 성분으로 만든 저자극 제품입니다. 민감한 피부에도 안심하고 사용 가능합니다.",
        "트렌디한 컬러와 텍스처로 구성된 메이크업 제품입니다.",
        "데일리로 사용하기 좋은 기본 아이템입니다. 가볍고 촉촉한 사용감이 특징입니다.",
        "신제품 출시 기념 특별 구성 세트입니다. 한정 수량으로 제공됩니다.",
        "베스트셀러 제품만 모아 구성했습니다. 검증된 품질을 경험하세요.",
        "여행용으로 편리한 미니 사이즈 세트입니다."
    ]

    def generate_brand_sponsors(self):
        """브랜드 협찬 상품 생성"""
        print(f"\n[브랜드 협찬] 브랜드 협찬 상품 생성 중...")

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM campaign")
            campaign_ids = [row['id'] for row in cursor.fetchall()]

            cursor.execute("SELECT id FROM brand")
            brand_ids = [row['id'] for row in cursor.fetchall()]

        if not campaign_ids or not brand_ids:
            print("[경고] 캠페인 또는 브랜드가 없습니다.")
            return

        sponsors = []
        for campaign_id in campaign_ids:
            # 각 캠페인당 1~3개의 협찬 상품 생성
            num_sponsors = self.fake.random_int(1, 3)
            brand_id = self.fake.random_element(brand_ids)

            for _ in range(num_sponsors):
                total_count = self.fake.random_int(5, 20)
                current_count = self.fake.random_int(0, total_count)

                sponsors.append({
                    'campaign_id': campaign_id,
                    'brand_id': brand_id,
                    'name': random.choice(self.SPONSOR_NAMES),
                    'content': random.choice(self.SPONSOR_CONTENTS),
                    'total_count': total_count,
                    'current_count': current_count,
                    'is_deleted': False,
                    'created_at': datetime.now() - timedelta(days=self.fake.random_int(0, 30)),
                    'updated_at': datetime.now()
                })

        sql = """
            INSERT INTO brand_available_sponsor (campaign_id, brand_id, name, content,
                total_count, current_count, is_deleted, created_at, updated_at)
            VALUES (%(campaign_id)s, %(brand_id)s, %(name)s, %(content)s,
                %(total_count)s, %(current_count)s, %(is_deleted)s, %(created_at)s, %(updated_at)s)
        """
        self.execute_many(sql, sponsors, "브랜드 협찬 상품")

    def generate_brand_categories(self):

        if not self._table_exists('brand_category'):
            return

        columns = self._get_table_columns('brand_category')

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id, industry_type FROM brand")
            brands = cursor.fetchall()

        if not brands:
            print("[경고] 브랜드가 없습니다.")
            return

        categories = []

        category_values = ['FASHION', 'BEAUTY']
        for category in category_values:
            categories.append({
                "name": category,
                "is_deleted": False,
                "created_at": datetime.now(),
            })

        if categories:
            sql = """
                INSERT INTO brand_category (name, is_deleted, created_at)
                VALUES (%(name)s, %(is_deleted)s, %(created_at)s)
            """
            self.execute_many(sql, categories, "브랜드 카테고리")
        else:
            print("  - 생성할 데이터가 없습니다.")

    def generate_brand_available_sponsors(self):

        if not self._table_exists('brand_available_sponsor'):
            return

        columns = self._get_table_columns('brand_available_sponsor')

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM brand")
            brand_ids = [row['id'] for row in cursor.fetchall()]

        if not brand_ids:
            print("[경고] 브랜드가 없습니다.")
            return

        # ENUM 값 동적 조회
        sponsor_types = self._get_enum_values('brand_available_sponsor', 'sponsor_type')
        if not sponsor_types:
            sponsor_types = self._get_enum_values('brand_available_sponsor', 'type')
        if not sponsor_types:
            sponsor_types = ['PRODUCT', 'MONEY', 'BOTH']

        sponsors = []
        for brand_id in brand_ids:
            data = {}
            if 'brand_id' in columns:
                data['brand_id'] = brand_id
            if 'sponsor_type' in columns:
                data['sponsor_type'] = self.fake.random_element(sponsor_types)
            if 'type' in columns:
                data['type'] = self.fake.random_element(sponsor_types)
            if 'name' in columns:
                data['name'] = self.fake.word()
            if 'description' in columns:
                data['description'] = self.fake.sentence()
            if 'is_deleted' in columns:
                data['is_deleted'] = False
            if 'created_at' in columns:
                data['created_at'] = datetime.now()
            if 'updated_at' in columns:
                data['updated_at'] = datetime.now()
            if data:
                sponsors.append(data)

        if sponsors:
            cols = list(sponsors[0].keys())
            col_names = ', '.join(cols)
            placeholders = ', '.join([f'%({c})s' for c in cols])
            sql = f"INSERT IGNORE INTO brand_available_sponsor ({col_names}) VALUES ({placeholders})"
            self.execute_many(sql, sponsors, "브랜드 스폰서")
        else:
            print("  - 생성할 데이터가 없습니다.")

    def generate_brand_sponsor_images(self):

        columns = self._get_table_columns('brand_sponsor_image')

        # brand_available_sponsor가 있으면 그것을 사용, 없으면 brand에서 직접 가져옴
        sponsor_ids = []
        if self._table_exists('brand_available_sponsor'):
            with self.connection.cursor() as cursor:
                cursor.execute("SELECT id FROM brand_available_sponsor")
                sponsor_ids = [row['id'] for row in cursor.fetchall()]

        # sponsor가 없으면 brand_id를 사용
        brand_ids = []
        if not sponsor_ids:
            with self.connection.cursor() as cursor:
                cursor.execute("SELECT id FROM brand")
                brand_ids = [row['id'] for row in cursor.fetchall()]

        if not sponsor_ids and not brand_ids:
            print("[경고] 브랜드 스폰서 또는 브랜드가 없습니다.")
            return

        images = []
        source_ids = sponsor_ids if sponsor_ids else brand_ids
        for source_id in source_ids:
            num_images = self.fake.random_int(1, 3)
            for _ in range(num_images):
                data = {}
                if 'brand_available_sponsor_id' in columns:
                    data['brand_available_sponsor_id'] = source_id
                if 'sponsor_id' in columns:
                    data['sponsor_id'] = source_id
                if 'brand_id' in columns:
                    data['brand_id'] = source_id
                if 'image_url' in columns:
                    data['image_url'] = f"https://picsum.photos/400/300?random={self.fake.random_int(1, 999999)}"
                if 'url' in columns:
                    data['url'] = f"https://picsum.photos/400/300?random={self.fake.random_int(1, 999999)}"
                if 'is_deleted' in columns:
                    data['is_deleted'] = False
                if 'created_at' in columns:
                    data['created_at'] = datetime.now()
                if 'updated_at' in columns:
                    data['updated_at'] = datetime.now()
                if data:
                    images.append(data)

        if images:
            cols = list(images[0].keys())
            col_names = ', '.join(cols)
            placeholders = ', '.join([f'%({c})s' for c in cols])
            sql = f"INSERT IGNORE INTO brand_sponsor_image ({col_names}) VALUES ({placeholders})"
            self.execute_many(sql, images, "브랜드 스폰서 이미지")
        else:
            print("  - 생성할 데이터가 없습니다.")

    def generate_brand_category_views(self):

        if not self._table_exists('brand_category_view'):
            return

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM brand")
            brand_ids = [row['id'] for row in cursor.fetchall()]

            cursor.execute("SELECT id FROM brand_category")
            category_ids = [row['id'] for row in cursor.fetchall()]

        if not brand_ids or not category_ids:
            print("[경고] 브랜드 또는 카테고리가 없습니다.")
            return

        views = []
        for brand_id in brand_ids:
            category_id = self.fake.random_element(category_ids)
            views.append({
                'brand_id': brand_id,
                'category_id': category_id,
                'created_at': datetime.now(),
                'updated_at': None,
                'is_deleted': False,
            })

        if views:
            sql = """
                INSERT IGNORE INTO brand_category_view (brand_id, category_id, created_at, updated_at, is_deleted)
                VALUES (%(brand_id)s, %(category_id)s, %(created_at)s, %(updated_at)s, %(is_deleted)s)
            """
            self.execute_many(sql, views, "브랜드 카테고리 뷰")
        else:
            print("[에러] 생성할 데이터가 없습니다.")

    def generate_brand_like_reads(self):

        if not self._table_exists('brand_like_read'):
            return

        columns = self._get_table_columns('brand_like_read')

        with self.connection.cursor() as cursor:
            # brand_like에서 가져오기
            like_ids = []
            if self._table_exists('brand_like'):
                cursor.execute("SELECT id FROM brand_like")
                like_ids = [row['id'] for row in cursor.fetchall()]

        if not like_ids:
            print("[경고] 브랜드 좋아요가 없습니다.")
            return

        reads = []
        for like_id in like_ids:
            if self.fake.boolean(chance_of_getting_true=70):
                data = {}
                if 'brand_like_id' in columns:
                    data['brand_like_id'] = like_id
                if 'like_id' in columns:
                    data['like_id'] = like_id
                if 'id' in columns and 'brand_like_id' not in columns and 'like_id' not in columns:
                    # id가 FK로 사용될 수 있음
                    pass
                if 'is_read' in columns:
                    data['is_read'] = True
                if 'read_at' in columns:
                    data['read_at'] = datetime.now() - timedelta(days=self.fake.random_int(0, 10))
                if 'is_deleted' in columns:
                    data['is_deleted'] = False
                if 'created_at' in columns:
                    data['created_at'] = datetime.now()
                if 'updated_at' in columns:
                    data['updated_at'] = datetime.now()
                if data:
                    reads.append(data)

        if reads:
            cols = list(reads[0].keys())
            col_names = ', '.join(cols)
            placeholders = ', '.join([f'%({c})s' for c in cols])
            sql = f"INSERT IGNORE INTO brand_like_read ({col_names}) VALUES ({placeholders})"
            self.execute_many(sql, reads, "브랜드 좋아요 읽음")
        else:
            print("  - 생성할 데이터가 없습니다.")

    def _run_generator(self, name, func):
        try:
            func()
            print(f"{name} 완료")
        except Exception as e:
            print(f"{name} 실패: {e}")

            
    def generate_all(self, brand_count=20):
        print("\n========== 브랜드 데이터 생성 ==========")
        self._run_generator("브랜드", lambda: self.generate_brands(brand_count))
        self._run_generator("브랜드 설명 태그", self.generate_brand_describe_tag)
        self._run_generator("브랜드 이미지", self.generate_brand_images)
        self._run_generator("브랜드 좋아요", self.generate_brand_likes)
        self._run_generator("브랜드 카테고리", self.generate_brand_categories)
        self._run_generator("브랜드 스폰서", self.generate_brand_available_sponsors)
        self._run_generator("브랜드 카테고리 뷰", self.generate_brand_category_views)
        self._run_generator("브랜드 좋아요 읽음", self.generate_brand_like_reads)
        print("\n========== 브랜드 데이터 생성 완료 ==========")

    def generate_sponsors(self):
        """협찬 관련 데이터 생성 (캠페인 생성 후 호출 필요)"""
        self.generate_brand_sponsors()
        self.generate_brand_sponsor_images()
