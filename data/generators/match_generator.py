from datetime import datetime, timedelta
from .base_generator import BaseGenerator


class MatchGenerator(BaseGenerator):

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

    def generate_all(self):
        self.generate_match_brand_history()
        self.generate_match_campaign_history()
