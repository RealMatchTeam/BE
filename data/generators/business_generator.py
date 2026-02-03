from datetime import datetime, timedelta
from .base_generator import BaseGenerator


class BusinessGenerator(BaseGenerator):

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

    def generate_brand_categories(self):
        """브랜드 카테고리 생성"""
        print(f"\n[브랜드 카테고리] 브랜드 카테고리 생성 중...")

        if not self._table_exists('brand_category'):
            print("  - brand_category 테이블이 없습니다. 스킵합니다.")
            return

        columns = self._get_table_columns('brand_category')
        print(f"  - 컬럼: {columns}")

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id, industry_type FROM brand")
            brands = cursor.fetchall()

        if not brands:
            print("[경고] 브랜드가 없습니다.")
            return

        # ENUM 값 동적 조회
        category_values = self._get_enum_values('brand_category', 'category')
        if not category_values:
            category_values = self._get_enum_values('brand_category', 'category_type')
        if not category_values:
            category_values = ['FASHION', 'BEAUTY']

        categories = []
        for brand in brands:
            industry = brand.get('industry_type', '')
            if industry in ['FASHION', 'fashion']:
                cat_list = ['FASHION'] if 'FASHION' in category_values else [category_values[0]] if category_values else ['FASHION']
            elif industry in ['BEAUTY', 'beauty']:
                cat_list = ['BEAUTY'] if 'BEAUTY' in category_values else [category_values[-1]] if category_values else ['BEAUTY']
            else:
                cat_list = self.fake.random_elements(category_values, unique=True, length=self.fake.random_int(1, min(2, len(category_values))))

            for cat in cat_list:
                data = {}
                if 'brand_id' in columns:
                    data['brand_id'] = brand['id']
                if 'category' in columns:
                    data['category'] = cat
                if 'category_type' in columns:
                    data['category_type'] = cat
                if 'name' in columns:
                    data['name'] = cat
                if 'is_deleted' in columns:
                    data['is_deleted'] = False
                if 'created_at' in columns:
                    data['created_at'] = datetime.now()
                if 'updated_at' in columns:
                    data['updated_at'] = datetime.now()
                if data:
                    categories.append(data)

        if categories:
            cols = list(categories[0].keys())
            col_names = ', '.join(cols)
            placeholders = ', '.join([f'%({c})s' for c in cols])
            sql = f"INSERT IGNORE INTO brand_category ({col_names}) VALUES ({placeholders})"
            self.execute_many(sql, categories, "브랜드 카테고리")
        else:
            print("  - 생성할 데이터가 없습니다.")

    def generate_brand_available_sponsors(self):
        """브랜드 가능한 스폰서 생성"""
        print(f"\n[브랜드 스폰서] 브랜드 가능한 스폰서 생성 중...")

        if not self._table_exists('brand_available_sponsor'):
            print("  - brand_available_sponsor 테이블이 없습니다. 스킵합니다.")
            return

        columns = self._get_table_columns('brand_available_sponsor')
        print(f"  - 컬럼: {columns}")

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
        """브랜드 스폰서 이미지 생성"""
        print(f"\n[브랜드 스폰서 이미지] 브랜드 스폰서 이미지 생성 중...")

        if not self._table_exists('brand_sponsor_image'):
            print("  - brand_sponsor_image 테이블이 없습니다. 스킵합니다.")
            return

        columns = self._get_table_columns('brand_sponsor_image')
        print(f"  - 컬럼: {columns}")

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
        """브랜드 카테고리 뷰 생성"""
        print(f"\n[브랜드 카테고리 뷰] 브랜드 카테고리 뷰 생성 중...")

        if not self._table_exists('brand_category_view'):
            print("  - brand_category_view 테이블이 없습니다. 스킵합니다.")
            return

        columns = self._get_table_columns('brand_category_view')
        print(f"  - 컬럼: {columns}")

        with self.connection.cursor() as cursor:
            # brand_category 또는 brand에서 ID 가져오기
            category_ids = []
            if self._table_exists('brand_category'):
                cursor.execute("SELECT id FROM brand_category")
                category_ids = [row['id'] for row in cursor.fetchall()]

            # category가 없으면 brand를 사용
            brand_ids = []
            if not category_ids:
                cursor.execute("SELECT id FROM brand")
                brand_ids = [row['id'] for row in cursor.fetchall()]

            cursor.execute("SELECT id FROM users WHERE role = 'CREATOR'")
            creator_ids = [row['id'] for row in cursor.fetchall()]

        source_ids = category_ids if category_ids else brand_ids
        if not source_ids or not creator_ids:
            print("[경고] 브랜드 카테고리/브랜드 또는 크리에이터가 없습니다.")
            return

        views = []
        for creator_id in creator_ids:
            viewed_items = self.fake.random_sample(source_ids, length=self.fake.random_int(1, min(5, len(source_ids))))
            for item_id in viewed_items:
                data = {}
                if 'brand_category_id' in columns:
                    data['brand_category_id'] = item_id
                if 'category_id' in columns:
                    data['category_id'] = item_id
                if 'brand_id' in columns:
                    data['brand_id'] = item_id
                if 'user_id' in columns:
                    data['user_id'] = creator_id
                if 'viewed_at' in columns:
                    data['viewed_at'] = datetime.now() - timedelta(days=self.fake.random_int(0, 30))
                if 'is_deleted' in columns:
                    data['is_deleted'] = False
                if 'created_at' in columns:
                    data['created_at'] = datetime.now()
                if 'updated_at' in columns:
                    data['updated_at'] = datetime.now()
                if data:
                    views.append(data)

        if views:
            cols = list(views[0].keys())
            col_names = ', '.join(cols)
            placeholders = ', '.join([f'%({c})s' for c in cols])
            sql = f"INSERT IGNORE INTO brand_category_view ({col_names}) VALUES ({placeholders})"
            self.execute_many(sql, views, "브랜드 카테고리 뷰")
        else:
            print("  - 생성할 데이터가 없습니다.")

    def generate_brand_like_reads(self):
        """브랜드 좋아요 읽음 상태 생성"""
        print(f"\n[브랜드 좋아요 읽음] 브랜드 좋아요 읽음 상태 생성 중...")

        if not self._table_exists('brand_like_read'):
            print("  - brand_like_read 테이블이 없습니다. 스킵합니다.")
            return

        columns = self._get_table_columns('brand_like_read')
        print(f"  - 컬럼: {columns}")

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

    def generate_campaign_applies(self, applies_per_campaign=3):
        """캠페인 지원 생성"""
        print(f"\n[캠페인 지원] 캠페인 지원 생성 중...")

        if not self._table_exists('campaign_apply'):
            print("  - campaign_apply 테이블이 없습니다. 스킵합니다.")
            return

        columns = self._get_table_columns('campaign_apply')

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM campaign")
            campaign_ids = [row['id'] for row in cursor.fetchall()]

            cursor.execute("SELECT id FROM users WHERE role = 'CREATOR'")
            creator_ids = [row['id'] for row in cursor.fetchall()]

        if not campaign_ids or not creator_ids:
            print("[경고] 캠페인 또는 크리에이터가 없습니다.")
            return

        # ENUM 값 동적 조회
        statuses = self._get_enum_values('campaign_apply', 'status')
        if not statuses:
            statuses = ['PENDING']
        applies = []
        for campaign_id in campaign_ids:
            applicants = self.fake.random_sample(creator_ids, length=min(applies_per_campaign, len(creator_ids)))
            for creator_id in applicants:
                data = {}
                if 'campaign_id' in columns:
                    data['campaign_id'] = campaign_id
                if 'user_id' in columns:
                    data['user_id'] = creator_id
                if 'creator_id' in columns:
                    data['creator_id'] = creator_id
                if 'status' in columns:
                    data['status'] = self.fake.random_element(statuses)
                if 'message' in columns:
                    data['message'] = self.fake.text(max_nb_chars=200)
                if 'created_at' in columns:
                    data['created_at'] = datetime.now() - timedelta(days=self.fake.random_int(0, 20))
                if 'updated_at' in columns:
                    data['updated_at'] = datetime.now() - timedelta(days=self.fake.random_int(0, 5))
                if data:
                    applies.append(data)

        if applies:
            cols = list(applies[0].keys())
            col_names = ', '.join(cols)
            placeholders = ', '.join([f'%({c})s' for c in cols])
            sql = f"INSERT IGNORE INTO campaign_apply ({col_names}) VALUES ({placeholders})"
            self.execute_many(sql, applies, "캠페인 지원")

    def generate_campaign_tags(self):
        """캠페인 태그 생성 (content_tag, fashion_tag)"""
        print(f"\n[캠페인 태그] 캠페인 태그 생성 중...")

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM campaign")
            campaign_ids = [row['id'] for row in cursor.fetchall()]

            cursor.execute("SELECT id, tag_type FROM tag")
            tags = cursor.fetchall()

        if not campaign_ids or not tags:
            print("[경고] 캠페인 또는 태그가 없습니다.")
            return

        content_tags = [t['id'] for t in tags if t['tag_type'] == '콘텐츠']
        fashion_tags = [t['id'] for t in tags if t['tag_type'] == '패션']

        # 캠페인 콘텐츠 태그
        if self._table_exists('campaign_content_tag') and content_tags:
            columns = self._get_table_columns('campaign_content_tag')
            campaign_content_tags = []
            for campaign_id in campaign_ids:
                selected = self.fake.random_sample(content_tags, length=self.fake.random_int(2, min(5, len(content_tags))))
                for tag_id in selected:
                    data = {}
                    if 'campaign_id' in columns:
                        data['campaign_id'] = campaign_id
                    if 'tag_id' in columns:
                        data['tag_id'] = tag_id
                    if 'created_at' in columns:
                        data['created_at'] = datetime.now()
                    if 'updated_at' in columns:
                        data['updated_at'] = datetime.now()
                    if data:
                        campaign_content_tags.append(data)

            if campaign_content_tags:
                cols = list(campaign_content_tags[0].keys())
                col_names = ', '.join(cols)
                placeholders = ', '.join([f'%({c})s' for c in cols])
                sql = f"INSERT IGNORE INTO campaign_content_tag ({col_names}) VALUES ({placeholders})"
                self.execute_many(sql, campaign_content_tags, "캠페인 콘텐츠 태그")
        else:
            print("  - campaign_content_tag 테이블이 없거나 콘텐츠 태그가 없습니다.")

        # 캠페인 패션 태그
        if self._table_exists('campaign_fashion_tag') and fashion_tags:
            columns = self._get_table_columns('campaign_fashion_tag')
            campaign_fashion_tags = []
            for campaign_id in campaign_ids:
                selected = self.fake.random_sample(fashion_tags, length=self.fake.random_int(2, min(5, len(fashion_tags))))
                for tag_id in selected:
                    data = {}
                    if 'campaign_id' in columns:
                        data['campaign_id'] = campaign_id
                    if 'tag_id' in columns:
                        data['tag_id'] = tag_id
                    if 'created_at' in columns:
                        data['created_at'] = datetime.now()
                    if 'updated_at' in columns:
                        data['updated_at'] = datetime.now()
                    if data:
                        campaign_fashion_tags.append(data)

            if campaign_fashion_tags:
                cols = list(campaign_fashion_tags[0].keys())
                col_names = ', '.join(cols)
                placeholders = ', '.join([f'%({c})s' for c in cols])
                sql = f"INSERT IGNORE INTO campaign_fashion_tag ({col_names}) VALUES ({placeholders})"
                self.execute_many(sql, campaign_fashion_tags, "캠페인 패션 태그")
        else:
            print("  - campaign_fashion_tag 테이블이 없거나 패션 태그가 없습니다.")

    def generate_campaign_like_reads(self):
        """캠페인 좋아요 읽음 상태 생성"""
        print(f"\n[캠페인 좋아요 읽음] 캠페인 좋아요 읽음 상태 생성 중...")

        if not self._table_exists('campaign_like_read'):
            print("  - campaign_like_read 테이블이 없습니다. 스킵합니다.")
            return

        columns = self._get_table_columns('campaign_like_read')

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM campaign_like")
            like_ids = [row['id'] for row in cursor.fetchall()]

        if not like_ids:
            print("[경고] 캠페인 좋아요가 없습니다.")
            return

        reads = []
        for like_id in like_ids:
            if self.fake.boolean(chance_of_getting_true=70):
                data = {}
                if 'campaign_like_id' in columns:
                    data['campaign_like_id'] = like_id
                if 'like_id' in columns:
                    data['like_id'] = like_id
                if 'is_read' in columns:
                    data['is_read'] = True
                if 'read_at' in columns:
                    data['read_at'] = datetime.now() - timedelta(days=self.fake.random_int(0, 10))
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
            sql = f"INSERT IGNORE INTO campaign_like_read ({col_names}) VALUES ({placeholders})"
            self.execute_many(sql, reads, "캠페인 좋아요 읽음")

    def generate_all(self, applies_per_campaign=3):
        # 브랜드 관련
        self.generate_brand_categories()
        self.generate_brand_available_sponsors()
        self.generate_brand_sponsor_images()
        self.generate_brand_category_views()
        self.generate_brand_like_reads()

        # 캠페인 관련
        self.generate_campaign_applies(applies_per_campaign)
        self.generate_campaign_tags()
        self.generate_campaign_like_reads()
