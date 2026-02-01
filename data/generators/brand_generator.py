from datetime import datetime, timedelta
from .base_generator import BaseGenerator


class BrandGenerator(BaseGenerator):
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
            brand_name = f"{self.fake.company()}" if industry == 'FASHION' else f"{self.fake.word().title()} Cosmetics"

            brand = {
                'brand_name': brand_name,
                'industry_type': industry,
                'logo_url': f"https://api.dicebear.com/7.x/shapes/svg?seed={self.fake.uuid4()}",
                'simple_intro': self.fake.catch_phrase()[:200],
                'detail_intro': self.fake.text(max_nb_chars=500),
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

    def generate_all(self, brand_count=20):
        self.generate_brands(brand_count)
        self.generate_brand_images()
        self.generate_brand_likes()
