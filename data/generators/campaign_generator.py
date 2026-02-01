from datetime import datetime, timedelta
from .base_generator import BaseGenerator


class CampaignGenerator(BaseGenerator):
    def generate_campaigns(self, count=30):
        print(f"\n[캠페인] {count}개의 캠페인 생성 중...")

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM users WHERE role = 'BRAND'")
            user_ids = [row['id'] for row in cursor.fetchall()]

            cursor.execute("SELECT id FROM brand")
            brand_ids = [row['id'] for row in cursor.fetchall()]

        if not user_ids:
            print("[경고] BRAND 역할의 사용자가 없습니다.")
            return

        if not brand_ids:
            print("[경고] 브랜드가 없습니다.")
            return

        campaigns = []
        for i in range(count):
            start_date = self.fake.date_between(start_date='today', end_date='+60d')
            end_date = start_date + timedelta(days=self.fake.random_int(30, 90))
            recruit_start = datetime.now() - timedelta(days=self.fake.random_int(0, 10))
            recruit_end = recruit_start + timedelta(days=self.fake.random_int(14, 30))

            campaign = {
                'title': self.fake.sentence(nb_words=6).rstrip('.'),
                'description': self.fake.text(max_nb_chars=500),
                'preferred_skills': ', '.join([self.fake.word() for _ in range(3)]),
                'schedule': f"주 {self.fake.random_int(1, 3)}회 콘텐츠 제작 및 업로드",
                'video_spec': f"{self.fake.random_element(['세로형', '가로형'])} {self.fake.random_element(['30초', '1분', '3분'])} 영상",
                'product': self.fake.sentence(nb_words=4).rstrip('.'),
                'reward_amount': self.fake.random_element([500000, 1000000, 1500000, 2000000, 3000000]),
                'start_date': start_date,
                'end_date': end_date,
                'recruit_start_date': recruit_start,
                'recruit_end_date': recruit_end,
                'quota': self.fake.random_int(5, 30),
                'brand_id': self.fake.random_element(brand_ids),
                'created_by': self.fake.random_element(user_ids),
                'is_deleted': False,
                'created_at': datetime.now() - timedelta(days=self.fake.random_int(10, 60)),
                'updated_at': datetime.now() - timedelta(days=self.fake.random_int(0, 10))
            }
            campaigns.append(campaign)

        sql = """
            INSERT INTO campaign (title, description, preferred_skills, schedule,
                                  video_spec, product, reward_amount, start_date,
                                  end_date, recruit_start_date, recruit_end_date,
                                  quota, brand_id, created_by, is_deleted, created_at, updated_at)
            VALUES (%(title)s, %(description)s, %(preferred_skills)s, %(schedule)s,
                    %(video_spec)s, %(product)s, %(reward_amount)s, %(start_date)s,
                    %(end_date)s, %(recruit_start_date)s, %(recruit_end_date)s,
                    %(quota)s, %(brand_id)s, %(created_by)s, %(is_deleted)s, %(created_at)s, %(updated_at)s)
        """
        self.execute_many(sql, campaigns, "캠페인")

    def generate_campaign_likes(self):
        print(f"\n[캠페인 좋아요] 캠페인 좋아요 생성 중...")

        with self.connection.cursor() as cursor:
            cursor.execute("SELECT id FROM campaign")
            campaign_ids = [row['id'] for row in cursor.fetchall()]

            cursor.execute("SELECT id FROM users WHERE role = 'CREATOR'")
            creator_ids = [row['id'] for row in cursor.fetchall()]

        if not campaign_ids or not creator_ids:
            print("[경고] 캠페인 또는 크리에이터가 없습니다.")
            return

        likes = []
        for creator_id in creator_ids:
            liked_campaigns = self.fake.random_sample(campaign_ids, length=self.fake.random_int(0, min(5, len(campaign_ids))))
            for campaign_id in liked_campaigns:
                likes.append({
                    'user_id': creator_id,
                    'campaign_id': campaign_id,
                    'created_at': datetime.now() - timedelta(days=self.fake.random_int(0, 30))
                })

        if likes:
            sql = """
                INSERT INTO campaign_like (user_id, campaign_id, created_at)
                VALUES (%(user_id)s, %(campaign_id)s, %(created_at)s)
            """
            self.execute_many(sql, likes, "캠페인 좋아요")

    def generate_all(self, campaign_count=30):
        self.generate_campaigns(campaign_count)
        self.generate_campaign_likes()
