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
                'matching_rate': self.fake.random_int(50, 100),
                'user_id': user_ids[i],
                'created_by': user_ids[i],
                'is_deleted': False,
                'created_at': datetime.now() - timedelta(days=self.fake.random_int(30, 365)),
                'updated_at': datetime.now() - timedelta(days=self.fake.random_int(0, 30))
            }
            brands.append(brand)

        sql = """
            INSERT INTO brand (brand_name, industry_type, logo_url, simple_intro,
                               detail_intro, homepage_url, matching_rate, user_id, created_by,
                               is_deleted, created_at, updated_at)
            VALUES (%(brand_name)s, %(industry_type)s, %(logo_url)s, %(simple_intro)s,
                    %(detail_intro)s, %(homepage_url)s, %(matching_rate)s, %(user_id)s, %(created_by)s,
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

    def generate_brand_sponsor_images(self):
        """브랜드 협찬 상품 이미지 생성"""
        print(f"\n[협찬 상품 이미지] 협찬 상품 이미지 생성 중...")

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM brand_available_sponsor")
            sponsor_ids = [row['id'] for row in cursor.fetchall()]

            cursor.execute("SELECT id FROM users WHERE role = 'BRAND' LIMIT 1")
            brand_user = cursor.fetchone()
            created_by = brand_user['id'] if brand_user else 1

        if not sponsor_ids:
            print("[경고] 협찬 상품이 없습니다.")
            return

        images = []
        for sponsor_id in sponsor_ids:
            # 각 협찬 상품당 1~4개의 이미지 생성
            num_images = self.fake.random_int(1, 4)
            for _ in range(num_images):
                images.append({
                    'sponsor_id': sponsor_id,
                    'image_url': f"https://picsum.photos/600/600?random={self.fake.uuid4()}",
                    'created_by': created_by,
                    'is_deleted': False,
                    'created_at': datetime.now(),
                    'updated_at': datetime.now()
                })

        sql = """
            INSERT INTO brand_sponsor_image (sponsor_id, image_url, created_by,
                is_deleted, created_at, updated_at)
            VALUES (%(sponsor_id)s, %(image_url)s, %(created_by)s,
                %(is_deleted)s, %(created_at)s, %(updated_at)s)
        """
        self.execute_many(sql, images, "협찬 상품 이미지")

    def generate_all(self, brand_count=20):
        self.generate_brands(brand_count)
        self.generate_brand_images()
        self.generate_brand_likes()

    def generate_sponsors(self):
        """협찬 관련 데이터 생성 (캠페인 생성 후 호출 필요)"""
        self.generate_brand_sponsors()
        self.generate_brand_sponsor_images()
