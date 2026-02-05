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
        print(f"[{table_name}] is creating...")

        try:
            with self.connection.cursor() as cursor:
                cursor.execute(f"SHOW TABLES LIKE '{table_name}'")
                return cursor.fetchone() is not None
        except Exception:
            print(f"[에러] {table_name} 이 없습니다")
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

    def generate_campaign_applies(self, applies_per_campaign=3):

        if not self._table_exists('campaign_apply'):
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

        if not self._table_exists('campaign_like_read'):
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

    def generate_campaign_proposals(self):

        if not self._table_exists('campaign_proposal'):
            return

        columns = self._get_table_columns('campaign_proposal')

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id, brand_id FROM campaign")
            campaigns = cursor.fetchall()

            cursor.execute("SELECT id FROM users WHERE role = 'CREATOR'")
            creator_ids = [row['id'] for row in cursor.fetchall()]

        if not campaigns or not creator_ids:
            print("[경고] 캠페인 또는 크리에이터가 없습니다.")
            return

        # ENUM 값 동적 조회
        statuses = self._get_enum_values('campaign_proposal', 'status')
        if not statuses:
            statuses = ['PENDING']
        proposals = []
        for campaign in campaigns:
            if self.fake.boolean(chance_of_getting_true=50):
                num_proposals = self.fake.random_int(1, 3)
                selected_creators = self.fake.random_sample(creator_ids, length=min(num_proposals, len(creator_ids)))
                for creator_id in selected_creators:
                    data = {}
                    if 'campaign_id' in columns:
                        data['campaign_id'] = campaign['id']
                    if 'brand_id' in columns:
                        data['brand_id'] = campaign['brand_id']
                    if 'creator_id' in columns:
                        data['creator_id'] = creator_id
                    if 'user_id' in columns:
                        data['user_id'] = creator_id
                    if 'status' in columns:
                        data['status'] = self.fake.random_element(statuses)
                    if 'message' in columns:
                        data['message'] = self.fake.text(max_nb_chars=300)
                    if 'created_at' in columns:
                        data['created_at'] = datetime.now() - timedelta(days=self.fake.random_int(0, 30))
                    if 'updated_at' in columns:
                        data['updated_at'] = datetime.now() - timedelta(days=self.fake.random_int(0, 10))
                    if data:
                        proposals.append(data)

        if proposals:
            cols = list(proposals[0].keys())
            col_names = ', '.join(cols)
            placeholders = ', '.join([f'%({c})s' for c in cols])
            sql = f"INSERT IGNORE INTO campaign_proposal ({col_names}) VALUES ({placeholders})"
            self.execute_many(sql, proposals, "캠페인 제안")

    def generate_campaign_proposal_content_tags(self):

        if not self._table_exists('campaign_proposal_content_tag'):
            return

        if not self._table_exists('campaign_proposal'):
            return

        columns = self._get_table_columns('campaign_proposal_content_tag')

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM campaign_proposal")
            proposal_ids = [row['id'] for row in cursor.fetchall()]

            cursor.execute("SELECT id FROM tag WHERE tag_type = '콘텐츠'")
            content_tag_ids = [row['id'] for row in cursor.fetchall()]

        if not proposal_ids or not content_tag_ids:
            print("[경고] 캠페인 제안 또는 콘텐츠 태그가 없습니다.")
            return

        proposal_tags = []
        for proposal_id in proposal_ids:
            selected = self.fake.random_sample(content_tag_ids, length=self.fake.random_int(1, min(3, len(content_tag_ids))))
            for tag_id in selected:
                data = {}
                if 'campaign_proposal_id' in columns:
                    data['campaign_proposal_id'] = proposal_id
                if 'proposal_id' in columns:
                    data['proposal_id'] = proposal_id
                if 'tag_id' in columns:
                    data['tag_id'] = tag_id
                if 'created_at' in columns:
                    data['created_at'] = datetime.now()
                if 'updated_at' in columns:
                    data['updated_at'] = datetime.now()
                if data:
                    proposal_tags.append(data)

        if proposal_tags:
            cols = list(proposal_tags[0].keys())
            col_names = ', '.join(cols)
            placeholders = ', '.join([f'%({c})s' for c in cols])
            sql = f"INSERT IGNORE INTO campaign_proposal_content_tag ({col_names}) VALUES ({placeholders})"
            self.execute_many(sql, proposal_tags, "캠페인 제안 콘텐츠 태그")

    def _run_generator(self, name, func):
        try:
            func()
            print(f"{name} 완료")
        except Exception as e:
            print(f"{name} 실패: {e}")

    def generate_all(self, applies_per_campaign=3):
        self._run_generator("캠페인 지원", lambda: self.generate_campaign_applies(applies_per_campaign))
        self._run_generator("캠페인 태그", self.generate_campaign_tags)
        self._run_generator("캠페인 좋아요 읽음", self.generate_campaign_like_reads)
        # self._run_generator("캠페인 제안", self.generate_campaign_proposals)
        self._run_generator("캠페인 제안 콘텐츠 태그", self.generate_campaign_proposal_content_tags)